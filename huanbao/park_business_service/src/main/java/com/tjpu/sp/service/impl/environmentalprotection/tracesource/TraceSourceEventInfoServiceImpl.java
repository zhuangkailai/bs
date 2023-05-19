package com.tjpu.sp.service.impl.environmentalprotection.tracesource;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.pollutantsmell.PollutantSmellMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.PollutaEventDetailInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TraceSourceEntInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TraceSourceEventInfoMapper;
import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.PollutaEventDetailInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceEntInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceEventInfoVO;
import com.tjpu.sp.service.environmentalprotection.petition.PetitionInfoService;
import com.tjpu.sp.service.environmentalprotection.tracesource.TraceSourceEventInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.EventTypeEnum.AlarmDataEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.EventTypeEnum.PetitionEnum;

@Service
@Transactional
public class TraceSourceEventInfoServiceImpl implements TraceSourceEventInfoService {

    @Autowired
    private TraceSourceEventInfoMapper traceSourceEventInfoMapper;
    @Autowired
    private PetitionInfoService petitionInfoService;
    @Autowired
    private PollutaEventDetailInfoMapper pollutaEventDetailInfoMapper;
    @Autowired
    private TraceSourceEntInfoMapper traceSourceEntInfoMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;
    @Autowired
    private PollutantSmellMapper pollutantSmellMapper;


    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 1:19
     * @Description:通过主键删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId, fkpetitoinid]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        TraceSourceEventInfoVO traceSourceEventInfoVO = traceSourceEventInfoMapper.selectByPrimaryKey(pkId);
        if(PetitionEnum.getCode()==traceSourceEventInfoVO.getEventtype()){
            petitionInfoService.deleteByPrimaryKey(traceSourceEventInfoVO.getFkPetitionid());
        }else if(AlarmDataEnum.getCode()==traceSourceEventInfoVO.getEventtype()){
            pollutaEventDetailInfoMapper.deleteByPolluteeventid(traceSourceEventInfoVO.getPkId());
        }
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("eventid",pkId);
        traceSourceEntInfoMapper.deleteByPetitionIdAndResultType(paramMap);
        taskFlowRecordInfoMapper.deleteByTaskid(pkId);
        return traceSourceEventInfoMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 1:19
     * @Description: 通过主键查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public TraceSourceEventInfoVO selectByPrimaryKey(String pkId) {
        return traceSourceEventInfoMapper.selectByPrimaryKey(pkId);
    }

    /**
     *
     * @author: lip
     * @date: 2019/8/14 0014 上午 10:43
     * @Description: 添加实体数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public int insert(TraceSourceEventInfoVO traceSourceEventInfoVO,JSONObject jsonObject) {
        String fkpollutantcode=jsonObject.get("fkpollutantcode")==null?"":jsonObject.getString("fkpollutantcode");
        if(PetitionEnum.getCode()==traceSourceEventInfoVO.getEventtype()){
//            Map<String, Object> smellinfo = pollutantSmellMapper.selectByPollutantcode(fkpollutantcode);
            PetitionInfoVO petitionInfoVO = new PetitionInfoVO();
            petitionInfoVO.setPetitiontitle(traceSourceEventInfoVO.getEventname());
            petitionInfoVO.setUpdatetime(traceSourceEventInfoVO.getUpdatetime());
            petitionInfoVO.setUpdateuser(traceSourceEventInfoVO.getUpdateuser());
            petitionInfoVO.setLongitude(traceSourceEventInfoVO.getLongitude());
            petitionInfoVO.setLatitude(traceSourceEventInfoVO.getLatitude());
            petitionInfoVO.setPkId(traceSourceEventInfoVO.getFkPetitionid());
            petitionInfoVO.setPetitioncontent(traceSourceEventInfoVO.getEventmark());
            petitionInfoVO.setPollutestarttime(traceSourceEventInfoVO.getStarttime());
            petitionInfoVO.setPolluteendtime(traceSourceEventInfoVO.getEndtime());
            petitionInfoVO.setDuration(traceSourceEventInfoVO.getDuration());
            petitionInfoVO.setStatus((short) 1);
            petitionInfoVO.setSubmittime(traceSourceEventInfoVO.getStarttime());
            petitionInfoVO.setSmell(jsonObject.get("smell")==null?"":jsonObject.getString("smell"));
            /*if(smellinfo!=null){
                petitionInfoVO.setSmell(smellinfo.get("Code")==null?"":smellinfo.get("Code").toString());
            }*/
            petitionInfoService.insert(petitionInfoVO);


            PollutaEventDetailInfoVO pollutaEventDetailInfoVO = new PollutaEventDetailInfoVO();
            pollutaEventDetailInfoVO.setPkId(UUID.randomUUID().toString());
            pollutaEventDetailInfoVO.setFkPollutantcode(fkpollutantcode);
            pollutaEventDetailInfoVO.setFkPolluteeventid(traceSourceEventInfoVO.getPkId());
            pollutaEventDetailInfoVO.setUpdatetime(traceSourceEventInfoVO.getUpdatetime());
            pollutaEventDetailInfoVO.setUpdateuser(traceSourceEventInfoVO.getUpdateuser());
            pollutaEventDetailInfoMapper.insert(pollutaEventDetailInfoVO);
        }else if(AlarmDataEnum.getCode()==traceSourceEventInfoVO.getEventtype()){
            PollutaEventDetailInfoVO pollutaEventDetailInfoVO = new PollutaEventDetailInfoVO();
            pollutaEventDetailInfoVO.setPkId(UUID.randomUUID().toString());
            pollutaEventDetailInfoVO.setFkMonitorpointid(jsonObject.get("fkmonitorpointid")==null?"":jsonObject.getString("fkmonitorpointid"));
            pollutaEventDetailInfoVO.setFkPollutantcode(fkpollutantcode);
            pollutaEventDetailInfoVO.setFkPolluteeventid(traceSourceEventInfoVO.getPkId());
            pollutaEventDetailInfoVO.setUpdatetime(traceSourceEventInfoVO.getUpdatetime());
            pollutaEventDetailInfoVO.setUpdateuser(traceSourceEventInfoVO.getUpdateuser());
            pollutaEventDetailInfoMapper.insert(pollutaEventDetailInfoVO);
        }

