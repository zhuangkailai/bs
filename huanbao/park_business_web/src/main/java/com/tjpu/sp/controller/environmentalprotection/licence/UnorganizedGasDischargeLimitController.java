package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.UnorganizedGasDischargeLimitService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("unorganizedGasDischargeLimit")
public class UnorganizedGasDischargeLimitController {

    private final UnorganizedGasDischargeLimitService unorganizedGasDischargeLimitService;

    public UnorganizedGasDischargeLimitController(UnorganizedGasDischargeLimitService unorganizedGasDischargeLimitService) {
        this.unorganizedGasDischargeLimitService = unorganizedGasDischargeLimitService;
    }


    /**
     * @Description: 大气无组织排放许可条件
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = unorganizedGasDischargeLimitService.getDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 挥发性有机物无组织排放量分类统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getVolatilityDataListByParam", method = RequestMethod.POST)
    public Object getVolatilityDataListByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = unorganizedGasDischargeLimitService.getVolatilityDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 排污单位大气排放总许可量
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
            List<Map<String, Object>> resultList = unorganizedGasDischargeLimitService.getTotalDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
