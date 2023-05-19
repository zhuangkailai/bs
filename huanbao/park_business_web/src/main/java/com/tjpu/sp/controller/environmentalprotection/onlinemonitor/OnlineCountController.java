package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.alibaba.fastjson.JSON;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.SessionUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.base.mn.AlarmMNService;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DeviceDevOpsInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.ContinuousExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: zhangzhenchao
 * @date: 2019/10/25 10:57
 * @Description: 统计报警次数
 */
@RestController
@ControllerAdvice
@RequestMapping("onlineCountController")
public class OnlineCountController {
    private final OnlineCountAlarmService onlineCountAlarmService;
    private final OnlineService onlineService;
    private final AlarmMNService alarmMNService;
    private final PollutantService pollutantService;
    private final CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private DeviceDevOpsInfoService deviceDevOpsInfoService;
    @Autowired
    private StopProductionInfoService stopProductionInfoService;
    @Autowired
    private MonitorControlService monitorControlService;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private WaterOutPutPollutantSetService waterOutPutPollutantSetService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    private final String DB_OverData = "OverData";
    private final String DB_ExceptionData = "ExceptionData";

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    public OnlineCountController(OnlineCountAlarmService onlineCountAlarmService, OnlineService onlineService, AlarmMNService alarmMNService, PollutantService pollutantService, CheckProblemExpoundService checkProblemExpoundService) {
        this.onlineCountAlarmService = onlineCountAlarmService;
        this.onlineService = onlineService;
        this.alarmMNService = alarmMNService;
        this.pollutantService = pollutantService;
        this.checkProblemExpoundService = checkProblemExpoundService;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/13 11:19
     * @Description: 获得监控预警下各个模块当天的预警次数
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorAlarmNumForMenusByMenuID", method = RequestMethod.POST)
    public Object getMonitorAlarmNumForMenusByMenuID(@RequestJson(value = "menuid") String menuid,
                                                     @RequestJson(value = "usercode", required = false) String userCode,
                                                     @RequestJson(value = "userauth", required = false) List<JSONObject> objectList) {
        try {
            if (userCode == null) {
                userCode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            }
            if (objectList == null) {
                objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            }

            Assert.notNull(userCode, "userCode must not be null!");
            return AuthUtil.parseJsonKeyToLower("success", getMonitorAlarmNumBySessionIDAndMenuID(objectList, userCode, menuid));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/5 0005 下午 1:50
     * @Description: 统计当天报警数据，用于app菜单展示
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmDataForAppMenus", method = RequestMethod.POST)
    public Object countAlarmDataForAppMenus(
            @RequestJson(value = "menuid") String menuid,
            @RequestJson(value = "nowday", required = false) String nowday,
            @RequestJson(value = "userauth", required = false) List<JSONObject> objectList) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String nowTime = DataFormatUtil.getDateYMD(new Date());
            if (StringUtils.isNotBlank(nowday)) {
                nowTime = nowday;
            }
            boolean isHaveData = false;
            if (objectList == null) {
                isHaveData = true;

                objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            }
            //根据菜单权限获取，监测点类型数组
            if (objectList != null) {
                JSONArray childrenList;
                JSONObject jsonObject;
                Map<String, Object> codeAndName = new LinkedHashMap<>();
                for (JSONObject jsonIndex : objectList) {
                    if (jsonIndex.get("menuid").equals("app10")) {
                        childrenList = jsonIndex.getJSONArray("datalistchildren");
                        if (childrenList != null) {
                            for (int i = 0; i < childrenList.size(); i++) {
                                jsonObject = childrenList.getJSONObject(i);
                                if (menuid.equals(jsonObject.get("menuid"))) {
                                    childrenList = jsonObject.getJSONArray("datalistchildren");
                                    for (int j = 0; j < childrenList.size(); j++) {
                                        jsonObject = childrenList.getJSONObject(j);
                                        codeAndName.put(jsonObject.getString("menucode"), jsonObject.getString("menuname"));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (codeAndName.size() > 0) {
                    Set<String> appSysmodel = new HashSet<>(codeAndName.keySet());
                    List<Integer> monitorpointtypes = new ArrayList<>();
                    List<Integer> types;
                    Map<Integer, String> typeAndSysmodel = new HashMap<>();
                    for (String sysmodel : appSysmodel) {
                        types = CommonTypeEnum.AppSysModelAndType.getTypesBySysmodel(sysmodel);
                        if (types != null) {
                            monitorpointtypes.addAll(types);
                            for (Integer typeIndex : types) {
                                typeAndSysmodel.put(typeIndex, sysmodel);
                            }
                        }
                    }
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("starttime", nowTime);
                    paramMap.put("endtime", nowTime);
                    paramMap.put("monitorpointtypes", monitorpointtypes);
                    List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
                    String mnCommon;
                    int type;
                    Map<String, Integer> mnAndType = new HashMap<>();
                    List<String> mns = new ArrayList<>();
                    for (Map<String, Object> mapIndex : deviceStatus) {
                        mnCommon = mapIndex.get("dgimn").toString();
                        type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                        mnAndType.put(mnCommon, type);
                        mns.add(mnCommon);
                    }
                    paramMap.put("mns", mns);
                    List<Map<String, Map<String, Object>>> mnAndAlarmDataList = getMnAndAlarmEarlyData(paramMap);
                    Map<Integer, Map<String, List<String>>> typeAndRemindAndMns = setTypeAndRemindAndMns(mnAndAlarmDataList, mnAndType);
                    Map<String, Map<String, Integer>> sysmodelAndTypeAndNum = new HashMap<>();
                    if (typeAndRemindAndMns.size() > 0) {
                        Map<String, Map<String, List<String>>> sysmodelAndTypeAndMns = new HashMap<>();
                        String sysmodel;
                        Map<String, List<String>> typeAndMns;
                        for (Integer typeIndex : typeAndRemindAndMns.keySet()) {
                            sysmodel = typeAndSysmodel.get(typeIndex);
                            if (sysmodelAndTypeAndMns.containsKey(sysmodel)) {
                                typeAndMns = sysmodelAndTypeAndMns.get(sysmodel);
                            } else {
                                typeAndMns = new HashMap<>();
                                typeAndMns.put("alarmdata", Arrays.asList());
                                typeAndMns.put("earlydata", Arrays.asList());
                            }
                            setAlarmData(typeAndMns, typeAndRemindAndMns.get(typeIndex));
                            sysmodelAndTypeAndMns.put(sysmodel, typeAndMns);
                        }

                        for (String sysmodelIndex : sysmodelAndTypeAndMns.keySet()) {
                            typeAndMns = sysmodelAndTypeAndMns.get(sysmodelIndex);
                            Map<String, Integer> typeAndNum = new HashMap<>();
                            for (String typeIndex : typeAndMns.keySet()) {
                                mns = typeAndMns.get(typeIndex).stream().distinct().collect(Collectors.toList());
                                typeAndNum.put(typeIndex, mns.size());
                            }
                            sysmodelAndTypeAndNum.put(sysmodelIndex, typeAndNum);
                        }
                    }
                    if (appSysmodel.contains("yqqy")) {
                        String sysmodel = "yqqy";
                        Map<String, String> allMnAndPollutionId = new HashMap<>();
                        List<String> outputIds = new ArrayList<>();
                        //报警企业提醒
                        for (Integer typeIndex : monitorpointtypes) {
                            allMnAndPollutionId.putAll(onlineService.getMNAndPId(outputIds, typeIndex));
                        }
                        //获取报警企业数，预警企业数
                        Map<String, Integer> pollutionAndRemindAndNum = setPollutionAndRemindAndNum(mnAndAlarmDataList, allMnAndPollutionId);
                        sysmodelAndTypeAndNum.put(sysmodel, pollutionAndRemindAndNum);
                    }
                    for (String sysmodeIndex : codeAndName.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("sysmodel", sysmodeIndex);
                        resultMap.put("name", codeAndName.get(sysmodeIndex));
                        if (sysmodelAndTypeAndNum.containsKey(sysmodeIndex)) {
                            resultMap.putAll(sysmodelAndTypeAndNum.get(sysmodeIndex));
                        } else {
                            resultMap.put("alarmdata", 0);
                            resultMap.put("earlydata", 0);
                        }
                        resultList.add(resultMap);
                    }
                }
            }
            if (isHaveData) {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            } else {
                return resultList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 上午 9:12
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> setPollutionAndRemindAndNum(List<Map<String, Map<String, Object>>> mnAndAlarmDataList, Map<String, String> allMnAndPollutionId) {
        Map<String, Object> alarmData;
        Integer remindType;
        String remindCode;
        Map<String, Set<String>> remindCodeAndPollutionIds = new HashMap<>();
        Set<String> pollutionIds;
        String pollutionId;
        Map<String, Map<String, Object>> mnAndAlarmData;
        for (int i = 0; i < mnAndAlarmDataList.size(); i++) {
            mnAndAlarmData = mnAndAlarmDataList.get(i);
            for (String mnIndex : mnAndAlarmData.keySet()) {
                alarmData = mnAndAlarmData.get(mnIndex);
                pollutionId = allMnAndPollutionId.get(mnIndex);
                remindType = Integer.parseInt(alarmData.get("remindcode").toString());
                remindCode = CommonTypeEnum.alarmTypeAndRemindType.getAlarmByType(remindType);
                if (remindCodeAndPollutionIds.containsKey(remindCode)) {
                    pollutionIds = remindCodeAndPollutionIds.get(remindCode);
                } else {
                    pollutionIds = new HashSet<>();
                }
                pollutionIds.add(pollutionId);
                remindCodeAndPollutionIds.put(remindCode, pollutionIds);
            }
        }
        Map<String, Integer> resultMap = new HashMap<>();
        pollutionIds = remindCodeAndPollutionIds.get("alarmdata");
        resultMap.put("alarmdata", pollutionIds != null ? pollutionIds.size() : 0);
        pollutionIds = remindCodeAndPollutionIds.get("earlydata");
        resultMap.put("earlydata", pollutionIds != null ? pollutionIds.size() : 0);
        return resultMap;
    }

    /**
     * @author: lip
     * @date: 2019/11/5 0005 下午 6:20
     * @Description: 设置提醒类型和数量关联关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setAlarmData(Map<String, List<String>> resultMap, Map<String, List<String>> remindAndMns) {
        if (remindAndMns != null) {
            List<String> mns;
            List<String> subMns;
            int remindType;
            String alarmType;
            for (String remindIndex : remindAndMns.keySet()) {
                remindType = Integer.parseInt(remindIndex);
                alarmType = CommonTypeEnum.alarmTypeAndRemindType.getAlarmByType(remindType);
                mns = remindAndMns.get(remindIndex);
                if (alarmType.equals("alarmdata")) {
                    subMns = resultMap.get("alarmdata");
                    mns.addAll(subMns);
                    resultMap.put("alarmdata", mns);
                } else if (alarmType.equals("earlydata")) {
                    subMns = resultMap.get("earlydata");
                    mns.addAll(subMns);
                    resultMap.put("earlydata", mns);
                }
            }
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/5 0005 下午 4:15
     * @Description: 组装监测点类型和提醒类型报警统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<Integer, Map<String, List<String>>> setTypeAndRemindAndMns(List<Map<String, Map<String, Object>>> mnAndAlarmDataList,
                                                                           Map<String, Integer> mnAndType) {
        Map<Integer, Map<String, List<String>>> typeAndRemindAndMns = new HashMap<>();
        int type;
        if (mnAndAlarmDataList.size() > 0) {
            Map<String, List<String>> remindAndMns;
            List<String> mns;
            String remindCode;
            Map<String, Map<String, Object>> mnAndAlarmData;
            for (int i = 0; i < mnAndAlarmDataList.size(); i++) {
                mnAndAlarmData = mnAndAlarmDataList.get(i);
                for (String mnIndex : mnAndAlarmData.keySet()) {
                    remindCode = mnAndAlarmData.get(mnIndex).get("remindcode").toString();
                    type = mnAndType.get(mnIndex);
                    if (typeAndRemindAndMns.containsKey(type)) {
                        remindAndMns = typeAndRemindAndMns.get(type);
                    } else {
                        remindAndMns = new HashMap<>();
                    }
                    if (remindAndMns.containsKey(remindCode)) {
                        mns = remindAndMns.get(remindCode);
                    } else {
                        mns = new ArrayList<>();
                    }
                    mns.add(mnIndex);
                    remindAndMns.put(remindCode, mns);
                    typeAndRemindAndMns.put(type, remindAndMns);
                }
            }
        }
        return typeAndRemindAndMns;
    }


    /**
     * @author: lip
     * @date: 2019/11/2 0002 上午 11:28
     * @Description: 根据监测点类型统计当天报警点位数据(分级预警 、 gis端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countTodayAlarmDataByMonitorType", method = RequestMethod.POST)
    public Object countTodayAlarmDataByMonitorType(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                   @RequestJson(value = "nowday", required = false) String nowday) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", nowday);
            paramMap.put("endtime", nowday);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            Map<Integer, Set<String>> typeAndMns = new HashMap<>();
            Map<Integer, List<Map<String, Object>>> typeAndAlarmDataList = getTypeAndAlarmDataByParam(paramMap, typeAndMns);
            List<Map<String, Object>> resultList = new ArrayList<>();
            String monitorpointtypename;
            for (Integer typeIndex : monitorpointtypes) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitorpointtype", typeIndex);
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(typeIndex).replace("点类型", "");
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("count", typeAndMns.get(typeIndex) != null ? typeAndMns.get(typeIndex).size() : 0);
                resultMap.put("data", typeAndAlarmDataList.get(typeIndex));
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/2 0002 上午 11:46
     * @Description: 组装mn号和报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setMnAndAlarmData(Map<String, Map<String, Object>> mnAndAlarmData,
                                   List<Document> documents,
                                   CommonTypeEnum.RemindTypeEnum enumData) {
        String remindname = enumData.getName();
        Integer remindcode = enumData.getCode();
        String mnCommon;
        Integer countnum;
        Integer sumCount;
        Integer subCountNum;
        Map<String, Object> alarmData;
        for (Document document : documents) {
            mnCommon = document.getString("_id");
            countnum = document.getInteger("countnum");
            if (mnAndAlarmData.containsKey(mnCommon)) {
                alarmData = mnAndAlarmData.get(mnCommon);
            } else {
                alarmData = new HashMap<>();
            }
            subCountNum = alarmData.get("num") != null ? Integer.parseInt(alarmData.get("num").toString()) : 0;
            alarmData.put("remindcode", remindcode);
            alarmData.put("remindname", remindname);
            alarmData.put("num", subCountNum + countnum);
            alarmData.put("count", 1);
            mnAndAlarmData.put(mnCommon, alarmData);
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/2 0002 上午 11:46
     * @Description: 组装mn号和报警详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setMnAndAlarmDetailData(Map<String, List<Map<String, Object>>> mnAndAlarmDataList,
                                         List<Document> documents,
                                         CommonTypeEnum.RemindTypeEnum enumData) {
        String remindname = enumData.getName();
        Integer remindcode = enumData.getCode();
        String mnCommon;
        int countnum;
        List<Map<String, Object>> alarmDataList;
        for (Document document : documents) {
            mnCommon = document.getString("_id");
            countnum = document.getInteger("countnum");
            if (mnAndAlarmDataList.containsKey(mnCommon)) {
                alarmDataList = mnAndAlarmDataList.get(mnCommon);
            } else {
                alarmDataList = new ArrayList<>();
            }
            Map<String, Object> alarmData = new HashMap<>();
            alarmData.put("remindcode", remindcode);
            alarmData.put("remindname", remindname);
            alarmData.put("countnum", countnum);
            alarmDataList.add(alarmData);
            mnAndAlarmDataList.put(mnCommon, alarmDataList);
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/20 19:32
     * @Description: 获取各个监测类型报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countMonitorTypeAlarmNum", method = RequestMethod.POST)
    public Object countMonitorTypeAlarmNum(@RequestJson(value = "monitortypes") List<Integer> monitortypes,
                                           @RequestJson(value = "starttime") String starttime,
                                           @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointtypes", monitortypes);
            Map<Integer, Set<String>> typeAndMns = new HashMap<>();
            Map<Integer, List<Map<String, Object>>> typeAndAlarmDataList = getTypeAndAlarmDataByParam(paramMap, typeAndMns);
            List<Map<String, Object>> resultList = new ArrayList<>();
            String monitorpointtypename;
            for (Integer typeIndex : monitortypes) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitorpointtype", typeIndex);
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(typeIndex).replace("点类型", "");
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("num", typeAndMns.get(typeIndex) != null ? typeAndMns.get(typeIndex).size() : 0);
                resultMap.put("data", typeAndAlarmDataList.get(typeIndex));
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/4 0004 下午 2:28
     * @Description: 获取各个监测点类型对应的报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<Integer, List<Map<String, Object>>> getTypeAndAlarmDataByParam(Map<String, Object> paramMap, Map<Integer, Set<String>> typeAndMns) {
        Map<Integer, List<Map<String, Object>>> typeAndAlarmDataList = new HashMap<>();
        String sessionID = SessionUtil.getSessionID();
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("userid", userid);

        List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
        String mnCommon;
        int type;
        Map<String, Integer> mnAndType = new HashMap<>();
        List<String> mns = new ArrayList<>();
        for (Map<String, Object> mapIndex : deviceStatus) {
            mnCommon = mapIndex.get("dgimn").toString();
            type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
            mnAndType.put(mnCommon, type);
            mns.add(mnCommon);
        }

        paramMap.put("mns", mns);
        List<Map<String, Map<String, Object>>> mnAndAlarmDataList = getMnAndAlarmEarlyData(paramMap);
        if (mnAndAlarmDataList.size() > 0) {
            Map<Integer, List<Integer>> typeAndRemind = new HashMap<>();
            List<Integer> remindTypes;
            List<Map<String, Object>> alarmDataList;
            Integer remindCode;
            Set<String> mnset;

            Map<String, Map<String, Object>> mnAndAlarmData;
            for (int i = 0; i < mnAndAlarmDataList.size(); i++) {
                mnAndAlarmData = mnAndAlarmDataList.get(i);
                for (String mnIndex : mnAndAlarmData.keySet()) {
                    remindCode = Integer.parseInt(mnAndAlarmData.get(mnIndex).get("remindcode").toString());
                    type = mnAndType.get(mnIndex);
                    if (typeAndRemind.containsKey(type)) {
                        remindTypes = typeAndRemind.get(type);
                        mnset = typeAndMns.get(type);

                    } else {
                        remindTypes = new ArrayList<>();
                        mnset = new HashSet<>();

                    }
                    mnset.add(mnIndex);
                    typeAndMns.put(type, mnset);
                    if (!remindTypes.contains(remindCode)) {
                        if (typeAndAlarmDataList.containsKey(type)) {
                            alarmDataList = typeAndAlarmDataList.get(type);
                        } else {
                            alarmDataList = new ArrayList<>();
                        }
                        alarmDataList.add(mnAndAlarmData.get(mnIndex));
                        typeAndAlarmDataList.put(type, alarmDataList);
                        remindTypes.add(remindCode);
                        typeAndRemind.put(type, remindTypes);
                    }
                }
            }


        }

        return typeAndAlarmDataList;
    }

    /**
     * @author: lip
     * @date: 2019/11/5 0005 下午 4:07
     * @Description: 获取mn和报警数据关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, List<Map<String, Object>>> getEarlyAndAlarmDetailData(Map<String, Object> paramMap) {
        Map<String, List<Map<String, Object>>> mnAndAlarmDataList = new HashMap<>();
        String startTime = paramMap.get("starttime") + " 00:00:00";
        String endTime = paramMap.get("endtime") + " 23:59:59";
        paramMap.put("starttime", startTime);
        paramMap.put("endtime", endTime);
        getEarlyDetailData(paramMap, mnAndAlarmDataList);
        getAlarmDetailData(paramMap, mnAndAlarmDataList);
        return mnAndAlarmDataList;
    }

    /**
     * @author: lip
     * @date: 2019/11/5 0005 下午 4:07
     * @Description: 获取mn和报警数据关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Map<String, Object>>> getMnAndAlarmEarlyData(Map<String, Object> paramMap) {
        List<Map<String, Map<String, Object>>> mnAndDataList = new ArrayList<>();
        String startTime = paramMap.get("starttime") + " 00:00:00";
        String endTime = paramMap.get("endtime") + " 23:59:59";
        paramMap.put("starttime", startTime);
        paramMap.put("endtime", endTime);
        List<Map<String, Map<String, Object>>> mnAndAlarmData = getMnAndAlarmDataList(paramMap);
        List<Map<String, Map<String, Object>>> mnAndEarlyData = getMnAndEarlyDataList(paramMap);
        mnAndDataList.addAll(mnAndAlarmData);
        mnAndDataList.addAll(mnAndEarlyData);
        return mnAndDataList;
    }

    /**
     * @author: lip
     * @date: 2019/11/6 0006 上午 11:18
     * @Description: 获取预警数据和mn号关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Map<String, Object>>> getMnAndEarlyDataList(Map<String, Object> paramMap) {

        List<Map<String, Map<String, Object>>> mnAndEarlyDataList = new ArrayList<>();
        Map<String, Map<String, Object>> mnAndConChangeData = new HashMap<>();
        Map<String, Map<String, Object>> mnAndFlowChangeData = new HashMap<>();
        Map<String, Map<String, Object>> mnAndCYZData = new HashMap<>();
        //1,浓度突变
        paramMap.put("collection", "SuddenRiseData");
        paramMap.put("timeKey", "ChangeTime");
        List<Document> conChange = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmData(mnAndConChangeData, conChange, CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum);
        //2,排放量突变
        paramMap.put("collection", "HourFlowData");
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("unwindkey", "HourFlowDataList");
        List<Document> flowChange = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmData(mnAndFlowChangeData, flowChange, CommonTypeEnum.RemindTypeEnum.FlowChangeEnum);
        //3,浓度超阈值
        paramMap.remove("unwindkey");
        paramMap.put("collection", "EarlyWarnData");
        paramMap.put("timeKey", "EarlyWarnTime");
        List<Document> CYZData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmData(mnAndCYZData, CYZData, CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum);
        mnAndEarlyDataList.add(mnAndConChangeData);
        mnAndEarlyDataList.add(mnAndFlowChangeData);
        mnAndEarlyDataList.add(mnAndCYZData);
        return mnAndEarlyDataList;
    }

    /**
     * @author: lip
     * @date: 2019/11/6 0006 上午 11:18
     * @Description: 获取预警数据和mn号关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getEarlyDetailData(Map<String, Object> paramMap, Map<String, List<Map<String, Object>>> mnAndAlarmDataList) {
        //1,浓度突变
        paramMap.put("collection", "SuddenRiseData");
        paramMap.put("timeKey", "ChangeTime");
        List<Document> conChange = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmDetailData(mnAndAlarmDataList, conChange, CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum);
        //2,排放量突变
        paramMap.put("collection", "HourFlowData");
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("unwindkey", "HourFlowDataList");
        List<Document> flowChange = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmDetailData(mnAndAlarmDataList, flowChange, CommonTypeEnum.RemindTypeEnum.FlowChangeEnum);
        //3,浓度超阈值
        paramMap.remove("unwindkey");
        paramMap.put("collection", "EarlyWarnData");
        paramMap.put("timeKey", "EarlyWarnTime");
        List<Document> CYZData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmDetailData(mnAndAlarmDataList, CYZData, CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum);
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 上午 11:18
     * @Description: 获取报警数据和mn号关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getAlarmDetailData(Map<String, Object> paramMap, Map<String, List<Map<String, Object>>> mnAndAlarmDataList) {
        paramMap.put("collection", "OverData");
        paramMap.put("timeKey", "OverTime");
        List<Document> overData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmDetailData(mnAndAlarmDataList, overData, CommonTypeEnum.RemindTypeEnum.OverAlarmEnum);
        paramMap.put("collection", "ExceptionData");
        paramMap.put("timeKey", "ExceptionTime");
        List<Document> exceptionData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmDetailData(mnAndAlarmDataList, exceptionData, CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum);
        //判断MN是否为空  是否需要查询无流量异常
        if (paramMap.get("noflowmns") != null) {
            paramMap.put("collection", "ExceptionData");
            paramMap.put("timeKey", "ExceptionTime");
            List<Document> noflowexceptionData = onlineCountAlarmService.countNoFlowExceptionDataForMnByParam(paramMap);
            setMnAndAlarmDetailData(mnAndAlarmDataList, noflowexceptionData, CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum);
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 上午 11:18
     * @Description: 获取报警数据和mn号关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Map<String, Object>>> getMnAndAlarmDataList(Map<String, Object> paramMap) {
        List<Map<String, Map<String, Object>>> mnAndAlarmDataList = new ArrayList<>();
        Map<String, Map<String, Object>> mnAndOverData = new HashMap<>();
        Map<String, Map<String, Object>> mnAndExceptionData = new HashMap<>();
        paramMap.put("collection", "OverData");
        paramMap.put("timeKey", "OverTime");
        List<Document> overData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmData(mnAndOverData, overData, CommonTypeEnum.RemindTypeEnum.OverAlarmEnum);
        paramMap.put("collection", "ExceptionData");
        paramMap.put("timeKey", "ExceptionTime");
        List<Document> exceptionData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
        setMnAndAlarmData(mnAndExceptionData, exceptionData, CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum);
        mnAndAlarmDataList.add(mnAndOverData);
        mnAndAlarmDataList.add(mnAndExceptionData);
        return mnAndAlarmDataList;


    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 17:32
     * @Description: 根据监测点类型统计报警预警超限次数(综合分析 - 报警分析统计)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countMonitorPointAlarmNumByParam", method = RequestMethod.POST)
    public Object countMonitorPointAlarmNumByParam(@RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "monitortype") List<Integer> monitortypes,
                                                   @RequestJson(value = "timetype") String timeType,
                                                   @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                   @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                   @RequestJson(value = "endtime") String endtime) {
        try {
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timeType, starttime, endtime);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setPollutionID(fkpollutionid);
            queryVO.setMonitorpointid(monitorpointid);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            queryVO.setUserId(userid);
            List<Map<String, Object>> list = countMonitorPointsAlarmNumForTime(timeType, monitortypes, queryVO);
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/8/18 11:41
     * @Description: 根据监测点类型和时间数据类型统计报警预警超限次数（app）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countMonitorPointAlarmNumByParamForApp", method = RequestMethod.POST)
    public Object countMonitorPointAlarmNumByParamForApp(@RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "monitortypes") List<Integer> monitortypes,
                                                         @RequestJson(value = "timetype") String timeType,
                                                         @RequestJson(value = "endtime") String endtime) {
        try {
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            Date start = null;
            Date end = null;
            switch (timeType) {
                case "hour":
                    start = DataFormatUtil.parseDate(starttime + ":00:00");
                    end = DataFormatUtil.parseDate(endtime + ":59:59");
                    break;
                case "day":
                    start = DataFormatUtil.parseDate(starttime + " 00:00:00");
                    end = DataFormatUtil.parseDate(endtime + " 23:59:59");
                    break;
                case "month":
                    String yearMothFirst = DataFormatUtil.getYearMothFirst(starttime);
                    String yearMothEnd = DataFormatUtil.getYearMothLast(endtime);
                    start = DataFormatUtil.parseDate(yearMothFirst + " 00:00:00");
                    end = DataFormatUtil.parseDate(yearMothEnd + " 23:59:59");
                    break;
            }
            queryVO.setStartTime(start);
            queryVO.setEndTime(end);
            List<Map<String, Object>> list = countMonitorPointsAlarmNumForTime(timeType, monitortypes, queryVO);
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/24 10:54
     * @Description: 历史预警报警异常统计 （分级预警总览-历史报警统计）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countHistoryAlarmNum", method = RequestMethod.POST)
    public Object countHistoryAlarmNum(@RequestJson(value = "starttime") String starttime,
                                       @RequestJson(value = "timetype") String timeType,
                                       @RequestJson(value = "endtime") String endtime,
                                       @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                       @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype
    ) {
        try {
            Set<String> monitorTypes = new HashSet<>();
            monitorTypes.add(String.valueOf(WasteWaterEnum.getCode()));
            monitorTypes.add(String.valueOf(WasteGasEnum.getCode()));
            monitorTypes.add(String.valueOf(SmokeEnum.getCode()));
            monitorTypes.add(String.valueOf(RainEnum.getCode()));
            monitorTypes.add(String.valueOf(AirEnum.getCode()));
            monitorTypes.add(String.valueOf(EnvironmentalVocEnum.getCode()));
            monitorTypes.add(String.valueOf(EnvironmentalStinkEnum.getCode()));
            monitorTypes.add(String.valueOf(FactoryBoundarySmallStationEnum.getCode()));
            monitorTypes.add(String.valueOf(FactoryBoundaryStinkEnum.getCode()));
            monitorTypes.add(String.valueOf(EnvironmentalDustEnum.getCode()));

            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            if (categorys != null && categorys.size() > 0) {
                if (categorys.contains("2")) {//安全
                    //monitorTypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode());
                    monitorTypes.add(String.valueOf(CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode()));
                    monitorTypes.add(String.valueOf(CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode()));
                    //monitorTypes.add(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode());
                    monitorTypes.add(String.valueOf(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode()));
                }
            }

            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timeType, starttime, endtime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            List<DeviceStatusVO> mns1 = alarmMNService.getMnsByMonitorPointTypes(monitorTypes);
            Set<String> mns = mns1.stream().map(DeviceStatusVO::getDgimn).collect(Collectors.toSet());


            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            userMonitorPointRelationDataService.ExcludeNoAuthDGIMNByParamMap(mns);

            /*-----设置权限end--------*/
            if (StringUtils.isNotBlank(pollutionid) && monitorpointtype != null) {
                mns.clear();
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("pollutionid", pollutionid);
                List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
                if (pointList.size() > 0) {
                    String mnCommon;
                    for (Map<String, Object> pointMap : pointList) {
                        if (pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null && monitorpointtype == Integer.parseInt(pointMap.get("monitorpointtype").toString())) {
                            mnCommon = pointMap.get("dgimn").toString();
                            mns.add(mnCommon);
                        }
                    }
                }
            }

            queryVO.setMns(mns);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            int[] reminds = {1, 2, 3, 4, 5};
            List<Map<String, Object>> list = countHistoryAlarmNumByParam(timeType, queryVO, reminds);
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> countHistoryAlarmNumByParam(String timeType, OnlineAlarmCountQueryVO queryVO, int[] reminds) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Object>> data = new HashMap<>();
        gettHistoryAlarmNumByParam(reminds, queryVO, timeType, data);
        if (data.size() > 0) {
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = entry.getValue();
                value.put("monitorTime", key);
                resultList.add(value);
            }
        }
        return resultList.stream().sorted(Comparator.comparing(m -> (m.get("monitorTime").toString()))).collect(Collectors.toList());
    }

    private void gettHistoryAlarmNumByParam(int[] reminds, OnlineAlarmCountQueryVO queryVO, String timeType, Map<String, Map<String, Object>> mapMap) {
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(timeType);
        for (int remind : reminds) {
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
            Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);
            queryVO.setCollection(collection);
            List<Document> documents = new ArrayList<>();
            if (remind == ConcentrationChangeEnum.getCode()) {
                documents = onlineCountAlarmService.countNDChangeAlarmNumByTimeType(queryVO, timeStyle);
            } else {
                documents = getAlarmRemindNumByParam(queryVO, remindTypeEnum, timeStyle);
            }

            if (timeType.equals("week")) {
                for (Document document1 : documents) {
                    document1.put("week", DataFormatUtil.getTimeOfYMWByString(document1.getString("_id")).toString());
                }
                Map<String, List<Document>> listMap = new HashMap<>();
                for (Document m : documents) {
                    listMap.computeIfAbsent(m.getString("week"), k -> new ArrayList<>()).add(m);
                }
                List<Document> documents1 = new ArrayList<>();
                for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                    String time = entry.getKey();
                    List<Document> value = entry.getValue();
                    int num = 0;
                    for (Document document1 : value) {
                        Integer count1 = document1.getInteger("num");
                        num = num + count1;
                    }
                    Document document = new Document();
                    document.put("_id", time);
                    document.put("num", num);
                    documents1.add(document);
                }
                documents = documents1;
            }
            getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/23 11:07
     * @Description: 企业当天预警报警统计（分级预警总览 - 企业详情 - 企业当天预警报警统计）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutionEarlyAlarmForToday", method = RequestMethod.POST)
    public Object countPollutionEarlyAlarmForToday(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<CommonTypeEnum.MonitorPointTypeEnum> monitorPointTypeEnums = new ArrayList<>();
            monitorPointTypeEnums.add(WasteWaterEnum);  //废水
            monitorPointTypeEnums.add(WasteGasEnum);    //废气
            monitorPointTypeEnums.add(RainEnum);    //雨水
            monitorPointTypeEnums.add(FactoryBoundaryStinkEnum);    //厂界恶臭
            monitorPointTypeEnums.add(FactoryBoundarySmallStationEnum); //厂界小型站
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            LocalDate localDate = LocalDate.now();
            queryVO.setStartTime(DataFormatUtil.parseDate(localDate + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(localDate + " 23:59:59"));
            List<Map<String, Object>> result = new ArrayList<>();
            queryVO.setPollutionID(pollutionid);
            for (CommonTypeEnum.MonitorPointTypeEnum anEnum : monitorPointTypeEnums) {
                int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(anEnum.getCode());
                queryVO.setMonitorPointType(anEnum.getCode());
                result.add(getAlarmNumByOneMonitorType(anEnum, reminds, queryVO));
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/1 15:59
     * @Description: 根据监测点类型等查询条件统计该监测点类型下各个报警类型的个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmNumForRemindTypeByParam", method = RequestMethod.POST)
    public Object countAlarmNumForRemindTypeByParam(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "timetype") String timetype,
                                                    @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                    @RequestJson(value = "endtime") String endtime) {

        try {
            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype);
            Assert.notNull(monitorPointTypeEnum, "monitorPointTypeEnum must not be null!");
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timetype, starttime, endtime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setPollutionID(pollutionid);
            int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitorPointTypeEnum.getCode());
            queryVO.setMonitorPointType(monitorpointtype);
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Map<String, Object>> data = new HashMap<>();
            countAlarmRemindNumByParam(reminds, queryVO, timetype, data);
            if (data.size() > 0) {
                for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Map<String, Object> value = entry.getValue();
                    value.put("monitorTime", key);
                    resultList.add(value);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().sorted(Comparator.comparing(m -> (m.get("monitorTime").toString()))).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 下午 5:00
     * @Description: 自定义查询条件根据提醒类型统计报警预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmDataForRemindTypeByParam", method = RequestMethod.POST)
    public Object countAlarmDataForRemindTypeByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                     @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            String mnCommon;
            int type;
            Map<String, Integer> mnAndType = new HashMap<>();
            //数据权限
            List<String> dgimnList = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<String> mns = new ArrayList<>();
            List<String> noflowmns = new ArrayList<>();
            if (StringUtils.isNotBlank(monitorpointcategory)) {
                paramMap.put("monitorPointCategorys", Arrays.asList(monitorpointcategory));
                List<Map<String, Object>> inMnDatas = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                if (inMnDatas.size() > 0) {
                    for (Map<String, Object> inMnData : inMnDatas) {
                        mnCommon = inMnData.get("dgimn").toString();
                        if (dgimnList.contains(mnCommon)) {
                            type = Integer.parseInt(inMnData.get("FK_MonitorPointTypeCode").toString());
                            mnAndType.put(mnCommon, type);
                            mns.add(mnCommon);
                        }
                    }
                }
            } else {
                List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
                for (Map<String, Object> mapIndex : deviceStatus) {
                    mnCommon = mapIndex.get("dgimn").toString();
                    if (dgimnList.contains(mnCommon)) {
                        type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                        mnAndType.put(mnCommon, type);
                        mns.add(mnCommon);
                        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {//判断是否为废水类型 存储其MN号
                            noflowmns.add(mnCommon);
                        }
                    }
                }
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("noflowmns", noflowmns);
                Map<String, List<Map<String, Object>>> mnAndEarlyAndAlarmDataList = getEarlyAndAlarmDetailData(paramMap);
                Map<Integer, List<Map<String, Object>>> typeAndAlarmDataListTemp = new HashMap<>();
                if (mnAndEarlyAndAlarmDataList.size() > 0) {
                    List<Map<String, Object>> alarmDataListTemp;
                    for (String mnIndex : mnAndEarlyAndAlarmDataList.keySet()) {
                        type = mnAndType.get(mnIndex);
                        if (typeAndAlarmDataListTemp.containsKey(type)) {
                            alarmDataListTemp = typeAndAlarmDataListTemp.get(type);
                        } else {
                            alarmDataListTemp = new ArrayList<>();
                        }
                        alarmDataListTemp.addAll(mnAndEarlyAndAlarmDataList.get(mnIndex));
                        typeAndAlarmDataListTemp.put(type, alarmDataListTemp);
                    }
                    Map<Integer, List<Map<String, Object>>> typeAndAlarmDataList = new HashMap<>();
                    if (typeAndAlarmDataListTemp.size() > 0) {
                        for (Integer typeIndex : typeAndAlarmDataListTemp.keySet()) {
                            String remindcode;
                            String remindname;
                            Integer countnum;
                            Map<String, Object> codeAndName = new HashMap<>();
                            Map<String, Integer> codeAndCount = new HashMap<>();
                            alarmDataListTemp = typeAndAlarmDataListTemp.get(typeIndex);
                            for (Map<String, Object> alarmData : alarmDataListTemp) {
                                remindcode = alarmData.get("remindcode").toString();
                                remindname = alarmData.get("remindname").toString();
                                countnum = Integer.parseInt(alarmData.get("countnum").toString());
                                if (codeAndCount.containsKey(remindcode)) {
                                    codeAndCount.put(remindcode, codeAndCount.get(remindcode) + countnum);
                                } else {
                                    codeAndCount.put(remindcode, countnum);
                                    codeAndName.put(remindcode, remindname);
                                }
                            }
                            List<Map<String, Object>> alarmDataList = new ArrayList<>();
                            for (String codeIndex : codeAndCount.keySet()) {
                                Map<String, Object> alarmData = new HashMap<>();
                                alarmData.put("remindcode", codeIndex);
                                alarmData.put("remindname", codeAndName.get(codeIndex));
                                alarmData.put("countnum", codeAndCount.get(codeIndex));
                                alarmDataList.add(alarmData);
                            }
                            typeAndAlarmDataList.put(typeIndex, alarmDataList);
                        }
                    }
                    for (Integer typeIndex : monitorpointtypes) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("monitorpointtype", typeIndex);
                        resultMap.put("alarmdata", typeAndAlarmDataList.get(typeIndex));
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
     * @author: zhangzc
     * @date: 2019/7/24 10:54
     * @Description: 企业历史预警报警异常统计 （分级预警总览 - 企业详情 - 企业历史预警报警异常统计）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutionHistoryAlarmNum", method = RequestMethod.POST)
    public Object countPollutionHistoryAlarmNum(@RequestJson(value = "starttime") String starttime,
                                                @RequestJson(value = "timetype") String timeType,
                                                @RequestJson(value = "pollutionid") String pollutionid,
                                                @RequestJson(value = "endtime") String endtime) {
        try {
            List<Integer> monitorTypes = new ArrayList<>();
            monitorTypes.add(WasteWaterEnum.getCode());
            monitorTypes.add(WasteGasEnum.getCode());
            monitorTypes.add(SmokeEnum.getCode());
            monitorTypes.add(RainEnum.getCode());
            monitorTypes.add(FactoryBoundarySmallStationEnum.getCode());
            monitorTypes.add(FactoryBoundaryStinkEnum.getCode());
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timeType, starttime, endtime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setPollutionID(pollutionid);
            List<Map<String, Object>> list = countMonitorPointsAlarmNumForTime(timeType, monitorTypes, queryVO);
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/23 11:10
     * @Description: 统计企业预警报警异常排名 (分级预警总览-企业报警排名）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEnterpriseEarlyAlarm", method = RequestMethod.POST)
    public Object countEnterpriseEarlyAlarm(@RequestJson(value = "starttime") String starttime,
                                            @RequestJson(value = "monitortype") List<Integer> monitortypes,
                                            @RequestJson(value = "endtime") String endtime) {
        try {
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            Map<String, Map<String, Object>> data = new HashMap<>();
            Map<String, String> enNameMap = new HashMap<>();

           /* List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            if (categorys!=null&&categorys.size()>0){
                if(categorys.contains("2")){//安全
                    //monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode());
                    monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode());
                    monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode());
                    //monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode());
                    monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode());
                }
            }*/

            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            Map<String, Object> params = new HashMap<>();
            String sessionID = SessionUtil.getSessionID();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            params.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataService.getDGIMNByParamMap(params);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            /*-----设置权限end--------*/

            for (Integer monitortype : monitortypes) {
                CommonTypeEnum.MonitorPointTypeEnum typeEnum = getCodeByInt(monitortype);
                Assert.notNull(typeEnum, "监测类型错误");
                queryVO.setMonitorPointType(typeEnum.getCode());
                List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
                Set<String> mns = new HashSet<>();
                for (Map<String, Object> document : mndata) {
                    String dgimn = document.get("DGIMN").toString();
                    String entName = document.get("EntName").toString();
                    mns.add(dgimn);
                    enNameMap.put(dgimn, entName);
                }

                /*-----设置权限--------*/
                //从查询出的监测点里筛选拥有权限的监测点
                mns.removeIf(m -> !collect.contains(m));
                /*-----设置权限end--------*/

                queryVO.setMns(mns);
                int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitortype);
                countAllAlarmRemindNum(reminds, queryVO, data, 1);
            }
            List<Map<String, Object>> list = formatDataByRanking(data, enNameMap, "pollutionname");
            List<Map<String, Object>> resultListInfo = new ArrayList<>();
            for (Map<String, Object> map : list) {
                if (resultListInfo.size() == 5) {
                    break;
                }
                resultListInfo.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultListInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/23 11:10
     * @Description: 统计企业预警报警
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEnterpriseEarlyAlarmByParam", method = RequestMethod.POST)
    public Object countEnterpriseEarlyAlarmByParam(@RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "monitortype") Integer monitortype,
                                                   @RequestJson(value = "remindtype") Integer remindtype,
                                                   @RequestJson(value = "endtime") String endtime) {
        try {
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            Map<String, Map<String, Object>> data = new HashMap<>();
            Map<String, String> enNameMap = new HashMap<>();
            CommonTypeEnum.MonitorPointTypeEnum typeEnum = getCodeByInt(monitortype);
            Assert.notNull(typeEnum, "监测类型错误");
            queryVO.setMonitorPointType(typeEnum.getCode());
            List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
            Set<String> mns = new HashSet<>();
            for (Map<String, Object> document : mndata) {
                String dgimn = document.get("DGIMN").toString();
                String entName = document.get("EntName").toString();
                mns.add(dgimn);
                enNameMap.put(dgimn, entName);
            }
            queryVO.setMns(mns);
            countAlarmRemindNum(remindtype, queryVO, data, 1);
            List<Map<String, Object>> list = formatDataByRanking(data, enNameMap, "pollutionname");
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 17:23
     * @Description: 获取数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void countAlarmRemindNum(int remind, OnlineAlarmCountQueryVO queryVO, Map<String, Map<String, Object>> mapMap, int countByWhat) {
        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
        String timeFieldName = getTimeFieldName(remindTypeEnum);
        String collection = getCollection(remindTypeEnum);
        queryVO.setTimeFieldName(timeFieldName);
        queryVO.setCollection(collection);
        List<Document> documents = new ArrayList<>();
        switch (countByWhat) {
            case 1:
                documents = getAlarmDataByParam(queryVO, remindTypeEnum);
                break;
            case 2:
                documents = getPollutantAlarmDataByParam(queryVO, remindTypeEnum);
                break;
        }
        getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/24 10:54
     * @Description: 统计污染源报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutionAlarmNum", method = RequestMethod.POST)
    public Object countPollutionAlarmNum(@RequestJson(value = "starttime") String starttime,
                                         @RequestJson(value = "timetype") String timeType,
                                         @RequestJson(value = "monitortype") Integer monitortype,
                                         @RequestJson(value = "remindtype") Integer remindtype,
                                         @RequestJson(value = "pollutionid") String pollutionid,
                                         @RequestJson(value = "endtime") String endtime) {
        try {
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timeType, starttime, endtime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setPollutionID(pollutionid);
            List<Map<String, Object>> list = countMonitorPointsAlarmNumForTime(timeType, monitortype, remindtype, queryVO);
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/21 14:55
     * @Description: 根据时间统计多个监测类型的统计报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> countMonitorPointsAlarmNumForTime(String timeType, Integer monitortype, Integer remindtype, OnlineAlarmCountQueryVO queryVO) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Object>> data = new HashMap<>();
        CommonTypeEnum.MonitorPointTypeEnum codeByInt = getCodeByInt(monitortype);
        Assert.notNull(codeByInt, "监测类型错误");
        queryVO.setMonitorPointType(monitortype);
        countAlarmRemindNumByParam(remindtype, queryVO, timeType, data);
        if (data.size() > 0) {
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = entry.getValue();
                value.put("monitorTime", key);
                resultList.add(value);
            }
        }
        return resultList.stream().sorted(Comparator.comparing(m -> (m.get("monitorTime").toString()))).collect(Collectors.toList());
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 17:23
     * @Description: 获取数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void countAlarmRemindNumByParam(int remind, OnlineAlarmCountQueryVO queryVO, String timeType, Map<String, Map<String, Object>> mapMap) {
        List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
        Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
        queryVO.setMns(mns);
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(timeType);
        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
        String timeFieldName = getTimeFieldName(remindTypeEnum);
        String collection = getCollection(remindTypeEnum);
        queryVO.setTimeFieldName(timeFieldName);
        queryVO.setCollection(collection);
        List<Document> documents = getAlarmRemindNumByParam(queryVO, remindTypeEnum, timeStyle);
        if (timeType.equals("week")) {
            for (Document document1 : documents) {
                document1.put("week", DataFormatUtil.getTimeOfYMWByString(document1.getString("_id")).toString());
            }
            Map<String, List<Document>> listMap = new HashMap<>();
            for (Document m : documents) {
                listMap.computeIfAbsent(m.getString("week"), k -> new ArrayList<>()).add(m);
            }
            List<Document> documents1 = new ArrayList<>();
            for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                String time = entry.getKey();
                List<Document> value = entry.getValue();
                int num = 0;
                for (Document document1 : value) {
                    Integer count1 = document1.getInteger("num");
                    num = num + count1;
                }
                Document document = new Document();
                document.put("_id", time);
                document.put("num", num);
                documents1.add(document);
            }
            documents = documents1;
        }
        getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/23 11:10
     * @Description: 污染物预警报警异常排名统计(分级预警总览 - 污染物预警报警异常排名统计)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutantAlarmDataByMonitorTimes", method = RequestMethod.POST)
    public Object countPollutantAlarmDataByMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "monitortype") List<Integer> monitortypes,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "remindlist", required = false) List<Integer> remindlist) {
        try {
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            Map<String, Map<String, Object>> data = new HashMap<>();
            Map<String, String> enNameMap = new HashMap<>();

            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            Map<String, Object> params = new HashMap<>();
            String sessionID = SessionUtil.getSessionID();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            params.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataService.getDGIMNByParamMap(params);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            /*-----设置权限end--------*/

            //List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            /*if (categorys!=null&&categorys.size()>0){
                if(categorys.contains("2")){//安全
                    //monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode());
                    monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode());
                    monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode());
                    //monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode());
                    monitortypes.add(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode());
                }
            }*/

            for (Integer monitortype : monitortypes) {
                queryVO.setMonitorPointType(monitortype);
                List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
                CommonTypeEnum.MonitorPointTypeEnum typeEnum = getCodeByInt(monitortype);
                Assert.notNull(typeEnum, "监测类型错误");
                Set<String> mns = new HashSet<>();
                Set<String> pollutantCodes = new HashSet<>();
                for (Map<String, Object> map : mndata) {
                    String dgimn = map.get("DGIMN").toString();
                    if (map.get("Code") != null) {
                        pollutantCodes.add(map.get("Code").toString());
                        enNameMap.put(map.get("Code").toString(), map.get("Name").toString());
                    }
                    mns.add(dgimn);
                }
                /*-----设置权限--------*/
                //从查询出的监测点里筛选拥有权限的监测点
                mns.removeIf(m -> !collect.contains(m));
                /*-----设置权限end--------*/

                queryVO.setMns(mns);
                queryVO.setPollutantCodes(pollutantCodes);
                if (remindlist == null || remindlist.size() == 0) {
                    int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitortype);
                    countAllAlarmRemindNum(reminds, queryVO, data, 2);
                } else {
                    int[] reminds = remindlist.stream().mapToInt(Integer::intValue).toArray();
                    countAllAlarmRemindNum(reminds, queryVO, data, 2);
                }

            }
            List<Map<String, Object>> list = formatDataByRanking(data, enNameMap, "name");
            List<Map<String, Object>> resultListInfo = new ArrayList<>();
            for (Map<String, Object> map : list) {
                if (resultListInfo.size() == 5) {
                    break;
                }
                resultListInfo.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultListInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/29 9:41
     * @Description: 根据企业id获取该企业下各污染物当天各监测类型下的报警次数以及报警时间段（分级预警总览 - 企业详情 - 当天报警预警异常统计的列表接口） 表格数据
     * @updateUser:xsm
     * @updateDate:2019/12/16 13:59
     * @updateDescription: 新增返回内容Mn号
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutantsAlarmNumAndLastTimes", method = RequestMethod.POST)
    public Object getPollutantsAlarmNumAndLastTimes(@RequestJson(value = "pollutionid") String pollutionid,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime) {
        try {
            List<CommonTypeEnum.MonitorPointTypeEnum> monitorPointTypeEnums = new ArrayList<>();
            monitorPointTypeEnums.add(WasteWaterEnum);  //废水
            monitorPointTypeEnums.add(WasteGasEnum);    //废气
            monitorPointTypeEnums.add(RainEnum);    //雨水
            monitorPointTypeEnums.add(SmokeEnum);//烟气
            monitorPointTypeEnums.add(FactoryBoundaryStinkEnum);    //厂界恶臭
            monitorPointTypeEnums.add(FactoryBoundarySmallStationEnum); //厂界小型站
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("day", starttime, endtime);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setPollutionID(pollutionid);
            List<Map<String, Object>> resultInfo = new ArrayList<>();
            for (CommonTypeEnum.MonitorPointTypeEnum anEnum : monitorPointTypeEnums) {
                queryVO.setMonitorPointType(anEnum.getCode());
                int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(anEnum.getCode());
                Set<String> mns = new HashSet<>();
                List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
                Map<String, String> pollutantMap = new HashMap<>();
                Map<String, String> pointNameMnMaps = new HashMap<>();
                Map<String, String> pointIDMap = new HashMap<>();
                Map<String, String> pointIDMNMap = new HashMap<>();
                Set<String> pollutantCodes = new HashSet<>();
                for (Map<String, Object> map : mndata) {
                    String dgimn = map.get("DGIMN").toString();
                    if (map.get("Code") != null) {
                        pollutantCodes.add(map.get("Code").toString());
                        pollutantMap.put(map.get("Code").toString(), map.get("Name").toString());
                    }
                    if (map.get("PointName") != null) {
                        pointNameMnMaps.put(dgimn, map.get("pointid").toString());
                        pointIDMap.put(map.get("pointid").toString(), map.get("PointName").toString());
                    }
                    pointIDMNMap.put(map.get("pointid").toString(), dgimn);
                    mns.add(dgimn);
                }
                queryVO.setMns(mns);
                queryVO.setPollutantCodes(pollutantCodes);
                Map<String, Object> monitorPointTypeMaps = new HashMap<>();
                Map<String, Map<String, List<Map<String, Object>>>> outputsMap = new HashMap<>();
                for (int remind : reminds) {
                    CommonTypeEnum.RemindTypeEnum remindTypeEnum = getObjectByCode(remind);
                    Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
                    String timeFieldName = getTimeFieldName(remindTypeEnum);
                    String collection = getCollection(remindTypeEnum);
                    queryVO.setTimeFieldName(timeFieldName);
                    queryVO.setCollection(collection);
                    List<Document> documents1 = getPollutantAlarmDataByParam4(queryVO, remindTypeEnum);
                    for (Document document : documents1) {
                        Map<String, Object> mapInfo = new HashMap<>();
                        String dataGatherCode = document.getString("DataGatherCode");
                        String pollutantCode = document.getString("PollutantCode");
                        String monitorPointID = pointNameMnMaps.get(dataGatherCode);
//                        String pollutantName = pollutantMap.get(pollutantCode);
                        Integer num = document.getInteger("num");
                        Date lastTime = document.getDate("LastTime");
                        String date = DataFormatUtil.getDate(lastTime);
                        String charSequence = date.subSequence(11, date.length()).toString();
                        mapInfo.put("num", num);
                        mapInfo.put("lasttime", charSequence);
                        mapInfo.put("remindtype", remind);
                        if (outputsMap.containsKey(monitorPointID)) {
                            Map<String, List<Map<String, Object>>> pollutantsMap = outputsMap.get(monitorPointID);
                            if (pollutantsMap.containsKey(pollutantCode)) {
                                List<Map<String, Object>> mapList = pollutantsMap.get(pollutantCode);
                                mapList.add(mapInfo);
                            } else {
                                List<Map<String, Object>> mapList = new ArrayList<>();
                                mapList.add(mapInfo);
                                pollutantsMap.put(pollutantCode, mapList);
                            }
                        } else {
                            Map<String, List<Map<String, Object>>> pollutantsMap = new HashMap<>();
                            List<Map<String, Object>> mapList = new ArrayList<>();
                            mapList.add(mapInfo);
                            pollutantsMap.put(pollutantCode, mapList);
                            outputsMap.put(monitorPointID, pollutantsMap);
                        }
                    }
                }
                monitorPointTypeMaps.put("monitorpointtype", anEnum.getCode());
                String name = anEnum.getName().replace("点类型", "");
                monitorPointTypeMaps.put("monitorpointtypename", name);
                List<Map<String, Object>> listMaps = new ArrayList<>();
                for (Map.Entry<String, Map<String, List<Map<String, Object>>>> entry : outputsMap.entrySet()) {
                    String monitorPointID = entry.getKey();
                    Map<String, List<Map<String, Object>>> polluants = entry.getValue();
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (Map.Entry<String, List<Map<String, Object>>> stringListEntry : polluants.entrySet()) {
                        String pollutantName = stringListEntry.getKey();
                        List<Map<String, Object>> value = stringListEntry.getValue();
                        Map<String, Object> map = new HashMap<>();
                        map.put("pollutantName", pollutantMap.get(pollutantName));
                        map.put("pollutantCode", pollutantName);
                        map.put("alarmdatas", value);
                        list.add(map);
                    }
                    Map<String, Object> outputMap = new HashMap<>();
                    outputMap.put("dgimn", pointIDMNMap.get(monitorPointID));
                    outputMap.put("monitorpointid", monitorPointID);
                    outputMap.put("monitorpointname", pointIDMap.get(monitorPointID));
                    outputMap.put("polluantdatas", list);
                    listMaps.add(outputMap);
                }
                monitorPointTypeMaps.put("outputdatas", listMaps);
                resultInfo.add(monitorPointTypeMaps);
            }
            System.out.println(AuthUtil.parseJsonKeyToLower("success", resultInfo));
            return AuthUtil.parseJsonKeyToLower("success", resultInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/29 9:41
     * @Description: 根据企业id获取该企业下各污染物当天各监测类型下的报警次数以及报警时间段（分级预警总览 - 企业详情 - 当天报警预警异常统计的列表接口）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "getPollutantsAlarmNumAndTimes", method = RequestMethod.POST)
    public Object getPollutantsAlarmNumAndTimes(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<CommonTypeEnum.MonitorPointTypeEnum> monitorPointTypeEnums = new ArrayList<>();
            monitorPointTypeEnums.add(WasteWaterEnum);  //废水
            monitorPointTypeEnums.add(WasteGasEnum);    //废气
            monitorPointTypeEnums.add(RainEnum);//雨水
            monitorPointTypeEnums.add(SmokeEnum);//烟气
            monitorPointTypeEnums.add(FactoryBoundaryStinkEnum);    //厂界恶臭
            monitorPointTypeEnums.add(FactoryBoundarySmallStationEnum); //厂界小型站
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            LocalDate localDate = LocalDate.now();
            queryVO.setStartTime(DataFormatUtil.parseDate(localDate + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(localDate + " 23:59:59"));
            queryVO.setPollutionID(pollutionid);
            List<Map<String, Object>> resultInfo = new ArrayList<>();
            for (CommonTypeEnum.MonitorPointTypeEnum anEnum : monitorPointTypeEnums) {
                queryVO.setMonitorPointType(anEnum.getCode());
                int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(anEnum.getCode());
                Set<String> mns = new HashSet<>();
                List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
                Map<String, String> enNameMap = new HashMap<>();
                Set<String> pollutantCodes = new HashSet<>();
                for (Map<String, Object> map : mndata) {
                    String dgimn = map.get("DGIMN").toString();
                    if (map.get("Code") != null) {
                        pollutantCodes.add(map.get("Code").toString());
                        enNameMap.put(map.get("Code").toString(), map.get("Name").toString());
                    }
                    mns.add(dgimn);
                }
                queryVO.setMns(mns);
                queryVO.setPollutantCodes(pollutantCodes);
                List<Document> documents = new ArrayList<>();
                for (int remind : reminds) {
                    CommonTypeEnum.RemindTypeEnum remindTypeEnum = getObjectByCode(remind);
                    Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
                    String timeFieldName = getTimeFieldName(remindTypeEnum);
                    String collection = getCollection(remindTypeEnum);
                    queryVO.setTimeFieldName(timeFieldName);
                    queryVO.setCollection(collection);
                    List<Document> param2 = getPollutantAlarmDataByParam2(queryVO, remindTypeEnum, "%H:%M");
                    documents.addAll(param2);
                }
                //污染物code分组
                Map<String, List<Document>> listMap = documents.stream().collect(Collectors.groupingBy(m -> m.get("_id").toString()));
                List<Map<String, Object>> mapList = new ArrayList<>();
                for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                    String entryKey = entry.getKey();
                    int num = 0;
                    List<Document> values = entry.getValue();
                    List<String> times = new ArrayList<>();
                    for (Document document : values) {
                        num = num + document.getInteger("num");
                        ArrayList times2 = document.get("Times", ArrayList.class);
                        times.addAll(times2);
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("num", num);
                    map.put("code", entryKey);
                    map.put("times", times);
                    mapList.add(map);
                }
                for (Map<String, Object> map : mapList) {
                    String code = map.get("code").toString();
                    int num = (int) map.get("num");
                    List<String> times = (List<String>) map.get("times");
                    if (enNameMap.containsKey(code)) {
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("name", enNameMap.get(code));
                        String s = DataFormatUtil.groupTimesByIntervalTime(times, 30);
                        map2.put("times", s);
                        map2.put("type", anEnum.getName().replace("监测点类型", ""));
                        map2.put("num", num);
                        resultInfo.add(map2);
                    }
                }
            }
            List<Map<String, Object>> collect = resultInfo.stream().filter(m -> m.get("num") != null).sorted(Comparator.comparingInt(m -> Integer.parseInt(((Map) m).get("num").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/19 10:03
     * @Description: 主要报警预警时段统计(分级预警总览 - 主要报警预警时段统计)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "countKeyAlarmAndEarlyWarning", method = RequestMethod.POST)
    public Object countKeyAlarmAndEarlyWarning(@RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "monitortype") Integer monitorType,
                                               @RequestJson(value = "remindtype") Integer remind,
                                               @RequestJson(value = "endtime") String endtime) {
        try {
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            Map<String, Object> resultMap = new HashMap<>();
            CommonTypeEnum.MonitorPointTypeEnum anEnum = getCodeByInt(monitorType);
            Assert.notNull(anEnum, "监测类型错误");
            queryVO.setMonitorPointType(anEnum.getCode());
            List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
            if (mndata.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            Map<String, String> codeName = new HashMap<>();
            Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> codes = mndata.stream().filter(m -> m.get("Code") != null).map(document -> document.get("Code").toString()).collect(Collectors.toSet());
            if (mns.size() == 0 || codes.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            queryVO.setMns(mns);
            queryVO.setPollutantCodes(codes);
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
            Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
            queryVO.setCollection(collection);
            List<Document> documents = getAlarmTimesByParam(queryVO, remindTypeEnum);
            if (documents.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            boolean isPollution = false;    //是否是企业
            Map<String, Map<String, Object>> mnDatas = new HashMap<>();
            for (Map<String, Object> mndatum : mndata) {
                String dgimn = mndatum.get("DGIMN").toString();
                if (!mnDatas.keySet().contains(dgimn)) {
                    Map<String, Object> map = new HashMap<>();
                    if (mndatum.keySet().contains("PointName")) {
                        isPollution = true;
                        map.put("monitorpointname", mndatum.get("PointName"));
                        map.put("monitorpointid", mndatum.get("pointid"));
                        map.put("pollutionid", mndatum.get("PK_PollutionID"));
                        map.put("pollutionname", mndatum.get("EntName"));
                    } else {
                        map.put("monitorpointname", mndatum.get("EntName"));
                        map.put("monitorpointid", mndatum.get("ID"));
                    }
                    mnDatas.put(dgimn, map);
                }
                if (mndatum.get("Code") != null && mndatum.get("Name") != null) {
                    codeName.put(mndatum.get("Code").toString(), mndatum.get("Name").toString());
                }
            }
            Map<String, Object> timenum = new LinkedHashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            //获取每个时间段对应的下标
            Map<String, List<Document>> timeToIndexes = getTimeToIndexes(documents, 10);
            for (Map.Entry<String, List<Document>> entry : timeToIndexes.entrySet()) {
                String key = entry.getKey();    //时间段
                int num = 0;
                List<Document> datalist = entry.getValue();    //对应的数据
                //根据排口分组，然后根据污染物分组
                Map<String, Map<String, List<Document>>> collect = datalist.stream().collect(Collectors.groupingBy(m -> m.getString("DataGatherCode"),
                        Collectors.groupingBy(m -> m.getString("PollutantCode"))));
                for (java.util.Map.Entry<String, java.util.Map<String, List<Document>>> mapEntry : collect.entrySet()) {
                    String DataGatherCode = mapEntry.getKey();
                    Map<String, Object> map = new HashMap<>();
                    if (isPollution) {
                        map.put("pollutionid", mnDatas.get(DataGatherCode).get("pollutionid"));
                        map.put("pollutionname", mnDatas.get(DataGatherCode).get("pollutionname"));
                    }
                    map.put("monitorpointname", mnDatas.get(DataGatherCode).get("monitorpointname"));
                    map.put("monitorpointid", mnDatas.get(DataGatherCode).get("monitorpointid"));
                    map.put("mainalarmtime", key);
                    map.put("mn", DataGatherCode);
                    List<Map<String, Object>> pollutants = new ArrayList<>();
                    Map<String, List<Document>> pollutantMaps = mapEntry.getValue();
                    for (Map.Entry<String, List<Document>> listEntry : pollutantMaps.entrySet()) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        String code = listEntry.getKey();
                        pollutantMap.put("code", code);
                        pollutantMap.put("name", codeName.get(code));
                        List<Document> value = listEntry.getValue();
                        int num1 = value.stream().mapToInt(document -> document.getInteger("num")).sum();
                        num += num1;
                        pollutantMap.put("num", num1);
                        pollutants.add(pollutantMap);
                    }
                    map.put("pollutants", pollutants);
                    resultList.add(map);
                }
                timenum.put(key, num);
            }
            resultMap.put("timenum", timenum);
            resultMap.put("data", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzhenchao
     * @date: 2019/10/31 14:59
     * @Description: 获取连续数据
     * @param:
     * @return: //key 为时间段，list为下标
     * @throws:
     */
    private Map<String, List<Document>> getTimeToIndexes(List<Document> documents, int m) {
        Map<Integer, Integer> indexMaps = new LinkedHashMap<>();
        int index = 0;
        for (int i = 0; i < documents.size() - 1; i++) {
            LocalTime Time1 = LocalTime.parse(documents.get(i).getString("Time"));
            LocalTime Time2 = LocalTime.parse(documents.get(i + 1).getString("Time"));
            LocalTime localTime = Time1.plusMinutes(m);
            //加上10分钟在下一个时间之后，相差小于十分钟
            if (localTime.isAfter(Time2)) {
                indexMaps.put(index, i + 1);
            } else { //相差大于十分钟
                index = i + 1;
                indexMaps.put(i + 1, i + 1);
            }
        }
        Map<String, List<Document>> listMap = new LinkedHashMap<>();  //key 为时间段，list为下标
        for (Map.Entry<Integer, Integer> entry : indexMaps.entrySet()) {
            Integer startIndex = entry.getKey();   //开始下标
            Integer endIndex = entry.getValue();   //结束下标
            String time;
            List<Document> indexes = new ArrayList<>();
            if (!startIndex.equals(endIndex)) { //连续值
                //获取开始时间结束时间
                String start = documents.get(startIndex).getString("Time");
                String end = documents.get(endIndex).getString("Time");
                time = start.equals(end) ? start : start + "~" + end;
                while (!startIndex.equals(endIndex)) {
                    indexes.add(documents.get(startIndex));
                    startIndex++;
                }
                indexes.add(documents.get(endIndex));
                listMap.put(time, indexes);
            } else { //单个值
                time = documents.get(startIndex).getString("Time");
                indexes.add(documents.get(startIndex));
                listMap.put(time, indexes);
            }
        }
        return listMap;
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/2 13:15
     * @Description: 各个监测点类型该时间内的报警企业的个数  （只统计异常和超限报警的企业个数)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEachMonitorTypeAlarmEntNum", method = RequestMethod.POST)
    public Object countEachMonitorTypeAlarmEntNum(@RequestJson(value = "monitorpointtypes") List<Integer> monitorPointTypes,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> resultMap = new ArrayList<>();
            if (monitorPointTypes.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();

            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            for (Integer pointType : monitorPointTypes) {
                CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointType);
                Assert.notNull(monitorPointTypeEnum, "monitorPointTypeEnum must not be null!");
                CommonTypeEnum.RemindTypeEnum[] reminds = {ExceptionAlarmEnum, OverAlarmEnum};
                queryVO.setMonitorPointType(pointType);
                List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
                Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
                queryVO.setMns(mns);
                List<Document> documents = new ArrayList<>();
                for (CommonTypeEnum.RemindTypeEnum remindTypeEnum : reminds) {
                    String timeFieldName = getTimeFieldName(remindTypeEnum);
                    String collection = getCollection(remindTypeEnum);
                    queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                    queryVO.setCollection(collection);          //放入连接表
                    documents.addAll(getAlarmDataByParam(queryVO, remindTypeEnum));
                }
                Set<String> alramMNs = new HashSet<>();
                for (Document document : documents) {
                    String id = document.getString("_id");
                    alramMNs.add(id);
                }
                Set<String> set = new HashSet<>();
                for (Map<String, Object> mndatum : mndata) {
                    if (alramMNs.contains(mndatum.get("DGIMN").toString())) {
                        String pk_pollutionID = mndatum.get("ID").toString();
                        set.add(pk_pollutionID);
                    }
                }
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("monitorpointtype", pointType);
                objectMap.put("num", set.size());
                resultMap.add(objectMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 上午 11:03
     * @Description: 根据监测点类型和指定日期统计监测点报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPointAlarmDataByTypeAndTime", method = RequestMethod.POST)
    public Object countPointAlarmDataByTypeAndTime(@RequestJson(value = "monitorpointtypes") List<Integer> monitorPointTypes,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (monitorPointTypes.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorPointTypes);
            List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
            String mnCommon;
            int type;
            Map<String, Integer> mnAndType = new HashMap<>();
            //数据权限
            List<String> dgimnList = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> mapIndex : deviceStatus) {
                mnCommon = mapIndex.get("dgimn").toString();
                if (dgimnList.contains(mnCommon)) {
                    type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                    mnAndType.put(mnCommon, type);
                    mns.add(mnCommon);
                }
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime + " 00:00:00");
                paramMap.put("endtime", endtime + " 23:59:59");
                List<Map<String, Map<String, Object>>> mnAndAlarmDataList = getMnAndAlarmDataList(paramMap);
                Map<Integer, Integer> typeAndCount = new HashMap<>();
                if (mnAndAlarmDataList.size() > 0) {
                    Map<String, Map<String, Object>> mnAndAlarmData;
                    for (int i = 0; i < mnAndAlarmDataList.size(); i++) {
                        mnAndAlarmData = mnAndAlarmDataList.get(i);
                        for (String mnIndex : mnAndAlarmData.keySet()) {
                            type = mnAndType.get(mnIndex);
                            if (typeAndCount.containsKey(type)) {
                                typeAndCount.put(type, typeAndCount.get(type) + 1);
                            } else {
                                typeAndCount.put(type, 1);
                            }
                        }
                    }
                }
                if (typeAndCount.size() > 0) {
                    for (Integer typeIndex : monitorPointTypes) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("monitorpointtype", typeIndex);
                        resultMap.put("alarmnum", typeAndCount.get(typeIndex) != null ? typeAndCount.get(typeIndex) : 0);
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
     * @author: zhangzc
     * @date: 2019/8/5 16:01
     * @Description: 根据监测点类型和报警类型统计企业报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEntAlarmNumByMonitorPointType", method = RequestMethod.POST)
    public Object countEntAlarmNumByMonitorPointType(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "remindtype") Integer remindtype,
                                                     @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                     @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = getCodeByInt(monitorpointtype);
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindtype);
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> resultMap = new ArrayList<>();
            int total = 0;
            if (monitorPointTypeEnum == null || remindTypeEnum == null) {
                result.put("total", total);
                result.put("datalist", resultMap);
                return AuthUtil.parseJsonKeyToLower("success", result);
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("day", starttime, endtime);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            //根据监测点类型获取mn号
            Set<String> mns = new HashSet<>();
            List<Map<String, Object>> mndata = getMnsByParam(monitorPointTypeEnum);
            Map<String, String> pollutantMap = new HashMap<>();
            Map<String, String> pointNameMnMap = new HashMap<>();
            Map<String, String> mnAndPkIDs = new HashMap<>();
            Set<String> pollutantCodes = new HashSet<>();
            for (Map<String, Object> map : mndata) {
                String dgimn = map.get("DGIMN").toString();
                if (map.get("Code") != null) {
                    pollutantCodes.add(map.get("Code").toString());
                    pollutantMap.put(map.get("Code").toString(), map.get("Name").toString());
                }
                if (map.get("EntName") != null) {
                    pointNameMnMap.put(dgimn, map.get("EntName").toString());
                }
                if (!mnAndPkIDs.containsKey(dgimn)) {
                    mnAndPkIDs.put(dgimn, map.get("ID").toString());
                }
                mns.add(dgimn);
            }
            queryVO.setMns(mns);
            queryVO.setPollutantCodes(pollutantCodes);
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);
            queryVO.setCollection(collection);
            List<Document> documents = getPollutantAlarmDataByParam4(queryVO, remindTypeEnum);
            for (Document document : documents) {
                String dataGatherCode = document.getString("DataGatherCode");
                String entName = pointNameMnMap.get(dataGatherCode);
                String id = mnAndPkIDs.get(dataGatherCode);
                document.put("entName", entName);
                document.put("id", id);
            }
            //根据entName分组
            Map<String, List<Document>> listMap = documents.stream().collect(Collectors.groupingBy(m -> m.get("entName").toString() + "##" + m.get("id").toString()));
            for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                Map<String, Object> map1 = new HashMap<>();
                Set<String> mnsets = new HashSet<>();
                String key = entry.getKey();
                String[] split = key.split("##");
                String monitorPointName = split[0];
                String monitorPointID = split[1];
                int num = 0;
                Date lastTime = null;
                List<Map<String, Object>> pollutants = new ArrayList<>();
                Map<String, Integer> pollutantsMap = new HashMap<>();
                List<Document> documents1 = entry.getValue();
                for (Document document : documents1) {
                    String dataGatherCode = document.getString("DataGatherCode");
                    mnsets.add(dataGatherCode);
                    Date lastTimeInfo = document.getDate("LastTime");
                    if (lastTime == null) {
                        lastTime = lastTimeInfo;
                    } else if (lastTimeInfo.after(lastTime)) {
                        lastTime = lastTimeInfo;
                    }
                    Integer num1 = document.getInteger("num");
                    num = num + num1;
                    String pollutantCode = document.getString("PollutantCode");
                    String pollutantName = pollutantMap.get(pollutantCode);
                    if (pollutantsMap.containsKey(pollutantName)) {
                        Integer integer = pollutantsMap.get(pollutantName);
                        int i = integer + num1;
                        pollutantsMap.put(pollutantName, i);
                    } else {
                        pollutantsMap.put(pollutantName, num1);
                    }
                }
                for (Map.Entry<String, Integer> stringIntegerEntry : pollutantsMap.entrySet()) {
                    Map<String, Object> map = new HashMap<>();
                    String key1 = stringIntegerEntry.getKey();
                    Integer value = stringIntegerEntry.getValue();
                    map.put("pollutantname", key1);
                    map.put("num", value);
                    pollutants.add(map);
                }
                map1.put("monitorpointname", monitorPointName);
                map1.put("mns", mnsets);
                map1.put("monitorpointid", monitorPointID);
                map1.put("num", num);
                String charSequence = "";
                if (lastTime != null) {
                    String date = DataFormatUtil.getDate(lastTime);
                    charSequence = date.subSequence(11, date.length()).toString();
                }
                map1.put("lasttime", charSequence);
                map1.put("pollutants", pollutants);
                resultMap.add(map1);
                total = num + total;
            }
            if (pagenum != null && pagesize != null && resultMap.size() != 0) {
                List<Map<String, Object>> collecta = resultMap.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                result.put("datalist", collecta);
            } else {
                result.put("datalist", resultMap);
            }
            result.put("total", resultMap.size());
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 下午 7:05
     * @Description: 自定义查询条件统计点位报警详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPointAlarmDetailDataByParam", method = RequestMethod.POST)
    public Object countPointAlarmDetailDataByParam(@RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime,
                                                   @RequestJson(value = "remindtype") Integer remindtype,
                                                   @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
                                                   @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                   @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", Arrays.asList(monitorpointtype));
            String mnCommon;
            int type;
            Map<String, Integer> mnAndType = new HashMap<>();
            List<String> mns = new ArrayList<>();
            if (StringUtils.isNotBlank(monitorpointcategory)) {
                paramMap.put("monitorPointCategorys", Arrays.asList(monitorpointcategory));
                List<Map<String, Object>> inMnDatas = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                if (inMnDatas.size() > 0) {
                    for (Map<String, Object> inMnData : inMnDatas) {
                        mnCommon = inMnData.get("dgimn").toString();
                        type = Integer.parseInt(inMnData.get("FK_MonitorPointTypeCode").toString());
                        mnAndType.put(mnCommon, type);
                        mns.add(mnCommon);
                    }
                }
            } else {
                List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
                for (Map<String, Object> mapIndex : deviceStatus) {
                    mnCommon = mapIndex.get("dgimn").toString();
                    type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                    mnAndType.put(mnCommon, type);
                    mns.add(mnCommon);
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            Map<String, Integer> mnAndCount = new HashMap<>();
            Map<String, Date> mnAndMaxTime = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            Map<String, List<Map<String, Object>>> mnAndAlarmDataList = countMnPollutantMaxTime(remindtype, mnAndCount, mnAndMaxTime, paramMap);

            List<Map<String, Object>> dataList = new ArrayList<>();
            if (mnAndAlarmDataList.size() > 0) {
                paramMap.put("outputids", Arrays.asList());
                List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
                Map<String, Object> mnAndPollutionName = new HashMap<>();
                Map<String, Object> mnAndMonitorPointName = new HashMap<>();
                Map<String, Object> mnAndMonitorPointId = new HashMap<>();
                Map<String, Object> mnAndPollutionId = new HashMap<>();
                for (Map<String, Object> map : monitorPoints) {
                    mnCommon = map.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, map.get("monitorpointid"));
                    mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                }
                for (String mnIndex : mnAndAlarmDataList.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("totalnum", mnAndCount.get(mnIndex));
                    dataMap.put("mn", mnIndex);
                    dataMap.put("maxtime", DataFormatUtil.getDateYMDHMS(mnAndMaxTime.get(mnIndex)));
                    dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnIndex));
                    dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                    dataMap.put("pollutionname", mnAndPollutionName.get(mnIndex));
                    dataMap.put("pollutionid", mnAndPollutionId.get(mnIndex));
                    dataMap.put("pollutantdata", mnAndAlarmDataList.get(mnIndex));
                    dataList.add(dataMap);
                }
            }
            if (dataList.size() > 0) {
                //根据totalnum排序
                dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("totalnum").toString())).reversed()).collect(Collectors.toList());
                if (pagenum != null && pagesize != null) {
                    List<Map<String, Object>> subList = dataList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                    resultMap.put("datalist", subList);
                } else {
                    dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("totalnum").toString())).reversed()).collect(Collectors.toList());
                    resultMap.put("datalist", dataList);
                }
            }
            resultMap.put("total", dataList.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/24 0024 下午 2:11
     * @Description: 获取单个监测点类型的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getCodeAndNameByType(Integer type) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", type);
        List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
        paramMap.clear();
        for (Map<String, Object> map : pollutantData) {
            paramMap.put(map.get("code").toString(), map.get("name"));
        }
        return paramMap;
    }

    /**
     * @author: lip
     * @date: 2019/11/6 0006 下午 7:25
     * @Description: 统计mn号、污染物、最新时间数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, List<Map<String, Object>>> countMnPollutantMaxTime(Integer remindtype,
                                                                           Map<String, Integer> mnAndCount,
                                                                           Map<String, Date> mnAndMaxTime,
                                                                           Map<String, Object> paramMap) {
        Map<String, List<Map<String, Object>>> mnAndAlarmDataList = new HashMap<>();
        paramMap.put("remindtype", remindtype);
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindtype)) {
            case ConcentrationChangeEnum:
                paramMap.put("collection", "MinuteData");
                paramMap.put("timeKey", "MonitorTime");
                paramMap.put("unwindkey", "MinuteDataList");
                break;
            case FlowChangeEnum:
                paramMap.put("collection", "HourFlowData");
                paramMap.put("timeKey", "MonitorTime");
                paramMap.put("unwindkey", "HourFlowDataList");
                break;
            case OverAlarmEnum:
                paramMap.put("collection", "OverData");
                paramMap.put("timeKey", "OverTime");
                break;
            case EarlyAlarmEnum:
                paramMap.put("collection", "EarlyWarnData");
                paramMap.put("timeKey", "EarlyWarnTime");
                break;
            case ExceptionAlarmEnum:
                paramMap.put("collection", "ExceptionData");
                paramMap.put("timeKey", "ExceptionTime");
                break;
            case WaterNoFlowEnum://废水无流量异常
                paramMap.put("collection", "ExceptionData");
                paramMap.put("timeKey", "ExceptionTime");
                break;
            default:
                break;
        }
        List<Document> documents = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);
        if (documents.size() > 0) {
            int monitorPointType = Integer.parseInt(paramMap.get("monitorpointtype").toString());
            Map<String, Object> codeAndName = getCodeAndNameByType(monitorPointType);
            String mnCommon;
            Date monitorTime;
            int countnum;
            List<Map<String, Object>> alarmDataList;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                monitorTime = document.getDate("maxtime");
                countnum = document.getInteger("countnum");
                if (mnAndAlarmDataList.containsKey(mnCommon)) {
                    alarmDataList = mnAndAlarmDataList.get(mnCommon);
                    mnAndCount.put(mnCommon, mnAndCount.get(mnCommon) + countnum);
                    if (!DataFormatUtil.isNeedUpdate(monitorTime, mnAndMaxTime.get(mnCommon), 0)) {
                        mnAndMaxTime.put(mnCommon, monitorTime);
                    }
                } else {
                    alarmDataList = new ArrayList<>();
                    mnAndCount.put(mnCommon, countnum);
                    mnAndMaxTime.put(mnCommon, monitorTime);
                }
                Map<String, Object> alarmData = new HashMap<>();
                alarmData.put("pollutantcode", document.get("PollutantCode"));
                alarmData.put("pollutantname", codeAndName.get(document.get("PollutantCode")));
                alarmData.put("countnum", countnum);
                alarmDataList.add(alarmData);
                mnAndAlarmDataList.put(mnCommon, alarmDataList);
            }
        }
        return mnAndAlarmDataList;
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/2 13:15
     * @Description: 统计这些监测点类型内的企业这段时间报警企业个数  （只统计异常和超限报警）(报警企业个数)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmEntNumByParam", method = RequestMethod.POST)
    public Object countAlarmEntNumByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorPointTypes,
                                          @RequestJson(value = "starttime") String starttime,
                                          @RequestJson(value = "endtime") String endtime) {
        try {
            Date startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
            Map resultMap = getAlarmEntNumByParam(monitorPointTypes, startTime, endTime);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Map getAlarmEntNumByParam(List<Integer> monitorPointTypes, Date starttime, Date endtime) {
        Map<String, Object> resultMap = new HashMap<>();
        if (monitorPointTypes.size() == 0) {
            return resultMap;
        }
        OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();

        queryVO.setStartTime(starttime);
        queryVO.setEndTime(endtime);
        Set<String> set = new HashSet<>();
        for (Integer pointType : monitorPointTypes) {
            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointType);
            Assert.notNull(monitorPointTypeEnum, "monitorPointTypeEnum must not be null!");
            CommonTypeEnum.RemindTypeEnum[] reminds = {ExceptionAlarmEnum, OverAlarmEnum};
            queryVO.setMonitorPointType(pointType);
            List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
            Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
            queryVO.setMns(mns);
            List<Document> documents = new ArrayList<>();
            for (CommonTypeEnum.RemindTypeEnum remindTypeEnum : reminds) {
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                queryVO.setCollection(collection);          //放入连接表
                documents.addAll(getAlarmDataByParam(queryVO, remindTypeEnum));
            }
            Set<String> alramMNs = new HashSet<>();
            for (Document document : documents) {
                String id = document.getString("_id");
                alramMNs.add(id);
            }
            for (Map<String, Object> mndatum : mndata) {
                if (alramMNs.contains(mndatum.get("DGIMN").toString())) {
                    String pk_pollutionID = mndatum.get("ID").toString();
                    set.add(pk_pollutionID);
                }
            }
        }
        resultMap.put("num", set.size());
        resultMap.put("pouutionids", set);
        return resultMap;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/21 14:55
     * @Description: 根据时间统计多个监测类型的统计报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> countMonitorPointsAlarmNumForTime(String timeType, List<Integer> monitorTypes, OnlineAlarmCountQueryVO queryVO) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Object>> data = new HashMap<>();
        for (Integer monitortype : monitorTypes) {
            CommonTypeEnum.MonitorPointTypeEnum codeByInt = getCodeByInt(monitortype);
            Assert.notNull(codeByInt, "监测类型错误");
            int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitortype);
            queryVO.setMonitorPointType(monitortype);
            countAlarmRemindNumByParam(reminds, queryVO, timeType, data);
        }
        if (data.size() > 0) {
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = entry.getValue();
                value.put("monitorTime", key);
                resultList.add(value);
            }
        }
        return resultList.stream().sorted(Comparator.comparing(m -> (m.get("monitorTime").toString()))).collect(Collectors.toList());
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/19 15:41
     * @Description: 根据时间数组处理结果集
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> formatKeyAlarmAndEarlyWarningData(Map<String, Integer> timeAndNum, String monitorTypeName) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        resultMap.put("monitortype", monitorTypeName.replace("点类型", ""));
        if (timeAndNum.size() == 0) {
            resultMap.put("timearr", resultList);
            return resultMap;
        }
        Map<String, Integer> result = new LinkedHashMap<>();    //根据数量排序
        timeAndNum.forEach((key, value) -> {
            if (result.size() != 3) {
                int i = key.indexOf(":");
                if (i == 1) {
                    key = 0 + key;
                }
                result.put(key, value);
            }
        });
        Map<String, Integer> resultInfo = new LinkedHashMap<>();    //根据时间排序
        result.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> resultInfo.put(x.getKey(), x.getValue()));
        resultInfo.forEach((String key, Integer value) -> {
            Map<String, Object> mapInfo = new HashMap<>();
            boolean b = key.startsWith("0", 0);
            if (b) {
                key = key.replaceFirst("0", "");
            }
            mapInfo.put("timerange", key);
            mapInfo.put("monitornum", value);
            resultList.add(mapInfo);
        });
        resultMap.put("timearr", resultList);
        return resultMap;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/19 14:36
     * @Description: 根据sessionID和菜单ID获取该用户和该菜单的子菜单的提醒个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMonitorAlarmNumBySessionIDAndMenuID(List<JSONObject> objectList, String usercode, String menuid) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> childrenByMenuId = getChildrenByMenuId(menuid, objectList, null);
        List<Map<String, Object>> ListMap = new ArrayList<>();
        if (childrenByMenuId == null) {
            resultMap.put("sum", 0);
            resultMap.put("datalist", ListMap);
            return resultMap;
        }
        OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
        queryVO.setUserId(usercode);
        LocalDate localDate = LocalDate.now();
        queryVO.setStartTime(DataFormatUtil.parseDate(localDate + " 00:00:00"));
        queryVO.setEndTime(DataFormatUtil.parseDate(localDate + " 23:59:59"));
        List<Map<String, Object>> menus = formatMenus(childrenByMenuId, dataList);    //将子集拿出放入list
        int sum = 0;
        Map<Integer, List<Map<String, Object>>> mndatas = new HashMap<>();
        for (Map<String, Object> menu : menus) {
            if (menu.get("menucode") != null) {
                String menuCode = menu.get("menucode").toString();
                String menuname = menu.get("menuname").toString();
                CommonTypeEnum.MonitorAlarmMenus alarmMenuEnum = CommonTypeEnum.MonitorAlarmMenus.getCodeByString(menuCode);
                List<Map<String, Object>> sonMenusByMenuCode = getSonMenusByMenuCode(menuCode, childrenByMenuId, null);
                int count = getEnumMenuAlarmNum(menuCode, queryVO, mndatas);  //获取该菜单预警个数
                if (sonMenusByMenuCode != null && sonMenusByMenuCode.size() > 0) {
                    count = getMenuAlarmNumBySysModel(sonMenusByMenuCode, count, queryVO, mndatas);   //子集不为空该菜单报警个数就为子菜单相加
                } else {    //没有子集
                    sum += count;   //最小级菜单个数相加为总报警个数
                }
                if ((sonMenusByMenuCode != null && sonMenusByMenuCode.size() > 0) || (alarmMenuEnum != null && alarmMenuEnum.getMonitorPointType() != null)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sysmodel", menuCode);
                    map.put("name", menuname);
                    map.put("count", count);
                    ListMap.add(map);
                }
            }
        }
        resultMap.put("sum", sum);
        resultMap.put("datalist", ListMap);
        return resultMap;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/17 17:02
     * @Description: 通过菜单code获取枚举出的菜单未读的提醒个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private int getEnumMenuAlarmNum(String menuCode, OnlineAlarmCountQueryVO queryVO, Map<Integer, List<Map<String, Object>>> mndatas) {
        CommonTypeEnum.MonitorAlarmMenus menuInfoEnum = CommonTypeEnum.MonitorAlarmMenus.getCodeByString(menuCode);
        if (menuInfoEnum != null) {
            if (menuCode.equals(CommonTypeEnum.MonitorAlarmMenus.app_yqqy.getCode())) { //园区企业报警个数
                Date startTime = queryVO.getStartTime();
                Date endTimeTime = queryVO.getEndTime();
                List<Integer> monitorPointTypes = new ArrayList<>();
                monitorPointTypes.add(WasteWaterEnum.getCode());  //废水
                monitorPointTypes.add(WasteGasEnum.getCode());    //废气
                monitorPointTypes.add(RainEnum.getCode());    //雨水
                monitorPointTypes.add(FactoryBoundaryStinkEnum.getCode());    //厂界恶臭
                monitorPointTypes.add(FactoryBoundarySmallStationEnum.getCode()); //厂界小型站
                Map alarmEntNumByParam = getAlarmEntNumByParam(monitorPointTypes, startTime, endTimeTime);
                return (int) alarmEntNumByParam.get("num");
            }
            Integer monitorPointType = menuInfoEnum.getMonitorPointType();
            if (monitorPointType == null) {
                return 0;
            }
            List<Map<String, Object>> mndata;
            if (mndatas.containsKey(menuInfoEnum.getMonitorPointType())) {
                mndata = mndatas.get(menuInfoEnum.getMonitorPointType());
            } else {
                queryVO.setMonitorPointType(monitorPointType);
                mndata = getMnsByMenuMonitorType(queryVO);
            }
            Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
            queryVO.setMns(mns);    //放入mn号
            boolean isApp = menuInfoEnum.isApp();
            Object remindTypes = menuInfoEnum.getRemindTypes();
            if (isApp) {    //目前app端都是多个
                queryVO.setUserId(null);
                if (remindTypes.getClass().isArray()) {
                    int[] reminds = (int[]) remindTypes;
                    List<Document> documents = new ArrayList<>();
                    //app需要根据企业id统计
                    for (int remind : reminds) {
                        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
                        String timeFieldName = getTimeFieldName(remindTypeEnum);
                        String collection = getCollection(remindTypeEnum);
                        queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                        queryVO.setCollection(collection);          //放入连接表
                        documents.addAll(getAlarmDataByParam(queryVO, remindTypeEnum));
                    }
                    Set<String> alramMNs = new HashSet<>();
                    for (Document document : documents) {
                        String id = document.getString("_id");
                        alramMNs.add(id);
                    }
                    Set<String> set = new HashSet<>();
                    for (Map<String, Object> mndatum : mndata) {
                        if (alramMNs.contains(mndatum.get("DGIMN").toString())) {
                            String pk_pollutionID = mndatum.get("ID").toString();
                            set.add(pk_pollutionID);
                        }
                    }
                    return set.size();
                }
            } else {    //目前pc端都是单个
                if (!remindTypes.getClass().isArray()) {
                    CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(Integer.parseInt(remindTypes.toString()));
                    Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
                    String timeFieldName = getTimeFieldName(remindTypeEnum);
                    String collection = getCollection(remindTypeEnum);
                    queryVO.setTimeFieldName(timeFieldName);
                    queryVO.setCollection(collection);
                    return getAlarmNumByParam(queryVO, remindTypeEnum);
                }
            }
        }
        return 0;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 10:35
     * @Description: 获取时间字段名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getCollection(CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        String collection = "";
        switch (remindTypeEnum) {
            case ExceptionAlarmEnum:
                collection = "ExceptionData";
                break;
            case OverAlarmEnum:
                collection = "OverData";
                break;
            case EarlyAlarmEnum:
                collection = "EarlyWarnData";
                break;
            case FlowChangeEnum:
                collection = "HourFlowData";
                break;
            case ConcentrationChangeEnum:
                collection = "MinuteData";
                break;
        }
        return collection;
    }

    private List<Map<String, Object>> getMnsByMenuMonitorType(OnlineAlarmCountQueryVO queryVO) {
        Map<String, Object> paramMap = new HashMap<>();
        int monitorPointType = queryVO.getMonitorPointType();
        List<Map<String, Object>> mns = new ArrayList<>();
        CommonTypeEnum.MonitorPointTypeEnum monitorEnum = getCodeByInt(monitorPointType);
        Assert.notNull(monitorEnum, "monitorEnum must not be null!");
        paramMap.put("monitorpointtype", monitorPointType);
        if (StringUtils.isNotBlank(queryVO.getPollutionID())) {
            paramMap.put("pollutionid", queryVO.getPollutionID());
        }
        if (StringUtils.isNotBlank(queryVO.getMonitorpointid())) {
            paramMap.put("monitorpointid", queryVO.getMonitorpointid());
        }
        if (StringUtils.isNotBlank(queryVO.getUserId())) {
            paramMap.put("userid", queryVO.getUserId());
        }
        switch (monitorEnum) {
            case WasteWaterEnum:
                mns = onlineService.getWaterMNSByParam(paramMap);
                break;
            case WasteGasEnum:
                mns = onlineService.getGasMNSByParam(paramMap);
                break;
            case SmokeEnum:
                mns = onlineService.getGasMNSByParam(paramMap);
                break;
            case RainEnum:
                mns = onlineService.getRainMNSByParam(paramMap);
                break;
            case AirEnum:
                mns = onlineService.getAirMNSByParam(paramMap);
                break;
            case WaterQualityEnum:
                mns = onlineService.getWaterStationMNSByParam(paramMap);
                break;
            case EnvironmentalStinkEnum:
            case EnvironmentalDustEnum://扬尘
                mns = onlineService.getStinkMNSByParam(paramMap);
                break;
            case MicroStationEnum:
                mns = onlineService.getStinkMNSByParam(paramMap);
                break;
            case EnvironmentalVocEnum:
                mns = onlineService.getVOCMNSByParam(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                mns = onlineService.getFactoryStinkMNSByParam(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                mns = onlineService.getFactorySmallStationMNSByParam(paramMap);
                break;
            /*case FactoryBoundaryDustEnum://扬尘
                mns = onlineService.getFactorySmallStationMNSByParam(paramMap);
                break;*/
            case SecurityCombustibleMonitor:
                mns = onlineService.getSecurityRiskAreaMonitorPointMNSByParam(paramMap);
                break;
            case SecurityToxicMonitor:
                mns = onlineService.getSecurityRiskAreaMonitorPointMNSByParam(paramMap);
                break;
            case StorageTankAreaEnum:
                mns = onlineService.getStorageTankAreaMNSByParam(paramMap);
                break;
        }
        return mns;
    }

    private List<Map<String, Object>> getMnsByParam(CommonTypeEnum.MonitorPointTypeEnum monitorEnum) {
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> mns = new ArrayList<>();
        Assert.notNull(monitorEnum, "monitorEnum must not be null!");
        paramMap.put("monitorpointtype", monitorEnum.getCode());
        switch (monitorEnum) {
            case WasteWaterEnum:
                mns = onlineService.getWaterMNSByParam(paramMap);
                break;
            case WasteGasEnum:
                mns = onlineService.getGasMNSByParam(paramMap);
                break;
            case RainEnum:
                mns = onlineService.getRainMNSByParam(paramMap);
                break;
            case AirEnum:
                mns = onlineService.getAirMNSByParam(paramMap);
                break;
            case EnvironmentalStinkEnum:
                mns = onlineService.getStinkMNSByParam(paramMap);
                break;
            case EnvironmentalVocEnum:
                mns = onlineService.getVOCMNSByParam(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                mns = onlineService.getFactoryStinkMNSByParam(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                mns = onlineService.getFactorySmallStationMNSByParam(paramMap);
                break;
        }
        return mns;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 10:35
     * @Description: 获取时间字段名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getTimeFieldName(CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        String timeFieldName = "MonitorTime";
        switch (remindTypeEnum) {
            case ExceptionAlarmEnum:
                timeFieldName = "ExceptionTime";
                break;
            case OverAlarmEnum:
                timeFieldName = "OverTime";
                break;
            case EarlyAlarmEnum:
                timeFieldName = "EarlyWarnTime";
                break;
        }
        return timeFieldName;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/17 16:57
     * @Description: 处理父子结构菜单，变为单一结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> formatMenus(List<Map<String, Object>> menuChildren, List<Map<String, Object>> resultList) {
        for (Map<String, Object> menuChild : menuChildren) {
            Map<String, Object> menuMap = new HashMap<>();
            menuChild.forEach(menuMap::put);
            menuMap.remove("datalistchildren");
            resultList.add(menuMap);
            Object info = menuChild.get("datalistchildren");
            if (info != null) {
                List<Map<String, Object>> datalistchildren = (List<Map<String, Object>>) info;
                if (datalistchildren.size() > 0) {
                    formatMenus(datalistchildren, resultList);
                }
            }
        }
        return resultList;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:04
     * @Description: pc端报警次数获取
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getAlarmRemindNumByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum, String timeType) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmNumByTimeType(queryVO, timeType);
        } else {
            queryVO.setUnwindFieldName(null);
            return onlineCountAlarmService.countOtherLAlarmNumByTimeType(queryVO, timeType);
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:04
     * @Description: 报警时间统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getAlarmTimesByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
            }
            return onlineCountAlarmService.getNDAndPFLAlarmHourTimes(queryVO);
        } else {
            return onlineCountAlarmService.getOtherAlarmHourTimes(queryVO);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 17:23
     * @Description: 获取数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void countAlarmRemindNumByParam(int[] reminds, OnlineAlarmCountQueryVO queryVO, String timeType, Map<String, Map<String, Object>> mapMap) {
        List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
        Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
        queryVO.setMns(mns);
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(timeType);
        if (mns != null && mns.size() > 0) {
            for (int remind : reminds) {
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);
                queryVO.setCollection(collection);
                List<Document> documents = getAlarmRemindNumByParam(queryVO, remindTypeEnum, timeStyle);
                if (timeType.equals("week")) {
                    for (Document document1 : documents) {
                        document1.put("week", DataFormatUtil.getTimeOfYMWByString(document1.getString("_id")).toString());
                    }
                    Map<String, List<Document>> listMap = new HashMap<>();
                    for (Document m : documents) {
                        listMap.computeIfAbsent(m.getString("week"), k -> new ArrayList<>()).add(m);
                    }
                    List<Document> documents1 = new ArrayList<>();
                    for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                        String time = entry.getKey();
                        List<Document> value = entry.getValue();
                        int num = 0;
                        for (Document document1 : value) {
                            Integer count1 = document1.getInteger("num");
                            num = num + count1;
                        }
                        Document document = new Document();
                        document.put("_id", time);
                        document.put("num", num);
                        documents1.add(document);
                    }
                    documents = documents1;
                }
                getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
                //判断是否为废水异常类型
                if (remind == ExceptionAlarmEnum.getCode() && queryVO.getMonitorPointType() == WasteWaterEnum.getCode()) {
                    countNoFlowExceptionDataNumByParam(queryVO, timeType, mapMap);
                }
            }
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/21 14:48
     * @Description: 根据什么统计，获取其各个提醒类型的监测数据 例如：根据时间统计，获取这个时间点各个提醒类型的报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getEveryOneRemindTypeAlaramNum(Map<String, Map<String, Object>> mapMap, CommonTypeEnum.RemindTypeEnum remindTypeEnum, List<Document> documents) {
        for (Document document : documents) {
            document.put("remindname", remindTypeEnum.getName());
            document.put("remindtype", remindTypeEnum.getCode());
            String time = document.getString("_id");
            Integer num = document.getInteger("num");
            if (!mapMap.containsKey(time)) {
                Map<String, Object> map = new HashMap<>();
                map.put(remindTypeEnum.getCode().toString(), num);
                mapMap.put(time, map);
            } else {
                Map<String, Object> objectMap = mapMap.get(time);
                if (objectMap.containsKey(remindTypeEnum.getCode().toString())) {
                    int number = Integer.parseInt(objectMap.get(remindTypeEnum.getCode().toString()).toString());
                    objectMap.put(remindTypeEnum.getCode().toString(), num + number);
                } else {
                    objectMap.put(remindTypeEnum.getCode().toString(), num);
                }
            }
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/20 17:23
     * @Description: 获取数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void countAlarmRemindNum(int[] reminds,
                                    OnlineAlarmCountQueryVO queryVO,
                                    Map<String, Map<String, Object>> mapMap, int countByWhat
    ) {
        for (int remind : reminds) {
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
            Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);
            queryVO.setCollection(collection);
            List<Document> documents = new ArrayList<>();
            switch (countByWhat) {
                case 1:
                    documents = getAlarmDataByParam(queryVO, remindTypeEnum);
                    break;
                case 2:
                    documents = getPollutantAlarmDataByParam(queryVO, remindTypeEnum);
                    break;
            }
            getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/04/29 14:00
     * @Description: 获取数据（新）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void countAllAlarmRemindNum(int[] reminds,
                                       OnlineAlarmCountQueryVO queryVO,
                                       Map<String, Map<String, Object>> mapMap, int countByWhat
    ) {
        for (int remind : reminds) {
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
            Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);
            queryVO.setCollection(collection);
            List<Document> documents = new ArrayList<>();
            switch (countByWhat) {
                case 1:
                    documents = getAllAlarmDataByParam(queryVO, remindTypeEnum);
                    break;
                case 2:
                    documents = getAllPollutantAlarmDataByParam(queryVO, remindTypeEnum);
                    break;
            }
            getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/19 02:17
     * @Description: 根据污染物统计返回污染物code及次数(新)
     * @updateUser:
     * @updateDate:
     * @updateDescription:ll
     * @param:
     * @return:
     */
    private List<Document> getAllPollutantAlarmDataByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum) {
            queryVO.setPollutantCodeFieldName("PollutantCode");
            return onlineCountAlarmService.countChangAlarmDataByPollutantCode(queryVO);
        } else if (remindTypeEnum == FlowChangeEnum) {
            queryVO.setUnwindFieldName("HourFlowDataList");
            queryVO.setPollutantCodeFieldName("HourFlowDataList.PollutantCode");
            return onlineCountAlarmService.countNDAndPFLAlarmDataByPollutantCode(queryVO);
        } else {
            queryVO.setUnwindFieldName(null);
            queryVO.setPollutantCodeFieldName("PollutantCode");
            return onlineCountAlarmService.countOtherAlarmDataByPollutantCode(queryVO);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/04/29 16:09
     * @Description: 根据mn号统计 返回mn号和个数(新)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getAllAlarmDataByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                return onlineCountAlarmService.countNDChangeLAlarmDataByMN(queryVO);
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
                return onlineCountAlarmService.countNDAndPFLAlarmDataByMN(queryVO);
            }
        } else {
            queryVO.setUnwindFieldName(null);
            return onlineCountAlarmService.countOtherAlarmDataByMN(queryVO);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 10:33
     * @Description: 获取单个监测类型多个报类型的各个报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getAlarmNumByOneMonitorType(CommonTypeEnum.MonitorPointTypeEnum anEnum,
                                                            int[] reminds,
                                                            OnlineAlarmCountQueryVO queryVO) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
        Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
        queryVO.setMns(mns);
        Set<String> alarmMn = new HashSet<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (int remind : reminds) {
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
            Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);
            queryVO.setCollection(collection);
            int alarmNum = getAlarmNumByParam(queryVO, remindTypeEnum);
            Map<String, Object> map = new HashMap<>();
            map.put("num", alarmNum);
            map.put("remindcode", remindTypeEnum.getCode());
            map.put("remindname", remindTypeEnum.getName());
            //获取报警监测点个数
            List<Document> alarmDataByParam = getAlarmDataByParam(queryVO, remindTypeEnum);
            for (Document document : alarmDataByParam) {
                alarmMn.add(document.getString("_id"));
            }
            resultList.add(map);
        }
        resultMap.put("count", alarmMn.size());
        resultMap.put("monitorpointtype", anEnum.getCode());
        resultMap.put("monitorpointtypename", anEnum.getName().replace("点类型", ""));
        resultMap.put("data", resultList);
        return resultMap;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:05
     * @Description: 根据mn号统计 返回mn号和个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getAlarmDataByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmDataByMN(queryVO);
        } else {
            queryVO.setUnwindFieldName(null);
            return onlineCountAlarmService.countOtherAlarmDataByMN(queryVO);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:05
     * @Description: 根据污染物统计返回污染物code及次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getPollutantAlarmDataByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
                queryVO.setPollutantCodeFieldName("MinuteDataList.PollutantCode");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
                queryVO.setPollutantCodeFieldName("HourFlowDataList.PollutantCode");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmDataByPollutantCode(queryVO);
        } else {
            queryVO.setUnwindFieldName(null);
            queryVO.setPollutantCodeFieldName("PollutantCode");
            return onlineCountAlarmService.countOtherAlarmDataByPollutantCode(queryVO);
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:05
     * @Description: 根据污染物统计返回污染物code、次数、时间数组、最新报警时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getPollutantAlarmDataByParam2(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum, String timeStyle) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
                queryVO.setPollutantCodeFieldName("MinuteDataList.PollutantCode");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
                queryVO.setPollutantCodeFieldName("HourFlowDataList.PollutantCode");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmDataByPollutantCode2(queryVO, timeStyle);
        } else {
            queryVO.setUnwindFieldName(null);
            queryVO.setPollutantCodeFieldName("PollutantCode");
            return onlineCountAlarmService.countOtherAlarmDataByPollutantCode2(queryVO, timeStyle);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:05
     * @Description: 根据MN号和污染物统计返回污染物code、次数、最新报警时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getPollutantAlarmDataByParam4(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
                queryVO.setPollutantCodeFieldName("MinuteDataList.PollutantCode");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
                queryVO.setPollutantCodeFieldName("HourFlowDataList.PollutantCode");
            }
            return onlineCountAlarmService.countAlarmDataByMnAndPollutantCode(queryVO);
        } else {
            queryVO.setUnwindFieldName(null);
            queryVO.setPollutantCodeFieldName("PollutantCode");
            return onlineCountAlarmService.countAlarmDataByMnAndPollutantCode(queryVO);
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/22 16:49
     * @Description: 单个监测点当天（某一天）报警报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneMonitorPointAlarmInfoByParam", method = RequestMethod.POST)
    public Object getOneMonitorPointAlarmInfoByParam(@RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                     @RequestJson(value = "endtime") String endtime) {
        try {
            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (monitorPointTypeEnum == null) {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitorpointtype);
            queryVO.setMonitorPointType(monitorpointtype);
            Set<String> mns = new HashSet<>();
            mns.add(dgimn);
            queryVO.setMns(mns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            for (int remind : reminds) {
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                queryVO.setCollection(collection);          //放入连接表
                List<Document> documents = getPollutantAlarmDataByParam3(queryVO, remindTypeEnum, timeStyle);
                if (documents.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("alamtype", remind);
                    String thetime = "";
                    List<Map<String, Object>> pollutantlist = new ArrayList<>();
                    for (Document document : documents) {
                        if (!"".equals(thetime)) {
                            Date d1 = DataFormatUtil.parseDate(thetime);
                            Date d2 = DataFormatUtil.parseDate(document.getString("MaxTime"));
                            int d = (int) (d2.getTime()) / 1000 - (int) (d1.getTime()) / 1000;
                            if (d > 0) {
                                thetime = document.getString("MaxTime");
                            }
                        } else {
                            thetime = document.getString("MaxTime");
                        }
                        Map<String, Object> pollutantmap = new HashMap<>();
                        pollutantmap.put("pollutantname", codemap.get(document.getString("_id")));
                        pollutantmap.put("num", document.getInteger("num"));
                        pollutantlist.add(pollutantmap);
                    }
                    map.put("lasttime", thetime);
                    map.put("pollutants", pollutantlist);
                    resultList.add(map);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:05
     * @Description: 根据污染物统计返回污染物code及次数、最新时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getPollutantAlarmDataByParam3(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum, String timeStyle) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
                queryVO.setPollutantCodeFieldName("MinuteDataList.PollutantCode");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
                queryVO.setPollutantCodeFieldName("HourFlowDataList.PollutantCode");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmDataByPollutantCode3(queryVO, timeStyle);
        } else {
            queryVO.setUnwindFieldName(null);
            queryVO.setPollutantCodeFieldName("PollutantCode");
            return onlineCountAlarmService.countOtherAlarmDataByPollutantCode3(queryVO, timeStyle);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 17:04
     * @Description: pc端报警次数获取
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private int getAlarmNumByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmNumInHourData(queryVO);
        } else {
            queryVO.setUnwindFieldName(null);
            return onlineCountAlarmService.countOtherLAlarmNum(queryVO);
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/18 13:23
     * @Description: 通过menuid获取子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getChildrenByMenuId(String menuid, List<JSONObject> objectList, List<Map<String, Object>> dataList) {
        if (objectList != null && dataList == null) {
            for (JSONObject jsonObject : objectList) {
                if (menuid.equals(jsonObject.get("menuid").toString())) {
                    dataList = (List<Map<String, Object>>) jsonObject.get("datalistchildren");
                    break;
                } else {
                    objectList = (List<JSONObject>) jsonObject.get("datalistchildren");
                    dataList = getChildrenByMenuId(menuid, objectList, dataList);
                }
            }
        }
        return dataList;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/17 18:20
     * @Description: 通过菜单code获取子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getSonMenusByMenuCode(String menuCode, List<Map<String, Object>> menus, List<Map<String, Object>> result) {
        if (menus != null && result == null) {
            for (Map<String, Object> map : menus) {
                Object menuCodeInfo = map.get("menucode");
                if (menuCodeInfo != null) {
                    if (menuCode.equals(menuCodeInfo)) {
                        result = (List<Map<String, Object>>) map.get("datalistchildren");
                        break;
                    } else {
                        menus = (List<Map<String, Object>>) map.get("datalistchildren");
                        result = getSonMenusByMenuCode(menuCodeInfo.toString(), menus, result);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/17 18:21
     * @Description: 统计总的提醒个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    private int getMenuAlarmNumBySysModel(List<Map<String, Object>> childrenMenus, int num, OnlineAlarmCountQueryVO queryVO, Map<Integer, List<Map<String, Object>>> mndatas) {
        for (Map<String, Object> map : childrenMenus) {
            Object menucode = map.get("menucode");
            if (menucode != null) {
                num += getEnumMenuAlarmNum(menucode.toString(), queryVO, mndatas);
                Object info = map.get("datalistchildren");
                if (info != null) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) info;
                    if (list.size() > 0) {
                        getMenuAlarmNumBySysModel(list, num, queryVO, mndatas);
                    }
                }
            }
        }
        return num;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/21 14:40
     * @Description: 排名统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> formatDataByRanking(Map<String, Map<String, Object>> data, Map<String, String> enNameMap, String keyName) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (data.size() > 0) {
            Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String entryKey = entry.getKey();
                Map<String, Object> value = entry.getValue();
                if (enNameMap.containsKey(entryKey)) {
                    String entName = enNameMap.get(entryKey);
                    if (resultMap.containsKey(entName)) {
                        resultMap.get(entName).add(value);
                    } else {
                        List<Map<String, Object>> list = new ArrayList<>();
                        list.add(value);
                        resultMap.put(entName, list);
                    }
                }
            }
            for (Map.Entry<String, List<Map<String, Object>>> entry : resultMap.entrySet()) {
                String entName = entry.getKey();
                List<Map<String, Object>> value = entry.getValue();
                Map<String, Object> map1 = new HashMap<>();
                int count = 0;
                for (Map<String, Object> map : value) {
                    for (Map.Entry<String, Object> map2 : map.entrySet()) {
                        String key = map2.getKey();
                        int value1 = Integer.parseInt(map2.getValue().toString());
                        if (map1.containsKey(key)) {
                            int value2 = Integer.parseInt(map1.get(key).toString());
                            map1.put(key, value1 + value2);
                            count = count + value2;
                        } else {
                            map1.put(key, value1);
                            count = count + value1;
                        }
                    }
                }
                map1.put(keyName, entName);
                map1.put("count", count);
                resultList.add(map1);
            }
            return resultList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("count").toString())).reversed()).collect(Collectors.toList());
        }
        return resultList;
    }

    /**
     * @author: xsm
     * @date: 2019/8/13 17:12
     * @Description: 根据时间段和自定义参数统计企业预警报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutionEarlyAlarmNumByParamMap", method = RequestMethod.POST)
    public Object countPollutionEarlyAlarmNumByParamMap(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "datamark", required = false) String datamark,
                                                        @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                        @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                        @RequestJson(value = "endtime") String endtime) {
        try {
            List<Integer> monitortypes = new ArrayList<>();
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            if (monitorpointtype != null) {//判断监测点类型是否为空
                monitortypes.add(monitorpointtype);
            } else {//为空则查全部类型
                monitortypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            }
            Map<String, Map<String, Object>> data = new HashMap<>();
            Map<String, String> enNameMap = new HashMap<>();
            if (pollutantcode != null && !"".equals(pollutantcode)) {
                Set<String> pollutants = new HashSet<>();
                pollutants.add(pollutantcode);
                queryVO.setPollutantCodes(pollutants);
                queryVO.setPollutantCodeFieldName("PollutantCode");
            }
            for (Integer monitortype : monitortypes) {
                CommonTypeEnum.MonitorPointTypeEnum typeEnum = getCodeByInt(monitortype);
                Assert.notNull(typeEnum, "监测类型错误");
                queryVO.setMonitorPointType(typeEnum.getCode());
                List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
                Set<String> mns = new HashSet<>();
                for (Map<String, Object> document : mndata) {
                    String dgimn = document.get("DGIMN").toString();
                    String entName = document.get("EntName").toString();
                    mns.add(dgimn);
                    enNameMap.put(dgimn, entName);
                }
                queryVO.setMns(mns);

                if (datamark != null && !"".equals(datamark)) {
                    if ("early".equals(datamark)) {
                        int[] reminds = new int[]{CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode(), CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode(), CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()};
                        countAlarmRemindNum(reminds, queryVO, data, 1);
                    } else if ("alarm".equals(datamark)) {
                        int[] reminds = new int[]{CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode(), CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()};
                        countAlarmRemindNum(reminds, queryVO, data, 1);
                    }
                } else {
                    int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitortype);
                    countAlarmRemindNum(reminds, queryVO, data, 1);
                }
            }
            List<Map<String, Object>> list = formatDataByRanking(data, enNameMap, "pollutionname");
            List<Map<String, Object>> resultListInfo = new ArrayList<>();
            for (Map<String, Object> map : list) {
                if (resultListInfo.size() == 5) {
                    break;
                }
                resultListInfo.add(map);
            }
          /*  if (result != null && result.size() > 0) {//按报警次数倒叙
                //按报警次数倒叙
                Comparator<Object> comparebynum = Comparator.comparingInt(m -> Integer.parseInt(((Map) m).get("num").toString())).reversed();
                result = result.stream().sorted(comparebynum).collect(Collectors.toList());
            }*/
            return AuthUtil.parseJsonKeyToLower("success", resultListInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 7:36
     * @Description: 统计污染源下或者监测点下，污染物浓度突变次数，污染物排放量突变次数，污染物超阈次数，污染物异常次数及污染物超限次数
     * @updateUser:xsm
     * @updateDate:2020/03/17
     * @updateDescription:异常不包括废水无流量异常，新增废水无流量异常次数
     * @param: [starttime, datetype, monitorpointtype, endtime]
     * @throws:
     */
    @RequestMapping(value = "countConcentrationChangeDataByParamMap", method = RequestMethod.POST)
    public Object countConcentrationChangeDataByParamMap(@RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "datetype") String datetype,
                                                         @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                         @RequestJson(value = "monitorpointtypes", required = false) Object monitorpointtypes,
                                                         @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            List<Integer> integers = (List<Integer>) monitorpointtypes;
            Map<String, Object> resultMap = new HashMap<>();
            List<Integer> monitortypes = new ArrayList<>();
            monitortypes.add(WasteWaterEnum.getCode());
            monitortypes.add(WasteGasEnum.getCode());
            monitortypes.add(SmokeEnum.getCode());
            monitortypes.add(RainEnum.getCode());
            monitortypes.add(unOrganizationWasteGasEnum.getCode());
            monitortypes.add(FactoryBoundarySmallStationEnum.getCode());
            monitortypes.add(FactoryBoundaryStinkEnum.getCode());

            List<Map<String, Object>> collect = new ArrayList<>();
            List<Map<String, Object>> collect1 = new ArrayList<>();
            List<Map<String, Object>> collect2 = new ArrayList<>();
            List<Map<String, Object>> collect3 = new ArrayList<>();
            List<Map<String, Object>> collect4 = new ArrayList<>();
            //废水无流量异常
            List<Map<String, Object>> collect5 = new ArrayList<>();
            boolean isnoflow = false;
            if (integers == null || integers.size() == 0) {
                if (monitorpointtype == WasteWaterEnum.getCode()) {
                    isnoflow = true; //当所查类型为废水时  无流量标记为true
                }
                setData(starttime, datetype, monitorpointtype, endtime, monitortypes, collect, collect1, collect2, collect3, collect4, collect5);
            } else {
                for (Integer integer : integers) {
                    monitorpointtype = integer;
                    if (monitorpointtype == WasteWaterEnum.getCode()) {
                        isnoflow = true; //当所查类型为废水时  无流量标记为true
                    }
                    setData(starttime, datetype, monitorpointtype, endtime, monitortypes, collect, collect1, collect2, collect3, collect4, collect5);
                }
            }
            resultMap.put("ConcentrationChange", collect);
            resultMap.put("FlowChange", collect1);
            resultMap.put("EarlyAlarm", collect2);
            resultMap.put("ExceptionAlarm", collect3);
            resultMap.put("OverAlarm", collect4);
            if (isnoflow == true) {
                resultMap.put("NoFlowException", collect5);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/12/12 0012 下午 5:16
     * @Description: 组装结果数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, datetype, monitorpointtype, endtime, monitortypes, collect, collect1, collect2, collect3, collect4]
     * @throws:
     */
    private void setData(String starttime, String datetype, Integer monitorpointtype, String endtime, List<Integer> monitortypes,
                         List<Map<String, Object>> collect, List<Map<String, Object>> collect1, List<Map<String, Object>> collect2, List<Map<String, Object>> collect3, List<Map<String, Object>> collect4, List<Map<String, Object>> collect5) throws ParseException {
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> monitorInfo = new ArrayList<>();
        List<Map<String, Object>> pollutants = new ArrayList<>();
        String groupName = "";
        String groupId = "";
        //查询监测点信息
        paramMap.put("monitortype", monitorpointtype);
        paramMap.put("pollutanttype", monitorpointtype);
        if (monitortypes.contains(monitorpointtype)) {
            monitorInfo = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
            groupName = "pollutionname";
            groupId = "pk_pollutionid";
        } else if (monitorpointtype == AirEnum.getCode()) {//大气
            monitorInfo = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            groupName = "monitorpointname";
            groupId = "pk_id";
        } else if (monitorpointtype == EnvironmentalVocEnum.getCode() || monitorpointtype == EnvironmentalStinkEnum.getCode()) {//voc//恶臭
            monitorInfo = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            groupName = "monitorpointname";
            groupId = "pk_id";
        }

        //查询污染物信息
        pollutants = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);


        List<String> dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
        paramMap.put("dgimns", dgimns);
        paramMap.put("starttime", starttime);
        paramMap.put("endtime", endtime);
        paramMap.put("datetype", datetype);

        //查询数据
        List<Map> maps = onlineCountAlarmService.countConcentrationChangeDataByParamMap(paramMap);//浓度
        List<Map> maps1 = onlineCountAlarmService.countFlowChangeDataByParamMap(paramMap);//排放量
        List<Map> maps2 = onlineCountAlarmService.countEarlyWarnDataByParamMap(paramMap);//超阈
        List<Map> maps3 = onlineCountAlarmService.countExceptionDatayParamMap(paramMap);//异常
        List<Map> maps4 = onlineCountAlarmService.countOverDataDataByParamMap(paramMap);//超限


        //往数据中添加污染源或者排口名称，污染物名称
        collect.addAll(setPollutantAndMonitorinfo(maps, monitorInfo, pollutants, groupName, groupId));
        collect1.addAll(setPollutantAndMonitorinfo(maps1, monitorInfo, pollutants, groupName, groupId));
        collect2.addAll(setPollutantAndMonitorinfo(maps2, monitorInfo, pollutants, groupName, groupId));
        collect3.addAll(setPollutantAndMonitorinfo(maps3, monitorInfo, pollutants, groupName, groupId));
        collect4.addAll(setPollutantAndMonitorinfo(maps4, monitorInfo, pollutants, groupName, groupId));
        if (monitorpointtype == WasteWaterEnum.getCode()) {
            //废水无流量异常
            List<Map> maps5 = onlineCountAlarmService.countNoFlowExceptionDatayParamMap(paramMap);//废水无流量异常
            collect5.addAll(setPollutantAndMonitorinfo(maps5, monitorInfo, pollutants, groupName, groupId));
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 7:34
     * @Description: 设置污染物及监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [maps, monitorInfo, pollutants, groupName, groupId]
     * @throws:
     */
    private List<Map<String, Object>> setPollutantAndMonitorinfo(List<Map> maps, List<Map<String, Object>> monitorInfo, List<Map<String, Object>> pollutants, String groupName, String groupId) {
        Map<Object, List<Map>> collect = maps.stream().peek(m -> {
            monitorInfo.stream().filter(monitor -> monitor.get("dgimn") != null && m.get("DataGatherCode") != null && m.get("DataGatherCode").toString().equals(monitor.get("dgimn").toString()))
                    .peek(monitorinfo1 -> {
                        m.put(groupName, monitorinfo1.get(groupName));
                        m.put(groupId, monitorinfo1.get(groupId));
                    }).collect(Collectors.toList());
            pollutants.stream().filter(pollutant -> pollutant.get("pollutantcode") != null && m.get("PollutantCode") != null && pollutant.get("pollutantcode").toString().equals(m.get("PollutantCode").toString()))
                    .peek(pollutant1 -> {
                        m.put("pollutantname", pollutant1.get("pollutantname"));
                    }).collect(Collectors.toList());
        }).collect(Collectors.groupingBy(m -> m.get(groupName)));

        List<Map<String, Object>> resultlist = new ArrayList<>();
        for (Object o : collect.keySet()) {
            Map<String, Object> result = new HashMap<>();
            result.put(groupName, o);
            result.put("statisticsinfo", collect.get(o));
            resultlist.add(result);
        }

        return resultlist;
    }

    /**
     * @author: xsm
     * @date: 2019/11/05 18:31
     * @Description: 根据污染源ID获取该污染源各报警类型数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAlarmMonthDataByPollutionId", method = RequestMethod.POST)
    public Object getAlarmMonthDataByPollutionId(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            //获取当前时间
            String endtime = DataFormatUtil.getDateYMD(new Date());//当前时间
            String starttime = DataFormatUtil.getDateYM(new Date()) + "-01"; //当前月的第一天
            Date preMonth = DataFormatUtil.getDateYM(starttime);
            String preStartTime = DataFormatUtil.getDateYMD(DataFormatUtil.getPreMonthDate(preMonth, 1));//上个月
            //根据污染源ID获取该污染源下所有MN号
            List<Map<String, Object>> allmnlist = pollutionService.getOutputInfosByPollutionID(pollutionid);
            List<String> mns = new ArrayList<>();
            if (allmnlist != null && allmnlist.size() > 0) {
                for (Map<String, Object> map : allmnlist)
                    mns.add(map.get("DGIMN").toString());
            }
            Map<String, Object> mapData = onlineCountAlarmService.countAlarmMonthDataByParamMap(preStartTime, endtime, mns);
            return AuthUtil.parseJsonKeyToLower("success", mapData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/11/07 9:50
     * @Description: 根据污染源ID获取该污染源下重点污染物排放量情况（废水废气雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getKeyPollutantYearFlowDataByPollutionID", method = RequestMethod.POST)
    public Object getKeyPollutantYearFlowDataByPollutionID(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            //获取当前时间
            String endtime = DataFormatUtil.getDateY(new Date());//当前年
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, -1);
            String starttime = DataFormatUtil.getDateY(calendar.getTime()); //前一年
            //根据污染源ID获取该污染源下所有MN号
            List<Map<String, Object>> allmnlist = pollutionService.getOutputInfosByPollutionID(pollutionid);
            List<String> mns = new ArrayList<>();
            Set set = new HashSet();
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (allmnlist != null && allmnlist.size() > 0) {
                for (Map<String, Object> map : allmnlist) {
                    mns.add(map.get("DGIMN").toString());
                    set.add(map.get("type"));
                }
                Map<String, Object> parammap = new HashMap<>();
                parammap.put("pollutanttypes", set);
                //根据监测类型获取该污染源下所有重点排放污染物
                List<Map<String, Object>> flowpollutants = pollutionService.getKeyFlowPollutantsByParam(parammap);
                List<String> pollutants = new ArrayList<>();
                parammap.clear();
                for (Map<String, Object> map : flowpollutants) {
                    pollutants.add(map.get("code").toString());
                    parammap.put(map.get("code").toString(), map.get("name"));
                }
                resultList = onlineCountAlarmService.getKeyPollutantYearFlowData(starttime, endtime, mns, pollutants, parammap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/11/08 09:34
     * @Description: 根据自定义查询条件统计单个或多个类型监测点下各个报警类型的个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmNumForMonitorTypeAndRemindTypeByParam", method = RequestMethod.POST)
    public Object countAlarmNumForMonitorTypeAndRemindTypeByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                                  @RequestJson(value = "starttime") String starttime,
                                                                  @RequestJson(value = "timetype") String timetype,
                                                                  @RequestJson(value = "reminds", required = false) List<Integer> reminds,
                                                                  @RequestJson(value = "endtime") String endtime) {

        try {
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timetype, starttime, endtime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            Map<String, Object> mnmap = new HashMap<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                OnlineAlarmCountQueryVO obj = new OnlineAlarmCountQueryVO();
                Set<String> mns = new HashSet<String>();
                for (Integer i : monitorpointtypes) {
                    obj.setMonitorPointType(i);
                    List<Map<String, Object>> mndata = getMnsByMenuMonitorType(obj);
                    mns.addAll(mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet()));
                    mnmap.put(i.toString(), mndata);
                }
                queryVO.setMns(mns);
            }
            Map<String, Object> result = countManyAlarmRemindNumByParam(reminds, queryVO, timetype, mnmap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 自定义查询条件统计报警/异常数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/3/19 10:00
     */
    @RequestMapping(value = "countOverOrExceptionByParam", method = RequestMethod.POST)
    public Object countOverOrExceptionByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                              @RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "endtime") String endtime,
                                              @RequestJson(value = "counttype") String counttype) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                List<Map<String, Object>> pointList = new ArrayList<>();
                Map<String, Object> paramMap = new HashMap<>();
                List<String> mns = new ArrayList<>();
                for (Integer type : monitorpointtypes) {
                    paramMap.put("monitorpointtypecode", type);
                    pointList.addAll(onlineService.getMNAndMonitorPointByParam(paramMap));

                }
                if (pointList.size() > 0) {
                    for (Map<String, Object> pointMap : pointList) {
                        if (pointMap.get("dgimn") != null) {
                            mns.add(pointMap.get("dgimn").toString());
                        } else if (pointMap.get("DGIMN") != null) {
                            mns.add(pointMap.get("DGIMN").toString());
                        }
                    }
                    String collection = "";
                    String timeKey = "";
                    switch (counttype) {
                        case "over":
                            collection = DB_OverData;
                            timeKey = "OverTime";
                            break;
                        case "exception":
                            collection = DB_ExceptionData;
                            timeKey = "ExceptionTime";
                            break;
                    }
                    if (StringUtils.isNotBlank(collection)) {
                        paramMap.clear();
                        paramMap.put("timeKey", timeKey);
                        paramMap.put("starttime", starttime + " 00:00:00");
                        paramMap.put("endtime", endtime + " 23:59:59");
                        paramMap.put("collection", collection);
                        paramMap.put("mns", mns);
                        List<Document> documents = countOverOrExceptionByParam(paramMap);
                        Map<String, Integer> thisTimeAndNum = new HashMap<>();
                        if (documents.size() > 0) {
                            thisTimeAndNum = setTimeAndNum(documents);
                        }
                        String thatStartTime = DataFormatUtil.getDayTBDate(starttime);
                        String thatEndTime = DataFormatUtil.getDayTBDate(endtime);
                        paramMap.put("starttime", thatStartTime + " 00:00:00");
                        paramMap.put("endtime", thatEndTime + " 23:59:59");
                        documents = countOverOrExceptionByParam(paramMap);
                        Map<String, Integer> thatTimeAndNum = new HashMap<>();
                        if (documents.size() > 0) {
                            thatTimeAndNum = setTimeAndNum(documents);
                        }
                        List<String> timeList = DataFormatUtil.getYMDBetween(starttime, endtime);
                        timeList.add(endtime);
                        String tbTime;
                        for (String time : timeList) {
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("monitortime", time);
                            resultMap.put("thisnum", thisTimeAndNum.get(time) != null ? thisTimeAndNum.get(time) : 0);
                            tbTime = DataFormatUtil.getDayTBDate(time);
                            resultMap.put("thatnum", thatTimeAndNum.get(tbTime) != null ? thatTimeAndNum.get(tbTime) : 0);
                            resultList.add(resultMap);
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
     * @Description: 自定义查询条件统计报警/异常数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/3/19 10:00
     */
    @RequestMapping(value = "countPointOverOrExceptionByParam", method = RequestMethod.POST)
    public Object countPointOverOrExceptionByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime,
                                                   @RequestJson(value = "counttype") String counttype) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                List<Map<String, Object>> pointList = new ArrayList<>();
                Map<String, Object> paramMap = new HashMap<>();
                List<String> mns = new ArrayList<>();
                for (Integer type : monitorpointtypes) {
                    paramMap.put("monitorpointtypecode", type);
                    pointList.addAll(onlineService.getMNAndMonitorPointByParam(paramMap));

                }
                if (pointList.size() > 0) {
                    Map<String, String> mnAndPid = new HashMap<>();
                    Map<String, String> mnAndMid = new HashMap<>();
                    Map<String, String> midAndName = new HashMap<>();
                    Map<String, String> pidAndName = new HashMap<>();
                    String mnCommon;
                    String polltionid;
                    String monitorpointid;
                    for (Map<String, Object> pointMap : pointList) {
                        if (pointMap.get("dgimn") != null) {
                            mnCommon = pointMap.get("dgimn").toString();
                        } else if (pointMap.get("DGIMN") != null) {
                            mnCommon = pointMap.get("DGIMN").toString();
                        } else {
                            mnCommon = "";
                        }
                        if (StringUtils.isNotBlank(mnCommon)) {
                            mns.add(mnCommon);
                            monitorpointid = pointMap.get("monitorpointid") + "";
                            mnAndMid.put(mnCommon, monitorpointid);

                            if (pointMap.get("pollutionid") != null || pointMap.get("pk_pollutionid") != null) {//企业关联点位
                                polltionid = pointMap.get("pollutionid") != null ? pointMap.get("pollutionid").toString() : pointMap.get("pk_pollutionid") + "";
                                mnAndPid.put(mnCommon, polltionid);
                                if (pointMap.get("ShorterName") != null) {
                                    pidAndName.put(polltionid, pointMap.get("ShorterName") + "");
                                } else {
                                    pidAndName.put(polltionid, pointMap.get("shortername") + "");
                                }
                            } else {//非企业关联点位
                                midAndName.put(monitorpointid, pointMap.get("monitorpointname") + "");
                            }
                        }

                    }
                    String collection = "";
                    String timeKey = "";
                    switch (counttype) {
                        case "over":
                            collection = DB_OverData;
                            timeKey = "OverTime";
                            break;
                        case "exception":
                            collection = DB_ExceptionData;
                            timeKey = "ExceptionTime";
                            break;
                    }
                    if (StringUtils.isNotBlank(collection)) {
                        paramMap.clear();
                        paramMap.put("timeKey", timeKey);
                        paramMap.put("starttime", starttime + " 00:00:00");
                        paramMap.put("endtime", endtime + " 23:59:59");
                        paramMap.put("collection", collection);
                        paramMap.put("mns", mns);
                        List<Document> documents = countMnOverOrExceptionByParam(paramMap);
                        if (documents.size() > 0) {
                            Map<String, Integer> pidAndNum = new HashMap<>();
                            Map<String, Integer> midAndNum = new HashMap<>();

                            for (Document document : documents) {
                                mnCommon = document.getString("_id");
                                if (mnAndPid.containsKey(mnCommon)) {//企业点位
                                    polltionid = mnAndPid.get(mnCommon);
                                    if (pidAndNum.containsKey(polltionid)) {
                                        pidAndNum.put(polltionid, document.getInteger("countnum") + pidAndNum.get(polltionid));
                                    } else {
                                        pidAndNum.put(polltionid, document.getInteger("countnum"));
                                    }
                                } else if (mnAndMid.containsKey(mnCommon)) {
                                    monitorpointid = mnAndMid.get(mnCommon);
                                    if (midAndNum.containsKey(monitorpointid)) {
                                        midAndNum.put(monitorpointid, document.getInteger("countnum") + midAndNum.get(monitorpointid));
                                    } else {
                                        midAndNum.put(monitorpointid, document.getInteger("countnum"));
                                    }
                                }
                            }
                            if (pidAndNum.size() > 0) {
                                for (String pid : pidAndNum.keySet()) {
                                    Map<String, Object> resultMap = new HashMap<>();
                                    resultMap.put("id", pid);
                                    resultMap.put("name", pidAndName.get(pid));
                                    resultMap.put("num", pidAndNum.get(pid));
                                    resultList.add(resultMap);
                                }
                            }
                            if (midAndNum.size() > 0) {
                                for (String mid : midAndNum.keySet()) {
                                    Map<String, Object> resultMap = new HashMap<>();
                                    resultMap.put("id", mid);
                                    resultMap.put("name", midAndName.get(mid));
                                    resultMap.put("num", midAndNum.get(mid));
                                    resultList.add(resultMap);
                                }
                            }
                        }
                    }
                }
            }
            if (resultList.size() > 0) {
                //排序
                resultList = resultList.stream().sorted(
                        Comparator.comparing(m -> Integer.valueOf(((Map) m).get("num").toString())).reversed()).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Integer> setTimeAndNum(List<Document> documents) {
        Map<String, Integer> timeAndNum = new HashMap<>();
        String monitorTime;
        for (Document document : documents) {
            monitorTime = document.getString("_id");
            timeAndNum.put(monitorTime, Integer.parseInt(document.get("countnum").toString()));
        }
        return timeAndNum;
    }

    private List<Document> countOverOrExceptionByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();

        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        aggregations.add(match(criteria));
        aggregations.add(Aggregation.project(timeKey)
                .and(DateOperators.DateToString.dateOf(timeKey).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
        );
        GroupOperation groupOperation = group("MonitorTime").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    private List<Document> countMnOverOrExceptionByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        aggregations.add(match(criteria));
        GroupOperation groupOperation = group("DataGatherCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: xsm
     * @date: 2019/11/08 10:01
     * @Description: 获取数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> countManyAlarmRemindNumByParam(List<Integer> reminds, OnlineAlarmCountQueryVO queryVO, String timeType, Map<String, Object> mnmap) {
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(timeType);
        Map<String, Object> result = new HashMap<>();
        for (int remind : reminds) {
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
            Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);
            queryVO.setCollection(collection);
            List<Document> documents = new ArrayList<>();
            if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
                if (remindTypeEnum == ConcentrationChangeEnum) {
                    queryVO.setUnwindFieldName("MinuteDataList");
                } else {
                    queryVO.setUnwindFieldName("HourFlowDataList");
                }
                documents = onlineCountAlarmService.countConcentrationAndFlowChangeNumGroupByMNAndTime(queryVO, timeStyle);
            } else {
                queryVO.setUnwindFieldName(null);
                documents = onlineCountAlarmService.countEarlyOrOverAlarmNumGroupByMNAndTime(queryVO, timeStyle);
            }
            Map<String, Object> resultmap = new HashMap<>();
            if (documents.size() > 0) {
                if (mnmap.size() > 0) {
                    for (Map.Entry<String, Object> entry : mnmap.entrySet()) {
                        String key = entry.getKey();
                        List<Map<String, Object>> mnlist = (List<Map<String, Object>>) entry.getValue();
                        List<Map<String, Object>> listdata = new ArrayList<>();
                        for (Document document : documents) {
                            for (Map<String, Object> themnmap : mnlist) {
                                if ((themnmap.get("DGIMN")).equals(document.getString("DataGatherCode"))) {//判断是否是相同Mn
                                    listdata.add(document);
                                    break;
                                }
                            }
                        }
                        if (listdata != null && listdata.size() > 0) {
                            List<Map<String, Object>> listdatatwo = new ArrayList<>();
                            Set set = new HashSet();
                            for (Map<String, Object> objmap : listdata) {
                                if (set.contains(objmap.get("MonitorDate"))) {//判断是否类型重复
                                    continue;//重复
                                } else {//不重复
                                    int num = 0;
                                    for (Map<String, Object> objmaptwo : listdata) {
                                        if ((objmap.get("MonitorDate").toString()).equals(objmaptwo.get("MonitorDate").toString())) {
                                            num += Integer.parseInt(objmaptwo.get("num").toString());
                                        }
                                    }
                                    if (num > 0) {
                                        Map<String, Object> obj = new HashMap<>();
                                        obj.put("monitortime", objmap.get("MonitorDate"));
                                        obj.put("num", num);
                                        listdatatwo.add(obj);
                                    }
                                    set.add(objmap.get("MonitorDate").toString());
                                }
                            }
                            Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                            List<Map<String, Object>> collect = listdatatwo.stream().sorted(comparebytime).collect(Collectors.toList());
                            resultmap.put(key, collect);
                        }

                    }
                }
            }
            if (resultmap != null && resultmap.size() > 0) {
                result.put(remind + "", resultmap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/11/22 09:24
     * @Description: 根据时间范围统计超标报警的点位名称和企业名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:[starttime:开始时间 yyyy-MM-dd，endtime:结束时间  yyyy-MM-dd]
     * @return:
     */
    @RequestMapping(value = "getOverAlarmMonitorPointInfoByParam", method = RequestMethod.POST)
    public Object getOverAlarmMonitorPointInfoByParam(@RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime
    ) {

        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //获取废水、废气、雨水、厂界恶臭和环境恶臭所有点位MN号和基础信息
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            List<Map<String, Object>> mndata = onlineService.getPollutionOutputMnAndStinkMonitorPointMn(paramMap);
            List<Map<String, Object>> listdata = onlineCountAlarmService.getOverAlarmMonitorPointInfoByParam(starttime, endtime, mndata);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/22 16:49
     * @Description: 单个监测点或排口当天（某一天）各报警类型的报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "CountMonitorPointMultipleAlarmTypesDataByParam", method = RequestMethod.POST)
    public Object CountMonitorPointMultipleAlarmTypesDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                                 @RequestJson(value = "starttime") String starttime,
                                                                 @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                 @RequestJson(value = "endtime") String endtime) {
        try {
            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype);
            Map<String, Object> resultMap = new HashMap<>();
            if (monitorPointTypeEnum == null) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(monitorpointtype);
            queryVO.setMonitorPointType(monitorpointtype);
            Set<String> mns = new HashSet<>();
            mns.add(dgimn);
            queryVO.setMns(mns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            for (int remind : reminds) {
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                queryVO.setCollection(collection);          //放入连接表
                if (remind == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {//当为异常报警时 统计各异常类型的条数（零值异常，连续值异常，超限异常，无流量异常）
                    queryVO.setUnwindFieldName(null);
                    queryVO.setPollutantCodeFieldName("PollutantCode");
                    List<Document> documents = onlineCountAlarmService.countExceptionTypeAlarmData(queryVO, timeStyle);
                    if (documents.size() > 0) {
                        List<String> exceptiontypes = CommonTypeEnum.getExceptionMainTypeList();
                        Map<String, Object> map = new HashMap<>();
                        List<Map<String, Object>> exceptionlist = new ArrayList<>();
                        String thetime = "";
                        for (String str : exceptiontypes) {
                            Map<String, Object> exceptionmap = new HashMap<>();
                            exceptionmap.put("exceptiontype", str);
                            List<Map<String, Object>> pollutantlist = new ArrayList<>();
                            for (Document document : documents) {
                                if (!"".equals(thetime)) {
                                    Date d1 = DataFormatUtil.parseDate(thetime);
                                    Date d2 = DataFormatUtil.parseDate(document.getString("MaxTime"));
                                    int d = (int) (d2.getTime()) / 1000 - (int) (d1.getTime()) / 1000;
                                    if (d > 0) {
                                        thetime = document.getString("MaxTime");
                                    }
                                } else {
                                    thetime = document.getString("MaxTime");
                                }
                                if (str.equals(document.getString("ExceptionType"))) {//异常类型相等
                                    Map<String, Object> pollutantmap = new HashMap<>();
                                    pollutantmap.put("pollutantname", codemap.get(document.getString("PollutantCode")));
                                    pollutantmap.put("num", document.getInteger("num"));
                                    pollutantlist.add(pollutantmap);
                                }
                            }
                            exceptionmap.put("pollutants", pollutantlist);
                            if (pollutantlist != null && pollutantlist.size() > 0) {
                                exceptionlist.add(exceptionmap);
                            }
                        }
                        map.put("lasttime", thetime);
                        map.put("exceptiondata", exceptionlist);
                        resultMap.put("exception", map);
                    }
                } else {
                    List<Document> documents = getPollutantAlarmDataByParam3(queryVO, remindTypeEnum, timeStyle);
                    if (documents.size() > 0) {
                        Map<String, Object> map = new HashMap<>();
                        List<Map<String, Object>> pollutantlist = new ArrayList<>();
                        String thetime = "";
                        for (Document document : documents) {
                            if (!"".equals(thetime)) {
                                Date d1 = DataFormatUtil.parseDate(thetime);
                                Date d2 = DataFormatUtil.parseDate(document.getString("MaxTime"));
                                int d = (int) (d2.getTime()) / 1000 - (int) (d1.getTime()) / 1000;
                                if (d > 0) {
                                    thetime = document.getString("MaxTime");
                                }
                            } else {
                                thetime = document.getString("MaxTime");
                            }
                            Map<String, Object> pollutantmap = new HashMap<>();
                            pollutantmap.put("pollutantname", codemap.get(document.getString("_id")));
                            pollutantmap.put("num", document.getInteger("num"));
                            pollutantlist.add(pollutantmap);
                        }
                        map.put("lasttime", thetime);
                        map.put("pollutants", pollutantlist);
                        if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {
                            resultMap.put("concentrationchange", map);
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                            resultMap.put("flowchange", map);
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {
                            resultMap.put("early", map);
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {
                            resultMap.put("over", map);
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
     * @author: xsm
     * @date: 2019/12/02 0002 上午 11:14
     * @Description: 自定义查询条件统计点位某一天报警详情和任务状态数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointAlarmDetailDataAndAlarmTaskInfoByParam", method = RequestMethod.POST)
    public Object getPointAlarmDetailDataAndAlarmTaskInfoByParam(@RequestJson(value = "starttime") String starttime,
                                                                 @RequestJson(value = "endtime") String endtime,
                                                                 @RequestJson(value = "remindtype") Integer remindtype,
                                                                 @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
                                                                 @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                                 HttpServletRequest request) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("userid", userId);
            List<Map<String, Object>> inMnDatas = new ArrayList<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (StringUtils.isNotBlank(monitorpointcategory)) {
                paramMap.put("monitorPointCategorys", Arrays.asList(monitorpointcategory));
                inMnDatas = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
            } else {
                inMnDatas = onlineService.getDeviceStatusDataByParam(paramMap);
            }
            for (Integer monitorpointtype : monitorpointtypes) {
                String mnCommon;
                int type;
                Map<String, Integer> mnAndType = new HashMap<>();
                List<String> mns = new ArrayList<>();
                if (StringUtils.isNotBlank(monitorpointcategory)) {
                    if (inMnDatas.size() > 0) {
                        for (Map<String, Object> inMnData : inMnDatas) {
                            mnCommon = inMnData.get("dgimn").toString();
                            type = Integer.parseInt(inMnData.get("FK_MonitorPointTypeCode").toString());
                            if (monitorpointtype == type) {
                                mnAndType.put(mnCommon, type);
                                mns.add(mnCommon);
                            }
                        }
                    }
                } else {
                    for (Map<String, Object> mapIndex : inMnDatas) {
                        mnCommon = mapIndex.get("dgimn").toString();
                        type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                        if (monitorpointtype == type) {
                            mnAndType.put(mnCommon, type);
                            mns.add(mnCommon);
                        }
                    }
                }
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime + " 00:00:00");
                paramMap.put("endtime", endtime + " 23:59:59");
                Map<String, Integer> mnAndCount = new HashMap<>();
                Map<String, Date> mnAndMaxTime = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                Map<String, List<Map<String, Object>>> mnAndAlarmDataList = countMnPollutantMaxTime(remindtype, mnAndCount, mnAndMaxTime, paramMap);

                Set pollutionids = new HashSet();
                if (mnAndAlarmDataList.size() > 0) {
                    paramMap.put("outputids", Arrays.asList());
                    List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
                    Map<String, Object> mnAndPollutionName = new HashMap<>();
                    Map<String, Object> mnAndShortername = new HashMap<>();
                    Map<String, Object> mnAndMonitorPointName = new HashMap<>();
                    Map<String, Object> mnAndMonitorPointId = new HashMap<>();
                    Map<String, Object> mnAndPollutionId = new HashMap<>();
                    for (Map<String, Object> map : monitorPoints) {
                        mnCommon = map.get("dgimn").toString();
                        mns.add(mnCommon);
                        mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                        mnAndShortername.put(mnCommon, map.get("shortername"));
                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                        mnAndMonitorPointId.put(mnCommon, map.get("monitorpointid"));
                        mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                        if (map.get("pk_pollutionid") != null) {
                            pollutionids.add(map.get("pk_pollutionid").toString());
                        }
                    }
                    List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
                    if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {
                        if (pollutionids.size() > 0) {
                            Map<String, Object> param = new HashMap<>();
                            param.put("userid", userId);
                            param.put("datauserid", userId);
                            param.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPOverAlarmTaskDisposeEnum.getMenucode());
                            List<String> rightList = getRightList(userId);
                            param.put("starttime", starttime);
                            param.put("endtime", endtime);
                            param.put("pollutionids", pollutionids);
                            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())){
                                param.put("isnobutton", "is");
                                listdata = alarmTaskDisposeService.getAlarmTaskInfoByParamMap(param);
                            }else {
                                param.put("isnobutton", "no");
                                listdata = alarmTaskDisposeService.getAlarmTaskInfoByParamMap(param);
                            }
                        }
                    }
                    for (String mnIndex : mnAndAlarmDataList.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("totalnum", mnAndCount.get(mnIndex));
                        dataMap.put("mn", mnIndex);
                        dataMap.put("maxtime", DataFormatUtil.getDateYMDHMS(mnAndMaxTime.get(mnIndex)));
                        dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnIndex));
                        dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                        dataMap.put("pollutionname", mnAndPollutionName.get(mnIndex));
                        dataMap.put("shortername", mnAndShortername.get(mnIndex));
                        dataMap.put("pollutionid", mnAndPollutionId.get(mnIndex));
                        dataMap.put("pollutantdata", mnAndAlarmDataList.get(mnIndex));
                        if (mnAndPollutionId.get(mnIndex) != null) {
                            if (listdata != null && listdata.size() > 0) {
                                for (Map<String, Object> map : listdata) {
                                    if (map.get("FK_Pollutionid") != null && (mnAndPollutionId.get(mnIndex).toString().equals(map.get("FK_Pollutionid").toString()))) {
                                        dataMap.putAll(map);
                                        break;
                                    }
                                }
                            }
                        }
                        dataList.add(dataMap);
                    }
                }
            }
            if (dataList.size() > 0) {
                //根据totalnum排序
                dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("totalnum").toString())).reversed()).collect(Collectors.toList());
                if (pagenum != null && pagesize != null) {
                    List<Map<String, Object>> subList = dataList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                    resultMap.put("datalist", subList);
                } else {
                    dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("totalnum").toString())).reversed()).collect(Collectors.toList());
                    resultMap.put("datalist", dataList);
                }
            } else {
                resultMap.put("datalist", dataList);
            }
            resultMap.put("total", dataList.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletype", CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode());
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }

    /**
     * @author: xsm
     * @date: 2019/8/22 16:49
     * @Description: 单个监测点或排口当天（某一天）各报警类型的报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getExceptionAlarmDetailDataByParam", method = RequestMethod.POST)
    public Object getExceptionAlarmDetailDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "exceptiontype") String exceptiontype,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            Date startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimn", dgimn);
            paramMap.put("exceptiontype", exceptiontype);
            paramMap.put("codemap", codemap);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> result = onlineCountAlarmService.getExceptionAlarmDetailDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/11 8:43
     * @Description: 单个监测点或排口当天（某一天）预警或超限的报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEarlyOrOverAlarmDetailDataByParam", method = RequestMethod.POST)
    public Object getEarlyOrOverAlarmDetailDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                       @RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "remindtype") Integer remindtype,
                                                       @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                       @RequestJson(value = "endtime") String endtime,
                                                       @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                       @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            Date startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimn", dgimn);
            paramMap.put("remindtype", remindtype);
            paramMap.put("codemap", codemap);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> result = onlineCountAlarmService.getEarlyOrOverAlarmDetailDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/11 8:43
     * @Description: 单个监测点或排口当天（某一天）浓度突变或排放量突变的报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getConcentrationOrFlowChangeDetailDataByParam", method = RequestMethod.POST)
    public Object getConcentrationOrFlowChangeDetailDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                                @RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "remindtype") Integer remindtype,
                                                                @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                @RequestJson(value = "endtime") String endtime,
                                                                @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                                @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            Date startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimn", dgimn);
            paramMap.put("remindtype", remindtype);
            paramMap.put("codemap", codemap);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> result = onlineCountAlarmService.getConcentrationOrFlowChangeDetailDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 3:54
     * @Description: 统计当天超限报警数据，用于app菜单展示
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countOverAlarmDataForAppMenus", method = RequestMethod.POST)
    public Object countOverAlarmDataForAppMenus(
            @RequestJson(value = "menuid") String menuid,
            @RequestJson(value = "nowday", required = false) String nowday,
            @RequestJson(value = "userauth", required = false) List<JSONObject> objectList, HttpSession session) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String nowTime = DataFormatUtil.getDateYMD(new Date());
            if (StringUtils.isNotBlank(nowday)) {
                nowTime = nowday;
            }
            boolean isHaveData = false;
            String userId = "";
            if (objectList == null) {
                isHaveData = true;
                String sessionId = session.getId();
                objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
                userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            }
            //根据菜单权限获取，监测点类型数组
            if (objectList != null) {
                JSONArray childrenList;
                JSONObject jsonObject;
                Map<String, Object> codeAndName = new LinkedHashMap<>();
                for (JSONObject jsonIndex : objectList) {
                    if (jsonIndex.get("menuid").equals("app10")) {
                        childrenList = jsonIndex.getJSONArray("datalistchildren");
                        if (childrenList != null) {
                            for (int i = 0; i < childrenList.size(); i++) {
                                jsonObject = childrenList.getJSONObject(i);
                                if (menuid.equals(jsonObject.get("menuid"))) {
                                    childrenList = jsonObject.getJSONArray("datalistchildren");
                                    for (int j = 0; j < childrenList.size(); j++) {
                                        jsonObject = childrenList.getJSONObject(j);
                                        codeAndName.put(jsonObject.getString("menucode"), jsonObject.getString("menuname"));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (codeAndName.size() > 0) {
                    Set<String> appSysmodel = new HashSet<>(codeAndName.keySet());
                    List<Integer> monitorpointtypes = new ArrayList<>();
                    List<Integer> types;
                    Map<Integer, String> typeAndSysmodel = new HashMap<>();
                    for (String sysmodel : appSysmodel) {
                        types = CommonTypeEnum.AppSysModelAndType.getTypesBySysmodel(sysmodel);
                        if (types != null) {
                            monitorpointtypes.addAll(types);
                            for (Integer typeIndex : types) {
                                typeAndSysmodel.put(typeIndex, sysmodel);
                            }
                        }
                    }
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("starttime", nowTime);
                    paramMap.put("endtime", nowTime);
                    paramMap.put("userid", userId);
                    paramMap.put("monitorpointtypes", monitorpointtypes);
                    List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
                    String mnCommon;
                    int type;
                    Map<String, Integer> mnAndType = new HashMap<>();
                    List<String> mns = new ArrayList<>();
                    for (Map<String, Object> mapIndex : deviceStatus) {
                        mnCommon = mapIndex.get("dgimn").toString();
                        type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                        mnAndType.put(mnCommon, type);
                        mns.add(mnCommon);
                    }
                    paramMap.put("mns", mns);
                    String startTime = paramMap.get("starttime") + " 00:00:00";
                    String endTime = paramMap.get("endtime") + " 23:59:59";
                    paramMap.put("starttime", startTime);
                    paramMap.put("endtime", endTime);
                    List<Map<String, Map<String, Object>>> mnAndAlarmDataList = new ArrayList<>();
                    Map<String, Map<String, Object>> mnAndOverData = new HashMap<>();
                    paramMap.put("collection", "OverData");
                    paramMap.put("timeKey", "OverTime");
                    List<Document> overData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
                    setMnAndAlarmData(mnAndOverData, overData, CommonTypeEnum.RemindTypeEnum.OverAlarmEnum);
                    mnAndAlarmDataList.add(mnAndOverData);
                    Map<Integer, Map<String, List<String>>> typeAndRemindAndMns = setTypeAndRemindAndMns(mnAndAlarmDataList, mnAndType);
                    Map<String, Map<String, Integer>> sysmodelAndTypeAndNum = new HashMap<>();
                    if (typeAndRemindAndMns.size() > 0) {
                        Map<String, Map<String, List<String>>> sysmodelAndTypeAndMns = new HashMap<>();
                        String sysmodel;
                        Map<String, List<String>> typeAndMns;
                        for (Integer typeIndex : typeAndRemindAndMns.keySet()) {
                            sysmodel = typeAndSysmodel.get(typeIndex);
                            if (sysmodelAndTypeAndMns.containsKey(sysmodel)) {
                                typeAndMns = sysmodelAndTypeAndMns.get(sysmodel);
                            } else {
                                typeAndMns = new HashMap<>();
                                typeAndMns.put("alarmdata", Arrays.asList());
                                typeAndMns.put("earlydata", Arrays.asList());
                            }
                            setAlarmData(typeAndMns, typeAndRemindAndMns.get(typeIndex));
                            sysmodelAndTypeAndMns.put(sysmodel, typeAndMns);
                        }

                        for (String sysmodelIndex : sysmodelAndTypeAndMns.keySet()) {
                            typeAndMns = sysmodelAndTypeAndMns.get(sysmodelIndex);
                            Map<String, Integer> typeAndNum = new HashMap<>();
                            for (String typeIndex : typeAndMns.keySet()) {
                                mns = typeAndMns.get(typeIndex).stream().distinct().collect(Collectors.toList());
                                typeAndNum.put(typeIndex, mns.size());
                            }
                            sysmodelAndTypeAndNum.put(sysmodelIndex, typeAndNum);
                        }
                    }
                    if (appSysmodel.contains("yqqy")) {
                        String sysmodel = "yqqy";
                        Map<String, String> allMnAndPollutionId = new HashMap<>();
                        List<String> outputIds = new ArrayList<>();
                        //报警企业提醒
                        for (Integer typeIndex : monitorpointtypes) {
                            allMnAndPollutionId.putAll(onlineService.getMNAndPollutionId(outputIds, typeIndex));
                        }
                        //获取报警企业数，预警企业数
                        Map<String, Integer> pollutionAndRemindAndNum = setPollutionAndRemindAndNum(mnAndAlarmDataList, allMnAndPollutionId);
                        sysmodelAndTypeAndNum.put(sysmodel, pollutionAndRemindAndNum);
                    }
                    for (String sysmodeIndex : codeAndName.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("sysmodel", sysmodeIndex);
                        resultMap.put("name", codeAndName.get(sysmodeIndex));
                        if (sysmodelAndTypeAndNum.containsKey(sysmodeIndex)) {
                            resultMap.putAll(sysmodelAndTypeAndNum.get(sysmodeIndex));
                        } else {
                            resultMap.put("alarmdata", 0);
                            resultMap.put("earlydata", 0);
                        }
                        resultList.add(resultMap);
                    }
                }
            }
            if (isHaveData) {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            } else {
                return resultList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 5:00
     * @Description: 统计当天各监测类型异常报警点位数量，用于app菜单展示
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "countExceptionAlarmNumGroupByMonitorPointType", method = RequestMethod.POST)
    public Object countExceptionAlarmNumGroupByMonitorPointType(@RequestJson(value = "nowday", required = false) String nowday) {
        try {
            String nowTime = DataFormatUtil.getDateYMD(new Date());
            if (StringUtils.isNotBlank(nowday)) {
                nowTime = nowday;
            }
            String startTime = nowTime + " 00:00:00";
            String endTime = nowTime + " 23:59:59";
            List<Integer> monitortypes = new ArrayList<>();
            monitortypes.add(WasteWaterEnum.getCode());
            monitortypes.add(WasteGasEnum.getCode());
            monitortypes.add(RainEnum.getCode());
            monitortypes.add(FactoryBoundarySmallStationEnum.getCode());
            monitortypes.add(FactoryBoundaryStinkEnum.getCode());
            monitortypes.add(AirEnum.getCode());
            monitortypes.add(EnvironmentalStinkEnum.getCode());
            monitortypes.add(EnvironmentalVocEnum.getCode());
            monitortypes.add(WaterQualityEnum.getCode());
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitortypes);
            List<Map<String, Object>> deviceStatus = onlineService.getDeviceStatusDataByParam(paramMap);
            String mnCommon;
            int type;
            Map<String, Integer> mnAndType = new HashMap<>();
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> mapIndex : deviceStatus) {
                mnCommon = mapIndex.get("dgimn").toString();
                type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
                mnAndType.put(mnCommon, type);
                mns.add(mnCommon);
            }
            paramMap.put("mns", mns);
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("collection", "ExceptionData");
            paramMap.put("timeKey", "ExceptionTime");
            List<Document> exceptionData = onlineCountAlarmService.countAlarmDataForMnByParam(paramMap);
            Map<String, Map<String, Object>> mnAndExceptionData = new HashMap<>();
            setMnAndAlarmData(mnAndExceptionData, exceptionData, CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum);
            Map<String, Integer> typeAndCount = new HashMap<>();
            if (mnAndExceptionData != null && mnAndExceptionData.size() > 0) {
                for (Integer typecode : monitortypes) {
                    int num = 0;
                    for (String mnIndex : mnAndExceptionData.keySet()) {
                        if (typecode == mnAndType.get(mnIndex)) {
                            num += 1;
                        }
                    }
                    typeAndCount.put(typecode.toString(), num);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", typeAndCount);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/20 9:39
     * @Description: 获取某监测类型异常详情数据信息（包含零值异常、连续值异常、超限异常、无流量异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getExceptionAlarmChildDetailDataByParam", method = RequestMethod.POST)
    public Object getExceptionAlarmChildDetailDataByParam(@RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                          @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitortype", monitorpointtype);
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(),
                    WasteGasEnum.getCode(), RainEnum.getCode(), unOrganizationWasteGasEnum.getCode(), FactoryBoundarySmallStationEnum.getCode(), FactoryBoundaryStinkEnum.getCode()
            );
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            Set<String> dgimns = new HashSet<>();
            //获取所点位名称和MN号
            if (monitortypes.contains(monitorpointtype)) {
                monitorPoints = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
            } else if (monitorpointtype == AirEnum.getCode()) {//大气
                monitorPoints = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                monitorPoints = waterStationService.getWaterStationByParamMap(paramMap);
            } else if (monitorpointtype == EnvironmentalVocEnum.getCode() || monitorpointtype == EnvironmentalStinkEnum.getCode()) {//voc//恶臭
                monitorPoints = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            }
            String mnCommon;
            Map<String, Object> mnAndPollutionName = new HashMap<>();//企业名称
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();//点位名称
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            Map<String, Object> mnAndShorterName = new HashMap<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                    mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                    mnAndMonitorPointId.put(mnCommon, map.get("pk_id"));
                    mnAndShorterName.put(mnCommon, map.get("shortername"));

                }
            }
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            queryVO.setMonitorPointType(monitorpointtype);
            queryVO.setMns(dgimns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            queryVO.setTimeFieldName("ExceptionTime");    //放入时间字段
            queryVO.setCollection("ExceptionData");          //放入连接表
            queryVO.setUnwindFieldName(null);
            queryVO.setPollutantCodeFieldName("PollutantCode");
            List<Document> documents = onlineCountAlarmService.getExceptionAlarmChildDetailDataByParam(queryVO, timeStyle);
            if (documents.size() > 0) {
                List<String> exceptiontypes = CommonTypeEnum.getExceptionMainTypeList();
                for (String mn : dgimns) {
                    Map<String, Object> map = new HashMap<>();
                    List<Map<String, Object>> exceptionlist = new ArrayList<>();
                    for (String type : exceptiontypes) {
                        Map<String, Object> exceptionmap = new HashMap<>();
                        exceptionmap.put("exceptiontype", type);
                        List<Map<String, Object>> pollutantlist = new ArrayList<>();
                        for (Document document : documents) {
                            if (mn.equals(document.getString("DataGatherCode"))) {//MN号相等
                                if (type.equals(document.getString("ExceptionType"))) {//异常类型相等
                                    Map<String, Object> pollutantmap = new HashMap<>();
                                    pollutantmap.put("pollutantname", codemap.get(document.getString("PollutantCode")));
                                    pollutantmap.put("num", document.getInteger("num"));
                                    pollutantlist.add(pollutantmap);
                                }
                            }
                        }
                        exceptionmap.put("pollutants", pollutantlist);
                        if (pollutantlist != null && pollutantlist.size() > 0) {
                            exceptionlist.add(exceptionmap);
                        }
                    }
                    if (exceptionlist != null && exceptionlist.size() > 0) {
                        map.put("dgimn", mn);
                        map.put("pollutionid", mnAndPollutionId.get(mn));
                        map.put("pollutionname", mnAndPollutionName.get(mn));
                        map.put("shortername", mnAndShorterName.get(mn));
                        map.put("monitorpointid", mnAndMonitorPointId.get(mn));
                        map.put("monitorpointname", mnAndMonitorPointName.get(mn));
                        map.put("exceptiondata", exceptionlist);
                        resultlist.add(map);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "CountAlarmTypesDataByParams", method = RequestMethod.POST)
    public Object CountAlarmTypesDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                              @RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                              @RequestJson(value = "reminds") List<Integer> reminds,
                                              @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                              @RequestJson(value = "timetype", required = false) String timetype,
                                              @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();

            if (StringUtils.isBlank(timetype)) {
                timetype = "day";
            }
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timetype, starttime, endtime);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setMonitorPointType(monitorpointtype);
            Set<String> mns = new HashSet<>();
            mns.add(dgimn);
            queryVO.setMns(mns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            int total = 0;
            for (int remind : reminds) {
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                queryVO.setCollection(collection);          //放入连接表

                List<Document> documents = getPollutantAlarmDataByParam3(queryVO, remindTypeEnum, timeStyle);
                if (documents.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    List<Map<String, Object>> pollutantlist = new ArrayList<>();
                    String thetime = "";
                    int totalnum = 0;
                    for (Document document : documents) {
                        if (!"".equals(thetime)) {
                            Date d1 = DataFormatUtil.parseDate(thetime);
                            Date d2 = DataFormatUtil.parseDate(document.getString("MaxTime"));
                            int d = (int) (d2.getTime()) / 1000 - (int) (d1.getTime()) / 1000;
                            if (d > 0) {
                                thetime = document.getString("MaxTime");
                            }
                        } else {
                            thetime = document.getString("MaxTime");
                        }
                        Map<String, Object> pollutantmap = new HashMap<>();
                        totalnum += document.getInteger("num");
                        if (pollutantcodes.size() > 0 && pollutantcodes.contains(document.getString("_id"))) {
                            pollutantmap.put("pollutantname", codemap.get(document.getString("_id")));
                            pollutantmap.put("num", document.getInteger("num"));
                            pollutantlist.add(pollutantmap);
                        } else if (pollutantcodes.size() == 0) {
                            pollutantmap.put("pollutantname", codemap.get(document.getString("_id")));
                            pollutantmap.put("num", document.getInteger("num"));
                            pollutantlist.add(pollutantmap);
                        }
                    }
                    map.put("lasttime", thetime);
                    map.put("pollutants", pollutantlist);
                    map.put("totalnum", totalnum);
                    total += totalnum;
                    if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {
                        resultMap.put("concentrationchange", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                        resultMap.put("flowchange", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {
                        resultMap.put("early", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {
                        resultMap.put("over", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                        resultMap.put("exception", map);
                    }
                }
                resultMap.put("total", total);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 自定义查询条件获取报警数量
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/15 11:35
     */
    @RequestMapping(value = "CountOverNumByParams", method = RequestMethod.POST)
    public Object CountOverNumByParams(@RequestJson(value = "dgimn") String dgimn,
                                       @RequestJson(value = "starttime") String starttime,
                                       @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setStartTime(DataFormatUtil.parseDate(starttime + ":00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(endtime + ":59:59"));
            Set<String> mns = new HashSet<>();
            mns.add(dgimn);
            queryVO.setMns(mns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.OverAlarmEnum;
            String timeFieldName = getTimeFieldName(remindTypeEnum);
            String collection = getCollection(remindTypeEnum);
            queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
            queryVO.setCollection(collection);          //放入连接表
            List<Document> documents = getPollutantAlarmDataByParam3(queryVO, remindTypeEnum, timeStyle);
            if (documents.size() > 0) {
                int totalnum = 0;
                for (Document document : documents) {
                    totalnum += document.getInteger("num");
                }
                resultMap.put("overnum", totalnum);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/2/27 16:36
     * @Description: 获取点位报警、设备运维提醒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:timetype:sh hour/day
     * @return:
     */
    @RequestMapping(value = "getMonitorPointAlarmAndDevOpsRemindData", method = RequestMethod.POST)
    public Object getMonitorPointAlarmAndDevOpsRemindData(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype", required = false) String timetype,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "ismodelalarm", required = false) Boolean ismodelalarm
    ) {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimn", dgimn);
            String start = "";
            String end = "";
            if (StringUtils.isNotBlank(timetype) && "hour".equals(timetype)) {
                start = starttime + ":00:00";
                end = endtime + ":59:59";
            } else {
                start = starttime + " 00:00:00";
                end = endtime + " 23:59:59";
            }

            paramMap.put("starttime", start);
            paramMap.put("endtime", end);
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), RainEnum.getCode()
            );
            int alarmnum = 0;
            int stopnum = 0;
            List<Integer> reminds = Arrays.asList(1, 2, 3, 4, 5);
            //报警统计
            Map<String, Object> map = new HashMap<>();
            if (ismodelalarm != null && ismodelalarm == true) {
                //有该标记 则查overmodel 表
                map = (Map<String, Object>) CountOnePointAlarmNumDataByParams(dgimn, start, new ArrayList<>(), reminds, monitorpointtype, timetype, end);
            } else {
                map = (Map<String, Object>) CountAlarmTypesDataByParams(dgimn, starttime, new ArrayList<>(), reminds, monitorpointtype, timetype, endtime);
            }
            if (map != null) {
                Map<String, Object> datamap = (Map<String, Object>) map.get("data");
                List<Map<String, Object>> pollutants = new ArrayList<>();
                Map<String, Object> alarmmap = null;
                if (datamap.get("concentrationchange") != null) {
                    alarmmap = (Map<String, Object>) datamap.get("concentrationchange");
                    pollutants = (List<Map<String, Object>>) alarmmap.get("pollutants");
                    for (Map<String, Object> pollutanmap : pollutants) {
                        alarmnum += Integer.parseInt(pollutanmap.get("num").toString());
                    }
                }
                if (datamap.get("flowchange") != null) {
                    alarmmap = (Map<String, Object>) datamap.get("flowchange");
                    pollutants = (List<Map<String, Object>>) alarmmap.get("pollutants");
                    for (Map<String, Object> pollutanmap : pollutants) {
                        alarmnum += Integer.parseInt(pollutanmap.get("num").toString());
                    }
                }
                if (datamap.get("early") != null) {
                    alarmmap = (Map<String, Object>) datamap.get("early");
                    pollutants = (List<Map<String, Object>>) alarmmap.get("pollutants");
                    for (Map<String, Object> pollutanmap : pollutants) {
                        alarmnum += Integer.parseInt(pollutanmap.get("num").toString());
                    }
                }
                if (datamap.get("over") != null) {
                    alarmmap = (Map<String, Object>) datamap.get("over");
                    pollutants = (List<Map<String, Object>>) alarmmap.get("pollutants");
                    for (Map<String, Object> pollutanmap : pollutants) {
                        alarmnum += Integer.parseInt(pollutanmap.get("num").toString());
                    }
                }
                if (datamap.get("exception") != null) {
                    alarmmap = (Map<String, Object>) datamap.get("exception");
                    pollutants = (List<Map<String, Object>>) alarmmap.get("pollutants");
                    for (Map<String, Object> pollutanmap : pollutants) {
                        alarmnum += Integer.parseInt(pollutanmap.get("num").toString());
                    }
                }
            }
            //运维设备统计
            paramMap.put("monitorpointid", monitorpointid);
            Long countall = deviceDevOpsInfoService.CountDeviceDevOpsHistoryListDataNumByParams(paramMap);
            paramMap.remove("monitorpointid");
            //停产记录统计
            if (monitortypes.contains(monitorpointtype)) {
                List<Map<String, Object>> stoplist = new ArrayList<>();
                if (monitorpointtype == RainEnum.getCode()) {
                    paramMap.clear();
                    paramMap.put("monitorpointtype", monitorpointtype);
                    paramMap.put("dgimn", dgimn);
                    paramMap.put("startmonitortime", start);
                    paramMap.put("endmonitortime", end);
                    stoplist = monitorControlService.getMonitorControlLogDataByParamMap(paramMap);
                } else {
                    stoplist = stopProductionInfoService.getStopProductionInfosByParamMap(paramMap);
                }
                if (stoplist != null && stoplist.size() > 0) {
                    stopnum = stoplist.size();
                }
            }
            //任务条数统计

            paramMap.put("starttime", start);
            paramMap.put("endtime", end);
            Map<String, Object> taskmap = alarmTaskDisposeService.getAlarmAndDevOpsTaskNumByParam(paramMap);
            result.put("alarmnum", alarmnum);
            result.put("devopsnum", countall);
            result.put("tasknum", taskmap);
            result.put("stopnum", stopnum);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @author: xsm
     * @date: 2020/03/13 9:36
     * @Description: 获取废水无流量异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void countNoFlowExceptionDataNumByParam(OnlineAlarmCountQueryVO queryVO, String timeType, Map<String, Map<String, Object>> mapMap) {
        List<Map<String, Object>> mndata = getMnsByMenuMonitorType(queryVO);
        Set<String> mns = mndata.stream().map(document -> document.get("DGIMN").toString()).collect(Collectors.toSet());
        queryVO.setMns(mns);
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(timeType);
        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(WaterNoFlowEnum.getCode());
        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
        String timeFieldName = "ExceptionTime";
        String collection = "ExceptionData";
        queryVO.setTimeFieldName(timeFieldName);
        queryVO.setCollection(collection);
        queryVO.setUnwindFieldName(null);
        List<Document> documents = onlineCountAlarmService.getNoFlowExceptionDataNumByParam(queryVO, timeStyle);
        if (timeType.equals("week")) {
            for (Document document1 : documents) {
                document1.put("week", DataFormatUtil.getTimeOfYMWByString(document1.getString("_id")).toString());
            }
            Map<String, List<Document>> listMap = new HashMap<>();
            for (Document m : documents) {
                listMap.computeIfAbsent(m.getString("week"), k -> new ArrayList<>()).add(m);
            }
            List<Document> documents1 = new ArrayList<>();
            for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                String time = entry.getKey();
                List<Document> value = entry.getValue();
                int num = 0;
                for (Document document1 : value) {
                    Integer count1 = document1.getInteger("num");
                    num = num + count1;
                }
                Document document = new Document();
                document.put("_id", time);
                document.put("num", num);
                documents1.add(document);
            }
            documents = documents1;
        }
        getEveryOneRemindTypeAlaramNum(mapMap, remindTypeEnum, documents);
    }

    /**
     * @author: xsm
     * @date: 2020/5/13 14:19
     * @Description: 根据用户数据权限获取当天各报警类型的报警数量（超阈值预警，突变预警，超标报警，零值异常，恒值异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllAlarmTypeDataNumByDayTime", method = RequestMethod.POST)
    public Object countAllAlarmTypeDataNumByDayTime(@RequestJson(value = "daytime") String daytime) {
        try {
            Set<String> mns = new HashSet(RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class));
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("day", daytime, daytime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setMns(mns);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            int[] reminds = {1, 2, 3, 5};
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> overmap = new HashMap<>();
            Map<String, Object> earlymap = new HashMap<>();
            Map<String, Object> changemap = new HashMap<>();
            for (int remind : reminds) {
                queryVO.setTimeFieldName(AlarmRemindUtil.getTimeFieldNameByRemindType(remind)); //放入时间字段
                queryVO.setCollection(AlarmRemindUtil.getCollectionByRemindType(remind));   //放入连接表
                queryVO.setUnwindFieldName(AlarmRemindUtil.getUnWindowNameByRemindType(remind));
                queryVO.setExceptionType(null);
                List<String> alarmMns = onlineCountAlarmService.getAlarmMnsByParams(queryVO);   //该提醒类型报警mn
                int num = 0;
                for (String dgimn : mns) {
                    if (alarmMns.contains(dgimn)) {
                        num++;
                    }
                }
                if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {  //阈值
                    earlymap.put("alarmtype", "early");
                    earlymap.put("num", num);
                } else if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode() ||
                        remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {    //突变
                    changemap.put("alarmtype", "change");
                    changemap.put("num", changemap.get("num") != null ? Integer.parseInt(changemap.get("num").toString()) + num : num);
                } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                    overmap.put("alarmtype", "over");
                    overmap.put("num", num);
                }

            }
            result.add(overmap);
            result.add(earlymap);
            result.add(changemap);
            List<Document> list = onlineCountAlarmService.countAllAlarmTypeDataNumByDayTime(daytime, mns);
            String[] exceptiontypes = {"1", "2"};
            for (String type : exceptiontypes) {
                Map<String, Object> map = new HashMap<>();
                map.put("exceptiontype", type);
                if (type.equals(ZeroExceptionEnum.getCode())) {
                    map.put("alarmtype", "zeroexception");
                } else if (type.equals(ContinuousExceptionEnum.getCode())) {
                    map.put("alarmtype", "continuousexception");
                }
                int num = 0;
                if (list.size() > 0) {
                    for (Document document : list) {
                        if (type.equals(document.getString("ExceptionType"))) {
                            num += 1;
                        }
                    }
                }
                map.put("num", num);
                result.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/5/27 14:32
     * @Description: 根据用户数据权限获取当天各报警类型的报警数量（超阈值预警，突变预警，超标报警，41零值异常，42.恒值异常）
     * @updateUser:xsm
     * @updateDate:2022/07/15 09:23
     * @updateDescription:报警点位数量 根据点位的实时状态来统计 不根据报警表的报警数据统计
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllMonitorTypeAlarmPointNumByDayTime", method = RequestMethod.POST)
    public Object countAllMonitorTypeAlarmPointNumByDayTime() {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultmMap = new HashMap<>();
            paramMap.put("userid", userId);
            Set<String> mns = new HashSet();
            //根据数据权限 获取监测的监测类型和MN信息
            List<Map<String, Object>> monitorpointtypes = alarmTaskDisposeService.getUserMonitorPointRelationDataByUserId(paramMap);
            Map<String, Object> mnandtypecode = new HashMap<>();
            Map<String, Object> typename = new HashMap<>();
            Map<String, Object> typeorder = new HashMap<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Map<String, Object> map : monitorpointtypes) {
                    if (map.get("DGIMN") != null) {
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

            Map<String, Integer> overmap = new HashMap<>();
            //查询状态表 查询实时状态为超标的点位MN
            List<String> alarmMns = deviceStatusService.getRealTimeAlarmStatusDgimns();
            for (String dgimn : mns) {
                if (alarmMns.contains(dgimn)) {
                    String typecode = mnandtypecode.get(dgimn).toString();
                    if (overmap.get(typecode) != null) {
                        overmap.put(typecode, overmap.get(typecode) + 1);
                    } else {
                        overmap.put(typecode, 1);
                    }
                }
            }
            List<Map<String, Object>> overlist = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : overmap.entrySet()) {
                Map<String, Object> overmap2 = new HashMap<>();
                overmap2.put("monitortypecode", entry.getKey());
                overmap2.put("num", entry.getValue());
                overmap2.put("monitortypename", typename.get(entry.getKey()));
                overmap2.put("orderindex", typeorder.get(entry.getKey()));
                overlist.add(overmap2);
            }
            if (overlist.size() > 0) {
                resultmMap.put("over", orderMonitorTypeData(overlist));
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/5/13 14:19
     * @Description: 根据MN号获取该点位当天各报警类型的报警数量（超阈值预警，突变预警，超标报警，零值异常，恒值异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllMonitorTypeAlarmNumByDayTimeAndMn", method = RequestMethod.POST)
    public Object countAllMonitorTypeAlarmNumByDayTimeAndMn(@RequestJson(value = "daytime") String daytime,
                                                            @RequestJson(value = "mn") String mn
    ) throws ParseException {
        try {
            Map<String, Map<String, Object>> resultmap = new HashMap<>();
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByDgimn(mn);
            Map<String, Object> codeandname = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("code") != null) {
                        codeandname.put(map.get("code").toString(), map.get("name"));
                    }
                }
            }
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("day", daytime, daytime);
            //超阈值预警
            Map<String, Object> earlymap = onlineCountAlarmService.countPointChangeAndOverAlarmNumByTimeType(CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode(), mn, dates[0], dates[1], codeandname);
            //突变预警
            Map<String, Object> changemap = onlineCountAlarmService.countPointChangeAlarmNumByTimeType(mn, dates[0], dates[1], codeandname);
            //超标报警
            Map<String, Object> overmap = onlineCountAlarmService.countPointChangeAndOverAlarmNumByTimeType(CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode(), mn, dates[0], dates[1], codeandname);
            //零值异常
            Map<String, Object> lz_exceptiondata = onlineCountAlarmService.countPointExceptionAlarmNumByDayTimeAnd(dates[0], dates[1], mn, ZeroExceptionEnum.getCode(), codeandname);
            //恒值异常
            Map<String, Object> hz_exceptiondata = onlineCountAlarmService.countPointExceptionAlarmNumByDayTimeAnd(dates[0], dates[1], mn, ContinuousExceptionEnum.getCode(), codeandname);
            resultmap.put("early", earlymap);
            resultmap.put("change", changemap);
            resultmap.put("over", overmap);
            resultmap.put("zeroexception", lz_exceptiondata);
            resultmap.put("continuousexception", hz_exceptiondata);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/6/17 0017 上午 10:28
     * @Description: 获取企业每天报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, starttime, endtime, monitorpointids]
     * @throws:
     */
    @RequestMapping(value = "countPollutionAlarmByParams", method = RequestMethod.POST)
    public Object countPollutionAlarmByParams(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                              @RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("fk_pollutionid", fk_pollutionid);
            paramMap.put("categorys", categorys);
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
            //在线排口mn号
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null)
                    .map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            List<Integer> remindTypes = new ArrayList<>();
            remindTypes.add(ExceptionAlarmEnum.getCode());
            remindTypes.add(OverAlarmEnum.getCode());
            remindTypes.add(FlowChangeEnum.getCode());
            remindTypes.add(ConcentrationChangeEnum.getCode());
            remindTypes.add(EarlyAlarmEnum.getCode());
            //默认查询今天
            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", "day");
            paramMap.put("remindTypes", remindTypes);
            //统计点位报警和预警次数
            Map<String, Object> onlineData = onlineCountAlarmService.countAlarmsInfoDataByParamMap(paramMap);
            Set<String> times = onlineData.values().stream().flatMap(m -> ((List<Map<String, Object>>) m).stream()).filter(m -> m.get("MonitorTime") != null).map(m -> m.get("MonitorTime").toString()).collect(Collectors.toSet());

            //预警
            List<Map<String, Object>> onlyearlywarn = onlineData.get("onlyearlywarn") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("onlyearlywarn");
            //浓度突变
            List<Map<String, Object>> concentrationchange = onlineData.get("concentrationchange") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("concentrationchange");
            //排放量突变
            List<Map<String, Object>> flowchange = onlineData.get("flowchange") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("flowchange");
            //不同类型异常
            List<Map<String, Object>> everyexception = onlineData.get("everyexception") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("everyexception");
            //超标
            List<Map<String, Object>> overdata = onlineData.get("overdata") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("overdata");


            Map<String, List<Map<String, Object>>> collect = everyexception.stream().filter(m -> m.get("ExceptionType") != null).collect(Collectors.groupingBy(m -> m.get("ExceptionType").toString()));
            Map<String, Integer> earlywarnmap = onlyearlywarn.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));
            Map<String, Integer> concentrationchangemap = concentrationchange.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));
            Map<String, Integer> flowchangemap = flowchange.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));
            Map<String, Integer> overdatamap = overdata.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));

            /*paramMap.clear();
            paramMap.put("fk_pollutionid", fk_pollutionid);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            List<Map<String, Object>> monitoringAlarmRecordInfoByParamMap = monitoringAlarmRecordService.getMonitoringAlarmRecordInfoByParamMap(paramMap);

            Map<String, Long> videoalarm = monitoringAlarmRecordInfoByParamMap.stream().filter(m -> m.get("AlarmTime") != null).map(m -> FormatUtils.formatDate(m.get("AlarmTime").toString(), "yyyy-MM-dd")).collect(Collectors.groupingBy(m -> m, Collectors.counting()));

            Map<String, Integer> videoalarmmap = new HashMap<>();
            for (String s : videoalarm.keySet()) {
                videoalarmmap.put(s, videoalarm.get(s).intValue());
                times.add(s);
            }*/

            for (String time : times) {
                Map<String, Object> data = new HashMap<>();
                data.put("monitortime", time);
                resultList.add(data);
            }

            for (String ExceptionType : collect.keySet()) {
                if (ExceptionType.equals(ZeroExceptionEnum.getCode())) {
                    List<Map<String, Object>> maps = collect.get(ExceptionType);
                    Map<String, Integer> exceptiondatamap = maps.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));
                    AssData(ZeroExceptionAlarmEnum.getCode().toString(), exceptiondatamap, resultList);//设置零值异常
                } else if (ExceptionType.equals(ContinuousExceptionEnum.getCode())) {
                    List<Map<String, Object>> maps = collect.get(ExceptionType);
                    Map<String, Integer> exceptiondatamap = maps.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));
                    AssData(ContinuousExceptionAlarmEnum.getCode().toString(), exceptiondatamap, resultList);//设置恒值异常
                }
            }
//            AssData(VideoOverEnum.getCode().toString(), videoalarmmap, resultList);//设置预警
            AssData(EarlyAlarmEnum.getCode().toString(), earlywarnmap, resultList);//设置预警
            AssData(ConcentrationChangeEnum.getCode().toString(), concentrationchangemap, resultList);//设置浓度突变
            AssData(FlowChangeEnum.getCode().toString(), flowchangemap, resultList);//设置排放量突变
            AssData(OverAlarmEnum.getCode().toString(), overdatamap, resultList);//设置超标
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/17 0017 下午 4:10
     * @Description: 当日任务处置情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countAlarmTaskInfoByParams", method = RequestMethod.POST)
    public Object countAlarmTaskInfoByParams(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "endtime") String endtime) throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(), unOrganizationWasteGasEnum.getCode()));
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            //在线排口mn号
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("Status") != null)
                    .map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            paramMap.put("tasktype", AlarmTaskEnum.getCode());//报警任务
            paramMap.put("starttime", DataFormatUtil.getDateYMD(new Date()));
            paramMap.put("endtime", DataFormatUtil.getDateYMD(new Date()));
            List<Map<String, Object>> alarmdatalist = alarmTaskDisposeService.countPollutionAlarmTaskGroupByStatusByParamMap(paramMap);
            int alarmsize = alarmdatalist.stream().filter(m -> m.get("num") != null).
                    map(m -> Integer.valueOf(m.get("num").toString())).collect(Collectors.summingInt(m -> m));

            paramMap.put("tasktype", DevOpsTaskEnum.getCode());//运维任务
            List<Map<String, Object>> devdatalist = alarmTaskDisposeService.countPollutionAlarmTaskGroupByStatusByParamMap(paramMap);
            int devsize = devdatalist.stream().filter(m -> m.get("num") != null).
                    map(m -> Integer.valueOf(m.get("num").toString())).collect(Collectors.summingInt(m -> m));
            Map<String, Object> pollutantMap = new HashMap<>();
            List<Map<String, Object>> pollutants = new ArrayList<>();
            Map<String, List<Map<String, Object>>> collect = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));
            for (String FK_MonitorPointTypeCode : collect.keySet()) {
                pollutantMap.put("pollutanttype", FK_MonitorPointTypeCode);
                //废气,烟气污染物
                if (FK_MonitorPointTypeCode.equals(WasteGasEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(SmokeEnum.getCode() + "")) {
                    pollutantMap.put("unorgflag", false);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetService.getGasPollutantByOutputId(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                //废气,烟气污染物
                else if (FK_MonitorPointTypeCode.equals(unOrganizationWasteGasEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(FactoryBoundaryStinkEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(FactoryBoundarySmallStationEnum.getCode() + "")) {
                    pollutantMap.put("unorgflag", true);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetService.getGasPollutantByOutputId(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                //废水污染物
                else if (FK_MonitorPointTypeCode.equals(WasteWaterEnum.getCode() + "")) {
                    pollutantMap.put("datamark", "1");
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = waterOutPutPollutantSetService.getPollutantByParamMap(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                //雨水污染物
                else if (FK_MonitorPointTypeCode.equals(RainEnum.getCode() + "")) {
                    pollutantMap.put("datamark", "3");
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = waterOutPutPollutantSetService.getPollutantByParamMap(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
            }
            //默认查询今天
            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            paramMap.put("mns", dgimns);

            List<Document> monitorDataByParamMap = new ArrayList<>();


            //查询异常数据
            String monitortimekey = "ExceptionTime";
            if (alarmsize > 0) {
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("collection", "ExceptionData");//异常
                monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
            }
            List<Map<String, Object>> exception = getAlarmResults(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, "exception");

            resultMap.put("exceptiontask", exception);
            monitorDataByParamMap.clear();
            //查询超标数据
            if (devsize > 0) {
                monitortimekey = "OverTime";
                paramMap.put("monitortimekey", "OverTime");
                paramMap.put("collection", "OverData");//超标
                monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
            }
            List<Map<String, Object>> over = getAlarmResults(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, "over");
            resultMap.put("overtask", over);


            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/4/10 0010 上午 10:24
     * @Description: 组装异常，超标报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, alarmtype]
     * @throws:
     */
    private List<Map<String, Object>> getAlarmResults(List<Document> monitorDataByParamMap, List<Map<String, Object>> pollutants, List<Map<String, Object>> outPutInfosByParamMap, String monitortimekey, String alarmtype) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Long>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("PollutantCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()
                , Collectors.groupingBy(n -> (n.get("PollutantCode").toString()) + "_" + (n.get("ExceptionType") == null ? "" : n.get("ExceptionType").toString()), Collectors.counting())));
        Map<String, List<Document>> datas = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        for (String DataGatherCode : datas.keySet()) {
            Map<String, Object> data = new HashMap<>();
            Object FK_MonitorPointTypeCode = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null && m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst().orElse(new HashMap<>()).get("FK_MonitorPointTypeCode");
            Object OutputName = outPutInfosByParamMap.stream().filter(m -> m.get("OutputName") != null && m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst().orElse(new HashMap<>()).get("OutputName");
            Integer monitortype = FK_MonitorPointTypeCode == null ? -1 : Integer.valueOf(FK_MonitorPointTypeCode.toString());
            String monitorpointname = OutputName == null ? "" : OutputName.toString();
            int interval;
            //获取配置的各类型排口的间隔时间
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//废气
                interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//废水、雨水
                interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
            } else {//其它类型监测点
                interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
            }
            List<String> times = datas.get(DataGatherCode).stream().filter(m -> m.get(monitortimekey) != null).map(m -> {
                try {
                    return DataFormatUtil.formatCST(m.get(monitortimekey).toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return "";
            }).collect(Collectors.toList());
            String timeline = DataFormatUtil.mergeContinueDate(times, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");


            Map<String, Long> stringLongMap = collect.get(DataGatherCode);
            List<String> pollutant = new ArrayList<>();
            for (String pollutantcode : stringLongMap.keySet()) {
                String[] split = pollutantcode.split("_");

                Map<String, Object> stringObjectMap = pollutants.stream().filter(m -> m.get("pollutantname") != null && m.get("pollutantcode") != null && split[0].equals(m.get("pollutantcode").toString())).findFirst().orElse(new HashMap<>());
                String pollutantname = stringObjectMap.get("pollutantname") == null ? "" : stringObjectMap.get("pollutantname").toString();
                if (split.length > 1) {
                    pollutantname += "【" + stringLongMap.get(split[0] + "_" + split[1]) + "次】";
                    String exception = OnlineController.getAlarmName(split[1], alarmtype);
                    pollutantname = pollutantname.replace("【", "【" + exception);
                } else {
                    pollutantname += "【" + stringLongMap.get(split[0] + "_") + "次】";
                    String exception = OnlineController.getAlarmName("", alarmtype);
                    pollutantname = pollutantname.replace("【", "【" + exception);
                }
                pollutant.add(pollutantname);
            }

            data.put("outputname", monitorpointname);
            data.put("timeline", timeline);
            data.put("pollutants", pollutant.stream().collect(Collectors.joining("、")));
            resultList.add(data);
        }
        return resultList;
    }

    /**
     * @author: chengzq
     * @date: 2020/6/29 0029 上午 9:20
     * @Description: 组装报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type, countdata, resultList]
     * @throws:
     */
    private void AssData(String type, Map<String, Integer> countdata, List<Map<String, Object>> resultList) {
        for (Map<String, Object> map : resultList) {
            String monitortime = map.get("monitortime") == null ? "" : map.get("monitortime").toString();
            if (countdata.get(monitortime) != null) {
                map.put(type, countdata.get(monitortime));
            }
        }
    }


    private List<Map<String, Object>> orderMonitorTypeData(List<Map<String, Object>> list) {
        //排序
        Comparator<Object> orderbyorder = Comparator.comparing(m -> ((Map) m).get("orderindex").toString());
        list = list.stream().sorted(orderbyorder).collect(Collectors.toList());
        return list;
    }

    /**
     * @author: xsm
     * @date: 2020/11/04 14:42
     * @Description: 根据监测时间和恶臭标记类型统计报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countStinkAlarmNumByParamForApp", method = RequestMethod.POST)
    public Object countStinkAlarmNumByParamForApp(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                  @RequestJson(value = "stinkflags", required = true) List<Integer> stinkflags
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);//数据权限
            paramMap.put("stinkflag", stinkflags);
            List<Map<String, Object>> stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
            Date starttime = DataFormatUtil.parseDate(monitortime + " 00:00:00");
            Date endtime = DataFormatUtil.parseDate(monitortime + " 23:59:59");
            int[] reminds = {1, 2, 3, 4, 5};
            List<Map<String, Object>> result = new ArrayList<>();
            if (stenchMonitorPointInfo != null && stenchMonitorPointInfo.size() > 0) {
                for (Integer stinkflag : stinkflags) {
                    Set mns = new HashSet();
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("stinktype", stinkflag);
                    for (Map<String, Object> map : stenchMonitorPointInfo) {
                        if (map.get("DGIMN") != null && map.get("MonitorPointCategory") != null && stinkflag == Integer.parseInt(map.get("MonitorPointCategory").toString())) {
                            mns.add(map.get("DGIMN").toString());
                        }
                    }
                    if (mns.size() > 0) {
                        OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
                        queryVO.setMns(mns);
                        queryVO.setStartTime(starttime);
                        queryVO.setEndTime(endtime);
                        List<Map<String, Object>> list = countHistoryAlarmNumByParam("day", queryVO, reminds);
                        resultmap.put("alarmdata", list);
                    }
                    result.add(resultmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/06 13:36
     * @Description: 统计某个点某段时间内各报警类型的报警次数（超阈值、超标、异常查询model表）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "CountOnePointAlarmNumDataByParams", method = RequestMethod.POST)
    public Object CountOnePointAlarmNumDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                    @RequestJson(value = "reminds") List<Integer> reminds,
                                                    @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                    @RequestJson(value = "timetype", required = false) String timetype,
                                                    @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();

            if (StringUtils.isBlank(timetype)) {
                timetype = "day";
            }
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timetype, starttime, endtime);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setMonitorPointType(monitorpointtype);
            Set<String> mns = new HashSet<>();
            mns.add(dgimn);
            queryVO.setMns(mns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            int total = 0;
            for (int remind : reminds) {
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getAlarmCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                queryVO.setCollection(collection);          //放入连接表
                List<Document> documents = new ArrayList<>();
                if (remindTypeEnum == ConcentrationChangeEnum) {
                    queryVO.setUnwindFieldName("MinuteDataList");
                    queryVO.setPollutantCodeFieldName("MinuteDataList.PollutantCode");
                    documents = onlineCountAlarmService.countNDAndPFLAlarmDataByPollutantCode3(queryVO, timeStyle);
                } else {

                    documents = onlineCountAlarmService.countPointAlarmNumDataByParam(collection, remind, starttime, endtime, mns);
                }
                if (documents.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    List<Map<String, Object>> pollutantlist = new ArrayList<>();
                    String thetime = "";
                    int totalnum = 0;
                    for (Document document : documents) {
                        if (!"".equals(thetime)) {
                            Date d1 = DataFormatUtil.parseDate(thetime);
                            Date d2 = DataFormatUtil.parseDate(document.getString("MaxTime"));
                            int d = (int) (d2.getTime()) / 1000 - (int) (d1.getTime()) / 1000;
                            if (d > 0) {
                                thetime = document.getString("MaxTime");
                            }
                        } else {
                            thetime = document.getString("MaxTime");
                        }
                        Map<String, Object> pollutantmap = new HashMap<>();
                        totalnum += document.getInteger("num");
                        if (pollutantcodes.size() > 0 && pollutantcodes.contains(document.getString("_id"))) {
                            pollutantmap.put("pollutantname", codemap.get(document.getString("_id")));
                            pollutantmap.put("num", document.getInteger("num"));
                            pollutantlist.add(pollutantmap);
                        } else if (pollutantcodes.size() == 0) {
                            pollutantmap.put("pollutantname", codemap.get(document.getString("_id")));
                            pollutantmap.put("num", document.getInteger("num"));
                            pollutantlist.add(pollutantmap);
                        }
                    }
                    map.put("lasttime", thetime);
                    map.put("pollutants", pollutantlist);
                    map.put("totalnum", totalnum);
                    total += totalnum;
                    if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {
                        resultMap.put("concentrationchange", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                        resultMap.put("flowchange", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {
                        resultMap.put("early", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {
                        resultMap.put("over", map);
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                        resultMap.put("exception", map);
                    }
                }
                resultMap.put("total", total);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/4/16 13:58
     * @Description: 根据类型获取要查询的表(2.0 超阈值 、 超标 、 异常用model表)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getAlarmCollection(CommonTypeEnum.RemindTypeEnum remindTypeEnum) {
        String collection = "";
        switch (remindTypeEnum) {
            case ExceptionAlarmEnum:
                collection = "ExceptionModel";
                break;
            case OverAlarmEnum:
            case EarlyAlarmEnum:
                collection = "OverModel";
                break;
            case ConcentrationChangeEnum:
                collection = "MinuteData";
                break;
        }
        return collection;
    }


    /**
     * @author: xsm
     * @date: 2022/5/14 14:53
     * @Description: 获取点位报警、设备运维提醒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:timetype:sh hour/day
     * @return:
     */
    @RequestMapping(value = "getMonitorPointAlarmRemindData", method = RequestMethod.POST)
    public Object getMonitorPointAlarmRemindData(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype", required = false) String timetype,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "ismodelalarm", required = false) Boolean ismodelalarm
    ) {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimn", dgimn);
            String start = "";
            String end = "";
            if (StringUtils.isNotBlank(timetype) && "hour".equals(timetype)) {
                start = starttime + ":00:00";
                end = endtime + ":59:59";
            } else {
                start = starttime + " 00:00:00";
                end = endtime + " 23:59:59";
            }

            paramMap.put("starttime", start);
            paramMap.put("endtime", end);
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), RainEnum.getCode()
            );
            int alarmnum = 0;
            String overexceptiontime = null;
            String exceptionexceptiontime = null;
            String earlyexceptiontime = null;
            int stopnum = 0;
            List<Integer> reminds = Arrays.asList(1, 2, 3, 4, 5);
            //报警统计
            Map<String, Object> map = (Map<String, Object>) CountOnePointAlarmTimeNumDataByParams(dgimn, start, new ArrayList<>(), reminds, monitorpointtype, timetype, end);
            if (map != null) {
                Map<String, Object> datamap = (Map<String, Object>) map.get("data");
                alarmnum = datamap.get("concentrationchange") != null && JSONObject.fromObject(datamap).getJSONObject("concentrationchange") != null ?
                        JSONObject.fromObject(datamap).getJSONObject("concentrationchange").getInt("totalnum") : 0;
                overexceptiontime =  datamap.get("over") != null && JSONObject.fromObject(datamap).getJSONObject("over") != null ?
                        JSONObject.fromObject(datamap).getJSONObject("over").getString("totalexceptiontime") : null;
                exceptionexceptiontime =  datamap.get("exception") != null && JSONObject.fromObject(datamap).getJSONObject("exception") != null ?
                        JSONObject.fromObject(datamap).getJSONObject("exception").getString("totalexceptiontime") : null;
                earlyexceptiontime =  datamap.get("early") != null && JSONObject.fromObject(datamap).getJSONObject("early") != null ?
                        JSONObject.fromObject(datamap).getJSONObject("early").getString("totalexceptiontime") : null;
            }
            //运维设备统计
            paramMap.put("monitorpointid", monitorpointid);
            Long countall = deviceDevOpsInfoService.CountDeviceDevOpsHistoryListDataNumByParams(paramMap);
            paramMap.remove("monitorpointid");
            //停产记录统计
            if (monitortypes.contains(monitorpointtype)) {
                List<Map<String, Object>> stoplist = new ArrayList<>();
                if (monitorpointtype == RainEnum.getCode()) {
                    paramMap.clear();
                    paramMap.put("monitorpointtype", monitorpointtype);
                    paramMap.put("dgimn", dgimn);
                    paramMap.put("startmonitortime", start);
                    paramMap.put("endmonitortime", end);
                    stoplist = monitorControlService.getMonitorControlLogDataByParamMap(paramMap);
                } else {
                    stoplist = stopProductionInfoService.getStopProductionInfosByParamMap(paramMap);
                }
                if (stoplist != null && stoplist.size() > 0) {
                    stopnum = stoplist.size();
                }
            }
            //任务条数统计
            paramMap.put("starttime", start);
            paramMap.put("endtime", end);
            Map<String, Object> taskmap = alarmTaskDisposeService.getAlarmAndDevOpsTaskNumByParam(paramMap);
            result.put("alarmnum", alarmnum);
            result.put("exceptionexceptiontime", exceptionexceptiontime);
            result.put("earlyexceptiontime", earlyexceptiontime);
            result.put("overexceptiontime", overexceptiontime);
            result.put("devopsnum", countall);
            result.put("tasknum", taskmap);
            result.put("stopnum", stopnum);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/06 13:36
     * @Description: 统计某个点某段时间内各报警类型的 报警时段 数（超阈值、超标、异常查询model表）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "CountOnePointAlarmTimeNumDataByParams", method = RequestMethod.POST)
    public Object CountOnePointAlarmTimeNumDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                                        @RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                        @RequestJson(value = "reminds") List<Integer> reminds,
                                                        @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                        @RequestJson(value = "timetype", required = false) String timetype,
                                                        @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codemap = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> obj : pollutants) {
                    codemap.put(obj.get("code").toString(), obj.get("name"));
                }
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();

            if (StringUtils.isBlank(timetype)) {
                timetype = "day";
            }
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType(timetype, starttime, endtime);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            queryVO.setMonitorPointType(monitorpointtype);
            Set<String> mns = new HashSet<>();
            mns.add(dgimn);
            queryVO.setMns(mns);
            String timeStyle = "%Y-%m-%d %H:%M:%S";
            int total = 0;
            for (int remind : reminds) {
                long totalExceptionTime = 0l;
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                String timeFieldName = getTimeFieldName(remindTypeEnum);
                String collection = getAlarmCollection(remindTypeEnum);
                queryVO.setTimeFieldName(timeFieldName);    //放入时间字段
                queryVO.setCollection(collection);          //放入连接表
                List<Document> documents = new ArrayList<>();
                Set<String> names = new HashSet<>();
                if (remindTypeEnum == ConcentrationChangeEnum) {
                    queryVO.setUnwindFieldName("MinuteDataList");
                    queryVO.setPollutantCodeFieldName("MinuteDataList.PollutantCode");
                    documents = onlineCountAlarmService.countNDAndPFLAlarmDataByPollutantCode3(queryVO, timeStyle);
                    if (documents.size() > 0) {
                        Map<String, Object> map = new HashMap<>();
                        List<Map<String, Object>> pollutantlist = new ArrayList<>();
                        String thetime = "";
                        int totalnum = 0;
                        for (Document document : documents) {
                            if (!"".equals(thetime)) {
                                Date d1 = DataFormatUtil.parseDate(thetime);
                                Date d2 = DataFormatUtil.parseDate(document.getString("MaxTime"));
                                int d = (int) (d2.getTime()) / 1000 - (int) (d1.getTime()) / 1000;
                                if (d > 0) {
                                    thetime = document.getString("MaxTime");
                                }
                            } else {
                                thetime = document.getString("MaxTime");
                            }
                            Map<String, Object> pollutantmap = new HashMap<>();
                            totalnum += document.getInteger("num");
                            if (pollutantcodes.size() > 0 && pollutantcodes.contains(document.getString("_id"))) {
                                if (codemap.get(document.getString("_id")) != null) {
                                    names.add(codemap.get(document.getString("_id")).toString());
                                }
                            } else if (pollutantcodes.size() == 0) {
                                if (codemap.get(document.getString("_id")) != null) {
                                    names.add(codemap.get(document.getString("_id")).toString());
                                }
                            }
                        }
                        map.put("lasttime", thetime);
                        map.put("pollutants", names);
                        map.put("totalnum", totalnum);
                        total += totalnum;
                        resultMap.put("concentrationchange", map);
                    }
                } else {
                    documents = onlineCountAlarmService.countPointAlarmTimesNumDataByParam(collection, remind, starttime, endtime, mns);
                    if (documents.size() > 0) {
                        Document onedoc = documents.get(0);
                        String thetime = onedoc.getString("MaxTime");
                        List<Document> timelist = (List<Document>) onedoc.get("times");
                        Date firsttime = null;
                        Date lasttime = null;
                        int timesnum = 0;
                        //按时间排序
                        timelist = timelist.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("starttime"))).collect(Collectors.toList());
                        if (timelist != null && timelist.size() > 0) {
                            for (Document podo : timelist) {
                                if (codemap.get(podo.getString("code")) != null) {
                                    names.add(codemap.get(podo.getString("code")).toString());
                                }
                                //比较时间 获取报警时段
                                if (podo.get("starttime") != null && podo.get("endtime") != null) {
                                    if (firsttime == null && lasttime == null) {
                                        if (podo.getDate("endtime").getTime() == podo.getDate("starttime").getTime()) {
                                            totalExceptionTime += 1000 * 60;
                                        } else {
                                            totalExceptionTime += ((podo.getDate("endtime").getTime() - podo.getDate("starttime").getTime()));
                                        }
                                        firsttime = podo.getDate("starttime");
                                        lasttime = podo.getDate("endtime");
                                        timesnum = timesnum + 1;
                                    } else {
                                        //比较时间段
                                        //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                        if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(lasttime)) ||
                                                podo.getDate("starttime").before(lasttime)) {
                                            //若被包含 比较两个结束时间
                                            if (lasttime.before(podo.getDate("endtime"))) {
                                                //若第一次报警的结束时间 小于 第二个报警时段的结束时间
                                                //则进行赋值
                                                totalExceptionTime = (podo.getDate("endtime").getTime() - podo.getDate("starttime").getTime()) - (lasttime.getTime() - podo.getDate("starttime").getTime()) + totalExceptionTime;
                                                lasttime = podo.getDate("endtime");
                                            }
                                        } else {
                                            timesnum += 1;
                                            if (podo.getDate("endtime").getTime() == podo.getDate("starttime").getTime()) {
                                                totalExceptionTime += 1000 * 60;
                                            } else {
                                                totalExceptionTime += ((podo.getDate("endtime").getTime() - podo.getDate("starttime").getTime()));
                                            }
                                            //将重新赋值开始 结束时间
                                            firsttime = podo.getDate("starttime");
                                            lasttime = podo.getDate("endtime");
                                        }
                                    }
                                }
                            }
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("lasttime", thetime);
                        map.put("pollutants", names);
                        map.put("totalnum", timesnum);
                        BigDecimal totalExceptionTimeDecimal = new BigDecimal(totalExceptionTime / 1000 / 60);
                        BigDecimal divisor = new BigDecimal(60);
                        BigDecimal divide = totalExceptionTimeDecimal.divide(divisor, 2, BigDecimal.ROUND_HALF_UP);
                        //大于等于0.1保留一位小数，小于0.1保留两位小数
                        if (divide.compareTo(new BigDecimal(0.1)) >= 0) {
                            divide = divide.setScale(1, BigDecimal.ROUND_HALF_UP);
                        }
                        map.put("totalExceptionTime", divide.toString().equals("0") ? null : divide.toString().replace(".0", "") + "小时");

                        total += timesnum;
                        if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {
                            resultMap.put("early", map);
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {
                            resultMap.put("over", map);
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                            resultMap.put("exception", map);
                        }
                    }
                }

            }
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }


    }

}
