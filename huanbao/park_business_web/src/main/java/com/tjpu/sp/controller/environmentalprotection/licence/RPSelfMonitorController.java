package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.RPSelfMonitorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("RPSelfMonitor")
public class RPSelfMonitorController {

    private final RPSelfMonitorService rpSelfMonitorService;

    public RPSelfMonitorController(RPSelfMonitorService rpSelfMonitorService) {
        this.rpSelfMonitorService = rpSelfMonitorService;
    }


    /**
     * @Description: 有组织废气污染物排放浓度监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getGasConcentrationListByParam", method = RequestMethod.POST)
    public Object getGasConcentrationListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getGasConcentrationListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 有组织废气污染物排放速率监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getGasSpeedListByParam", method = RequestMethod.POST)
    public Object getGasSpeedListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getGasSpeedListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 废水污染物排放浓度监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getWaterConcentrationListByParam", method = RequestMethod.POST)
    public Object getWaterConcentrationListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getWaterConcentrationListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 3 无组织废气污染物排放浓度监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getUnGasConcentrationListByParam", method = RequestMethod.POST)
    public Object getUnGasConcentrationListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getUnGasConcentrationListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 非正常工况有组织废气污染物监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getExceptionGasConcentrationListByParam", method = RequestMethod.POST)
    public Object getExceptionGasConcentrationListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getExceptionGasConcentrationListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @Description: 非正常工况无组织废气污染物浓度监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getExceptionUnGasConcentrationListByParam", method = RequestMethod.POST)
    public Object getExceptionUnGasConcentrationListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getExceptionUnGasConcentrationListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 特殊时段有组织废气污染物监测数据统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getSpecialGasConcentrationListByParam", method = RequestMethod.POST)
    public Object getSpecialGasConcentrationListByParam(
            @RequestJson(value = "reportid") String reportid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpSelfMonitorService.getSpecialGasConcentrationListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
