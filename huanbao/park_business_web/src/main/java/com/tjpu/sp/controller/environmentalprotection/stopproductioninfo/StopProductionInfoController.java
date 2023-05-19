package com.tjpu.sp.controller.environmentalprotection.stopproductioninfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.MessageReadUserVO;
import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.StopProductionInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.environmentalprotection.pointofflinerecord.PointOffLineRecordService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("stopProductionInfo")
public class StopProductionInfoController {
    @Autowired
    private StopProductionInfoService stopProductionInfoService;
    @Autowired
    private MonitorControlService monitorControlService;
    @Autowired
    private RabbitSender rabbitSender;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private PointOffLineRecordService pointOffLineRecordService;


    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 6:36
     * @Description: 根据自定义参数获取停产排口列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getStopProductionInfosByParamMap", method = RequestMethod.POST)
    public Object getStopProductionInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            setSortData(jsonObject);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist= stopProductionInfoService.getStopProductionListDataByParamMap(jsonObject);

            //Map<String, List<Map<String, Object>>> collect = datalist.stream().filter(m -> m.get("pkids") != null).collect(Collectors.groupingBy(m -> m.get("pkids").toString()));

           /* datalist.clear();
            for (String pkids : collect.keySet()) {
                List<Map<String, Object>> maps = collect.get(pkids);
                String collect1 = maps.stream().filter(m -> m.get("StopProductionTypeName") != null).map(m -> m.get("StopProductionTypeName").toString()).collect(Collectors.joining("、"));
                Map<String, Object> stringObjectMap = maps.stream().findFirst().orElse(new HashMap<>());
                stringObjectMap.put("StopProductionTypeName",collect1);
                datalist.add(stringObjectMap);
            }*/

