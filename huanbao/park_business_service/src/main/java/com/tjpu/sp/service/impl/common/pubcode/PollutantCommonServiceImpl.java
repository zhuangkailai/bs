package com.tjpu.sp.service.impl.common.pubcode;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DeviceDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.TaskAlarmPollutantInfoMapper;
import com.tjpu.sp.service.common.pubcode.PollutantCommonService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: chengzq
 * @date: 2021/3/10 0010 13:22
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@Transactional
public class PollutantCommonServiceImpl implements PollutantCommonService {

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private DeviceDevOpsInfoMapper deviceDevOpsInfoMapper;
    @Autowired
    private TaskAlarmPollutantInfoMapper taskAlarmPollutantInfoMapper;
    @Autowired
    private MongoTemplate mongoTemplate;

    private final String monitorpointid = "fkmonitorpointid";
    private final String pollutantcode = "fkpollutantcode";
    private final String alarmtime = "alarmtime";
    private final String alarmtype = "alarmtype";
    private final String dgimn = "dgimn";



    private final String overdatacollection = "OverData";


    /*污染物相关*/
    /**
     * @author: chengzq
     * @date: 2021/3/10 0010 下午 1:48
     * @Description: 通过多个参数设置污染物标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datalist, contrast, pollutanttype]
     * @throws:
     * 注：contrast 可根据该对象获取不同数据集datalist中不同的污染物key和监测点id的key
     *
     */
    @Override
    public void setPollutantStandardValueDataByParam(List<Map<String, Object>> datalist,Map<String,String> contrast,String pollutanttype) {
        Map<String,Object> param =new HashMap<>();
        param.put("monitorpointtype",pollutanttype);

        List<Map<String, Object>> pollutantStandardByParams = pollutantFactorMapper.getPollutantStandardByParams(param);
        Map<String, String> minvalueMap = pollutantStandardByParams.stream().filter(m -> m.get("FK_PollutantCode") != null && m.get("monitorpointid") != null && m.get("StandardMinValue") != null).
                collect(Collectors.toMap(m -> m.get("monitorpointid").toString() + "_" + m.get("FK_PollutantCode").toString(), m -> m.get("StandardMinValue").toString(), (a, b) -> a));
        Map<String, String> maxvalueMap = pollutantStandardByParams.stream().filter(m -> m.get("FK_PollutantCode") != null && m.get("monitorpointid") != null && m.get("StandardMaxValue") != null).
                collect(Collectors.toMap(m -> m.get("monitorpointid").toString() + "_" + m.get("FK_PollutantCode").toString(), m -> m.get("StandardMaxValue").toString(), (a, b) -> a));
        Map<String, String> AlarmTypeMap = pollutantStandardByParams.stream().filter(m -> m.get("FK_PollutantCode") != null && m.get("monitorpointid") != null && m.get("AlarmType") != null).
                collect(Collectors.toMap(m -> m.get("monitorpointid").toString() + "_" + m.get("FK_PollutantCode").toString(), m -> m.get("AlarmType").toString(), (a, b) -> a));

        for (Map<String, Object> stringObjectMap : datalist) {
            String fkpollutantcode = stringObjectMap.get(contrast.get(pollutantcode)) == null ? "" : stringObjectMap.get(contrast.get(pollutantcode)).toString();
            String fkmonitorpointid = stringObjectMap.get(contrast.get(monitorpointid)) == null ? "" : stringObjectMap.get(contrast.get(monitorpointid)).toString();
            stringObjectMap.put("StandardMinValue",minvalueMap.get(fkmonitorpointid+"_"+fkpollutantcode));
            stringObjectMap.put("StandardMaxValue",maxvalueMap.get(fkmonitorpointid+"_"+fkpollutantcode));
            stringObjectMap.put("pollutantalarmtypecode",AlarmTypeMap.get(fkmonitorpointid+"_"+fkpollutantcode));
        }
    }



