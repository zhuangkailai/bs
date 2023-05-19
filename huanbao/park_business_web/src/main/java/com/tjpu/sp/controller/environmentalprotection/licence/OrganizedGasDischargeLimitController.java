package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.OrganizedGasDischargeLimitService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("organizedGasDischargeLimit")
public class OrganizedGasDischargeLimitController {

    private final OrganizedGasDischargeLimitService organizedGasDischargeLimitService;

    public OrganizedGasDischargeLimitController(OrganizedGasDischargeLimitService organizedGasDischargeLimitService) {
        this.organizedGasDischargeLimitService = organizedGasDischargeLimitService;
    }


    /**
     * @Description: 大气污染物有组织排放许可限值（主要排口、一般排口）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam(@RequestJson(value = "licenceid") String licenceid,
                                     @RequestJson(value = "outlettype") String outlettype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            paramMap.put("outlettype", outlettype);
            List<Map<String, Object>> resultList = organizedGasDischargeLimitService.getDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 全厂有组织排放总计
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getTotalDataListByParam", method = RequestMethod.POST)
    public Object getTotalDataListByParam(@RequestJson(value = "licenceid") String licenceid ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = organizedGasDischargeLimitService.getTotalDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}
