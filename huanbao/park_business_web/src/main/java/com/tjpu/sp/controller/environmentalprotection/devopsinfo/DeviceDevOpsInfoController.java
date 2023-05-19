package com.tjpu.sp.controller.environmentalprotection.devopsinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DeviceDevOpsInfoVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DeviceDevOpsInfoService;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import net.sf.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("deviceDevOpsInfo")
public class DeviceDevOpsInfoController {
    @Autowired
    private DeviceDevOpsInfoService deviceDevOpsInfoService;
    @Autowired
    private RabbitSender rabbitSender;

    /**
     * @author: xsm
     * @date: 2019/12/04 0004 下午 2:44
     * @Description: 根据自定义参数获取设备报备列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getDeviceDevOpsInfosByParamMap", method = RequestMethod.POST)
    public Object getDeviceDevOpsInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = deviceDevOpsInfoService.getDeviceDevOpsInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
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
     * @date: 2019/12/05 0005 下午 1:16
     * @Description: 新增设备报备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addDeviceDevOpsInfo", method = RequestMethod.POST)
    public Object addDeviceDevOpsInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            DeviceDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DeviceDevOpsInfoVO.class);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkId(pkid);
            entity.setDevopspeople(userId);
            entity.setCreatetime(new Date());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            deviceDevOpsInfoService.addDeviceDevOpsInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/28 0028 上午 8:51
     * @Description: 发送消息到雨水监控队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToRabbit(JSONObject jsonObject) {
        MessageProperties properties = new MessageProperties();
        Message message = new Message(jsonObject.toString().getBytes(), properties);
        rabbitSender.sendMessage(RabbitMqConfig.RAIN_MONITOR_DIRECT_EXCHANGE, RabbitMqConfig.RAIN_MONITOR_DIRECT_KEY, message);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:16
     * @Description: 修改设备报备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "editDeviceDevOpsInfo", method = RequestMethod.POST)
    public Object editDeviceDevOpsInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            DeviceDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DeviceDevOpsInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            deviceDevOpsInfoService.editDeviceDevOpsInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:30
     * @Description: 根据主键ID获取运维设备详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDeviceDevOpsInfoDetailByID", method = RequestMethod.POST)
    public Object getDeviceDevOpsInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = deviceDevOpsInfoService.getDeviceDevOpsInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/03 0003 上午 9:09
     * @Description: 删除运维记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deleteDeviceDevOpsByID", method = RequestMethod.POST)
    public Object deleteDeviceDevOpsByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            deviceDevOpsInfoService.deleteDeviceDevOpsInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 2:21
     * @Description: 根据污染源id和监测类型获取排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointDataByPollutionIDAndType", method = RequestMethod.POST)
    public Object getMonitorPointDataByPollutionIDAndType(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                          @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                                          @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes) throws Exception {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<String> pollutionids = new ArrayList<>();
            if (pollutionid!=null&&!"".equals(pollutionid)) {
                pollutionids.add(pollutionid);
            }
            if (monitorpointtypes==null||monitorpointtypes.size()==0) {
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype!=null) {
                    monitorpointtypes.add(Integer.valueOf(monitorpointtype));
                }
            }
            for (Integer i:monitorpointtypes){
                dataList.addAll(deviceDevOpsInfoService.getMonitorPointDataByPollutionIDAndType(pollutionids, i));
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/03 0003 上午 9:09
     * @Description: 根据污染源id获取污染源关联的所有点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEntMonitorPointDataByPollutionID", method = RequestMethod.POST)
    public Object getEntMonitorPointDataByPollutionID(@RequestJson(value = "pollutionid") String pollutionid ) throws Exception {

        try {
           List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> dataList = deviceDevOpsInfoService.getEntMonitorPointDataByPollutionID(paramMap);
            if (dataList != null && dataList.size() > 0) {
                Set set = new HashSet();
                for (Map<String, Object> map:dataList){
                    if (!set.contains(map.get("MonitorPointTypeName").toString())) {
                        List<Map<String, Object>> points = new ArrayList<>();
                        for (Map<String, Object> map1:dataList){
                            if (map.get("MonitorPointTypeName").toString().equals(map1.get("MonitorPointTypeName").toString())) {
                                points.add(map1);
                            }
                        }
                        Map<String,Object> map2 = new HashMap<>();
                        map2.put("monitorpointtypename",map.get("MonitorPointTypeName").toString());
                        map2.put("monitorpointtypecode",map.get("FK_MonitorPointType").toString());
                        map2.put("pointdatas",points);
                        result.add(map2);
                        set.add(map.get("MonitorPointTypeName").toString());
                    }else{
                        continue;
                    }
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
     * @date: 2019/12/05 0005 下午 2:37
     * @Description: 根据监测点id和监测类型获取监测点监测的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointPollutantDataByIDAndType", method = RequestMethod.POST)
    public Object getMonitorPointPollutantDataByIDAndType(@RequestJson(value = "monitorpointid") String monitorpointid,
                                                          @RequestJson(value = "monitorpointtype") String monitorpointtype) throws Exception {

        try {
            List<Map<String, Object>> dataList = deviceDevOpsInfoService.getMonitorPointPollutantDataByIDAndType(monitorpointid, Integer.parseInt(monitorpointtype));
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/12/04 0004 下午 2:44
     * @Description: 根据自定义参数获取设备运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getDeviceDevOpsHistoryInfosByParamMap", method = RequestMethod.POST)
    public Object getDeviceDevOpsHistoryInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = (Map<String, Object>) JSONObject.fromObject(paramsjson);
            //获取总条数
            Long countall = deviceDevOpsInfoService.getAllDeviceDevOpsHistoryInfoCountByParams(paramMap);
            List<Map<String, Object>> dataList = deviceDevOpsInfoService.getDeviceDevOpsHistoryInfosByParamMap(paramMap);
            resultMap.put("datalist", dataList);
            resultMap.put("total", countall);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/12/18 0018 下午 3:38
     * @Description: 获取当前运维设备数目
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "countDeviceDevOpsHistoryByParamMap", method = RequestMethod.POST)
    public Object countDeviceDevOpsHistoryByParamMap() throws Exception {
        try {
            Map<String,Object> resultMap=new HashMap<>();
            Map<String,Object> data=new HashMap<>();
            List<Map<String,Object>> datalist=new ArrayList<>();
            //获取总条数
            Long countall = deviceDevOpsInfoService.getAllDeviceDevOpsInfoCountByParams(new HashMap<>());
            data.put("count", countall);
            data.put("name", "当前运维设备");
            data.put("sysmodel", "thisDevOps");
            datalist.add(data);
            resultMap.put("datalist",datalist);
            resultMap.put("sum",countall);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/01/09 0009 下午 3:19
     * @Description: 根据自定义参数获取相关运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime endtime yyyy-MM-dd HH:mm
     * @throws:
     */
    @RequestMapping(value = "getDeviceDevOpsHistoryListDataByParamMap", method = RequestMethod.POST)
    public Object getDeviceDevOpsHistoryListDataByParamMap(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                           @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                           @RequestJson(value = "starttime", required = false) String starttime,
                                                           @RequestJson(value = "endtime", required = false) String endtime,
                                                           @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                           @RequestJson(value = "pagesize", required = false) Integer pagesize) throws Exception {
        try {
            Map<String,Object> parammap=new HashMap<>();
            parammap.put("monitorpointtype",monitorpointtype);
            parammap.put("monitorpointid",monitorpointid);
            parammap.put("starttime",starttime+":00");
            parammap.put("endtime",endtime+":59");
            Map<String,Object> resultMap=new HashMap<>();
            if (pagenum!=null&&pagesize!=null){
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> datalist = deviceDevOpsInfoService.getDeviceDevOpsHistoryListDataByParamMap(parammap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/4/9 0009 下午 2:25
     * @Description: 通过企业id，运维时间查询运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, starttime, endtime, pagenum, pagesize]
     * @throws:
     */
    @RequestMapping(value = "getDeviceDevOpsInfoByParams", method = RequestMethod.POST)
    public Object getDeviceDevOpsInfoByParams(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                               @RequestJson(value = "starttime", required = false) String starttime,
                                               @RequestJson(value = "endtime", required = false) String endtime,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                               @RequestJson(value = "pagesize", required = false) Integer pagesize) throws Exception {
        try {
            Map<String,Object> parammap=new HashMap<>();
            Map<String,Object> resultMap=new HashMap<>();

            parammap.put("fkpollutionid",fkpollutionid);
            parammap.put("starttime",starttime);
            parammap.put("endtime",endtime);

            List<Map<String, Object>> deviceDevOpsInfoByParamMap = deviceDevOpsInfoService.getDeviceDevOpsInfoByParamMap(parammap);
            resultMap.put("total", deviceDevOpsInfoByParamMap.size());

            if(pagenum!=null && pagesize!=null){
                deviceDevOpsInfoByParamMap=deviceDevOpsInfoByParamMap.stream().skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("datalist", deviceDevOpsInfoByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/02/24 0024 下午 2:25
     * @Description: 通过运维记录ID获取运维详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDeviceDevOpsDetailByID", method = RequestMethod.POST)
    public Object getDeviceDevOpsDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> resultMap = deviceDevOpsInfoService.getDeviceDevOpsDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/01 0001 下午 2:34
     * @Description: 统计某段时间例行运维完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countDeviceDevOpsCompletionDataByParam", method = RequestMethod.POST)
    public Object countDeviceDevOpsCompletionDataByParam(@RequestJson(value = "monitorpointid" ,required = false) String monitorpointid,
                                                         @RequestJson(value = "devopstype") Integer devopstype,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String,Object> parammap = new HashMap<>();
            parammap.put("monitorpointid",monitorpointid);
            parammap.put("starttime",starttime);
            parammap.put("endtime",endtime);
            parammap.put("devopstype",devopstype);
            List<Map<String,Object>> result = deviceDevOpsInfoService.countDeviceDevOpsCompletionDataByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success",result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/03/02 0002 下午 15:51
     * @Description: 修改设备运维记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateDeviceDevOpsRecordInfo", method = RequestMethod.POST)
    public Object updateDeviceDevOpsRecordInfo(@RequestJson(value = "updateformdata") Object updateformdata,
                                            @RequestJson(value = "dgimn",required = false) String dgimn
    ) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            DeviceDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DeviceDevOpsInfoVO.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            deviceDevOpsInfoService.editDeviceDevOpsInfo(entity);
            //放到消息队列中
            /*jsonObject = new JSONObject();
            jsonObject.put("dgimn", dgimn);
            jsonObject.put("devopsstarttime", DataFormatUtil.getDateYMDHMS(entity.getDevopsstarttime()));
            jsonObject.put("endtime", "@");
            jsonObject.put("pollutantcodes", entity.getPollutantcodes());
            jsonObject.put("monitorpointtype", monitorpointtype);
            jsonObject.put("messagetype", CommonTypeEnum.RabbitMQMessageTypeEnum.PollutantMonitorMessage.getCode());
            sendMessageToRabbit(jsonObject);*/
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/03 0003 上午 9:09
     * @Description: 通过运维记录ID删除运维详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deleteDeviceDevOpsInfoByID", method = RequestMethod.POST)
    public Object deleteDeviceDevOpsInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
             deviceDevOpsInfoService.deleteDeviceDevOpsInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/11 0011 上午 11:35
     * @Description: 获取运维监测点位树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDevOpsPointTreeData", method = RequestMethod.POST)
    public Object getDevOpsPointTreeData(@RequestJson(value = "customname",required = false) String customname) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid",userId);
            paramMap.put("customname",customname);
            List<Map<String, Object>> listdata = deviceDevOpsInfoService.getDevOpsPointTreeData(paramMap);
            if (listdata!=null&&listdata.size()>0){
                List<String> parentgroup = new ArrayList<>();
                //获取企业点位信息
                List<Map<String, Object>> pollutiondata = listdata.stream().filter(m -> m.get("branchcode") != null && "pollution".equals(m.get("branchcode").toString())).collect(Collectors.toList());
                //按企业ID分组
                Map<String, List<Map<String, Object>>> collect = new HashMap<>();
                if (pollutiondata!=null&&pollutiondata.size()>0) {
                    collect = pollutiondata.stream().filter(m -> m.get("pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("pollutionid").toString()));
                }
                List<Map<String, Object>> entpointlist;
                for (Map<String, Object> map:listdata){
                    if (map.get("branchcode")!=null&&!parentgroup.contains(map.get("branchcode").toString())){
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("id", UUID.randomUUID().toString());
                        onemap.put("label", map.get("label"));
                        onemap.put("type", map.get("branchcode"));
                        List<Map<String, Object>> pointlist = new ArrayList<>();
                        if ("pollution".equals(map.get("branchcode").toString())){
                            for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                                entpointlist = entry.getValue();
                                List<Map<String, Object>> valuelist = new ArrayList<>();
                                Map<String, Object> entmap = new HashMap<>();
                                entmap.put("id", entry.getKey());
                                entmap.put("label", entpointlist.get(0).get("PollutionName"));
                                entmap.put("type", "ent");
                                for (Map<String, Object> threemap:entpointlist){
                                    Map<String, Object> valuemap = new HashMap<>();
                                    valuemap.put("id", threemap.get("monitorpointid"));
                                    valuemap.put("label",  threemap.get("monitorpointname"));
                                    valuemap.put("entid", threemap.get("pollutionid"));
                                    valuemap.put("type", threemap.get("FK_MonitorPointTypeCode"));
                                    valuemap.put("dgimn", threemap.get("DGIMN"));
                                    valuemap.put("children", new ArrayList<>());
                                    valuelist.add(valuemap);
                                }
                                entmap.put("children", valuelist);
                                pointlist.add(entmap);
                            }
                        }else{
                            for (Map<String, Object> maptwo:listdata){
                                if (map.get("branchcode").toString().equals(maptwo.get("branchcode").toString())) {
                                    Map<String, Object> pointmap = new HashMap<>();
                                    pointmap.put("id", maptwo.get("monitorpointid"));
                                    pointmap.put("label", maptwo.get("monitorpointname"));
                                    pointmap.put("entid", maptwo.get("pollutionid"));
                                    pointmap.put("type", maptwo.get("FK_MonitorPointTypeCode"));
                                    pointmap.put("dgimn", maptwo.get("DGIMN"));
                                    pointmap.put("children", new ArrayList<>());
                                    pointlist.add(pointmap);
                                }
                            }
                        }
                        onemap.put("children", pointlist);
                        parentgroup.add(map.get("branchcode").toString());
                        result.add(onemap);
                    }else{
                        continue;
                    }
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
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取例行运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getRoutineDevOpsInfosByParamMap", method = RequestMethod.POST)
    public Object getRoutineDevOpsInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = deviceDevOpsInfoService.getRoutineDevOpsInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
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
     * @date: 2022/04/12 0012 上午 10:21
     * @Description: 修改例行运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "editRoutineDeviceDevOpsInfo", method = RequestMethod.POST)
    public Object editRoutineDeviceDevOpsInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            DeviceDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DeviceDevOpsInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            deviceDevOpsInfoService.editDeviceDevOpsInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04 0012 上午 10:25
     * @Description: 根据主键ID获取例行运维详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getRoutineDevOpsInfoDetailByID", method = RequestMethod.POST)
    public Object getRoutineDevOpsInfoDetailByID(@RequestJson(value = "id") String id
    ) throws Exception {
        try {
            Map<String, Object> result = deviceDevOpsInfoService.getRoutineDevOpsInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取运维记录统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getDevOpsRecordStatisticsDataByParamMap", method = RequestMethod.POST)
    public Object getDevOpsRecordStatisticsDataByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = deviceDevOpsInfoService.getDevOpsRecordStatisticsDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
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
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 导出运维记录统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "exportDevOpsRecordStatisticsData", method = RequestMethod.POST)
    public void exportDevOpsRecordStatisticsData(@RequestJson(value = "paramsjson", required = true) Object paramsjson,
                                                   HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            List<Map<String, Object>> datalist = deviceDevOpsInfoService.getDevOpsRecordStatisticsDataByParamMap(jsonObject);
            List<Map<String, Object>> tabletitledata = deviceDevOpsInfoService.getDevOpsRecordStatisticsTableTitleData();
            String titlename = "运维记录统计";
            //设置文件名称
            String fileName = titlename + "_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, datalist, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取企业说明列表信息（企业报备）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntExplainInfosByParamMap", method = RequestMethod.POST)
    public Object getEntExplainInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = deviceDevOpsInfoService.getEntExplainInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
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
     * @date: 2022/04/13 0013 上午 8:50
     * @Description: 根据主键ID获取企业说明（企业报备）详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEntExplainInfoDetailByID", method = RequestMethod.POST)
    public Object getEntExplainInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = deviceDevOpsInfoService.getEntExplainInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
