package com.tjpu.sp.controller.environmentalprotection.devopsinfo;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DevOpsAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @description: 运维概览分析控制层
 * @create: 2022-03-09 09:16
 * @version: V1.0
 */

@RestController
@RequestMapping("devOpsAnalysis")
public class DevOpsAnalysisController {
    @Autowired
    private DevOpsAnalysisService devOpsAnalysisService;

    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 09:33
     * @Description: 运维设备统计
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceDevOpsStatusData", method = RequestMethod.POST)
    public Object countDeviceDevOpsStatusData() {
        try {
            //获取点位各状态数量
            Map<String,Object> statusmap = new HashMap<>();
            Map<String,Object> param = new HashMap<>();
            statusmap.putAll(devOpsAnalysisService.countAllPonitStatusDataByParam(param));
           //获取正在运维中的设备数量
            statusmap.putAll(devOpsAnalysisService.countAllDeviceDevOpsNumDataByParam(param));
           //获取缺数设备个数
            String daydate = DataFormatUtil.getDateYMD(new Date());
            param.put("countdate",daydate);
            statusmap.putAll(devOpsAnalysisService.countAllDataMissingDeviceNumByParam(param));
            return AuthUtil.parseJsonKeyToLower("success", statusmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 运维工单统计(某月)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceDevOpsWorkOrderData", method = RequestMethod.POST)
    public Object countDeviceDevOpsWorkOrderData(@RequestJson(value = "monthdate") String monthdate,
                                                 @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonth",monthdate);
            param.put("monitorpointtypes",monitorpointtypes);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceDevOpsWorkOrderData(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 设备运维分类统计(某月)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceDevOpsDataGroupByMonitorType", method = RequestMethod.POST)
    public Object countDeviceDevOpsDataGroupByMonitorType(@RequestJson(value = "monthdate") String monthdate) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonthdate",monthdate);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceDevOpsDataGroupByMonitorType(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 运维单位工单统计(企业分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceDevOpsDataGroupByPollution", method = RequestMethod.POST)
    public Object countDeviceDevOpsDataGroupByPollution(@RequestJson(value = "monthdate",required = false) String monthdate) {
        try {
            List<Map<String,Object>> resultlist = new ArrayList<>();
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonth",monthdate);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceDevOpsDataGroupByPollution(param);
            if (result!=null&&result.size()>0){
                Double total ;
                Double completednum ;
                String value;
                for (Map<String,Object> map:result){
                    total = (map.get("total")!=null&&!"".equals(map.get("total").toString()))?Double.valueOf(map.get("total").toString()):0d;
                    completednum = (map.get("completednum")!=null&&!"".equals(map.get("completednum").toString()))?Double.valueOf(map.get("completednum").toString()):0d;
                    if (total>0){
                        value = DataFormatUtil.SaveTwoAndSubZero(completednum*100/total);
                        map.put("rate",value);
                        resultlist.add(map);
                    }
                }
            }
            //排序
            if (resultlist.size()>0){
                resultlist = resultlist.stream().sorted(Comparator.comparingDouble((Map m) -> Double.valueOf(m.get("rate").toString())).reversed().thenComparing(Comparator.comparingInt((Map m) -> Integer.valueOf(m.get("total").toString()))).reversed()).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 历史运维单位工单统计分析(月份分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceDevOpsDataGroupByMonth", method = RequestMethod.POST)
    public Object countDeviceDevOpsDataGroupByMonth(@RequestJson(value = "yeardate") String yeardate) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("startyear",yeardate);
            param.put("endyear",yeardate);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceDevOpsDataGroupByMonth(param);
            if (result!=null&&result.size()>0){
                Double total ;
                Double completednum ;
                String value;
                for (Map<String,Object> map:result){
                    total = (map.get("total")!=null&&!"".equals(map.get("total").toString()))?Double.valueOf(map.get("total").toString()):0d;
                    completednum = (map.get("completednum")!=null&&!"".equals(map.get("completednum").toString()))?Double.valueOf(map.get("completednum").toString()):0d;
                    if (total>0){
                        value = DataFormatUtil.SaveTwoAndSubZero(completednum*100/total);
                        map.put("rate",value);
                    }else{
                        map.put("rate",0);
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
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 统计设备传输率(按类型分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceTransmissionRateDataGroupByType", method = RequestMethod.POST)
    public Object countDeviceTransmissionRateDataGroupByType(@RequestJson(value = "monthdate") String monthdate) {
        try {
            List<Map<String,Object>> resultlist = new ArrayList<>();
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonth",monthdate);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceTransmissionRateDataGroupByType(param);
            if (result!=null&&result.size()>0) {
                //按监测类型名称分组
                Map<String, List<Map<String, Object>>> collect = result.stream().filter(m -> m.get("fkmonitorpointtypename") != null).collect(Collectors.groupingBy(m -> m.get("fkmonitorpointtypename").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                    Map<String,Object> resultmap = new HashMap<>();
                    resultmap.put("monitorpointname",entry.getKey());
                    List<Map<String, Object>> valuelist = entry.getValue();
                    Double ShouldNumber =0d;
                    Double TransmissionNumber =0d;
                    for (Map<String, Object> map:valuelist){
                        if (map.get("ShouldNumber")!=null&&!"".equals(map.get("ShouldNumber").toString())){
                            ShouldNumber += Double.valueOf(map.get("ShouldNumber").toString());
                            if (map.get("TransmissionNumber")!=null&&!"".equals(map.get("TransmissionNumber").toString())){
                                TransmissionNumber += Double.valueOf(map.get("TransmissionNumber").toString());
                            }else{
                                TransmissionNumber += 0;
                            }
                        }
                    }
                    if (ShouldNumber>0){
                        resultmap.put("proportion",DataFormatUtil.SaveTwoAndSubZero(100 * TransmissionNumber / ShouldNumber));
                    }else{
                        resultmap.put("proportion","0");
                    }
                    resultlist.add(resultmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 异常排名统计(按点位分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDeviceExceptionRateDataByParamMap", method = RequestMethod.POST)
    public Object countDeviceExceptionRateDataByParamMap(@RequestJson(value = "monthdate") String monthdate) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonth",monthdate);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceExceptionRateDataByParamMap(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 点位巡警统计(按点位分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDevicePatrolNumDataByParamMap", method = RequestMethod.POST)
    public Object countDevicePatrolNumDataByParamMap(@RequestJson(value = "monthdate") String monthdate) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonth",monthdate);
            //异常次数
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceExceptionRateDataByParamMap(param);
            List<Map<String,Object>> resulttwo = devOpsAnalysisService.countDeviceDevOpsDataGroupByPoint(param);
            Map<String,Object> idandrate = new HashMap<>();
            //计算完成率
            if (resulttwo!=null&&resulttwo.size()>0){
                Double total ;
                Double completednum ;
                String value;
                for (Map<String,Object> map:resulttwo){
                    total = (map.get("total")!=null&&!"".equals(map.get("total").toString()))?Double.valueOf(map.get("total").toString()):0d;
                    completednum = (map.get("completednum")!=null&&!"".equals(map.get("completednum").toString()))?Double.valueOf(map.get("completednum").toString()):0d;
                    if (total>0){
                        value = DataFormatUtil.SaveTwoAndSubZero(completednum*100/total);
                        idandrate.put(map.get("monitorpointid").toString(),value);
                    }
                }
            }
            if (result!=null&&result.size()>0){
                String id;
                for (Map<String,Object> map:result){
                    id = map.get("monitorpointid").toString();
                    if (idandrate.get(id)!=null){
                        map.put("rate",idandrate.get(id));
                    }else{
                        map.put("rate","0");
                    }
                }
            }
            //排序
            if (result.size()>0){
                result = result.stream().sorted(Comparator.comparingDouble((Map m) -> Double.valueOf(m.get("rate").toString())).reversed().thenComparing(Comparator.comparingInt((Map m) -> Integer.valueOf(m.get("total").toString()))).reversed()).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 运维单位评价排名(按企业)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEntEvaluateRankDataByParamMap", method = RequestMethod.POST)
    public Object getEntEvaluateRankDataByParamMap(@RequestJson(value = "monthdate") String monthdate) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("startmonth",monthdate);
            param.put("endmonth",monthdate);
            List<Map<String,Object>> result = devOpsAnalysisService.countDeviceDevOpsDataGroupByPollution(param);
            List<Map<String,Object>> resultlist =  new ArrayList<>();
            if (result!=null&&result.size()>0){
                Double total ;
                Double completednum ;
                String value;
                for (Map<String,Object> map:result){
                    total = (map.get("total")!=null&&!"".equals(map.get("total").toString()))?Double.valueOf(map.get("total").toString()):0d;
                    completednum = (map.get("completednum")!=null&&!"".equals(map.get("completednum").toString()))?Double.valueOf(map.get("completednum").toString()):0d;
                    if (total>0){
                        value = DataFormatUtil.SaveTwoAndSubZero(completednum*100/total);
                        map.remove("total");
                        map.remove("completednum");
                        map.put("rate",value);
                        resultlist.add(map);
                    }
                }
            }
            //排序
            if (resultlist.size()>0){
                resultlist = resultlist.stream().sorted(Comparator.comparingDouble((Map m) -> Double.valueOf(m.get("rate").toString())).reversed()).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 获取运维点位分布
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getAllDeviceDevOpsPointDataByParamMap", method = RequestMethod.POST)
    public Object getAllDeviceDevOpsPointDataByParamMap() {
        try {
            List<Map<String,Object>> result = devOpsAnalysisService.getAllDeviceDevOpsPointDataByParamMap(new HashMap<>());
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
