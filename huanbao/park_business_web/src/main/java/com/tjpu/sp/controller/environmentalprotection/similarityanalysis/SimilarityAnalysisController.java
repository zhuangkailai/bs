package com.tjpu.sp.controller.environmentalprotection.similarityanalysis;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.similarityanalysis.SimilarityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年7月19日 下午 2:23
 * @Description:恶臭相似度分析处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("similarityAnalysis")
public class SimilarityAnalysisController {


    @Autowired
    private SimilarityAnalysisService similarityAnalysisService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;


    /**
     * @author: xsm
     * @date: 2019/7/19 0019 下午 2:18
     * @Description:获取所有恶臭监测点信息（包括厂界恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAllStenchMonitorPointInfo", method = RequestMethod.POST)
    public Object getAllStenchMonitorPointInfo() {
        try {
            List<Map<String, Object>> listdata = similarityAnalysisService.getAllStenchMonitorPointInfo();
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("MonitorPointName"));
                    objmap.put("monitorpointtype", map.get("FK_MonitorPointTypeCode"));
                    objmap.put("dgimn", map.get("DGIMN"));
                    objmap.put("monitorpointcategory", map.get("MonitorPointCategory"));
                    result.add(objmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @author: mmt
     * @date: 2022/9/16 0019 下午 2:18
     * @Description:获取废水监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getWaterOutputPointInfo", method = RequestMethod.POST)
    public Object getWaterOutputPointInfo(@RequestJson(value = "type") Integer type) {
        try {
            HashMap<String, Object> paramMap = new HashMap<>();
            //污水处理厂进口排口
            if (type == 1) {
                paramMap.put("outputproperty", 6);
                paramMap.put("inorout", 2);
            }else if (type == 2){
                //废水总排口
                paramMap.put("outputproperty", 1);
            }else if (type == 3){
                //管网排口
                paramMap.put("outputproperty", 7);
            }
            List<Map<String, Object>> listdata = similarityAnalysisService.getWaterOutputPointInfo(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("MonitorPointName"));
                    objmap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
                    objmap.put("dgimn", map.get("DGIMN"));
                    result.add(objmap);
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
     * @date: 2019/7/22 0022 下午 2:18
     * @Description:获取所有VOC监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAllVocMonitorPointInfo", method = RequestMethod.POST)
    public Object getAllVocMonitorPointInfo() {
        try {
            List<Map<String, Object>> listdata = otherMonitorPointService.getAllMonitorEnvironmentalVocAndStatusInfo();
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("MonitorPointName"));
                    objmap.put("monitorpointtype", map.get("FK_MonitorPointTypeCode"));
                    objmap.put("monitorpointcategory", map.get("MonitorPointCategory"));
                    objmap.put("dgimn", map.get("DGIMN"));
                    result.add(objmap);
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
     * @date: 2019/7/23 0023 下午 1:36
     * @Description:获取所有空气监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAllAirMonitorStationInfo", method = RequestMethod.POST)
    public Object getAllAirMonitorStationInfo() {
        try {
            List<Map<String, Object>> listdata = airMonitorStationService.getAllMonitorAirStationAndStatusInfo();
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("MonitorPointName"));
                    objmap.put("dgimn", map.get("DGIMN"));
                    result.add(objmap);
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
     * @date: 2019/7/23 0023 上午 9:33
     * @Description:获取所有关联废气在线排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getSelectPollutionInfo", method = RequestMethod.POST)
    public Object getSelectPollutionInfo() {
        try {
            List<Map<String, Object>> result = similarityAnalysisService.getSelectPollutionInfo();
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 9:34
     * @Description:根据污染源ID获取该污染源下废气排口所监测的重点污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getGsaOutPutKeyPollutants", method = RequestMethod.POST)
    public Object getGsaOutPutKeyPollutants() {
        try {
            List<Map<String, Object>> result = similarityAnalysisService.getGsaOutPutKeyPollutants();
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/19 0019 下午 2:18
     * @Description:根据监测点类型获取该类型重点监测污染物（支持多类型，返合并后的污染物）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAllPollutantInfosByMonitorPointTypes", method = RequestMethod.POST)
    public Object getAllPollutantInfosByTypes(@RequestJson(value = "monitorpointtypes") List<String> monitorpointtypes) {
        try {
            List<Map<String, Object>> result = similarityAnalysisService.getAllKeyPollutantsByTypes(monitorpointtypes);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 9:14
     * @Description:根据自定义参数获取某类型监测点和其它监测点比较的相识度分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getPointAndOtherPointSimilarityDataByParamMap", method = RequestMethod.POST)
    public Object getPointAndOtherPointSimilarityDataByParamMap(@RequestJson(value = "datamark") Integer datamark,
                                                                @RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "endtime") String endtime,
                                                                @RequestJson(value = "dgimn") String dgimn,
                                                                @RequestJson(value = "pollutantcode") String pollutantcode,
                                                                @RequestJson(value = "comparedgimns") List<String> comparedgimns,
                                                                @RequestJson(value = "beforetime") Integer beforetime,
                                                                @RequestJson(value = "comparepollutantcode") String comparepollutantcode,
                                                                @RequestJson(value = "comparetype", required = false) Integer comparetype) {
        try {
            String comparestarttime = "";
            String compareendtime = "";
            //根据比较点位的监测时间判断比较的是哪个时间段的监测数据
            if (beforetime == 0) {//同时间时
                comparestarttime = starttime;
                compareendtime = endtime;
            } else {
                comparestarttime = addDate(starttime, datamark, beforetime);
                compareendtime = addDate(endtime, datamark, beforetime);
            }
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> listdatas = similarityAnalysisService.getOnePointMonitorDataByParamMap(paramMap);
            paramMap.clear();
            paramMap.put("starttime", comparestarttime);
            paramMap.put("endtime", compareendtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimnlist", comparedgimns);
            paramMap.put("pollutantcode", comparepollutantcode);
            if (comparetype!=null) {
                paramMap.put("monitorpointtype", comparetype);
            }
            List<Map<String, Object>> comparelistdatas = similarityAnalysisService.getMorePointMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> resultlist = similarityAnalysisService.getMonitorPointSimilarityByParamMap(listdatas, comparelistdatas, paramMap);
            Comparator<Object> comparebysimilarity = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("similarity").toString())).reversed();
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebysimilarity).collect(Collectors.toList());
            Map<String, Object> resultmap = new HashMap<>();
            if (comparelistdatas != null && comparelistdatas.size() > 0 && resultlist != null && resultlist.size() > 0) {
                List<Map<String, Object>> orderdata = getOrderBySimilarityData(collect, comparelistdatas);
                resultmap.put("comparedatalist", orderdata);
            } else {
                resultmap.put("comparedatalist", comparelistdatas);
            }
            resultmap.put("datalist", listdatas);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 9:14
     * @Description:根据自定义参数获取某类型监测点和排口比较的相识度分析数据
     * @updateUser:xsm
     * @updateDate:2020/11/30 0030 下午 4:48
     * @updateDescription:VOC类型可查全部污染物总和
     * @param: []
     */
    @RequestMapping(value = "getPointAndOutputSimilarityDataByParamMap", method = RequestMethod.POST)
    public Object getPointAndOutputSimilarityDataByParamMap(@RequestJson(value = "datamark") Integer datamark,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime,
                                                            @RequestJson(value = "dgimn") String dgimn,
                                                            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                            @RequestJson(value = "pollutantcode") String pollutantcode,
                                                            @RequestJson(value = "pollutionids") List<String> pollutionids,
                                                            @RequestJson(value = "beforetime") Integer beforetime,
                                                            @RequestJson(value = "comparepollutantcode") String comparepollutantcode
                                                           ) {
        try {
            String comparestarttime = "";
            String compareendtime = "";
            //根据比较点位的监测时间判断比较的是哪个时间段的监测数据
            if (beforetime == 0) {//同时间时
                comparestarttime = starttime;
                compareendtime = endtime;
            } else {
                comparestarttime = addDate(starttime, datamark, beforetime);
                compareendtime = addDate(endtime, datamark, beforetime);
            }
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            if (monitorpointtype!=null) {
                paramMap.put("monitorpointtype", monitorpointtype);
            }
            List<Map<String, Object>> listdatas = similarityAnalysisService.getOnePointMonitorDataByParamMap(paramMap);
            paramMap.clear();
            paramMap.put("starttime", comparestarttime);
            paramMap.put("endtime", compareendtime);
            paramMap.put("collection", collection);
            paramMap.put("pollutionids", pollutionids);
            paramMap.put("pollutantcode", comparepollutantcode);
            List<Map<String, Object>> comparelistdatas = similarityAnalysisService.getMoreOutputMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> resultlist = similarityAnalysisService.getOutputSimilarityByParamMap(listdatas, comparelistdatas, paramMap);
            Comparator<Object> comparebysimilarity = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("similarity").toString())).reversed();
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebysimilarity).collect(Collectors.toList());
            Map<String, Object> resultmap = new HashMap<>();
            if (comparelistdatas != null && comparelistdatas.size() > 0 && resultlist != null && resultlist.size() > 0) {
                List<Map<String, Object>> orderdata = getOrderBySimilarityData(collect, comparelistdatas);
                resultmap.put("comparedatalist", orderdata);
            } else {
                resultmap.put("comparedatalist", comparelistdatas);
            }
            resultmap.put("datalist", listdatas);

            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/6 0006 下午 7:07
     * @Description:按相似度进行排序，并返回数据（从大到小）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getOrderBySimilarityData(List<Map<String, Object>> collect, List<Map<String, Object>> comparelistdatas) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> obj : collect) {
            for (Map<String, Object> objmap : comparelistdatas) {
                if ((obj.get("dgimn").toString()).equals(objmap.get("dgimn").toString())) {
                    result.add(objmap);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 9:14
     * @Description:根据时间、时间类型和时间差值获取时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private String addDate(String datetime, Integer datamark, int num) {
        SimpleDateFormat format = null;
        if ("MinuteData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//分钟数据
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");// 分钟
        } else if ("HourData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//小时数据
            format = new SimpleDateFormat("yyyy-MM-dd HH");// 24小时制
        } else if ("DayData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//日数据
            format = new SimpleDateFormat("yyyy-MM-dd");// 日
        }
        Date date = null;
        try {
            date = format.parse(datetime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (date == null)
            return "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if ("MinuteData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//分钟数据
            cal.add(Calendar.MINUTE, -num);// 分钟
        } else if ("HourData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//小时数据
            cal.add(Calendar.HOUR, -num);// 24小时制
        } else if ("DayData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//日数据
            cal.add(Calendar.DAY_OF_YEAR, -num);// 日
        }
        date = cal.getTime();
        cal = null;
        return format.format(date);

    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 4:17
     * @Description:根据自定义参数获取相似度列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getSimilarityListDataByParamMap", method = RequestMethod.POST)
    public Object getSimilarityListDataByParamMap(@RequestJson(value = "datamark") Integer datamark,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "dgimn", required = false) String dgimn,
                                                  @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                  @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                  @RequestJson(value = "comparedgimns", required = false) List<String> comparedgimns,
                                                  @RequestJson(value = "pollutionids", required = false) List<String> pollutionids,
                                                  @RequestJson(value = "beforetime") Integer beforetime,
                                                  @RequestJson(value = "comparepollutantcode") String comparepollutantcode,
                                                  @RequestJson(value = "comparetype", required = false) Integer comparetype) {
        try {
            String comparestarttime = "";
            String compareendtime = "";
            //根据比较点位的监测时间判断比较的是哪个时间段的监测数据
            if (beforetime == 0) {//同时间时
                comparestarttime = starttime;
                compareendtime = endtime;
            } else {
                comparestarttime = addDate(starttime, datamark, beforetime);
                compareendtime = addDate(endtime, datamark, beforetime);
            }
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            if (monitorpointtype!=null) {
                paramMap.put("monitorpointtype", monitorpointtype);
            }
            List<Map<String, Object>> listdatas = similarityAnalysisService.getOnePointMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> resultlist = new ArrayList<>();
            if (comparedgimns != null) {
                paramMap.clear();
                paramMap.put("starttime", comparestarttime);
                paramMap.put("endtime", compareendtime);
                paramMap.put("collection", collection);
                paramMap.put("dgimnlist", comparedgimns);
                paramMap.put("pollutantcode", comparepollutantcode);
                if (comparetype!=null) {
                    paramMap.put("monitorpointtype", comparetype);
                }
                List<Map<String, Object>> comparelistdatas = similarityAnalysisService.getMorePointMonitorDataByParamMap(paramMap);
                resultlist = similarityAnalysisService.getMonitorPointSimilarityByParamMap(listdatas, comparelistdatas, paramMap);
            }
            if (pollutionids != null) {
                paramMap.clear();
                paramMap.put("starttime", comparestarttime);
                paramMap.put("endtime", compareendtime);
                paramMap.put("collection", collection);
                paramMap.put("pollutionids", pollutionids);
                paramMap.put("pollutantcode", comparepollutantcode);
                List<Map<String, Object>> comparelistdatas = similarityAnalysisService.getMoreOutputMonitorDataByParamMap(paramMap);
                resultlist = similarityAnalysisService.getOutputSimilarityByParamMap(listdatas, comparelistdatas, paramMap);
            }
            //按相似度倒序排
            Comparator<Object> comparebysimilarity = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("similarity").toString())).reversed();
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebysimilarity).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/8/25 0025 上午 10:50
     * @Description:根据恶臭标记类型获取该类型恶臭点位的相似度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [stinkflag:1 敏感点 ，2 传输点 3 厂界恶臭]
     */
    @RequestMapping(value = "getStinkSimilarityDataByParamMap", method = RequestMethod.POST)
    public Object getStinkSimilarityDataByParamMap(@RequestJson(value = "stinkflag") List<Integer> stinkflag,
                                                   @RequestJson(value = "datetype") String datetype,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "pollutantcodes") List<String> pollutantcodes
                                                     ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> collect = new ArrayList<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            if (stinkflag!=null&&stinkflag.size()>0) {
                paramMap.put("stinkflag", stinkflag);
                List<Map<String, Object>> otherMonitorPoint = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
                if (otherMonitorPoint != null && otherMonitorPoint.size() > 0) {
                    paramMap.clear();
                    paramMap.put("starttime", starttime);
                    paramMap.put("endtime", endtime);
                    paramMap.put("pollutantcodes", pollutantcodes);
                    paramMap.put("datetype", datetype);
                    resultlist = similarityAnalysisService.getMorePointMonitorSimilarityDataByParamMap(paramMap, otherMonitorPoint);
                }
                //按相似度倒序排
                Comparator<Object> comparebysimilarity = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("similarity").toString())).reversed();
                collect = resultlist.stream().sorted(comparebysimilarity).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/30 0030 下午 4:01
     * @Description:获取VOC主要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getVocKeyPollutantData", method = RequestMethod.POST)
    public Object getVocKeyPollutantData() {
        try {
            List<String> monitorpointtypes = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            monitorpointtypes.add( CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()+"");
            List<Map<String, Object>> voclist = similarityAnalysisService.getAllKeyPollutantsByTypes(monitorpointtypes);
            if (voclist!=null&&voclist.size()>0){
                Map<String, Object> map = new HashMap<>();
                String voccode  = DataFormatUtil.parseProperties("pollutant.voccode");
                String vocname  = DataFormatUtil.parseProperties("pollutant.vocname");
                map.put("fk_monitorpointtypecode",CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                map.put("pollutantunit","ppb");
                map.put("orderindex","0");
                map.put("code",voccode);
                map.put("name",vocname);
                result.add(map);
                result.addAll(voclist);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