            //处理分页数据
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                resultMap.put("datalist", datalist);
                datalist = getPageData(datalist, Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
                resultMap.put("total", datalist.size());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void setSortData(Map<String, Object> jsonObject) {
        if (jsonObject.get("sortdata") != null) {
            Map<String, Object> sortdata = (Map<String, Object>) jsonObject.get("sortdata");
            for (String key : sortdata.keySet()) {
                jsonObject.put("sortkey", key);
                jsonObject.put("sorttype", sortdata.get(key).equals("ascending") ? "asc" : "desc");
            }
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/9/2 0002 下午 6:52
     * @Description: 通过自定义条件获取停产历史数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getStopProductionHistoryByParamMap", method = RequestMethod.POST)
    public Object getStopProductionHistoryByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            Integer pagesize=Integer.MAX_VALUE;
            Integer pagenum=1;
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                pagenum=Integer.valueOf(jsonObject.get("pagenum").toString());
                pagesize=Integer.valueOf(jsonObject.get("pagesize").toString());
            }
            List<Map<String, Object>> datalist= stopProductionInfoService.getStopProductHistory(jsonObject);

            for (Map<String, Object> stringObjectMap : datalist) {
                stringObjectMap.remove("groupid");
                Set<Map<String,Object>> set=stringObjectMap.get("outputinfo")==null?new HashSet<>():(Set)stringObjectMap.get("outputinfo");
                String collect = set.stream().filter(m -> m.get("outputname") != null).map(m -> m.get("outputname").toString()).collect(Collectors.joining("、"));
                stringObjectMap.put("outputnames",collect);
            }

            resultMap.put("total", datalist.size());
            resultMap.put("datalist", datalist.stream().skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList()));
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:16
     * @Description: 停产开始
     * @updateUser:xsm
     * @updateDate:2020/02/25 0025 上午 9:06
     * @updateDescription:保存停产信息
     * @updateUser:xsm
     * @updateDate:2020/09/03 0003 上午 10:56
     * @updateDescription:保存停产信息（多点位）
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addStopProductionInfo", method = RequestMethod.POST)
    public Object addStopProductionInfo(@RequestJson(value = "addformdata") Object addformdata
                                       ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<String> pointidtypes = jsonObject.getJSONArray("idandtypes");
            StopProductionInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), StopProductionInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            Date nowTime = new Date();
            String pointid = "";
            String type = "";
            String dgimn = "";
            List<String> pkids = new ArrayList<>();
            Map<String,String> idandmn = new HashMap<>();
            List<StopProductionInfoVO> objs =  new ArrayList<>();
            for (String str:pointidtypes){
                String[] strs = str.split("_");
                pointid = strs[0];
                dgimn = strs[1];
                type = strs[2];
                idandmn.put(pointid,dgimn);
                StopProductionInfoVO obj = new StopProductionInfoVO();
                String pkid = UUID.randomUUID().toString();
                pkids.add(pkid);
                obj.setPkId(pkid);
                if (entity.getFkStopproductiontype()==null){//为空  默认停产
                    obj.setFkStopproductiontype("1");
                }else{
                    obj.setFkStopproductiontype(entity.getFkStopproductiontype());
                }
                obj.setFkOutputid(pointid);
                obj.setFkRecoveryproductionfileid(entity.getFkRecoveryproductionfileid());
                obj.setRecoveryproductionreason(entity.getRecoveryproductionreason());
                obj.setRecoveryproductiontime(entity.getRecoveryproductiontime());
                obj.setStarttime(nowTime);
                obj.setEndtime(entity.getEndtime());
                obj.setStopproductionremark(entity.getStopproductionremark());
                obj.setFkPollutionid(entity.getFkPollutionid());
                obj.setFkFileid(entity.getFkFileid());
                obj.setFkMonitorpointtype(type);
                obj.setUpdateuser(username);
                obj.setUpdatetime(nowTime);
                objs.add(obj);
            }
            if (objs.size()>0){//批量添加
                stopProductionInfoService.insertStopProductionInfos(objs);
            }
            //推送消息到首页
            boolean effectiveDate = false;
            if (entity.getEndtime()!=null){
                effectiveDate = DataFormatUtil.isEffectiveDate(nowTime, nowTime, entity.getEndtime());
            }else{
                Calendar date = Calendar.getInstance();
                date.setTime(nowTime);
                Calendar begin = Calendar.getInstance();
                begin.setTime(entity.getStarttime());
                if (date.after(begin) || date.before(begin)) {
                    effectiveDate =  true;
                }
            }
            List<Map<String, Object>> resultlist = stopProductionInfoService.getStopProductionInfoByPkids(pkids);
            if (resultlist!=null&&resultlist.size()>0&&effectiveDate==true){
                for (Map<String,Object> resultmap:resultlist){
                //当前时间在范围内
               String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.OutPutStopProductionMessage.getCode();
               jsonObject = new JSONObject();
               jsonObject.put("pk_id",resultmap.get("PK_ID"));
               jsonObject.put("outputname",resultmap.get("outputname"));
               jsonObject.put("pollutionname",resultmap.get("pollutionname"));
               jsonObject.put("endtime",  resultmap.get("EndTime"));
               jsonObject.put("starttime", resultmap.get("StartTime"));
               jsonObject.put("updatetime", resultmap.get("UpdateTime"));
               jsonObject.put("messagestr", resultmap.get("pollutionname")+"设置停产");
               jsonObject.put("MN",resultmap.get("FK_Outputid")!=null?idandmn.get(resultmap.get("FK_Outputid").toString()):"" );
               jsonObject.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.StopProductionMessage.getCode());
               jsonObject.put("isread","0");
               //推送到首页
               rabbitmqController.sendEmissionControlInfo(jsonObject, messageType);
               //放到消息队列中
               JSONObject jsonObjecttwo = new JSONObject();
               jsonObjecttwo.put("monitorpointtype", resultmap.get("FK_MonitorPointType"));
               jsonObjecttwo.put("recoveryproductiontime","");
               jsonObjecttwo.put("monitorpointid", resultmap.get("FK_Outputid"));
               jsonObjecttwo.put("pollutionid", resultmap.get("FK_Pollutionid"));
               jsonObjecttwo.put("stoprecoverytype", "stop");
               jsonObjecttwo.put("starttime",resultmap.get("StartTime"));
               jsonObjecttwo.put("endtime", resultmap.get("EndTime"));
               jsonObjecttwo.put("messagetype", CommonTypeEnum.RabbitMQMessageTypeEnum.StopProductionMessage.getCode());
               sendMessageToRabbit(jsonObjecttwo);

           }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/25 0025 下午 3:28
     * @Description: 保存复产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateStopProductionInfo", method = RequestMethod.POST)
    public Object updateStopProductionInfo(@RequestJson(value = "pkids") List<String> pkids,
                                            @RequestJson(value = "recoveryproductiontime") String recoveryproductiontime,
                                           @RequestJson(value = "fkrecoveryproductionfileid") String fkrecoveryproductionfileid,
                                           @RequestJson(value = "recoveryproductionreason") String recoveryproductionreason
                                            ) throws Exception {
        try {
            if (pkids!=null&&pkids.size()>0) {
                List<StopProductionInfoVO> entitys = stopProductionInfoService.selectByPrimaryKeys(pkids);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                if (entitys != null && entitys.size() > 0) {
                    String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                    Date nowTime = new Date();
                    for (StopProductionInfoVO entity : entitys) {
                        entity.setUpdateuser(username);
                        entity.setUpdatetime(nowTime);
                        entity.setRecoveryproductiontime(simpleDateFormat.parse(recoveryproductiontime+":00.000"));
                        entity.setRecoveryproductionreason(recoveryproductionreason);
                        entity.setFkRecoveryproductionfileid(fkrecoveryproductionfileid);
                        //修改一条停产信息
                        stopProductionInfoService.updateStopProductionInfo(entity);
                        //放到消息队列中
                        JSONObject jsonObjecttwo = new JSONObject();
                        jsonObjecttwo.put("monitorpointtype", entity.getFkMonitorpointtype());
                        jsonObjecttwo.put("recoveryproductiontime",recoveryproductiontime+":00");
                        jsonObjecttwo.put("monitorpointid", entity.getFkOutputid());
                        jsonObjecttwo.put("pollutionid", entity.getFkPollutionid());
                        jsonObjecttwo.put("stoprecoverytype", "recovery");
                        jsonObjecttwo.put("starttime",DataFormatUtil.getDateYMDHMS(entity.getStarttime()));
                        jsonObjecttwo.put("endtime", DataFormatUtil.getDateYMDHMS(entity.getEndtime()));
                        jsonObjecttwo.put("messagetype", CommonTypeEnum.RabbitMQMessageTypeEnum.StopProductionMessage.getCode());
                        sendMessageToRabbit(jsonObjecttwo);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/28 0028 上午 8:51
     * @Description: 发送消息到停产监控队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToRabbit(JSONObject jsonObject) {
        MessageProperties properties = new MessageProperties();
        Message message = new Message(jsonObject.toString().getBytes(), properties);
        rabbitSender.sendMessage(RabbitMqConfig.STOP_PRODUCTION_DIRECT_EXCHANGE, RabbitMqConfig.STOP_PRODUCTION_DIRECT_KEY, message);
    }


    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 9:17
     * @Description: 根据自定义参数获取停产历史信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getStopProductionHistoryInfosByParamMap", method = RequestMethod.POST)
    public Object getStopProductionHistoryInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("sortdata") != null) {
                Map<String, Object> sortdata = (Map<String, Object>) jsonObject.get("sortdata");
                for (String key : sortdata.keySet()) {
                    jsonObject.put("sortkey",key);
                    jsonObject.put("sorttype",sortdata.get(key).equals("ascending")?"asc":"desc");
                }
            }
            List<Map<String, Object>> datalist= stopProductionInfoService.getStopProductionHistoryInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            datalist.stream().filter(m -> m.get("FKFileIDs") != null).peek(m -> {
                if(JSONArray.fromObject(m.get("FKFileIDs")).size()==0){
                    m.put("hasrule","无");
                }else{
                    m.put("hasrule","有");
                }
            }).collect(Collectors.toList());

            //处理分页数据
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                resultMap.put("datalist", datalist);
                datalist = getPageData(datalist, Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
                resultMap.put("total", datalist.size());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 下午 5:03
     * @Description: 根据监测类型获取废气、废水停产信息，雨水排口监控信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getOutPutStopProductionInfosByMonitorPointType", method = RequestMethod.POST)
    public Object getOutPutStopProductionInfosByMonitorPointType(@RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype,
                                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize) throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            List<Map<String, Object>> datalist= new ArrayList<>();
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum!=null&&pagesize!=null){
                PageHelper.startPage(pagenum, pagesize);
            }
            if (monitorpointtype==CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()||monitorpointtype==CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()){
                parammap.put("monitorpointtype",monitorpointtype);
                datalist = stopProductionInfoService.getStopProductionInfosByParamMap(parammap);
            }else if (monitorpointtype==CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()){
                datalist = monitorControlService.getMonitorPointMonitorControlInfo(parammap);
            }
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 上午 9:59
     * @Description: 验证停产信息是否重复
     * @updateUser:xsm
     * @updateDate:2021/10/11 0011 上午 9:29
     * @updateDescription:停产结束时间必传 开始时间非必传 未传则默认当前系统时间
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/isTableDataHaveInfoByParamMap", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByParamMap(@RequestJson(value = "monitorpointids", required = true) List<String> monitorpointids,
                                                @RequestJson(value = "starttime", required = false) String starttime,
                                                @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointids", monitorpointids);
            List<Map<String, Object>> listmap = stopProductionInfoService.getLatestStopProductionInfoByParamMap(paramMap);
            Date start;
            if (StringUtils.isNotBlank(starttime)){
                start = DataFormatUtil.getDateYMDHMS(starttime);
            }else{
                start = DataFormatUtil.getDate();
            }
            Date end = DataFormatUtil.getDateYMDHMS(endtime);
            Calendar cc1 = Calendar.getInstance();
            Calendar cc2 = Calendar.getInstance();
            cc1.setTime(end);
            cc2.setTime(start);
            int iscf= cc1.compareTo(cc2);
            if (iscf < 0){//结束时间 早于开始时间 则验证不通过
                return AuthUtil.parseJsonKeyToLower("success", "yes1");
            }
            if (listmap!=null) {    //等于0 没有此条数据可以添加
                String ishave = "no";
                Map<String, List<Map<String, Object>>> listMap = listmap.stream().collect(Collectors.groupingBy(m -> m.get("FK_Outputid").toString()));
                for (String id :monitorpointids){
                    if (listMap.get(id)!=null){
                        Map<String, Object> map = listMap.get(id).get(0);
                        if (endtime!=null&&!"".equals(endtime)) {//当结束时间不为空  开始和结束时间 都要进行判断
                            //Date start = DataFormatUtil.getDateYMDHMS(starttime);
                            //Date end = DataFormatUtil.getDateYMDHMS(endtime);
                            Date startdate = (Date) map.get("StartTime");
                            if (map.get("EndTime")!=null) {
                                Date enddate = (Date) map.get("EndTime");
                                if (start.getTime() == startdate.getTime() && end.getTime() == enddate.getTime()) {
                                    ishave = "yes";
                                } else {
                                    boolean lateststart = DataFormatUtil.isEffectiveDate(start, startdate, enddate);
                                    boolean latestend = DataFormatUtil.isEffectiveDate(end, startdate, enddate);
                                    boolean startflag = DataFormatUtil.isEffectiveDate(startdate, start, end);
                                    boolean endflag = DataFormatUtil.isEffectiveDate(enddate, start, end);
                                    if (lateststart == true || latestend == true || startflag == true || endflag == true) {//判断开始时间和结束时间最新一条停场数据的时间范围内
                                        //已经有了不添加
                                        ishave = "yes";
                                    }
                                }
                            }else{
                                ishave = "yes";//已有永久停产记录  无法再进行停产操作
                            }
                        }else {//当结束时间为空  永久停产时
                            //Date start = DataFormatUtil.getDateYMDHMS(starttime);
                            Date startdate = (Date) map.get("StartTime");
                            if (map.get("EndTime") != null) {//已有记录是否为永久停产记录
                                Date enddate = (Date) map.get("EndTime");
                                if (start.getTime() == startdate.getTime()) {
                                    return AuthUtil.parseJsonKeyToLower("success", "yes");
                                } else {
                                    Calendar c1 = Calendar.getInstance();
                                    Calendar c2 = Calendar.getInstance();
                                    c1.setTime(enddate);
                                    c2.setTime(start);
                                    int result = c1.compareTo(c2);
                                    if (result >= 0){
                                        ishave = "yes";
                                    }
                                }
                            }else{
                                    ishave = "yes";//已有永久停产记录  无法再进行停产操作
                                }
                        }

                    }
                }
                return AuthUtil.parseJsonKeyToLower("success", ishave);
                } else{
                    return AuthUtil.parseJsonKeyToLower("success", "no");
                }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 下午 3:23
     * @Description: 根据id获取停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getStopProductionInfoByID", method = RequestMethod.POST)
    public Object getStopProductionInfoByID(@RequestJson(value = "id", required = true) String id) {
        try {
            Map<String, Object> resultmap = stopProductionInfoService.getStopProductionInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 下午 3:23
     * @Description: 根据id删除停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteStopProductionInfoByIDs", method = RequestMethod.POST)
    public Object deleteStopProductionInfoByIDs(@RequestJson(value = "pkids", required = true) List<String> pkids) {
        try {
             stopProductionInfoService.deleteStopProductionInfoByIDs(pkids);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/03 0003 下午 1:36
     * @Description: 根据主键ID数组获取复产页面回显数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getReproductionInfoByIDs", method = RequestMethod.POST)
    public Object getReproductionInfoByIDs(@RequestJson(value = "pkids", required = true) List<String> pkids) {
        try {
            Map<String,Object> resultmap = stopProductionInfoService.getReproductionInfoByIDs(pkids);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/03 0003 下午 1:36
     * @Description: 根据主键ID数组获取历史停产详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getHistoryStopProductionDetailByIDs", method = RequestMethod.POST)
    public Object getHistoryStopProductionDetailByIDs(@RequestJson(value = "pkids", required = true) List<String> pkids) {
        try {
            List<Map<String,Object>> result = stopProductionInfoService.getHistoryStopProductionDetailByIDs(pkids);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 下午 5:03
     * @Description: 根据监测类型获取废气、废水停产信息，雨水排口监控信息
     * @updateUser:xsm
     * @updateDate: 2021/05/14 0014 上午 11:45
     * @updateDescription:只查未读信息  加入点位离线数据
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getOutPutStopProductionAndRainMonitorData", method = RequestMethod.POST)
    public Object getOutPutStopProductionAndRainMonitorData() throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            parammap.put("userid",userid);
            //排口停产
            parammap.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.StopProductionMessage.getCode());
            List<Map<String, Object>> datalist = stopProductionInfoService.getNowStopProductionInfosByParamMap(parammap);
            //点位离线
            parammap.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
            List<Map<String, Object>> offlist = pointOffLineRecordService.getNowPointOffLineRecordsByParamMap(parammap);
            datalist.addAll(offlist);
            //雨水排放
            parammap.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.RainDischargeMessage.getCode());
            List<JSONObject>  objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth",  List.class);
            boolean havemenu =  rabbitmqController.isHaveMenuAuthor(objectList, CommonTypeEnum.menuAuthorityControlMenusEnum.RainMonitorEnum.getCode());
            if (havemenu==true) {//有雨水排放控制菜单权限
                List<Map<String, Object>> raindatalist = monitorControlService.getNowRainMonitorControlInfo(parammap);
                datalist.addAll(raindatalist);
            }
            int noreadnum =0;
           if (datalist.size()>0){
               for (Map<String, Object> map:datalist){
                   if (map.get("isread")!=null&&"0".equals(map.get("isread").toString())){
                       noreadnum+=1;
                       result.add(map);
                   }
               }
           }

           if (result!=null&&result.size()>0) {
               Comparator<Object> comparebyisread = Comparator.comparing(m -> ((Map) m).get("isread").toString());
               Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("UpdateTime").toString()).reversed();
               Comparator<Object> finalComparator = comparebyisread.thenComparing(comparebytime);
               List<Map<String, Object>> collect = result.stream().sorted(finalComparator).collect(Collectors.toList());
               resultMap.put("datalist", collect);
           }else{
               resultMap.put("datalist", result);
           }
            resultMap.put("noreadnum", noreadnum);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/18 0018 下午 3:48
     * @Description: 保存首页已读的停产信息（排放信息）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/addMessageReadInfoByParamMap", method = RequestMethod.POST)
    public Object addMessageReadInfoByParamMap(@RequestJson(value = "id", required = true) String id,
                                               @RequestJson(value = "messagetype", required = true) String messagetype
                                                ) {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            MessageReadUserVO obj =new MessageReadUserVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setUserid(userid);
            obj.setFkRecordid(id);
            obj.setMessagetype(messagetype);
            stopProductionInfoService.addMessageReadUserInfo(obj);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
     * @date: 2022/05/23 0023 上午 9:19
     * @Description: 根据自定义参数获取停产企业列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntStopProductionInfosByParamMap", method = RequestMethod.POST)
    public Object getEntStopProductionInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = stopProductionInfoService.getEntStopProductionInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:16
     * @Description: 保存企业停产信息
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addEntStopProductionInfo", method = RequestMethod.POST)
    public Object addEntStopProductionInfo(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            StopProductionInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), StopProductionInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            Date nowTime = new Date();
            StopProductionInfoVO obj = new StopProductionInfoVO();
            String pkid = UUID.randomUUID().toString();
            obj.setPkId(pkid);
            obj.setStarttime(entity.getStarttime());
            obj.setEndtime(entity.getEndtime());
            obj.setFkStopproductiontype(entity.getFkStopproductiontype());
            obj.setStopproductionremark(entity.getStopproductionremark());
            obj.setFkPollutionid(entity.getFkPollutionid());
            obj.setFkFileid(entity.getFkFileid());
            obj.setUpdateuser(username);
            obj.setUpdatetime(nowTime);
            stopProductionInfoService.addEntStopProductionInfo(obj);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/23 0023 上午 10:41
     * @Description: 根据主键ID获取企业停产详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getEntStopProductionDetailByID", method = RequestMethod.POST)
    public Object getEntStopProductionDetailByID(@RequestJson(value = "id", required = true) String id) {
        try {
            Map<String,Object> result = stopProductionInfoService.getEntStopProductionDetailByID(id);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 下午 3:23
     * @Description: 根据id删除停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteEntStopProductionInfoByID", method = RequestMethod.POST)
    public Object deleteEntStopProductionInfoByID(@RequestJson(value = "id", required = true)String id) {
        try {
            stopProductionInfoService.deleteEntStopProductionInfoByID(id);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/23 0023 上午 10:48
     * @Description: 修改停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateEntStopProductionInfo", method = RequestMethod.POST)
    public Object updateEntStopProductionInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            StopProductionInfoVO obj = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), StopProductionInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            Date nowTime = new Date();
            obj.setUpdateuser(username);
            obj.setUpdatetime(nowTime);
            stopProductionInfoService.updateEntStopProductionInfo(obj);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
