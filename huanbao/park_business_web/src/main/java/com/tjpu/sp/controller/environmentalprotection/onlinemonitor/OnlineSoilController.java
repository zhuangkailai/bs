package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.SoilPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;


/**
 * @author: xsm
 * @date: 2020/05/20 11:39
 * @Description: 在线土壤监测数据
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineSoil")
public class OnlineSoilController {

    @Autowired
    private SoilPointService soilPointService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 土壤监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.soilEnum.getCode();

    private final String soilMongoDBName = "DayData";

    //判断点位超标
    /*
     * 获取监测点位信息
     * 1、获取监测点位数据
     * 2、根据设置的标准判断点位的超标级别，其监测污染物最高的一个界别为 该点位的污染级别
     * 3、求比
     *  */

    /**
     * @author: xsm
     * @date: 2020/05/20 14:15
     * @Description: 统计各污染等级监测点位超标率
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPointOverRateForPolluteLevel", method = RequestMethod.POST)
    public Object countPointOverRateForPolluteLevel(@RequestJson(value = "monitoryear") String monitoryear
    ) {
        try {
            //土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
            Set<String> mns = soilPoints.stream().map(soilPoint -> soilPoint.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> pollutantCodes = soilPoints.stream().map(soilPoint -> soilPoint.get("PollutantCode").toString()).collect(Collectors.toSet());
            //根据MN号和时间获取监测点信息从mongdb中
            List<AggregationOperation> queryAggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns)
                    .and("DayDataList.PollutantCode").in(pollutantCodes)
                    .and("MonitorTime").gte(DataFormatUtil.parseDate(monitoryear + "-01-01 00:00:00")).lte(DataFormatUtil.parseDate(monitoryear + "-12-31 00:00:00"));
            queryAggregations.add(match(criteria));
            Aggregation aggregation = newAggregation(queryAggregations);
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, soilMongoDBName, Document.class);
            List<Document> mappedResults = aggregationResults.getMappedResults();
            //通过mn号分组点位信息
            Map<String, List<Map<String, Object>>> listMap = soilPoints.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            //判断mn号的污染级别 每个污染物的级别取最大的
            Map<String, Object> datamap = countSoilMonitorPointPolluteData(mapDocuments, listMap);
            int pointcount = Integer.parseInt(datamap.get("pointcount").toString());
            List<Map<String, Object>> objlist = (List<Map<String, Object>>) datamap.get("datalist");
            if (pointcount > 0) {
                //通过级别分组
                Map<String, List<Map<String, Object>>> levelMap = objlist.stream().collect(Collectors.groupingBy(m -> m.get("levelnum").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : levelMap.entrySet()) {
                    String level = entry.getKey();
                    String levelname = "";
                    if ("1".equals(level)) {
                        levelname = "normalvalue";
                    } else if ("2".equals(level)) {
                        levelname = "onelevel";
                    } else if ("3".equals(level)) {
                        levelname = "twolevel";
                    } else if ("4".equals(level)) {
                        levelname = "threelevel";
                    } else if ("5".equals(level)) {
                        levelname = "fourlevel";
                    }
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("levelname", levelname);
                    resultmap.put("total", pointcount);
                    resultmap.put("num", entry.getValue().size());
                    resultmap.put("ratio", DataFormatUtil.SaveOneAndSubZero((entry.getValue().size()) * 100d / pointcount) + "%");
                    result.add(resultmap);
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
     * @date: 2020/05/20 14:15
     * @Description: 统计各污染等级监测点位超标率
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPointOverRateForPolluteType", method = RequestMethod.POST)
    public Object countPointOverRateForPolluteType(@RequestJson(value = "monitoryear") String monitoryear
    ) {
        try {
            //土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
            Set<String> mns = soilPoints.stream().map(soilPoint -> soilPoint.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> pollutantCodes = soilPoints.stream().map(soilPoint -> soilPoint.get("PollutantCode").toString()).collect(Collectors.toSet());
            //根据MN号和时间获取监测点信息从mongdb中
            List<AggregationOperation> queryAggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns)
                    .and("DayDataList.PollutantCode").in(pollutantCodes)
                    .and("MonitorTime").gte(DataFormatUtil.parseDate(monitoryear + "-01-01 00:00:00")).lte(DataFormatUtil.parseDate(monitoryear + "-12-31 00:00:00"));
            queryAggregations.add(match(criteria));
            Aggregation aggregation = newAggregation(queryAggregations);
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, soilMongoDBName, Document.class);
            List<Document> mappedResults = aggregationResults.getMappedResults();
            //通过mn号分组点位信息
            Map<String, List<Map<String, Object>>> listMap = soilPoints.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            Map<String, Object> datamap = countSoilMonitorPointPolluteData(mapDocuments, listMap);
            int pointcount = Integer.parseInt(datamap.get("pointcount").toString());
            List<Map<String, Object>> objlist = (List<Map<String, Object>>) datamap.get("datalist");
            //判断mn号的污染级别 每个污染物的级别取最大的
            if (pointcount > 0) {
                //通过级别分组
                Map<String, List<Map<String, Object>>> levelMap = objlist.stream().collect(Collectors.groupingBy(m -> m.get("organicinorganic").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : levelMap.entrySet()) {
                    String organicinorganiccode = entry.getKey();
                    String name = "";
                    if ("1".equals(organicinorganiccode)) {
                        name = "有机型";
                    } else if ("2".equals(organicinorganiccode)) {
                        name = "无机型";
                    } else if ("3".equals(organicinorganiccode)) {
                        name = "复合型";
                    }
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("organicinorganic", name);
                    resultmap.put("total", pointcount);
                    resultmap.put("num", entry.getValue().size());
                    resultmap.put("ratio", DataFormatUtil.SaveOneAndSubZero((entry.getValue().size()) * 100d / pointcount) + "%");
                    result.add(resultmap);
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
     * @date: 2020/05/21 10:07
     * @Description: 根据监测时间统计土壤超标点位趋势分析（年）
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPointOverTrendAnalysisByMonitorTimes", method = RequestMethod.POST)
    public Object countPointOverTrendAnalysisByMonitorTimes(@RequestJson(value = "startyear") String startyear,
                                                            @RequestJson(value = "endyear") String endyear
    ) {
        try {
            //土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            Set<String> yearlist = new HashSet<>();
            yearlist.add(startyear);
            for (int i = Integer.parseInt(startyear); i < Integer.parseInt(endyear); i++) {
                yearlist.add(Integer.toString(i));
            }
            yearlist.add(endyear);
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
            Set<String> mns = soilPoints.stream().map(soilPoint -> soilPoint.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> pollutantCodes = soilPoints.stream().map(soilPoint -> soilPoint.get("PollutantCode").toString()).collect(Collectors.toSet());
            //根据MN号和时间获取监测点信息从mongdb中
            List<AggregationOperation> queryAggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns)
                    .and("DayDataList.PollutantCode").in(pollutantCodes)
                    .and("MonitorTime").gte(DataFormatUtil.parseDate(startyear + "-01-01 00:00:00")).lte(DataFormatUtil.parseDate(endyear + "-12-31 00:00:00"));
            queryAggregations.add(match(criteria));
            // 加8小时
            queryAggregations.add(Aggregation.project("DataGatherCode", "MonitorTime", "DayDataList").andExpression("add(MonitorTime,8 * 3600000)").as("date8"));
            queryAggregations.add(Aggregation.project("DataGatherCode", "DayDataList").andExpression("substr(date8,0,4)").as("theyear").andExclude("_id"));
            Aggregation aggregation = newAggregation(queryAggregations);
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, soilMongoDBName, Document.class);
            List<Document> mappedResults = aggregationResults.getMappedResults();
            //通过mn号分组点位信息
            Map<String, List<Map<String, Object>>> listMap = soilPoints.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            //通过年份分组数据
            Map<String, List<Document>> dateDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("theyear").toString()));
            List<String> levellist = Arrays.asList("onelevel", "twolevel", "threelevel", "fourlevel");
            for (String year : yearlist) {
                List<Document> yeardocument = dateDocuments.get(year);
                Map<String, Object> result = new HashMap<>();
                result.put("yeartime", year);
                Map<String, Object> resultmap = new HashMap<>();
                for (String str : levellist) {
                    resultmap.put(str, 0);
                }
                resultmap.put("total", 0);
                if (yeardocument != null) {
                    //通过mn号分组数据
                    Map<String, List<Document>> mapDocuments = yeardocument.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                    Map<String, Object> datamap = countSoilMonitorPointPolluteData(mapDocuments, listMap);
                    int pointcount = Integer.parseInt(datamap.get("pointcount").toString());
                    List<Map<String, Object>> objlist = (List<Map<String, Object>>) datamap.get("datalist");
                    int normalnum = 0;
                    if (pointcount > 0) {
                        //通过级别分组
                        Map<String, List<Map<String, Object>>> levelMap = objlist.stream().collect(Collectors.groupingBy(m -> m.get("levelnum").toString()));
                        for (Map.Entry<String, List<Map<String, Object>>> entry : levelMap.entrySet()) {
                            String level = entry.getKey();
                            String levelname = "";
                            if ("1".equals(level)) {
                                normalnum = entry.getValue().size();
                            } else if ("2".equals(level)) {
                                levelname = "onelevel";
                            } else if ("3".equals(level)) {
                                levelname = "twolevel";
                            } else if ("4".equals(level)) {
                                levelname = "threelevel";
                            } else if ("5".equals(level)) {
                                levelname = "fourlevel";
                            }
                            if (!"".equals(levelname)) {
                                resultmap.put(levelname, entry.getValue().size());
                            }
                        }
                    }
                    resultmap.put("total", pointcount - normalnum);
                }
                result.put("valuedata", resultmap);
                resultlist.add(result);
            }
            //排序
            if (resultlist != null && resultlist.size() > 0) {
                Comparator<Object> orderbytime = Comparator.comparing(m -> ((Map) m).get("yeartime").toString());
                resultlist = resultlist.stream().sorted(orderbytime).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/05/21 1:33
     * @Description: 根据污染类型统计当前年有机或无机物的的超标情况
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutantOverRateByYearAndDataType", method = RequestMethod.POST)
    public Object countPollutantOverRateByYearAndDataType(@RequestJson(value = "monitoryear") String monitoryear,
                                                          @RequestJson(value = "datatype") Integer datatype
    ) {
        try {
            //土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("organicinorganic", datatype);
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
            Set<String> mns = soilPoints.stream().map(soilPoint -> soilPoint.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> pollutantCodes = soilPoints.stream().map(soilPoint -> soilPoint.get("PollutantCode").toString()).collect(Collectors.toSet());
            //根据MN号和时间获取监测点信息从mongdb中
            List<AggregationOperation> queryAggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns)
                    .and("DayDataList.PollutantCode").in(pollutantCodes)
                    .and("MonitorTime").gte(DataFormatUtil.parseDate(monitoryear + "-01-01 00:00:00")).lte(DataFormatUtil.parseDate(monitoryear + "-12-31 00:00:00"));
            queryAggregations.add(match(criteria));
            Aggregation aggregation = newAggregation(queryAggregations);
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, soilMongoDBName, Document.class);
            List<Document> mappedResults = aggregationResults.getMappedResults();
            //通过mn号分组点位信息
            Map<String, List<Map<String, Object>>> listMap = soilPoints.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            Map<String, Object> datamap = countSoilMonitorPointPolluteData(mapDocuments, listMap);
            List<String> levellist = Arrays.asList("onelevel", "twolevel", "threelevel", "fourlevel");
            int totalnum = 0;//Integer.parseInt(datamap.get("pointcount").toString());
            List<Map<String, Object>> objlist = (List<Map<String, Object>>) datamap.get("datalist");
            List<Map<String, Object>> organicinorganiclist = new ArrayList<>();
            //根据有机无机类型筛选数据
            for (Map<String, Object> obj : objlist) {
                if (obj.get("organicinorganic") != null && datatype == (Integer.parseInt(obj.get("organicinorganic").toString()))) {
                    totalnum += 1;
                    organicinorganiclist.add(obj);
                }
            }
            if (totalnum > 0) {
                //通过级别分组
                Map<String, List<Map<String, Object>>> pollutantcodeMap = organicinorganiclist.stream().collect(Collectors.groupingBy(m -> m.get("code").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : pollutantcodeMap.entrySet()) {
                    Map<String, Object> pollutant = new HashMap<>();
                    String pollutantcode = entry.getKey();
                    Object name = "";
                    List<Map<String, Object>> onelist = entry.getValue();
                    int overnum = 0;
                    for (String level : levellist) {
                        int levelnum = 0;
                        for (Map<String, Object> levelmap : onelist) {
                            name = levelmap.get("name");
                            String levelname = "";
                            if ("2".equals(levelmap.get("levelnum").toString())) {
                                levelname = "onelevel";
                            } else if ("3".equals(levelmap.get("levelnum").toString())) {
                                levelname = "twolevel";
                            } else if ("4".equals(levelmap.get("levelnum").toString())) {
                                levelname = "threelevel";
                            } else if ("5".equals(levelmap.get("levelnum").toString())) {
                                levelname = "fourlevel";
                            }
                            if (level.equals(levelname)) {
                                levelnum += 1;
                                overnum += 1;
                            }
                        }
                        pollutant.put(level, levelnum > 0 ? DataFormatUtil.SaveOneAndSubZero((Double.valueOf(levelnum) / totalnum) * 100) : "-");
                    }
                    pollutant.put("pointover", overnum > 0 ? DataFormatUtil.SaveOneAndSubZero((Double.valueOf(overnum) / totalnum) * 100) : "-");
                    pollutant.put("code", pollutantcode);
                    pollutant.put("name", name);
                    result.add(pollutant);
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
     * @date: 2020/05/21 4:42
     * @Description: 根据土壤类型和时间段统计各类型土壤监测点超标情况
     * @param:
     * @return:
     */
    @RequestMapping(value = "countSoilOverRateGroupBySoilPointTypeByParam", method = RequestMethod.POST)
    public Object countSoilOverRateGroupBySoilPointTypeByParam(@RequestJson(value = "starttime") String starttime,
                                                               @RequestJson(value = "endtime") String endtime,
                                                               @RequestJson(value = "fksoilpointtypecodes", required = false) List<String> fksoilpointtypecodes
    ) {
        try {
            //土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fksoilpointtypecodes", fksoilpointtypecodes);
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
            Set<String> mns = soilPoints.stream().map(soilPoint -> soilPoint.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> pollutantCodes = soilPoints.stream().map(soilPoint -> soilPoint.get("PollutantCode").toString()).collect(Collectors.toSet());
            //根据MN号和时间获取监测点信息从mongdb中
            List<AggregationOperation> queryAggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns)
                    .and("DayDataList.PollutantCode").in(pollutantCodes)
                    .and("MonitorTime").gte(DataFormatUtil.parseDate(starttime + "-01 00:00:00")).lte(DataFormatUtil.parseDate(endtime + "-31 00:00:00"));
            queryAggregations.add(match(criteria));
            Aggregation aggregation = newAggregation(queryAggregations);
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, soilMongoDBName, Document.class);
            List<Document> mappedResults = aggregationResults.getMappedResults();
            //通过mn号分组点位信息
            Map<String, List<Map<String, Object>>> listMap = soilPoints.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            Map<String, Object> datamap = countSoilMonitorPointPolluteData(mapDocuments, listMap);
            List<String> levellist = Arrays.asList("onelevel", "twolevel", "threelevel", "fourlevel");
            int totalnum = Integer.parseInt(datamap.get("pointcount").toString());//点位总条数
            List<Map<String, Object>> objlist = (List<Map<String, Object>>) datamap.get("datalist");//数据
            if (totalnum > 0) {
                //通过级别分组
                Map<String, List<Map<String, Object>>> pointtypecodeMap = objlist.stream().collect(Collectors.groupingBy(m -> m.get("soilpointtypecode").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : pointtypecodeMap.entrySet()) {
                    Map<String, Object> soilpoints = new HashMap<>();
                    String soilpointtypecode = entry.getKey();
                    Object soilpointtypename = "";
                    List<Map<String, Object>> onelist = entry.getValue();
                    int overnum = 0;
                    Set<String> pollutantnames = new HashSet<>();
                    for (String level : levellist) {
                        int levelnum = 0;
                        for (Map<String, Object> levelmap : onelist) {
                            soilpointtypename = levelmap.get("soilpointtypename");
                            String levelname = "";
                            if ("2".equals(levelmap.get("levelnum").toString())) {
                                levelname = "onelevel";
                            } else if ("3".equals(levelmap.get("levelnum").toString())) {
                                levelname = "twolevel";
                            } else if ("4".equals(levelmap.get("levelnum").toString())) {
                                levelname = "threelevel";
                            } else if ("5".equals(levelmap.get("levelnum").toString())) {
                                levelname = "fourlevel";
                            }
                            if (level.equals(levelname)) {
                                pollutantnames.add(levelmap.get("name").toString());
                                levelnum += 1;
                                overnum += 1;
                            }
                        }
                        soilpoints.put(level, levelnum > 0 ? DataFormatUtil.SaveOneAndSubZero((Double.valueOf(levelnum) / totalnum) * 100) : "-");
                    }
                    String pollutants = "";
                    if (pollutantnames.size() > 0) {
                        for (String code : pollutantnames) {
                            pollutants = pollutants + code + "、";
                        }
                        if (StringUtils.isNotBlank(pollutants)) {
                            pollutants = pollutants.substring(0, pollutants.length() - 1);
                        }
                    }
                    soilpoints.put("pollutants", pollutants);
                    soilpoints.put("pointover", overnum > 0 ? DataFormatUtil.SaveOneAndSubZero((Double.valueOf(overnum) / totalnum) * 100) : "-");
                    soilpoints.put("soilpointtypecode", soilpointtypecode);
                    soilpoints.put("soilpointtypename", soilpointtypename);
                    result.add(soilpoints);
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
     * @date: 2020/05/21 4:42
     * @Description: 根据土壤类型和时间段统计各类型土壤监测点超标情况
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllSoilPointTypeOverRateByParam", method = RequestMethod.POST)
    public Object countAllSoilPointTypeOverRateByParam(@RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "endtime") String endtime,
                                                       @RequestJson(value = "fksoilpointtypecodes", required = false) List<String> fksoilpointtypecodes
    ) {
        try {
            //土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fksoilpointtypecodes", fksoilpointtypecodes);
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
            Set<String> mns = soilPoints.stream().map(soilPoint -> soilPoint.get("DGIMN").toString()).collect(Collectors.toSet());
            Set<String> pollutantCodes = soilPoints.stream().map(soilPoint -> soilPoint.get("PollutantCode").toString()).collect(Collectors.toSet());
            //根据MN号和时间获取监测点信息从mongdb中
            List<AggregationOperation> queryAggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns)
                    .and("DayDataList.PollutantCode").in(pollutantCodes)
                    .and("MonitorTime").gte(DataFormatUtil.parseDate(starttime + "-01 00:00:00")).lte(DataFormatUtil.parseDate(endtime + "-31 00:00:00"));
            queryAggregations.add(match(criteria));
            Aggregation aggregation = newAggregation(queryAggregations);
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, soilMongoDBName, Document.class);
            List<Document> mappedResults = aggregationResults.getMappedResults();
            //通过mn号分组点位信息
            Map<String, List<Map<String, Object>>> listMap = soilPoints.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            Map<String, Object> datamap = countSoilMonitorPointPolluteData(mapDocuments, listMap);
            int totalnum = Integer.parseInt(datamap.get("pointcount").toString());//点位总条数
            List<Map<String, Object>> objlist = (List<Map<String, Object>>) datamap.get("datalist");//数据
            if (totalnum > 0) {
                //通过级别分组
                Map<String, List<Map<String, Object>>> pointtypecodeMap = objlist.stream().collect(Collectors.groupingBy(m -> m.get("soilpointtypecode").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : pointtypecodeMap.entrySet()) {
                    Map<String, Object> soilpoints = new HashMap<>();
                    String soilpointtypecode = entry.getKey();
                    Object soilpointtypename = "";
                    List<Map<String, Object>> onelist = entry.getValue();
                    int overnum = 0;
                    for (Map<String, Object> levelmap : onelist) {
                        soilpointtypename = levelmap.get("soilpointtypename");
                        String levelname = "";
                        if ("2".equals(levelmap.get("levelnum").toString())) {
                            levelname = "onelevel";
                        } else if ("3".equals(levelmap.get("levelnum").toString())) {
                            levelname = "twolevel";
                        } else if ("4".equals(levelmap.get("levelnum").toString())) {
                            levelname = "threelevel";
                        } else if ("5".equals(levelmap.get("levelnum").toString())) {
                            levelname = "fourlevel";
                        }
                        if (!"".equals(levelname)) {
                            overnum += 1;
                        }
                    }
                    soilpoints.put("overnum", overnum);
                    soilpoints.put("pointover", overnum > 0 ? DataFormatUtil.SaveOneAndSubZero((Double.valueOf(overnum) / totalnum) * 100) : "-");
                    soilpoints.put("soilpointtypecode", soilpointtypecode);
                    soilpoints.put("soilpointtypename", soilpointtypename);
                    result.add(soilpoints);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> countSoilMonitorPointPolluteData(Map<String, List<Document>> mapDocuments, Map<String, List<Map<String, Object>>> listMap) {
        Map<String, Object> datamap = new HashMap<>();
        int pointcount = 0;
        List<Map<String, Object>> objlist = new ArrayList<>();
        for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
            String mn = entry.getKey();    //mn
            List<Document> valuedata = entry.getValue();
            int levelnum = 0;
            Object organicinorganic = null;
            Object code = "";
            Object name = "";
            Object soilpointtypecode = "";
            Object soilpointtypename = "";
            if (valuedata.size() > 0) {
                for (Document document : valuedata) {
                    List<Map<String, Object>> pollutantDataList = document.get("DayDataList", List.class);
                    for (Map<String, Object> pollutantmap : pollutantDataList) {
                        String pollutantcode = pollutantmap.get("PollutantCode") != null ? pollutantmap.get("PollutantCode").toString() : "";
                        if (pollutantmap.get("AvgStrength") != null && !"".equals(pollutantmap.get("AvgStrength").toString())) {
                            double value = Double.parseDouble(pollutantmap.get("AvgStrength").toString());
                            List<Map<String, Object>> standlist = listMap.get(mn);
                            if (standlist != null && standlist.size() > 0) {
                                for (Map<String, Object> standmap : standlist) {
                                    if (pollutantcode.equals(standmap.get("PollutantCode").toString())) {
                                        Object onelevel = standmap.get("OneLevel");
                                        Object twolevel = standmap.get("TwoLevel");
                                        Object threelevel = standmap.get("ThreeLevel");
                                        Object fourlevel = standmap.get("FourLevel");
                                        int thislevel = 0;
                                        if (onelevel != null && value < Double.parseDouble(onelevel.toString())) {
                                            thislevel = 1;
                                        } else if (onelevel != null && twolevel != null && value >= Double.parseDouble(onelevel.toString()) && value < Double.parseDouble(twolevel.toString())) {
                                            thislevel = 2;
                                        } else if (twolevel != null && threelevel != null && value >= Double.parseDouble(twolevel.toString()) && value < Double.parseDouble(threelevel.toString())) {
                                            thislevel = 3;
                                        } else if (threelevel != null && fourlevel != null && value >= Double.parseDouble(threelevel.toString()) && value < Double.parseDouble(fourlevel.toString())) {
                                            thislevel = 4;
                                        } else if (fourlevel != null && value >= Double.parseDouble(fourlevel.toString())) {
                                            thislevel = 5;
                                        }
                                        if (levelnum < thislevel) {
                                            levelnum = thislevel;
                                            organicinorganic = standmap.get("organicinorganic");
                                            code = standmap.get("PollutantCode");
                                            name = standmap.get("PollutantName");
                                            soilpointtypecode = standmap.get("SoilPointTypeCode");
                                            soilpointtypename = standmap.get("SoilPointTypeName");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (levelnum > 0) {
                Map<String, Object> objmap = new HashMap<>();
                objmap.put("organicinorganic", organicinorganic);
                objmap.put("levelnum", levelnum);
                objmap.put("code", code);
                objmap.put("name", name);
                objmap.put("dgimn", mn);
                objmap.put("soilpointtypecode", soilpointtypecode);
                objmap.put("soilpointtypename", soilpointtypename);
                objlist.add(objmap);
                pointcount += 1;
            }
        }
        datamap.put("pointcount", pointcount);
        datamap.put("datalist", objlist);
        return datamap;
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询土壤相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getSoilRelationListDataByParams", method = RequestMethod.POST)
    public Object getSoilRelationListDataByParams(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputpollutant") String outputpollutant,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum

    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String collection = soilMongoDBName;
            List<Map<String, Object>> outPuts;
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
                case WasteGasEnum:
                    paramMap.put("monitorpointtype",CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
                case WasteWaterEnum:
                    paramMap.put("outputtype","water");
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    break;
                default:
                    outPuts = null;
                    break;
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (outPuts != null) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.put("collection", collection);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("monitorpointid", monitorpointid);
                paramMap.put("monitorpointpollutant", monitorpointpollutant);
                paramMap.put("outputpollutant", outputpollutant);
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                resultMap = soilPointService.getRelationListDataByParamMap(outPuts, paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询土壤相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportSoilRelationListDataByParams", method = RequestMethod.POST)
    public void exportSoilRelationListDataByParams(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputpollutant") String outputpollutant,
            HttpServletResponse response,
            HttpServletRequest request

    ) throws IOException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String collection = soilMongoDBName;
            List<Map<String, Object>> outPuts;
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
                case WasteGasEnum:
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
                case WasteWaterEnum:
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    break;
                default:
                    outPuts = null;
                    break;
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (outPuts != null) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.put("collection", collection);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("monitorpointid", monitorpointid);
                paramMap.put("monitorpointpollutant", monitorpointpollutant);
                paramMap.put("outputpollutant", outputpollutant);
                resultMap = soilPointService.getRelationListDataByParamMap(outPuts, paramMap);

                List<Map<String, Object>> tablelistdata = resultMap.get("tablelistdata") != null ? (List<Map<String, Object>>) resultMap.get("tablelistdata") : null;
                //设置导出文件数据格式
                List<String> headers = Arrays.asList("企业名称", "排口名称","相关度");
                List<String> headersField = Arrays.asList("pollutionname", "outputname","relationpercent");
                //设置文件名称
                String fileName = "相关性分析导出列表文件_" + new Date().getTime();
                ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废气相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getSoilRelationChartDataByParams", method = RequestMethod.POST)
    public Object getSoilRelationChartDataByParams(
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputpollutant") String outputpollutant,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", Arrays.asList(outputid));
            String collection = soilMongoDBName;
            List<Map<String, Object>> outPuts;
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtypecode)) {
                case WasteGasEnum:
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
                case WasteWaterEnum:
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    break;
                default:
                    outPuts = null;
                    break;
            }
            if (outPuts != null) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.put("collection", collection);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("monitorpointid", monitorpointid);
                paramMap.put("monitorpointpollutant", monitorpointpollutant);
                paramMap.put("outputpollutant", outputpollutant);
                if (pagesize != null && pagenum != null) {
                    paramMap.put("pagesize", pagesize);
                    paramMap.put("pagenum", pagenum);
                }
                resultMap = soilPointService.getSoilRelationChartDataByParamMap(outPuts, paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
