package com.tjpu.sp.controller.environmentalprotection.licence;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.LicenceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description: 排污口处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/4/2 9:28
 */
@RestController
@RequestMapping("outPutData")
public class OutPutDataController {

    private final LicenceService licenceService;

    @Autowired
    public OutPutDataController(LicenceService licenceService) {
        this.licenceService = licenceService;
    }


    /**
     * @Description: 获取排污口及因子排放量数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/2 9:30
     */
    @RequestMapping(value = "getPWOutPutDataListByParam", method = RequestMethod.POST)
    public Object getPWOutPutDataListByParam(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "reportid", required = false) String reportid
    ) {

        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportid", reportid);
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("monitorpointtypecode", monitorpointtypecode);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
            case WasteGasEnum:
                //有组织主要排放口
                paramMap.put("outputtype", 1);
                List<Map<String, Object>> mainOutList = licenceService.gasOutListByParam(paramMap);
                //有组织一般排放口
                paramMap.put("outputtype", 2);
                List<Map<String, Object>> YBOutList = licenceService.gasOutListByParam(paramMap);
                //无组织排放口
                List<Map<String, Object>> UnOutList = licenceService.gasUnOutListByParam(paramMap);
                resultMap.put("mainOutList", mainOutList);
                resultMap.put("YBOutList", YBOutList);
                resultMap.put("UnOutList", UnOutList);
                break;
            case WasteWaterEnum:
                List<Map<String, Object>> OutList = licenceService.getWaterOutPutDataListByParam(paramMap);
                resultMap.put("WaterOutList", OutList);
                break;
        }
        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
    }


    /**
     * @Description: 获取排污口及因子排放量季度数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/2 9:30
     */
    @RequestMapping(value = "getPWOutPutQDataListByParam", method = RequestMethod.POST)
    public Object getPWOutPutQDataListByParam(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "reportid", required = false) String reportid
    ) {

        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportid", reportid);
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("monitorpointtypecode", monitorpointtypecode);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
            case WasteGasEnum:
                //有组织主要排放口
                paramMap.put("outputtype", 1);
                List<Map<String, Object>> mainOutList = licenceService.gasQOutListByParam(paramMap);
                //有组织一般排放口
                paramMap.put("outputtype", 2);
                List<Map<String, Object>> YBOutList = licenceService.gasQOutListByParam(paramMap);
                //无组织排放口
                List<Map<String, Object>> UnOutList = licenceService.gasUnQOutListByParam(paramMap);
                resultMap.put("mainOutList", mainOutList);
                resultMap.put("YBOutList", YBOutList);
                resultMap.put("UnOutList", UnOutList);
                break;
            case WasteWaterEnum:
                List<Map<String, Object>> OutList = licenceService.getWaterOutPutQDataListByParam(paramMap);
                resultMap.put("WaterOutList", OutList);
                break;
        }
        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
    }



    /**
     * @Description: 获取排污口及因子排放量年度数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/2 9:30
     */
    @RequestMapping(value = "getPWOutPutYDataListByParam", method = RequestMethod.POST)
    public Object getPWOutPutYDataListByParam(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "reportid", required = false) String reportid
    ) {

        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportid", reportid);
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("monitorpointtypecode", monitorpointtypecode);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
            case WasteGasEnum:
                //有组织主要排放口
                paramMap.put("outputtype", 1);
                List<Map<String, Object>> mainOutList = licenceService.gasYOutListByParam(paramMap);
                //有组织一般排放口
                paramMap.put("outputtype", 2);
                List<Map<String, Object>> YBOutList = licenceService.gasYOutListByParam(paramMap);
                //无组织排放口
                List<Map<String, Object>> UnOutList = licenceService.gasUnYOutListByParam(paramMap);
                resultMap.put("mainOutList", mainOutList);
                resultMap.put("YBOutList", YBOutList);
                resultMap.put("UnOutList", UnOutList);
                break;
            case WasteWaterEnum:
                List<Map<String, Object>> OutList = licenceService.getWaterOutPutYDataListByParam(paramMap);
                resultMap.put("WaterOutList", OutList);
                break;
        }
        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
    }

    /**
     * @Description: 获取排污口及因子超标数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/2 9:30
     */
    @RequestMapping(value = "getPWOutPutOverDataListByParam", method = RequestMethod.POST)
    public Object getPWOutPutOverDataListByParam(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "reportid", required = false) String reportid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportid", reportid);
        paramMap.put("monitorpointtypecode", monitorpointtypecode);
        List<Map<String, Object>> overList = licenceService.PWOutOverListByParam(paramMap);

        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, overList);
    }

    /**
     * @Description: 获取治理设施异常数据信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/2 9:30
     */
    @RequestMapping(value = "getFacilityExceptionDataListByParam", method = RequestMethod.POST)
    public Object getFacilityExceptionDataListByParam(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "reportid", required = false) String reportid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportid", reportid);
        paramMap.put("monitorpointtypecode", monitorpointtypecode);
        List<Map<String, Object>> exceptList = licenceService.getFacilityExceptionDataListByParam(paramMap);
        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, exceptList);
    }

    /**
     * @Description: 获取治理设施数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/2 9:30
     */
    @RequestMapping(value = "getFacilityDataListByParam", method = RequestMethod.POST)
    public Object getFacilityDataListByParam(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "pollutionid", required = false) String pollutionid) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutionid", pollutionid);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
            case WasteGasEnum:
                resultList = licenceService.getGasFacilityDataListByParam(paramMap);
                break;
            case WasteWaterEnum:
                resultList = licenceService.getWaterFacilityDataListByParam(paramMap);
                break;
        }
        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
    }


}
