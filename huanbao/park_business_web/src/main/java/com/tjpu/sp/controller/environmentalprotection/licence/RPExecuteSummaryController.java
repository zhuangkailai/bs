package com.tjpu.sp.controller.environmentalprotection.licence;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.RPExecuteSummaryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("RPExecuteSummary")
public class RPExecuteSummaryController {

    private final RPExecuteSummaryService rpExecuteSummaryService;

    public RPExecuteSummaryController(RPExecuteSummaryService rpExecuteSummaryService) {
        this.rpExecuteSummaryService = rpExecuteSummaryService;
    }


    /**
     * @Description: 排污许可证执行情况汇总表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getExecuteSumListByParam", method = RequestMethod.POST)
    public Object getExecuteSumListByParam(
            @RequestJson(value = "reportid") String reportid,
            @RequestJson(value = "pollutionid") String pollutionid
    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reportid", reportid);
            paramMap.put("itemtype", 1);
            paramMap.put("contenttype", 1);
            paramMap.put("contentsubtype", 1);
            //1排污单位基本信息
            Map<String, Object> map1 = new HashMap<>();
            map1.put("ItemType", "排污单位基本信息");
            map1.put("ContentType", "排污单位基本信息");
            List<Map<String, Object>> paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
            map1.put("DataList", paramList);
            resultList.add(map1);
            //2-1主要原料
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("materialtype", 1);
            List<Map<String, Object>> ylList = rpExecuteSummaryService.getYFLDataListByParam(paramMap);
            if (ylList.size() > 0) {
                paramMap.put("contenttype", 2);
                paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
                List<Map<String, Object>> ylDataList = getItemDataList(paramList, ylList);
                Map<String, Object> map = new HashMap<>();
                map.put("ItemType", "排污单位基本信息");
                map.put("ContentType", "主要原辅材料及燃料");
                map.put("contentsubtype", "原料");
                map.put("DataList", ylDataList);
                resultList.add(map);
            }


            //2-2主要辅料
            paramMap.put("materialtype", 2);
            List<Map<String, Object>> flList = rpExecuteSummaryService.getYFLDataListByParam(paramMap);
            if (flList.size()>0){
                paramMap.put("contenttype", 2);
                paramMap.put("contentsubtype", 2);
                paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
                List<Map<String, Object>> ylDataList = getItemDataList(paramList, flList);
                Map<String, Object> map = new HashMap<>();
                map.put("ItemType", "排污单位基本信息");
                map.put("ContentType", "主要原辅材料及燃料");
                map.put("contentsubtype", "辅料");
                map.put("DataList", ylDataList);
                resultList.add(map);
            }

            //2-3燃料
            List<Map<String, Object>> rlList = rpExecuteSummaryService.getRLDataListByParam(paramMap);
            if (rlList.size()>0){
                paramMap.put("contenttype", 2);
                paramMap.put("contentsubtype", 3);
                paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
                List<Map<String, Object>> dataList = getItemDataList(paramList, rlList);
                Map<String, Object> map = new HashMap<>();
                map.put("ItemType", "排污单位基本信息");
                map.put("ContentType", "主要原辅材料及燃料");
                map.put("contentsubtype", "燃料");
                map.put("DataList", dataList);
                resultList.add(map);
            }

            //3产排污节点、污染物及污染防治设施
            //3-1废水
            List<Map<String, Object>> waterList = rpExecuteSummaryService.getWaterFacilityDataListByParam(paramMap);
            if (waterList.size()>0){
                paramMap.put("contenttype", 3);
                paramMap.put("contentsubtype", 1);
                paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
                List<Map<String, Object>> dataList = getItemDataList(paramList, waterList);
                Map<String, Object> map = new HashMap<>();
                map.put("ItemType", "排污单位基本信息");
                map.put("ContentType", "产排污节点、污染物及污染防治设施");
                map.put("contentsubtype", "废水");
                map.put("DataList", dataList);
                resultList.add(map);
            }
            //3-2废气
            List<Map<String, Object>> gasList = rpExecuteSummaryService.getGasFacilityDataListByParam(paramMap);
            if (gasList.size()>0){
                paramMap.put("contenttype", 3);
                paramMap.put("contentsubtype", 2);
                paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
                List<Map<String, Object>> dataList = getItemDataList(paramList, gasList);
                Map<String, Object> map = new HashMap<>();
                map.put("ItemType", "排污单位基本信息");
                map.put("ContentType", "产排污节点、污染物及污染防治设施");
                map.put("contentsubtype", "废气");
                map.put("DataList", dataList);
                resultList.add(map);
            }
            //4自行监测要求
            List<Map<String, Object>> zxList = rpExecuteSummaryService.getZXDataListByParam(paramMap);
            if (zxList.size()>0){
                paramMap.put("itemtype", 2);
                paramMap.put("contenttype", 1);
                paramMap.put("contentsubtype", 1);
                paramList = rpExecuteSummaryService.getParamDataListByParam(paramMap);
                List<Map<String, Object>> dataList = getItemDataList(paramList, zxList);
                Map<String, Object> map = new HashMap<>();
                map.put("ItemType", "环境管理要求");
                map.put("ContentType", "自行监测要求");
                map.put("contentsubtype", "自行监测要求");
                map.put("DataList", dataList);
                resultList.add(map);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getItemDataList(List<Map<String, Object>> paramList, List<Map<String, Object>> itemList) {

        String itemid;
        for (Map<String, Object> item : itemList) {
            itemid = item.get("itemid").toString();

            List<Map<String, Object>> paramlist = new ArrayList<>();

            for (Map<String, Object> param : paramList) {
                if (itemid.equals(param.get("fk_otherid"))) {
                    paramlist.add(param);
                }else {
                    /*if (!paramlist.contains(param)){
                        paramlist.add(param);
                    }*/
                }
            }
            item.put("paramlist", paramlist);
        }
        return itemList;
    }



    /**
     * @Description: 执行（守法）报告要求
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getReportRequireByParam", method = RequestMethod.POST)
    public Object getReportRequireByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = rpExecuteSummaryService.getReportRequireByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
