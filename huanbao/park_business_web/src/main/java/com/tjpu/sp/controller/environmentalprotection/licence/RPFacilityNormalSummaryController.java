package com.tjpu.sp.controller.environmentalprotection.licence;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.RPFacilityNormalSummaryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("RPFacilityNormalSummary")
public class RPFacilityNormalSummaryController {

    private final RPFacilityNormalSummaryService rpFacilityNormalSummaryService;

    public RPFacilityNormalSummaryController(RPFacilityNormalSummaryService rpFacilityNormalSummaryService) {
        this.rpFacilityNormalSummaryService = rpFacilityNormalSummaryService;
    }


    /**
     * @Description: 废水废气污染治理设施正常运转情况表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getNormalDataListByParam", method = RequestMethod.POST)
    public Object getNormalDataListByParam(@RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
                                           @RequestJson(value = "reportid") String reportid ) {
        try {

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypecode",monitorpointtypecode);
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpFacilityNormalSummaryService.getNormalDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 废水废气污染治理设施正常运转情况表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getSpecialTimeGasPollutantByParam", method = RequestMethod.POST)
    public Object getSpecialTimeGasPollutantByParam(@RequestJson(value = "timetype") Integer timetype,
                                           @RequestJson(value = "reportid") String reportid ) {
        try {

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("timetype",timetype);
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpFacilityNormalSummaryService.getSpecialTimeGasPollutantByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
