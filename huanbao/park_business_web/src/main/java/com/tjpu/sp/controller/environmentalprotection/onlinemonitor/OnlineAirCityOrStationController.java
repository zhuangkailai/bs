package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirCityMonthDataService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @Description: 空气站点或城市统计分析
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/5/24 8:45
 */
@RestController
@RequestMapping("onlineCityOrStation")
public class OnlineAirCityOrStationController {

    private final PollutantService pollutantService;
    private final AirCityMonthDataService airCityMonthDataService;
    private final AirMonitorStationService airMonitorStationService;

    private final String DB_CityDayAQIData = "CityDayAQIData";
    private final String DB_StationDayAQIData = "StationDayAQIData";


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    /**
     * 大气监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();

    public OnlineAirCityOrStationController(PollutantService pollutantService, AirCityMonthDataService airCityMonthDataService, AirMonitorStationService airMonitorStationService) {
        this.pollutantService = pollutantService;
        this.airCityMonthDataService = airCityMonthDataService;
        this.airMonitorStationService = airMonitorStationService;
    }


    /**
     * @Description: 获取空气质量优良天数数据（同比、环比）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/24 8:50
     */
    @RequestMapping(value = "getAirQualityNumDataByParamMap", method = RequestMethod.POST)
    public Object getAirQualityNumDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "datatype") String datatype
    ) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            String collection;
            String mnKey;
            if (datatype.equals("city")) {
                collection = DB_CityDayAQIData;
                mnKey = "RegionCode";
            } else {
                collection = DB_StationDayAQIData;
                mnKey = "StationCode";
            }
            //获取同比、环比时间段
            String tb_starttime = DataFormatUtil.getDayYearTBDate(starttime, 1);
            String tb_endtime = DataFormatUtil.getDayYearTBDate(endtime, 1);
            String hb_starttime = DataFormatUtil.getDayTBDate(starttime);
            String hb_endtime = DataFormatUtil.getDayTBDate(endtime);
            //判断当前日期是否当月最后一天
            String month = DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd", "yyyy-MM");
            String lastDay = DataFormatUtil.getLastDayOfMonth(month);
            if (endtime.equals(lastDay)) {
                month = DataFormatUtil.FormatDateOneToOther(hb_endtime, "yyyy-MM-dd", "yyyy-MM");
                hb_endtime = DataFormatUtil.getLastDayOfMonth(month);
            }
            paramMap.put("collection", collection);
            paramMap.put("mnKey", mnKey);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Document> this_doc = getCityOrStationDayDataByParam(paramMap);
            //优良天数、优良率、占比数据
            int yltsnum = getYLTS(this_doc);
            int totalnum = this_doc.size();
            resultMap.put("yltsnum", yltsnum);
            if (totalnum > 0) {
                resultMap.put("yltsrate", DataFormatUtil.SaveOneAndSubZero(100D * yltsnum / totalnum));
            } else {
                resultMap.put("yltsrate", "0");
            }
            List<Map<String, Object>> rateList = getRateList(this_doc);
            resultMap.put("rateList", rateList);
            paramMap.put("starttime", tb_starttime);
            paramMap.put("endtime", tb_endtime);
            List<Document> tb_doc = getCityOrStationDayDataByParam(paramMap);

            resultMap.put("tb_yltsnum", getYLTS(tb_doc));

            paramMap.put("starttime", hb_starttime);
            paramMap.put("endtime", hb_endtime);
            List<Document> hb_doc = getCityOrStationDayDataByParam(paramMap);
            resultMap.put("hb_yltsnum", getYLTS(hb_doc));

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取空气质量优良占比数据（同比、环比）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/24 8:50
     */
    @RequestMapping(value = "getAirQualityRateDataByParamMap", method = RequestMethod.POST)
    public Object getAirQualityRateDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "datatype") String datatype) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            String collection;
            String mnKey;
            if (datatype.equals("city")) {
                collection = DB_CityDayAQIData;
                mnKey = "RegionCode";
            } else {
                collection = DB_StationDayAQIData;
                mnKey = "StationCode";
            }
            //获取同比、环比时间段
            String tb_starttime = DataFormatUtil.getDayYearTBDate(starttime, 1);
            String tb_endtime = DataFormatUtil.getDayYearTBDate(endtime, 1);
            String hb_starttime = DataFormatUtil.getDayTBDate(starttime);
            String hb_endtime = DataFormatUtil.getDayTBDate(endtime);
            //判断当前日期是否当月最后一天
            String month = DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd", "yyyy-MM");
            String lastDay = DataFormatUtil.getLastDayOfMonth(month);
            if (endtime.equals(lastDay)) {
                month = DataFormatUtil.FormatDateOneToOther(hb_endtime, "yyyy-MM-dd", "yyyy-MM");
                hb_endtime = DataFormatUtil.getLastDayOfMonth(month);
            }
            paramMap.put("collection", collection);
            paramMap.put("mnKey", mnKey);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Document> this_doc = getCityOrStationDayDataByParam(paramMap);
            List<Map<String, Object>> this_data = getRateList(this_doc);
            resultMap.put("this_data", this_data);
            paramMap.put("starttime", tb_starttime);
            paramMap.put("endtime", tb_endtime);
            List<Document> tb_doc = getCityOrStationDayDataByParam(paramMap);
            List<Map<String, Object>> tb_data = getRateList(tb_doc);
            resultMap.put("tb_data", tb_data);
            paramMap.put("starttime", hb_starttime);
            paramMap.put("endtime", hb_endtime);
            List<Document> hb_doc = getCityOrStationDayDataByParam(paramMap);
            List<Map<String, Object>> hb_data = getRateList(hb_doc);
            resultMap.put("hb_data", hb_data);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取空气质量优良占比数据（同比、环比）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/24 8:50
     */
    @RequestMapping(value = "getAirQualityOverDataByParamMap", method = RequestMethod.POST)
    public Object getAirQualityOverDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "datatype") String datatype) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            String collection;
            String mnKey;
            if (datatype.equals("city")) {
                collection = DB_CityDayAQIData;
                mnKey = "RegionCode";
            } else {
                collection = DB_StationDayAQIData;
                mnKey = "StationCode";
            }

            paramMap.put("collection", collection);
            paramMap.put("mnKey", mnKey);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Document> this_doc = getCityOrStationDayDataByParam(paramMap);
            Map<String, Object> yl_map = getYLMap(this_doc);
            resultMap.put("yl_map", yl_map);
            List<Map<String, Object>> primaryList = getPrimaryList(this_doc);
            resultMap.put("primaryList", primaryList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getPrimaryList(List<Document> this_doc) {

        Map<String, Object> codeAndName = getCodeAndName();
        Map<String, Integer> codeAndNum = new HashMap<>();
        String code;
        String[] codes;
        int totalnum = 0;
        for (Document document : this_doc) {
            code = document.getString("PrimaryPollutant");
            codes = code.split(",");
            for (int i = 0; i < codes.length; i++) {
                code = codes[i];
                totalnum++;
                codeAndNum.put(code, codeAndNum.get(code) != null ? codeAndNum.get(code) + 1 : 1);
            }
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        int num;
        for (String codeIndex : codeAndNum.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", codeIndex);
            resultMap.put("name", codeAndName.get(codeIndex));
            num = codeAndNum.get(codeIndex);
            resultMap.put("num", num);
            resultMap.put("rate", DataFormatUtil.SaveOneAndSubZero(100D * num / totalnum));
            resultList.add(resultMap);
        }
        return resultList;
    }

    private Map<String, Object> getYLMap(List<Document> this_doc) {
        Map<String, Object> resultMap = new HashMap<>();
        int totalnum = this_doc.size();
        int yl_num = getYLTS(this_doc);
        resultMap.put("totalnum", totalnum);
        resultMap.put("yl_num", yl_num);
        int over_num = totalnum - yl_num;
        if (totalnum > 0) {
            resultMap.put("yl_rate", DataFormatUtil.SaveOneAndSubZero(100D * yl_num / totalnum));
        } else {
            resultMap.put("yl_rate", 0);
        }
        resultMap.put("over_num", over_num);
        if (totalnum > 0) {
            resultMap.put("over_rate", DataFormatUtil.SaveOneAndSubZero(100D * over_num / totalnum));
        } else {
            resultMap.put("over_rate", 0);
        }
        return resultMap;
    }


    private List<Map<String, Object>> getRateList(List<Document> this_doc) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Integer> codeAndNum = new HashMap<>();
        String code;
        for (Document document : this_doc) {
            code = document.getString("AirQuality");
            codeAndNum.put(code, codeAndNum.get(code) != null ? codeAndNum.get(code) + 1 : 1);
        }
        int total = this_doc.size();
        int num;
        for (String codeIndex : codeAndNum.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("airquality", codeIndex);
            num = codeAndNum.get(codeIndex);
            resultMap.put("num", num);
            resultMap.put("rate", DataFormatUtil.SaveOneAndSubZero(100D * num / total));
            resultList.add(resultMap);

        }
        return resultList;
    }

    private int getYLTS(List<Document> this_doc) {

        int yltsnum = 0;
        int maxAqi = 100;
        for (Document document : this_doc) {
            if (document.get("AQI") != null) {
                if (document.getInteger("AQI") <= maxAqi) {
                    yltsnum++;
                }
            }
        }
        return yltsnum;
    }

    private List<Document> getCityOrStationDayDataByParam(Map<String, Object> paramMap) {

        Date startDate = DataFormatUtil.getDateYMD(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMD(paramMap.get("endtime").toString());
        String mnKey = (String) paramMap.get("mnKey");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", mnKey, "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        String collection = (String) paramMap.get("collection");
        List<String> mns = (List<String>) paramMap.get("mns");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and(mnKey).in(mns)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> documents = hourAQIData.getMappedResults();
        return documents;
    }


    private Map<String, Object> getCodeAndName() {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", monitorPointTypeCode);
        List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
        paramMap.clear();
        for (Map<String, Object> pollutant : pollutants) {
            paramMap.put(pollutant.get("code").toString(), pollutant.get("name"));
        }
        return paramMap;
    }

    /**
     * @Description: 获取城市、站点因子分指数数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/24 8:50
     */
    @RequestMapping(value = "getPollutantIAQIDataByParamMap", method = RequestMethod.POST)
    public Object getPollutantIAQIDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                 @RequestJson(value = "monitortime") String monitortime,
                                                 @RequestJson(value = "datatype") String datatype) throws Exception {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            String collection;
            String mnKey;
            if (datatype.equals("city")) {
                collection = DB_CityDayAQIData;
                mnKey = "RegionCode";
            } else {
                collection = DB_StationDayAQIData;
                mnKey = "StationCode";
            }
            String starttime = DataFormatUtil.getFirstDayOfMonth(monitortime);
            String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
            paramMap.put("collection", collection);
            paramMap.put("mnKey", mnKey);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<String> pollutantCodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
            paramMap.put("pollutantcodes", pollutantCodes);
            Map<String, Object> codeAndAvg = getCityOrStationDayPollutantDataByParam(paramMap);

            Map<String, Object> codeAndZS = getCityOrStationZSByParam(datatype, monitortime, dgimn);
            Double total = codeAndZS.get("total")!=null?Double.parseDouble(codeAndZS.get("total").toString()):0d;
            Double value;
            for (String codeIndex : pollutantCodes) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("code",codeIndex);
                resultMap.put("name",CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(codeIndex));
                resultMap.put("nd",codeAndAvg.get(codeIndex));
                value = codeAndZS.get(codeIndex)!=null?Double.parseDouble(codeAndZS.get(codeIndex).toString()):0D;
                resultMap.put("zs",value);
                if (total>0){
                    resultMap.put("fdl",DataFormatUtil.SaveOneAndSubZero(100d*value/total));
                }else {
                    resultMap.put("fdl","0");

                }
                resultList.add(resultMap);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> getCityOrStationZSByParam(String datatype, String monitortime, String dgimn) throws Exception {
        Map<String, Map<String, Object>> monthAndMap;
        Map<String, Object> nameAndValue;
        Map<String, Object> codeAndValue = new HashMap<>();
        if ("city".equals(datatype)) {
            Map<String, Object> map = new HashMap<>();
            map.put("dgimn", dgimn);
            map.put("monitortime", monitortime);
            monthAndMap = airCityMonthDataService.getMonthCompositeIndexByParam(map);
            nameAndValue = monthAndMap.get(monitortime);
        } else {
            monthAndMap = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(monitortime);
            nameAndValue = monthAndMap.get(dgimn);
        }
        if (nameAndValue != null) {
            String code;
            for (String name : nameAndValue.keySet()) {
                name = name.equals("PM25")?"PM2.5":name;
                code = CommonTypeEnum.AirCommonSixIndexEnum.getCodeByName(name);
                if (StringUtils.isNotBlank(code)){
                    codeAndValue.put(code, nameAndValue.get(name));
                }else {
                    codeAndValue.put("total", nameAndValue.get(name));
                }

            }
        }
        return codeAndValue;
    }

    private Map<String, Object> getCityOrStationDayPollutantDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> operations = new ArrayList<>();

        Date startDate = DataFormatUtil.getDateYMD(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMD(paramMap.get("endtime").toString());
        String mnKey = (String) paramMap.get("mnKey");
        String collection = (String) paramMap.get("collection");
        List<String> mns = (List<String>) paramMap.get("mns");

        Criteria criteria = Criteria.where("MonitorTime").gte(startDate).lte(endDate).and(mnKey).in(mns);
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("DataList"));
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        operations.add(match(Criteria.where("DataList.PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("MonitorTime")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("Strength"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        Map<String, Object> codeAndAvg = new HashMap<>();
        if (documents.size() > 0) {
            Map<String, List<Double>> codeAndValues = new HashMap<>();
            List<Double> values;
            String code;
            Double value;
            for (Document document : documents) {
                if (document.get("Strength") != null) {
                    value = Double.parseDouble(document.getString("Strength"));
                    code = document.getString("PollutantCode");
                    values = codeAndValues.get(code) != null ? codeAndValues.get(code) : new ArrayList<>();
                    values.add(value);
                    codeAndValues.put(code, values);
                }

            }
            for (String codeIndex : codeAndValues.keySet()) {
                values = codeAndValues.get(codeIndex);
                value = DataFormatUtil.getListAvgDValue(values);
                if (codeIndex.equals("a21005")) {//CO
                    codeAndAvg.put(codeIndex, DataFormatUtil.formatDoubleSaveOne(value));
                } else {
                    codeAndAvg.put(codeIndex, DataFormatUtil.formatDoubleSaveNo(value));
                }

            }
        }
        return codeAndAvg;

    }


}
