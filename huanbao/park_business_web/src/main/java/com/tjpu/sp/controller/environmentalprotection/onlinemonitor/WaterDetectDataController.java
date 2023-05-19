package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.RiverSectionService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineDataCountService;
import com.tjpu.sp.service.environmentalprotection.upgradefunction.WallChartOperationService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description: 水检测数据类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/3/9 16:25
 */
@RestController
@RequestMapping("waterDetectData")
public class WaterDetectDataController {


    @Autowired
    private RiverSectionService riverSectionService;
    @Autowired
    private OnlineDataCountService onlineDataCountService;
    @Autowired
    private WallChartOperationService wallChartOperationService;
    @Autowired
    private PollutantService pollutantService;

    /**
     * @Description: 获取点位地表水评价数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/9 15:36
     */
    @RequestMapping(value = "getPointEnvDataList", method = RequestMethod.POST)
    public Object getPointEnvDataList(@RequestJson(value = "monitortime") String monitortime) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            List<Map<String, Object>> pointList = riverSectionService.getRiverSectionPointListByParam(paramMap);
            if (pointList.size() > 0) {
                //获取所有mn号
                List<String> dgimns = pointList.stream().filter(m -> m.get("pk_id") != null).map(m -> m.get("pk_id").toString()).collect(Collectors.toList());
                paramMap.put("dgimns", dgimns);
                paramMap.put("starttime", DataFormatUtil.getFirstDayOfMonth(monitortime));
                paramMap.put("endtime", DataFormatUtil.getLastDayOfMonth(monitortime));
                Map<String, Object> idAndLevel = new HashMap<>();
                List<Document> documents = onlineDataCountService.getWaterDetectDataByParam(paramMap);
                String id;
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        id = document.getString("DataGatherCode");
                        idAndLevel.put(id, document.get("WaterQualityClass"));
                    }
                }
                for (Map<String, Object> point : pointList) {
                    id = point.get("pk_id").toString();
                    point.put("targetlevel", point.get("waterqualityclassname"));
                    if (idAndLevel.get(id) != null) {
                        point.put("envlevel", idAndLevel.get(id));
                        point.put("envtime", DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM", "yyyy年MM月"));
                    } else {
                        point.put("envlevel", "-");
                        point.put("envtime", "-");
                    }
                    resultList.add(point);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/09 09:43
     * @Description: 通过自定义参数河流断面点位分布及最新监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "countRiverSectionLastDataRankByParam", method = RequestMethod.POST)
    public Object countRiverSectionLastDataRankByParam(@RequestJson(value = "monitortime",required = false) String monitortime,
                                                              @RequestJson(value = "pollutantcode") String pollutantcode

    ) {
        try {
            List<String> dgimnList = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<Map<String,Object>> result = new ArrayList<>();
            Map<String,Object> parammap = new HashMap<>();
            List<Map<String, Object>> pointList = riverSectionService.getRiverSectionPointListByParam(parammap);
            pointList = pointList.stream().filter(m -> m.get("pk_id") != null && dgimnList.contains(m.get("pk_id").toString())).collect(Collectors.toList());
            //获取所有水质级别等级
            List<Map<String, Object>> qaulitylist = wallChartOperationService.getAllWaterQualityLevelData();
            Map<String, Object> levelandcode = new HashMap<>();
            for (Map<String, Object> map:qaulitylist){
                if (map.get("code")!=null&&map.get("levelnum")!=null) {
                    levelandcode.put(map.get("name").toString(), map.get("code"));
                }
            }
            if (pointList.size() > 0) {
                //获取所有mn号
                List<String> dgimns = pointList.stream().filter(m -> m.get("pk_id") != null).map(m -> m.get("pk_id").toString()).collect(Collectors.toList());
                parammap.put("dgimns", dgimns);
                if (monitortime!=null&&!"".equals(monitortime)) {
                    parammap.put("starttime", DataFormatUtil.getFirstDayOfMonth(monitortime));
                    parammap.put("endtime", DataFormatUtil.getLastDayOfMonth(monitortime));
                }
                parammap.put("pollutantcode",pollutantcode);
                List<Document> documents = onlineDataCountService.getWaterDetectOnePollutantDataByParam(parammap);
                String id;
                for (Map<String, Object> onepoint : pointList) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("monitorpointid",onepoint.get("pk_id"));
                    point.put("monitorpointname",onepoint.get("sectionname"));
                    point.put("longitude",onepoint.get("longitude"));
                    point.put("latitude",onepoint.get("latitude"));
                    point.put("targetlevelcode",onepoint.get("waterlevelcode"));
                    id = onepoint.get("pk_id").toString();
                    point.put("targetlevel", onepoint.get("waterqualityclassname"));
                    if (monitortime!=null) {
                        point.put("monitortime", DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM", "yyyy年MM月"));
                    }
                    point.put("pointlevelname","");
                    point.put("pointlevel","");
                    point.put("value","");
                    point.put("codelevelname","");
                    point.put("codelevel","");
                    point.put("isover","");
                    if (documents.size() > 0) {
                        for (Document document : documents) {
                            if (id.equals(document.getString("DataGatherCode"))){
                                if (point.get("monitortime")==null){
                                    point.put("monitortime", DataFormatUtil.FormatDateOneToOther(DataFormatUtil.getDateYM(document.getDate("MonitorTime")), "yyyy-MM", "yyyy年MM月"));
                                }
                                point.put("pointlevelname",document.get("pointquality"));
                                if (document.get("pointquality")!=null){
                                    point.put("pointlevel",levelandcode.get(document.get("pointquality").toString()));
                                }
                                point.put("value",document.get("value"));
                                point.put("codelevelname",document.get("codequality"));
                                if (document.get("codequality")!=null){
                                    point.put("codelevel",levelandcode.get(document.get("codequality").toString()));
                                }
                                point.put("isover",document.get("isover"));
                                break;
                            }
                        }
                    }
                    result.add(point);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/09 0009 下午 14:19
     * @Description: 获取单个断面点位最新一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOneRiverSectionLastDataByParam", method = RequestMethod.POST)
    public Object getOneRiverSectionLastDataByParam(@RequestJson(value = "monitorpointid") String monitorpointid ) {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            //获取该类型监测的污染物
            paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.DBWaterEnum.getCode());
            List<Map<String, Object>> pollutantlist = pollutantService.getPollutantsByCodesAndType(paramMap);
            //获取所有水质级别等级
            List<Map<String, Object>> qaulitylist = wallChartOperationService.getAllWaterQualityLevelData();
            Map<String, Object> levelandcode = new HashMap<>();
            for (Map<String, Object> map:qaulitylist){
                if (map.get("code")!=null&&map.get("levelnum")!=null) {
                    levelandcode.put(map.get("name").toString(), map.get("code"));
                }
            }
            Map<String,Object> codeandname = new HashMap<>();
            Map<String,Object> codeandorderindex = new HashMap<>();
            if (pollutantlist.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                for (Map<String, Object> map : pollutantlist) {
                    pollutantcodes.add(map.get("code").toString());
                    codeandname.put(map.get("code").toString(), map.get("name"));
                    codeandorderindex.put(map.get("code").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
                }
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("dgimn", monitorpointid);
                Document document = onlineDataCountService.getOneRiverSectionLastDataByParam(paramMap);
                List<Map<String, Object>> valuelist = new ArrayList<>();
                if (document!=null) {
                    resultMap.put("monitorpointid", monitorpointid);
                    resultMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                    resultMap.put("pointlevelname",document.get("WaterQualityClass"));
                    resultMap.put("pointlevel","");
                    if (document.get("pointquality")!=null){
                        resultMap.put("pointlevel",levelandcode.get(document.get("WaterQualityClass").toString()));
                    }
                    List<Document> polist = (List<Document>) document.get("DataList");
                    String code;
                    String value;
                    for (Document doc : polist) {
                        Map<String, Object> codemap = new HashMap<>();
                        code = doc.getString("PollutantCode");
                        if (pollutantcodes.contains(code)) {
                            value = doc.getString("MonitorValue");
                            codemap.put("pollutantcode", code);
                            codemap.put("pollutantname", codeandname.get(code));
                            codemap.put("value", value);
                            codemap.put("codelevelname", doc.get("WaterQualityClass"));
                            if (document.get("WaterQualityClass")!=null){
                                codemap.put("codelevel",levelandcode.get(document.get("WaterQualityClass").toString()));
                            }
                            codemap.put("orderindex", codeandorderindex.get(code));
                            valuelist.add(codemap);
                        }
                    }
                }
                if (valuelist!=null&&valuelist.size()>0){
                    //排序
                    valuelist = valuelist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                }
                resultMap.put("valuedata", valuelist);
            }
            return AuthUtil.parseJsonKeyToLower("success",resultMap );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
