package com.tjpu.sp.controller.common.alarm;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.parkintegration.OnlineAlarmCountService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.OverAlarmEnum;

/**
 * @author: xsm
 * @date: 2021/01/20 0020 下午 4:47
 * @Description: 园区一体化在线报警数据统计控制层（环保、安全）
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("onlineAlarmCount")
public class OnlineAlarmCountController {

    @Autowired
    private OnlineAlarmCountService onlineAlarmCountService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private PollutantService pollutantService;




    /**
     * @author: xsm
     * @date: 2021/01/20 0020 下午 4:57
     * @Description:根据自定义参数获取当日所有监测类型的报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getTodayAllAlarmDataByParams", method = RequestMethod.POST)
    public Object getTodayAllAlarmDataByParams(@RequestJson(value = "customname", required = false) String customname,
                                               @RequestJson(value = "categorys", required = false)  List<String> categorys,
                                                @RequestJson(value = "monitorpointtypes") List<Integer> monitortypes,
                                                @RequestJson(value = "pagesize", required = false)  Integer  pagesize,
                                                @RequestJson(value = "pagenum", required = false)  Integer  pagenum,
                                                @RequestJson(value = "alarmtypes", required = false) List<Integer> alarmtypes
    ) throws ParseException {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<Map<String, Object>> allpoints = new ArrayList<>();
            List<Map<String, Object>> hb_allpoints = new ArrayList<>();
            List<Map<String, Object>> aq_allpoints = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            if (categorys==null||categorys.size()==0) {
                categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            }
            paramMap.put("customname",customname);
            paramMap.put("dgimns",dgimns);
            List<Integer> monitorpointtypes = new ArrayList<>();
            boolean isvideotype = false;
            if (monitortypes!=null&&monitortypes.size()>0){
             for (Integer type :monitortypes){
                if (type == CommonTypeEnum.MonitorPointTypeEnum.videoEnum.getCode()){
                    isvideotype = true;//有视频类型
                    break;
                }
                }
            }else{
                //无监测类型  查所有
                isvideotype = true;
            }
            paramMap.put("monitorpointtypes",monitortypes);
            hb_allpoints = deviceStatusService.getAllHBMonitorPointDataList(paramMap);
            allpoints.addAll(hb_allpoints);
            Date nowDay = new Date();
            Date startDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 00:00:00");
            Date endDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 23:59:59");
            if (alarmtypes == null || alarmtypes.size() == 0) {//默认全部报警类型：预警、报警、突变、异常、视频报警
                alarmtypes = Arrays.asList(CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()
                );
            }
            //获取报警数据
            List<Map> maps = new ArrayList<>();
            Map<String, Map<String, Object>> mnAndPointData = new HashMap<>();
            if (allpoints!=null&&allpoints.size()>0) {
                for (Map<String, Object> tempMn : allpoints) {
                    if (tempMn.get("dgimn") != null && dgimns.contains(tempMn.get("dgimn").toString())) {
                        mns.add(tempMn.get("dgimn").toString());
                        mnAndPointData.put(tempMn.get("dgimn").toString(), tempMn);
                        if (tempMn.get("monitorpointtype")!=null&&!"52".equals(tempMn.get("monitorpointtype").toString())) {
                           if (monitorpointtypes.contains(Integer.parseInt(tempMn.get("monitorpointtype").toString()))) {
                               monitorpointtypes.add(Integer.parseInt(tempMn.get("monitorpointtype").toString()));
                           }
                        }
                    }
                }
            }
            //获取监测污染物
        paramMap.put("monitorpointtypes", monitorpointtypes);
        List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = pollutantService.getPollutantsByPollutantType(paramMap);
        Map<String, Object> pollutants = new HashMap<>();
        Map<String, Object> codeandunit = new HashMap<>();
        Map<String, Object> pollutantdata = new HashMap<>();
        for (Map<String, Object> map:gasOutPutPollutantSetsByOutputIds){
            if (map.get("code")!=null){
                pollutants.put(map.get("code")+"_"+map.get("PollutantType"),map.get("name"));
                codeandunit.put(map.get("code")+"_"+map.get("PollutantType"),map.get("PollutantUnit"));
            }
        }
        pollutantdata.put("pollutants",pollutants);
        pollutantdata.put("codeandunit",codeandunit);
        //获取当日报警任务
        List<Integer> tasktypes= Arrays.asList(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode(),
                CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(),
                CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode(),
                CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()
        );
        paramMap.put("starttime",DataFormatUtil.getDateYMDHMS(startDate));
        paramMap.put("endtime",DataFormatUtil.getDateYMDHMS(endDate));
        paramMap.put("tasktypelist",tasktypes);
        List<Map<String,Object>> tasklist = onlineAlarmCountService.getTodayAlarmTasksByTaskTypes(paramMap);
        for (Integer remind :alarmtypes) {
            if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()||remind ==  CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                if (mns!=null&&mns.size()>0) {
                    maps.addAll(onlineAlarmCountService.countIntegrationChangeDataByParamMap(remind, mns, startDate, endDate,pollutantdata, mnAndPointData));
                }
                    }else {
                        if (mns!=null&&mns.size()>0) {
                            //环保报警数据 预警 超标 异常
                            maps.addAll(onlineAlarmCountService.countIntegrationAlarmDataByParamForApp(remind,mns, hb_allpoints, startDate, endDate, pollutantdata));
                        }
                     }
                }
                //添加任务状态
            //排序 报警时间 倒序
            if (maps.size()>0){
                List<Map> collect = maps.stream().sorted(Comparator.comparing((Map m) -> m.get("alarmlasttime").toString()).reversed()).collect(Collectors.toList());
                List<Map> subDataList;
                if (pagenum != null && pagesize!=null) {
                    subDataList = collect.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                } else {
                    subDataList = collect;
                }
                resultMap.put("datalist",subDataList);
                resultMap.put("total",collect.size());
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }else{
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
    }

    private Object getTaskStatusByParam(Integer remind,List<Map<String, Object>> tasklist, Map<String, Object> mappedResult) {
        Object taskstatus = "";
        //安全监测类型
        int tasktype ;
        List<Integer> monitorpointtypes = new ArrayList<>(CommonTypeEnum.getAllSecurityMonitorPointTypeList());
        if (tasklist!=null&&tasklist.size()>0){
            String id = "";
            String monitortype = "";
            if (mappedResult!=null&&mappedResult.size()>0&&mappedResult.get("monitorpointtype")!=null){
                Integer thetype = Integer.parseInt(mappedResult.get("monitorpointtype").toString());
                if (remind == OverAlarmEnum.getCode()) {
                    if (monitorpointtypes.contains(thetype)){//安全报警
                        id = mappedResult.get("monitorpointid")!=null?mappedResult.get("monitorpointid").toString():"";
                        if (!"".equals(id)&&!"".equals(monitortype)&&tasklist!=null&&tasklist.size()>0){
                            for (Map<String, Object> map:tasklist){
                                if (map.get("FK_Pollutionid")!=null&&id.equals(map.get("FK_Pollutionid").toString())){
                                    taskstatus = map.get("TaskStatus");
                                    break;
                                }
                            }
                        }
                    }else{//环保报警
                        id = mappedResult.get("pollutionid")!=null?mappedResult.get("pollutionid").toString():"";
                        if (!"".equals(id)&&!"".equals(monitortype)&&tasklist!=null&&tasklist.size()>0){
                            for (Map<String, Object> map:tasklist){
                                if (map.get("FK_Pollutionid")!=null&&id.equals(map.get("FK_Pollutionid").toString())){
                                    taskstatus = map.get("TaskStatus");
                                    break;
                                }
                            }
                        }
                    }
                } else if (remind == ExceptionAlarmEnum.getCode()) {
                    if (monitorpointtypes.contains(thetype)){//安全运维
                        id = mappedResult.get("monitorpointid")!=null?mappedResult.get("monitorpointid").toString():"";
                        if (!"".equals(id)&&!"".equals(monitortype)&&tasklist!=null&&tasklist.size()>0){
                            for (Map<String, Object> map:tasklist){
                                if (map.get("FK_Pollutionid")!=null&&id.equals(map.get("FK_Pollutionid").toString())){
                                    taskstatus = map.get("TaskStatus");
                                    break;
                                }
                            }
                        }
                    }else{//环保运维
                        id = mappedResult.get("pollutionid")!=null?mappedResult.get("pollutionid").toString():"";
                        monitortype = mappedResult.get("monitorpointtype")!=null?mappedResult.get("monitorpointtype").toString():"";
                        if (!"".equals(id)&&!"".equals(monitortype)&&tasklist!=null&&tasklist.size()>0){
                            for (Map<String, Object> map:tasklist){
                                if (map.get("FK_Pollutionid")!=null&&map.get("FK_MonitorPointTypeCode")!=null&&
                                        id.equals(map.get("FK_Pollutionid").toString())&&monitortype.equals(map.get("FK_MonitorPointTypeCode").toString())){
                                    taskstatus = map.get("TaskStatus");
                                    break;
                                }
                            }
                        }

                    }
                }

            }
        }
        return taskstatus;
    }

    /**
     * @author: xsm
     * @date: 2021/01/25 0025 上午 11:03
     * @Description:根据自定义参数统计当日所有监测类型的报警数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datatype:alarmtype  报警类型，monitortype  监测类型]
     */
    @RequestMapping(value = "counTodayAlarmDataNumByParams", method = RequestMethod.POST)
    public Object counTodayAlarmDataNumByParams(@RequestJson(value = "customname", required = false) String customname,
                                                @RequestJson(value = "datatype") String datatype
    ) {
        Map<String, Object> paramMap = new HashMap<>();
        List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
        List<Map<String, Object>> allpoints = new ArrayList<>();
        List<Map<String, Object>> hb_allpoints = new ArrayList<>();
        List<Map<String, Object>> aq_allpoints = new ArrayList<>();
        List<String> mns = new ArrayList<>();
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        paramMap.put("customname",customname);
        paramMap.put("dgimns",dgimns);
        hb_allpoints = deviceStatusService.getAllHBMonitorPointDataList(paramMap);
        allpoints.addAll(hb_allpoints);
        Date nowDay = new Date();
        Date startDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 00:00:00");
        Date endDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 23:59:59");
        //默认全部报警类型：预警、报警、突变、异常、视频报警
        List<Integer> alarmtypes = Arrays.asList(CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode(),
                    CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode(),
                    CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                    CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()
            );
        //获取报警数据
        List<String> hb_mns = new ArrayList<>();
        List<String> aq_mns = new ArrayList<>();
        List<Map> maps = new ArrayList<>();
        Map<String,String> mn_type = new HashMap<>();
        if (allpoints!=null&&allpoints.size()>0) {
            for (Map<String, Object> tempMn : allpoints) {
                if (tempMn.get("dgimn") != null && dgimns.contains(tempMn.get("dgimn").toString())) {
                    mns.add(tempMn.get("dgimn").toString());
                    if (tempMn.get("monitorpointtype") != null&&!"52".equals(tempMn.get("monitorpointtype").toString())){
                        mn_type.put(tempMn.get("dgimn").toString(),tempMn.get("monitorpointtype").toString());
                    }
                }
            }
        }
        //环保
        if (hb_allpoints!=null&&hb_allpoints.size()>0) {
            for (Map<String, Object> tempMn : hb_allpoints) {
                if (tempMn.get("dgimn") != null && dgimns.contains(tempMn.get("dgimn").toString())) {
                    hb_mns.add(tempMn.get("dgimn").toString());
                }
            }
        }
            paramMap.put("starttime",DataFormatUtil.getDateYMDHMS(startDate));
            paramMap.put("endtime",DataFormatUtil.getDateYMDHMS(endDate));
            paramMap.put("searchname",customname);
            if ("alarmtype".equals(datatype)){
                maps = onlineAlarmCountService.countAlarmDataByParamGroupByAlarmType(alarmtypes, mns,hb_mns,aq_mns,startDate, endDate,paramMap);
            }else if("monitortype".equals(datatype)){
                maps =  onlineAlarmCountService.countAlarmDataByParamGroupByMonitorType(alarmtypes, mns,hb_mns,aq_mns, startDate, endDate,paramMap,mn_type);
            }

        return AuthUtil.parseJsonKeyToLower("success", maps);

    }


}
