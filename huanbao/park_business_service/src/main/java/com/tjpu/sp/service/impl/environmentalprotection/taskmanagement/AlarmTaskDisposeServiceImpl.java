package com.tjpu.sp.service.impl.environmentalprotection.taskmanagement;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.common.UserMapper;
import com.tjpu.sp.dao.common.pubcode.AlarmLevelMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.common.pubcode.PubCodeMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.dao.environmentalprotection.patroluserent.PatrolUserEntMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.TaskAlarmPollutantInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;
import com.tjpu.sp.dao.extand.TextMessageMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.TaskAlarmPollutantInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.model.extand.TextMessageVO;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorTaskStatusEnum.PendingEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class AlarmTaskDisposeServiceImpl implements AlarmTaskDisposeService {

    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private AlarmLevelMapper alarmLevelMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;
    @Autowired
    private TextMessageMapper textMessageMapper;
    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;
    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;
    @Autowired
    private DeviceStatusMapper deviceStatusMapper;
    @Autowired
    private VideoCameraMapper videoCameraMapper;
    @Autowired
    private PatrolUserEntMapper patrolUserEntMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;
    @Autowired
    private PubCodeMapper pubCodeMapper;
    @Autowired
    private TaskAlarmPollutantInfoMapper taskAlarmPollutantInfoMapper;


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    //超标数据表
    private final String minuteData_db = "MinuteData";
    //实时数据表
    private final String realData_db = "RealTimeData";
    //分钟数据表
    private final String overData_db = "OverData";
    //任务类型  超标报警
    private final String tasktype = "1";
    //异常数据表
    private final String exceptionData_db = "ExceptionData";



    private List<String> getRightList(String userid) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userid",userid);
        paramMap.put("moduletypes",Arrays.asList(
                CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode(),
                CommonTypeEnum.ModuleTypeEnum.DevEnum.getCode(),
                CommonTypeEnum.ModuleTypeEnum.TBEnum.getCode(),
                CommonTypeEnum.ModuleTypeEnum.RCFPEnum.getCode()
                )
                );
        List<String> rightList = checkProblemExpoundMapper.getUserModuleByParam(paramMap);
        return rightList;
    }

    /**
     * @author: mmt
     * @date: 2022/8/23
     * @Description:根据自定义参数获取报警、运维、突变、日常任务数统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    public Map<String, Object> countTaskByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> datas = new HashMap<>();
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("feedbackuserid", userId);
            List<String> rightList = getRightList(userId);



            //获取报警总条数
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())){
                paramMap.put("hasauthor", "1");
            }else {
                paramMap.put("hasauthor", "0");
            }
            paramMap.put("tasktype", AlarmTaskEnum.getCode());
            Long alarmCount = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoCountByParams(paramMap);
            //获取突变任务总条数
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.TBEnum.getCode())){
                paramMap.put("hasauthor", "1");
            }else {
                paramMap.put("hasauthor", "0");
            }
            paramMap.put("tasktype", ChangeAlarmTaskEnum.getCode());
            Long changeAlarmCount = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoCountByParams(paramMap);
            //获取运维任务总条数

            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())){
                paramMap.put("hasauthor", "1");
            }else {
                paramMap.put("hasauthor", "0");
            }
            paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
            Long devOpsTaskCount = alarmTaskDisposeManagementMapper.getAllDevOpsTaskInfoCountByParams(paramMap);

            //获取日常任务总条数
            if (!rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.RCFPEnum.getCode())){
                paramMap.put("isown", "1");
            }
            paramMap.put("tasktype", DailyEnum.getCode());
            Long daliyTaskCount = alarmTaskDisposeManagementMapper.getAllDaliyTaskByParamMap(paramMap);
            datas.put("alarmCount",alarmCount);
            datas.put("changeAlarmCount",changeAlarmCount);
            datas.put("devOpsTaskCount",devOpsTaskCount);
            datas.put("daliyTaskCount",daliyTaskCount);
            return datas;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getTaskDisposeDataListByParam(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTaskDisposeDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getLastFlowDataListByParam(Map<String, Object> dataMap) {
        return alarmTaskDisposeManagementMapper.getLastFlowDataListByParam(dataMap);
    }



    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:20
     * @Description: 获取报警任务处置管理信息（有分派按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getAllAlarmTaskDisposeListDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> datas = new HashMap<>();
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String theuserid = paramMap.get("userid").toString();
            String fktaasktype = paramMap.get("fktaasktype") == null ? "" : paramMap.get("fktaasktype").toString();
            if(StringUtils.isNotBlank(fktaasktype)){
                paramMap.put("tasktype", fktaasktype);
            }else{
                paramMap.put("tasktype", AlarmTaskEnum.getCode());
            }
            paramMap.put("feedbackuserid", theuserid);

            List<String> statuslist = new ArrayList<>();
            if (paramMap.get("statuslist") != null) {
                statuslist = (List<String>) paramMap.get("statuslist");
            }

            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
           /* paramMap.put("unallocatedtime", unallocatedtime);
            paramMap.put("uncompletetime", uncompletetime);*/
            //获取所有报警任务信息
            //获取总条数
            Long countall = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoCountByParams(paramMap);
            List<Map<String, Object>> listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(paramMap);
            //获取所有视频信息
            List<Map<String, Object>> videolist = videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
            Map<String, List<Map<String, Object>>> pointidAndrtsp = new HashMap<>();
            if (videolist != null && videolist.size() > 0) {
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videolist) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videolist) {
                                if (map2.get("monitorpointid") != null && (map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))) {
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp", map2.get("rtsp"));
                                    objmap.put("name", map2.get("name"));
                                    objmap.put("id", map2.get("pkid"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(), rtsplist);
                        }
                        //
                    }
                }
            }
            //获取分页后的任务ID
            List<String> taskids = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    map.put("rtsplist", pointidAndrtsp.get(map.get("FK_Pollutionid").toString()));
                    String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    taskids.add(map.get("PK_TaskID").toString());
                    String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                    if (map.get("overtimenum") != null) {
                        int overtimenum = Integer.parseInt(map.get("overtimenum").toString());
                        map.put("alarmovertime", countHourMinuteTime(overtimenum));
                    }
                    if ("0".equals(status)) {
                        if (map.get("unallocatednum") != null) {
                            int unallocatednum = Integer.parseInt(map.get("unallocatednum").toString());
                            if (unallocatednum > unallocatedtime) {
                                map.put("unallocatedflag", "1");
                                int tatalnum = unallocatednum - unallocatedtime;
                                String str = countHourMinuteTime(tatalnum);
                                map.put("overunallocatedtime", str);
                            } else {
                                map.put("unallocatedflag", "0");
                            }
                        }
                    } else {
                        map.put("unallocatedflag", "0");
                    }
                    map.put("lastalarmtime", map.get("TaskEndTime"));

                    //报警时段
                    StringBuffer alarmPeriod = new StringBuffer();
                    if (map.get("AlarmStartTime") != null) {
                        alarmPeriod.append(map.get("AlarmStartTime").toString().substring(5,16));
                        alarmPeriod.append("至");
                        if (map.get("lastalarmtime") != null) {
                            alarmPeriod.append(map.get("lastalarmtime").toString().substring(5,16));
                        }else{
                            alarmPeriod.append("今");
                        }
                    }

                    if (map.get("alarmovertime") != null) {
                        alarmPeriod.append("【");
                        alarmPeriod.append(map.get("alarmovertime"));
                        alarmPeriod.append("】");
                    }
                    map.put("alarmperiod",alarmPeriod.toString());
                }
                paramMap.put("taskids", taskids);
                //获取流程数据
                List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(paramMap);
                //获取报警污染物数据
                List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(paramMap);
                Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                //通过任务ID分组数据
                Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                for (Map<String, Object> map : listdata) {
                    String taskid = map.get("PK_TaskID").toString();
                    map.put("pollutantname",idandcodenames.get(taskid));
                    setTaskAllFlagData(theuserid, uncompletetime, map, lc_map.get(taskid));
                }
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
                datas.put("total", countall);
                datas.put("datalist", listdata);
                return datas;
            } else {
                datas.put("datalist", listdata);
                return datas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return datas;
    }

    /**
     * 组装任务ID 报警污染物数据
     */
    private Map<String,Object> getTaskPollutants(List<Map<String, Object>> wrw_listdata) {
        Map<String,Object> map = new HashMap<>();
        if (wrw_listdata!=null&&wrw_listdata.size()>0){
            //按任务ID 分组
            Map<String, List<Map<String, Object>>> collect = wrw_listdata.stream().filter(m -> m.get("taskid") != null).collect(Collectors.groupingBy(m -> m.get("taskid").toString()));
            for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                List<Map<String, Object>> polist = entry.getValue();
                String str = "";
                for (Map<String, Object> pomap :polist){
                    str = str+pomap.get("name")+"、";
                }
                if (!"".equals(str)){
                    str = str.substring(0,str.length()-1);
                }
                map.put(entry.getKey(),str);
            }
        }
        return map;
    }

    private void setTaskAllFlagData(String theuserid, Integer uncompletetime, Map<String, Object> result, List<Map<String, Object>> lc_listdata) {
        try{
            String status = result.get("TaskStatus").toString();
            String isfeedback = "0";//是否有反馈权限
            String isgenerate = "0";//是否
            String isscuser = "0";//是否审查人
            String uncompleteflag = "0";//是否超时未完成
            String zbrwtime = "";
            String user_name = "";
            String str = "";
            String assignmenttime = "";
            String completetime = "";
            String csisread ="";//抄送已读 APP端专用
            long min = 0;
            //获取处置人
            List<String> userids = new ArrayList<>();
            //待处理 处理中状态下有发出评论的用户ID
            Set<String> pl_userids = new HashSet<>();
            //判断是否是分派人 设置是否有结束任务的权限
            if ("0".equals(status)) {
                isgenerate = "1";
            }
            if (lc_listdata != null && lc_listdata.size() > 0) {
                //判断任务是否有转办  获取最后一次转办任务的转办时间  获取接收转办任务的人
                for(Map<String, Object> map : lc_listdata){
                    //获取分派时间 和分派时间到当前时间的分钟差值
                    if (map.get("CurrentTaskStatus")!=null&&CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())){
                        assignmenttime = map.get("TaskHandleTime")!=null?map.get("TaskHandleTime").toString():"";
                        if (!"".equals(assignmenttime)) {
                            Date now = new Date();
                            // 这样得到的差值是微秒级别
                            long diff = now.getTime() - DataFormatUtil.getDateYMDHMS(assignmenttime).getTime();
                            // 获取分
                            min = diff / (1000 * 60);
                        }
                    }
                    if (map.get("CurrentTaskStatus")!=null
                            &&(CommonTypeEnum.AlarmTaskStatusEnum.TransferEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())||
                                CommonTypeEnum.AlarmTaskStatusEnum.TransferTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())
                    )
                    ){
                        if ("".equals(zbrwtime)){
                            zbrwtime =  map.get("TaskHandleTime")!=null?map.get("TaskHandleTime").toString():"";
                        }else{
                            if(map.get("TaskHandleTime")!=null&&!zbrwtime.equals(map.get("TaskHandleTime").toString())){
                                //比较两个时间 留取最大（最近） 一条转办任务时间
                                if(compare(zbrwtime, map.get("TaskHandleTime").toString())){
                                    zbrwtime = map.get("TaskHandleTime").toString();
                                }
                            }
                        }
                    }
                    //获取完成时间
                    if (map.get("CurrentTaskStatus")!=null&&(CommonTypeEnum.AlarmTaskStatusEnum.NeglectTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())
                            ||CommonTypeEnum.AlarmTaskStatusEnum.ComplateTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString()))){
                        completetime = map.get("TaskHandleTime")!=null?map.get("TaskHandleTime").toString():"";
                    }

                    if (map.get("CurrentTaskStatus")!=null&&(CommonTypeEnum.AlarmTaskStatusEnum.ReviewedEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())
                            )){
                    if (map.get("FK_TaskHandleUserID")!=null&&theuserid.equals(map.get("FK_TaskHandleUserID").toString()) ) {
                        isscuser = "1";
                    }
                    }
                }
                for (Map<String, Object> map : lc_listdata) {
                    String CurrentTaskStatus = map.get("CurrentTaskStatus") != null ? map.get("CurrentTaskStatus").toString() : "";
                    String UserID = map.get("FK_TaskHandleUserID") != null ? map.get("FK_TaskHandleUserID").toString() : "";
                    //是否有反馈权限
                    if ("".equals(zbrwtime) && map.get("TaskComment") == null &&
                            CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName())) {
                        if (map.get("User_Name")!=null) {
                            userids.add(UserID);//任务未转办 添加待处置人ID
                            user_name = user_name + map.get("User_Name") + ",";
                            if (theuserid.equals(UserID)) {
                                isfeedback = "1";
                            }
                        }
                    } else if (!"".equals(zbrwtime)&&((compare(zbrwtime, map.get("TaskHandleTime").toString()))||zbrwtime.equals(map.get("TaskHandleTime").toString()))
                            && map.get("TaskComment") == null &&
                            CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.TransferTaskEnum.getName())) {
                        if (map.get("User_Name")!=null) {
                            userids.add(UserID);//任务有转办 添加转办人ID
                            user_name = user_name + map.get("User_Name") + ",";
                            if (theuserid.equals(UserID)) {
                                isfeedback = "1";
                            }
                        }
                    }
                    if (theuserid.equals(UserID) && CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName())) {
                        isgenerate = "1";
                    }
                    if ("1".equals(status) || "2".equals(status)) {//当任务处于 已分派 未完成的状态时 获取有评论的用户ID
                        if (map.get("TaskComment") != null && (CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName()) ||
                                CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName()))) {
                            pl_userids.add(UserID);
                        }
                    }
                    if (theuserid.equals(UserID) ) {
                        //抄送
                        if (CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.CarbonCopyTaskEnum.getName())){
                            csisread = "0";
                        }
                        //抄送已读
                        if (CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.ReadCopyTaskEnum.getName())){
                            csisread = "1";
                        }
                    }
                }
            }
            if ("1".equals(status) || "2".equals(status)) {//当任务处于 已分派 未完成的状态时 判断获取超标完成时限
                boolean hasflag = false;
                if (pl_userids != null && pl_userids.size() > 0) {//有评论信息
                    for (String id : pl_userids) {
                        if (userids.contains(id)) {//有评论信息
                            hasflag = true;
                            break;
                        }
                    }
                }
                if (hasflag == false) {
                    if (result.get("uncompletenum") != null) {
                        int uncompletenum = Integer.parseInt(result.get("uncompletenum").toString());
                        if (uncompletenum > uncompletetime) {
                            uncompleteflag = "1";
                            int tatalnum = uncompletenum - uncompletetime;
                            str = countHourMinuteTime(tatalnum);
                        } else {
                            uncompleteflag = "0";
                        }
                    }
                }
            }
            if (!"".equals(user_name)){
                user_name = user_name.substring(0, user_name.length() - 1);
            }
            result.put("csisread", csisread);
            result.put("isscuser", isscuser);
            result.put("assignmenttime", assignmenttime);
            result.put("completetime", completetime);
            result.put("uncompletenum", min);
            result.put("user_name", user_name);
            result.put("isfeedback", isfeedback);
            result.put("isgenerate", isgenerate);
            result.put("uncompleteflag", uncompleteflag);
            if (!"".equals(str)) {
                result.put("overuncompletetime", str);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 8:29
     * @Description: 获取污染源各类型报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getPollutionOutPutOverDataByParamMap(Map<String, Object> paramMap) {
        //判断是否有查询条件
        int monitorpointtype = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(), SmokeEnum.getCode(),
                WasteGasEnum.getCode(), RainEnum.getCode(), unOrganizationWasteGasEnum.getCode(), FactoryBoundarySmallStationEnum.getCode(), FactoryBoundaryStinkEnum.getCode(), StorageTankAreaEnum.getCode(), EnvironmentalDustEnum.getCode()
        );
        List<String> pollutionids = new ArrayList<>();
        //获取所点位名称和MN号
        if (monitortypes.contains(monitorpointtype)) {
            pollutionids.add(paramMap.get("pollutionid").toString());
            paramMap.put("pollutionids", pollutionids);
        }
        List<Map<String, Object>> allpoints = getAllPollutionOutputDgimnInfoByParam(paramMap);
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        //判断是否有查询条件
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Map<String, Object> resultmap = new HashMap<>();
        Set<String> mnlist = new HashSet<String>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> unitmap = new HashMap<>();
        for (Map<String, Object> obj : allpoints) {
            if (obj.get("dgimn") != null) {
                mnlist.add(obj.get("dgimn").toString());
            }
        }
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                unitmap.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
            }
        }
        //构建Mongdb查询条件
        Query query = new Query();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("PollutantCode").in(codelist));
        query.addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
        //总条数
        long totalCount = mongoTemplate.count(query, overData_db);
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        query.with(new Sort(Sort.Direction.ASC, "OverTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, overData_db);
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                Object standardmaxvalue = null;
                Object standardminvalue = null;
                Object alarmtype = null;
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                            alarmtype = objmap.get("AlarmType");
                            standardminvalue = objmap.get("StandardMinValue");
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            break;
                        }
                    }
                }
                for (Map<String, Object> objmap : allpoints) {
                    if (objmap.get("dgimn") != null && mn.equals(objmap.get("dgimn"))) {//当MN号相同时
                        Map<String, Object> result = new HashMap<>();
                        result.put("pollutionname", objmap.get("pollutionname"));
                        result.put("outputname", objmap.get("outputname"));
                        result.put("outputid", objmap.get("pk_id"));
                        if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getCode());
                            result.put("datatype", "实时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getCode());
                            result.put("datatype", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("OverTime")));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getCode());
                            result.put("datatype", "小时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("OverTime")));
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.DayDataEnum.getCode());
                            result.put("datatype", "日数据");
                            result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("OverTime")));
                        }
                        result.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantcode", pollutantcode);
                        result.put("pollutantunit", unitmap.get(pollutantcode));
                        result.put("standardmaxvalue", "");
                        if (alarmtype != null) {
                            if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype.toString())) {//上限报警
                                if (standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                }
                            } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype.toString())) {//下限报警
                                if (standardminvalue != null && !"".equals(standardminvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()));
                                }
                            } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(alarmtype.toString())) {//区间报警
                                if (standardminvalue != null && !"".equals(standardminvalue.toString()) && standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()) + "-" + DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                }
                            }
                        }
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        result.put("alarmlevelcode", document.get("AlarmLevel"));
                        if (document.getBoolean("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {//判断是否超标报警
                            //判断是否即超标又超限
                            if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                result.put("alarmlevel", (codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "、" : "") + "超标报警");
                                result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                            } else {
                                result.put("alarmlevel", "超标报警");
                                result.put("alarmlevelvalue", "");
                            }

                        } else {
                            if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                result.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString())  : "");
                                result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                            }
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        resultmap.put("datalist", resultlist);
        resultmap.put("total", totalCount);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取关联空气监测点的恶臭超标污染物浓度及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String,Object> getStinkOverAndAirWeatherData(String dgimn, String airmn, String pollutantcode, Date startDate, Date endDate,List<Map<String, Object>> pollutantValueScopeByParamMap,Map<String,Object> standmap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        aggregations.add(match(criteria));
        criteria1.and("RealDataList.PollutantCode").is(pollutantcode).and("RealDataList.IsOverStandard").is(true);
        aggregations.add(unwind("RealDataList"));
        aggregations.add(match(criteria1));
        aggregations.add(project("DataGatherCode", "MonitorTime").and("RealDataList.MonitorValue").as("MonitorValue")
                .and("RealDataList.PollutantCode").as("PollutantCode"));
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "RealTimeData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> overlist = new ArrayList<>();
        List<Map<String,Object>> weatherlist = new ArrayList<>();
        List<Map<String,Object>> weathernumlist = new ArrayList<>();
        if (resultDocument.size()>0){
            Date start =  resultDocument.get(0).getDate("MonitorTime");
            Date end =  resultDocument.get(resultDocument.size()-1).getDate("MonitorTime");
            //从枚举类中获取风速风向的编码
            List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                    , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
            List<AggregationOperation> aggregationstwo = new ArrayList<>();
            Criteria criteria2 = new Criteria();
            criteria2.and("DataGatherCode").is(airmn).and("MonitorTime").gte(start).lte(end).and("RealDataList.PollutantCode").in(pollutantcodes);
            aggregationstwo.add(match(criteria2));
            aggregationstwo.add(project("DataGatherCode", "MonitorTime","RealDataList"));
            aggregationstwo.add(sort(Sort.Direction.ASC, "MonitorTime"));
            Aggregation aggregation2 = newAggregation(aggregationstwo);
            AggregationResults<Document> resulttwo = mongoTemplate.aggregate(aggregation2, "RealTimeData", Document.class);
            List<Document> resultDocumenttwo = resulttwo.getMappedResults();
            for (Document document:resultDocument) {
                String monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                String value = "";
                String windspeed = "";
                String winddirection = "";
                if (pollutantcode.equals(document.getString("PollutantCode"))){
                    value = document.getString("MonitorValue");
                }
                if (resultDocumenttwo.size()>0) {
                    for (Document documenttwo : resultDocumenttwo) {
                        String monitortimetwo = DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime"));
                        if (!(documenttwo.getDate("MonitorTime")).after(document.getDate("MonitorTime"))) {
                            if (monitortime.equals(monitortimetwo)) {
                                List<Document> pollutants = (List<Document>) documenttwo.get("RealDataList");
                                if (pollutants.size() > 0) {
                                    for (Document podocument : pollutants) {
                                        if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(podocument.getString("PollutantCode"))) {
                                            winddirection = podocument.getString("MonitorValue");
                                        }
                                        if ((CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode()).equals(podocument.getString("PollutantCode"))) {
                                            windspeed = podocument.getString("MonitorValue");
                                        }
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (!"".equals(value)) {
                    Map<String,Object> valuemap = new HashMap<>();
                    valuemap.put("monitortime",monitortime);
                    valuemap.put("monitorvalue",value);
                    valuemap.put("standardmaxvalue",standmap.get("standardmaxvalue"));
                    valuemap.put("standardminvalue",standmap.get("standardminvalue"));
                    overlist.add(valuemap);
                    Map<String,Object> valuemap2 = new HashMap<>();
                    valuemap2.put("monitortime",monitortime);
                    if (!"".equals(winddirection)) {
                        valuemap2.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "code"));
                        valuemap2.put("winddirectionname", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "name"));
                        valuemap2.put("winddirectionvalue", winddirection);
                        valuemap2.put("windspeed", !"".equals(windspeed)?windspeed:null);
                    } else {
                        valuemap2.put("winddirectioncode", null);
                        valuemap2.put("winddirectionvalue", null);
                        valuemap2.put("winddirectionname", null);
                        valuemap2.put("windspeed", !"".equals(windspeed)?windspeed:null);
                    }
                    weatherlist.add(valuemap2);
                }
            }

        }
        if (weatherlist!=null&&weatherlist.size()>0) {
            for (Map<String, Object> map : pollutantValueScopeByParamMap) {
                String valuescope = map.get("valuescope") == null ? "" : map.get("valuescope").toString();
                String[] scopeArr = valuescope.split("-");
                //单个数字类型
                if (scopeArr.length == 1) {
                    String standard = scopeArr[0];
                    weatherlist.stream().filter(m -> m.get("winddirectionvalue") != null && standard.equals(m.get("winddirectionvalue").toString())).forEach(m -> {
                        m.put("valuescope", valuescope);
                    });
                }
                //范围类型
                else if (scopeArr.length == 2) {
                    Double standardfir = Double.valueOf(scopeArr[0]);
                    Double standardsec = Double.valueOf(scopeArr[1]);
                    weatherlist.stream().filter(m -> m.get("winddirectionvalue") != null && standardfir <= Double.valueOf(m.get("winddirectionvalue").toString()) && standardsec >= Double.valueOf(m.get("winddirectionvalue").toString())).forEach(m -> {
                        m.put("valuescope", valuescope);
                    });
                }
            }
            Map<String, List<Map<String, Object>>> collect = weatherlist.stream().filter(m -> m.get("valuescope") != null).collect(Collectors.groupingBy(m -> m.get("valuescope").toString()));
            String[] names = DataFormatUtil.directName;
            String[] codes = DataFormatUtil.directCode;
            for (String valuescope : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(valuescope);
                Map<String, List<Map<String, Object>>> collect2 = list.stream().filter(m -> m.get("winddirectionname") != null).collect(Collectors.groupingBy(m -> m.get("winddirectionname").toString()));
                List<Map<String, Object>> winddatalist = new ArrayList<>();
                Map<String, Object> onemap = new HashMap<>();
                for (String name : collect2.keySet()) {
                    String code = "";
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < names.length; i++) {
                        if (name.equals(names[i])) {
                            code = codes[i];
                            break;
                        }
                    }
                    map.put("winddirectioncode", code);
                    map.put("winddirectionname", name);
                    map.put("num", collect2.get(name).size());
                    winddatalist.add(map);
                }
                onemap.put("valuescope", valuescope);
                onemap.put("winddatalist", winddatalist);
                weathernumlist.add(onemap);
            }
        }
        result.put("overdata",overlist);
        result.put("weatherdata",weatherlist);
        if(weatherlist!=null&&weatherlist.size()>0){
            //排序 监测点类型 污染源名称 监测点名称 升序
            List<Map<String, Object>> col = weathernumlist.stream().sorted(Comparator.comparing((Map m) -> m.get("valuescope").toString())).collect(Collectors.toList());
            result.put("weathernumdata",col);
        }else{
            result.put("weathernumdata",weathernumlist);
        }
        return result;
    }

    @Override
    public Map<String, Object> getOnePointOverDataByParamMap(Map<String, Object> paramMap) {
        String dgimn = paramMap.get("dgimn") == null ? "" : paramMap.get("dgimn").toString();
        //String pointname = paramMap.get("pointname") == null ? "" : paramMap.get("pointname").toString();
        String monitorpointtype = paramMap.get("monitorpointtype") == null ? "" : paramMap.get("monitorpointtype").toString();
        String starttime = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        String datatype = paramMap.get("datatype") == null ? "" : paramMap.get("datatype").toString();
        String datatypename = "";
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        String collection = "";
        String timeliststr = "";
        String valuestr = "";
        String convertstr = "";
        if ("1".equals(datatype)) {
            //实时在线
            collection = "RealTimeData";
            timeliststr = "RealDataList";
            datatypename = "实时数据";
            valuestr = "MonitorValue";
            convertstr = "ConvertConcentration";

        } else if("2".equals(datatype)){
            //分钟在线
            collection = "MinuteData";
            timeliststr = "MinuteDataList";
            datatypename = "分钟数据";
            valuestr = "AvgStrength";
            convertstr = "AvgConvertStrength";
        }else if("3".equals(datatype)){
            //小时在线
            collection = "HourData";
            timeliststr = "HourDataList";
            datatypename = "小时数据";
            valuestr = "AvgStrength";
            convertstr = "AvgConvertStrength";
        }else if("4".equals(datatype)){
            //日在线
            collection = "DayData";
            timeliststr = "DayDataList";
            datatypename = "日数据";
            valuestr = "AvgStrength";
            convertstr = "AvgConvertStrength";
        }
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        //废气、烟气查污染物数据判断是否有折算
        Map<String, Object> codeandflag = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        //判断是否有查询条件
        Map<String, Object> resultmap = new HashMap<>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> unitmap = new HashMap<>();
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                unitmap.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
                if (obj.get("IsHasConvertData")!=null&&!"".equals(obj.get("IsHasConvertData").toString())&&Integer.parseInt(obj.get("IsHasConvertData").toString())==1) {
                    codeandflag.put(obj.get("code").toString(), obj.get("IsHasConvertData"));
                }
            }
        }
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        //mongodb or查询  查超标或超限数据
        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        criterialist.add(Criteria.where(timeliststr + ".IsOver").gt(0));
        criterialist.add(Criteria.where(timeliststr + ".IsOverStandard").is(true));
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
        //criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate).andOperator(timecriteria);
        //构建Mongdb查询条件
        /*Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where(timeliststr+".PollutantCode").in(codelist).andOperator(timecriteria));*/
        //总条数
        //long totalCount = mongoTemplate.count(query, collection);
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria2 = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        criteria2.and(timeliststr+".PollutantCode").in(codelist).andOperator(timecriteria);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.unwind(timeliststr));
        operations.add(Aggregation.match(criteria2));
        if (monitorpointtype.equals(SmokeEnum.getCode()+"")){
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime",timeliststr+".IsOverStandard").and(timeliststr+".IsOver").as("AlarmLevel").and(timeliststr+".PollutantCode").
                    as("PollutantCode").and(timeliststr+"."+valuestr).as("MonitorValue").and(timeliststr+"."+convertstr).as("convertstr"));
        }else{
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime",timeliststr+".IsOverStandard").and(timeliststr+".IsOver").as("AlarmLevel").and(timeliststr+".PollutantCode").as("PollutantCode").and(timeliststr+"."+valuestr).as("MonitorValue"));
        }

        operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
        Aggregation aggregationList_one = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults_one = mongoTemplate.aggregate(aggregationList_one, collection, Document.class);
        long totalCount = pageResults_one.getMappedResults().size();
       if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        List<Map<String,Object>> resultlist = new ArrayList<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                Object standardmaxvalue = null;
                Object standardminvalue = null;
                Object alarmtype = null;
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && document.getString("PollutantCode").equals(objmap.get("Code").toString())) {
                            alarmtype = objmap.get("AlarmType");
                            standardminvalue = objmap.get("StandardMinValue");
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            break;
                        }
                    }
                }
                Map<String, Object> result = new HashMap<>();

                result.put("outputname", paramMap.get("pointname"));
                result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getCode());
                result.put("datatype", datatypename);
                result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                //result.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
                result.put("pollutantname", codemap.get(document.getString("PollutantCode")));
                result.put("pollutantcode", document.getString("PollutantCode"));
                result.put("pollutantunit", unitmap.get(document.getString("PollutantCode")));
                result.put("standardmaxvalue", "");
                if (alarmtype != null) {
                    if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype.toString())) {//上限报警
                        if (standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                            result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                        }
                    } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype.toString())) {//下限报警
                        if (standardminvalue != null && !"".equals(standardminvalue.toString())) {
                            result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()));
                        }
                    } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(alarmtype.toString())) {//区间报警
                        if (standardminvalue != null && !"".equals(standardminvalue.toString()) && standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                            result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()) + "-" + DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                        }
                    }
                }
                if (monitorpointtype.equals(SmokeEnum.getCode()+"")&&codeandflag.get(document.getString("PollutantCode"))!=null){
                    result.put("monitorvalue", document.get("convertstr"));
                }else {
                    result.put("monitorvalue", document.get("MonitorValue"));
                }
                result.put("alarmlevelcode", document.get("AlarmLevel"));
                if (document.getBoolean("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {//判断是否超标报警
                    //判断是否即超标又超限
                    if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                        result.put("alarmlevel", (codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "超限、" : "") + "超标报警");
                        result.put("alarmlevelvalue", getAlarmLevelValue(mn, document.getString("PollutantCode"), document.get("AlarmLevel"), outputlist));
                    } else {
                        result.put("alarmlevel", "超标报警");
                        result.put("alarmlevelvalue", "");
                    }

                } else {
                    if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                        result.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "超限" : "");
                        result.put("alarmlevelvalue", getAlarmLevelValue(mn, document.getString("PollutantCode"), document.get("AlarmLevel"), outputlist));
                    }
                }
                resultlist.add(result);
            }
        }
        resultmap.put("datalist", resultlist);
        resultmap.put("total", totalCount);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 1:15
     * @Description: 根据监测点类型和自定义参数获取各企业下各排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getAllPollutionOutputDgimnInfoByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> outputs = new ArrayList<>();
        int type = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//废气
            outputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//烟气
            outputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {//废水
            paramMap.put("outputtype", "water");
            outputs = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//雨水
            paramMap.put("outputtype", "rain");
            outputs = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
            outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
            outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
            outputs = airMonitorStationMapper.getALLAirStationInfoByParamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {//TVOC
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质
            outputs = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
        }
        return outputs;
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 1:15
     * @Description: 根据监测点类型和自定义参数获取各企业下各排口监测的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */

    private List<Map<String, Object>> getAllOutputDgimnAndPollutantInfosByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> outputs = new ArrayList<>();
        if (paramMap.get("monitorpointtype") != null && !"".equals(paramMap.get("monitorpointtype").toString())) {
            int type = Integer.parseInt(paramMap.get("monitorpointtype").toString());
            if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//废气
                outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//烟气
                outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {//废水
                paramMap.put("outputtype", 1);
                outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//雨水
                paramMap.put("outputtype", 3);
                outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
                outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
                outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
                outputs = airMonitorStationMapper.getAirMonitorStationDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {//TVOC
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质
                outputs = waterStationMapper.getWaterStationDgimnAndPollutantInfosByParam(paramMap);
            }
        }
        return outputs;
    }


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 10:00
     * @Description: 根据报警级别获取对应级别的标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Object getAlarmLevelValue(String mn, String pollutantcode, Object alarmLevel, List<Map<String, Object>> outputlist) {
        Object alarmLevelvalue = "";
        for (Map<String, Object> objmap : outputlist) {
            if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {//当污染物相同时
                    if (objmap.get("FK_AlarmLevelCode") != null && (alarmLevel.toString()).equals(objmap.get("FK_AlarmLevelCode").toString())) {
                        alarmLevelvalue = objmap.get("ConcenAlarmMaxValue");
                    }
                }
            }
        }
        return alarmLevelvalue;
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 10:59
     * @Description: 获取处置人下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDisposePersonSelectData(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getDisposePersonSelectData(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 11:29
     * @Description: 添加任务信息(分派任务)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void saveTaskInfo(String userid, String username, Map<String, Object> formdata, Integer tasktype) {

        try {
            String taskid = formdata.get("pk_taskid").toString();
            List<String> userids = (List<String>) formdata.get("userids");
            List<String> cs_userids = formdata.get("cs_userids") != null ? (List<String>) formdata.get("cs_userids") : new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            //添加任务处置信息
            //根据任务ID获取任务信息
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeManagementMapper.selectByPrimaryKey(taskid);
            //根据任务类型判断是报警任务还是日常任务
            if (tasktype == AlarmTaskEnum.getCode()) {//报警任务
                alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());//任务状态
                alarmTaskDisposeManagement.setTaskremark(formdata.get("taskremark") != null ? formdata.get("taskremark").toString() : "");//任务说明
                alarmTaskDisposeManagement.setUpdatetime(new Date());//更新时间
                alarmTaskDisposeManagement.setUpdateuser(username);//更新人
            } else if (tasktype == CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode()) {//日常任务
                alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());//任务状态
                alarmTaskDisposeManagement.setTaskrealstartdate(DataFormatUtil.getDateYMDHMS(new Date()));
                alarmTaskDisposeManagement.setUpdatetime(new Date());//更新时间
                alarmTaskDisposeManagement.setUpdateuser(username);//更新人
            }else if (tasktype == CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()) {//浓度突变
                alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());//任务状态
                alarmTaskDisposeManagement.setUpdatetime(new Date());//更新时间
                alarmTaskDisposeManagement.setUpdateuser(username);//更新人
                alarmTaskDisposeManagement.setTaskremark(formdata.get("taskremark") != null ? formdata.get("taskremark").toString() : "");//任务说明
            }else if (tasktype == CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode()
                    ||tasktype == CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode()
                    ||tasktype == CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()) {//安全报警任务
                alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());//任务状态
                alarmTaskDisposeManagement.setUpdatetime(new Date());//更新时间
                alarmTaskDisposeManagement.setUpdateuser(username);//更新人
                alarmTaskDisposeManagement.setTaskremark(formdata.get("taskremark") != null ? formdata.get("taskremark").toString() : "");//任务说明
            }
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
            //任务分派人
            //添加任务处置记录信息
            Calendar calendar = Calendar.getInstance();
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());//主键ID
            obj.setFkTaskid(taskid);//任务ID
            obj.setFkTaskhandleuserid(userid);//分派人用户ID
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName().toString());//任务状态
            obj.setFkTasktype(tasktype.toString());//任务类型
            obj.setTaskhandletime(calendar.getTime());//任务分派时间
            if (tasktype == AlarmTaskEnum.getCode() || tasktype == ChangeAlarmTaskEnum.getCode() || tasktype == SupervisoryControlEnum.getCode()) {//报警任务
                obj.setTaskcomment(formdata.get("taskremark") != null ? formdata.get("taskremark").toString() : "");
            }
            taskFlowRecordInfoMapper.insert(obj);
            //判断是否存在抄送人
            if (cs_userids != null && cs_userids.size() > 0) {
                calendar.add(Calendar.SECOND, 1);
                for (String str : cs_userids) {
                    //添加任务处置记录信息
                    TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
                    taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());//主键ID
                    taskFlowRecordInfo.setFkTaskid(taskid);//任务ID
                    taskFlowRecordInfo.setFkTaskhandleuserid(str.toString());//被抄送该任务的抄送人人ID
                    taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.CarbonCopyTaskEnum.getName().toString());//任务状态
                    taskFlowRecordInfo.setFkTasktype(tasktype.toString());//任务类型
                    taskFlowRecordInfo.setTaskhandletime(calendar.getTime());//被分派该任务的时间
                    taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
                }
            }
            //在当前时间的基础上添加一秒
            calendar.add(Calendar.SECOND, 1);
            // 处置人信息
            for (String str : userids) {
                //添加任务处置记录信息
                TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
                taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());//主键ID
                taskFlowRecordInfo.setFkTaskid(taskid);//任务ID
                taskFlowRecordInfo.setFkTaskhandleuserid(str.toString());//被分派该任务的处置人ID
                taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName().toString());//任务状态
                taskFlowRecordInfo.setFkTasktype(tasktype.toString());//任务类型
                taskFlowRecordInfo.setTaskhandletime(calendar.getTime());//被分派该任务的时间
                taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 2:17
     * @Description: 根据主键ID获取报警任务处置详情和任务流程(报警任务 、 运维任务)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public Map<String, Object> getAlarmTaskDisposeManagementDetailByID(String id, String userId, String currentuser) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("taskid", id);
        param.put("userid", userId);
        //获取报警任务处置详情信息
        Map<String, Object> obj = alarmTaskDisposeManagementMapper.getAlarmTaskDisposeManagementDetailByID(param);
        List<String> taskids = new ArrayList<>();
        taskids.add(id);
        param.put("taskids", taskids);
        //获取报警污染物数据
        List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(param);
        result.put("pollutantdata", wrw_listdata);
        //组装污染物数据
        Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
        //获取流程数据
        List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(param);
        setTaskDisposerForHomePage(obj, lc_listdata);
        String problem = "";
        if (obj != null) {
            String problemtype = obj.get("FK_ProblemType") != null ? obj.get("FK_ProblemType").toString() : "";
            if (problemtype != null && !"".equals(problemtype)) {
                String[] str = problemtype.split(",");
                List<Map<String, Object>> problemtypelist = alarmTaskDisposeManagementMapper.getProblemTypeSelectData(null);
                if (str.length > 0) {
                    for (String code : str) {
                        for (Map<String, Object> problemmap : problemtypelist) {
                            if (code.equals(problemmap.get("Code").toString())) {
                                problem = problem + problemmap.get("Name") + ",";
                            }
                        }
                    }
                }
            }
            if (!"".equals(problem)) {
                problem = problem.substring(0, problem.length() - 1);
            }
            String cs_time = "";
            String cs_user = "";
            String iscomment = "0";
            String sc_user = "";
            String sc_time = "";
            List<String> sc_userids= new ArrayList<>();
            String bj_time = "";
            //判断任务是否有添加审查人员  获取最后一次添加审查人员的时间
            for(Map<String, Object> map : lc_listdata){
                if (map.get("CurrentTaskStatus")!=null&&CommonTypeEnum.AlarmTaskStatusEnum.ReviewedEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())){
                    if ("".equals(sc_time)){
                        sc_time =  map.get("TaskHandleTime")!=null?map.get("TaskHandleTime").toString():"";
                    }else{
                        if(map.get("TaskHandleTime")!=null&&!sc_time.equals(map.get("TaskHandleTime").toString())){
                            //比较两个时间 留取最大（最近） 一条转办任务时间
                            try {
                                if(compare(sc_time, map.get("TaskHandleTime").toString())){
                                    sc_time = map.get("TaskHandleTime").toString();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            for(Map<String, Object> map : lc_listdata){
                if (map.get("CurrentTaskStatus")!=null&&(CommonTypeEnum.AlarmTaskStatusEnum.CarbonCopyTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())||
                        CommonTypeEnum.AlarmTaskStatusEnum.ReadCopyTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString()))){
                    if ("".equals(cs_time)){
                        cs_time = map.get("TaskHandleTime").toString();
                    }
                    cs_user = cs_user+map.get("User_Name")+"、";
                }
                if (userId.equals(map.get("FK_TaskHandleUserID").toString())){
                    iscomment = "1";
                }
                if (!"".equals(sc_time)) {//有审查时间
                    if (map.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.ReviewedEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())
                    )&&sc_time.equals(map.get("TaskHandleTime").toString())) {
                        sc_user = sc_user + map.get("User_Name") + "、";
                        sc_userids.add(map.get("FK_TaskHandleUserID").toString());
                    }
                    if (map.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.ConfirmTaskEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())
                    )) {
                        bj_time = map.get("TaskHandleTime").toString();
                    }
                }
            }
            if (!"".equals(cs_user)){
                cs_user = cs_user.substring(0,cs_user.length()-1);
            }
            if (!"".equals(sc_user)){
                sc_user = sc_user.substring(0,sc_user.length()-1);
            }

            result.put("cstasktime", cs_time);
            result.put("csuser_name", cs_user);
            result.put("iscomment", iscomment);
            result.put("bjtasktime", bj_time);
            result.put("scuser_name", sc_user);
            result.put("csuserids", sc_userids);
            result.put("username", obj.get("user_name"));
            if (obj.get("overtimenum") != null) {
                int overtimenum = Integer.parseInt(obj.get("overtimenum").toString());
                result.put("alarmovertime", countHourMinuteTime(overtimenum));
            }else{
                result.put("alarmovertime", "-");
            }
            result.put("cstasktime", obj.get("cstasktime"));
            result.put("taskremark", obj.get("TaskRemark"));
            result.put("problemtypecode", obj.get("FK_ProblemType"));
            result.put("pollutantname", idandcodenames.get(id));
            result.put("problemtype", problem);
            result.put("overtime", obj.get("overtime"));
            result.put("createtime", obj.get("TaskCreateTime"));
            result.put("taskstarttime", obj.get("AlarmStartTime"));
            result.put("taskendtime", obj.get("TaskEndTime"));
            result.put("feedbackresults", obj.get("FeedbackResults"));
            result.put("fileid", obj.get("FileID"));

        }

        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 4:23
     * @Description: 根据主键ID获取报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public AlarmTaskDisposeManagementVO selectByPrimaryKey(String pk_taskid) {
        return alarmTaskDisposeManagementMapper.selectByPrimaryKey(pk_taskid);
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 4:26
     * @Description: 修改报警任务信息为已完成状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void updateAlarmTaskInfo(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement) {

        try {
            int oldstatus = alarmTaskDisposeManagement.getTaskstatus();
            alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode());
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
           //当任务未分派  直接结束时 默认结束人为任务处理人  添加一条待处理流程记录
            Calendar calendar = Calendar.getInstance();
            if (oldstatus==CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getCode()){
                //添加任务处置记录信息
                TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
                obj.setPkId(UUID.randomUUID().toString());
                obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
                obj.setFkTaskhandleuserid(userId);
                obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName().toString());
                obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
                obj.setTaskhandletime(calendar.getTime());
                taskFlowRecordInfoMapper.insert(obj);
            }
            //在当前时间的基础上添加一秒
            calendar.add(Calendar.SECOND, 1);
            //添加任务处置记录信息
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
            obj.setFkTaskhandleuserid(userId);
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.FeedbackEnum.getName().toString());
            obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj.setTaskhandletime(calendar.getTime());
            obj.setTaskcomment(alarmTaskDisposeManagement.getFeedbackresults());
            taskFlowRecordInfoMapper.insert(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/03 003 下午 4:26
     * @Description: 修改报警任务反馈信息，且不改变任务状态（待抄送人确定）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void updateTaskCarbonCopyStatus(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement) {
        try {
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
            //添加任务处置记录信息
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
            obj.setFkTaskhandleuserid(userId);
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.FeedbackEnum.getName().toString());
            obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj.setTaskhandletime(new Date());
            obj.setTaskcomment(alarmTaskDisposeManagement.getFeedbackresults());
            taskFlowRecordInfoMapper.insert(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/03 003 下午 4:26
     * @Description: 抄送人确认报警任务为完成状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void addCarbonCopyUserCompleteTaskInfo(String userId,Object comment, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement) {
        alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
        //添加任务处置记录信息
        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
        obj.setPkId(UUID.randomUUID().toString());
        obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
        obj.setFkTaskhandleuserid(userId);
        obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.ConfirmTaskEnum.getName().toString());
        if (comment!=null){
            obj.setTaskcomment(comment.toString());
        }
        obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
        obj.setTaskhandletime(new Date());
        taskFlowRecordInfoMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2021/12/03 003 下午 4:26
     * @Description: 抄送人打回任务为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void addCarbonCopyUserRepulseTaskInfo(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement, JSONObject jsonObject) {
        alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
        //添加任务处置记录信息
        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
        obj.setPkId(UUID.randomUUID().toString());
        obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
        obj.setFkTaskhandleuserid(userId);
        obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.TaskRepulseEnum.getName().toString());
        obj.setTaskcomment(jsonObject.get("comment")!=null?jsonObject.getString("comment"):"");
        obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
        obj.setTaskhandletime(new Date());
        taskFlowRecordInfoMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2021/12/08 0008 下午 2:25
     * @Description: 新增超标超限任务（新）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addNewAlarmTaskInfo(AlarmTaskDisposeManagementVO obj, List<String> overcodes, JSONObject messageobject) {
        //根据MN号 监测时间查询是否有该点位的报警信息
        messageobject.put("TaskID", null);
        if (obj.getFkPollutionid() != null && !"".equals(obj.getFkPollutionid())) {//判断查询数据是否为空
            String pkid = UUID.randomUUID().toString();
            obj.setPkTaskid(pkid);
            obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
            obj.setRecoverystatus((short) 0);//默认新增任务的设备恢复状态为未恢复 0；
            alarmTaskDisposeManagementMapper.insert(obj);
            messageobject.put("TaskID", pkid);
            //添加任务成功后添加该任务的报警污染物信息
            if (overcodes != null && overcodes.size() > 0) {
                for (String code : overcodes) {
                    TaskAlarmPollutantInfoVO objvo = new TaskAlarmPollutantInfoVO();
                    objvo.setPkId(UUID.randomUUID().toString());
                    objvo.setFkTaskid(pkid);
                    objvo.setFkPolluantcode(code);
                    objvo.setFkTasktype(obj.getFkTasktype());
                    objvo.setUpdatetime(new Date());
                    taskAlarmPollutantInfoMapper.insertSelective(objvo);
                }
            }
        }
    }


    /**
     * 自动分派超标超限报警任务（新）
     * */
    @Override
    public void automaticDispatchAlarmTask(String taskid,List<String> userids) {
        //修改任务状态为待处理
        AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeManagementMapper.selectByPrimaryKey(taskid);
        alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());
        alarmTaskDisposeManagement.setUpdatetime(new Date());
        alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
        //获取配置的 分派人
        String assignuseraccount = DataFormatUtil.parseProperties("assignuseraccount");
        String userid = "";
        if (!"".equals(assignuseraccount)) {
            Map<String, Object> uservo = userMapper.getUserInfoByUserAccount(assignuseraccount);
            if (uservo != null) {
                userid =  uservo.get("id") != null ? uservo.get("id").toString() : "";
            }
        }
        Calendar calendar = Calendar.getInstance();
        //有巡查人员时  存一条分配记录
        //添加分派任务  处理人信息
        TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
        taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());
        taskFlowRecordInfo.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
        taskFlowRecordInfo.setFkTaskhandleuserid(userid);
        taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName().toString());
        taskFlowRecordInfo.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
        taskFlowRecordInfo.setTaskhandletime(calendar.getTime());
        taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
        //Calendar calendar = Calendar.getInstance();
        //在当前时间的基础上添加一秒
        calendar.add(Calendar.SECOND, 1);
        //添加任务处置记录信息
        List<TaskFlowRecordInfoVO> listobj = new ArrayList<>();
        for (String id:userids) {
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
            obj.setFkTaskhandleuserid(id);
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName().toString());
            obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj.setTaskhandletime(calendar.getTime());
            listobj.add(obj);
        }
        if (listobj.size()>0) {
            taskFlowRecordInfoMapper.batchInsert(listobj);
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 7:02
     * @Description: 获取报警任务处置管理表头信息(导出)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForAlarmTask() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"监测点名称",  "超标污染物","超标等级","超标开始时间", "超标结束时间","连续报警时长","任务生成时间", "任务派发时间", "处置完成时间", "处置人","恢复状态", "状态"};
        String[] titlefiled = new String[]{"monitorpointname", "pollutantname","overlevelname","alarmstarttime", "taskendtime", "alarmovertime","taskcreatetime", "assignmenttime","completetime", "user_name","recoverystatusname", "taskstatuname"};
        for (int i = 0; i < titlefiled.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", titlefiled[i]);
            map.put("label", titlename[i]);
            map.put("align", "center");
            tableTitleData.add(map);
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 9:48
     * @Description: 获取问题类型下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getProblemTypeSelectData(String monitorpointtypecode) {
        return alarmTaskDisposeManagementMapper.getProblemTypeSelectData(monitorpointtypecode);
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 11:21
     * @Description: 暂存报警任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void temporaryTaskInfo(AlarmTaskDisposeManagementVO alarmTaskDisposeManagement) {
        try {
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 1:36
     * @Description: 根据时间范围获取该时间段内报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countAlarmTaskNumDataByParams(Map<String, Object> paramMap) {
        try {
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            //获取该段时间内的的所有年月

            List<Map<String, Object>> resultlist = new ArrayList<>();
            List<String> datelist = DataFormatUtil.getMonthBetween(starttime, endtime);
            paramMap.put("tasktype", AlarmTaskEnum.getCode());
            //根据时间范围获取该时段的报警任务处置信息
            List<Map<String, Object>> datalist = alarmTaskDisposeManagementMapper.getAlarmTaskDisposeManagementByParamMap(paramMap);
            if (datalist != null && datalist.size() > 0) {
                for (String thedate : datelist) {
                    int num = 0;
                    int total = 0;
                    for (Map<String, Object> map : datalist) {
                        if (map.get("TaskCreateTime") != null && thedate.equals(map.get("TaskCreateTime").toString())) {//同一个月时
                            total += 1;
                            if (map.get("TaskStatus") != null && "4".equals(map.get("TaskStatus").toString())) {//已完成任务个数
                                num += 1;
                            }
                        }
                    }
                    Map<String, Object> objmap = new HashMap<>();
                    objmap.put("yearmonth", thedate);
                    if (total > 0) {
                        objmap.put("num", num);
                        objmap.put("total", total);
                    } else {
                        objmap.put("num", "");
                        objmap.put("total", "");
                    }
                    resultlist.add(objmap);
                }
            }
            return resultlist;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 2:49
     * @Description: 根据时间范围获取该时间段内按企业进行分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countAlarmTaskNumGroupByPollution(Map<String, Object> paramMap) {
        //根据时间范围获取该时段的报警任务处置信息
        paramMap.put("tasktype", AlarmTaskEnum.getCode());
        List<Map<String, Object>> datalist = alarmTaskDisposeManagementMapper.getAlarmTaskNumGroupByPollution(paramMap);
        Comparator<Object> comparebynum = Comparator.comparing(m -> Integer.parseInt(((Map) m).get("num").toString()));
        List<Map<String, Object>> collect = datalist.stream().sorted(comparebynum).collect(Collectors.toList());
        return collect;
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 3:11
     * @Description: 根据时间范围获取该时间段内按问题类型进行分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countAlarmTaskNumGroupByProblemType(Map<String, Object> paramMap) {
        //获取问题类型信息
        List<Map<String, Object>> problemtypes = alarmTaskDisposeManagementMapper.getProblemTypeSelectData(null);
        //根据时间范围获取该时段的报警任务处置信息
        paramMap.put("tasktype", AlarmTaskEnum.getCode());
        List<Map<String, Object>> datalist = alarmTaskDisposeManagementMapper.getAlarmTaskNumGroupByProblemType(paramMap);
        List<Map<String, Object>> resultlist = new ArrayList<>();
        if (datalist != null && datalist.size() > 0 && problemtypes.size() > 0) {
            for (Map<String, Object> map : problemtypes) {
                String code = map.get("Code").toString();
                int total = 0;
                int alltotal = 0;
                for (Map<String, Object> map2 : datalist) {
                    String problem = map2.get("FK_ProblemType").toString();
                    int num = Integer.parseInt(map2.get("num").toString());
                    String[] str = problem.split(",");
                    if (str.length > 1) {//说明不止一个问题类型
                        for (String strstr : str) {
                            if (strstr.equals(code)) {//当编码相同
                                total = total + num;
                            }
                        }
                        alltotal = alltotal + (str.length) * num;
                    } else {
                        if (str[0].equals(code)) {//当编码相同
                            total = total + num;
                        }
                        alltotal = alltotal + num;
                    }
                }
                Map<String, Object> result = new HashMap<>();
                result.put("proportion", map.get("Name"));
                result.put("num", total);
                if (total > 0) {
                    result.put("problemtype", String.format("%.2f", (float) total / (float) alltotal * 100));
                    resultlist.add(result);
                }
            }
        }
        return resultlist;
    }



    /**
     * @author: xsm
     * @date: 2019/7/30 0030 下午 3:59
     * @Description: 保存报警任务忽略信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void neglectTaskInfo(AlarmTaskDisposeManagementVO alarmTaskDisposeManagement, String userId) {
        try {
            alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode());
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
            //添加分派人的评论意见
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
            obj.setFkTaskhandleuserid(userId);
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.NeglectTaskEnum.getName().toString());
            obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj.setTaskhandletime(new Date());
            obj.setTaskcomment(alarmTaskDisposeManagement.getFeedbackresults());
            taskFlowRecordInfoMapper.insert(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 11:25
     * @Description: 自定义查询条件获取报警处置数量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAlarmTaskDisposeNumDataByParams(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getAlarmTaskDisposeNumDataByParams(paramMap);
    }


    /**
     * @author: lip
     * @date: 2019/8/1 0001 下午 6:26
     * @Description: 获取所有部门下的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllOrganizationUser(Map<String, Object> param) {
        return alarmTaskDisposeManagementMapper.getAllOrganizationUser(param);
    }

    /**
     * @author: xsm
     * @date: 2019/8/2 0002 上午 11:27
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addAlarmTaskStatusToHandle(String id, String userId, String username) {
        //获取报警任务处置详情信息
        AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = alarmTaskDisposeManagementMapper.selectByPrimaryKey(id);
        //根据任务ID获取处理过该任务的相关人员信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(id);
        boolean isdisposaluser = false;//判断是否是被分派处理该任务的处置人
        boolean iszbtask = false;//判断是否是转办任务
        boolean iszbdisposaluser = false;//判断是否是转办任务处置人
        boolean isdisposal = false;//判断该任务是否有人正在处理
        for (Map<String, Object> obj : datalist) {
            //待处理 可能为多个用户
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                if (userId.equals(obj.get("FK_TaskHandleUserID").toString())) {//判断当前用户是否为被分派处理该任务的处置人
                    isdisposaluser = true;
                }
            }
            //转办任务
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.TransferTaskEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                iszbtask = true;
                if (userId.equals(obj.get("FK_TaskHandleUserID").toString())) {//判断当前用户是否为被转办处理该任务的处置人
                    iszbdisposaluser = true;
                }
            }
            //处理中
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                //已有人处置该任务
                isdisposal = true;
            }
        }
        //判断任务是否是转办任务
        if (iszbtask == true) {
            //判断并记录任务状态信息为处置中
            if (iszbdisposaluser == true) {//是该任务的处理人
                if (isdisposal == false) {//任务还未处理
                    //修改任务状态
                    alarmTaskDisposeManagementVO.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode());
                    alarmTaskDisposeManagementVO.setUpdatetime(new Date());
                    alarmTaskDisposeManagementVO.setUpdateuser(username);
                    int i = alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagementVO);
                    if (i > 0) {
                        //任务处理人
                        //添加处理人信息
                        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
                        obj.setPkId(UUID.randomUUID().toString());
                        obj.setFkTaskid(id);
                        obj.setFkTaskhandleuserid(userId);
                        obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName().toString());
                        obj.setFkTasktype(alarmTaskDisposeManagementVO.getFkTasktype());
                        obj.setTaskhandletime(new Date());
                        taskFlowRecordInfoMapper.insert(obj);
                    }
                }
            }
        } else {
            //判断并记录任务状态信息为处置中
            if (isdisposaluser == true) {//是该任务的处理人
                if (isdisposal == false) {//任务还未处理
                    //修改任务状态
                    alarmTaskDisposeManagementVO.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode());
                    alarmTaskDisposeManagementVO.setUpdatetime(new Date());
                    alarmTaskDisposeManagementVO.setUpdateuser(username);
                    int i = alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagementVO);
                    if (i > 0) {
                        //任务处理人
                        //添加处理人信息
                        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
                        obj.setPkId(UUID.randomUUID().toString());
                        obj.setFkTaskid(id);
                        obj.setFkTaskhandleuserid(userId);
                        obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName().toString());
                        obj.setFkTasktype(alarmTaskDisposeManagementVO.getFkTasktype());
                        obj.setTaskhandletime(new Date());
                        taskFlowRecordInfoMapper.insert(obj);
                    }
                }
            }
        }

    }


    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:20
     * @Description: 获取报警任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getAllAlarmTaskDisposeListDataByParamMapForApp(Map<String, Object> paramMap) {
      /*  Map<String, Object> datas = new HashMap<>();
        try {
            String theuserid = paramMap.get("userid").toString();
            paramMap.put("tasktype", AlarmTaskEnum.getCode());
            paramMap.put("feedbackuserid", theuserid);
            List<String> mns = new ArrayList<>();
            //分页获取所有报警任务
            //获取总条数
            Long countall = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoCountByParams(paramMap);
            List<Map<String, Object>> listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(paramMap);
            //获取分页后的任务ID
            List<String> taskids = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    taskids.add(map.get("PK_TaskID").toString());
                    String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                    if (map.get("overtimenum") != null) {
                        int overtimenum = Integer.parseInt(map.get("overtimenum").toString());
                        map.put("alarmovertime", countHourMinuteTime(overtimenum));
                    }
                    map.put("lastalarmtime", map.get("TaskEndTime"));
                }
                paramMap.put("taskids", taskids);
                //获取流程数据
                List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(paramMap);
                //获取报警污染物数据
                List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(paramMap);
                Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                //通过任务ID分组数据
                Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                for (Map<String, Object> map : listdata) {
                    String taskid = map.get("PK_TaskID").toString();
                    map.put("pollutantname",idandcodenames.get(taskid));
                    setTaskAllFlagData(theuserid, uncompletetime, map, lc_map.get(taskid));
                }
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
                datas.put("total", countall);
                datas.put("datalist", listdata);
                return datas;
            } else {
                datas.put("datalist", listdata);
                return datas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        return null;
    }


    /**
     * @author: xsm
     * @date: 2019/8/3 0003 上午 11:23
     * @Description: 根据任务主键ID获取报警任务详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAlarmTaskDetailByID(String id, String userId, String name) {
        Map<String, Object> result = new HashMap<>();
        //获取报警任务处置详情信息
        AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = alarmTaskDisposeManagementMapper.selectByPrimaryKey(id);
        //根据任务ID获取处理过该任务的相关人员信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(id);
        String username = "";
        String distributor = "";
        String assigntime = "";
        List<String> sc_userids= new ArrayList<>();
        String sc_time = "";
        String sc_user = "";
        //Object taskhandletime = null;
        for (Map<String, Object> obj : datalist) {
            //2.分派任务
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                distributor = obj.get("User_Name").toString();
                assigntime = DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime"));
            }
            //3.待处理 可能为多个用户，拼接用户名
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                if (obj.get("TaskComment")==null){
                    username = username + obj.get("User_Name") + "、";
                }
                //askhandletime = DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime"));
            }
            if (obj.get("CurrentTaskStatus")!=null&&CommonTypeEnum.AlarmTaskStatusEnum.ReviewedEnum.getName().toString().equals(obj.get("CurrentTaskStatus").toString())){
                if ("".equals(sc_time)){
                    sc_time =  obj.get("TaskHandleTime")!=null?obj.get("TaskHandleTime").toString():"";
                }else{
                    if(obj.get("TaskHandleTime")!=null&&!sc_time.equals(obj.get("TaskHandleTime").toString())){
                        //比较两个时间 留取最大（最近） 一条转办任务时间
                        try {
                            if(compare(sc_time, obj.get("TaskHandleTime").toString())){
                                sc_time = obj.get("TaskHandleTime").toString();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (!"".equals(username)) {
            username = username.substring(0, username.length() - 1);
        }
        //判断任务是否有添加审查人员  获取最后一次添加审查人员的时间
        for(Map<String, Object> map : datalist){
            if (!"".equals(sc_time)) {//有审查时间
                if (map.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.ReviewedEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())
                )&&sc_time.equals(map.get("TaskHandleTime").toString())) {
                    sc_user = sc_user + map.get("User_Name") + "、";
                    sc_userids.add(map.get("FK_TaskHandleUserID").toString());
                }
            }
        }
        if (!"".equals(sc_user)){
            sc_user = sc_user.substring(0,sc_user.length()-1);
        }
        //获取问题类型
        String problemtype = alarmTaskDisposeManagementVO.getFkProblemtype();
        String problem = "";
        if (problemtype != null && !"".equals(problemtype)) {
            String[] str = problemtype.split(",");
            List<Map<String, Object>> problemtypelist = alarmTaskDisposeManagementMapper.getProblemTypeSelectData(null);
            if (str.length > 0) {
                for (String code : str) {
                    for (Map<String, Object> problemmap : problemtypelist) {
                        if (code.equals(problemmap.get("Code").toString())) {
                            problem = problem + problemmap.get("Name") + ",";
                        }
                    }
                }
            }
        }
        if (!"".equals(problem)) {
            problem = problem.substring(0, problem.length() - 1);
        }
        result.put("sc_user", sc_user);
        result.put("sc_userids", sc_userids);
        result.put("handlerusers", username);
        result.put("distributor", distributor);
        result.put("assigntime", assigntime);
        result.put("taskremark", alarmTaskDisposeManagementVO.getTaskremark());
        result.put("problemtypecode", problemtype);
        result.put("problemtype", problem);
        result.put("feedbackresults", alarmTaskDisposeManagementVO.getFeedbackresults());
        result.put("fileid", alarmTaskDisposeManagementVO.getFileid());
        return result;
    }


    @Override
    public Map<String, Object> getMonitorPointAlarmTaskDetailByID(String id, String userId, String name) {
        Map<String, Object> result = new HashMap<>();
        //获取报警任务处置详情信息
        AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = alarmTaskDisposeManagementMapper.selectByPrimaryKey(id);
        //根据任务ID获取处理过该任务的相关人员信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(id);
        String username = datalist.stream().filter(m -> m.get("User_Name") != null).map(m -> m.get("User_Name").toString()).collect(Collectors.joining("、"));
        String distributor = "";
        String assigntime = "";
        //Object taskhandletime = null;
        for (Map<String, Object> obj : datalist) {
            assigntime = DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime"));
        }
        //获取问题类型
        String problemtype = alarmTaskDisposeManagementVO.getFkProblemtype();
        String problem = "";
        if (problemtype != null && !"".equals(problemtype)) {
            String[] str = problemtype.split(",");
            List<Map<String, Object>> problemtypelist = alarmTaskDisposeManagementMapper.getProblemTypeSelectData(null);
            if (str.length > 0) {
                for (String code : str) {
                    for (Map<String, Object> problemmap : problemtypelist) {
                        if (code.equals(problemmap.get("Code").toString())) {
                            problem = problem + problemmap.get("Name") + ",";
                        }
                    }
                }
            }
        }
        if (!"".equals(problem)) {
            problem = problem.substring(0, problem.length() - 1);
        }
        result.put("handlerusers", username);
        result.put("distributor", distributor);
        result.put("assigntime", assigntime);
        result.put("taskremark", alarmTaskDisposeManagementVO.getTaskremark());
        result.put("problemtypecode", problemtype);
        result.put("problemtype", problem);
        result.put("feedbackresults", alarmTaskDisposeManagementVO.getFeedbackresults());
        result.put("fileid", alarmTaskDisposeManagementVO.getFileid());
        return result;
    }

    /**
     * @author: chengzq
     * @date: 2019/8/19 0019 上午 8:51
     * @Description: 通过自定义条件查询日常任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDaliyTaskByParamMap(Map<String, Object> param) {
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        param.put("categorys", categorys);
        return alarmTaskDisposeManagementMapper.getDaliyTaskByParamMap(param);
    }


    /**
     * @author: chengzq
     * @date: 2019/8/19 0019 下午 1:08
     * @Description: 通过主键删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkTaskid]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkTaskid) {
        return alarmTaskDisposeManagementMapper.deleteByPrimaryKey(pkTaskid);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/21 0021 上午 9:52
     * @Description: 修改任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int updateByPrimaryKey(AlarmTaskDisposeManagementVO record) {
        return alarmTaskDisposeManagementMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2019/8/21 0021 上午 9:53
     * @Description: 新增任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insert(AlarmTaskDisposeManagementVO record) {
        return alarmTaskDisposeManagementMapper.insertSelective(record);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/22 0022 上午 8:46
     * @Description: 获取日常任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getDaliyTaskDetailByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getDaliyTaskDetailByParamMap(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/8/23 0023 上午 9:37
     * @Description: 通过自定义参数获取任务流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getDaliyTaskRecordInfoByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getDaliyTaskRecordInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/23 0023 上午 9:18
     * @Description: 根据监测时间获取该月份报警任务列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAlarmTaskListDataByMonitortime(Map<String, Object> paramMap) {
        paramMap.put("tasktype", AlarmTaskEnum.getCode());
        List<Map<String, Object>> datalist = alarmTaskDisposeManagementMapper.getAlarmTaskListDataByMonitortime(paramMap);
        int count = alarmTaskDisposeManagementMapper.getAlarmTaskNumByParamMap(paramMap);
        if (datalist != null && datalist.size() > 0) {
            for (Map<String, Object> map : datalist) {
                map.put("total", count);
            }
        }
        return datalist;
    }

    /**
     * @author: xsm
     * @date: 2019/8/24 0024 上午 11:28
     * @Description: 根据自定义参数统计某状态的任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTaskDisposeNumDataByParams(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTaskDisposeNumDataByParams(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/27 0027 上午 11:19
     * @Description: 通过id查询处理中用户id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkTaskid]
     * @throws:
     */
    @Override
    public String getFlowRecordInfoByTaskID(String pkTaskid) {
        return taskFlowRecordInfoMapper.getFlowRecordInfoByTaskID(pkTaskid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/31 0031 上午 10:56
     * @Description: 根据自定义参数统计超标或日常任务各状态的任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countTaskDisposeNumGroupByStatusByParams(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskDisposeNumGroupByStatusByParams(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2019/8/27 0027 上午 9:21
     * @Description: 根据自定义参数按任务状态分组统计单个企业的任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countPollutionAlarmTaskGroupByStatusByParamMap(Map<String, Object> paramMap) {
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        //获取当个企业的报警任务信息
        if(paramMap.get("tasktype")==null){
            paramMap.put("tasktype", AlarmTaskEnum.getCode());
        }else if(categorys.contains("2") && Integer.valueOf(paramMap.get("tasktype").toString())==AlarmTaskEnum.getCode()){
            paramMap.put("tasktypes",Arrays.asList(AlarmTaskEnum.getCode(),MonitorTaskEnum.getCode(),SupervisoryControlEnum.getCode()));
            paramMap.remove("paramMap");
        }else if(categorys.contains("2") && Integer.valueOf(paramMap.get("tasktype").toString())==DevOpsTaskEnum.getCode()){
            paramMap.put("tasktypes",Arrays.asList(DevOpsTaskEnum.getCode(),SecurityDevOpsTaskEnum.getCode()));
            paramMap.remove("paramMap");
        }

        List<Map<String, Object>> resultlist = new ArrayList<>();
        List<Map<String, Object>> datalist = alarmTaskDisposeManagementMapper.countPollutionAlarmTaskGroupByStatusByParamMap(paramMap);
        List<Integer> statuslist = CommonTypeEnum.getAlarmTaskStatusList();
        for (int status : statuslist) {
            Map<String, Object> map = new HashMap<>();
            if (CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode() == status) {
                //待分派
                map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getName());
            } else if (CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode() == status) {
                //待处理
                map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getName());
            } else if (CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode() == status) {
                //处理中
                map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getName());
            } else if (CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode() == status) {
                //已完成
                map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getName());
            }
            Object num = 0;
            if (datalist != null && datalist.size() > 0) {
                for (Map<String, Object> objmap : datalist) {
                    if ((String.valueOf(status)).equals(objmap.get("TaskStatus").toString())) {
                        num = objmap.get("num");
                        break;
                    }
                }
            }
            map.put("num", num);
            map.put("taskstatus", status);
            resultlist.add(map);
        }
        return resultlist;
    }


    /**
     * @author: xsm
     * @date: 2019/8/20 0020 下午 1:08
     * @Description: 比较两个时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private boolean compare(String time1, String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //Date类的一个方法，如果a早于b返回true，否则返回false
        if (a.before(b))
            return true;
        else
            return false;
    }


    /**
     * @author: xsm
     * @date: 2019/12/02 0002 下午 1:09
     * @Description: app端根据污染源ID获取关联的报警任务信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAlarmTaskInfoByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getAlarmTaskInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/1/10 0010 下午 1:56
     * @Description: 通过任务类型集合获取任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTaskInfoByTaskTypes(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTaskInfoByTaskTypes(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 10:18
     * @Description: 统计报警任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> countAlarmTaskCompletionStatusByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countAlarmTaskCompletionStatusByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 上午 10:23
     * @Description: 根据自定义参数获取一段时间范围内的报警任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getAlarmTaskListDataByParam(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            //获取所有报警任务信息
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
            }
            List<Map<String, Object>> listdata = new ArrayList<>();
            if (paramMap.get("datauserid") != null) {
                listdata = alarmTaskDisposeManagementMapper.getAlarmTaskListDataByParamAndDataUserId(paramMap);
            } else {
                listdata = alarmTaskDisposeManagementMapper.getAlarmTaskListDataByParam(paramMap);
            }

            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(listdata);
                long total = pageInfo.getTotal();
                resultMap.put("total", total);
            }
            Set<String> pollutions = new HashSet<String>();
            String maxdate = "";
            String mindate = "";
            List<Map<String, Object>> alltypelist = deviceStatusMapper.getALLMonitorPointTypeInfoByTypes(new HashMap<>());
            Map<Integer, Object> codeandname = new HashMap<>();
            if (alltypelist != null && alltypelist.size() > 0) {
                for (Map<String, Object> typemap : alltypelist) {
                    if (typemap.get("Code") != null) {
                        codeandname.put(Integer.parseInt(typemap.get("Code").toString()), typemap.get("Name"));
                    }
                }
            }
            if (listdata != null && listdata.size() > 0) {
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        map.put("tasktypename", "报警工单");
                        pollutions.add(map.get("FK_Pollutionid").toString());
                        Date d2 = DataFormatUtil.parseDateYMD(map.get("TaskCreateTime").toString());
                        if (!"".equals(maxdate)) {
                            Date d1 = DataFormatUtil.parseDateYMD(maxdate);
                            if (d2.compareTo(d1) > 0) {
                                maxdate = map.get("TaskCreateTime").toString();
                            }
                        } else {
                            maxdate = map.get("TaskCreateTime").toString();
                        }
                        if (!"".equals(mindate)) {
                            Date d1 = DataFormatUtil.parseDateYMD(mindate);
                            if (d2.compareTo(d1) < 0) {
                                mindate = map.get("TaskCreateTime").toString();
                            }
                        } else {
                            mindate = map.get("TaskCreateTime").toString();
                        }
                    }
                    paramMap.put("pollutions", pollutions);
                }
                //获取所有企业排口的MN号,包括废水、废气、雨水排口
                List<Map<String, Object>> allmns = alarmTaskDisposeManagementMapper.getAllOutputMn(paramMap);
                Set<String> mnlist = new HashSet<String>();
                for (Map<String, Object> obj : allmns) {
                    if (obj.get("DGIMN") != null) {
                        mnlist.add(obj.get("DGIMN").toString());
                    }
                }
                if (mnlist.size() > 0) {
                    paramMap.put("mnlist", mnlist);
                }
                //获取所有排口监测的污染物
                List<Map<String, Object>> allpollutants = alarmTaskDisposeManagementMapper.getAllEntPollutants();
                //获取废水、废气、雨水排口各类型污染物（并集）
                List<AggregationOperation> operations = new ArrayList<>();
                operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist).andOperator(Criteria.where("OverTime").gte(DataFormatUtil.getDateYMDHMS(mindate + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(maxdate + " 23:59:59")))));
                // 加8小时
                operations.add(Aggregation.project("PollutantCode", "OverTime", "num", "DataGatherCode").andExpression("add(OverTime,8 * 3600000)").as("date8"));
                operations.add(Aggregation.project("PollutantCode", "date8", "OverTime", "num", "DataGatherCode").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
                operations.add(Aggregation.group(new String[]{"theDate", "PollutantCode", "DataGatherCode"}).count().as("num").last("OverTime").as("OverTime"));
                Aggregation aggregationquery = newAggregation(operations);
                AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, overData_db, Document.class);
                List<Document> documents = resultdocument.getMappedResults();
                if (documents.size() > 0) {//判断查询数据是否为空
                    //遍历任务组装任务数据信息
                    for (Map<String, Object> map : listdata) {
                        String lasttime = "";
                        if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
                            //待分派
                            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getName());
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
                            //待处理
                            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getName());
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
                            //处理中
                            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getName());
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
                            //已完成
                            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getName());
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
                            //已忽略
                            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getName());
                        }
                        Map<Integer, Set<String>> typemap = new HashMap<>();
                    /*Set<String> waterset = new HashSet<>();
                    Set<String> gasset = new HashSet<>();
                    Set<String> rainset = new HashSet<>();
                    Set<String> smokeset = new HashSet<>();*/
                        if (map.get("FK_Pollutionid") != null) {
                            for (Map<String, Object> obj : allmns) {
                                if ((map.get("FK_Pollutionid").toString()).equals(obj.get("Pollutionid").toString())) {//当污染源ID相同，得到该污染源下的MN号
                                    boolean isoutput = false;
                                    //根据MN号去MongoDB中查询出的数据里找到相应的报警信息
                                    Set<String> pollutantnames = new HashSet<>();
                                    for (Document document : documents) {
                                        //当日期相同，MN号相同，统计超标次数和超标污染物
                                        if ((map.get("TaskCreateTime").toString()).equals(document.getString("theDate")) && (document.getString("DataGatherCode")).equals(obj.get("DGIMN").toString())) {
                                            if (!"".equals(lasttime)) {
                                                if (compare(lasttime, DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")))) {
                                                    lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("OverTime"));
                                                }
                                            } else {
                                                lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("OverTime"));
                                            }
                                            for (Map<String, Object> obj2 : allpollutants) {
                                                if (document.getString("PollutantCode").equals(obj2.get("Code").toString())) {
                                                    isoutput = true;
                                                    pollutantnames.add(obj2.get("Name").toString());
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (isoutput == true) {
                                        int type = Integer.parseInt(obj.get("type").toString());
                                        if (typemap.get(type) != null) {
                                            Set<String> strset = typemap.get(type);
                                            strset.addAll(pollutantnames);
                                        } else {
                                            typemap.put(type, pollutantnames);
                                        }
                                    }
                                }
                            }
                        }
                        List<Map<String, Object>> pollutantnamess = new ArrayList<>();
                        for (Map.Entry<Integer, Set<String>> entry : typemap.entrySet()) {
                            Integer monitortype = entry.getKey();
                            Set<String> mapValue = entry.getValue();
                            if (mapValue.size() > 0) {
                                Map<String, Object> watermap = new HashMap<>();
                                watermap.put("typename", codeandname.get(monitortype));
                                String pollutantname = "【";
                                for (String str : mapValue) {
                                    pollutantname = pollutantname + str + "、";
                                }
                                pollutantname = pollutantname.substring(0, pollutantname.length() - 1);
                                watermap.put("pollutantname", pollutantname + "】");
                                pollutantnamess.add(watermap);
                            }
                        }
                        map.put("pollutantlist", pollutantnamess);
                        map.put("lastalarmtime", lasttime);

                    }
                } else {
                    for (Map<String, Object> map : listdata) {
                        String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                        map.put("pollutantlist", new ArrayList<>());
                        map.put("taskstatuname", taskstatuname);
                        map.put("lastalarmtime", "");
                    }
                }
            }
            if (listdata != null && listdata.size() > 0) {
                Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("lastalarmtime").toString()).reversed();
                List<Map<String, Object>> collect = listdata.stream().sorted(comparebytime).collect(Collectors.toList());
                resultMap.put("datalist", collect);
            } else {
                resultMap.put("datalist", listdata);
            }
            return resultMap;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 下午 13:10
     * @Description: 根据监测时间段返回该时间段内所有日期的报警情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTaskSituationByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTaskSituationByParamMap(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2020/2/20 0020 下午 16:32
     * @Description: 根据时间范围获取该时间段内运维任务各个任务状态的统计情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countDevOpsTaskNumGroupByStatusByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countDevOpsTaskNumGroupByStatusByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/02/21 0021 上午 9:51
     * @Description:根据自定义参数获取以监测点位分组的运维任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDevOpsTaskInfosGroupByMonitorPointByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getDevOpsTaskInfosGroupByMonitorPointByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/02/21 0021 上午 11:00
     * @Description:根据自定义参数统计按监测类型分组的运维任务条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countDevOpsTaskDataNumGroupByMonitorTypeByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countDevOpsTaskDataNumGroupByMonitorTypeByParamMap(paramMap);
    }


    @Override
    public Map<String, Object> getAlarmAndDevOpsTaskNumByParam(Map<String, Object> paramMap) {
        List<Integer> monitortypes = new ArrayList<>();
        monitortypes.add(WasteWaterEnum.getCode());
        monitortypes.add(WasteGasEnum.getCode());
        monitortypes.add(SmokeEnum.getCode());
        monitortypes.add(RainEnum.getCode());
        monitortypes.add(FactoryBoundarySmallStationEnum.getCode());
        monitortypes.add(FactoryBoundaryStinkEnum.getCode());
        monitortypes.add(EnvironmentalStinkEnum.getCode());
        monitortypes.add(TZFEnum.getCode());
        monitortypes.add(XKLWEnum.getCode());
        monitortypes.add(EnvironmentalVocEnum.getCode());
        monitortypes.add(MicroStationEnum.getCode());
        monitortypes.add(EnvironmentalDustEnum.getCode());
        monitortypes.add(AirEnum.getCode());
        monitortypes.add(WaterQualityEnum.getCode());
        Map<String, Object> result = new HashMap<>();
        result.put("alarmtasknum", 0);
        result.put("devopstasknum", 0);
        result.put("changetasknum", 0);
        if (monitortypes.contains(Integer.parseInt(paramMap.get("monitorpointtype").toString()))) {
            List<Map<String, Object>> allmap = alarmTaskDisposeManagementMapper.getAlarmTaskByDgimnAndTimes(paramMap);
            if (allmap!=null&&allmap.size()>0){
                for (Map<String, Object> map:allmap){
                    String tasktype = map.get("FK_TaskType")!=null?map.get("FK_TaskType").toString():"";
                    if (tasktype.equals(AlarmTaskEnum.getCode()+"")){
                        result.put("alarmtasknum",map.get("num"));
                    }else if(tasktype.equals(DevOpsTaskEnum.getCode()+"")){
                        result.put("devopstasknum",map.get("num"));
                    }else if(tasktype.equals(ChangeAlarmTaskEnum.getCode()+"")){
                        result.put("changetasknum", map.get("num"));
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 13:07
     * @Description:根据任务类型和自定义参数获取报警、运维任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getTaskListDataByTaskTypeAndParamMap(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Map<String, Object> resultmap = new HashMap<>();
        //获取所有视频信息
        List<Map<String, Object>> videolist = videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
        Map<String, List<Map<String, Object>>> pointidAndrtsp = new HashMap<>();
        Map<String, List<Map<String, Object>>> usermenus = RedisTemplateUtil.getRedisCacheDataByToken("usermenuandbuttonauth", Map.class);
        try {
            if (videolist != null && videolist.size() > 0) {
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videolist) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videolist) {
                                if (map2.get("monitorpointid") != null && (map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))) {
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp", map2.get("rtsp"));
                                    objmap.put("name", map2.get("name"));
                                    objmap.put("id", map2.get("pkid"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(), rtsplist);
                        }
                        //
                    }
                }
            }
            //根据任务类型获取监测任务和运维任务
            if ((paramMap.get("tasktype").toString()).equals(AlarmTaskEnum.getCode().toString())) {//报警任务
                Query query = new Query();
                query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
                query.addCriteria(Criteria.where("OverTime").gte(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59")));
                query.with(new Sort(Sort.Direction.ASC, "OverTime"));
                List<Document> documents = mongoTemplate.find(query, Document.class, overData_db);
                //判断菜单权限
                List<Map<String, Object>> menus = usermenus.get(CommonTypeEnum.EnvSupervisionMenus.alarmTaskManagement.getCode().toLowerCase());
                boolean isDassign = false;
                if (menus != null && menus.size() > 0) {
                    for (Map<String, Object> buttons : menus) {
                        if (buttons.get("button_code") != null && "assignButton".equals(buttons.get("button_code").toString())) {
                            isDassign = true;
                            break;
                        }
                    }
                }
                if (isDassign) {
                    paramMap.put("hasauthor", "1");
                } else {//无分派按钮权限  值显示和自己相关的报警任务
                    paramMap.put("hasauthor", "0");
                }
                List<Map<String, Object>> listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    List<String> taskids =listdata.stream().filter(m -> m.get("PK_TaskID") != null).map(m->m.get("PK_TaskID").toString()).distinct().collect(Collectors.toList());
                    //获取报警污染物数据
                    paramMap.put("taskids",taskids);
                    List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(paramMap);
                    Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                    for (Map<String, Object> map : listdata) {
                        map.put("rtsplist", pointidAndrtsp.get(map.get("FK_Pollutionid").toString()));
                        String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                        map.put("taskstatuname", taskstatuname);
                        if(map.get("PK_TaskID")!=null) {
                            map.put("pollutantname", idandcodenames.get(map.get("PK_TaskID").toString()));
                        }
                        if (map.get("isfeedbackuserid") != null) {
                            map.put("isfeedback", "1");
                        } else {
                            map.put("isfeedback", "0");
                        }
                        map.put("lastalarmtime", map.get("TaskEndTime"));
                        int num = 0;
                        if (documents != null && documents.size() > 0) {
                            if (map.get("AlarmStartTime") != null && map.get("TaskEndTime") != null) {
                                String start = map.get("AlarmStartTime").toString();
                                String end = map.get("TaskEndTime").toString();
                                boolean flag1 = false;
                                boolean flag2 = false;
                                for (Document document : documents) {
                                    if (start.equals(DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")))) {
                                        flag1 = true;
                                    } else {
                                        if (compare(start, DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")))) {
                                            flag1 = true;
                                        } else {
                                            flag1 = false;
                                        }
                                    }
                                    if (end.equals(DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")))) {
                                        flag2 = false;
                                    } else {
                                        if (compare(end, DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")))) {
                                            flag2 = true;
                                        } else {
                                            flag2 = false;
                                        }
                                    }
                                    if (flag1 == true && flag2 == false) {
                                        num += 1;
                                    }
                                }

                            }
                        }
                        map.put("alarmnumber", num);
                    }
                }
                resultlist.addAll(listdata);
            } else if ((paramMap.get("tasktype").toString()).equals(DevOpsTaskEnum.getCode().toString())) {//运维任务
                Query query = new Query();
                query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
                query.addCriteria(Criteria.where("ExceptionTime").gte(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59")));
                query.with(new Sort(Sort.Direction.ASC, "ExceptionTime"));
                List<Document> documents = mongoTemplate.find(query, Document.class, exceptionData_db);
                if (documents.size() > 0) {//
                    //判断菜单权限
                    List<Map<String, Object>> menus = usermenus.get(CommonTypeEnum.EnvSupervisionMenus.operateTaskManagement.getCode().toLowerCase());
                    boolean isDassign = false;
                    if (menus != null && menus.size() > 0) {
                        for (Map<String, Object> buttons : menus) {
                            if (buttons.get("button_code") != null && "assignButton".equals(buttons.get("button_code").toString())) {
                                isDassign = true;
                                break;
                            }
                        }
                    }
                    if (isDassign) {
                        paramMap.put("hasauthor", "1");
                    } else {//无分派按钮权限  值显示和自己相关的报警任务
                        paramMap.put("hasauthor", "0");
                    }
                    List<Map<String, Object>> devopslist = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(paramMap);
                    if (devopslist != null && devopslist.size() > 0) {
                        List<String> taskids =devopslist.stream().filter(m -> m.get("PK_TaskID") != null).map(m->m.get("PK_TaskID").toString()).distinct().collect(Collectors.toList());
                        //获取报警污染物数据
                        paramMap.put("taskids",taskids);
                        List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(paramMap);
                        Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                        for (Map<String, Object> map : devopslist) {
                            map.put("rtsplist", pointidAndrtsp.get(map.get("FK_Pollutionid").toString()));
                            String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                            map.put("taskstatuname", taskstatuname);
                            if(map.get("PK_TaskID")!=null) {
                                map.put("pollutantname", idandcodenames.get(map.get("PK_TaskID").toString()));
                            }
                            if (map.get("isfeedbackuserid") != null) {
                                map.put("isfeedback", "1");
                            } else {
                                map.put("isfeedback", "0");
                            }
                            map.put("lastalarmtime", map.get("TaskEndTime"));
                            int num = 0;
                            if (documents != null && documents.size() > 0) {
                                if (map.get("AlarmStartTime") != null && map.get("TaskEndTime") != null) {
                                    String start = map.get("AlarmStartTime").toString();
                                    String end = map.get("TaskEndTime").toString();
                                    boolean flag1 = false;
                                    boolean flag2 = false;
                                    for (Document document : documents) {
                                        if (start.equals(DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                            flag1 = true;
                                        } else {
                                            if (compare(start, DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                                flag1 = true;
                                            } else {
                                                flag1 = false;
                                            }
                                        }
                                        if (end.equals(DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                            flag2 = false;
                                        } else {
                                            if (compare(end, DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                                flag2 = true;
                                            } else {
                                                flag2 = false;
                                            }
                                        }
                                        if (flag1 == true && flag2 == false) {
                                            num += 1;
                                        }
                                    }

                                }
                            }
                            map.put("alarmnumber", num);
                        }
                    }
                    resultlist.addAll(devopslist);
                }
            }else if ((paramMap.get("tasktype").toString()).equals(ChangeAlarmTaskEnum.getCode().toString())) {//突变任务
                //判断菜单权限
                List<Map<String, Object>> menus = usermenus.get(CommonTypeEnum.EnvSupervisionMenus.operateTaskManagement.getCode().toLowerCase());
                boolean isDassign = false;
                if (menus != null && menus.size() > 0) {
                    for (Map<String, Object> buttons : menus) {
                        if (buttons.get("button_code") != null && "assignButton".equals(buttons.get("button_code").toString())) {
                            isDassign = true;
                            break;
                        }
                    }
                }
                if (isDassign) {
                    paramMap.put("hasauthor", "1");
                } else {//无分派按钮权限  值显示和自己相关的报警任务
                    paramMap.put("hasauthor", "0");
                }
                List<Map<String, Object>> devopslist = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(paramMap);
                if (devopslist != null && devopslist.size() > 0) {
                    List<String> taskids =devopslist.stream().filter(m -> m.get("PK_TaskID") != null).map(m->m.get("PK_TaskID").toString()).distinct().collect(Collectors.toList());
                    //获取报警污染物数据
                    paramMap.put("taskids",taskids);
                    List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(paramMap);
                    Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                    for (Map<String, Object> map : devopslist) {
                        map.put("rtsplist", pointidAndrtsp.get(map.get("FK_Pollutionid").toString()));
                        String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                        map.put("taskstatuname", taskstatuname);
                        if(map.get("PK_TaskID")!=null) {
                            map.put("pollutantname", idandcodenames.get(map.get("PK_TaskID").toString()));
                        }
                        if (map.get("isfeedbackuserid") != null) {
                            map.put("isfeedback", "1");
                        } else {
                            map.put("isfeedback", "0");
                        }
                        map.put("lastalarmtime", map.get("TaskEndTime"));
                        int num = 0;

                        map.put("alarmnumber", num);
                    }
                }
                resultlist.addAll(devopslist);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result.put("total", resultlist != null ? resultlist.size() : 0);
        result.put("datalist", resultlist);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 16:23
     * @Description:根据任务类型统计该类型任务的时效性
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> countTaskDisposeTimelinessNumByTaskType(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> resultmap = alarmTaskDisposeManagementMapper.getTaskDisposeTimelinessInfoByTaskType(paramMap);
        if (resultmap != null) {
            int completiondays = Integer.parseInt(resultmap.get("CompletionDays").toString());
            int expirereminddays = Integer.parseInt(resultmap.get("ExpireRemindDays").toString());
            paramMap.put("completiondays", completiondays);
            paramMap.put("expirereminddays", expirereminddays);
            result = alarmTaskDisposeManagementMapper.countTaskDisposeTimelinessNumByTaskType(paramMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTaskInfo(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTaskInfo(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 8:34
     * @Description:各任务状态任务数量统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> countTaskNumGroupByTaskStatusByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskNumGroupByTaskStatusByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/3/02 0002 下午 16:58
     * @Description:根据时间和任务类型统计每日任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> countTaskNumGroupByMonitorTimeByTaskType(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskNumGroupByMonitorTimeByTaskType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 8:56
     * @Description:根据自定义查询条件统计处置人员任务完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> countTaskNumGroupByDisposerPeopleByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> listdata = alarmTaskDisposeManagementMapper.countTaskNumGroupByDisposerPeopleByParamMap(paramMap);
        List<Map<String, Object>> resultlist = new ArrayList<>();
        if (listdata != null && listdata.size() > 0) {
            Set<String> nameset = new HashSet<>();
            for (Map<String, Object> obj : listdata) {
                if (obj.get("User_Name") != null) {
                    if (nameset.contains(obj.get("User_Name").toString())) {//判断是否类型重复
                        continue;//重复
                    } else {//不重复
                        nameset.add(obj.get("User_Name").toString());
                        int total = 0;
                        Map<String, Object> map = new HashMap<>();
                        map.put("completednum", 0);
                        map.put("username", obj.get("User_Name"));
                        for (Map<String, Object> objmap : listdata) {
                            if (objmap.get("User_Name") != null && ((obj.get("User_Name").toString()).equals(objmap.get("User_Name").toString()))) {
                                total += Integer.parseInt(objmap.get("num").toString());
                                if ("已完成".equals(objmap.get("status").toString())) {
                                    map.put("completednum", objmap.get("num"));
                                }
                            }
                        }
                        map.put("total", total);
                        resultlist.add(map);
                    }
                }
            }
        }
        if (resultlist != null && resultlist.size() > 0) {
            Comparator<Object> comparebytotal = Comparator.comparing(m -> Integer.valueOf(((Map) m).get("total").toString())).reversed();
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebytotal).collect(Collectors.toList());
            return collect;
        } else {
            return resultlist;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/04/21 0021 上午 09:27
     * @Description:根据用户ID获取用户数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUserMonitorPointRelationDataByUserId(Map<String, Object> paramMap) {
        return userMonitorPointRelationDataMapper.getUserMonitorPointRelationDataByParams(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTaskDisposeManagementDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> listdata = alarmTaskDisposeManagementMapper.getTaskDisposeManagementDataByParam(paramMap);
        //获取分页后的任务ID
        List<String> taskids = new ArrayList<>();
        if (listdata != null && listdata.size() > 0) {
            for (Map<String, Object> map : listdata) {
                taskids.add(map.get("PK_TaskID").toString());
                map.put("lastalarmtime", map.get("TaskEndTime"));
            }
            paramMap.put("taskids", taskids);
            //获取流程数据
            List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(paramMap);
            //通过任务ID分组数据
            Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
            for (Map<String, Object> map : listdata) {
                String taskid = map.get("PK_TaskID").toString();
                setTaskDisposerForHomePage( map, lc_map.get(taskid));

            }
        }
        return listdata;
    }

    /**
     * 获取处置人（未转办  则处置人未待处理的人   已转办  则处置人为最后一次转办的  转办人）
     * */
    private void setTaskDisposerForHomePage(  Map<String, Object> result, List<Map<String, Object>> lc_listdata) {
        try{
            String zbrwtime = "";
            String user_name = "";
            if (lc_listdata != null && lc_listdata.size() > 0) {
                //判断任务是否有转办  获取最后一次转办任务的转办时间  获取接收转办任务的人
                for(Map<String, Object> map : lc_listdata){
                    if (map.get("CurrentTaskStatus")!=null&&CommonTypeEnum.AlarmTaskStatusEnum.TransferEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())){
                        if ("".equals(zbrwtime)){
                            zbrwtime =  map.get("TaskHandleTime")!=null?map.get("TaskHandleTime").toString():"";
                        }else{
                            if(map.get("TaskHandleTime")!=null&&!zbrwtime.equals(map.get("TaskHandleTime").toString())){
                                //比较两个时间 留取最大（最近） 一条转办任务时间
                                if(compare(zbrwtime, map.get("TaskHandleTime").toString())){
                                    zbrwtime = map.get("TaskHandleTime").toString();
                                }
                            }
                        }
                    }
                }
                for (Map<String, Object> map : lc_listdata) {
                    String CurrentTaskStatus = map.get("CurrentTaskStatus") != null ? map.get("CurrentTaskStatus").toString() : "";
                    //String UserID = map.get("FK_TaskHandleUserID") != null ? map.get("FK_TaskHandleUserID").toString() : "";
                    //是否有反馈权限
                    if ("".equals(zbrwtime) && map.get("TaskComment") == null &&
                            CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName())) {
                       if (map.get("User_Name")!=null) {
                           user_name = user_name + map.get("User_Name") + ",";
                       }
                    } else if (!"".equals(zbrwtime)&&((compare(zbrwtime, map.get("TaskHandleTime").toString()))||zbrwtime.equals(map.get("TaskHandleTime").toString()))
                            && map.get("TaskComment") == null &&
                            CurrentTaskStatus.equals(CommonTypeEnum.AlarmTaskStatusEnum.TransferTaskEnum.getName())) {
                        if (map.get("User_Name")!=null) {
                            user_name = user_name + map.get("User_Name") + ",";
                        }
                    }
                }
            }
            if (!"".equals(user_name)){
                user_name = user_name.substring(0, user_name.length() - 1);
            }
            result.put("user_name", user_name);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/5/13 0013 下午 2:29
     * @Description: 自定义查询条件统计处置任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countTaskDataByParam(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskDataByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2020/5/13 0013 下午 5:27
     * @Description: 自定义查询条件根据运维公司统计任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countTaskNumGroupByCompanyByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskNumGroupByCompanyByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/5/15 0015 下午 13:17
     * @Description: 统计某一段时间内报警及运维工单各状态情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countAlarmAndDevopsTaskStatusNumByMonitorTimes(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countAlarmAndDevopsTaskStatusNumByMonitorTimes(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/03/29 0029 上午 09:54
     * @Description: 新增任务（根据推送的报警数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addAlarmTaskInfo(AlarmTaskDisposeManagementVO obj, List<String> overcodes, JSONObject messageobject) {
        //根据MN号 监测时间查询是否有该点位的报警信息
        messageobject.put("TaskID", null);
        String pollutionid = messageobject.get("PollutionID")!=null?messageobject.get("PollutionID").toString():"";
        if (obj.getFkPollutionid() != null && !"".equals(obj.getFkPollutionid())) {//判断查询数据是否为空
            String pkid = UUID.randomUUID().toString();
            obj.setPkTaskid(pkid);
            obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
            obj.setRecoverystatus((short)0);//默认新增任务的设备恢复状态为未恢复 0；
            alarmTaskDisposeManagementMapper.insert(obj);
            messageobject.put("TaskID", pkid);
            //判断是否需要自动分派任务
            String isautomaticassigntask = DataFormatUtil.parseProperties("isautomaticassigntask");//1 表示自动分派 0 表示否
            if("1".equals(isautomaticassigntask)){
                //自动分派
                automaticAssignTasks(obj,pollutionid);
            }
            //添加任务成功后添加该任务的报警污染物信息
            if (overcodes != null && overcodes.size() > 0) {
                for (String code : overcodes) {
                    TaskAlarmPollutantInfoVO objvo = new TaskAlarmPollutantInfoVO();
                    objvo.setPkId(UUID.randomUUID().toString());
                    objvo.setFkTaskid(pkid);
                    if (obj.getFkTasktype().equals(DevOpsTaskEnum.getCode().toString())) {
                        //异常污染物分隔code和异常类型  例：006_1
                        String[] onecodes = code.split("_");
                        objvo.setFkPolluantcode(onecodes[0]);
                        objvo.setAlarmtype(onecodes[1]);
                    } else {
                        objvo.setFkPolluantcode(code);
                    }
                    objvo.setFkTasktype(obj.getFkTasktype());
                    objvo.setUpdatetime(new Date());
                    taskAlarmPollutantInfoMapper.insertSelective(objvo);
                }
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2021/03/29 0029 上午 09:54
     * @Description: 自动分派环保运维任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void automaticAssignTasks(AlarmTaskDisposeManagementVO obj,String pollutionid) {
        Map<String,Object> paramMap = new HashMap<>();
        Date nowTime = new Date();
        String ym = DataFormatUtil.getDateYM(nowTime);//当前时间
        if (!"".equals(pollutionid)){
            paramMap.put("id", pollutionid);//企业ID
        }else{
            paramMap.put("id", obj.getFkPollutionid());//监测点ID
        }
        paramMap.put("patroltime",ym);
        //获取所有用户信息
        List<Map<String, Object>> allusers = userMapper.getAllUserInfo();
        List<String> alluserids = allusers.stream().filter(m -> m.get("id") != null).map(m -> m.get("id").toString()).collect(Collectors.toList());
        List<String> userids = new ArrayList<>();
        if (obj.getFkTasktype().equals(DevOpsTaskEnum.getCode().toString())){
            paramMap.put("monitorpointtype", obj.getFkmonitorpointtypecode());
            //运维任务分派给运维人员
            //根据企业/监测点ID 获取运维人员
            EntDevOpsInfoVO entdevopsinfovo = entDevOpsInfoMapper.getEntDevOpsInfoVOByParam(paramMap);
            if (entdevopsinfovo!=null&&entdevopsinfovo.getDevopspeople()!=null&&!"".equals(entdevopsinfovo.getDevopspeople())) {
                String useridstrs = entdevopsinfovo.getDevopspeople();
                String[] list = useridstrs.split(",");
                for (String strid:list){
                if (alluserids!=null&&alluserids.contains(strid)){
                    userids.add(strid);
                }
                }
            }
        }else{//报警及突变任务分配给巡查人员
            List<Map<String,Object>> overlist = new ArrayList<>();
            if (!"".equals(pollutionid)){
                paramMap.put("pointorentid", pollutionid);//企业ID
            }else{
                //paramMap.put("monitorpointtype", obj.getFkmonitorpointtypecode());
                paramMap.put("pointorentid", obj.getFkPollutionid());//监测点ID
            }
            if (paramMap.get("pointorentid")!=null) {
                // param.put("monitorpointtype",monitorpointtype);
                overlist = userMapper.getTaskAssignUserData(paramMap);
            }
            //overlist = patrolUserEntMapper.getPatrolPersonnelIdsByMonitorPointID(paramMap);
            if (overlist!=null&&overlist.size()>0){
                for (Map<String,Object> map:overlist){
                    if (map.get("User_ID")!=null&&alluserids!=null&&alluserids.contains(map.get("User_ID").toString())){
                        userids.add(map.get("User_ID").toString());
                    }
                }
            }
        }
        if (userids != null && userids.size() > 0) {
            //根据任务ID获取任务信息
            //AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeManagementMapper.selectByPrimaryKey(obj.getPkTaskid());
            //获取配置的 分派人
            String assignuseraccount = DataFormatUtil.parseProperties("assignuseraccount");
            String username = "";
            String userid = "";
            if (!"".equals(assignuseraccount)) {
                Map<String, Object> uservo = userMapper.getUserInfoByUserAccount(assignuseraccount);
                if (uservo != null) {
                    username = uservo.get("name") != null ? uservo.get("name").toString() : "";
                    userid =  uservo.get("id") != null ? uservo.get("id").toString() : "";
                }
            }
            //有巡查人员时  存一条分配记录
            //添加处理人信息
            Calendar calendar = Calendar.getInstance();

            TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
            taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());
            taskFlowRecordInfo.setFkTaskid(obj.getPkTaskid());
            taskFlowRecordInfo.setFkTaskhandleuserid(userid);
            taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName().toString());
            taskFlowRecordInfo.setFkTasktype(obj.getFkTasktype());
            taskFlowRecordInfo.setTaskhandletime(calendar.getTime());
            taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
            //修改任务状态为待处理
            obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());//任务状态
            obj.setUpdateuser(username);//更新人
            obj.setUpdatetime(new Date());//更新时间
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
            //保存待处理记录
            //在当前时间的基础上添加一秒
            calendar.add(Calendar.SECOND, 1);
            for (String uid : userids) {
                //添加待处理人信息
                TaskFlowRecordInfoVO obj1 = new TaskFlowRecordInfoVO();
                obj1.setPkId(UUID.randomUUID().toString());
                obj1.setFkTaskid(obj.getPkTaskid());
                obj1.setFkTaskhandleuserid(uid);
                obj1.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName().toString());
                obj1.setFkTasktype(obj.getFkTasktype());
                obj1.setTaskhandletime(calendar.getTime());
                taskFlowRecordInfoMapper.insert(obj1);
            }
        }
    }

    @Override
    public Long getAlarmTaskNumByMonitorTimes(Map<String, Object> param) {
        //获取总条数
        Long countall = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoCountByParams(param);
        return countall;
    }



    @Override
    public List<Map<String, Object>> countUserTaskInfoByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countUserTaskInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> countTaskDataByParamGroupByTaskType(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskDataByParamGroupByTaskType(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTodayTaskInfoByTaskType(Map<String, Object> param) {
        return alarmTaskDisposeManagementMapper.getTodayTaskInfoByTaskType(param);
    }


    /**
     * @author: xsm
     * @date: 2020/09/25 0025 下午 13:42
     * @Description: 添加任务转办流程信息
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addTransferTaskInfo(Map<String, Object> paramMap) {
        String taskid = paramMap.get("taskid").toString();
        String taskremark = paramMap.get("taskremark").toString();
        List<String> userids = (List<String>) paramMap.get("userids");
        String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeManagementMapper.selectByPrimaryKey(taskid);
        //alarmTaskDisposeManagement.setTaskremark(taskremark);
        alarmTaskDisposeManagement.setUpdateuser(username);//更新人
        alarmTaskDisposeManagement.setUpdatetime(new Date());//更新时间
        alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
        Calendar calendar = Calendar.getInstance();
        //添加转办任务人的评论信息
        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
        obj.setPkId(UUID.randomUUID().toString());
        obj.setFkTaskid(taskid);
        obj.setFkTaskhandleuserid(userId);
        obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.TransferEnum.getName().toString());
        obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
        obj.setTaskhandletime(calendar.getTime());
        obj.setTaskcomment(taskremark);
        taskFlowRecordInfoMapper.insert(obj);
        //在当前时间的基础上添加一秒
        calendar.add(Calendar.SECOND, 1);
        for (String uid : userids) {
            //添加待处理人信息
            TaskFlowRecordInfoVO obj1 = new TaskFlowRecordInfoVO();
            obj1.setPkId(UUID.randomUUID().toString());
            obj1.setFkTaskid(taskid);
            obj1.setFkTaskhandleuserid(uid);
            obj1.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.TransferTaskEnum.getName().toString());
            obj1.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj1.setTaskhandletime(calendar.getTime());
            taskFlowRecordInfoMapper.insert(obj1);
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/1/25 0025 下午 5:00
     * @Description: 获取监测点报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getMonitrpointTaskInfoByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> monitrpointTaskInfoByParamMap = alarmTaskDisposeManagementMapper.getMonitrpointTaskInfoByParamMap(paramMap);
        String userid = paramMap.get("userid") == null ? "" : paramMap.get("userid").toString();
        String user = paramMap.get("userid") == null ? "" : paramMap.get("userid").toString();

        if (monitrpointTaskInfoByParamMap.size() == 0) {
            return monitrpointTaskInfoByParamMap;
        }

        paramMap.put("tablename", "PUB_CODE_PollutantFactor");
        paramMap.put("wherestring", "PollutantType in ('12','9','10','33') and isused=1");
        List<Map<String, Object>> pollutants = pubCodeMapper.getPubCodeDataByParam(paramMap);
        Map<String, String> PollutantMap = pollutants.stream().filter(m -> m.get("Code") != null && m.get("Name") != null).collect(Collectors.toMap(m -> m.get("Code").toString(), m -> m.get("Name").toString(), BinaryOperator.maxBy(String::compareTo)));


        List<String> mnlist = monitrpointTaskInfoByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
        Date date = new Date();
        Date maxdate = monitrpointTaskInfoByParamMap.stream().filter(m -> m.get("taskcreatetime") != null).map(m -> DataFormatUtil.getDateYMDHMS(m.get("taskcreatetime").toString())).max(Date::compareTo).orElse(date);
        Date mindate = monitrpointTaskInfoByParamMap.stream().filter(m -> m.get("taskcreatetime") != null).map(m -> DataFormatUtil.getDateYMDHMS(m.get("taskcreatetime").toString())).min(Date::compareTo).orElse(date);

        Criteria criteria = Criteria.where("DataGatherCode").in(mnlist).and("OverTime").lte(maxdate).gte(mindate);
        List<Document> documents = mongoTemplate.aggregate(newAggregation(match(criteria)), overData_db, Document.class).getMappedResults();
        Map<String, List<Document>> collect = documents.stream().filter(m -> m.get("DataGatherCode") != null && m.get("OverTime") != null).peek(m -> m.put("OverTime", FormatUtils.formatCSTString(m.get("OverTime").toString(), "yyyy-MM-dd HH:mm:ss"))).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));


        Iterator<Map<String, Object>> iterator = monitrpointTaskInfoByParamMap.iterator();
        while (iterator.hasNext()) {
            String pollutantnames = "";
            Map<String, Object> stringObjectMap = iterator.next();
            Set<Map<String, Object>> users = (stringObjectMap.get("users") == null && !"".equals(stringObjectMap.get("users").toString())) ? new HashSet<>() : (Set<Map<String, Object>>) stringObjectMap.get("users");
            String dgimn = stringObjectMap.get("dgimn") == null ? "" : stringObjectMap.get("dgimn").toString();
            long times = stringObjectMap.get("taskcreatetime") == null ? 0 : DataFormatUtil.getDateYMDHMS(stringObjectMap.get("taskcreatetime").toString()).getTime();
            if (!collect.isEmpty()) {
                pollutantnames = collect.get(dgimn).stream().filter(m -> m.get("PollutantCode") != null && m.get("OverTime") != null &&
                        DataFormatUtil.getDateYMDHMS(m.get("OverTime").toString()).getTime() == times)
                        .map(m -> PollutantMap.get(m.get("PollutantCode").toString())).distinct().collect(Collectors.joining("、"));
            }
            boolean contains = users.stream().filter(m -> m.get("userid") != null).map(m -> m.get("userid").toString()).collect(Collectors.joining("、")).contains(userid);
            boolean ishaveauth = users.stream().filter(m -> m.get("userid") != null).map(m -> m.get("userid").toString()).collect(Collectors.joining("、")).contains(user);
            stringObjectMap.put("ishaveauth", true);
            if (users.size() > 0 && !ishaveauth) {
                stringObjectMap.put("ishaveauth", false);
            }

            //如果userid不为null 则需要设置权限 查询自己及处置人为null的数据
            if (StringUtils.isNotBlank(userid)) {
                //如果用户数据不为空,并且不包含自己
                if (users.size() > 0 && !contains) {
                    iterator.remove();
                } else {
                    stringObjectMap.put("pollutantnames", pollutantnames);
                    stringObjectMap.put("users", users.stream().filter(m -> m.get("username") != null).map(m -> m.get("username").toString()).collect(Collectors.joining("、")));
                }
            } else {
                //查询所有数据
                stringObjectMap.put("pollutantnames", pollutantnames);
                stringObjectMap.put("users", users.stream().filter(m -> m.get("username") != null).map(m -> m.get("username").toString()).collect(Collectors.joining("、")));
            }
        }

        return monitrpointTaskInfoByParamMap;
    }


    /**
     * @author: chengzq
     * @date: 2021/1/25 0025 下午 6:45
     * @Description: 统计用户监测点报警任务个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Integer CountMonitrpointTaskInfoByParamMap(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("user", userid);
        paramMap.put("taskstatus", PendingEnum.getCode());
        return getMonitrpointTaskInfoByParamMap(paramMap).size();
    }


    @Override
    public List<Map<String, Object>> getMonitorPointLastTaskInfoByTaskType(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        result = alarmTaskDisposeManagementMapper.getMonitorPointLastTaskInfoByTaskType(param);
        if (result != null && result.size() > 0) {
            for (Map<String, Object> map : result) {
                if (map.get("alarmcodes") != null) {
                    String alarmcodes = map.get("alarmcodes").toString();
                    String[] onecodes = alarmcodes.split(",");
                    map.put("alarmcodes", onecodes);
                }
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2021/03/18 0018 下午 13:32
     * @Description: 添加报警任务的报警污染物
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addTaskPollutantInfo(List<String> addcode, Object taskid, String tasktype) {
        if (addcode != null && addcode.size() > 0) {
            for (String code : addcode) {
                TaskAlarmPollutantInfoVO objvo = new TaskAlarmPollutantInfoVO();
                objvo.setPkId(UUID.randomUUID().toString());
                objvo.setFkTaskid(taskid.toString());
                if (tasktype.equals(DevOpsTaskEnum.getCode().toString())) {
                    //异常污染物分隔code和异常类型  例：006_1
                    String[] onecodes = code.split("_");
                    objvo.setFkPolluantcode(onecodes[0]);
                    objvo.setAlarmtype(onecodes[1]);
                } else {
                    objvo.setFkPolluantcode(code);
                }
                objvo.setFkTasktype(tasktype);
                objvo.setUpdatetime(new Date());
                taskAlarmPollutantInfoMapper.insertSelective(objvo);
            }
        }
    }

    @Override
    public void insertTaskFlowRecordInfoVO(TaskFlowRecordInfoVO obj) {
        taskFlowRecordInfoMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2021/03/10 0010 下午 15:26
     * @Description: 统计某类型任务未分派数量
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countUnassignedTaskDataNum(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countUnassignedTaskDataNum(paramMap);
    }


    @Override
    public List<Map<String, Object>> countOtherStatusTaskDataNumForUserID(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countOtherStatusTaskDataNumForUserID(paramMap);
    }

    @Override
    public List<Map<String, Object>> getOwnTaskDisposeInfoByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist =  alarmTaskDisposeManagementMapper.getOwnTaskDisposeInfoByParamMap(paramMap);
        return datalist;
    }

    @Override
    public List<Map<String, Object>> getOwnPetitionByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getOwnPetitionByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> countAlarmTaskByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countAlarmTaskByParamMap(paramMap);
    }

    @Override
    public void updateByUseridAndTaskid(Map<String, Object> paramMap) {
        alarmTaskDisposeManagementMapper.updateByUseridAndTaskid(paramMap);
    }

    @Override
    public List<String> getUseridsByTaskid(String taskid) {
        return alarmTaskDisposeManagementMapper.getUseridsByTaskid(taskid);
    }

    /**
     * @author: xsm
     * @date: 2021/03/18 0018 上午 11:06
     * @Description: 获取点位最新报警任务且更新报警任务最新结束时间
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPointLastTaskInfoByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        try {
            String endtime = "";
            if (paramMap.get("taskendtime") != null) {
                endtime = paramMap.get("taskendtime").toString();
            }
            String datatype = "";
            if (paramMap.get("datatype") != null) {
                datatype = paramMap.get("datatype").toString();
            }
            String dgimn = "";
            if (paramMap.get("dgimn") != null) {
                dgimn = paramMap.get("dgimn").toString();
            }
            Map<String, Object> overlevelmap = new HashMap<>();
            if (paramMap.get("overlevelmap")!=null){
                overlevelmap = (Map<String, Object>) paramMap.get("overlevelmap");
            }
            String overlevelcode ="";
            if (paramMap.get("overlevelcode")!=null){
                overlevelcode = paramMap.get("overlevelcode").toString();
            }
            List<String> overcodes =(List<String>) paramMap.get("overcodes");
            resultmap = alarmTaskDisposeManagementMapper.getPointLastTaskInfoByParamMap(paramMap);
            if (resultmap!=null) {//有最新一条任务
                //报警污染物
                if (resultmap.get("alarmcodes") != null) {
                    String alarmcodes = resultmap.get("alarmcodes").toString();
                    String[] onecodes = alarmcodes.split(",");
                    List<String> list = Arrays.asList(onecodes);
                    resultmap.put("alarmcodes", list);
                }
                //当最新任务不为空 报警开始时间不为空  最新任务的报警结束时间不为空
                if (!"".equals(endtime) && resultmap != null && resultmap.get("taskid") != null && resultmap.get("taskendtime") != null) {
                    //比较最新任务的报警结束时间 和当前报警数据的报警开始时间
                    if (compare(resultmap.get("taskendtime").toString(), endtime)) {
                        //当结束时间小于当前报警时间时
                        //判断其是否连续任务
                        if ("RealTimeData".equals(datatype)||"MinuteData".equals(datatype)){
                            //根据上一个报警任务的报警结束时间  到 现在报警数据的开始报警时间  之内 有无正常数据 来判断是否连续
                            boolean iscontinue = getAlarmDataIsContinue(datatype, dgimn, resultmap.get("taskendtime").toString(), endtime,paramMap.get("tasktype"));
                            //当结束时间小于当前报警开始时间时  更新结束时间
                            //替换任务的最新结束时间
                            if (iscontinue==true){//为true  则是连续报警 更新时间
                                //超标报警  还需判断报警级别 若当前报警级别大于任务报警级别 则需新生成
                                if (paramMap.get("tasktype")!=null&&AlarmTaskEnum.getCode().toString().equals(paramMap.get("tasktype").toString())){
                                    boolean tj_or_xg = false;
                                    if (!"".equals(overlevelcode)&&resultmap.get("overlevelcode")!=null&&!"".equals(resultmap.get("overlevelcode").toString())){
                                        //比较超标级别
                                        String oldlevel = resultmap.get("overlevelcode").toString();
                                        if (overlevelmap.get(overlevelcode)!=null&&overlevelmap.get(oldlevel)!=null){//比较时间
                                            if (Integer.parseInt(overlevelmap.get(overlevelcode).toString())>Integer.parseInt(overlevelmap.get(oldlevel).toString())){
                                                //若最新报警数据的报警级别大于redis中的该点位最新任务的报警级别 则新增
                                                tj_or_xg = true;
                                            }
                                        }
                                    }
                                    if (tj_or_xg){//返回空值 新增任务
                                        resultmap = null;
                                    }else{//判断是否有新报警污染物 有则添加
                                        AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(resultmap.get("taskid").toString());
                                        obj.setTaskendtime(endtime);
                                        List<String> codes = new ArrayList<>();
                                        if (resultmap.get("alarmcodes")!=null){
                                            codes = new ArrayList((List<String>) resultmap.get("alarmcodes"));
                                        }
                                        List<String> addcode = new ArrayList<>();
                                        if (codes != null&&codes.size()>0&& overcodes != null&&overcodes.size()>0) {
                                            for (String code : overcodes) {
                                                if (!codes.contains(code)) {
                                                    addcode.add(code);
                                                    codes.add(code);
                                                }
                                            }
                                        } else {
                                            addcode = overcodes;
                                            codes = overcodes;
                                        }
                                        resultmap.put("alarmcodes",codes);
                                        addTaskPollutantInfo(addcode,resultmap.get("taskid"),paramMap.get("tasktype").toString());
                                        alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
                                    }
                                }else{//判断是否有新报警污染物 有则添加
                                    AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(resultmap.get("taskid").toString());
                                    obj.setTaskendtime(endtime);
                                    List<String> codes = resultmap.get("alarmcodes")!=null? new ArrayList((List<String>) resultmap.get("alarmcodes")):null;
                                    List<String> addcode = new ArrayList<>();
                                    if (codes != null) {
                                        for (String code : overcodes) {
                                            if (!codes.contains(code)) {
                                                addcode.add(code);
                                                codes.add(code);
                                            }
                                        }
                                    } else {
                                        addcode = overcodes;
                                        codes = overcodes;
                                    }
                                    resultmap.put("alarmcodes",codes);
                                    addTaskPollutantInfo(addcode,resultmap.get("taskid"),paramMap.get("tasktype").toString());
                                    alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
                                }
                            }else{//非连续报警则反null
                                resultmap = null;
                            }
                        }else{//小时类型 不需要再次进行判断
                            AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(resultmap.get("taskid").toString());
                            obj.setTaskendtime(endtime);
                            alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2021/12/09 0009 下午 13:50
     * @Description: 获取点位最新报警任务且更新报警任务最新结束时间（新）
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:级别有变化时 不重新生成  只更新（级别低到高时）
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getNewPointLastTaskInfoByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        try {
            String endtime = "";
            if (paramMap.get("taskendtime") != null) {
                endtime = paramMap.get("taskendtime").toString();
            }
            String starttime = "";
            if (paramMap.get("taskstarttime") != null) {
                starttime = paramMap.get("taskstarttime").toString();
            }
            String datatype = "";
            if (paramMap.get("datatype") != null) {
                datatype = paramMap.get("datatype").toString();
            }
            String dgimn = "";
            if (paramMap.get("dgimn") != null) {
                dgimn = paramMap.get("dgimn").toString();
            }
            Map<String, Object> overlevelmap = new HashMap<>();
            if (paramMap.get("overlevelmap")!=null){
                overlevelmap = (Map<String, Object>) paramMap.get("overlevelmap");
            }
            String overlevelcode ="";
            if (paramMap.get("overlevelcode")!=null){
                overlevelcode = paramMap.get("overlevelcode").toString();
            }
            String recoverytime ="";
            if (paramMap.get("recoverytime")!=null){
                recoverytime = paramMap.get("recoverytime").toString();
            }
            List<String> overcodes =(List<String>) paramMap.get("overcodes");
            resultmap = alarmTaskDisposeManagementMapper.getPointLastTaskInfoByParamMap(paramMap);
            if (resultmap!=null) {//有最新一条任务
                //报警污染物
                if (resultmap.get("alarmcodes") != null) {
                    String alarmcodes = resultmap.get("alarmcodes").toString();
                    String[] onecodes = alarmcodes.split(",");
                    List<String> list = Arrays.asList(onecodes);
                    resultmap.put("alarmcodes", list);
                }
                //当最新任务不为空 报警开始时间不为空  最新任务的报警结束时间不为空
                if (!"".equals(endtime) && resultmap != null && resultmap.get("taskid") != null && resultmap.get("taskendtime") != null) {
                    //比较最新任务的报警结束时间 和当前报警数据的报警结束时间
                    if (compare(resultmap.get("taskendtime").toString(), endtime)) {
                        //当结束时间小于当前报警结束时间时
                        //判断其是否连续任务
                        if ("RealTimeData".equals(datatype)||"MinuteData".equals(datatype)){
                            //根据上一个报警任务的报警结束时间  到 现在报警数据的开始报警时间  之内 有无正常数据 来判断是否连续
                            boolean iscontinue = getAlarmDataIsContinue(datatype, dgimn, resultmap.get("taskendtime").toString(), starttime,paramMap.get("tasktype"));
                            //当结束时间小于当前报警开始时间时  更新结束时间
                            //替换任务的最新结束时间
                            if (iscontinue==true){//为true  则是连续报警 更新时间
                                //超标报警  还需判断报警级别 若当前报警级别大于任务报警级别 则需新生成
                                if (paramMap.get("tasktype")!=null&&AlarmTaskEnum.getCode().toString().equals(paramMap.get("tasktype").toString())){
                                    boolean tj_or_xg = false;
                                    if (!"".equals(overlevelcode)&&resultmap.get("overlevelcode")!=null&&!"".equals(resultmap.get("overlevelcode").toString())){
                                        //比较超标级别
                                        String oldlevel = resultmap.get("overlevelcode").toString();
                                        if (overlevelmap.get(overlevelcode)!=null&&overlevelmap.get(oldlevel)!=null){//比较时间
                                            if (Integer.parseInt(overlevelmap.get(overlevelcode).toString())>Integer.parseInt(overlevelmap.get(oldlevel).toString())){
                                                //若最新报警数据的报警级别大于redis中的该点位最新任务的报警级别 则更新级别
                                                tj_or_xg = true;
                                            }
                                        }
                                    }
                                    AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(resultmap.get("taskid").toString());
                                    obj.setTaskendtime(endtime);
                                    obj.setUpdatetime(new Date());
                                    if (!"".equals(recoverytime)){
                                        obj.setRecoverystatus((short)1);
                                        obj.setRecoverytime(DataFormatUtil.getDateYMDHMS(recoverytime));
                                    }else{
                                        //连续报警
                                        //恢复时间为空
                                        //置空恢复状态和时间
                                        obj.setRecoverytime(null);
                                        obj.setRecoverystatus((short)0);
                                    }
                                    if (tj_or_xg){//有级别变化  更新级别
                                        resultmap.put("overlevelcode",overlevelcode);
                                        obj.setOverlevelcode(overlevelcode+"");
                                    }
                                    //判断是否有新报警污染物 有则添加
                                        List<String> codes = new ArrayList<>();
                                        if (resultmap.get("alarmcodes")!=null){
                                            codes = new ArrayList((List<String>) resultmap.get("alarmcodes"));
                                        }
                                        List<String> addcode = new ArrayList<>();
                                        if (codes != null&&codes.size()>0&& overcodes != null&&overcodes.size()>0) {
                                            for (String code : overcodes) {
                                                if (!codes.contains(code)) {
                                                    addcode.add(code);
                                                    codes.add(code);
                                                }
                                            }
                                        } else {
                                            addcode = overcodes;
                                            codes = overcodes;
                                        }
                                        resultmap.put("alarmcodes",codes);
                                        addTaskPollutantInfo(addcode,resultmap.get("taskid"),paramMap.get("tasktype").toString());
                                        alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);

                                }else{//判断是否有新报警污染物 有则添加
                                    AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(resultmap.get("taskid").toString());
                                    obj.setTaskendtime(endtime);
                                    List<String> codes = resultmap.get("alarmcodes")!=null?new ArrayList((List<String>) resultmap.get("alarmcodes")):null;
                                    List<String> addcode = new ArrayList<>();
                                    if (codes != null) {
                                        for (String code : overcodes) {
                                            if (!codes.contains(code)) {
                                                addcode.add(code);
                                                codes.add(code);
                                            }
                                        }
                                    } else {
                                        addcode = overcodes;
                                        codes = overcodes;
                                    }
                                    resultmap.put("alarmcodes",codes);
                                    addTaskPollutantInfo(addcode,resultmap.get("taskid"),paramMap.get("tasktype").toString());
                                    alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
                                }
                            }else{//非连续报警则反null
                                resultmap = null;
                            }
                        }else{//小时类型 不需要再次进行判断
                            AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(resultmap.get("taskid").toString());
                            obj.setTaskendtime(endtime);
                            alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultmap;
    }

    /**
     * 统计报警、运维 抄送给自己的任务
     * */
    @Override
    public List<Map<String, Object>> countCarbonCopyTaskDataNum(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countCarbonCopyTaskDataNum(paramMap);
    }

    @Override
    public List<Map<String, Object>> countOtherStatusAlarmTaskNumByUserID(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countOtherStatusAlarmTaskNumByUserID(paramMap);
    }

    @Override
    public List<Map<String, Object>> getUnassignedTaskDisposeInfoByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist =  alarmTaskDisposeManagementMapper.getUnassignedTaskDisposeInfoByParamMap(paramMap);
        if(datalist!=null&&datalist.size()>0){
            Date now = new Date();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            //未分派任务时限
            String unallocatedtime = DataFormatUtil.parseProperties("unallocatedtime.minute");//报警任务
            String taskstarttime;
            String taskcreatetime;
            String taskendtime;
            List<String> taskids = datalist.stream().filter(m -> m.get("pk_taskid") != null).map(m -> m.get("pk_taskid").toString()).collect(Collectors.toList());
            paramMap.put("taskids", taskids);
            List<Map<String, Object>> ownTaskDisposeInfoByParamMap = alarmTaskDisposeManagementMapper.getUnassignedTaskPollutantDataByParamMap(paramMap);
            Map<String, List<Map<String, Object>>> lc_map = ownTaskDisposeInfoByParamMap.stream().collect(Collectors.groupingBy(m -> m.get("pk_taskid").toString()));
            for(Map<String, Object> map:datalist){
                Map<String, Object> stringObjectMap = lc_map.get(map.get("pk_taskid").toString()).get(0);
                Set<Map<String, Object>> pollutants = stringObjectMap.get("pollutants") == null ? new HashSet<>() : (Set<Map<String, Object>>) stringObjectMap.get("pollutants");
                Map<String, List<Map<String, Object>>> collect = pollutants.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
                //组装污染物数据
                List<String> polltantnameslist = new ArrayList<>();
                for (String pollutantname : collect.keySet()) {
                    String alarmtypes = collect.get(pollutantname).stream().filter(m -> m.get("alarmtype") != null).map(m -> CommonTypeEnum.ExceptionTypeEnum.getNameByCode(m.get("alarmtype").toString())).collect(Collectors.joining("、"));
                    if (StringUtils.isNotBlank(alarmtypes)) {
                        polltantnameslist.add(pollutantname + "【" + alarmtypes + "】");
                    } else {
                        polltantnameslist.add(pollutantname);
                    }
                }
                map.put("pollutantname", polltantnameslist.stream().collect(Collectors.joining("、")));
                //设置是否超时
                 taskstarttime = stringObjectMap.get("alarmstarttime") == null ? "" : stringObjectMap.get("alarmstarttime").toString();
                 taskcreatetime = stringObjectMap.get("taskcreatetime") == null ? "" : stringObjectMap.get("taskcreatetime").toString();
                 taskendtime = stringObjectMap.get("taskendtime") == null ? "" : stringObjectMap.get("taskendtime").toString();
                if (StringUtils.isNotBlank(taskcreatetime)) {
                    Date unallocatedtimePlus = FormatUtils.getDateYMDHMSPlus(taskcreatetime, Integer.valueOf(unallocatedtime) * 60);
                    //如果超过未分派任务时限并且未分派任务设置    超时未分派
                    if (unallocatedtimePlus.getTime() < now.getTime()) {
                        map.put("unallocatedflag", 1); //是否超时未分派 1是，0否
                    }
                }
                //设置持续时间
                if (StringUtils.isNotBlank(taskendtime) && StringUtils.isNotBlank(taskstarttime)) {
                    long time = DataFormatUtil.getDateYMDHM(taskendtime).getTime() - DataFormatUtil.getDateYMDHM(taskstarttime).getTime();
                    map.put("Duration", decimalFormat.format(Double.valueOf(time) / 1000 / 60 / 60));
                }else{
                    map.put("Duration", "0");
                }
            }
        }

        return datalist;
    }

    @Override
    public List<Map<String, Object>> getOwnTaskDisposeIdsByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = alarmTaskDisposeManagementMapper.getOwnTaskDisposeIdsByParamMap(paramMap);
        String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        if (datalist != null && datalist.size() > 0) {
            Date now = new Date();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            //未分派任务时限
            String unallocatedtime = DataFormatUtil.parseProperties("unallocatedtime.minute");//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            String taskstatus;
            String taskstarttime;
            String taskcreatetime;
            String taskendtime;
            List<String> taskids = datalist.stream().filter(m -> m.get("pk_taskid") != null).map(m -> m.get("pk_taskid").toString()).collect(Collectors.toList());
            paramMap.put("taskids", taskids);
            List<Map<String, Object>> ownTaskDisposeInfoByParamMap = alarmTaskDisposeManagementMapper.getOwnTaskDisposeInfoByParamMap(paramMap);
            Map<String, List<Map<String, Object>>> lc_map = ownTaskDisposeInfoByParamMap.stream().collect(Collectors.groupingBy(m -> m.get("pk_taskid").toString()));
            for (Map<String, Object> onemap : datalist) {
                Map<String, Object> stringObjectMap = lc_map.get(onemap.get("pk_taskid").toString()).get(0);
                List<Map<String, Object>> list = stringObjectMap.get("taskrecordlist") == null ? new ArrayList<>() : (List<Map<String, Object>>) stringObjectMap.get("taskrecordlist");
                Set<Map<String, Object>> pollutants = stringObjectMap.get("pollutants") == null ? new HashSet<>() : (Set<Map<String, Object>>) stringObjectMap.get("pollutants");
                taskstatus = stringObjectMap.get("taskstatus") == null ? "" : stringObjectMap.get("taskstatus").toString();
                taskstarttime = stringObjectMap.get("alarmstarttime") == null ? "" : stringObjectMap.get("alarmstarttime").toString();
                taskcreatetime = stringObjectMap.get("taskcreatetime") == null ? "" : stringObjectMap.get("taskcreatetime").toString();
                taskendtime = stringObjectMap.get("taskendtime") == null ? "" : stringObjectMap.get("taskendtime").toString();
                Map<String, Object> fenpairenMap = list.stream().filter(m -> m.get("currenttaskstatus") != null
                        && "分派任务".equals(m.get("currenttaskstatus").toString())).findFirst().orElse(new HashMap<>());
                Set<String> scuserlist = (Set<String>) list.stream().filter(m -> m.get("currenttaskstatus") != null && "审核".equals(m.get("currenttaskstatus").toString())).findFirst().orElse(new HashMap<>()).get("users");
                Map<String, Object> complateMap = list.stream().filter(m -> m.get("currenttaskstatus") != null
                        && ("完成".equals(m.get("currenttaskstatus").toString())
                        || "已完成".equals(m.get("currenttaskstatus").toString())
                        || "反馈信息".equals(m.get("currenttaskstatus").toString())
                        || "忽略任务".equals(m.get("currenttaskstatus").toString()))).findFirst().orElse(new HashMap<>());
                Set<String> zhixinguserlist = new HashSet<>();
                if (list.stream().filter(n -> n.get("currenttaskstatus") != null && "转办".equals(n.get("currenttaskstatus").toString())).count() > 0) {
                    String max = list.stream().filter(n -> n.get("currenttaskstatus") != null && n.get("statusandtime") != null && "转办".equals(n.get("currenttaskstatus").toString())).map(m -> m.get("statusandtime").toString()).max(String::compareTo).orElse("");
                    zhixinguserlist = list.stream().filter(n -> n.get("currenttaskstatus") != null && n.get("statusandtime") != null && n.get("users") != null && "转办".equals(n.get("currenttaskstatus").toString())
                            && max.equals(n.get("statusandtime").toString())).flatMap(m -> ((Set<String>) m.get("users")).stream()).collect(Collectors.toSet());
                } else {
                    zhixinguserlist = (Set<String>) list.stream().filter(m -> m.get("currenttaskstatus") != null && "待处理".equals(m.get("currenttaskstatus").toString())&&m.get("TaskComment")==null).findFirst().orElse(new HashMap<>()).get("users");
                }
                List<Map<String, Object>> copylist = list.stream().filter(m -> m.get("currenttaskstatus") != null
                        && ("抄送已读".equals(m.get("currenttaskstatus").toString())
                        || "抄送".equals(m.get("currenttaskstatus").toString()))).collect(Collectors.toList());
                String taskcomments = list.stream().filter(m -> m.get("currenttaskstatus") != null && m.get("taskcomment") != null
                        && !"转办".equals(m.get("currenttaskstatus").toString())
                        && !"待处理".equals(m.get("currenttaskstatus").toString())
                        && !"处理中".equals(m.get("currenttaskstatus").toString())).map(m -> m.get("taskcomment").toString()).collect(Collectors.joining("、"));
                String fenpaitime = "";
                onemap.put("fenpaitime", fenpaitime);
                //分派人
                if (fenpairenMap != null && !"0".equals(taskstatus)) {
                    fenpaitime = fenpairenMap.get("taskhandletime") == null ? "" : fenpairenMap.get("taskhandletime").toString();
                    Set<String> fenpaiuserlist = fenpairenMap.get("users") == null ? new HashSet<>() : (Set<String>) fenpairenMap.get("users");
                    String user = fenpaiuserlist.stream().findFirst().orElse("");
                    onemap.put("fenpaitime", fenpaitime);
                    onemap.put("fenpairen", user);
                }
                //审核人
                if (scuserlist != null && !"0".equals(taskstatus)) {
                    String user = scuserlist.stream().collect(Collectors.joining("、"));
                    onemap.put("shenchauser", user);
                    if (user.contains(username)) {
                        onemap.put("isscuser", "1");//能否反馈 1是0否
                    } else {
                        onemap.put("isscuser", 0);
                    }
                } else {
                    onemap.put("shenchauser", "");
                    onemap.put("isscuser", 0);
                }
                //设置是否超时
                if (StringUtils.isNotBlank(taskcreatetime)) {
                    Date unallocatedtimePlus = FormatUtils.getDateYMDHMSPlus(taskcreatetime, Integer.valueOf(unallocatedtime) * 60);
                    //如果超过未分派任务时限并且未分派任务设置    超时未分派
                    if (unallocatedtimePlus.getTime() < now.getTime() && fenpairenMap.size() == 0) {
                        onemap.put("unallocatedflag", 1); //是否超时未分派 1是，0否
                    } else {
                        onemap.put("unallocatedflag", 0);
                        if (StringUtils.isNotBlank(fenpaitime)) {
                            Date uncompletetimePlus = FormatUtils.getDateYMDHMSPlus(fenpaitime, Integer.valueOf(uncompletetime) * 60);
                            //如果超过未完成任务时限并且没有任何处置人或转办人评论
                            if (uncompletetimePlus.getTime() < now.getTime() && StringUtils.isBlank(taskcomments)) {
                                onemap.put("uncompleteflag", 1); //是否超时未完成 1是，0否
                            } else {
                                onemap.put("uncompleteflag", 0);
                            }
                        }
                    }
                }

                //设置持续时间
                if (StringUtils.isNotBlank(taskendtime) && StringUtils.isNotBlank(taskstarttime)) {
                    long time = DataFormatUtil.getDateYMDHM(taskendtime).getTime() - DataFormatUtil.getDateYMDHM(taskstarttime).getTime();
                    onemap.put("Duration", decimalFormat.format(Double.valueOf(time) / 1000 / 60 / 60));
                } else {
                    onemap.put("Duration", "0");
                }


                //抄送列表设置抄送状态，区分已读未读
                if (copylist.size() > 0) {
                    String copystr = copylist.stream().filter(map -> map.get("currenttaskstatus") != null && map.get("users") != null && ("抄送已读").equals(map.get("currenttaskstatus")) &&
                            ((Set<String>) map.get("users")).contains(username)).map(m -> m.get("currenttaskstatus").toString()).findFirst().orElse("抄送");
                    onemap.put("currenttaskstatus", copystr);
                }


                //执行人
                if (zhixinguserlist != null && !"0".equals(taskstatus)) {
                    String user = zhixinguserlist.stream().collect(Collectors.joining("、"));
                    onemap.put("zhixingren", user);
                    if (user.contains(username)) {
                        onemap.put("isfeedback", 1);//能否反馈 1是0否
                    } else {
                        onemap.put("isfeedback", 0);
                    }
                } else {
                    onemap.put("zhixingren", "");
                    onemap.put("isfeedback", 0);
                }

                Map<String, List<Map<String, Object>>> collect = pollutants.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
                //组装污染物数据
                List<String> polltantnameslist = new ArrayList<>();
                for (String pollutantname : collect.keySet()) {
                    String alarmtypes = collect.get(pollutantname).stream().filter(m -> m.get("alarmtype") != null).map(m -> CommonTypeEnum.ExceptionTypeEnum.getNameByCode(m.get("alarmtype").toString())).collect(Collectors.joining("、"));
                    if (StringUtils.isNotBlank(alarmtypes)) {
                        polltantnameslist.add(pollutantname + "【" + alarmtypes + "】");
                    } else {
                        polltantnameslist.add(pollutantname);
                    }
                }
                onemap.remove("taskrecordlist");
                onemap.put("pollutantname", polltantnameslist.stream().collect(Collectors.joining("、")));
                onemap.put("completetime", complateMap.get("taskhandletime"));
            }
        }
        return datalist;
    }

    private boolean getAlarmDataIsContinue(String datatype, String dgimn, String taskendtime, String endtime,Object tasktype) {
        boolean iscontinue = true;
        String collection = "";
        String listdatastr = "";
        long alarmcount = 0;
        long totalCount = 0;
        if ("RealTimeData".equals(datatype)){
            listdatastr = "RealDataList";
            collection = realData_db;
        }else if("MinuteData".equals(datatype)){
            listdatastr ="MinuteDataList";
            collection = minuteData_db;
        }

        if (!"".equals(datatype)&&!"".equals(dgimn)){
            Date startDate = DataFormatUtil.getDateYMDHMS(taskendtime);
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime);

            if (tasktype!=null&&AlarmTaskEnum.getCode().toString().equals(tasktype.toString())) {
                Criteria criteria = new Criteria();
                Criteria timecriteria = new Criteria();
                List<Criteria> criterialist = new ArrayList<>();
                criterialist.add(Criteria.where(listdatastr + ".IsOver").gt(0));
                criterialist.add(Criteria.where(listdatastr + ".IsOverStandard").is(true));
                timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
                criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate).andOperator(timecriteria);
                //超标数据
                List<Map> result  = mongoTemplate.aggregate(newAggregation(
                        match(criteria), project("DataGatherCode", "MonitorTime")), collection, Map.class).getMappedResults();
                Query query = new Query();
                query.addCriteria(Criteria.where("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate));
                if (result!=null&&result.size()>0){
                    alarmcount =result.size();
                }
                 totalCount = mongoTemplate.count(query, collection);
                if (totalCount>0&&alarmcount>0){
                    if ((totalCount-alarmcount)>0){//有正常数据
                        iscontinue = false;
                    }
                }
            }else if(tasktype!=null&&DevOpsTaskEnum.getCode().toString().equals(tasktype.toString())){
                Query query = new Query();
                Query querytwo = new Query();
                query.addCriteria(Criteria.where("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate).and(listdatastr+".IsException").gt(0));
                querytwo.addCriteria(Criteria.where("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate));
                 alarmcount = mongoTemplate.count(query, collection);
                 totalCount = mongoTemplate.count(querytwo, collection);
                if (totalCount>0&&alarmcount>0){
                    if ((totalCount-alarmcount)>0){//有正常数据
                        iscontinue = false;
                    }
                }
            }else if(tasktype!=null&&ChangeAlarmTaskEnum.getCode().toString().equals(tasktype.toString())){
                Query query = new Query();
                Query querytwo = new Query();
                query.addCriteria(Criteria.where("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate).and(listdatastr+".IsSuddenChange").is(true));
                querytwo.addCriteria(Criteria.where("DataGatherCode").is(dgimn).and("MonitorTime").gt(startDate).lt(endDate));
                alarmcount = mongoTemplate.count(query, collection);
                totalCount = mongoTemplate.count(querytwo, collection);
                if (totalCount>0&&alarmcount>0){
                    if ((totalCount-alarmcount)>0){//有正常数据
                        iscontinue = false;
                    }
                }
            }
            //List<Document> documents = mongoTemplate.find(query, Document.class, collection);
            /*if (num>0){
                iscontinue = false;//有非报警 数据 则不是连续报警
            }*/
        }
        return iscontinue;
    }

    /**
     * @author: xsm
     * @date: 2021/03/18 0018 上午 11:06
     * @Description: 更新报警任务最新结束时间
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateAlarmTaskEndTime(Object taskid, String endtime) {
        if (taskid != null && !"".equals(endtime)) {
            AlarmTaskDisposeManagementVO obj = alarmTaskDisposeManagementMapper.selectByPrimaryKey(taskid.toString());
            obj.setTaskendtime(endtime);
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(obj);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/03/12 0012 上午 9:50
     * @Description: 获取首页今日工单报警任务
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllAlarmTaskByParamForHome(Map<String, Object> param) {
        List<Map<String, Object>> listdata = new ArrayList<>();
        try {
            listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(param);
            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            //获取分页后的任务ID
            List<String> taskids = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    String taskstatuname = null;
                    taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    map.put("tasktypename", "报警工单");
                    taskids.add(map.get("PK_TaskID").toString());
                    if (map.get("overtimenum") != null) {
                        int overtimenum = Integer.parseInt(map.get("overtimenum").toString());
                        map.put("alarmovertime", countHourMinuteTime(overtimenum));
                    }
                    String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                    if ("0".equals(status)) {
                        if (map.get("unallocatednum") != null) {
                            int unallocatednum = Integer.parseInt(map.get("unallocatednum").toString());
                            if (unallocatednum > unallocatedtime) {
                                map.put("unallocatedflag", "1");
                                int tatalnum = unallocatednum - unallocatedtime;
                                String str = countHourMinuteTime(tatalnum);
                                map.put("overunallocatedtime", str);
                            } else {
                                map.put("unallocatedflag", "0");
                            }
                        }
                    } else {
                        map.put("unallocatedflag", "0");
                    }
                    map.put("lastalarmtime", map.get("TaskEndTime"));
                }
                param.put("taskids", taskids);
                List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(param);
                //获取报警污染物数据
                List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(param);
                Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                //通过任务ID分组数据
                Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                for (Map<String, Object> map : listdata) {
                    String taskid = map.get("PK_TaskID").toString();
                    map.put("pollutantname",idandcodenames.get(taskid));
                    setTaskAllFlagData(param.get("feedbackuserid").toString(), uncompletetime, map, lc_map.get(taskid));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return listdata;
    }

    /**
     * @author: xsm
     * @date: 2021/04/21 0021 上午 9:50
     * @Description: 获取首页今日工单视频报警任务
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllVideoAlarmTaskByParamForHome(Map<String, Object> param) {
        List<Map<String, Object>> listdata = new ArrayList<>();
        try {
            listdata = alarmTaskDisposeManagementMapper.getAllVideoAlarmTaskByParamForHome(param);
            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            //获取分页后的任务ID
            List<String> taskids = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    String taskstatuname = null;
                    taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    map.put("tasktypename", "报警工单");
                    taskids.add(map.get("PK_TaskID").toString());
                    String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                    if ("0".equals(status)) {
                        if (map.get("unallocatednum") != null) {
                            int unallocatednum = Integer.parseInt(map.get("unallocatednum").toString());
                            if (unallocatednum > unallocatedtime) {
                                map.put("unallocatedflag", "1");
                                int tatalnum = unallocatednum - unallocatedtime;
                                String str = countHourMinuteTime(tatalnum);
                                map.put("overunallocatedtime", str);
                            } else {
                                map.put("unallocatedflag", "0");
                            }
                        }
                    } else {
                        map.put("unallocatedflag", "0");
                    }
                    map.put("lastalarmtime", map.get("TaskEndTime"));
                }
                param.put("taskids", taskids);
                List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(param);
                //通过任务ID分组数据
                Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                for (Map<String, Object> map : listdata) {
                    String taskid = map.get("PK_TaskID").toString();
                    setTaskAllFlagData(param.get("feedbackuserid").toString(), uncompletetime, map, lc_map.get(taskid));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return listdata;
    }

    @Override
    public Long countAllOverAlarmTaskCountByParams(Map<String, Object> param) {
        //获取总条数
        Long countall = alarmTaskDisposeManagementMapper.countAllOverAlarmTaskCountByParams(param);
        return countall;
    }

    @Override
    public List<Map<String, Object>> countSecurityAlarmTaskByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countSecurityAlarmTaskByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/19 0019 上午 8:51
     * @Description: 通过自定义条件查询日常任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAPPDaliyTaskByParamMap(JSONObject paramMap) {
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        paramMap.put("categorys", categorys);
        return alarmTaskDisposeManagementMapper.getAPPDaliyTaskByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> countCompletedStatusTaskDataNumForUserID(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countCompletedStatusTaskDataNumForUserID(paramMap);
    }

    @Override
    public Map<String, Object> getTaskInfoDataByTaskID(Map<String, Object> param) {
        return alarmTaskDisposeManagementMapper.getTaskInfoDataByTaskID(param);
    }

    @Override
    public List<Map<String, Object>> getDisposePersonSelectDataByParams(Map<String, Object> param) {
        return alarmTaskDisposeManagementMapper.getDisposePersonSelectDataByParams(param);
    }

    @Override
    public void addTextMessageDatas(List<TextMessageVO> listobj) {
        textMessageMapper.batchInsert(listobj);
    }

    @Override
    public Map<String, Object> getAssignmentTaskUserInfo(Map<String, Object> param) {
        return alarmTaskDisposeManagementMapper.getAssignmentTaskUserInfo(param);
    }

    @Override
    public List<Map<String, Object>> getAssignAndUndisposedTaskNumDataByParams(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getAssignAndUndisposedTaskNumDataByParams(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/12/03 0003 下午 1:03
     * @Description:  通过任务ID获取该任务已抄送的抄送人
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTaskCarbonCopyUsersByTaskID(Map<String, Object> paramMap) {
        return taskFlowRecordInfoMapper.getTaskCarbonCopyUsersByTaskID(paramMap);
    }

    @Override
    public void insertReviewedInfo(List<TaskFlowRecordInfoVO> objs) {
        taskFlowRecordInfoMapper.batchInsert(objs);
    }


    /**
     * @author: xsm
     * @date: 2019/9/02 0002 下午 6:24
     * @Description: 根据报警任务状态编码获取状态名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private String getStatusNameByStatusCode(String Code) throws ParseException {
        String name = "";
        if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode().toString()).equals(Code)) {
            //待分派
            name = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getName();
        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(Code)) {
            //待处理
            name = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getName();
        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(Code)) {
            //处理中
            name = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getName();
        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(Code)) {
            //已完成
            name = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getName();
        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString()).equals(Code)) {
            //已忽略
            name = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getName();
        }else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.ConfirmEndEnum.getCode().toString()).equals(Code)) {
            //待审查
            name = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.ConfirmEndEnum.getName();
        }
        return name;
    }

    private String countHourMinuteTime(int tatalnum) {
        String str = "";
        if (tatalnum < 60) {
            str = tatalnum + "分钟";
        } else if (tatalnum == 60) {
            str = "1小时";
        } else {
            int onenum = tatalnum / 60;
            str = onenum + "小时" + ((tatalnum - onenum * 60) > 0 ? (tatalnum - onenum * 60) + "分钟" : "");
        }
        return str;
    }

    @Override
    public Map<String, Object> getMonitorPointOverDataByParamMap(Map<String, Object> paramMap) {
        String dgimn = paramMap.get("dgimn") == null ? "" : paramMap.get("dgimn").toString();
        List<String> datatypes = new ArrayList<>();
        List<String> datatypenames = new ArrayList<>();
        if (paramMap.get("datatypes") != null) {
            datatypes = (List<String>) paramMap.get("datatypes");
        }
        List<Map<String, Object>> allpoints = getAllPollutionOutputDgimnInfoByParam(paramMap);
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        //判断是否有查询条件
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Map<String, Object> resultmap = new HashMap<>();
        Set<String> mnlist = new HashSet<String>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> unitmap = new HashMap<>();
        mnlist.add(dgimn);

        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                unitmap.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
            }
        }
        //构建Mongdb查询条件
        Query query = new Query();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("PollutantCode").in(codelist));
        query.addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
        if (datatypes.size()>0){
            for (String str : datatypes) {
                datatypenames.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
            }
            query.addCriteria(Criteria.where("DataType").in(datatypenames));
        }
        //总条数
        long totalCount = mongoTemplate.count(query, overData_db);
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        query.with(new Sort(Sort.Direction.ASC, "OverTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, overData_db);
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                Object standardmaxvalue = null;
                Object standardminvalue = null;
                Object alarmtype = null;
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                            alarmtype = objmap.get("AlarmType");
                            standardminvalue = objmap.get("StandardMinValue");
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            break;
                        }
                    }
                }
                for (Map<String, Object> objmap : allpoints) {
                    if (objmap.get("dgimn") != null && mn.equals(objmap.get("dgimn"))) {//当MN号相同时
                        Map<String, Object> result = new HashMap<>();
                        result.put("pollutionname", objmap.get("pollutionname"));
                        result.put("outputname", objmap.get("outputname"));
                        result.put("outputid", objmap.get("pk_id"));
                        if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getCode());
                            result.put("datatype", "实时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getCode());
                            result.put("datatype", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("OverTime")));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getCode());
                            result.put("datatype", "小时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("OverTime")));
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.DayDataEnum.getCode());
                            result.put("datatype", "日数据");
                            result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("OverTime")));
                        }
                        result.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantcode", pollutantcode);
                        result.put("pollutantunit", unitmap.get(pollutantcode));
                        result.put("standardmaxvalue", "");
                        if (alarmtype != null) {
                            if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype.toString())) {//上限报警
                                if (standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                }
                            } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype.toString())) {//下限报警
                                if (standardminvalue != null && !"".equals(standardminvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()));
                                }
                            } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(alarmtype.toString())) {//区间报警
                                if (standardminvalue != null && !"".equals(standardminvalue.toString()) && standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()) + "-" + DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                }
                            }
                        }
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        result.put("alarmlevelcode", document.get("AlarmLevel"));
                        if (document.getBoolean("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {//判断是否超标报警
                            //判断是否即超标又超限
                            if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                result.put("alarmlevel", (codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "超限、" : "") + "超标报警");
                                result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                            } else {
                                result.put("alarmlevel", "超标报警");
                                result.put("alarmlevelvalue", "");
                            }

                        } else {
                            if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                result.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "超限" : "");
                                result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                            }
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        resultmap.put("datalist", resultlist);
        resultmap.put("total", totalCount);
        return resultmap;
    }

    @Override
    public List<Map<String, Object>> getAssignAndUndisposedTaskNumDataGroupByType(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getAssignAndUndisposedTaskNumDataGroupByType(paramMap);
    }

    @Override
    public long getTotalTaskNumByParam(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTotalTaskNumByParam(paramMap);
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
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:20
     * @Description: 获取报警任务处置管理信息（有分派按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getAlarmTaskDataForOverViewByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> datas = new HashMap<>();
        try {
            String theuserid = paramMap.get("userid").toString();
            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            //获取所有报警任务信息
            //获取总条数
            Long countall = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoCountByParams(paramMap);
            List<Map<String, Object>> listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskInfoByParams(paramMap);
            //获取所有视频信息
            List<Map<String, Object>> videolist = videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
            Map<String, List<Map<String, Object>>> pointidAndrtsp = new HashMap<>();
            if (videolist != null && videolist.size() > 0) {
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videolist) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videolist) {
                                if (map2.get("monitorpointid") != null && (map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))) {
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp", map2.get("rtsp"));
                                    objmap.put("name", map2.get("name"));
                                    objmap.put("id", map2.get("pkid"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(), rtsplist);
                        }
                        //
                    }
                }
            }
            //获取分页后的任务ID
            List<String> taskids = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    map.put("rtsplist", pointidAndrtsp.get(map.get("FK_Pollutionid").toString()));
                    String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    taskids.add(map.get("PK_TaskID").toString());
                    String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                    if (map.get("overtimenum") != null) {
                        int overtimenum = Integer.parseInt(map.get("overtimenum").toString());
                        map.put("alarmovertime", countHourMinuteTime(overtimenum));
                    }
                    if ("0".equals(status)) {
                        if (map.get("unallocatednum") != null) {
                            int unallocatednum = Integer.parseInt(map.get("unallocatednum").toString());
                            if (unallocatednum > unallocatedtime) {
                                map.put("unallocatedflag", "1");
                                int tatalnum = unallocatednum - unallocatedtime;
                                String str = countHourMinuteTime(tatalnum);
                                map.put("overunallocatedtime", str);
                            } else {
                                map.put("unallocatedflag", "0");
                            }
                        }
                    } else {
                        map.put("unallocatedflag", "0");
                    }
                    map.put("lastalarmtime", map.get("TaskEndTime"));
                }
                paramMap.put("taskids", taskids);
                //获取流程数据
                List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(paramMap);
                //获取报警污染物数据
                List<Map<String, Object>> wrw_listdata = alarmTaskDisposeManagementMapper.getAllAlarmTaskPollutantDataByParams(paramMap);
                Map<String, Object> idandcodenames = getTaskPollutants(wrw_listdata);
                //通过任务ID分组数据
                Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                for (Map<String, Object> map : listdata) {
                    String taskid = map.get("PK_TaskID").toString();
                    map.put("pollutantname",idandcodenames.get(taskid));
                    setTaskAllFlagData(theuserid, uncompletetime, map, lc_map.get(taskid));
                }
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
                datas.put("total", countall);
                datas.put("datalist", listdata);
                return datas;
            } else {
                datas.put("datalist", listdata);
                return datas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return datas;
    }


    @Override
    public List<Map<String, Object>> countTaskCompletionDataByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countTaskCompletionDataByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getFidAndTypeByParam(Map<String, Object> param) {
        return alarmTaskDisposeManagementMapper.getFidAndTypeByParam(param);
    }

}
