package com.tjpu.sp.controller.environmentalprotection.tracesource;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceEventInfoVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutaEventDetailInfoService;
import com.tjpu.sp.service.environmentalprotection.tracesource.TraceSourceEventInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: lip
 * @date: 2019/8/13 0013 下午 3:58
 * @Description: 溯源事件处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("traceSourceEvent")
public class TraceSourceEventController {
    @Autowired
    private TraceSourceEventInfoService traceSourceEventInfoService;

    private final String sysmodel = "PollutantEvent";
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private PollutaEventDetailInfoService pollutaEventDetailInfoService;

    private String pk_id = "pk_id";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author:chengzq
     * @date: 2019/8/14 0014 下午 2:56
     * @Description: 获取污染事件配置列表页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getTraceSourceEventListPage", method = RequestMethod.POST)
    public Object getTraceSourceEventListPage(@RequestBody Object map) {
        try {
            JSONObject resultMap = JSONObject.fromObject(map);
            PageHelper.startPage(Integer.valueOf(resultMap.get("pagenum").toString()), Integer.valueOf(resultMap.get("pagesize").toString()));
            List<TraceSourceEventInfoVO> traceSourceEventInfoByParamMap = traceSourceEventInfoService.getTraceSourceEventInfoByParamMap(new HashMap<>());
            PageInfo<Map<String, Object>> pageInfo = new PageInfo(traceSourceEventInfoByParamMap);

            long total = pageInfo.getTotal();
            resultMap.put("data", traceSourceEventInfoByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:29
     * @Description: 通过自定义参数获取污染事件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceEventByParamMap", method = RequestMethod.POST)
    public Object getTraceSourceEventByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(map);
            PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum") == null ? "" : jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize") == null ? Integer.MAX_VALUE + "" : jsonObject.get("pagesize").toString()));
            List<TraceSourceEventInfoVO> traceSourceEventInfoByParamMap = traceSourceEventInfoService.getTraceSourceEventInfoByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo(traceSourceEventInfoByParamMap);

