package com.tjpu.sp.service.impl.environmentalprotection.taskmanagement;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.dao.common.UserMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.patroluserent.PatrolUserEntMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.DevOpsTaskDisposeService;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;

@Service
@Transactional
public class DevOpsTaskDisposeServiceImpl implements DevOpsTaskDisposeService {

    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;
    @Autowired
    private  WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private  GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private  PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private  UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    @Autowired
    private  AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private  OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;
    @Autowired
    private VideoCameraMapper videoCameraMapper;
    @Autowired
    private PatrolUserEntMapper patrolUserEntMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    //异常数据表
    private final String exceptionData_db = "ExceptionData";
    //任务类型  超标报警
    private final String tasktype = CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString();


    /**
     * @author: xsm
     * @date: 2019/7/16 0016 上午 11:50
     * @Description: 获取报警任务处置管理信息（无分派按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getAssignDevOpsTaskDisposeListDataByParamMap(Map<String, Object> paramMap) {
        return null;
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
    public Map<String, Object> getAllDevOpsTaskDisposeListDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> datas = new HashMap<>();
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String theuserid = paramMap.get("userid").toString();
            paramMap.put("feedbackuserid", theuserid);

            if(paramMap.get("fktasktype")==null){
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
            }else{
                paramMap.put("tasktype",paramMap.get("fktasktype"));

            }
            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            Long countall = alarmTaskDisposeManagementMapper.getAllDevOpsTaskInfoCountByParams(paramMap);
            //获取运维任务
            List<Map<String, Object>> devopslist = alarmTaskDisposeManagementMapper.getAllDevOpsTaskInfoByParams(paramMap);
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
            if (devopslist != null && devopslist.size() > 0) {
                for (Map<String, Object> map : devopslist) {
                    map.put("rtsplist", pointidAndrtsp.get(map.get("FK_Pollutionid").toString()));
                    String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    taskids.add(map.get("PK_TaskID").toString());
                    String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                    if (map.get("overtimenum")!=null){
                        int overtimenum = Integer.parseInt(map.get("overtimenum").toString());
                        map.put("alarmovertime",countHourMinuteTime(overtimenum));
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
                List<Map<String, Object>> lc_listdata  = new ArrayList<>();
                Map<String, List<Map<String, Object>>> lc_map = new HashMap<>();
                List<Map<String, Object>> wrw_listdata  = new ArrayList<>();
                lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(paramMap);
                //通过任务ID分组数据
                 lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                //获取污染物
                wrw_listdata = alarmTaskDisposeManagementMapper.getAlarmPollutantInfoByParamMap(paramMap);
                for (Map<String,Object> map:devopslist){
                    String taskid = map.get("PK_TaskID").toString();
                    setTaskAllFlagData(theuserid,uncompletetime,map,lc_map.get(taskid));
                    //组装污染物数据
                    String pollutantname = setTaskAlarmPollutantData(taskid,wrw_listdata);
                    map.put("pollutantname",pollutantname);
                }
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
                datas.put("total", countall);
                datas.put("datalist", devopslist);
                return datas;
            } else {
                datas.put("datalist", devopslist);
                return datas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return datas;
    }

    private String setTaskAlarmPollutantData(String taskid, List<Map<String, Object>> wrw_listdata) {
    String str = "";
    Set<String> set = new HashSet<>();
    for (Map<String, Object> map:wrw_listdata){
        if (map.get("PK_TaskID")!=null&&taskid.equals(map.get("PK_TaskID").toString())){
            String name = map.get("Name")!=null?map.get("Name").toString():"";
            if (!set.contains(name)){
                set.add(name);
                String str_1 = name+"【";
                String str_2 = "";
                String str_3 = "】";
                for (Map<String, Object> obj:wrw_listdata){
                    if (obj.get("PK_TaskID")!=null&&taskid.equals(obj.get("PK_TaskID").toString())&&
                            obj.get("Name")!=null &&name.equals(obj.get("Name").toString())){
                        String excstr = obj.get("AlarmType")!=null?obj.get("AlarmType").toString():"";
                        if (!"".equals(excstr)) {
                            str_2 += CommonTypeEnum.ExceptionTypeEnum.getNameByCode(excstr)+"、";
                        }

                    }
                }
                if (!"".equals(str_2)){
                    str_2 = str_2.substring(0, str_2.length() - 1);
                    str += str_1+str_2+str_3+"、";
                }
            }else{
                continue;
            }
        }
    }
        if (!"".equals(str)){
            str = str.substring(0, str.length() - 1);
        }
    return str;
    }


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 8:39
     * @Description: 处理运维标记数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void setTaskAllFlagData(String theuserid,Integer uncompletetime,Map<String, Object> result, List<Map<String, Object>> lc_listdata) {
        try{
            String status = result.get("TaskStatus").toString();
            String isfeedback = "0";
            String isgenerate = "0";
            String isscuser = "0";//是否审查人
            String uncompleteflag = "0";
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
                    if (map.get("CurrentTaskStatus")!=null&&(
                            CommonTypeEnum.AlarmTaskStatusEnum.TransferEnum.getName().toString().equals(map.get("CurrentTaskStatus").toString())||
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
     * @date: 2019/7/17 0017 下午 8:39
     * @Description: 处理企业报警任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public Map<String, Object> getPollutionAlarmTaskInfo(Map<String, Object> map, List<Map<String, Object>> allmns, List<Map<String, Object>> allpollutants, List<Document> documents,  Map<String,List<Map<String,Object>>> pointidandvideos) throws ParseException {
        Set<String> pollutants = new HashSet<String>();
        String lasttime = "";
        int num = 0;
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
        List<Map<String, Object>> outputlist = new ArrayList<>();
        if (map.get("FK_Pollutionid") != null) {
            for (Map<String, Object> obj : allmns) {
                if ((map.get("PK_TaskID").toString()).equals(obj.get("PK_TaskID").toString())&&(map.get("FK_Pollutionid").toString()).equals(obj.get("Pollutionid").toString())) {//当污染源ID相同，得到该污染源下的MN号
                    boolean isoutput = false;
                    //根据MN号去MongoDB中查询出的数据里找到相应的报警信息
                    List<Map<String,Object>> pollutantlist = new ArrayList<>();
                    for (Document document : documents) {
                        //当日期相同，MN号相同，统计超标次数和超标污染物
                        if ((map.get("TaskCreateTime").toString()).equals(document.getString("theDate")) && (document.getString("DataGatherCode")).equals(obj.get("DGIMN").toString())) {
                            if (!"".equals(lasttime)) {
                                if (compare(lasttime, DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                    lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                }
                            } else {
                                lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                            }
                            int alarmnumber = document.getInteger("num");
                            num += alarmnumber;
                            for (Map<String, Object> obj2 : allpollutants) {
                                if (document.getString("PollutantCode").equals(obj2.get("Code").toString())) {
                                    isoutput = true;
                                    //判断是否有该污染物的数据
                                    if (pollutantlist.size()>0) {
                                        boolean haspollutant =false;
                                        for (Map<String, Object> obj3 : pollutantlist) {
                                            if ((obj3.get("code").toString()).equals(document.getString("PollutantCode"))) {//判断污染物是否相同
                                                haspollutant = true;
                                                obj3.put("pollutantnum", (int) (obj3.get("pollutantnum")) + alarmnumber);
                                            }
                                        }
                                        if (haspollutant==false){
                                            Map<String, Object> pollutantmap = new HashMap<>();
                                            pollutantmap.put("code",document.getString("PollutantCode"));
                                            pollutantmap.put("name",obj2.get("Name"));
                                            pollutantmap.put("pollutantnum",alarmnumber);
                                            pollutantlist.add(pollutantmap);
                                        }
                                    }else{
                                        Map<String, Object> pollutantmap = new HashMap<>();
                                        pollutantmap.put("code",document.getString("PollutantCode"));
                                        pollutantmap.put("name",obj2.get("Name"));
                                        pollutantmap.put("pollutantnum",alarmnumber);
                                        pollutantlist.add(pollutantmap);
                                    }
                                    if (pollutants.size()>0){//判断拼接的污染物名称中  是否有污染物报两种异常 有则拼接名称
                                        Set<String> set = new HashSet<String>();
                                        boolean hasthename = false;
                                        for (String thename:pollutants){
                                            String [] strs =  thename.split("【");//分隔
                                            if (strs[0].equals(obj2.get("Name").toString())){
                                                hasthename = true;
                                                //判断是否有相同污染物的异常类型重复
                                                String exceptionnames = strs[1].replaceAll("】","");
                                                String [] strss =  exceptionnames.split("、");//分隔
                                                boolean isexception = false;
                                                for(String strname:strss){
                                                    String exceptionnametwo =CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"));
                                                    if (strname.equals(exceptionnametwo)){
                                                        isexception=true;
                                                    }
                                                }
                                                if (isexception==false){
                                                    set.add(thename.substring(0,thename.length() - 1)+"、"+CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"))+"】");
                                                }else{
                                                    set.add(thename);
                                                }

                                            }else{
                                                set.add(thename);
                                            }
                                        }
                                        pollutants.clear();
                                        pollutants = set;
                                        if (hasthename==false){
                                            pollutants.add(obj2.get("Name").toString()+"【"+CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"))+"】" );
                                        }
                                    }else{
                                        pollutants.add(obj2.get("Name").toString()+"【"+CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"))+"】" );
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (isoutput == true) {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("outputname", obj.get("outputname"));
                        maps.put("outputid", obj.get("outputid"));
                        maps.put("dgimn", obj.get("DGIMN"));
                        if (pointidandvideos!=null) {
                            maps.put("rtsplist", obj.get("outputid") != null ? pointidandvideos.get(obj.get("outputid").toString()) : null);
                        }else{
                            maps.put("rtsplist",new ArrayList<>());
                        }
                        maps.put("pointtype", obj.get("type"));
                        maps.put("pollutantlist", pollutantlist);
                        outputlist.add(maps);
                    }
                }
            }
        }

        String pollutantname = "";
        for (String str : pollutants) {
            pollutantname = pollutantname + str+ "、";
        }
        if (!"".equals(pollutantname)) {
            pollutantname = pollutantname.substring(0, pollutantname.length() - 1);
        }
        map.put("alarmnumber", num);
        map.put("pollutantname", pollutantname);
        map.put("lastalarmtime", lasttime);
        map.put("outputlist", outputlist);
        return map;
    }


    /**
     * @author: xsm
     * @date: 2019/12/11 0011 下午 5:24
     * @Description: 根据自定义参数获取运维监测点异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getDevOpsMonitorPointExceptionDataByParamMap(Map<String, Object> paramMap) {
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
        Map<String, Object> resultmap=new HashMap<>();
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
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("PollutantCode").in(codelist));
        if (paramMap.get("exceptiontypes")!=null){
            List<String> exceptiontypes = (List<String>) paramMap.get("exceptiontypes");
            if (exceptiontypes.size()>0) {//判断是否有异常类型参数
                query.addCriteria(Criteria.where("ExceptionType").in(exceptiontypes));
            }
        }
        query.addCriteria(Criteria.where("ExceptionTime").gte(startDate).lte(endDate));
        if (datatypes.size()>0){
            for (String str : datatypes) {
                datatypenames.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
            }
            query.addCriteria(Criteria.where("DataType").in(datatypenames));
        }
        //总条数
        long totalCount = mongoTemplate.count(query, exceptionData_db);
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        query.with(new Sort(Sort.Direction.ASC, "ExceptionTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, exceptionData_db);
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        Map<String, Object> codeAndLevel = new HashMap<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                Object standardmaxvalue = null;
                String exceptionvalue = "";
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            if (objmap.get("ExceptionMinValue") != null && !"".equals(objmap.get("ExceptionMinValue").toString())) {
                                exceptionvalue = exceptionvalue + "<" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMinValue").toString());
                            }
                            if (objmap.get("ExceptionMaxValue") != null && !"".equals(objmap.get("ExceptionMaxValue").toString())) {
                                if (!"".equals(exceptionvalue)) {
                                    exceptionvalue = exceptionvalue + "或>" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                                } else {
                                    exceptionvalue = exceptionvalue + ">" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                                }
                            }
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
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getCode());
                            result.put("datatype", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("ExceptionTime")));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getCode());
                            result.put("datatype", "小时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("ExceptionTime")));
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            result.put("datatypecode", CommonTypeEnum.MongodbDataTypeEnum.DayDataEnum.getCode());
                            result.put("datatype", "日数据");
                            result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("ExceptionTime")));
                        }
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantcode", pollutantcode);
                        result.put("pollutantunit", unitmap.get(pollutantcode));
                        result.put("standardmaxvalue", standardmaxvalue);
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        result.put("remindtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());
                        result.put("exceptiontype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType")));
                        if ((document.getString("ExceptionType")).equals(CommonTypeEnum.ExceptionTypeEnum.OverExceptionEnum.getCode())) {
                            result.put("exceptionvalue", exceptionvalue);
                        } else {
                            result.put("exceptionvalue", "");
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        resultmap.put("datalist",resultlist);
        resultmap.put("total",totalCount);
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
        if (type==CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//废气
            outputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//烟气
            outputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {//废水
            paramMap.put("outputtype", "water");
            outputs = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//雨水
            paramMap.put("outputtype", "rain");
            outputs = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
            outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
            outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        }else if (type==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
            outputs = airMonitorStationMapper.getALLAirStationInfoByParamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
            outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质
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
            }else if (type==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//厂界扬尘
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
                outputs = airMonitorStationMapper.getAirMonitorStationDgimnAndPollutantInfosByParam(paramMap);
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
     * @date: 2019/7/17 0017 上午 11:29
     * @Description: 添加任务信息(分派任务)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void saveDevOpsTaskInfo(String userid, String username, Map<String, Object> formdata) {
        try {
            String taskid = formdata.get("pk_taskid").toString();
            List<String> userids = (List<String>) formdata.get("userids");
            List<String> cs_userids = formdata.get("cs_userids")!=null?(List<String>) formdata.get("cs_userids"):new ArrayList<>();
            String fktasktype = formdata.get("fktasktype")==null?"":formdata.get("fktasktype").toString();
            //添加任务处置信息
            //根据任务ID获取任务信息
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeManagementMapper.selectByPrimaryKey(taskid);
            alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode());//任务状态
            alarmTaskDisposeManagement.setTaskremark(formdata.get("taskremark") != null ? formdata.get("taskremark").toString() : "");//任务说明
            alarmTaskDisposeManagement.setUpdatetime(new Date());//更新时间
            alarmTaskDisposeManagement.setUpdateuser(username);//更新人
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
            //任务分派人
            //添加任务处置记录信息
            Calendar calendar = Calendar.getInstance();
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());//主键ID
            obj.setFkTaskid(taskid);//任务ID
            obj.setFkTaskhandleuserid(userid);//分派人用户ID
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName());//任务状态
            if(StringUtils.isNotBlank(fktasktype)){
                obj.setFkTasktype(fktasktype);//任务类型
            }else{
                obj.setFkTasktype(tasktype);//任务类型
            }
            obj.setTaskhandletime(calendar.getTime());//任务分派时间
            obj.setTaskcomment(formdata.get("taskremark") != null ? formdata.get("taskremark").toString() : "");
            taskFlowRecordInfoMapper.insert(obj);
            //判断是否存在抄送人
            if(cs_userids!=null&&cs_userids.size()>0){
                calendar.add(Calendar.SECOND, 1);
                for (String str : cs_userids) {
                    //添加任务处置记录信息
                    TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
                    taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());//主键ID
                    taskFlowRecordInfo.setFkTaskid(taskid);//任务ID
                    taskFlowRecordInfo.setFkTaskhandleuserid(str.toString());//被抄送该任务的抄送人人ID
                    taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.CarbonCopyTaskEnum.getName());//任务状态
                    if(StringUtils.isNotBlank(fktasktype)){
                        taskFlowRecordInfo.setFkTasktype(fktasktype);//任务类型
                    }else{
                        taskFlowRecordInfo.setFkTasktype(tasktype);//任务类型
                    }
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
                if(StringUtils.isNotBlank(fktasktype)){
                    taskFlowRecordInfo.setFkTasktype(fktasktype);//任务类型
                }else{
                    taskFlowRecordInfo.setFkTasktype(tasktype);//任务类型
                }
                taskFlowRecordInfo.setTaskhandletime(calendar.getTime());//被分派该任务的时间
                taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 4:26
     * @Description: 修改运维任务信息为已完成状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void updateDevOpsTaskInfo(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement) {

        try {
            int oldstatus = alarmTaskDisposeManagement.getTaskstatus();
            alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.FeedbackEnum.getCode());
            alarmTaskDisposeManagementMapper.updateByPrimaryKey(alarmTaskDisposeManagement);
            //当任务未分派  直接结束时 默认结束人为任务处理人  添加一条待处理流程记录
            if (oldstatus==CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getCode()){
                //添加任务处置记录信息
                TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
                obj.setPkId(UUID.randomUUID().toString());
                obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
                obj.setFkTaskhandleuserid(userId);
                obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName().toString());
                obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
                obj.setTaskhandletime(new Date());
                taskFlowRecordInfoMapper.insert(obj);
            }
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
     * @date: 2019/12/12 0012 上午 9:32
     * @Description: 根据任务ID获取任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public AlarmTaskDisposeManagementVO selectByPrimaryKey(String pk_taskid) {
        return alarmTaskDisposeManagementMapper.selectByPrimaryKey(pk_taskid);
    }


    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 12:00
     * @Description: 获取运维任务处置管理表头信息(导出)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
   @Override
    public List<Map<String, Object>> getTableTitleForDevOpsTask() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"监测点名称",  "异常污染物", "异常开始时间","异常结束时间","连续报警时长","任务生成时间", "任务派发时间", "处置完成时间", "处置人","恢复状态",  "状态"};
        String[] titlefiled = new String[]{"monitorpointname", "pollutantname", "alarmstarttime","taskendtime", "alarmovertime","taskcreatetime", "assignmenttime","completetime", "user_name","recoverystatusname", "taskstatuname"};
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
     * @date: 2019/7/17 0017 下午 8:39
     * @Description: 处理有分派按钮权限的报警任务数据并返回给app端(app)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Map<String, Object> getPollutionAlarmTaskDataByAuthority(Map<String, Object> map, List<Map<String, Object>> allmns, List<Map<String, Object>> allpollutants, List<Document> documents) throws ParseException {
        //Set<String> pollutants = new HashSet<String>();
        String lasttime = "";
        int num = 0;
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
        List<Map<String, Object>> outputlist = new ArrayList<>();
        List<String> exceptiontypes = CommonTypeEnum.getExceptionMainTypeList();
        if (map.get("FK_Pollutionid") != null) {
            for (Map<String, Object> obj : allmns) {
                if (((map.get("FK_Pollutionid").toString()).equals(obj.get("Pollutionid").toString()))&&(map.get("FK_MonitorPointTypeCode").toString().equals(obj.get("type").toString()))) {//当污染源ID相同，得到该污染源下的MN号
                    //boolean isoutput = false;
                    int alarmnumber = 0;
                    //根据MN号去MongoDB中查询出的数据里找到相应的报警信息
                    List<Map<String,Object>> exceptionlist = new ArrayList<>();
                    for(String exceptiontype:exceptiontypes) {
                        //int exceptionnum = 0;
                        Map<String,Object> exceptionmap = new HashMap<>();
                        List<Map<String,Object>> pollutantdata = new ArrayList<>();
                        //Set<String> pollutants = new HashSet<String>();
                        for (Document document : documents) {
                            //当日期相同，MN号相同，统计超标次数和超标污染物
                            if ((map.get("TaskCreateTime").toString()).equals(document.getString("theDate")) && (document.getString("DataGatherCode")).equals(obj.get("DGIMN").toString())) {
                                if (!"".equals(lasttime)) {
                                    if (compare(lasttime, DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                        lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                    }
                                } else {
                                    lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                }
                                if (document.get("ExceptionType")!=null&&exceptiontype.equals(document.getString("ExceptionType"))){
                                    //exceptionnum+=document.getInteger("num");
                                    for (Map<String, Object> obj2 : allpollutants) {
                                        if (document.getString("PollutantCode").equals(obj2.get("Code").toString())) {
                                            Map<String,Object> pollutantmap = new HashMap<>();
                                            pollutantmap.put("pollutantname",obj2.get("Name").toString());
                                            pollutantmap.put("pollutantcode",obj2.get("Code").toString());
                                            pollutantmap.put("exceptionnum",document.getInteger("num"));
                                            alarmnumber += document.getInteger("num");
                                            num += document.getInteger("num");
                                            pollutantdata.add(pollutantmap);
                                            //pollutants.add(obj2.get("Name").toString());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (pollutantdata.size()>0) {
                            exceptionmap.put("exceptiontype",CommonTypeEnum.ExceptionTypeEnum.getNameByCode(exceptiontype));
                            exceptionmap.put("exceptiondata",pollutantdata);
                            exceptionlist.add(exceptionmap);
                        }
                    }
                    if (exceptionlist.size()>0) {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("outputname", obj.get("outputname"));
                        maps.put("outputid", obj.get("outputid"));
                        maps.put("pointtype", obj.get("type"));
                        maps.put("dgimn", obj.get("DGIMN"));
                        maps.put("lastalarmtime", lasttime);
                        maps.put("alarmnumber", alarmnumber);;
                        maps.put("exceptiondata", exceptionlist);
                        outputlist.add(maps);
                    }
                }
            }
        }

       /* String pollutantname = "";
        for (String str : pollutants) {
            pollutantname = pollutantname + str + "、";
        }
        if (!"".equals(pollutantname)) {
            pollutantname = pollutantname.substring(0, pollutantname.length() - 1);
        }*/
        map.put("alarmnumber", num);
        //map.put("pollutantname", pollutantname);
        map.put("lastalarmtime", lasttime);
        map.put("outputlist", outputlist);
        if (map.get("FK_MonitorPointTypeCode")!=null&&!"".equals(map.get("FK_MonitorPointTypeCode").toString())) {
            int monitorpointtype = Integer.parseInt(map.get("FK_MonitorPointTypeCode").toString());
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(),
                    WasteGasEnum.getCode(), RainEnum.getCode(), unOrganizationWasteGasEnum.getCode(), FactoryBoundarySmallStationEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),EnvironmentalDustEnum.getCode()
            );
            if (monitortypes.contains(monitorpointtype)==false) {
                map.put("PollutionName", "");
                map.put("FK_Pollutionid", "");
            }
        }
        return map;
    }

    /**
     * @author: xsm
     * @date: 2019/8/2 0002 下午 7:07
     * @Description: 处理没有分派按钮权限的数据并返回给app端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    private Map<String, Object> getPollutionAlarmTaskDataByUserid(Map<String, Object> map, List<Map<String, Object>> allmns, List<Map<String, Object>> allpollutants, List<Document> documents) throws ParseException {
        //Set<String> pollutants = new HashSet<String>();
        int num = 0;
        String lasttime = "";
        List<Map<String, Object>> outputlist = new ArrayList<>();
        List<String> exceptiontypes = CommonTypeEnum.getExceptionMainTypeList();
        if (map.get("FK_Pollutionid") != null) {
            for (Map<String, Object> obj : allmns) {
                if (((map.get("FK_Pollutionid").toString()).equals(obj.get("Pollutionid").toString()))&&(map.get("FK_MonitorPointTypeCode").toString().equals(obj.get("type").toString()))) {//当污染源ID相同，得到该污染源下的MN号
                    //boolean isoutput = false;
                    int alarmnumber = 0;
                    //根据MN号去MongoDB中查询出的数据里找到相应的报警信息
                    List<Map<String,Object>> exceptionlist = new ArrayList<>();
                    for(String exceptiontype:exceptiontypes) {
                        //int exceptionnum = 0;
                        Map<String, Object> exceptionmap = new HashMap<>();
                        List<Map<String,Object>> pollutantdata = new ArrayList<>();
                       // Set<String> pollutants = new HashSet<String>();
                        for (Document document : documents) {
                            //当日期相同，MN号相同，统计超标次数和超标污染物
                            if ((map.get("TaskCreateTime").toString()).equals(document.getString("theDate")) && (document.getString("DataGatherCode")).equals(obj.get("DGIMN").toString())) {
                                if (!"".equals(lasttime)) {
                                    if (compare(lasttime, DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                        lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                    }
                                } else {
                                    lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                }
                                if (document.get("ExceptionType")!=null&&exceptiontype.equals(document.getString("ExceptionType"))){
                                    //exceptionnum+=document.getInteger("num");
                                    for (Map<String, Object> obj2 : allpollutants) {
                                        if (document.getString("PollutantCode").equals(obj2.get("Code").toString())) {
                                            Map<String,Object> pollutantmap = new HashMap<>();
                                            pollutantmap.put("pollutantname",obj2.get("Name").toString());
                                            pollutantmap.put("pollutantcode",obj2.get("Code").toString());
                                            pollutantmap.put("exceptionnum",document.getInteger("num"));
                                            alarmnumber += document.getInteger("num");
                                            num += document.getInteger("num");
                                            pollutantdata.add(pollutantmap);
                                            //pollutants.add(obj2.get("Name").toString());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (pollutantdata.size()>0) {
                            exceptionmap.put("exceptiontype",CommonTypeEnum.ExceptionTypeEnum.getNameByCode(exceptiontype));
                            exceptionmap.put("exceptiondata",pollutantdata);
                            exceptionlist.add(exceptionmap);
                        }
                    }
                    if (exceptionlist.size()>0) {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("outputname", obj.get("outputname"));
                        maps.put("outputid", obj.get("outputid"));
                        maps.put("dgimn", obj.get("DGIMN"));
                        maps.put("pointtype", obj.get("type"));
                        maps.put("lastalarmtime", lasttime);
                        maps.put("alarmnumber", alarmnumber);
                        maps.put("exceptiondata", exceptionlist);
                        outputlist.add(maps);
                    }
                }
            }
        }
        map.put("alarmnumber", num);
        //map.put("pollutantname", pollutantname);
        map.put("lastalarmtime", lasttime);
        map.put("outputlist", outputlist);
        if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getName());
        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getName());
        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(map.get("TaskStatus").toString())) {
            map.put("taskstatuname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getName());
        }

        return map;
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

    /**
     * @author: xsm
     * @date: 2019/12/17 0017 上午 9:54
     * @Description: 根据报警类型和监测时间获取报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String,Object>> getAlarmTaskInfoByRemindTypeAndParamMap(Map<String, Object> paramMap){
        return alarmTaskDisposeManagementMapper.getAlarmTaskInfoByRemindTypeAndParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 2:08
     * @Description: 获取有运维任务处置权限的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDisposePersonSelectData(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getDisposePersonSelectData(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 2:08
     * @Description: 根据自定义参数获取企业运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public EntDevOpsInfoVO getEntDevOpsInfoVOByParam(Map<String, Object> paramMap) {
        return entDevOpsInfoMapper.getEntDevOpsInfoVOByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 上午 9:48
     * @Description: 根据自运维任务主键ID获取运维任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDevOpsTaskDetailByID(String id, String userId, String name) {
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

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 下午 3:12
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addDevOpsTaskStatusToHandle(String id, String userId, String username) {
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
        if(iszbtask == true){
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
        }else{
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
            //任务忽略人
            //添加任务处置记录信息
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());
            obj.setFkTaskhandleuserid(userId);
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.NeglectTaskEnum.getName().toString());
            obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj.setTaskhandletime(new Date());
            taskFlowRecordInfoMapper.insert(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 11:36
     * @Description: 统计运维任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countDevOpsTaskCompletionStatusByParamMap(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.countDevOpsTaskCompletionStatusByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 下午 3:49
     * @Description: 根据时间范围获取运维任务信息（数据分析）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getDevOpsTaskListDataByParam(Map<String, Object> paramMap) {
        try{
            Map<String, Object> resultMap = new HashMap<>();
            //获取所有报警任务信息
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
            }
            List<Map<String, Object>> listdata = new ArrayList<>();
            if (paramMap.get("datauserid")!=null){
                listdata = alarmTaskDisposeManagementMapper.getDevOpsTaskListDataByParamAndDataUserId(paramMap);
            }else{
                listdata = alarmTaskDisposeManagementMapper.getDevOpsTaskListDataByParam(paramMap);
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(listdata);
                long total = pageInfo.getTotal();
                resultMap.put("total", total);
            }
            Set<String> taskids = new HashSet<String>();
            Set<String> pointtypes = new HashSet<String>();
            Set<String> mnlist = new HashSet<String>();
            String maxdate = "";
            String mindate = "";
            if (listdata != null && listdata.size() > 0) {
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        map.put("tasktypename", "运维工单");
                        taskids.add(map.get("PK_TaskID").toString());
                        if (map.get("FK_MonitorPointTypeCode")!=null&&!"".equals(map.get("FK_MonitorPointTypeCode").toString())){
                            pointtypes.add(map.get("FK_MonitorPointTypeCode").toString());
                        }
                        Date d2 = DataFormatUtil.parseDateYMD(map.get("TaskCreateTime").toString());
                        if (!"".equals(maxdate)) {
                            Date d1 = DataFormatUtil.parseDateYMD(maxdate);
                            if (d2.compareTo(d1)>0) {
                                maxdate = map.get("TaskCreateTime").toString();
                            }
                        } else {
                            maxdate = map.get("TaskCreateTime").toString();
                        }
                        if (!"".equals(mindate)) {
                            Date d1 = DataFormatUtil.parseDateYMD(mindate);
                            if (d2.compareTo(d1)<0) {
                                mindate = map.get("TaskCreateTime").toString();
                            }
                        } else {
                            mindate = map.get("TaskCreateTime").toString();
                        }
                    }
                    paramMap.put("taskids", taskids);
                }
                //获取监测点位信息
                List<Map<String, Object>> allmns = alarmTaskDisposeManagementMapper.getAllMonitorPointInfoByTaskIds(paramMap);
                for (Map<String, Object> obj : allmns) {
                    if (obj.get("DGIMN") != null) {
                        mnlist.add(obj.get("DGIMN").toString());
                    }
                }
                //获取所有排口监测的污染物
                List<Map<String, Object>> allpollutants = new ArrayList<>();
                if (pointtypes!=null&&pointtypes.size()>0) {
                    allpollutants = alarmTaskDisposeManagementMapper.getAllEntPollutantsByPointTypes(pointtypes);
                }
                //获取废水、废气、雨水排口各类型污染物（并集）
                List<AggregationOperation> operations = new ArrayList<>();
                operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist).andOperator(Criteria.where("ExceptionTime").gte(DataFormatUtil.getDateYMDHMS(mindate + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(maxdate + " 23:59:59")))));
                // 加8小时
                operations.add(Aggregation.project("PollutantCode", "ExceptionTime", "num", "DataGatherCode","ExceptionType").andExpression("add(ExceptionTime,8 * 3600000)").as("date8"));
                operations.add(Aggregation.project("PollutantCode", "date8", "ExceptionTime", "num", "DataGatherCode","ExceptionType").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
                operations.add(Aggregation.group(new String[]{"theDate", "PollutantCode", "DataGatherCode","ExceptionType"}).count().as("num").last("ExceptionTime").as("ExceptionTime"));
                Aggregation aggregationquery = Aggregation.newAggregation(operations);
                AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, exceptionData_db, Document.class);
                List<Document> documents = resultdocument.getMappedResults();
                if (documents.size() > 0) {//判断查询数据是否为空
                    //遍历任务组装任务数据信息
                    for (Map<String, Object> map : listdata) {
                        Set<String> pollutants = new HashSet<String>();
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
                        if (map.get("FK_Pollutionid") != null) {
                            for (Map<String, Object> obj : allmns) {
                                if ((map.get("PK_TaskID").toString()).equals(obj.get("PK_TaskID").toString())&&(map.get("FK_Pollutionid").toString()).equals(obj.get("Pollutionid").toString())) {//当污染源ID相同，得到该污染源下的MN号
                                    boolean isoutput = false;
                                    //根据MN号去MongoDB中查询出的数据里找到相应的报警信息
                                    for (Document document : documents) {
                                        //当日期相同，MN号相同，统计超标次数和超标污染物
                                        if ((map.get("TaskCreateTime").toString()).equals(document.getString("theDate")) && (document.getString("DataGatherCode")).equals(obj.get("DGIMN").toString())) {
                                            if (!"".equals(lasttime)) {
                                                if (compare(lasttime, DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                                                    lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                                }
                                            } else {
                                                lasttime = DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime"));
                                            }
                                            for (Map<String, Object> obj2 : allpollutants) {
                                                if (document.getString("PollutantCode").equals(obj2.get("Code").toString())) {
                                                    isoutput = true;
                                                    pollutants.add(obj2.get("Name").toString());
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        List<Map<String,Object>> pollutantnames = new ArrayList<>();
                        if (pollutants.size()>0){
                            Map<String,Object> objmap = new HashMap<>();
                            objmap.put("typename",map.get("monitorpointtypename"));
                            String pollutantname = "【";
                            for (String str : pollutants) {
                                pollutantname = pollutantname + str + "、";
                            }
                            pollutantname = pollutantname.substring(0, pollutantname.length() - 1);
                            objmap.put("pollutantname",pollutantname+"】");
                            pollutantnames.add(objmap);
                        }
                        map.put("pollutantlist", pollutantnames);
                        map.put("lastalarmtime", lasttime);
                    }
                } else {
                    for (Map<String, Object> map : listdata) {
                        String taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                        map.put("taskstatuname", taskstatuname);
                        map.put("pollutantlist", new ArrayList<>());
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
     * @date: 2020/2/26 0026 上午 11:36
     * @Description: 根据自定义参数获取点位运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorPointDevOpsTaskDataByParamMap(Map<String, Object> paramMap) {
       String starttime = paramMap.get("starttime").toString();
       String endtime = paramMap.get("endtime").toString();
       String userid=paramMap.get("userid").toString();
        Map<String,Object> obj = alarmTaskDisposeManagementMapper.getMonitorPointDevOpsTaskDataByParamMap(paramMap);
        List<Map<String,Object>> pollutants = new ArrayList<>();
        if (obj!=null&&obj.get("DGIMN")!=null){
            String mn=obj.get("DGIMN").toString();
            String taskid=(obj.get("PK_TaskID")!=null)?obj.get("PK_TaskID").toString():"";
            String status=(obj.get("TaskStatus")!=null)?obj.get("TaskStatus").toString():"";
            paramMap.put("taskid",taskid);
            obj.put("feedbackflag",false);
            obj.put("undisposedpeople","");
            //根据任务ID获取
            if(!(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode().toString()).equals(status)){
                List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(taskid);
               String undisposedpeople = "";
               boolean feedbackflag  = false;
                if (datalist!=null&&datalist.size()>0){
                    for(Map<String, Object> taskflowrecord:datalist){
                        if (taskflowrecord.get("CurrentTaskStatus")!=null&&(taskflowrecord.get("CurrentTaskStatus").toString()).equals(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName())){
                            undisposedpeople = undisposedpeople+taskflowrecord.get("User_Name")+"、";
                            if (userid.equals(taskflowrecord.get("FK_TaskHandleUserID").toString())){
                                feedbackflag = true;
                            }
                        }

                    }
                }
                if (StringUtils.isNotBlank(undisposedpeople)){
                    undisposedpeople = undisposedpeople.substring(0,undisposedpeople.length()-1);
                }
                obj.put("feedbackflag",feedbackflag);
                obj.put("undisposedpeople",undisposedpeople);
            }
            //获取问题类型
            String problemtype = (obj.get("FK_ProblemType")!=null)?obj.get("FK_ProblemType").toString():"";
            String problem = "";
            if (problemtype != null && !"".equals(problemtype)) {
                String[] str = problemtype.split(",");
                List<Map<String, Object>> problemtypelist = alarmTaskDisposeManagementMapper.getProblemTypeSelectData(null);
                if (str.length > 0) {
                    for (String code : str) {
                        for (Map<String, Object> problemmap : problemtypelist) {
                            if (code.equals(problemmap.get("Code").toString())) {
                                problem = problem + problemmap.get("Name") + "、";
                            }
                        }
                    }
                }
            }
            if (!"".equals(problem)) {
                problem = problem.substring(0, problem.length() - 1);
            }
            obj.put("problemtypenames",problem);
            int monitortype=(obj.get("FK_MonitorPointTypeCode")!=null&&!"".equals(obj.get("FK_MonitorPointTypeCode").toString()))?Integer.parseInt(obj.get("FK_MonitorPointTypeCode").toString()):null;
            int interval=0;
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//废气
                interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//废水、雨水
                interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode() ) {//扬尘
                interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
            } else {//其它类型监测点
                interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
            }
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mn).andOperator(Criteria.where("ExceptionTime").gte(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endtime+ " 23:59:59")))));
            // 加8小时
            String orderBy = "ExceptionTime";
            Sort.Direction direction = Sort.Direction.ASC;
            operations.add(Aggregation.sort(direction, orderBy.split(",")));
            operations.add(Aggregation.project( "PollutantCode","ExceptionTime","ExceptionType"));
            Map<String, Object> timeAndRead = new HashMap<>();
            timeAndRead.put("time", "$ExceptionTime");
            timeAndRead.put("exceptiontype", "$ExceptionType");
            operations.add(
                    Aggregation.group("PollutantCode")
                            .push(timeAndRead).as("timeList")

            );
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, exceptionData_db, Document.class);
            List<Document> documents = resultdocument.getMappedResults();
            if (documents.size()>0){
                paramMap.clear();
                paramMap.put("monitorpointtype",monitortype);
                paramMap.put("dgimn",mn);
                List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
                for(Map<String, Object> map:outputlist){
                    String code = map.get("Code").toString();
                    String name = map.get("Name").toString();
                    for(Document document:documents ){
                        if (code.equals(document.getString("_id"))){
                            Map<String, Object> objmap = new HashMap<>();
                            objmap.put("code",code);
                            objmap.put("name",name);
                            objmap.put("pollutantdata","");
                            List<Document> timeList = (List<Document>) document.get("timeList");
                            List<String> dateList = new ArrayList<>();
                            Set<String> strss = new HashSet<>();
                            for (Document time : timeList) {
                                String ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                                strss.add(time.getString("exceptiontype"));
                                if (!dateList.contains(ymdhms)) {
                                    dateList.add(ymdhms);


                                }
                            }
                            String continuityvalue = mergeContinueTimeDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                            String exceptionname ="";
                            for(String strname:strss){
                                 exceptionname =CommonTypeEnum.ExceptionTypeEnum.getNameByCode(strname)+"、";
                            }
                            if (StringUtils.isNotBlank(exceptionname)){
                                exceptionname = exceptionname.substring(0,exceptionname.length()-1);
                            }
                            objmap.put("pollutantdata",continuityvalue+"出现"+exceptionname);
                            pollutants.add(objmap);
                        }
                    }

                }
            }
            obj.put("pollutants",pollutants);
        }
        return obj;
    }

    /**
     * @author: xsm
     * @date: 2020/04/22 0022 上午 10:50
     * @Description: 统计某个时间范围内运维任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDevOpsTaskRemindDataByMonitorTimes(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getDevOpsTaskRemindDataByMonitorTimes(paramMap);
    }

    /**
     * @author: lp
     * @date: 2019/9/24 0024 上午 10:28
     * @Description: 合并连续数字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: dateList:[yyyy-MM-dd HH:mm:ss]
     * @return:
     */
    private  String mergeContinueTimeDate(List<String> dateList,int interval, String beforeDataFormat, String split, String afterDataFormat) {
        List<Date> dates = new ArrayList<>();
        String ymd = DataFormatUtil.getDateYMD(new Date());
        String hourMinute;
        String ymdhm;
        Date date;
        for (String time : dateList) {
            hourMinute = DataFormatUtil.FormatDateOneToOther(time, beforeDataFormat, afterDataFormat);
            ymdhm = ymd + " " + hourMinute;
            date = DataFormatUtil.parseDateByFormat(ymdhm, beforeDataFormat);
            if (!dates.contains(date)) {
                dates.add(date);
            }
        }
        Collections.sort(dates);
        int startIndex = 0;
        String timeListString = getMergeDateContinueTimeData(dates,startIndex,interval,split,afterDataFormat);
        if (StringUtils.isNotBlank(timeListString)){
            timeListString = timeListString.substring(0,timeListString.length()-1);
        }
        return timeListString;

    }

    private static String getMergeDateContinueTimeData(List<Date> dates, int startIndex,int interval, String split, String afterDataFormat) {
        int endIndex = startIndex;
        if (dates.size() == startIndex) {//结束条件，遍历完数组
            return "";
        } else {
            for (int i = startIndex; i < dates.size(); i++) {
                if (i < dates.size() - 1) {
                    if (DataFormatUtil.isLessEqualUpdate(dates.get(i), dates.get(i + 1), interval)) {
                        endIndex = i;
                    } else {
                        if (i > startIndex)
                            endIndex = endIndex + 1;
                        break;
                    }
                } else {
                    if (endIndex == dates.size() - 2) {
                        endIndex = dates.size() - 1;
                        break;
                    }
                }
            }
            if (startIndex == endIndex)//相等说明不连续
                return DataFormatUtil.parseDateToStringByFormat(dates.get(startIndex),afterDataFormat)
                        + split + getMergeDateContinueTimeData(dates, endIndex + 1,interval, split, afterDataFormat);
            else {
                return DataFormatUtil.parseDateToStringByFormat(dates.get(startIndex),afterDataFormat) + "至" + DataFormatUtil.parseDateToStringByFormat(dates.get(endIndex),afterDataFormat)
                        + split + getMergeDateContinueTimeData(dates, endIndex + 1,interval, split, afterDataFormat);
            }

        }

    }

    @Override
    public void addDevOpsTaskInfo(String monitorpointtypecode,String pollutionid, String daytime,JSONObject messageobject,Object mn) {
        //根据MN号 监测时间查询是否有该点位的报警信息
        messageobject.put("TaskID", null);
        if (mn!=null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(daytime.substring(0, 10)+" 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(daytime.substring(0, 10)+" 23:59:59");
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(mn.toString()));
            //query.addCriteria(Criteria.where("DataType").is("RealTimeData"));
            query.addCriteria(Criteria.where("ExceptionTime").gte(startDate).lte(endDate));
            Document document = mongoTemplate.findOne(query, Document.class, "ExceptionData");
            if (document != null) {//判断查询数据是否为空
                AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                String pkid = UUID.randomUUID().toString();
                obj.setPkTaskid(pkid);
                obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                obj.setTaskcreatetime(daytime);
                obj.setUpdatetime(new Date());
                obj.setFkPollutionid(pollutionid);
                obj.setFkmonitorpointtypecode(monitorpointtypecode);
                obj.setFkTasktype(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString());
                alarmTaskDisposeManagementMapper.insert(obj);
                messageobject.put("TaskID", pkid);
            }
        }
    }


    /**
     * @author: xsm
     * @date: 2021/03/12 0012 上午 9:50
     * @Description: 获取首页今日工单运维任务
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllDevOpsTaskByParamForHome(Map<String, Object> param) {
        List<Map<String, Object>> listdata =new ArrayList<>();
        try {
            listdata = alarmTaskDisposeManagementMapper.getAllDevOpsTaskInfoByParams(param);
            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            //获取分页后的任务ID
            List<String> taskids = new ArrayList<>();
            if (listdata!=null&&listdata.size()>0){
                for (Map<String,Object> map:listdata){
                    String taskstatuname = null;
                    taskstatuname = getStatusNameByStatusCode(map.get("TaskStatus").toString());
                    map.put("taskstatuname", taskstatuname);
                    map.put("tasktypename", "运维工单");
                    if (map.get("overtimenum") != null) {
                        int overtimenum = Integer.parseInt(map.get("overtimenum").toString());
                        map.put("alarmovertime", countHourMinuteTime(overtimenum));
                    }
                    taskids.add(map.get("PK_TaskID").toString());
                    String status = map.get("TaskStatus")!=null?map.get("TaskStatus").toString():"";
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
                    }else{
                        map.put("unallocatedflag", "0");
                    }
                    map.put("lastalarmtime", map.get("TaskCreateTime"));
                }
                param.put("taskids", taskids);
                List<Map<String, Object>> lc_listdata = alarmTaskDisposeManagementMapper.getAllTaskFlowRecordInfoByParams(param);
                //通过任务ID分组数据
                Map<String, List<Map<String, Object>>> lc_map = lc_listdata.stream().collect(Collectors.groupingBy(m -> m.get("PK_TaskID").toString()));
                //获取污染物
                List<Map<String, Object>> wrw_listdata  = new ArrayList<>();
                wrw_listdata = alarmTaskDisposeManagementMapper.getAlarmPollutantInfoByParamMap(param);
                for (Map<String,Object> map:listdata){
                    String taskid = map.get("PK_TaskID").toString();
                    setTaskAllFlagData(param.get("feedbackuserid").toString(),uncompletetime,map,lc_map.get(taskid));
                    //组装污染物数据
                    String pollutantname = setTaskAlarmPollutantData(taskid,wrw_listdata);
                    map.put("pollutantname",pollutantname);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return listdata;
    }


    private String countHourMinuteTime(int tatalnum) {
        String str ="";
        if (tatalnum<60){
            str = tatalnum+"分钟";
        }else if(tatalnum == 60){
            str = "1小时";
        }else{
            int onenum = tatalnum/60;
            str = onenum+"小时"+((tatalnum-onenum*60)>0?(tatalnum-onenum*60)+"分钟":"");
        }
        return str;
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
}
