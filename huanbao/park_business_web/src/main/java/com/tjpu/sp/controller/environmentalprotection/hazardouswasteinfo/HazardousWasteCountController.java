package com.tjpu.sp.controller.environmentalprotection.hazardouswasteinfo;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2020/09/27 0011 下午 1:58
 * @Description: 危废信息统计控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("hazardouswastecount")
public class HazardousWasteCountController {

    @Autowired
    private HazardousWasteInfoService hazardousWasteInfoService;


    /**
     * @author: chengzq
     * @date: 2020/9/27 0027 上午 10:34
     * @Description: 通过自定义参数获取危废统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [counttype] 1按日期统计，2按控制级别统计，3按行业类型统计
     * @throws:
     */
    @RequestMapping(value = "/countHazardousWasteDataByParamMap", method = RequestMethod.POST)
    public Object countHazardousWasteDataByParamMap(@RequestJson(value = "counttype") Integer counttype,
                                                    @RequestJson(value = "starttime",required = false) String starttime,
                                                    @RequestJson(value = "endtime",required = false) String endtime,
                                                    @RequestJson(value = "wastematerialtype",required = false) String wastematerialtype,
                                                    @RequestJson(value = "industrytype",required = false) String industrytype,
                                                    @RequestJson(value = "controllevetype",required = false) String controllevetype){
        try {
            Map<String,Object> paramMap=new HashMap<>();
            if(counttype==1){
                paramMap.put("data","MonthDate");
                paramMap.put("alias","MonthDate");
                paramMap.put("starttime",starttime);
                paramMap.put("endtime",endtime);
            }else if(counttype==2){
                paramMap.put("data","PUB_CODE_ControlLeve.name");
                paramMap.put("type","PUB_CODE_ControlLeve.code");
                paramMap.put("alias","ControlLevename");
                paramMap.put("starttime",starttime);
                paramMap.put("endtime",endtime);
                paramMap.put("controllevetype",controllevetype);
            }else if(counttype==3){
                paramMap.put("data","PUB_CODE_IndustryType.name");
                paramMap.put("type","PUB_CODE_IndustryType.code");
                paramMap.put("alias","Industryname");
                paramMap.put("starttime",starttime);
                paramMap.put("endtime",endtime);
                paramMap.put("industrytype",industrytype);
            }else if(counttype==4){
                paramMap.put("data","PUB_CODE_WasteMaterial.code+PUB_CODE_WasteMaterial.name");
                paramMap.put("type","PUB_CODE_WasteMaterial.code");
                paramMap.put("alias","wastematerialtypename");
                paramMap.put("starttime",starttime);
                paramMap.put("endtime",endtime);
                paramMap.put("wastematerialtype",wastematerialtype);
            }else {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }

            List<Map<String, Object>> maps = hazardousWasteInfoService.countHazardousWasteDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/05/18 0018 上午 10:00
     * @Description: 通过自定义参数统计危废年生产、同比情况（生产、贮存、利用）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime:yyyy  endtime:yyyy ;datatype:1(生产)，2（贮存），3（利用）
     * @throws:
     */
    @RequestMapping(value = "/countHazardousWasteDataGroupYearByParamMap", method = RequestMethod.POST)
    public Object countHazardousWasteDataGroupYearByParamMap(@RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "datatype") Integer datatype
                                                             ){
        try {
            Map<String,Object> paramMap = new HashMap<>();
            List<Map<String,Object>> result = new ArrayList<>();
            if (datatype > 3){
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
            }
            int firstyear = Integer.valueOf(starttime) -1;
            int lastyear = Integer.valueOf(endtime);
            paramMap.put("starttime",firstyear);
            paramMap.put("endtime",endtime);
            List<Map<String, Object>> datalist = hazardousWasteInfoService.countHazardousWasteDataGroupYearByParamMap(paramMap);
            if (datalist!=null&&datalist.size()>0){
                //需要计算同比 年份往前推一年
                double previousvalue = 0d;
                double thisvalue = 0d;
                for (int i = firstyear;i<=lastyear;i++){
                    for (Map<String, Object> map :datalist){
                        if(map.get("yeardate")!=null &&!"".equals(map.get("yeardate").toString()) && i == Integer.valueOf(map.get("yeardate").toString())){
                            if (i == Integer.valueOf(starttime) -1){
                                if (1 == datatype){
                                    previousvalue = map.get("ProductionQuantitysum")!=null?Double.valueOf(map.get("ProductionQuantitysum").toString()):0d;
                                }else if(2 == datatype){
                                    previousvalue = map.get("EndingStockssum")!=null?Double.valueOf(map.get("EndingStockssum").toString()):0d;
                                }else if(3 == datatype){
                                    previousvalue = map.get("SelfuseQuantitysum")!=null?Double.valueOf(map.get("SelfuseQuantitysum").toString()):0d;
                                }
                            }else{
                                Map<String, Object> onemap = new HashMap<>();
                                onemap.put("year",i);
                                if (1 == datatype){
                                    thisvalue = map.get("ProductionQuantitysum")!=null?Double.valueOf(map.get("ProductionQuantitysum").toString()):0d;
                                }else if(2 == datatype){
                                    thisvalue = map.get("EndingStockssum")!=null?Double.valueOf(map.get("EndingStockssum").toString()):0d;
                                }else if(3 == datatype){
                                    thisvalue = map.get("SelfuseQuantitysum")!=null?Double.valueOf(map.get("SelfuseQuantitysum").toString()):0d;
                                }
                                onemap.put("previousvalue",previousvalue);
                                onemap.put("thisvalue",thisvalue);
                                if (thisvalue>0 && previousvalue>0){
                                    onemap.put("change", DataFormatUtil.SaveTwoAndSubZero((thisvalue - previousvalue) *100 / previousvalue));
                                }else{
                                    if (thisvalue>0){
                                        onemap.put("change","100");
                                    }
                                    if (previousvalue>0){
                                        onemap.put("change","-100");
                                    }
                                }
                                result.add(onemap);
                                previousvalue = thisvalue;
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/05/18 0018 上午 10:00
     * @Description: 通过自定义参数统计主要产废类别产废总量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countMainHazardousWasteTypeDataByParamMap", method = RequestMethod.POST)
    public Object countMainHazardousWasteTypeDataByParamMap(@RequestJson(value = "starttime",required = false) String starttime,
                                                            @RequestJson(value = "endtime",required = false) String endtime,
                                                            @RequestJson(value = "datatype") Integer datatype
    ){
        try {
            Map<String,Object> paramMap = new HashMap<>();
            List<Map<String,Object>> result = new ArrayList<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            List<Map<String, Object>> datalist = new ArrayList<>();
            datalist = hazardousWasteInfoService.countMainHazardousWasteTypeDataByParamMap(paramMap);
            if (datalist.size()>0){
                for (Map<String, Object> map:datalist){
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("categorycode",map.get("parentcode"));
                    onemap.put("categoryname",map.get("name"));
                    if (1 == datatype){
                        onemap.put("value", map.get("ProductionQuantitysum")!=null?Double.valueOf(map.get("ProductionQuantitysum").toString()):null);
                    }else if(2 == datatype){
                        onemap.put("value",map.get("EndingStockssum")!=null?Double.valueOf(map.get("EndingStockssum").toString()):null);
                    }else if(3 == datatype){
                        onemap.put("value",map.get("SelfuseQuantitysum")!=null?Double.valueOf(map.get("SelfuseQuantitysum").toString()):null);
                    }
                    //产污值不为空 则保存
                    if (onemap.get("value")!=null) {
                        result.add(onemap);
                    }
                }
            }
            if (result.size()>0){
                //按产废量排序
                result = result.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("value").toString())).reversed()).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/18 0018 下午 4:16
     * @Description: 通过自定义参数统计危废特性占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countHazardousWasteCharacteristicRatioData", method = RequestMethod.POST)
    public Object countHazardousWasteCharacteristicRatioData(@RequestJson(value = "starttime",required = false) String starttime,
                                                            @RequestJson(value = "endtime",required = false) String endtime
    ){
        try {
            Map<String,Object> paramMap = new HashMap<>();
            List<Map<String,Object>> result = new ArrayList<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            List<Map<String, Object>> datalist = new ArrayList<>();
            datalist = hazardousWasteInfoService.countHazardousWasteCharacteristicRatioData(paramMap);
            if (datalist.size()>0){
                List<Map<String,Object>> alllist = new ArrayList<>();
                String wxtx = "";
                //将 多危险特性数据 分隔成单个特性
                for( Map<String,Object> map:datalist){
                    if (map.get("HazardousProperty")!=null && !"".equals(map.get("HazardousProperty").toString())){
                        wxtx = map.get("HazardousProperty").toString();
                        String[] wxtxs = wxtx.split(",");
                        if (wxtxs.length>1){
                            for (String str:wxtxs){
                                Map<String,Object> onemap = new HashMap<>();
                                onemap.put("pkid",map.get("pkid"));
                                onemap.put("wastematerialtypecode",map.get("wastematerialtypecode"));
                                onemap.put("wastematerialtypename",map.get("wastematerialtypename"));
                                onemap.put("hazardousproperty",str);
                                alllist.add(onemap);
                            }
                        }else{
                            alllist.add(map);
                        }
                    }
                }
                if (alllist.size()>0){
                    //按危险特性分组
                    Map<String, List<Map<String, Object>>> collect = alllist.stream().filter(m -> m.get("hazardousproperty") != null).collect(Collectors.groupingBy(m -> m.get("hazardousproperty").toString()));
                    Double count = 0d;
                    for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                        Map<String,Object> twomap = new HashMap<>();
                        twomap.put("hazardousproperty",entry.getKey());
                        twomap.put("num",entry.getValue().size());
                        count = Double.valueOf(entry.getValue().size());
                        twomap.put("proportion",DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(count * 100/alllist.size())));
                        result.add(twomap);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/19 0019 上午 8:49
     * @Description: 通过自定义参数统计企业贮存危废量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countEntKeepStorageHazardousWasteRankData", method = RequestMethod.POST)
    public Object countEntKeepStorageHazardousWasteRankData(@RequestJson(value = "starttime",required = false) String starttime,
                                                             @RequestJson(value = "endtime",required = false) String endtime
    ){
        try {
            Map<String,Object> paramMap = new HashMap<>();
            List<Map<String,Object>> result = new ArrayList<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            result = hazardousWasteInfoService.countEntKeepStorageHazardousWasteRankData(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
