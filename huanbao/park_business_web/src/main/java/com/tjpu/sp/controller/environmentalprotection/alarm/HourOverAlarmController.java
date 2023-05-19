package com.tjpu.sp.controller.environmentalprotection.alarm;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import net.sf.json.JSONObject;
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

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;


/**
 * @Description: 小时报警数据
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/9/8 16:10
 */
@RestController
@RequestMapping("hourOverAlarmData")
public class HourOverAlarmController {
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private OnlineMonitorService onlineMonitorService;
    private String overDataCollect = "OverData";
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;


    /**
     * @Description: 获取小时超标次数统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/8 16:12
     */
    @RequestMapping(value = "/getHourOverNumData", method = RequestMethod.POST)
    public Object getHourAlarmNumData(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime + ":00:00");
            paramMap.put("endtime", endtime + ":59:59");

            List<Document> documents = countOverHourNumDataByParam(paramMap);
            if (documents.size() > 0) {
                List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);
                hours.add(endtime);
                Map<String, Integer> timeAndNum = new HashMap<>();
                for (Document document : documents) {
                    timeAndNum.put(document.getString("_id"), document.getInteger("countnum"));
                }
                String hh;
                for (String hour : hours) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitortime", DataFormatUtil.FormatDateOneToOther(hour, "yyyy-MM-dd HH", "H时"));
                    hh = DataFormatUtil.FormatDateOneToOther(hour, "yyyy-MM-dd HH", "HH");
                    resultMap.put("countnum", timeAndNum.get(hh) != null ? timeAndNum.get(hh) : 0);
                    resultList.add(resultMap);
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Document> countOverHourNumDataByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("OverTime").gte(startTime).lte(endTime)
                .and("DataType").is("RealTimeData");
        aggregations.add(match(criteria));
        aggregations.add(Aggregation.project("OverTime")
                .and(DateOperators.DateToString.dateOf("OverTime").toString("%H")
                        .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("HourTime")
        );
        GroupOperation groupOperation = group("HourTime").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = Aggregation.newAggregation(aggregations).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, overDataCollect, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;

    }

