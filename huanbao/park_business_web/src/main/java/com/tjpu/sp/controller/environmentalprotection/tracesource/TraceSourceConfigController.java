package com.tjpu.sp.controller.environmentalprotection.tracesource;

import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceConfigInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.tracesource.TraceSourceConfigInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: lip
 * @date: 2019/8/13 0013 下午 3:58
 * @Description: 溯源配置表处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("traceSourceConfig")
public class TraceSourceConfigController {
    @Autowired
    private TraceSourceConfigInfoService traceSourceConfigInfoService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;


    /**
     * @author: lip
     * @date: 2019/8/13 0013 下午 4:21
     * @Description: 获取溯源点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTraceSourcePointData", method = RequestMethod.POST)
    public Object getTraceSourcePointData() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("attributecode", CommonTypeEnum.TraceSourceConfigEnum.MonitorPointEnum.getCode());

            List<Map<String, Object>> dataList = traceSourceConfigInfoService.getTraceSourceConfigDataByParamMap(paramMap);
            if (dataList.size() > 0) {
                for (Map<String, Object> mapTemp : dataList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dgimn", mapTemp.get("DGIMN"));
                    map.put("monitorpointid", mapTemp.get("PK_MonitorPointID"));
                    map.put("monitorpointname", mapTemp.get("MonitorPointName"));
                    map.put("monitorpointtype", mapTemp.get("FK_MonitorPointTypeCode"));
                    resultList.add(map);
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/13 0013 下午 4:21
     * @Description: 获取溯源污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTraceSourcePollutantData", method = RequestMethod.POST)
    public Object getTraceSourcePollutantData() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("attributecode", CommonTypeEnum.TraceSourceConfigEnum.PollutantEnum.getCode());
            List<Map<String, Object>> dataList = traceSourceConfigInfoService.getTraceSourceConfigDataByParamMap(paramMap);
            if (dataList.size() > 0) {
                Set<Object> codes = new HashSet<>();
                for (Map<String, Object> mapTemp : dataList) {
                    if (!codes.contains(mapTemp.get("Code"))) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pollutantcode", mapTemp.get("Code"));
                        map.put("pollutantname", mapTemp.get("Name"));
                        map.put("pollutantunit", mapTemp.get("PollutantUnit"));
                        resultList.add(map);
                        codes.add(mapTemp.get("Code"));
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/28 0028 下午 5:01
     * @Description: 修改溯源配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateTraceSourceConfigInfo", method = RequestMethod.POST)
    public Object updateTraceSourceConfigInfo(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            List<Map<String, Object>> formdata = (List<Map<String, Object>>) paramMap.get("formdata");
            List<TraceSourceConfigInfoVO> adddata = new ArrayList<>();
            List<String> delete = new ArrayList<>();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            //设置参数
            if (formdata != null && formdata.size() > 0) {
                for (Map<String, Object> map : formdata) {//遍历属性
                    Object value = map.get("attributevalue");
                    List<Object> valuelist = new ArrayList<>();
                    // Object m =value.getClass();
                    if (value.getClass().equals(net.sf.json.JSONArray.class)) {
                        //数组
                        if (value != null) {
                            valuelist = (List<Object>) value;
                        }
                    } else if (value.getClass().equals(String.class)) {
                        //字符串
                        if (value != null && !"".equals(value)) {
                            valuelist.add(value);
                        }
                    }
                    if (valuelist!=null&&valuelist.size()>0) {
                        for (Object object : valuelist) {
                            TraceSourceConfigInfoVO obj = new TraceSourceConfigInfoVO();
                            obj.setPkId(UUID.randomUUID().toString());
                            obj.setAttributecode(map.get("attributecode").toString());
                            obj.setAttributename(map.get("attributename").toString());
                            obj.setAttributevalue(object != null ? object.toString() : "");
                            obj.setUpdatetime(new Date());
                            obj.setUpdateuser(username);
                            adddata.add(obj);
                        }
                    }else{
                        TraceSourceConfigInfoVO obj = new TraceSourceConfigInfoVO();
                        obj.setPkId(UUID.randomUUID().toString());
                        obj.setAttributecode(map.get("attributecode").toString());
                        obj.setAttributename(map.get("attributename").toString());
                        obj.setAttributevalue("");
                        obj.setUpdatetime(new Date());
                        obj.setUpdateuser(username);
                        adddata.add(obj);
                    }
                    delete.add(map.get("attributecode").toString());
                }
            }
            traceSourceConfigInfoService.updateTraceSourceConfigInfo(adddata, delete);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/28 0028 下午 5:01
     * @Description: 获取溯源配置详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTraceSourceConfigDetail", method = RequestMethod.POST)
    public Object getTraceSourceConfigDetail() throws Exception {
        try {
            List<Map<String, Object>> datalist = traceSourceConfigInfoService.getTraceSourceConfigInfoGroupByCode(new HashMap<>());
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/30 0030 上午 9:14
     * @Description: 获取溯源点位下拉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTraceSourcePointSelectData", method = RequestMethod.POST)
    public Object getTraceSourcePointSelectData() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> dataList = otherMonitorPointService.getAllMonitorInfoByParams(new HashMap<>());
            if (dataList.size() > 0) {
                for (Map<String, Object> mapTemp : dataList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("monitorpointid", mapTemp.get("pk_id"));
                    map.put("monitorpointname", mapTemp.get("monitorpointname"));
                    map.put("monitorpointtype", mapTemp.get("fk_monitorpointtypecode"));
                    resultList.add(map);
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/13 0013 下午 4:21
     * @Description: 获取溯源污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTraceSourcePollutantSelectData", method = RequestMethod.POST)
    public Object getTraceSourcePollutantSelectData() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> dataList = traceSourceConfigInfoService.getTraceSourcePollutantSelectData();
            if (dataList.size() > 0) {
                Set<Object> codes = new HashSet<>();
                for (Map<String, Object> mapTemp : dataList) {
                    if (!codes.contains(mapTemp.get("code"))) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pollutantcode", mapTemp.get("code"));
                        map.put("pollutantname", mapTemp.get("name"));
                        map.put("pollutantunit", mapTemp.get("PollutantUnit"));
                        resultList.add(map);
                        codes.add(mapTemp.get("code"));
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
