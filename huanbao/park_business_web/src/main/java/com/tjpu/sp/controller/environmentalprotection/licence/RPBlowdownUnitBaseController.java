package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.RPBlowdownUnitBaseService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("RPBlowdownUnitBase")
public class RPBlowdownUnitBaseController {

    private final RPBlowdownUnitBaseService rpBlowdownUnitBaseService;

    public RPBlowdownUnitBaseController(RPBlowdownUnitBaseService rpBlowdownUnitBaseService) {
        this.rpBlowdownUnitBaseService = rpBlowdownUnitBaseService;
    }


    /**
     * @Description: 获取排污单位基本信息数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getUnitBaseDataListByParam", method = RequestMethod.POST)
    public Object getUnitBaseDataListByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                             @RequestJson(value = "reportid") String reportid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("reportid", reportid);
            CommonTypeEnum.BlowdownUnitBaseEnum[] values = CommonTypeEnum.BlowdownUnitBaseEnum.values();
            List<Map<String, Object>> dataList;
            for (CommonTypeEnum.BlowdownUnitBaseEnum value : values) {
                paramMap.put("recordtype", value.getCode());
                dataList = getUnitBaseDataList(value, paramMap);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("recordtype", value.getCode());
                dataMap.put("recordname", value.getName());
                dataMap.put("datalist", dataList);
                resultList.add(dataMap);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getUnitBaseDataList(CommonTypeEnum.BlowdownUnitBaseEnum value, Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        switch (value) {
            case yuanliaoEnum:
            case fuliaoEnum:
                dataList = rpBlowdownUnitBaseService.getBlowdownUnitYLDataListByParam(paramMap);
                break;
            case nengyuanEnum:
            case yunxingshijianEnum:
            case qupaishuiEnum:
                dataList = rpBlowdownUnitBaseService.getBlowdownUnitNYDataListByParam(paramMap);
                break;
            case shengchanEnum:
                break;
            case chanpinEnum:
                dataList = rpBlowdownUnitBaseService.getBlowdownUnitCPDataListByParam(paramMap);
                break;
            case touziEnum:
                dataList = rpBlowdownUnitBaseService.getBlowdownUnitTZDataListByParam(paramMap);
                break;
        }
        return dataList;
    }


}