    /**
     * @Description: 获取小时超标次数统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/8 16:12
     */
    @RequestMapping(value = "/getHourOverDetailData", method = RequestMethod.POST)
    public Object getHourOverDetailData(
            @RequestJson(value = "paramjson") Object paramJson) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(paramJson);
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> outputList = new ArrayList<>();
            List<Map<String, Object>> standardList = new ArrayList<>();
            List<Integer> types;
            if (jsonObject.get("monitorpointtypes")!=null){
                types = jsonObject.getJSONArray("monitorpointtypes");
                if (types.size()==0){
                    types = Arrays.asList(WasteWaterEnum.getCode(),RainEnum.getCode(),
                            WasteGasEnum.getCode(),SmokeEnum.getCode(),
                            FactoryBoundaryStinkEnum.getCode(),EnvironmentalStinkEnum.getCode(),
                            EnvironmentalVocEnum.getCode(),
                            WaterQualityEnum.getCode(),AirEnum.getCode(),
                            MicroStationEnum.getCode(),EnvironmentalDustEnum.getCode());
                }
            }else {
                types = Arrays.asList(WasteWaterEnum.getCode(),RainEnum.getCode(),
                        WasteGasEnum.getCode(),SmokeEnum.getCode(),
                        FactoryBoundaryStinkEnum.getCode(),EnvironmentalStinkEnum.getCode(),
                        EnvironmentalVocEnum.getCode(),
                        WaterQualityEnum.getCode(),AirEnum.getCode(),
                        MicroStationEnum.getCode(),EnvironmentalDustEnum.getCode());
            }
            for (Integer type : types) {
                paramMap.put("monitorPointType", type);
                paramMap.put("pollutionname", jsonObject.get("pollutionname"));
                outputList.addAll(onlineMonitorService.getOnlineOutPutListByParamMap(paramMap));
                paramMap.put("monitorpointtype", type);
                standardList.addAll(pollutantService.getPollutantStandardValueDataByParam(paramMap));
            }
            if (outputList.size() > 0) {
                Map<String, Object> mnAndId = new HashMap<>();
                Map<String, Object> mnAndName = new HashMap<>();
                Map<String, Object> mnAndPId = new HashMap<>();
                Map<String, Object> mnAndPName = new HashMap<>();
                Map<String, Integer> mnAndType = new HashMap<>();
                List<String> mns = new ArrayList<>();
                String mnCommon;
                for (Map<String, Object> output : outputList) {
                    if (output.get("dgimn") != null && output.get("monitorpointtype") != null) {
                        mnCommon = output.get("dgimn").toString();
                        mnAndId.put(mnCommon, output.get("monitorpointid"));
                        mnAndName.put(mnCommon, output.get("monitorpointname"));
                        mnAndPId.put(mnCommon, output.get("fk_pollutionid"));
                        mnAndPName.put(mnCommon, output.get("pollutionname"));
                        mnAndType.put(mnCommon, Integer.parseInt(output.get("monitorpointtype").toString()));
                        mns.add(mnCommon);
                    }
                }

                Map<String, Map<String, Object>> mnAndCodeAndName = new HashMap<>();
                Map<String, Map<String, Object>> mnAndCodeAndStandard = new HashMap<>();
                Map<String, Object> codeAndName;
                Map<String, Object> codeAndStandard;
                Object standardValue;
                for (Map<String, Object> standard : standardList) {
                    if (standard.get("DGIMN") != null && standard.get("Code") != null) {
                        mnCommon = standard.get("DGIMN").toString();
                        if (mnAndCodeAndName.containsKey(mnCommon)) {
                            codeAndName = mnAndCodeAndName.get(mnCommon);
                        } else {
                            codeAndName = new HashMap<>();
                        }
                        codeAndName.put(standard.get("Code").toString(), standard.get("pollutantname"));
                        mnAndCodeAndName.put(mnCommon, codeAndName);
                        if (mnAndCodeAndStandard.containsKey(mnCommon)) {
                            codeAndStandard = mnAndCodeAndStandard.get(mnCommon);
                        } else {
                            codeAndStandard = new HashMap<>();
                        }
                        if (standard.get("alarmtype")!=null){
                            switch (CommonTypeEnum.AlarmTypeEnum.getCodeByString(standard.get("alarmtype") + "")) {
                                case UpperAlarmEnum:
                                    standardValue = standard.get("StandardMaxValue");
                                    break;
                                case LowerAlarmEnum:
                                    standardValue = standard.get("StandardMinValue");
                                    break;
                                case BetweenAlarmEnum:
                                    if (standard.get("StandardMaxValue") != null && standard.get("StandardMinValue") != null) {
                                        standardValue = standard.get("StandardMinValue") + "-" + standard.get("StandardMaxValue");
                                    }else {
                                        standardValue = "";
                                    }

                                    break;
                                default:
                                    standardValue = "";
                                    break;
                            }
                            codeAndStandard.put(standard.get("Code").toString(),standardValue);
                            mnAndCodeAndStandard.put(mnCommon, codeAndStandard);
                        }

                    }
                }
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", jsonObject.get("starttime") + ":00:00");
                paramMap.put("endtime", jsonObject.get("endtime") + ":59:59");
                paramMap.put("pagesize",jsonObject.get("pagesize"));
                paramMap.put("pagenum",jsonObject.get("pagenum"));

                PageEntity<Document> pageEntity = getOverPageData(paramMap);
                List<Document> documents = pageEntity.getListItems();
                List<Map<String, Object>> dataList = new ArrayList<>();
                if (documents.size() > 0) {
                    String pollutantcode;
                    for (Document document : documents) {
                        Map<String, Object> dataMap = new HashMap<>();
                        mnCommon = document.getString("DataGatherCode");
                        dataMap.put("monitorpointtypecode", mnAndType.get(mnCommon));
                        dataMap.put("monitorpointtypename", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnCommon)));
                        dataMap.put("pollutionid", mnAndPId.get(mnCommon));
                        dataMap.put("pollutionname", mnAndPName.get(mnCommon));
                        dataMap.put("monitorpointid", mnAndId.get(mnCommon));
                        dataMap.put("monitorpointname", mnAndName.get(mnCommon));
                        dataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
                        pollutantcode = document.getString("PollutantCode");
                        dataMap.put("pollutantcode", pollutantcode);
                        codeAndName = mnAndCodeAndName.get(mnCommon);
                        if (codeAndName!=null){
                            dataMap.put("pollutantname",codeAndName.get(pollutantcode) );
                        }else {
                            dataMap.put("pollutantname","" );
                        }

                        dataMap.put("monitorvalue", document.get("MonitorValue"));
                        codeAndStandard = mnAndCodeAndStandard.get(mnCommon);
                        if (codeAndStandard!=null){
                            dataMap.put("standardvalue",codeAndStandard.get(pollutantcode) );
                        }else {
                            dataMap.put("standardvalue","" );
                        }
                        if (document.get("OverMultiple")!=null){
                            dataMap.put("overmultiple",DataFormatUtil.SaveTwoAndSubZero( document.getDouble("OverMultiple")));
                        }else {
                            dataMap.put("overmultiple","");
                        }
                        dataList.add(dataMap);
                    }
                }
                resultMap.put("total", pageEntity.getTotalCount());
                resultMap.put("dataList", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private PageEntity<Document> getOverPageData(Map<String, Object> paramMap) {
        int pageNum = 1;
        int pageSize = Integer.MAX_VALUE;
        boolean isPage = true;
        if (paramMap.get("pagenum") != null) {
            pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
        } else {
            isPage = false;
        }
        if (paramMap.get("pagesize") != null) {
            pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
        } else {
            isPage = false;
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        pageEntity.setPageNum(pageNum);
        pageEntity.setPageSize(pageSize);
        List<AggregationOperation> operations = new ArrayList<>();

        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        operations.add(Aggregation.match(Criteria.where("OverTime").gte(startDate).lte(endDate)));
        List<String> mns = (List<String>) paramMap.get("mns");
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        operations.add(Aggregation.match(Criteria.where("DataType").is("RealTimeData")));
        //排序条件
        String orderBy = "OverTime";
        Sort.Direction direction = Sort.Direction.DESC;
        if (paramMap.get("direction") != null && "asc".equals(paramMap.get("direction"))) {
            direction = Sort.Direction.ASC;
        }
        if (isPage) {
            long totalCount = 0;
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, overDataCollect, Document.class);
            totalCount = resultsCount.getMappedResults().size();
            pageEntity.setTotalCount(totalCount);
            int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
            pageEntity.setPageCount(pageCount);
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, overDataCollect, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;
    }

}
