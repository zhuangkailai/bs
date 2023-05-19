package com.tjpu.sp.controller.environmentalprotection.tracesamplesimilarity;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.RealTimeDataVO;
import com.tjpu.sp.model.environmentalprotection.tracesamplesimilarity.TraceSampleSimilarityVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.environmentalprotection.tracesamplesimilarity.TraceSampleSimilarityService;
import com.tjpu.sp.service.environmentalprotection.tracesourcesample.TraceSourceSampleService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.FingerPrintDatabaseEnum;


/**
 * @author: chengzq
 * @date: 2020/11/11 0011 下午 1:58
 * @Description: 溯源样品相似度控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("tracesamplesimilarity")
public class TraceSampleSimilarityController {

    @Autowired
    private TraceSampleSimilarityService traceSampleSimilarityService;
    @Autowired
    private TraceSourceSampleService traceSourceSampleService;
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PubCodeService pubCodeService;
    @Autowired
    private StopProductionInfoService stopProductionInfoService;

    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 2:58
     * @Description: 通过自定义参数获取溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSampleSimilarityByParamMap", method = RequestMethod.POST)
    public Object getTraceSampleSimilarityByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();

            List<Map<String, Object>> traceSampleSimilarityByParamMap = traceSampleSimilarityService.getTraceSampleSimilarityByParamMap(jsonObject);
            long total = traceSampleSimilarityByParamMap.size();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                traceSampleSimilarityByParamMap = traceSampleSimilarityByParamMap.stream().skip((Integer.valueOf(jsonObject.get("pagenum").toString()) - 1) * Integer.valueOf(jsonObject.get("pagesize").toString()))
                        .limit(Integer.valueOf(jsonObject.get("pagesize").toString())).collect(Collectors.toList());
            }
            resultMap.put("datalist", traceSampleSimilarityByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 3:37
     * @Description: 通过样品id计算样品、指纹相似度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/calculatTraceSampleSimilarity", method = RequestMethod.POST)
    public Object calculatTraceSampleSimilarity(@RequestJson(value = "fktracesampleid") String fktracesampleid) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Double prop = 0.3d;
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            //样品
            Map<String, Object> result = traceSourceSampleService.selectByPrimaryKey(fktracesampleid);

            String CharacterPollutantcodes = result.get("CharacterPollutantcodes") == null ? "" : result.get("CharacterPollutantcodes").toString();

            if (result == null) {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }


            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("isfingerdatabase", 1);
            //所有企业指纹
            List<Map<String, Object>> traceSourceSampleByParamMap = traceSourceSampleService.getTraceSourceSampleByParamMap(paramMap);
            traceSourceSampleByParamMap.add(result);//将样品添加到指纹里统一查询


            String pkids = traceSourceSampleByParamMap.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).collect(Collectors.joining(","));
            RealTimeDataVO realTimeDataVO = new RealTimeDataVO();
            realTimeDataVO.setDataGatherCode(pkids);
            List<RealTimeDataVO> realTimeData = mongoBaseService.getListByParam(realTimeDataVO, "RealTimeData", null);
            //将样品数据和实时数据组装起来
            Iterator<Map<String, Object>> iterator = traceSourceSampleByParamMap.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> stringObjectMap = iterator.next();
                String pkid = stringObjectMap.get("pkid") == null ? "" : stringObjectMap.get("pkid").toString();
                RealTimeDataVO realTimeDataVO1 = realTimeData.stream().filter(m -> m.getDataGatherCode() != null && pkid.equals(m.getDataGatherCode())).findFirst().orElse(new RealTimeDataVO());
                List<Map<String, Object>> realDataList = realTimeDataVO1.getRealDataList() == null ? new ArrayList<>() : realTimeDataVO1.getRealDataList();
                Map<String, String[]> collect = realDataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("MonitorValue") != null && m.get("OverMultiple") != null).collect(Collectors.
                        toMap(m -> m.get("PollutantCode").toString(), m -> new String[]{m.get("MonitorValue").toString(), m.get("OverMultiple").toString()}, (a, b) -> a));
                stringObjectMap.put("pollutants", collect);
                stringObjectMap.put("onlineid", realTimeDataVO1.getId());

                //将样品重新赋值，并且从指纹数据中移除
                if (pkid.equals(fktracesampleid)) {
                    result = stringObjectMap;
                    iterator.remove();
                }
            }
            List<Map<String, Object>> title = getTitle();


            List<Map<String, Object>> relationPercentList = new ArrayList<>();//相关性算法容器
            List<Map<String, Object>> cosineSimilarityList = new ArrayList<>();//余弦相似度容器
            List<Map<String, Object>> vectorSimilarityList = new ArrayList<>();//向量相似度容器

            //只获取每个排口相似度最大的指纹信息（一个排口不同实际会产生不同的指纹），所以分组获取最大的
            Map<String, List<Map<String, Object>>> collect = traceSourceSampleByParamMap.stream().filter(m -> m.get("outputname") != null).collect(Collectors.groupingBy(m -> m.get("outputname").toString()));
            for (String samplename : collect.keySet()) {
                List<Map<String, Object>> maps = collect.get(samplename);
                for (Map<String, Object> map : maps) {
                    Map<String, List<Double>> sampleVuleData = getSampleVuleData(result, map, title, CharacterPollutantcodes);
                    List<Double> sampleVuleDatum = sampleVuleData.get("sampleList");
                    List<Double> samplePropVuleDatum = sampleVuleData.get("samplepropList");
                    List<Double> fingerVuleDatum = sampleVuleData.get("fingerList");
                    List<Double> fingerPropVuleDatum = sampleVuleData.get("fingerpropList");
                    Double CharacterPollutantValue = sampleVuleData.get("CharacterPollutantValue").get(0);


                    Double relationPercent = DataFormatUtil.getRelationPercent(sampleVuleDatum, fingerVuleDatum) == null ? 0d : DataFormatUtil.getRelationPercent(sampleVuleDatum, fingerVuleDatum);//浓度相关性
                    Double cosineSimilarity = DataFormatUtil.cosineSimilarity(sampleVuleDatum, fingerVuleDatum) == null ? 0d : DataFormatUtil.cosineSimilarity(sampleVuleDatum, fingerVuleDatum);//浓度余弦相似度
                    Double vectorSimilarity = DataFormatUtil.vectorSimilarity(sampleVuleDatum, fingerVuleDatum) == null ? 0d : DataFormatUtil.vectorSimilarity(sampleVuleDatum, fingerVuleDatum);//浓度向量相似度

                    Double relationpropPercent = DataFormatUtil.getRelationPercent(samplePropVuleDatum, fingerPropVuleDatum) == null ? 0d : DataFormatUtil.getRelationPercent(sampleVuleDatum, fingerVuleDatum);//占比相关性
                    Double cosinepropSimilarity = DataFormatUtil.cosineSimilarity(samplePropVuleDatum, fingerPropVuleDatum) == null ? 0d : DataFormatUtil.cosineSimilarity(sampleVuleDatum, fingerVuleDatum);//占比余弦相似度
                    Double vectorpropSimilarity = DataFormatUtil.vectorSimilarity(samplePropVuleDatum, fingerPropVuleDatum) == null ? 0d : DataFormatUtil.vectorSimilarity(sampleVuleDatum, fingerVuleDatum);//占比向量相似度


                    /*
                        1.如果是溯源样本没有特征污染物则使用原来的算法不变，
                        2.溯源样本有特征污染物则使用特征污染物占比进行计算：
                        公式：原算法计算结果*权重（0.4）+ （特征污染物1占比+特征污染物2占比...）*权重（0.6）
                     */
                    if (StringUtils.isNotBlank(CharacterPollutantcodes)) {
                        //相似度大于百分之30返回数据
                        getTraceSampleSimilarity(relationPercent, relationpropPercent, map, result, decimalFormat, "a", relationPercentList, CharacterPollutantValue);
                        getTraceSampleSimilarity(cosineSimilarity, cosinepropSimilarity, map, result, decimalFormat, "b", cosineSimilarityList, CharacterPollutantValue);
                        getTraceSampleSimilarity(vectorSimilarity, vectorpropSimilarity, map, result, decimalFormat, "c", vectorSimilarityList, CharacterPollutantValue);

                    } else {
                        getTraceSampleSimilarity(relationPercent, relationpropPercent, map, result, decimalFormat, "a", relationPercentList, null);
                        getTraceSampleSimilarity(cosineSimilarity, cosinepropSimilarity, map, result, decimalFormat, "b", cosineSimilarityList, null);
                        getTraceSampleSimilarity(vectorSimilarity, vectorpropSimilarity, map, result, decimalFormat, "c", vectorSimilarityList, null);
                    }
                }
            }
            //默认按照浓度相似度排序
            resultMap.put("a", relationPercentList.stream().filter(m -> m.get("similarity") != null).sorted(Comparator.comparing(m -> Double.valueOf(((Map<String, Object>) m).get("similarity").toString())).reversed()).collect(Collectors.toList()));
            resultMap.put("b", cosineSimilarityList.stream().filter(m -> m.get("similarity") != null).sorted(Comparator.comparing(m -> Double.valueOf(((Map<String, Object>) m).get("similarity").toString())).reversed()).collect(Collectors.toList()));
            resultMap.put("c", vectorSimilarityList.stream().filter(m -> m.get("similarity") != null).sorted(Comparator.comparing(m -> Double.valueOf(((Map<String, Object>) m).get("similarity").toString())).reversed()).collect(Collectors.toList()));

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void getTraceSampleSimilarity(Double similarity, Double similarityprop, Map<String, Object> map, Map<String, Object> result, DecimalFormat decimalFormat, String calculattype, List<Map<String, Object>> relationPercentList, Double CharacterPollutantValue) {

        Double similarityweight = 0.4d; //趋势权重
        Double proportionweight = 0.6d; //特征污染物权重
        Integer prop = 30;

        Map<String, Object> data = new HashMap<>();
        data.put("pollutionname", map.get("PollutionName"));
        data.put("outputname", map.get("outputname"));
        data.put("CalculatType", calculattype);

        data.put("fkfingerprintid", map.get("pkid") == null ? "" : map.get("pkid").toString());//指纹id
        data.put("fktracesampleid", result.get("pkid") == null ? "" : result.get("pkid").toString());//样品id


        Double similarityvalue = CharacterPollutantValue == null ? Double.valueOf(decimalFormat.format(similarity * 100)) : Double.valueOf(decimalFormat.format(similarity * 100 * similarityweight + CharacterPollutantValue * 100 * proportionweight));
        Double proportionsimilarityvalue = CharacterPollutantValue == null ? Double.valueOf(decimalFormat.format(similarityprop * 100)) : Double.valueOf(decimalFormat.format(similarityprop * 100 * similarityweight + CharacterPollutantValue * 100 * proportionweight));
        data.put("similarity", similarityvalue);//浓度相似度
        data.put("proportionsimilarity", proportionsimilarityvalue);//占比相似度

        //相似度大于百分之30返回数据
        if (similarityvalue > prop || proportionsimilarityvalue > prop) {
            relationPercentList.add(data);
        }
    }


    private Map<String, List<Double>> getSampleVuleData(Map<String, Object> sample, Map<String, Object> finger, List<Map<String, Object>> title, String CharacterPollutantcodes) {
        //污染物监测数据缺失需要补充0
        Map<String, Object> sampleMap = sample.get("pollutants") == null ? new HashMap<>() : (Map<String, Object>) sample.get("pollutants");
        Map<String, Object> fingerMap = finger.get("pollutants") == null ? new HashMap<>() : (Map<String, Object>) finger.get("pollutants");
        List<Double> sampleList = new ArrayList<>();
        List<Double> samplepropList = new ArrayList<>();
        List<Double> fingerList = new ArrayList<>();
        List<Double> fingerpropList = new ArrayList<>();
        Map<String, List<Double>> result = new HashMap<>();
        result.put("sampleList", sampleList);
        result.put("samplepropList", samplepropList);
        result.put("fingerList", fingerList);
        result.put("fingerpropList", fingerpropList);


        List<Double> CharacterPollutantValues=new ArrayList<>();
        for (Map<String, Object> stringObjectMap : title) {
            String code = stringObjectMap.get("code") == null ? "" : stringObjectMap.get("code").toString();
            String[] samplevalues = sampleMap.get(code) == null ? new String[]{} : (String[]) sampleMap.get(code);
            Double sampledata=0d;
            if (samplevalues.length > 1) {
                if (StringUtils.isNotBlank(samplevalues[0])) {
                    Double value = Double.valueOf(samplevalues[0]);//浓度
                    sampleList.add(value);

                } else {
                    sampleList.add(0d);
                }
                if (StringUtils.isNotBlank(samplevalues[1])) {
                    Double propvalue = Double.valueOf(samplevalues[1]);//占比
                    samplepropList.add(propvalue);


                    if (StringUtils.isNotBlank(CharacterPollutantcodes) && CharacterPollutantcodes.contains(code) && propvalue>0) {
                        sampledata=propvalue;
                    }


                } else {
                    samplepropList.add(0d);
                }
            }
            String[] fingervalues = fingerMap.get(code) == null ? new String[]{} : (String[]) fingerMap.get(code);
            Double fingerdata=0d;
            if (fingervalues.length > 1) {
                if (StringUtils.isNotBlank(fingervalues[0])) {
                    Double value = Double.valueOf(fingervalues[0]);//浓度

                    fingerList.add(value);
                } else {
                    fingerList.add(0d);

                }
                if (StringUtils.isNotBlank(fingervalues[1])) {
                    Double propvalue = Double.valueOf(fingervalues[1]);//占比
                    fingerpropList.add(propvalue);
                    if (StringUtils.isNotBlank(CharacterPollutantcodes) && CharacterPollutantcodes.contains(code) && propvalue>0) {
                        fingerdata=propvalue;
                    }
                } else {
                    fingerpropList.add(0d);

                }
            }

            if(fingerdata!=0){
                double v = 1 - Math.abs((fingerdata - sampledata) / (fingerdata + sampledata));
                CharacterPollutantValues.add(v);
            }
        }
        result.put("CharacterPollutantValue", Arrays.asList(CharacterPollutantValues.stream().collect(Collectors.averagingDouble(m->m))));

        return result;
    }


    /**
     * @author: chengzq
     * @date: 2020/10/22 0022 下午 4:19
     * @Description: 获取voc污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private List<Map<String, Object>> getTitle() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tablename", "PUB_CODE_PollutantFactor");
        paramMap.put("fields", Arrays.asList("code", "name", "pollutantunit"));
        paramMap.put("wherestring", "PollutantType=" + FingerPrintDatabaseEnum.getCode() + " and isused=1");
        paramMap.put("orderfield", "OrderIndex");
        return pubCodeService.getPubCodesDataByParam(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 3:17
     * @Description: 新增溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addTraceSampleSimilarity", method = RequestMethod.POST)
    public Object addTraceSampleSimilarity(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            List<TraceSampleSimilarityVO> datalist = new ArrayList<>();
            JSONArray jsonArray = JSONArray.fromObject(addformdata);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            for (Object o : jsonArray) {
                TraceSampleSimilarityVO entity = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new TraceSampleSimilarityVO());
                entity.setpkid(UUID.randomUUID().toString());
                entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                entity.setupdateuser(username);
                datalist.add(entity);
            }

            traceSampleSimilarityService.insertBatch(datalist);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 3:19
     * @Description: 通过id获取溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSampleSimilarityByID", method = RequestMethod.POST)
    public Object getTraceSampleSimilarityByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = traceSampleSimilarityService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 3:19
     * @Description: 修改溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateTraceSampleSimilarity", method = RequestMethod.POST)
    public Object updateTraceSampleSimilarity(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            TraceSampleSimilarityVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TraceSampleSimilarityVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            traceSampleSimilarityService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 3:21
     * @Description: 通过id删除溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteTraceSampleSimilarityByID", method = RequestMethod.POST)
    public Object deleteTraceSampleSimilarityByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            traceSampleSimilarityService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 4:54
     * @Description: 通过样品id删除溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fktracesampleid]
     * @throws:
     */
    @RequestMapping(value = "/deleteByFktracesampleid", method = RequestMethod.POST)
    public Object deleteByFktracesampleid(@RequestJson(value = "fktracesampleid") String fktracesampleid) throws Exception {
        try {
            traceSampleSimilarityService.deleteByFktracesampleid(fktracesampleid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0011 下午 3:31
     * @Description: 通过id查询溯源样品相似度信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSampleSimilarityDetailByID", method = RequestMethod.POST)
    public Object getTraceSampleSimilarityDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> detailInfo = traceSampleSimilarityService.getTraceSampleSimilarityDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/26 0026 下午 4:43
     * @Description: 溯源gis接口，通过多参数获取相似度排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [calculattypes]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSampleSimilarityRanking", method = RequestMethod.POST)
    public Object getTraceSampleSimilarityRanking(@RequestJson(value = "calculattypes") Object calculattypes,
                                                  @RequestJson(value = "starttime", required = false) String starttime,
                                                  @RequestJson(value = "endtime", required = false) String endtime,
                                                  @RequestJson(value = "similaritytype") Integer similaritytype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("calculattypes", calculattypes);
            List<Map<String, Object>> traceSampleSimilarityByParamMap = traceSampleSimilarityService.getTraceSampleSimilarityByParamMap(paramMap);

            String key = "";
            if (similaritytype == 1) {//浓度相似度
                key = "similarity";
            } else if (similaritytype == 2) {//占比相似度
                key = "proportionsimilarity";
            } else {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }


            List<Map<String, Object>> currentTimeStopProductionInfoByParamMap = stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(paramMap);//停产排口
            //如果停产设置排口状态为停产
            for (Map<String, Object> stringObjectMap : currentTimeStopProductionInfoByParamMap) {
                String FK_Outputid = stringObjectMap.get("FK_Outputid") == null ? "" : stringObjectMap.get("FK_Outputid").toString();
                String FK_MonitorPointType = stringObjectMap.get("FK_MonitorPointType") == null ? "" : stringObjectMap.get("FK_MonitorPointType").toString();
                for (Map<String, Object> map : traceSampleSimilarityByParamMap) {
                    String FK_MonitorPointTypeCode = map.get("FK_MonitorPointTypeCode") == null ? "" : map.get("FK_MonitorPointTypeCode").toString();
                    String FK_MonitorpointId = map.get("FK_MonitorpointId") == null ? "" : map.get("FK_MonitorpointId").toString();
                    if (FK_Outputid.equals(FK_MonitorpointId) && FK_MonitorPointType.equals(FK_MonitorPointTypeCode)) {
                        map.put("Status", 4);
                    }
                }
            }

            //当传入单个计算类型时，根据每个排口相似度排名；当传入多个计算类型时，根据每个排口多个计算类型的最大相似度排名
            String finalKey = key;
            List<Map<String, Object>> values = traceSampleSimilarityByParamMap.stream().filter(m -> (m.get("FK_MonitorPointTypeCode") != null && m.get("FK_MonitorpointId") != null) || m.get("SampleName") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorpointId").toString()
                    + m.get("FK_MonitorPointTypeCode").toString() + m.get("SampleName").toString(), Collectors.collectingAndThen(Collectors.toList(), m -> m.stream().filter(n -> n.get(finalKey) != null).max(Comparator.comparing(n -> Double.valueOf(n.get(finalKey).toString())))
                    .orElse(new HashMap<>())))).values().stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map<String, Object>) m).get(finalKey).toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", values);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