    /*报警任务相关*/
    /**
     * @author: chengzq
     * @date: 2021/3/11 0011 上午 10:14
     * @Description:  设置监测点报警任务状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datalist, contrast]
     * @throws:
     */
    @Override
    public void setAlarmTaskStatusDataByParam(List<Map<String, Object>> datalist, Map<String, String> contrast) {
        List<String> fkmonitorpointids = datalist.stream().filter(m -> m.get(contrast.get(monitorpointid)) != null).map(m -> m.get(contrast.get(monitorpointid)).toString()).distinct().collect(Collectors.toList());
        Map<String,Object> param =new HashMap<>();
        param.put("monitorpointids",fkmonitorpointids);
        List<Map<String, Object>> monitorPointTaskStatusByParams = alarmTaskDisposeManagementMapper.getMonitorPointTaskStatusByParams(param);
        Map<String ,Object> tasktypeMap=new HashMap<>();
        tasktypeMap.put("1","超标报警");
        tasktypeMap.put("5","异常报警");
        for (Map<String, Object> stringObjectMap : datalist) {
            String fkmonitorpointid = stringObjectMap.get(contrast.get(monitorpointid)) == null ? "" : stringObjectMap.get(contrast.get(monitorpointid)).toString();
            String alarTtime = stringObjectMap.get(contrast.get(alarmtime)) == null ? "" : stringObjectMap.get(contrast.get(alarmtime)).toString();
            String alarmType = stringObjectMap.get(contrast.get(alarmtype)) == null ? "" : stringObjectMap.get(contrast.get(alarmtype)).toString();
            stringObjectMap.put("TaskStatus",null);
            stringObjectMap.put("PKTaskID",null);
            for (Map<String, Object> monitorPointTaskStatusByParam : monitorPointTaskStatusByParams) {
                String Fk_pollutionID = monitorPointTaskStatusByParam.get("Fk_pollutionID") == null ? "" : monitorPointTaskStatusByParam.get("Fk_pollutionID").toString();
                String TaskCreateTime = monitorPointTaskStatusByParam.get("TaskCreateTime") == null ? "" : monitorPointTaskStatusByParam.get("TaskCreateTime").toString();
                String TaskEndTime = monitorPointTaskStatusByParam.get("TaskEndTime") == null ? "" : monitorPointTaskStatusByParam.get("TaskEndTime").toString();
                String TaskStatus = monitorPointTaskStatusByParam.get("TaskStatus") == null ? "" : monitorPointTaskStatusByParam.get("TaskStatus").toString();
                String FK_TaskType = monitorPointTaskStatusByParam.get("FK_TaskType") == null ? "" : monitorPointTaskStatusByParam.get("FK_TaskType").toString();
                String PK_TaskID = monitorPointTaskStatusByParam.get("PK_TaskID") == null ? "" : monitorPointTaskStatusByParam.get("PK_TaskID").toString();
                if(fkmonitorpointid.equals(Fk_pollutionID) && DataFormatUtil.getDateYMDHMS(alarTtime).getTime()>=DataFormatUtil.getDateYMDHMS(TaskCreateTime).getTime()
                        && DataFormatUtil.getDateYMDHMS(alarTtime).getTime()<=DataFormatUtil.getDateYMDHMS(TaskEndTime).getTime() && alarmType.equals(tasktypeMap.get(FK_TaskType))){
                    stringObjectMap.put("TaskStatus",TaskStatus);
                    stringObjectMap.put("PKTaskID",PK_TaskID);
                }
            }
        }

    }



    /*设备运维信息*/

    /**
     * @author: chengzq
     * @date: 2021/3/11 0011 下午 1:56
     * @Description: 获取未过期的设备运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datalist, contrast, pollutanttype]
     * @throws:
     */
    @Override
    public void setUnexpiredDeviceDevOpsDataByParam(List<Map<String, Object>> datalist, Map<String, String> contrast, String pollutanttype) {
        Map<String,Object> param =new HashMap<>();
        param.put("fkmonitorpointtypecode",pollutanttype);
        List<Map<String, Object>> unexpiredDeviceDevOpsInfos = deviceDevOpsInfoMapper.getUnexpiredDeviceDevOpsInfos(param);
        for (Map<String, Object> stringObjectMap : datalist) {
            String fkpollutantcode = stringObjectMap.get(contrast.get(pollutantcode)) == null ? "" : stringObjectMap.get(contrast.get(pollutantcode)).toString();
            String fkmonitorpointid = stringObjectMap.get(contrast.get(monitorpointid)) == null ? "" : stringObjectMap.get(contrast.get(monitorpointid)).toString();
            stringObjectMap.put("devopsstarttime",null);
            stringObjectMap.put("devopsendtime",null);
            stringObjectMap.put("DevOpsPeoples",null);
            for (Map<String, Object> unexpiredDeviceDevOpsInfo : unexpiredDeviceDevOpsInfos) {
                String fkMonitorpointid = unexpiredDeviceDevOpsInfo.get("fkMonitorpointid") == null ? "" : unexpiredDeviceDevOpsInfo.get("fkMonitorpointid").toString();
                String pollutantcodes = unexpiredDeviceDevOpsInfo.get("pollutantcodes") == null ? "" : unexpiredDeviceDevOpsInfo.get("pollutantcodes").toString();
                String devopsstarttime = unexpiredDeviceDevOpsInfo.get("devopsstarttime") == null ? "" : unexpiredDeviceDevOpsInfo.get("devopsstarttime").toString();
                String devopsendtime = unexpiredDeviceDevOpsInfo.get("devopsendtime") == null ? "" : unexpiredDeviceDevOpsInfo.get("devopsendtime").toString();
                Set<String> DevOpsPeopleNames = unexpiredDeviceDevOpsInfo.get("DevOpsPeoples") == null ? new HashSet<>() : (HashSet<String>)unexpiredDeviceDevOpsInfo.get("DevOpsPeoples");
                String collect = DevOpsPeopleNames.stream().collect(Collectors.joining("、"));
                if(fkmonitorpointid.equals(fkMonitorpointid) && pollutantcodes.contains(fkpollutantcode)){
                    stringObjectMap.put("devopsstarttime",devopsstarttime);
                    stringObjectMap.put("devopsendtime",devopsendtime);
                    stringObjectMap.put("DevOpsPeoples",collect);
                }
            }
        }


    }




}
