package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.RPStandingBookRecordDataService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("RPStandingBookRecordData")
public class RPStandingBookRecordDataController {

    private final RPStandingBookRecordDataService rpStandingBookRecordDataService;

    public RPStandingBookRecordDataController(RPStandingBookRecordDataService rpStandingBookRecordDataService) {
        this.rpStandingBookRecordDataService = rpStandingBookRecordDataService;
    }


    /**
     * @Description: 获取台账管理情况表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam( @RequestJson(value = "reportid") String reportid ) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpStandingBookRecordDataService.getDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 信息公开情况表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getInfoPublicDataListByParam", method = RequestMethod.POST)
    public Object getInfoPublicDataListByParam( @RequestJson(value = "reportid") String reportid ) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpStandingBookRecordDataService.getInfoPublicDataListByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 信息公开情况表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getYearTextContentByParam", method = RequestMethod.POST)
    public Object getYearTextContentByParam( @RequestJson(value = "texttypes") List<Integer> texttypes,
        @RequestJson(value = "reportid") String reportid
    ) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("texttypes",texttypes);
            paramMap.put("reportid",reportid);
            List<Map<String, Object>> resultList = rpStandingBookRecordDataService.getYearTextContentByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
