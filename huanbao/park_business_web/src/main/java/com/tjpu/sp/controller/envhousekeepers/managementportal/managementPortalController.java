package com.tjpu.sp.controller.envhousekeepers.managementportal;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.envhousekeepers.checkentinfo.CheckEntInfoService;
import com.tjpu.sp.service.envhousekeepers.managementportal.ManagementPortalService;
import com.tjpu.sp.service.envhousekeepers.problemconsult.EntProblemConsultRecordService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.notice.NoticeService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.pointofflinerecord.PointOffLineRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.OverAlarmEnum;


/**
 * @author: xsm
 * @date: 2021/09/07 0007 下午 13:35
 * @Description: 管委会门户控制层
 */
@RestController
@RequestMapping("managementPortal")
public class managementPortalController {

    @Autowired
    private ManagementPortalService managementPortalService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private EntProblemConsultRecordService entProblemConsultRecordService;
    @Autowired
    private CheckEntInfoService checkEntInfoService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private PointOffLineRecordService pointOffLineRecordService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;



    /**
     * @author: xsm
     * @date: 2021/09/07 0007 下午 2:23
     * @Description: 获取管委会监督检查巡查任务提醒(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countManagementCommitteePatrolDataNum", method = RequestMethod.POST)
    public Object countManagementCommitteePatrolDataNum() throws Exception {
        try {
            List<Map<String, Object>> maplist = managementPortalService.countManagementCommitteePatrolDataNum();
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/07 0007 下午 17:09
     * @Description: 获取年度问题企业排行(管委会端监督检查)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getProblemDataGroupByEntForYearRank", method = RequestMethod.POST)
    public Object getProblemDataGroupByEntForYearRank(@RequestJson(value = "year") String year
                                                      ) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("checkyear",year);
            List<Map<String, Object>> maplist = managementPortalService.getProblemDataGroupByEntForYearRank(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/08 0008 下午 16:34
     * @Description: 统计近一个月企业自查问题情况(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countLastMonthEntProblemDataSituation", method = RequestMethod.POST)
    public Object countLastMonthEntProblemDataSituation(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            List<Map<String, Object>> maplist = managementPortalService.countLastMonthEntProblemDataSituation(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/07 0007 下午 17:09
     * @Description: 获取自查企业整改信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEntSelfExaminationSituationByParam", method = RequestMethod.POST)
    public Object getEntSelfExaminationSituationByParam(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "fkproblemsourcecode", required = false) String fkproblemsourcecode,
                                                        @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                        @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                        @RequestJson(value = "pagesize", required = false) Integer pagesize) throws Exception {
        try {
            Map<String,Object> resultMap = new HashMap<>();
            Map<String,Object> param = new HashMap<>();
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            param.put("fkproblemsourcecode",fkproblemsourcecode);
            param.put("pollutionname",pollutionname);
            List<Map<String, Object>> datalist = managementPortalService.getEntSelfExaminationSituationByParam(param);
            int total = datalist.size();
            //分页
            if (pagesize != null && pagenum != null) {
                datalist = datalist.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/10 0010 上午 11:25
     * @Description: 统计该管委会首页所有未读提醒消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countManagementNoReadRemindDataByParam", method = RequestMethod.POST)
    public Object countManagementNoReadRemindDataByParam() throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            parammap.put("userid",userid);
            //企业检查问题提交
            parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckSubmitMessage.getCode());
            List<Map<String, Object>> qytj_list = checkEntInfoService.getEntCheckSubmitDataByParam(parammap);
            //企业咨询
            parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntProblemConsultMessage.getCode());
            List<Map<String, Object>> qyzx_list = entProblemConsultRecordService.getNoReadEntProblemConsultRecordByParam(parammap);
            //通知信息
            List<Map<String, Object>> tzxx_list = noticeService.getNoReadNoticeDataByParam(parammap);
            //点位离线
            parammap.put("isread","0");
            parammap.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
            List<Map<String, Object>> offlist = pointOffLineRecordService.getEntPointOffLineRecordsByParamMap(parammap);
            resultMap.put("qytj_num",qytj_list.size());
            resultMap.put("qyzx_num",qyzx_list.size());
            resultMap.put("tzxx_num",tzxx_list.size());
            resultMap.put("dwlx_num",offlist.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/31 0031 上午 9:22
     * @Description: 获取该管委会首页所有未读提醒消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getManagementNoReadRemindDataByParam", method = RequestMethod.POST)
    public Object getManagementNoReadRemindDataByParam(@RequestJson(value = "messagetype") String messagetype
                                                       ) throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            parammap.put("userid", userid);
            if (messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.EntCheckSubmitMessage.getCode())){
                parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckSubmitMessage.getCode());
                result = checkEntInfoService.getEntCheckSubmitDataByParam(parammap);
            }else if(messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.EntProblemConsultMessage.getCode())){
                parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntProblemConsultMessage.getCode());
                result = entProblemConsultRecordService.getNoReadEntProblemConsultRecordByParam(parammap);
            }else if(messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.NoticeMessage.getCode())){
                result = noticeService.getNoReadNoticeDataByParam(parammap);
            }else if(messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode())){
                //点位离线
                parammap.put("isread","0");
                parammap.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
                result = pointOffLineRecordService.getEntPointOffLineRecordsByParamMap(parammap);
            }
            if (result != null && result.size() > 0) {
                Comparator<Object> comparebyisread = Comparator.comparing(m -> ((Map) m).get("isread").toString());
                Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("UpdateTime").toString()).reversed();
                Comparator<Object> finalComparator = comparebyisread.thenComparing(comparebytime);
                List<Map<String, Object>> collect = result.stream().sorted(finalComparator).collect(Collectors.toList());
                resultMap.put("datalist", collect);
            } else {
                resultMap.put("datalist", result);
            }
            resultMap.put("messagetype", messagetype);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/14 0014 上午 9:52
     * @Description:根据用户数据权限获取管委会首页的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllMonitorTypesForManagementHomeMap", method = RequestMethod.POST)
    public Object getAllMonitorTypesForManagementHomeMap() throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            result = deviceStatusService.getAllMonitorTypesForManagementHomeMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/14 0014 上午 9:52
     * @Description:统计所有未完成检查问题和本月新增问题个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countNotCompleteCheckProblemNum", method = RequestMethod.POST)
    public Object countNotCompleteCheckProblemNum() throws Exception {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String ym = DataFormatUtil.getDateYM(new Date());
            paramMap.put("yearmonth", ym);
            result = managementPortalService.countNotCompleteCheckProblemNum(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/15 0015 上午 9:27
     * @Description: 统计某个监测类型当天超标、异常信息数量（一个点位一条）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countTodayAlarmDateByParam", method = RequestMethod.POST)
    public Object countTodayAlarmDateByParam(@RequestJson(value = "daytime") String daytime,
                                            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> overmap = new HashMap<>();
            Map<String, Object> exceptionmap = new HashMap<>();
            Set<String> mns = new HashSet<>();
            String mnCommon;
            List<Map<String, Object>> pointDataList;
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            paramMap.put("monitorpointtypecode", monitorpointtypecode);
            paramMap.put("userid", userid);
            //根据类型获取点位信息
            pointDataList = onlineService.getMNAndMonitorPointByParam(paramMap);
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("DGIMN") != null||pointData.get("dgimn") != null) {
                        mnCommon =  pointData.get("DGIMN")!=null?pointData.get("DGIMN").toString():(pointData.get("dgimn")!=null?pointData.get("dgimn").toString():"");
                        mns.add(mnCommon);
                    }
                }
            }
            //设置要查的报警类型
            List<Integer> remindcodes = new ArrayList<>();
            remindcodes.add(ExceptionAlarmEnum.getCode());//异常
            remindcodes.add(OverAlarmEnum.getCode());//超标
            for (Integer code : remindcodes) {
                List<String> alarmMns = managementPortalService.getOverOrExceptionAlarmMnsByParams(mns,daytime,code);   //该提醒类型报警mn
                int num = 0;
                for (String dgimn : mns) {
                    if (alarmMns.contains(dgimn)) {
                        num++;
                    }
                }
                if (code == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {  //异常
                    exceptionmap.put("alarmtype", "exception");
                    exceptionmap.put("num", num);
                    result.add(exceptionmap);
                } else if (code == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                    overmap.put("alarmtype", "over");
                    overmap.put("num", num);
                    result.add(overmap);
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
     * @date: 2021/09/15 0015 上午 9:27
     * @Description: 获取当天超标、异常信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getTodayAlarmDateByParam", method = RequestMethod.POST)
    public Object getTodayAlarmDateByParam(@RequestJson(value = "daytime") String daytime,
                                           @RequestJson(value = "remindtype") Integer remindtype,
                                           @RequestJson(value = "monitorpointtypecode", required = false) Integer monitorpointtypecode,
                                           @RequestJson(value = "pollutionname", required = false) String pollutionname
                                                        ) throws Exception {
        try {
            Map<String,Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndShorterName = new HashMap<>();
            Map<String, Object> codeAndName = new HashMap<>();
            String mnCommon;
            List<Map<String, Object>> pointDataList;
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            paramMap.put("monitorpointtypecode", monitorpointtypecode);
            paramMap.put("userid", userid);
            //根据类型获取点位信息
            pointDataList = onlineService.getMNAndMonitorPointByParam(paramMap);
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("DGIMN") != null||pointData.get("dgimn") != null) {
                        mnCommon =  pointData.get("DGIMN")!=null?pointData.get("DGIMN").toString():(pointData.get("dgimn")!=null?pointData.get("dgimn").toString():"");
                        mns.add(mnCommon);
                        mnAndPollutionId.put(mnCommon, pointData.get("pollutionid") != null ? pointData.get("pollutionid") : "");
                        mnAndPollutionName.put(mnCommon, pointData.get("pollutionname") != null ? pointData.get("pollutionname") : "");
                        mnAndShorterName.put(mnCommon, pointData.get("shortername") != null ? pointData.get("shortername") : "");
                        mnAndMonitorPointId.put(mnCommon, pointData.get("monitorpointid"));
                        mnAndMonitorPointName.put(mnCommon, pointData.get("monitorpointname"));
                    }
                }
            }
            //获取该类型污染物
            paramMap.put("pollutanttype", monitorpointtypecode);
            List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantData.size() > 0) {
                String key;
                for (Map<String, Object> pollutant : pollutantData) {
                    key = pollutant.get("code").toString();
                    if (pollutant.get("name") != null) {
                        codeAndName.put(key, pollutant.get("name"));
                    }
                }
            }
            Map<String,Object> mnAndPollutantDataList;
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", daytime);
                paramMap.put("endtime", daytime);
                paramMap.put("codeandname", codeAndName);
                mnAndPollutantDataList = managementPortalService.getOverOrExceptionDataByParam(remindtype,paramMap);
                if (mnAndPollutantDataList!=null){
                    Map<String,Object> onemap = new HashMap<>();
                    for(String onemn:mnAndPollutantDataList.keySet()){
                        onemap = (Map<String, Object>) mnAndPollutantDataList.get(onemn);
                        onemap.put("monitorpointid", mnAndMonitorPointId.get(onemn));
                        onemap.put("pollutionid", mnAndPollutionId.get(onemn));
                        onemap.put("pollutionname", mnAndPollutionName.get(onemn));
                        onemap.put("dgimn", onemn);
                        onemap.put("monitorpointname", mnAndShorterName.get(onemn)+"-"+mnAndMonitorPointName.get(onemn));
                    }
                }
            //resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", mnAndPollutantDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }





}
