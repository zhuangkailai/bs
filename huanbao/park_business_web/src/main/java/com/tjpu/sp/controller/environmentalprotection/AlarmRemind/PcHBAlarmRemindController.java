package com.tjpu.sp.controller.environmentalprotection.AlarmRemind;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.base.mn.AlarmMNService;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;

/**
 * @author: zhangzhenchao
 * @date: 2019/11/2 13:51
 * @Description: pc端环保报警提醒
 */
@RestController
@RequestMapping("pcHBAlarmRemindController")
public class PcHBAlarmRemindController {

    private final AlarmMNService alarmMNService;
    private final OnlineCountAlarmService onlineCountAlarmService;
    private final CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;

    public PcHBAlarmRemindController(AlarmMNService alarmMNService, OnlineCountAlarmService onlineCountAlarmService, CheckProblemExpoundService checkProblemExpoundService) {
        this.alarmMNService = alarmMNService;
        this.onlineCountAlarmService = onlineCountAlarmService;
        this.checkProblemExpoundService = checkProblemExpoundService;
    }
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;

    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: zhangzc
     * @date: 2019/7/13 11:19
     * @Description: 获得监控预警下各个模块当天的预警次数
     * @param:
     * @return: 注：添加新的监测点类型时需要统计各类型报警个数时只需要在   PcHBAlarmMenus枚举中添加各菜单即可
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "getMonitorAlarmNumForMenusByMenuID", method = RequestMethod.POST)
    public Object getMonitorAlarmNumForMenusByMenuID(@RequestJson(value = "menuid") String menuid,
                                                     @RequestJson(value = "usercode", required = false) String userCode,
                                                     @RequestJson(value = "userauth", required = false) List<JSONObject> objectList) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            String userid = "";
            //汉源项目 父级ID为信息查询
            if (!CommonTypeEnum.SocketTypeEnum.GradeAlarmEnum.getMenuid().equals(menuid) &&
                    !CommonTypeEnum.SocketTypeEnum.alarmDataSearchEnum.getMenuid().equals(menuid)) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            if (userCode == null) {
                userCode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
                userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            }
            if (objectList == null) {
                objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            }
            Assert.notNull(userCode, "用户code不为空！");
            Assert.notNull(userCode, "权限菜单不为空！");
            //根据菜单ID获取用户的报警菜单集合
            List<Map<String, Object>> childrenMenus = AlarmRemindUtil.getChildrenMenusByMenuId(menuid, objectList, null);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (childrenMenus == null || childrenMenus.size() == 0) {
                resultMap.put("sum", 0);
                resultMap.put("datalist", dataList);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            //用户权限下拥有的报警菜单集合
            List<CommonTypeEnum.PcHBAlarmMenus> pcHBAlarmMenus = new ArrayList<>();
            //监测点类型
            Set<String> monitorPointTypes = new HashSet<>();
            //每个提醒类型对应的监测点类型
            Map<Integer, Set<Integer>> rmsMapSet = new HashMap<>();
            getPcHBAlarmMenuEnums(childrenMenus, pcHBAlarmMenus, monitorPointTypes, rmsMapSet);
            List<DeviceStatusVO> mns = alarmMNService.getMnsByMonitorPointTypes(monitorPointTypes);

            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            Map<String, Object> params = new HashMap<>();
            params.put("userid", userid);
            params.put("usercode", userCode);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataService.getDGIMNByParamMap(params);
            //System.out.println(dgimnByParamMap);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            //从查询出的监测点里筛选拥有权限的监测点
            mns.removeIf(m -> !collect.contains(m.getDgimn()));
            /*-----设置权限end--------*/

            if (mns.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            //根据监测点类型获取mn报警的mn号
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            LocalDate localDate = LocalDate.now();
            queryVO.setStartTime(DataFormatUtil.parseDate(localDate + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(localDate + " 23:59:59"));
            queryVO.setUserId(userCode);
            //每个监测点类型对应的MN号
            Map<String, List<DeviceStatusVO>> monitorMnsMap = mns.stream().collect(Collectors.groupingBy(DeviceStatusVO::getFkMonitorpointtypecode));
            if (pcHBAlarmMenus.size() == 0 || monitorPointTypes.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            //提醒类型-监测点类型-报警个数
            Map<Integer, Map<Integer, Integer>> rmmns = new HashMap<>();
            for (Map.Entry<Integer, Set<Integer>> entry : rmsMapSet.entrySet()) {
                Integer remindType = entry.getKey(); //提醒类型
                Set<String> remindMns = new HashSet<>();    // mn号
                Set<Integer> rmSet = entry.getValue();  //监测点类型
                rmSet.stream().filter(m -> monitorMnsMap.get(m.toString()) != null).map(m -> monitorMnsMap.get(m.toString())).forEach(deviceStatusVOS -> deviceStatusVOS.stream().map(DeviceStatusVO::getDgimn).forEach(remindMns::add));
                //根据mn号和提醒类型去查询
                queryVO.setTimeFieldName(AlarmRemindUtil.getTimeFieldNameByRemindType(remindType)); //放入时间字段
                queryVO.setCollection(AlarmRemindUtil.getCollectionByRemindType(remindType));   //放入连接表
                queryVO.setMns(remindMns);
                queryVO.setUnwindFieldName(AlarmRemindUtil.getUnWindowNameByRemindType(remindType));
                if (CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode() == remindType) {//无流量报警
                    queryVO.setExceptionType(CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum.getCode());
                } else if (CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode() == remindType
                        || CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum2.getCode() == remindType) {//普通异常类型
                    queryVO.setExceptionType("0");
                } else {
                    queryVO.setExceptionType(null);
                }
                List<String> alarmMns = new ArrayList<>();
                if (remindType == ExceptionAlarmEnum2.getCode() ||
                        remindType == OverAlarmEnum2.getCode() ||
                        remindType == EarlyAlarmEnum2.getCode()) {
                    alarmMns = onlineCountAlarmService.getAlarmMnsByParamsForHierarchicalEarly(queryVO, remindType);   //该提醒类型报警mn
                } else {
                    alarmMns = onlineCountAlarmService.getAlarmMnsByParams(queryVO);   //该提醒类型报警mn
                }
                Map<Integer, Integer> monitorTypeAlarmNum = new HashMap<>();
                for (Integer monitor : rmSet) {
                    List<DeviceStatusVO> deviceStatusVOS = monitorMnsMap.get(monitor.toString()) == null ? new ArrayList<>() : monitorMnsMap.get(monitor.toString());
                    int num = 0;
                    for (DeviceStatusVO statusVO : deviceStatusVOS) {
                        String dgimn = statusVO.getDgimn();
                        if (alarmMns.contains(dgimn)) {
                            num++;
                        }
                    }
                    monitorTypeAlarmNum.put(monitor, num);
                }
                rmmns.put(remindType, monitorTypeAlarmNum);
            }
            int sum = 0;
            for (CommonTypeEnum.PcHBAlarmMenus pcHBAlarmMenu : pcHBAlarmMenus) {
                Integer remindType = pcHBAlarmMenu.getRemindType();
                Integer monitorPointType = pcHBAlarmMenu.getMonitorPointType();
                sum += rmmns.get(remindType).get(monitorPointType);
            }
            format(childrenMenus, dataList, rmmns, 0);
            resultMap.put("sum", sum);
            resultMap.put("datalist", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private int format(List<Map<String, Object>> childrenMenus, List<Map<String, Object>> dataList, Map<Integer, Map<Integer, Integer>> rmmns, int sum) {
        for (Map<String, Object> menu : childrenMenus) {
            String menuCode = menu.get("menucode").toString();
            String menuname = menu.get("menuname").toString();
            Map<String, Object> map = new HashMap<>();
            map.put("sysmodel", menuCode);
            map.put("name", menuname);
            CommonTypeEnum.PcHBAlarmMenus menuEnum = CommonTypeEnum.PcHBAlarmMenus.getCodeByString(menuCode);
            if (menuEnum != null) {
                Integer remindType = menuEnum.getRemindType();
                Integer monitorPointType = menuEnum.getMonitorPointType();
                int count = rmmns.get(remindType).get(monitorPointType);
                map.put("count", count);
                sum += count;
                dataList.add(map);
            } else {    //子集不为空
                Object children = menu.get("datalistchildren");
                if (children != null) {
                    childrenMenus = (List<Map<String, Object>>) children;
                    if (childrenMenus.size() > 0) {
                        int format = format(childrenMenus, dataList, rmmns, 0);
                        map.put("count", format);
                        dataList.add(map);
                    }
                }
            }
        }
        return sum;
    }

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/1 15:39
     * @Description: 获取需要报警的菜单枚举
     * @param:
     * @return:
     * @throws:
     */
    @SuppressWarnings("unchecked")
    private void getPcHBAlarmMenuEnums(List<Map<String, Object>> childrenByMenuId,
                                       List<CommonTypeEnum.PcHBAlarmMenus> menuEnums,
                                       Set<String> monitorPointTypes, Map<Integer, Set<Integer>> rmsMapSet) {
        for (Map<String, Object> map : childrenByMenuId) {
            String menuCode = map.get("menucode").toString();
            CommonTypeEnum.PcHBAlarmMenus menuEnum = CommonTypeEnum.PcHBAlarmMenus.getCodeByString(menuCode);
            if (menuEnum != null) {
                menuEnums.add(menuEnum);
                Integer monitorPointType = menuEnum.getMonitorPointType();
                monitorPointTypes.add(monitorPointType.toString());
                Integer remindType = menuEnum.getRemindType();
                if (rmsMapSet.containsKey(remindType)) {
                    rmsMapSet.get(remindType).add(monitorPointType);
                } else {
                    Set<Integer> types = new HashSet<>();
                    types.add(monitorPointType);
                    rmsMapSet.put(remindType, types);
                }
            }
            Object children = map.get("datalistchildren");
            if (children != null) {
                childrenByMenuId = (List<Map<String, Object>>) children;
                getPcHBAlarmMenuEnums(childrenByMenuId, menuEnums, monitorPointTypes, rmsMapSet);
            }
        }
    }

//    @SuppressWarnings("unchecked")
//    public Object getAlarmNumByParams(Map<String, Object> paramMap) {
//        try {
//            String userCode = paramMap.get("usercode").toString();
//            String monitorPointType = paramMap.get("monitorpointtype").toString();
//            List<Map<String, Object>> remindTypes = (List<Map<String, Object>>) paramMap.get("remindtypes");
//            Map<String, Object> resultMap = new HashMap<>();
//            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
//            LocalDate localDate = LocalDate.now();
//            queryVO.setStartTime(DataFormatUtil.parseDate(localDate + " 00:00:00"));
//            queryVO.setEndTime(DataFormatUtil.parseDate(localDate + " 23:59:59"));
//            queryVO.setUserId(userCode);
//            Set<String> monitorPointTypes = new HashSet<>();
//            monitorPointTypes.add(monitorPointType);
//            List<DeviceStatusVO> deviceStatusVOS = alarmMNService.getMnsByMonitorPointTypes(monitorPointTypes);
//            Set<String> mns = deviceStatusVOS.stream().map(DeviceStatusVO::getDgimn).collect(Collectors.toSet());
//            queryVO.setMns(mns);
//            List<Map<String, Object>> resultList = new ArrayList<>();
//            int sum = 0;
//            for (Map<String, Object> map1 : remindTypes) {
//                int remindType = (int) map1.get("remindtype");
//                String parentCode = map1.get("parentcode").toString();
//                CommonTypeEnum.PcHBAlarmMenus enumByParam = CommonTypeEnum.PcHBAlarmMenus.getEnumByParam(Integer.valueOf(monitorPointType), remindType);
//                if (enumByParam != null) {
//                    Map<String, Object> map = new HashMap<>();
//                    queryVO.setTimeFieldName(AlarmRemindUtil.getTimeFieldNameByRemindType(remindType)); //放入时间字段
//                    queryVO.setCollection(AlarmRemindUtil.getCollectionByRemindType(remindType));   //放入连接表
//                    queryVO.setUnwindFieldName(AlarmRemindUtil.getUnWindowNameByRemindType(remindType));
//                    map.put("sysmodel", enumByParam.getCode());
//                    map.put("name", enumByParam.getName());
//                    int num = onlineCountAlarmService.getAlarmNumByParams(queryVO);
//                    map.put("count", num);
//                    map.put("parentcode", parentCode);
//                    sum += num;
//                    resultList.add(map);
//                }
//            }
//            resultMap.put("sum", sum);
//            resultMap.put("datalist", resultList);
//            if (sum == 0) {
//                return null;
//            }
//            return AuthUtil.parseJsonKeyToLower("success", resultMap);
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }

    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletypes",
                Arrays.asList(
                        CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.DevEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.TBEnum.getCode()
                )
                );
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }

    /**
     * @author: xsm
     * @date: 2022/01/13 09:59
     * @Description: 获得监测管理下各个模块的报警次数
     * @param:
     * @return: 注：添加新的监测点类型时需要统计各类型报警个数时只需要在   PcHBAlarmMenus枚举中添加各菜单即可
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "getMonitorAlarmNumForMenusByMenuCode", method = RequestMethod.POST)
    public Object getMonitorAlarmNumForMenusByMenuCode(@RequestJson(value = "menucode") String menucode,
                                                       @RequestJson(value = "usercode", required = false) String userCode,
                                                       @RequestJson(value = "userauth", required = false) List<JSONObject> objectList) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            //根据code
            if (objectList == null) {
                objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            }
            //根据菜单Code获取用户的报警菜单集合
            List<Map<String, Object>> childrenMenus = AlarmRemindUtil.getSonMenusByMenuCode(menucode, objectList, null);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (childrenMenus == null || childrenMenus.size() == 0) {
                resultMap.put("sum", 0);
                resultMap.put("datalist", dataList);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            //用户权限下拥有的报警菜单集合
            List<CommonTypeEnum.MonitorManagementAlarmMenus> monitorManagementAlarmMenus = new ArrayList<>();
            //监测点类型
            Set<String> monitorPointTypes = new HashSet<>();
            //每个提醒类型对应的监测点类型
            Map<Integer, Set<Integer>> rmsMapSet = new HashMap<>();
            getMonitorAlarmMenuEnums(childrenMenus, monitorManagementAlarmMenus, monitorPointTypes, rmsMapSet);
            List<DeviceStatusVO> mns = alarmMNService.getMnsByMonitorPointTypes(monitorPointTypes);

            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            Map<String, Object> params = new HashMap<>();
            params.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataService.getDGIMNByParamMap(params);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            //从查询出的监测点里筛选拥有权限的监测点
            mns.removeIf(m -> !collect.contains(m.getDgimn()));
            /*-----设置权限end--------*/

            if (mns.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            //根据监测点类型获取mn报警的mn号
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            LocalDate localDate = LocalDate.now();
            queryVO.setStartTime(DataFormatUtil.parseDate(localDate + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(localDate + " 23:59:59"));
            queryVO.setUserId(userCode);
            //每个监测点类型对应的MN号
            Map<String, List<DeviceStatusVO>> monitorMnsMap = mns.stream().collect(Collectors.groupingBy(DeviceStatusVO::getFkMonitorpointtypecode));
            if (monitorManagementAlarmMenus.size() == 0 || monitorPointTypes.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            //提醒类型-监测点类型-报警个数
            Map<Integer, Map<Integer, Integer>> rmmns = new HashMap<>();
            for (Map.Entry<Integer, Set<Integer>> entry : rmsMapSet.entrySet()) {
                Integer remindType = entry.getKey(); //提醒类型
                Set<String> remindMns = new HashSet<>();    // mn号
                Set<Integer> rmSet = entry.getValue();  //监测点类型
                rmSet.stream().filter(m -> monitorMnsMap.get(m.toString()) != null).map(m -> monitorMnsMap.get(m.toString())).forEach(deviceStatusVOS -> deviceStatusVOS.stream().map(DeviceStatusVO::getDgimn).forEach(remindMns::add));
                if (remindType == CommonTypeEnum.RemindTypeEnum.AlarmWorkOrderEnum.getCode()) {
                    Map<String, Object> paramMap = new HashMap<>();
                    List<String> rightList = getRightList(userid);
                    paramMap.put("userid", userid);
                    if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                        paramMap.put("ishaveflag", "yes");
                    }else {
                        paramMap.put("ishaveflag", "no");
                    }
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());
                    paramMap.put("starttime", DataFormatUtil.getDateYMD(new Date()));
                    paramMap.put("endtime", DataFormatUtil.getDateYMD(new Date()));
                    List<Map<String, Object>> alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataGroupByType(paramMap);
                    Map<Integer, Integer> taskmap = new HashMap<>();
                    for (Map<String, Object> map : alarmlistData) {
                        taskmap.put(Integer.valueOf(map.get("FK_MonitorPointTypeCode").toString()), Integer.valueOf(map.get("num").toString()));
                    }
                    rmmns.put(remindType, taskmap);
                } else if(remindType == DevOpsWorkOrderEnum.getCode()){
                    Map<String, Object> paramMap = new HashMap<>();
                    List<String> rightList = getRightList(userid);
                    paramMap.put("userid", userid);
                    if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                        paramMap.put("ishaveflag", "yes");
                    }else {
                        paramMap.put("ishaveflag", "no");
                    }
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
                    paramMap.put("starttime", DataFormatUtil.getDateYMD(new Date()));
                    paramMap.put("endtime", DataFormatUtil.getDateYMD(new Date()));

                    List<Map<String, Object>> alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataGroupByType(paramMap);
                    Map<Integer, Integer> taskmap = new HashMap<>();
                    for (Map<String, Object> map : alarmlistData) {
                        taskmap.put(Integer.valueOf(map.get("FK_MonitorPointTypeCode").toString()), Integer.valueOf(map.get("num").toString()));
                    }
                    rmmns.put(remindType, taskmap);
                }else if(remindType == TBWorkOrderEnum.getCode()){
                    Map<String, Object> paramMap = new HashMap<>();
                    List<String> rightList = getRightList(userid);
                    paramMap.put("userid", userid);
                    if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.TBEnum.getCode())) {
                        paramMap.put("ishaveflag", "yes");
                    }else {
                        paramMap.put("ishaveflag", "no");
                    }
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode());
                    paramMap.put("starttime", DataFormatUtil.getDateYMD(new Date()));
                    paramMap.put("endtime", DataFormatUtil.getDateYMD(new Date()));
                    List<Map<String, Object>> alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataGroupByType(paramMap);
                    Map<Integer, Integer> taskmap = new HashMap<>();
                    for (Map<String, Object> map : alarmlistData) {
                        taskmap.put(Integer.valueOf(map.get("FK_MonitorPointTypeCode").toString()), Integer.valueOf(map.get("num").toString()));
                    }
                    rmmns.put(remindType, taskmap);
                } else{
                    //根据mn号和提醒类型去查询
                    queryVO.setTimeFieldName(AlarmRemindUtil.getTimeFieldNameByRemindType(remindType)); //放入时间字段
                    queryVO.setCollection(AlarmRemindUtil.getCollectionByRemindType(remindType));   //放入连接表
                    queryVO.setMns(remindMns);
                    queryVO.setUnwindFieldName(AlarmRemindUtil.getUnWindowNameByRemindType(remindType));
                    if (CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode() == remindType) {//无流量报警
                        queryVO.setExceptionType(CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum.getCode());
                    } else if (CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode() == remindType
                            || CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum2.getCode() == remindType) {//普通异常类型
                        queryVO.setExceptionType("0");
                    } else {
                        queryVO.setExceptionType(null);
                    }
                    List<String> alarmMns = new ArrayList<>();
                    if (remindType == ExceptionAlarmEnum2.getCode() ||
                            remindType == OverAlarmEnum2.getCode() ||
                            remindType == EarlyAlarmEnum2.getCode()) {
                        alarmMns = onlineCountAlarmService.getAlarmMnsByParamsForHierarchicalEarly(queryVO, remindType);   //该提醒类型报警mn
                    } else if (remindType == CommonTypeEnum.RemindTypeEnum.AlarmCountListEnum.getCode()) {
                        queryVO.setTimeFieldName(AlarmRemindUtil.getTimeFieldNameByRemindType(OverAlarmEnum2.getCode())); //放入时间字段
                        queryVO.setCollection(AlarmRemindUtil.getCollectionByRemindType(OverAlarmEnum2.getCode()));   //放入连接表
                        queryVO.setUnwindFieldName(AlarmRemindUtil.getUnWindowNameByRemindType(OverAlarmEnum2.getCode()));
                        alarmMns = onlineCountAlarmService.getAlarmMnsByParamsForHierarchicalEarly(queryVO, OverAlarmEnum2.getCode());   //该提醒类型报警mn
                    } else {
                        alarmMns = onlineCountAlarmService.getAlarmMnsByParams(queryVO);   //该提醒类型报警mn
                    }
                    Map<Integer, Integer> monitorTypeAlarmNum = new HashMap<>();
                    for (Integer monitor : rmSet) {
                        List<DeviceStatusVO> deviceStatusVOS = monitorMnsMap.get(monitor.toString()) == null ? new ArrayList<>() : monitorMnsMap.get(monitor.toString());
                        int num = 0;
                        for (DeviceStatusVO statusVO : deviceStatusVOS) {
                            String dgimn = statusVO.getDgimn();
                            if (alarmMns.contains(dgimn)) {
                                num++;
                            }
                        }
                        monitorTypeAlarmNum.put(monitor, num);
                    }
                    rmmns.put(remindType, monitorTypeAlarmNum);
                }

            }
            int sum = 0;
            for (CommonTypeEnum.MonitorManagementAlarmMenus monitorManagementAlarmMenu : monitorManagementAlarmMenus) {
                Integer remindType = monitorManagementAlarmMenu.getRemindType();
                Integer monitorPointType = monitorManagementAlarmMenu.getMonitorPointType();
                if (rmmns.get(remindType) != null) {
                    if (monitorPointType == CommonTypeEnum.MonitorPointTypeEnum.SmokeGasEnum.getCode()) {
                        if (rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) != null) {
                            sum += rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
                        }
                        if (rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) != null) {
                            sum += rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
                        }
                    } else {
                        if (rmmns.get(remindType).get(monitorPointType) != null) {
                            sum += rmmns.get(remindType).get(monitorPointType);
                        }
                    }

                }
            }
            setChildMunuAlarmData(childrenMenus, dataList, rmmns, 0);
            if (dataList.size() > 0) {
                List<Map<String, Object>> twolist = new ArrayList<>();
                //按父级菜单分组
                Map<String, List<Map<String, Object>>> collectmap = dataList.stream().filter(m -> m.get("parentcode") != null).collect(Collectors.groupingBy(m -> m.get("parentcode").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : collectmap.entrySet()) {
                    Map<String, Object> themap = new HashMap<>();
                    themap.put("sysmodel", entry.getKey());
                    themap.put("name", "-");
                    int n = 0;
                    List<Map<String, Object>> onelist = entry.getValue();
                    for (Map<String, Object> onemap : onelist) {
                        n += Integer.valueOf(onemap.get("count").toString());
                    }
                    themap.put("count", n);
                    themap.put("children", onelist);
                    twolist.add(themap);
                }
                resultMap.put("sum", sum);
                resultMap.put("datalist", twolist);
            } else {
                resultMap.put("sum", sum);
                resultMap.put("datalist", dataList);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/11 15:59
     * @Description: 获取需要报警的菜单枚举
     * @param:
     * @return:
     * @throws:
     */
    @SuppressWarnings("unchecked")
    private void getMonitorAlarmMenuEnums(List<Map<String, Object>> childrenByMenuId,
                                          List<CommonTypeEnum.MonitorManagementAlarmMenus> menuEnums,
                                          Set<String> monitorPointTypes, Map<Integer, Set<Integer>> rmsMapSet) {
        for (Map<String, Object> map : childrenByMenuId) {
            String menuCode = map.get("menucode").toString();
            CommonTypeEnum.MonitorManagementAlarmMenus menuEnum = CommonTypeEnum.MonitorManagementAlarmMenus.getCodeByString(menuCode);
            if (menuEnum != null) {
                menuEnums.add(menuEnum);
                Integer monitorPointType = menuEnum.getMonitorPointType();
                //当类型为废气烟气合并
                if (monitorPointType == CommonTypeEnum.MonitorPointTypeEnum.SmokeGasEnum.getCode()) {
                    monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "");
                    monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "");
                } else {
                    monitorPointTypes.add(monitorPointType + "");
                }
                Integer remindType = menuEnum.getRemindType();
                if (rmsMapSet.containsKey(remindType)) {
                    rmsMapSet.get(remindType).add(monitorPointType);
                } else {
                    Set<Integer> types = new HashSet<>();
                    //当类型为废气烟气合并
                    if (monitorPointType == CommonTypeEnum.MonitorPointTypeEnum.SmokeGasEnum.getCode()) {
                        types.add(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
                        types.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
                    } else {
                        types.add(monitorPointType);
                    }
                    rmsMapSet.put(remindType, types);
                }
            }
            Object children = map.get("datalistchildren");
            if (children != null) {
                childrenByMenuId = (List<Map<String, Object>>) children;
                getMonitorAlarmMenuEnums(childrenByMenuId, menuEnums, monitorPointTypes, rmsMapSet);
            }
        }
    }


    private int setChildMunuAlarmData(List<Map<String, Object>> childrenMenus, List<Map<String, Object>> dataList, Map<Integer, Map<Integer, Integer>> rmmns, int sum) {
        for (Map<String, Object> menu : childrenMenus) {
            String menuCode = menu.get("menucode").toString();
            String menuname = menu.get("menuname").toString();
            CommonTypeEnum.MonitorManagementAlarmMenus menuEnum = CommonTypeEnum.MonitorManagementAlarmMenus.getCodeByString(menuCode);
            if (menuEnum != null) {
                String parentcode = CommonTypeEnum.MonitorManagementAlarmMenus.getParentCodeByString(menuCode);
                Map<String, Object> map = new HashMap<>();
                map.put("sysmodel", menuCode);
                map.put("name", menuname);
                map.put("parentcode", parentcode);
                if (CommonTypeEnum.MonitorManagementAlarmMenus.getCodeByString(menuCode).getCode().contains("OverstandardGradedWarning")||
                        CommonTypeEnum.MonitorManagementAlarmMenus.getCodeByString(menuCode).getCode().contains("EarlystandardGradedWarning")) {
                    map.put("datatype","RealTime");
                }
                Integer remindType = menuEnum.getRemindType();
                Integer monitorPointType = menuEnum.getMonitorPointType();
                int count = 0;
                if (rmmns.get(remindType) != null) {
                    if (monitorPointType == CommonTypeEnum.MonitorPointTypeEnum.SmokeGasEnum.getCode()) {
                        if (rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) != null) {
                            count += rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
                        }
                        if (rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) != null) {
                            count += rmmns.get(remindType).get(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
                        }
                    } else {
                        if (rmmns.get(remindType).get(monitorPointType) != null) {
                            count += rmmns.get(remindType).get(monitorPointType);
                        }
                    }

                }
                map.put("count", count);
                sum += count;
                dataList.add(map);
            } else {    //子集不为空
                Object children = menu.get("datalistchildren");
                if (children != null) {
                    childrenMenus = (List<Map<String, Object>>) children;
                    if (childrenMenus.size() > 0) {
                        setChildMunuAlarmData(childrenMenus, dataList, rmmns, 0);
                        /*int format = setChildMunuAlarmData(childrenMenus, dataList, rmmns, 0);
                        map.put("count", format);
                        dataList.add(map);*/
                    }
                }
            }
        }
        return sum;
    }
}
