package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.WaterDischargeLimitService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("waterDischargeLimit")
public class WaterDischargeLimitController {

    private final WaterDischargeLimitService waterDischargeLimitService;

    public WaterDischargeLimitController(WaterDischargeLimitService waterDischargeLimitService) {
        this.waterDischargeLimitService = waterDischargeLimitService;
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
            List<Map<String, Object>> resultList = waterDischargeLimitService.getDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 全厂排放口总计
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getTotalDataListByParam", method = RequestMethod.POST)
    public Object getTotalDataListByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            paramMap.put("outlettype", "1");
            List<Map<String, Object>> resultList = waterDischargeLimitService.getTotalDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 特殊情况下废水污染物排放许可限值
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getSpecialDataListByParam", method = RequestMethod.POST)
    public Object getSpecialDataListByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = waterDischargeLimitService.getSpecialDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




}