            long total = pageInfo.getTotal();
            resultMap.put("data", traceSourceEventInfoByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/13 0013 下午 4:21
     * @Description: 新建污染事件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addTraceSourceEvent", method = RequestMethod.POST)
    public Object addTraceSourceEvent(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String petitionid = UUID.randomUUID().toString();
            String eventid = UUID.randomUUID().toString();
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            TraceSourceEventInfoVO traceSourceEventInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TraceSourceEventInfoVO());
            traceSourceEventInfoVO.setPkId(eventid);
            traceSourceEventInfoVO.setEventstatus((short) 1);
            traceSourceEventInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            traceSourceEventInfoVO.setUpdateuser(username);
            traceSourceEventInfoVO.setFkPetitionid(petitionid);

            Calendar instance = Calendar.getInstance();
            instance.setTime(DataFormatUtil.getDateYMDHM(traceSourceEventInfoVO.getStarttime()));
            instance.add(Calendar.MINUTE, traceSourceEventInfoVO.getDuration());
            Date endtime = instance.getTime();
            traceSourceEventInfoVO.setEndtime(DataFormatUtil.getDateYMDHMS(endtime));


            jsonObject.put("userid", userid);
            traceSourceEventInfoService.insert(traceSourceEventInfoVO, jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", eventid);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 下午 5:29
     * @Description: 通过主键获取污染事件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getTraceSourceEventById", method = RequestMethod.POST)
    public Object getTraceSourceEventById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pkId", id);
            List<TraceSourceEventInfoVO> traceSourceEventAndDetailByParamMap = traceSourceEventInfoService.getTraceSourceEventAndDetailByParamMap(paramMap);
            if (traceSourceEventAndDetailByParamMap.size() > 0) {
                return AuthUtil.parseJsonKeyToLower("success", traceSourceEventAndDetailByParamMap.get(0));
            }
            return AuthUtil.parseJsonKeyToLower("success", new TraceSourceEventInfoVO());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 上午 11:56
     * @Description:修改污染事件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [formdata, session]
     * @throws:
     */
    @RequestMapping(value = "updateTraceSourceEvent", method = RequestMethod.POST)
    public Object updateTraceSourceEvent(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            TraceSourceEventInfoVO traceSourceEventInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TraceSourceEventInfoVO());
            traceSourceEventInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            traceSourceEventInfoVO.setUpdateuser(username);

            Calendar instance = Calendar.getInstance();
            instance.setTime(DataFormatUtil.getDateYMDHM(traceSourceEventInfoVO.getStarttime()));
            instance.add(Calendar.MINUTE, traceSourceEventInfoVO.getDuration());
            Date endtime = instance.getTime();
            traceSourceEventInfoVO.setEndtime(DataFormatUtil.getDateYMDHMS(endtime));

            traceSourceEventInfoService.update(traceSourceEventInfoVO, jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/5/8 0008 下午 3:56
     * @Description: 通过名称修改经纬度及走航路径json信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [formdata]
     * @throws:
     */
    @RequestMapping(value = "updateTraceSourceEventInfo", method = RequestMethod.POST)
    public Object updateTraceSourceEventInfo(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            traceSourceEventInfoService.updateByParamMap(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 下午 1:13
     * @Description: 通过主键id删除污染事件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteTraceSourceEventById", method = RequestMethod.POST)
    public Object deleteTraceSourceEventById(@RequestJson(value = "id") String id) {
        try {
            traceSourceEventInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 3:06
     * @Description: 通过id获取污染事件详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getTraceSourceEventDetailById", method = RequestMethod.POST)
    public Object getTraceSourceEventDetailById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("pkId", id);
            TraceSourceEventInfoVO traceSourceEventDetailById = traceSourceEventInfoService.getTraceSourceEventDetailById(map);

            List<Map<String, Object>> record = traceSourceEventInfoService.getTraceSourceEventFlowInfoByID(id);

            Map<String, Object> resultMap = new HashMap<>();

            resultMap.put("detail", traceSourceEventDetailById);
            resultMap.put("record", record);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/23 0023 上午 10:50
     * @Description: 通过自定义参数修改污染事件以及溯源企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "updateEventAndPollutionInfo", method = RequestMethod.POST)
    public Object updateEventAndPollutionInfo(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {

            JSONObject jsonObject = JSONObject.fromObject(paramsjson);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            jsonObject.put("username", username);
            jsonObject.put("userid", userid);
            traceSourceEventInfoService.updateEventAndPollution(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/24 0024 上午 9:28
     * @Description: 通过污染事件id获取会商结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, session]
     * @throws:
     */
    @RequestMapping(value = "getConsultationResultByEventId", method = RequestMethod.POST)
    public Object getConsultationResultByEventId(@RequestJson(value = "eventid") String eventid) {
        try {
            LinkedHashSet<Map<String, Object>> consultationResultByEventId = traceSourceEventInfoService.getConsultationResultByEventId(eventid);
            return AuthUtil.parseJsonKeyToLower("success", consultationResultByEventId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计溯源类型
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/14 14:38
     */
    @RequestMapping(value = "countEventTypeDataByYear", method = RequestMethod.POST)
    public Object countEventTypeDataByYear(@RequestJson(value = "year") String year) {
        try {
            List<Map<String, Object>> resultList = traceSourceEventInfoService.countEventTypeDataByYear(year);


            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 4:51
     * @Description: 通过自定义参数获取溯源事件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [eventid]
     * @throws:
     */
    @RequestMapping(value = "selectTraceEventInfoByParamMap", method = RequestMethod.POST)
    public Object selectTraceEventInfoByParamMap(@RequestJson(value = "eventid", required = false) String eventid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pkid", eventid);
            List<Map<String, Object>> list = traceSourceEventInfoService.selectTraceEventInfoByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getHistoryNavigationDataByEventID", method = RequestMethod.POST)
    public Object getHistoryNavigationDataByEventID(@RequestJson(value = "eventid") String eventid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pkid", eventid);
            Map<String, Object> map = traceSourceEventInfoService.getHistoryNavigationDataByEventID(paramMap);
            if (map != null) {
                JSON jsonObject = JSON.parseObject(map.get("VoyageJson").toString());
                return AuthUtil.parseJsonKeyToLower("success", jsonObject);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
