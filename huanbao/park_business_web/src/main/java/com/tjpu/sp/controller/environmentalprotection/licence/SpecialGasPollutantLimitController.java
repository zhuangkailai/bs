package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.SpecialGasPollutantLimitService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("specialGasPollutantLimit")
public class SpecialGasPollutantLimitController {

    private final SpecialGasPollutantLimitService specialGasPollutantLimitService;

    public SpecialGasPollutantLimitController(SpecialGasPollutantLimitService specialGasPollutantLimitService) {
        this.specialGasPollutantLimitService = specialGasPollutantLimitService;
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
                                     @RequestJson(value = "situationtype") String situationtype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            paramMap.put("situationtype", situationtype);
            List<Map<String, Object>> resultList = specialGasPollutantLimitService.getDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }





}