//        Map<String,Object> data=new HashMap<>();
//        data.put("pk_id",traceSourceEventInfoVO.getPkId());
//        data.put("userid",jsonObject.get("userid"));
//        data.put("status",traceSourceEventInfoVO.getEventstatus());
//        addTraceSourceEventFlowByParamMap(data);
        return traceSourceEventInfoMapper.insert(traceSourceEventInfoVO);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 上午 11:55
     * @Description: 修改数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [traceSourceEventInfoVO, petitionInfoVO]
     * @throws:
     */
    @Override
    public int update(TraceSourceEventInfoVO traceSourceEventInfoVO,JSONObject jsonObject) {
        String fkpollutantcode=jsonObject.get("fkpollutantcode")==null?"":jsonObject.getString("fkpollutantcode");
        if(PetitionEnum.getCode()==traceSourceEventInfoVO.getEventtype()){
//            Map<String, Object> smellinfo = pollutantSmellMapper.selectByPollutantcode(fkpollutantcode);
            /*PetitionInfoVO petitionInfoVO = petitionInfoService.selectByPrimaryKey(traceSourceEventInfoVO.getFkPetitionid());
            if(petitionInfoVO!=null){
                petitionInfoVO.setPetitiontitle(traceSourceEventInfoVO.getEventname());
                petitionInfoVO.setUpdatetime(traceSourceEventInfoVO.getUpdatetime());
                petitionInfoVO.setUpdateuser(traceSourceEventInfoVO.getUpdateuser());
                petitionInfoVO.setLongitude(traceSourceEventInfoVO.getLongitude());
                petitionInfoVO.setLatitude(traceSourceEventInfoVO.getLatitude());
                petitionInfoVO.setPetitioncontent(traceSourceEventInfoVO.getEventmark());
                petitionInfoVO.setPollutestarttime(traceSourceEventInfoVO.getStarttime());
                petitionInfoVO.setPolluteendtime(traceSourceEventInfoVO.getEndtime());
                petitionInfoVO.setDuration(traceSourceEventInfoVO.getDuration());
                petitionInfoVO.setSmell(jsonObject.get("smell")==null?"":jsonObject.getString("smell"));
//            if(smellinfo!=null){
//                petitionInfoVO.setSmell(smellinfo.get("Code")==null?"":smellinfo.get("Code").toString());
//            }
                petitionInfoService.updateByPrimaryKey(petitionInfoVO);

            }*/

            PollutaEventDetailInfoVO pollutaEventDetailInfoVO = pollutaEventDetailInfoMapper.selectByPolluteeventid(traceSourceEventInfoVO.getPkId());
            pollutaEventDetailInfoVO.setFkPollutantcode(fkpollutantcode);
            pollutaEventDetailInfoMapper.updateByPrimaryKey(pollutaEventDetailInfoVO);
        }else if(AlarmDataEnum.getCode()==traceSourceEventInfoVO.getEventtype()){
            PollutaEventDetailInfoVO pollutaEventDetailInfoVO = pollutaEventDetailInfoMapper.selectByPolluteeventid(traceSourceEventInfoVO.getPkId());
            pollutaEventDetailInfoVO.setFkMonitorpointid(jsonObject.get("fkmonitorpointid")==null?"":jsonObject.getString("fkmonitorpointid"));
            pollutaEventDetailInfoVO.setFkPollutantcode(fkpollutantcode);
            pollutaEventDetailInfoMapper.updateByPrimaryKey(pollutaEventDetailInfoVO);
        }
        return traceSourceEventInfoMapper.updateByPrimaryKey(traceSourceEventInfoVO);
    }

    @Override
    public int updateByParamMap(Map<String, Object> paramMap) {
        return traceSourceEventInfoMapper.updateByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 上午 10:33
     * @Description: 通过自定义条件查询溯源事件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<TraceSourceEventInfoVO> getTraceSourceEventInfoByParamMap(Map<String, Object> paramMap) {
        return traceSourceEventInfoMapper.getTraceSourceEventInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 2:17
     * @Description: 通过自定义参数获取污染事件及污染事件详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<TraceSourceEventInfoVO> getTraceSourceEventAndDetailByParamMap(Map<String, Object> paramMap) {
        return traceSourceEventInfoMapper.getTraceSourceEventAndDetailByParamMap(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/9/20 0020 下午 1:22
     * @Description: 通过id获取污染事件详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public TraceSourceEventInfoVO getTraceSourceEventDetailById(Map<String, Object> paramMap) {
        return traceSourceEventInfoMapper.getTraceSourceEventDetailById(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/9/23 0023 上午 9:48
     * @Description: 修改污染事件以及溯源企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public void updateEventAndPollution(Map<String, Object> paramMap) {
        Date updatetime = new Date();
        List<String> pollutionids=(List)paramMap.get("pollutionids");
        String eventid=paramMap.get("petitionid")==null?"":paramMap.get("petitionid").toString();
//        String committype=paramMap.get("committype")==null?"":paramMap.get("committype").toString();
//        Integer resulttype=paramMap.get("resulttype")==null?-1:Integer.valueOf(paramMap.get("resulttype").toString());
        String result=paramMap.get("consultationresult")==null?"":paramMap.get("consultationresult").toString();
        String username=paramMap.get("username")==null?"":paramMap.get("username").toString();
//        String userid=paramMap.get("userid")==null?"":paramMap.get("userid").toString();



        TraceSourceEventInfoVO traceSourceEventInfoVO = traceSourceEventInfoMapper.selectByPrimaryKey(eventid);
        traceSourceEventInfoVO.setConsultationresult(result);

        if(paramMap.get("voyageendtime")!=null && paramMap.get("voyagestarttime")!=null){
            traceSourceEventInfoVO.setVoyagestarttime(paramMap.get("voyagestarttime").toString());
            traceSourceEventInfoVO.setVoyageendtime(paramMap.get("voyageendtime").toString());
        }

//        short status =(short) (resulttype+1);
//        if("commit".equals(committype) || "update".equals(committype)){
            //如果为提交操作或者只更新事件状态时修改事件状态  如果为暂存操作没有其他操作
//            traceSourceEventInfoVO.setEventstatus(status);
            //流程表中添加信息
//            Map<String,Object> data=new HashMap<>();
//            data.put("pk_id",eventid);
//            data.put("userid",userid);
//            data.put("status",status);
//            addTraceSourceEventFlowByParamMap(data);

//        }
        //修改污染事件
        traceSourceEventInfoMapper.updateByPrimaryKey(traceSourceEventInfoVO);

        //修改溯源企业
        if(pollutionids!=null && pollutionids.size()>0){
            Map<String,Object> paramMaps=new HashMap<>();
            paramMaps.put("eventid",eventid);
//            paramMaps.put("resulttype",status-1);
            traceSourceEntInfoMapper.deleteByPetitionIdAndResultType(paramMaps);
            List<TraceSourceEntInfoVO> list=new ArrayList<>();
            for (int i = 0; i < pollutionids.size(); i++) {
                TraceSourceEntInfoVO traceSourceEntInfoVO = new TraceSourceEntInfoVO();
                traceSourceEntInfoVO.setFkPolluteeventid(eventid);
                traceSourceEntInfoVO.setFkPollutionid(pollutionids.get(i));
                traceSourceEntInfoVO.setPkId(UUID.randomUUID().toString());
                traceSourceEntInfoVO.setRanking(i+1);
//                traceSourceEntInfoVO.setResulttype((short)(status-1));
                traceSourceEntInfoVO.setResulttype((short)3);
                traceSourceEntInfoVO.setUpdatetime(updatetime);
                traceSourceEntInfoVO.setUpdateuser(username);
                list.add(traceSourceEntInfoVO);
            }

            traceSourceEntInfoMapper.insertEntInfoBatch(list);
        }


    }

    /**
     * @author: xsm
     * @date: 2019/9/23 0023 上午 9:40
     * @Description: 根据投诉事件状态和任务事件ID修改投诉事件状态并保存流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addTraceSourceEventFlowByParamMap(Map<String, Object> paramMap) {

        try {
            String id = paramMap.get("pk_id").toString();
            String statusname="";
            //根据事件状态判断
            if ((paramMap.get("status").toString()).equals(CommonTypeEnum.TraceSourceEventStatusEnum.PlatformTraceSourceEnum.getCode().toString())){
                //平台溯源
                statusname = CommonTypeEnum.TraceSourceEventStatusEnum.PlatformTraceSourceEnum.getName();
            }else if ((paramMap.get("status").toString()).equals(CommonTypeEnum.TraceSourceEventStatusEnum.VoyageTraceSourceEnum.getCode().toString())){
                //走航溯源
                statusname = CommonTypeEnum.TraceSourceEventStatusEnum.VoyageTraceSourceEnum.getName();
            }else if ((paramMap.get("status").toString()).equals(CommonTypeEnum.TraceSourceEventStatusEnum.ConsultationEnum.getCode().toString())){
                //溯源会商
                statusname = CommonTypeEnum.TraceSourceEventStatusEnum.ConsultationEnum.getName();
            }else if ((paramMap.get("status").toString()).equals(CommonTypeEnum.TraceSourceEventStatusEnum.CompleteEnum.getCode().toString())){
                //完成
                statusname = CommonTypeEnum.TraceSourceEventStatusEnum.CompleteEnum.getName();
            }
                //事件
                TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
                taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());//主键ID
                taskFlowRecordInfo.setFkTaskid(id);//任务ID
                taskFlowRecordInfo.setFkTaskhandleuserid(paramMap.get("userid").toString());//处理事件人ID
                taskFlowRecordInfo.setCurrenttaskstatus(statusname);//事件状态
                taskFlowRecordInfo.setFkTasktype(CommonTypeEnum.TaskTypeEnum.TraceSourceEvent.getCode().toString());//事件类型
                taskFlowRecordInfo.setTaskhandletime(new Date());//处理事件时间
                taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: xsm
     * @date: 2019/9/23 0023 上午 11:50
     * @Description: 溯源事件流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceEventFlowInfoByID(String pkid) {
        List<Map<String, Object>> resultlist = new ArrayList<>();
        //根据事件ID获取相关流程信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(pkid);
        for (Map<String, Object> obj : datalist) {
            //1.平台溯源
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.TraceSourceEventStatusEnum.PlatformTraceSourceEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("username", obj.get("User_Name"));
                map1.put("taskstatus", CommonTypeEnum.TraceSourceEventStatusEnum.PlatformTraceSourceEnum.getName());
                map1.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map1);
            }
            //2.走航监测
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.TraceSourceEventStatusEnum.VoyageTraceSourceEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("username", obj.get("User_Name"));
                map1.put("taskstatus", CommonTypeEnum.TraceSourceEventStatusEnum.VoyageTraceSourceEnum.getName());
                map1.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map1);
            }
            //3.会商
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.TraceSourceEventStatusEnum.ConsultationEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("username", obj.get("User_Name"));
                map1.put("taskstatus", CommonTypeEnum.TraceSourceEventStatusEnum.ConsultationEnum.getName());
                map1.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map1);
            }
            //4.完成
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.TraceSourceEventStatusEnum.CompleteEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map3 = new HashMap<>();
                map3.put("username", obj.get("User_Name"));
                map3.put("taskstatus", CommonTypeEnum.TraceSourceEventStatusEnum.CompleteEnum.getName());
                map3.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map3);
            }
        }
        if (resultlist!=null&&resultlist.size()>0) {
            //按时间正序排
            Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("taskhandletime").toString());
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebytime).collect(Collectors.toList());
            return collect;
        }
        return resultlist;
    }


    /**
     * @author: chengzq
     * @date: 2019/9/24 0024 上午 9:35
     * @Description: 通过事件id获取会商结果
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [eventid]
     * @throws:
     */
    @Override
    public LinkedHashSet<Map<String,Object>> getConsultationResultByEventId(String eventid) {
        return traceSourceEntInfoMapper.getConsultationResultByEventId(eventid);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 4:49
     * @Description: 通过自定义参数获取溯源事件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String,Object>> selectTraceEventInfoByParamMap(Map<String,Object> paramMap){
        return traceSourceEventInfoMapper.selectTraceEventInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/07/07 0007 下午 2:20
     * @Description: 根据事件ID获取历史走航数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [eventid]
     * @throws:
     */
    @Override
    public Map<String, Object> getHistoryNavigationDataByEventID(Map<String, Object> paramMap) {
        return traceSourceEventInfoMapper.getHistoryNavigationDataByEventID(paramMap);
    }

    @Override
    public List<Map<String, Object>> countEventTypeDataByYear(String year) {
        return traceSourceEventInfoMapper.countEventTypeDataByYear(year);
    }

}
