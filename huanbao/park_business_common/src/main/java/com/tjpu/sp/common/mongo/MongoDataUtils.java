package com.tjpu.sp.common.mongo;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.FormatUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum;

/**
 * @author: lip
 * @date: 2019/6/24 0024 下午 3:04
 * @Description: 对mongodb数据处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class MongoDataUtils {


    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 组装多站点多污染物图表监测数据（实时数据，分钟数据，小时数据，日数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyOutPutManyPollutantsCharDataList(List<Document> documents,
                                                                                    List<String> pollutantcodes,
                                                                                    String collection,
                                                                                    Map<String, String> outPutIdAndMn,
                                                                                    List<String> outputids,
                                                                                    Map<String, String> idAndName,
                                                                                    Map<String, String> codeAndName) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        } else if ("MonthData".equals(collection)) {
            pollutantDataKey = "MonthDataList";
            valueKey = "AvgStrength";
        }
        //数采仪mn号
        String mnCode = "";
        //污染物编码
        String pollutantCode = "";
        //数采仪MN号+编码
        String mnPollutantCode = "";
        //key:数采仪MN号+编码,value:list(monitortime+monitorvalue)
        Map<String, List<Map<String, Object>>> tempMap = new HashMap<>();
        List<Map<String, Object>> tempList;
        for (Document document : documents) {
            mnCode = document.getString("DataGatherCode");
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                if (pollutantcodes.contains(pollutantCode) && !"".equals(pollutantCode)) {
                    mnPollutantCode = mnCode + "," + pollutantCode;
                    if (tempMap.get(mnPollutantCode) != null) {
                        tempList = tempMap.get(mnPollutantCode);
                    } else {
                        tempList = new ArrayList<>();
                    }
                    Map<String, Object> map = new HashMap<>();
                    String MonitorTime = "";
                    if ("RealTimeData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                    } else if ("MinuteData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                    } else if ("HourData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    } else if ("DayData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    } else if ("MonthData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    }
                    map.put("monitortime", MonitorTime);
                    map.put("monitorvalue", pollutant.get(valueKey));
                    tempList.add(map);
                    tempMap.put(mnPollutantCode, tempList);
                }
            }
        }
        List<Map<String, Object>> monitorDataList;
        for (String outputid : idAndName.keySet()) {
            for (String tempCode : pollutantcodes) {
                mnPollutantCode = outPutIdAndMn.get(outputid) + "," + tempCode;
                monitorDataList = tempMap.get(mnPollutantCode);
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("outputid", outputid);
                pollutantMap.put("dataname", idAndName.get(outputid) + "-" + codeAndName.get(tempCode));
                pollutantMap.put("pollutantcode", tempCode);
                pollutantMap.put("monitorDataList", monitorDataList);

                if (monitorDataList != null && monitorDataList.size() > 0) {
                    dataList.add(pollutantMap);
                }

            }
        }

        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 组装多站点多污染物图表监测数据（实时数据，分钟数据，小时数据，日数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManySomkeOutPutManyPollutantsCharDataList(List<Document> documents,
                                                                                         List<Map<String, Object>> pollutants,
                                                                                         String collection,
                                                                                         Map<String, String> outPutIdAndMn,
                                                                                         Map<String, String> idAndName,
                                                                                         Map<String, String> codeAndName) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        } else if ("MonthData".equals(collection)) {
            pollutantDataKey = "MonthDataList";
            valueKey = "AvgStrength";
        }

        String pollutantCode = "";
        Integer IsHasConvertData;
        Map<String, Integer> codeAndIs = new HashMap<>();
        for (Map<String, Object> pollutant : pollutants) {
            pollutantCode = pollutant.get("code") + "";
            IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
            codeAndIs.put(pollutantCode, IsHasConvertData);
        }


        //数采仪mn号
        String mnCode = "";
        //污染物编码

        //数采仪MN号+编码
        String mnPollutantCode = "";
        //key:数采仪MN号+编码,value:list(monitortime+monitorvalue)
        Map<String, List<Map<String, Object>>> tempMap = new HashMap<>();
        List<Map<String, Object>> tempList;
        for (Document document : documents) {
            mnCode = document.getString("DataGatherCode");
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                if (codeAndIs.containsKey(pollutantCode)) {
                    valueKey = getValueKey(collection, codeAndIs.get(pollutantCode));
                    mnPollutantCode = mnCode + "," + pollutantCode;
                    if (tempMap.get(mnPollutantCode) != null) {
                        tempList = tempMap.get(mnPollutantCode);
                    } else {
                        tempList = new ArrayList<>();
                    }
                    Map<String, Object> map = new HashMap<>();
                    String MonitorTime = "";
                    if ("RealTimeData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                    } else if ("MinuteData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                    } else if ("HourData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    } else if ("DayData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    }
                    map.put("monitortime", MonitorTime);
                    map.put("monitorvalue", pollutant.get(valueKey));
                    map.put("IsException", pollutant.get("IsException"));
                    map.put("IsOverStandard", pollutant.get("IsOverStandard"));
                    map.put("IsSuddenChange", pollutant.get("IsSuddenChange"));
                    map.put("waterlevel", pollutant.get("WaterLevel"));
                    tempList.add(map);
                    tempMap.put(mnPollutantCode, tempList);
                }
            }
        }
        //添加水质类别
        if (codeAndName.containsKey("waterquality")) {
            codeAndIs.put("waterquality", 0);
            for (Document document : documents) {
                mnCode = document.getString("DataGatherCode");
                pollutantCode = "waterquality";
                mnPollutantCode = mnCode + "," + pollutantCode;
                if (tempMap.get(mnPollutantCode) != null) {
                    tempList = tempMap.get(mnPollutantCode);
                } else {
                    tempList = new ArrayList<>();
                }
                Map<String, Object> map = new HashMap<>();
                String MonitorTime = "";
                if ("RealTimeData".equals(collection)) {
                    MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                } else if ("MinuteData".equals(collection)) {
                    MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                } else if ("HourData".equals(collection)) {
                    MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("DayData".equals(collection)) {
                    MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                map.put("monitortime", MonitorTime);
                map.put("monitorvalue", document.get("WaterLevel") + "类");
                tempList.add(map);
                tempMap.put(mnPollutantCode, tempList);
            }
        }


        List<Map<String, Object>> monitorDataList;
        for (String outputid : idAndName.keySet()) {
            for (String tempCode : codeAndIs.keySet()) {
                mnPollutantCode = outPutIdAndMn.get(outputid) + "," + tempCode;
                monitorDataList = tempMap.get(mnPollutantCode);
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("outputid", outputid);
                pollutantMap.put("dataname", idAndName.get(outputid) + "-" + codeAndName.get(tempCode));
                pollutantMap.put("pollutantcode", tempCode);
                pollutantMap.put("monitorDataList", monitorDataList);

                if (monitorDataList != null && monitorDataList.size() > 0) {
                    dataList.add(pollutantMap);
                }

            }
        }

        return dataList;
    }


    public static List<Map<String, Object>> setManyOutPutManyPollutantsCharDataList(List<Document> documents,
                                                                                    List<Map<String, Object>> pollutants,
                                                                                    String collection,
                                                                                    Map<String, String> outPutIdAndMn,
                                                                                    Map<String, String> idAndName,
                                                                                    Map<String, String> codeAndName,
                                                                                    Map<String, Integer> mnAndType) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        String valueCKey = "";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
            valueCKey = "AvgConvertStrength";
            pattern = "yyyy-MM-dd HH:mm";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
            valueCKey = "AvgConvertStrength";
            pattern = "yyyy-MM-dd HH";
        } else if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
            valueCKey = "ConvertConcentration";
            pattern = "yyyy-MM-dd HH:mm:ss";
        }


        Set<String> pollutantcodes = pollutants.stream().filter(m -> m.get("code") != null).map(m -> m.get("code").toString()).collect(Collectors.toSet());
        Map<String, Object> collect2 = pollutants.stream().filter(m -> m.get("code") != null && m.get("PollutantUnit") != null && StringUtils.isNotBlank(m.get("PollutantUnit").toString())).collect(Collectors.toMap(m -> m.get("code").toString(), m -> m.get("PollutantUnit"), (a, b) -> a));
        Map<Integer, Map<String, Object>> typeAndCodeAndIsCon = new HashMap<>();
        Integer type;
        Map<String, Object> codeAndIsCon;
        for (Map<String, Object> pollutant : pollutants) {
            type = Integer.parseInt(pollutant.get("PollutantType").toString());
            if (typeAndCodeAndIsCon.containsKey(type)) {
                codeAndIsCon = typeAndCodeAndIsCon.get(type);
            } else {
                codeAndIsCon = new HashMap<>();
            }
            codeAndIsCon.put(pollutant.get("code") + "", pollutant.get("IsHasConvertData"));
            typeAndCodeAndIsCon.put(type, codeAndIsCon);
        }
        String finalPattern = pattern;
        String finalPollutantDataKey = pollutantDataKey;

        Map<String, List<Document>> collect = documents.stream().filter(m -> m.get("MonitorTime") != null && m.get("DataGatherCode") != null && m.get(finalPollutantDataKey) != null).peek(m -> {
            String monitorTime = FormatUtils.formatCSTString(m.get("MonitorTime").toString(), finalPattern);
            m.put("MonitorTime", monitorTime);
            List<Map<String, Object>> maps = (List<Map<String, Object>>) m.get(finalPollutantDataKey);
            for (Map<String, Object> map : maps) {
                map.put("MonitorTime", monitorTime);
            }
        }).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString() + "_" + m.get("MonitorTime").toString().substring(0, 10)));
        Map<String, String> mnAndId = outPutIdAndMn.entrySet().stream().collect(Collectors.toMap(m -> m.getValue(), m -> m.getKey()));
        for (String monitordaytime : collect.keySet()) {
            String[] split = monitordaytime.split("_");
            String dgimn = split[0];
            String monitortime = split[1];
            List<Document> documentList = collect.get(monitordaytime);
            Map<String, List<Map<String, Object>>> collect1 = documentList.stream().filter(m -> m.get(finalPollutantDataKey) != null).flatMap(m -> ((List<Map<String, Object>>) m.get(finalPollutantDataKey)).stream())
                    .filter(m -> m.get("PollutantCode") != null && pollutantcodes.contains(m.get("PollutantCode").toString())).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
            for (String PollutantCode : collect1.keySet()) {
                String pollutantname = codeAndName.get(PollutantCode);
                String dataname = idAndName.get(mnAndId.get(dgimn));
                Map<String, Object> data = new HashMap<>();
                data.put("monitortime", monitortime);
                data.put("dataname", dataname + "-" + pollutantname + "-" + monitortime);
                List<Map<String, Object>> datamaplist = new ArrayList<>();
                List<Map<String, Object>> maps = collect1.get(PollutantCode);
                for (Map<String, Object> map : maps) {
                    Map<String, Object> datamap = new HashMap<>();
                    String MonitorTime = map.get("MonitorTime") == null ? "" : map.get("MonitorTime").toString();
                    String monitorvalue = map.get(valueKey) == null ? "" : map.get(valueKey).toString();
                    if (mnAndType.get(dgimn) != null) {
                        codeAndIsCon = typeAndCodeAndIsCon.get(mnAndType.get(dgimn));
                        if (codeAndIsCon.containsKey(PollutantCode) && 1 == Integer.parseInt(codeAndIsCon.get(PollutantCode).toString())) {
                            monitorvalue = map.get(valueCKey) == null ? "" : map.get(valueCKey).toString();
                        }
                    }
                    datamap.put("monitorpoint", MonitorTime.substring(11, MonitorTime.length()));
                    datamap.put("monitorvalue", monitorvalue);
                    datamap.put("PollutantUnit", collect2.get(PollutantCode));
                    datamaplist.add(datamap);
                }
                data.put("data", datamaplist.stream().distinct().filter(m -> m.get("monitorpoint") != null).sorted(Comparator.comparing(m -> m.get("monitorpoint").toString())).collect(Collectors.toList()));
                dataList.add(data);
            }
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 组装多站点单污染物图表监测数据（实时数据，分钟数据，小时数据，日数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyOutPutOnePollutantCharDataList(List<Document> documents,
                                                                                  String pollutantcode,
                                                                                  String collection,
                                                                                  Map<String, Object> mnAndId,
                                                                                  Map<String, Object> mnAndName) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, List<Map<String, Object>>> mnAndDataList = setMnAndDataList(documents, pollutantcode, collection);
        if (mnAndDataList.size() > 0) {
            for (String mnKey : mnAndDataList.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitorpointid", mnAndId.get(mnKey));
                resultMap.put("monitorpointname", mnAndName.get(mnKey));
                resultMap.put("datalist", mnAndDataList.get(mnKey));
                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 设置mn+监测数据集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Map<String, List<Map<String, Object>>> setMnAndDataList(List<Document> documents,
                                                                          String pollutantcode,
                                                                          String collection) {
        Map<String, List<Map<String, Object>>> mnAndDataList = new HashMap<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        }
        String mnCommon;
        String pollutantCode = "";
        Double value;
        String MonitorTime = "";

        List<Map<String, Object>> dataList;
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            value = null;
            if ("RealTimeData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
            } else if ("MinuteData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
            } else if ("HourData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            } else if ("DayData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                if (pollutantcode.equals(pollutantCode)) {
                    value = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                    break;
                }
            }
            if (value != null) {
                if (mnAndDataList.containsKey(mnCommon)) {
                    dataList = mnAndDataList.get(mnCommon);
                } else {
                    dataList = new ArrayList<>();
                }
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("monitortime", MonitorTime);
                dataMap.put("monitorvalue", value);
                dataList.add(dataMap);
                mnAndDataList.put(mnCommon, dataList);
            }
        }
        return mnAndDataList;
    }


    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 组装多站点单污染物图表监测数据（实时数据，分钟数据，小时数据，日数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyOutPutOnePollutantListDataList(List<Document> documents,
                                                                                  String pollutantcode,
                                                                                  String collection,
                                                                                  Map<String, Object> mnAndId,
                                                                                  Map<String, Object> mnAndName) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        }
        String mnCommon;
        String pollutantCode = "";
        Double value;
        String MonitorTime = "";
        Map<String, List<Map<String, Object>>> timeAndDataList = new HashMap<>();
        List<Map<String, Object>> dataList;
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            value = null;
            if ("RealTimeData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
            } else if ("MinuteData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
            } else if ("HourData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            } else if ("DayData".equals(collection)) {
                MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                if (pollutantcode.equals(pollutantCode)) {
                    value = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                    break;
                }
            }
            if (value != null) {
                if (timeAndDataList.containsKey(MonitorTime)) {
                    dataList = timeAndDataList.get(MonitorTime);
                } else {
                    dataList = new ArrayList<>();
                }
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("monitorpointid", mnAndId.get(mnCommon));
                dataMap.put("monitorpointname", mnAndName.get(mnCommon));
                dataMap.put("monitorvalue", value);
                dataList.add(dataMap);
                timeAndDataList.put(MonitorTime, dataList);
            }
        }
        if (timeAndDataList.size() > 0) {
            for (String timeKey : timeAndDataList.keySet()) {
                dataList = timeAndDataList.get(timeKey);
                if (dataList != null) {
                    for (Map<String, Object> dataMap : dataList) {
                        dataMap.put("monitortime", timeKey);
                        resultList.add(dataMap);
                    }
                }
            }
        }
        return resultList;
    }


    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 组装多空气站点多污染物图表监测数据（实时数据，分钟数据，小时数据，日数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyAirStationManyPollutantsCharDataList(
            List<Document> documents, List<String> pollutantcodes,
            String collection,
            Map<String, String> outPutIdAndMn,
            List<String> outputids,
            Map<String, String> idAndName,
            Map<String, String> codeAndName) {


        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        } else if ("MonthData".equals(collection)) {
            pollutantDataKey = "MonthDataList";
            valueKey = "AvgStrength";
        }
        //数采仪mn号
        String mnCode = "";
        //污染物编码
        String pollutantCode = "";
        //数采仪MN号+编码
        String mnPollutantCode = "";
        //key:数采仪MN号+编码,value:list(monitortime+monitorvalue)
        Map<String, List<Map<String, Object>>> tempMap = new HashMap<>();
        List<Map<String, Object>> tempList;
        for (Document document : documents) {

            String MonitorTime = "";
            if (collection.indexOf("Hour") > -1) {
                MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            } else if (collection.indexOf("Day") > -1) {
                MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            } else if (collection.indexOf("Month") > -1) {
                MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
            }
            if (document.get("StationCode") != null) {
                mnCode = document.getString("StationCode");
                mnPollutantCode = mnCode + ",aqi";
                if (tempMap.get(mnPollutantCode) != null) {
                    tempList = tempMap.get(mnPollutantCode);
                } else {
                    tempList = new ArrayList<>();
                }
                Map<String, Object> aqiMap = new HashMap<>();
                aqiMap.put("monitortime", MonitorTime);
                aqiMap.put("monitorvalue", document.get("AQI"));
                tempList.add(aqiMap);
                tempMap.put(mnPollutantCode, tempList);

            } else if (document.get("DataGatherCode") != null) {
                mnCode = document.getString("DataGatherCode");
                List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
                for (Document pollutant : pollutantDataList) {
                    pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                    if (pollutantcodes.contains(pollutantCode) && !"".equals(pollutantCode)) {
                        mnPollutantCode = mnCode + "," + pollutantCode;
                        if (tempMap.get(mnPollutantCode) != null) {
                            tempList = tempMap.get(mnPollutantCode);
                        } else {
                            tempList = new ArrayList<>();
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("monitortime", MonitorTime);
                        map.put("monitorvalue", pollutant.get(valueKey));
                        tempList.add(map);
                        tempMap.put(mnPollutantCode, tempList);
                    }
                }
            }
        }
        List<Map<String, Object>> monitorDataList;
        for (String outputid : idAndName.keySet()) {
            for (String tempCode : pollutantcodes) {
                mnPollutantCode = outPutIdAndMn.get(outputid) + "," + tempCode;
                monitorDataList = tempMap.get(mnPollutantCode);
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("outputid", outputid);
                if (tempCode.equals("aqi")) {
                    pollutantMap.put("dataname", idAndName.get(outputid) + "-AQI");
                } else {
                    pollutantMap.put("dataname", idAndName.get(outputid) + "-" + codeAndName.get(tempCode));
                }
                pollutantMap.put("pollutantcode", tempCode);
                pollutantMap.put("monitorDataList", monitorDataList);
                dataList.add(pollutantMap);
            }
        }
        return dataList;
    }


    /**
     * @author: lip
     * @date: 2019/6/24 0024 上午 11:52
     * @Description: 组装多站点多污染物图表监测数据（月、年数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyOutPutManyPollutantsMonthYearCharDataList(
            List<Document> documents,
            List<String> pollutantcodes,
            String collection,
            Map<String, String> outPutIdAndMn,
            List<String> outputids,
            Map<String, String> idAndName,
            Map<String, String> codeAndName
    ) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("MonthFlowData".equals(collection)) {
            pollutantDataKey = "MonthFlowDataList";
            valueKey = "PollutantFlow";
        } else if ("YearFlowData".equals(collection)) {
            pollutantDataKey = "YearFlowDataList";
            valueKey = "PollutantFlow";
        }
        //数采仪mn号
        String mnCode = "";
        //污染物编码
        String pollutantCode = "";
        //数采仪MN号+编码
        String mnPollutantCode = "";
        //key:数采仪MN号+编码,value:list(monitortime+monitorvalue)
        Map<String, List<Map<String, Object>>> tempMap = new HashMap<>();
        List<Map<String, Object>> tempList;
        for (Document document : documents) {
            mnCode = document.getString("DataGatherCode");
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                if (pollutantcodes.contains(pollutantCode) && !"".equals(pollutantCode)) {
                    mnPollutantCode = mnCode + "," + pollutantCode;
                    if (tempMap.get(mnPollutantCode) != null) {
                        tempList = tempMap.get(mnPollutantCode);
                    } else {
                        tempList = new ArrayList<>();
                    }
                    Map<String, Object> map = new HashMap<>();
                    String MonitorTime = "";
                    if ("MonthFlowData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    } else if ("YearFlowData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateY(document.getDate("MonitorTime"));
                    }
                    map.put("monitortime", MonitorTime);
                    map.put("monitorvalue", pollutant.get(valueKey));
                    tempList.add(map);
                    tempMap.put(mnPollutantCode, tempList);
                }
            }
        }
        List<Map<String, Object>> monitorDataList;
        for (String outputid : outputids) {
            for (String tempCode : pollutantcodes) {
                mnPollutantCode = outPutIdAndMn.get(outputid) + "," + tempCode;
                monitorDataList = tempMap.get(mnPollutantCode);
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("outputid", outputid);
                pollutantMap.put("dataname", idAndName.get(outputid) + "-" + codeAndName.get(tempCode));
                pollutantMap.put("pollutantcode", tempCode);
                pollutantMap.put("monitorDataList", monitorDataList);
                dataList.add(pollutantMap);
            }
        }
        return dataList;
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:21
     * @Description: 组装单站点多污染物图表监测数据（月数据、年数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setOneOutPutManyPollutantsMonthYearDataList(List<Document> documents, List<String> pollutantcodes, String collection) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> monitorDataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("MonthFlowData".equals(collection)) {
            pollutantDataKey = "MonthFlowDataList";
            valueKey = "PollutantFlow";
        } else if ("YearFlowData".equals(collection)) {
            pollutantDataKey = "YearFlowDataList";
            valueKey = "PollutantFlow";
        }
        for (String tempCode : pollutantcodes) {
            Map<String, Object> pollutantMap = new HashMap<>();
            for (Document document : documents) {
                List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
                for (Document temp : pollutantDataList) {
                    if (tempCode.equals(temp.get("PollutantCode"))) {
                        Map<String, Object> map = new HashMap<>();
                        String MonitorTime = "";
                        if ("MonthFlowData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                        } else if ("YearFlowData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateY(document.getDate("MonitorTime"));
                        }
                        map.put("monitortime", MonitorTime);
                        map.put("monitorvalue", temp.get(valueKey));
                        monitorDataList.add(map);
                        break;
                    }
                }
            }
            pollutantMap.put("pollutantcode", tempCode);
            pollutantMap.put("monitorDataList", monitorDataList);
            dataList.add(pollutantMap);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:21
     * @Description: 组装单站点多污染物图表监测数据（实时数据，分钟数据，小时数据，日数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setOneOutPutManyPollutantsCharDataList(List<Document> documents, List<Map<String, Object>> pollutants, String collection) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> monitorDataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
        }
        String pollutantCode;
        Integer IsHasConvertData;

        for (Map<String, Object> pollutant : pollutants) {
            pollutantCode = pollutant.get("code") + "";
            IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
            valueKey = getValueKey(collection, IsHasConvertData);
            Map<String, Object> pollutantMap = new HashMap<>();
            String MonitorTime;
            for (Document document : documents) {
                List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
                MonitorTime = "";
                Map<String, Object> monitorMap = new HashMap<>();
                for (Document temp : pollutantDataList) {
                    if (pollutantCode.equals(temp.get("PollutantCode"))) {
                        if ("RealTimeData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                        } else if ("MinuteData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                        } else if ("HourData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        } else if ("DayData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                        }
                        if (temp.get(valueKey) != null) {
                            monitorMap.put("monitortime", MonitorTime);
                            monitorMap.put("monitorvalue", DataFormatUtil.subZeroAndDot(temp.get(valueKey).toString()));
                            monitorMap.put("isoverstandard", temp.get("IsOverStandard"));
                            monitorMap.put("IsOver", temp.get("IsOver"));
                        } else {
                            MonitorTime = "";
                        }
                        break;
                    }
                }
                if (StringUtils.isNotBlank(MonitorTime)) {
                    monitorDataList.add(monitorMap);
                }
            }
            pollutantMap.put("pollutantcode", pollutantCode);
            pollutantMap.put("monitorDataList", monitorDataList);
            dataList.add(pollutantMap);
        }
        return dataList;
    }

    private static String getValueKey(String collection, Integer isHasConvertData) {
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            valueKey = "MonitorValue";
            if (isHasConvertData == 1) {
                valueKey = "ConvertConcentration";
            }

        } else if ("MinuteData".equals(collection)) {
            valueKey = "AvgStrength";
            if (isHasConvertData == 1) {
                valueKey = "AvgConvertStrength";
            }


        } else if ("HourData".equals(collection)) {
            valueKey = "AvgStrength";
            if (isHasConvertData == 1) {
                valueKey = "AvgConvertStrength";
            }


        } else if ("DayData".equals(collection)) {
            valueKey = "AvgStrength";
            if (isHasConvertData == 1) {
                valueKey = "AvgConvertStrength";
            }

        }
        return valueKey;
    }

    /**
     * @author: lip
     * @date: 2019/6/24 0024 下午 3:51
     * @Description: 根据数据标记获取mongodb集合名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getCollectionByDataMark(Integer datamark) {
        String collection = "";
        if (datamark == 1) {
            collection = "RealTimeData";
        } else if (datamark == 2) {
            collection = "MinuteData";
        } else if (datamark == 3) {
            collection = "HourData";
        } else if (datamark == 4) {
            collection = "DayData";
        } else if (datamark == 5) {
            collection = "MonthData";
        } else if (datamark == 6) {
            collection = "YearData";
        }
        return collection;
    }


    /**
     * @author: lip
     * @date: 2019/6/24 0024 下午 3:51
     * @Description: 根据数据标记获取mongodb集合名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getMonitorTimeByCollection(String collection, Date monitortime) {
        String time = "";
        if (collection.equals("RealTimeData")) {
            time = DataFormatUtil.getDateYMDHMS(monitortime);
        } else if (collection.equals("MinuteData")) {
            time = DataFormatUtil.getDateYMDHM(monitortime);
        } else if (collection.equals("HourData")) {
            time = DataFormatUtil.getDateYMDH(monitortime);
        } else if (collection.equals("DayData")) {
            time = DataFormatUtil.getDateYMD(monitortime);
        }
        return time;
    }


    /**
     * @author: lip
     * @date: 2019/6/24 0024 下午 3:51
     * @Description: 根据数据标记补充开始时间格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String setStartTimeByDataMark(Integer datamark, String time) {
        if (datamark == 2) {
            time += ":00";
        } else if (datamark == 3) {
            time += ":00:00";
        } else if (datamark == 4) {
            time += " 00:00:00";
        } else if (datamark == 5) {
            time += "-01 00:00:00";
        }
        return time;
    }

    /**
     * @author: lip
     * @date: 2019/6/24 0024 下午 3:51
     * @Description: 根据数据标记补充查询时间格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String setEndTimeByDataMark(Integer datamark, String time) {
        if (datamark == 2) {//分钟数据
            time += ":59";
        } else if (datamark == 3) {//小时数据
            time += ":59:59";
        } else if (datamark == 4) {//日数据
            time += " 23:59:59";
        } else if (datamark == 5) {//月数据
            time = DataFormatUtil.getLastDayOfMonth(time) + " 23:59:59";
        }
        return time;
    }

    /**
     * @author: lip
     * @date: 2019/7/8 0008 下午 3:02
     * @Description: 设置单个点位，多个污染物数据集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    public static List<Map<String, Object>> setOneAirStationManyPollutantsCharDataList(List<Document> documents, List<String> pollutantcodes, String collection) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> monitorDataList = new ArrayList<>();

        String pollutantDataKey = getPollutantDataKeyByCollection(collection);
        String valueKey = getValueKeyByCollection(collection);
        String primarypollutant;
        for (String tempCode : pollutantcodes) {
            Map<String, Object> pollutantMap = new HashMap<>();
            if (tempCode.equals("aqi")) {
                pollutantDataKey = "DataList";
                valueKey = "Strength";
                for (Document document : documents) {
                    String MonitorTime = "";
                    if (collection.indexOf("Hour") > -1) {
                        MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    } else if (collection.indexOf("Day") > -1) {
                        MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    } else if (collection.indexOf("Month") > -1) {
                        MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("monitortime", MonitorTime);
                    map.put("monitorvalue", document.get("AQI"));
                    primarypollutant = document.get("PrimaryPollutant") != null ? document.getString("PrimaryPollutant") : "";
                    map.put("primarypollutant", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(primarypollutant));
                    monitorDataList.add(map);
                }
            } else {
                for (Document document : documents) {
                    String MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                    ;
                    if (collection.indexOf("Hour") > -1) {
                        MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    } else if (collection.indexOf("Day") > -1) {
                        MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    } else if (collection.indexOf("Month") > -1) {
                        MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    }
                    List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
                    for (Document temp : pollutantDataList) {
                        if (tempCode.equals(temp.get("PollutantCode"))) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("monitortime", MonitorTime);
                            map.put("monitorvalue", temp.get(valueKey));
                            monitorDataList.add(map);
                            break;
                        }
                    }
                }
            }
            pollutantMap.put("pollutantcode", tempCode);
            pollutantMap.put("monitorDataList", monitorDataList);
            dataList.add(pollutantMap);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 上午 9:28
     * @Description: 根据数据标记获取空气站点监测数据集合名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getAirCollectionByDataMark(Integer datamark) {
        String collection = "";
        if (datamark == 1) {
            collection = "StationHourAQIData";
        } else if (datamark == 2) {
            collection = "StationDayAQIData";
        } else if (datamark == 3) {
            collection = "StationMonthAQIData";
        }
        return collection;
    }

    /**
     * @author: lip
     * @date: 2019/7/2 0002 下午 3:39
     * @Description: 设置空气站点开始时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String setAirStartTimeByDataMark(Integer datamark, String time) {
        if (datamark == 1) {//小时数据
            time += ":00:00";
        } else if (datamark == 2) {//日数据
            time += " 00:00:00";
        } else if (datamark == 3) {//月数据
            time += "-01 00:00:00";
        }
        return time;
    }

    /**
     * @author: lip
     * @date: 2019/7/2 0002 下午 3:39
     * @Description: 设置空气站点结束时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String setAirEndTimeByDataMark(Integer datamark, String time) {
        if (datamark == 1) {//小时数据
            time += ":59:59";
        } else if (datamark == 2) {//日数据
            time += " 23:59:59";
        } else if (datamark == 3) {//月数据
            time = DataFormatUtil.getLastDayOfMonth(time);
            time += " 23:59:59";
        }
        return time;
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 4:43
     * @Description: 统计风列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> countWindDataList(List<Document> documents, String collection, Map<String, Object> mnAndOutPutName, int totalnum) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> pollutantList;
        String[] speedCodeList = DataFormatUtil.speedCode;
        String[] speedNameList = DataFormatUtil.speedName;

        String pollutantDataKey = "";
        String valueKey = "";
        if (collection.equals("HourData")) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("DayData")) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("MinuteData")) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        }
        for (String mn : mnAndOutPutName.keySet()) {
            Double windSpeed = null;
            String windLevel = "";
            List<Double> speedValueList;
            Map<String, List<Double>> speedLevelAndSpeedValueMap = new HashMap<>();
            int num = 0;
            for (Document document : documents) {
                if (mn.equals(document.get("DataGatherCode"))) {
                    pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
                    for (Map<String, Object> pollutant : pollutantList) {
                        if (WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                            windSpeed = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                            break;
                        }
                    }
                    if (windSpeed != null) {
                        num += 1;
                        windSpeed = Double.parseDouble(DataFormatUtil.formatDoubleSaveOne(windSpeed));
                        windLevel = DataFormatUtil.windSpeedSwitch(windSpeed, "name");
                        if (speedLevelAndSpeedValueMap.get(windLevel) != null) {
                            speedValueList = speedLevelAndSpeedValueMap.get(windLevel);
                        } else {
                            speedValueList = new ArrayList<>();
                        }
                        speedValueList.add(windSpeed);
                        speedLevelAndSpeedValueMap.put(windLevel, speedValueList);
                    }
                }
            }
            Double percent = 0d;
            int subtimenum = 0;
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("stationname", mnAndOutPutName.get(mn));
            dataMap.put("counttimenum", num);
            for (int i = 0; i < speedCodeList.length; i++) {
                speedValueList = speedLevelAndSpeedValueMap.get(speedNameList[i]);
                subtimenum = speedValueList != null ? speedValueList.size() : 0;
                if (num > 0) {
                    percent = 100d * subtimenum / num;
                } else {
                    percent = 0.0;
                }
                if (speedCodeList[i].equals("quietwind")) {
                    dataMap.put(speedCodeList[i] + "timenum", subtimenum);
                    dataMap.put(speedCodeList[i] + "percent", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(percent)) + "%");
                } else {
                    dataMap.put(speedCodeList[i] + "timenum", subtimenum);
                    dataMap.put(speedCodeList[i] + "percent", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(percent)) + "%");
                    dataMap.put(speedCodeList[i] + "avgspeed", DataFormatUtil.getListAvgValue(speedValueList).equals("") ? "-" : DataFormatUtil.getListAvgValue(speedValueList));
                }
            }
            dataList.add(dataMap);
        }


        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 4:43
     * @Description: 统计风图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> countWindChartData(List<Document> documents, String collection) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> pollutantList;
        String[] speedValueList = DataFormatUtil.speedValue;
        String[] speedNameList = DataFormatUtil.speedName;
        String[] directCodeList = DataFormatUtil.directCode;
        String[] directNameList = DataFormatUtil.directName;


        String pollutantDataKey = "";
        String valueKey = "";
        if (collection.equals("HourData")) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("DayData")) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("MinuteData")) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        }


        for (int i = 0; i < speedNameList.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("windspeedname", speedNameList[i]);
            map.put("windspeedvalue", speedValueList[i]);
            Double windspeed = 0d;
            Double winddirection = null;
            String winddirectioncode = "";
            List<Double> speedList;
            Map<String, List<Double>> directioncodeAndSpeedMap = new HashMap<>();
            for (Document document : documents) {
                pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
                for (Map<String, Object> pollutant : pollutantList) {
                    if (WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        windspeed = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : 0d;
                        break;
                    }
                }
                for (Map<String, Object> pollutant : pollutantList) {
                    if (WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        winddirection = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                        break;
                    }
                }
                if (winddirection != null) {
                    if (DataFormatUtil.windSpeedSwitch(windspeed, "name").equals(speedNameList[i])) {//同一风级
                        winddirectioncode = DataFormatUtil.windDirectionSwitch(winddirection, "code");
                        if (directioncodeAndSpeedMap.get(winddirectioncode) != null) {
                            speedList = directioncodeAndSpeedMap.get(winddirectioncode);
                        } else {
                            speedList = new ArrayList<>();
                        }
                        speedList.add(windspeed);
                        directioncodeAndSpeedMap.put(winddirectioncode, speedList);
                    }
                }
            }
            List<Map<String, Object>> winddatalist = new ArrayList<>();
            for (int j = 0; j < directCodeList.length; j++) {
                Map<String, Object> windDataMap = new HashMap<>();

                if (directioncodeAndSpeedMap.get(directCodeList[j]) != null) {
                    windDataMap.put("windspeednum", directioncodeAndSpeedMap.get(directCodeList[j]).size());
                } else {
                    windDataMap.put("windspeednum", 0);
                }
                windDataMap.put("winddirectioncode", directCodeList[j]);
                windDataMap.put("winddirectionname", directNameList[j]);
                winddatalist.add(windDataMap);
            }
            map.put("winddatalist", winddatalist);
            dataList.add(map);
        }


        return dataList;
    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 上午 10:28
     * @Description: 获取主导风向
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [documents, monitordata, collection, pollutantcode]
     * @throws:
     */
    public static String getPredominantWindDirection(List<Document> monitordata) {
        //风向
        String WindDirection = WindDirectionEnum.getCode();
        List<String> directionlist = new ArrayList<>();
        for (Document monitordatum : monitordata) {
            List<Map<String, Object>> airlist = monitordatum.get("HourDataList") == null ? new ArrayList<Map<String, Object>>() : (List<Map<String, Object>>) monitordatum.get("HourDataList");
            Optional<Double> first = airlist.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && WindDirection.equals(m.get("PollutantCode").toString()))
                    .map(m -> Double.valueOf(m.get("AvgStrength").toString())).findFirst();
            if (first.isPresent()) {
                Double aDouble = first.get();
                //获取风向名称
                String windDirectionName = DataFormatUtil.windDirectionSwitch(aDouble, "name");
                directionlist.add(windDirectionName);
            }
        }
        Optional<Map.Entry<String, Long>> max = directionlist.stream().collect(Collectors.groupingBy(m -> m, Collectors.counting())).entrySet().stream().max(Comparator.comparing(m -> m.getValue()));
        if (max.isPresent()) {
            Map.Entry<String, Long> stringLongEntry = max.get();
            return stringLongEntry.getKey();
        }
        return null;
    }


    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 4:43
     * @Description: 获取监测时间，风向，风速数据
     * @updateUser:xsm
     * @updateDate:2019/08/29 0029 下午6:40
     * @updateDescription:分钟数据处理
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> getWindDataList(List<Document> documents, String collection) {

        String pollutantDataKey = "";
        String valueKey = "";
        if (collection.equals("MinuteData")) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("HourData")) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("DayData")) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Double windspeed = 0d;
        Double winddirection = null;
        List<Map<String, Object>> pollutantList;
        for (Document document : documents) {
            if (document.get("MonitorTime") != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
                for (Map<String, Object> pollutant : pollutantList) {
                    if (WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        windspeed = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : 0d;
                        break;
                    }
                }
                for (Map<String, Object> pollutant : pollutantList) {
                    if (WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        winddirection = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                        break;
                    }
                }
                if (winddirection != null) {
                    map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                    map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                    map.put("winddirectionvalue", winddirection);
                } else {
                    map.put("winddirectioncode", winddirection);
                    map.put("winddirectionvalue", winddirection);
                    map.put("winddirectionname", winddirection);
                }
                map.put("windspeed", windspeed);
                if (collection.equals("DayData")) {
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                } else if (collection.equals("HourData")) {
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                } else {
                    map.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime")));
                }
                dataList.add(map);
            }
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/27 0027 下午 2:24
     * @Description: 拼装表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> getHeaderData(String countmark) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        stationname.put("headername", "站点名称");
        stationname.put("headercode", "stationname");
        stationname.put("rownum", "2");
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);

        Map<String, Object> counttimenum = new HashMap<>();
        counttimenum.put("headername", "统计" + countmark);
        counttimenum.put("headercode", "counttimenum");
        counttimenum.put("rownum", "2");
        counttimenum.put("columnnum", "1");
        counttimenum.put("chlidheader", new ArrayList<>());
        dataList.add(counttimenum);


        String[] windLevel = DataFormatUtil.speedName;

        String columnnum = "2";
        for (int i = 0; i < windLevel.length; i++) {
            if (!"静风".equals(windLevel[i])) {
                columnnum = "3";
            }
            Map<String, Object> wind = new HashMap<>();
            wind.put("headername", windLevel[i]);
            wind.put("headercode", DataFormatUtil.speedCode[i]);
            wind.put("rownum", "1");
            wind.put("columnnum", columnnum);
            List<Map<String, Object>> chlidheader = new ArrayList<>();

            Map<String, Object> windtimenum = new HashMap<>();
            windtimenum.put("headername", countmark);
            windtimenum.put("headercode", DataFormatUtil.speedCode[i] + "timenum");
            windtimenum.put("rownum", "1");
            windtimenum.put("columnnum", "1");
            windtimenum.put("chlidheader", new ArrayList<>());
            chlidheader.add(windtimenum);

            Map<String, Object> windpercent = new HashMap<>();
            windpercent.put("headername", "占比");
            windpercent.put("headercode", DataFormatUtil.speedCode[i] + "percent");
            windpercent.put("rownum", "1");
            windpercent.put("columnnum", "1");
            windpercent.put("chlidheader", new ArrayList<>());
            chlidheader.add(windpercent);
            if (!"静风".equals(windLevel[i])) {
                Map<String, Object> avgspeed = new HashMap<>();
                avgspeed.put("headername", "平均风速");
                avgspeed.put("headercode", DataFormatUtil.speedCode[i] + "avgspeed");
                avgspeed.put("rownum", "1");
                avgspeed.put("columnnum", "1");
                avgspeed.put("chlidheader", new ArrayList<>());
                chlidheader.add(avgspeed);
            }
            dataList.add(wind);
            wind.put("chlidheader", chlidheader);
        }


        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/7/3 0003 上午 10:04
     * @Description: 设置突增表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> getHeaderDataPollutantUpRushByMonitorPointType(int monitorPointTypeCode) {
        List<String> header = new ArrayList<>();
        if (CommonTypeEnum.getOutPutTypeList().contains(monitorPointTypeCode)) {
            header = Arrays.asList("企业名称", "排口名称", "污染物", "日期", "突变时间点", "前4小时浓度均值", "当前时间点浓度值", "突变增量");
        } else if (CommonTypeEnum.getMonitorPointTypeList().contains(monitorPointTypeCode)) {
            header = Arrays.asList("监测点名称", "污染物", "日期", "突变时间点", "前4小时浓度均值", "当前时间点浓度值", "突变增量");
        } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(monitorPointTypeCode)) {
            header = Arrays.asList("企业名称", "监测点名称", "污染物", "日期", "突变时间点", "前4小时浓度均值", "当前时间点浓度值", "突变增量");
        }
        return header;
    }

    /**
     * @author: lip
     * @date: 2019/7/3 0003 上午 10:04
     * @Description: 设置突增表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> setHeaderFieldDataPollutantUpRushByMonitorPointType(int monitorPointTypeCode) {
        List<String> headersField = new ArrayList<>();
        if (CommonTypeEnum.getOutPutTypeList().contains(monitorPointTypeCode)) {
            headersField = Arrays.asList("pollutionname", "outputname", "pollutantname", "monitortime", "changetime", "before4houravg", "currenthour", "uprushpercent");
        } else if (CommonTypeEnum.getMonitorPointTypeList().contains(monitorPointTypeCode)) {
            headersField = Arrays.asList("outputname", "pollutantname", "monitortime", "changetime", "before4houravg", "currenthour", "uprushpercent");
        } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(monitorPointTypeCode)) {
            headersField = Arrays.asList("pollutionname", "outputname", "pollutantname", "monitortime", "changetime", "before4houravg", "currenthour", "uprushpercent");
        }
        return headersField;
    }


    /**
     * @author: chengzq
     * @date: 2019/7/9 0009 下午 4:58
     * @Description: 获取mongdb中的字段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    public static String getFlowFiled(Integer type) {
        if (type == 1) {//浓度表中污染物修正浓度字段
            return "CorrectedFlow";
        } else if (type == 2) {
            return "AvgStrength";
        }
        return "";
    }

    /**
     * @author: lip
     * @date: 2019/7/27 0027 下午 2:09
     * @Description: 统计风级风频次数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> getWindLevelDataList(List<Document> documents, String collection, Map<String, String> mnAndOutputName) {

        String pollutantDataKey = "";
        String valueKey = "";
        if (collection.equals("HourData")) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if (collection.equals("DayData")) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        Double windspeed = 0d;
        Map<String, List<Double>> mnAndSpeedWind = new HashMap<>();
        String mn;
        List<Double> speedWind;
        List<Map<String, Object>> pollutantList;
        for (Document document : documents) {
            mn = document.getString("DataGatherCode");
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
            pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
            for (Map<String, Object> pollutant : pollutantList) {
                if (WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                    windspeed = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : 0d;
                    break;
                }
            }
            if (mnAndSpeedWind.containsKey(mn)) {

                speedWind = mnAndSpeedWind.get(mn);
            } else {
                speedWind = new ArrayList<>();

            }
            speedWind.add(windspeed);
            mnAndSpeedWind.put(mn, speedWind);

        }
        if (mnAndSpeedWind.size() > 0) {
            for (String mnKey : mnAndSpeedWind.keySet()) {
                Map<String, Object> map = new HashMap<>();
                speedWind = mnAndSpeedWind.get(mnKey);
                Map<String, Integer> windLevel = getWindLevelMap(speedWind);
                map.put("monitorpointname", mnAndOutputName.get(mnKey));
                map.put("winddata", windLevel);
                dataList.add(map);
            }
        }
        return dataList;


    }

    /**
     * @author: lip
     * @date: 2019/7/27 0027 下午 2:52
     * @Description: 根据风速数组获取风级对象分组频次
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static Map<String, Integer> getWindLevelMap(List<Double> speedWind) {
        Map<String, Integer> windLevelMap = new LinkedHashMap<>();
        Map<String, Integer> windLevelMapSub = new LinkedHashMap<>();
        String[] speedCodes = DataFormatUtil.speedCode;
        String speedCode;
        for (int i = 0; i < speedWind.size(); i++) {
            speedCode = DataFormatUtil.windSpeedSwitch(speedWind.get(i), "code");
            if (windLevelMapSub.containsKey(speedCode)) {
                windLevelMapSub.put(speedCode, windLevelMapSub.get(speedCode) + 1);
            } else {
                windLevelMapSub.put(speedCode, +1);
            }
        }
        for (int i = 0; i < speedCodes.length; i++) {
            windLevelMap.put(speedCodes[i], windLevelMapSub.get(speedCodes[i]) != null ? windLevelMapSub.get(speedCodes[i]) : 0);
        }
        return windLevelMap;
    }

    /**
     * @author: lip
     * @date: 2019/7/29 0029 下午 2:03
     * @Description: 组合mongodb气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> getWeatherDataList(List<Document> documents, String collection, List<String> times, List<String> pollutantcodes) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (documents.size() > 0) {

            String pollutantDataKey = "";
            String valueKey = "";
            if (collection.equals("HourData")) {
                pollutantDataKey = "HourDataList";
                valueKey = "AvgStrength";
            } else if (collection.equals("DayData")) {
                pollutantDataKey = "DayDataList";
                valueKey = "AvgStrength";
            }
            Map<String, Map<String, Object>> codeAndTimeAndValue = new HashMap<>();
            Map<String, Object> timeAndValue;
            String monitortime = "";
            String pollutantcode;
            List<Map<String, Object>> pollutantList;
            Double monitorvalue = null;
            for (Document document : documents) {

                if (collection.equals("HourData")) {
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if (collection.equals("DayData")) {
                    monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
                for (Map<String, Object> pollutant : pollutantList) {
                    pollutantcode = pollutant.get("PollutantCode").toString();
                    if (pollutantcodes.contains(pollutantcode)) {
                        monitorvalue = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                    }
                    if (codeAndTimeAndValue.containsKey(pollutantcode)) {
                        timeAndValue = codeAndTimeAndValue.get(pollutantcode);
                    } else {
                        timeAndValue = new HashMap<>();
                    }
                    timeAndValue.put(monitortime, monitorvalue);
                    codeAndTimeAndValue.put(pollutantcode, timeAndValue);
                }
            }
            for (String code : pollutantcodes) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("pollutantcode", code);
                resultMap.put("pollutantname", CommonTypeEnum.WeatherPollutionEnum.getNameByCode(code));
                resultMap.put("datalist", getDataList(codeAndTimeAndValue.get(code), times));
                resultList.add(resultMap);
            }

        }


        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/7/29 0029 下午 2:26
     * @Description: 补全日期+数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static List<Map<String, Object>> getDataList(Map<String, Object> timeAndValue, List<String> times) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (String time : times) {
            Map<String, Object> map = new HashMap<>();
            map.put("monitortime", time);
            if (timeAndValue != null) {
                map.put("monitorvalue", timeAndValue.get(time));
            } else {
                map.put("monitorvalue", null);
            }
            dataList.add(map);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/8/7 0007 下午 3:15
     * @Description: 组装多个监测点根据监测时间分组数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyMNData(List<String> dgimns, List<Document> monitorData, String collection, String pollutantcode) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        String pollutantDataKey = getPollutantDataKeyByCollection(collection);
        String valueKey = getValueKeyByCollection(collection);
        String mn;
        Date monitortime;
        String monitorTimeString;
        Double monitorvalue = null;
        String pollutantcodekey;
        Map<String, Map<String, Object>> timeAndMNAndValue = new LinkedHashMap<>();
        List<Map<String, Object>> pollutantList;
        Map<String, Map<String, Double>> mnAndTimeAndValue = new HashMap<>();

        for (Document document : monitorData) {
            pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
            for (Map<String, Object> pollutant : pollutantList) {
                pollutantcodekey = pollutant.get("PollutantCode").toString();
                if (pollutantcode.contains(pollutantcodekey)) {
                    monitorvalue = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                    break;
                }
            }
            mn = document.getString("DataGatherCode");
            monitortime = document.getDate("MonitorTime");
            monitorTimeString = getMonitorTimeByCollection(collection, monitortime);
            if (timeAndMNAndValue.containsKey(monitorTimeString)) {
                timeAndMNAndValue.get(monitorTimeString).put(mn, monitorvalue);
            } else {
                Map<String, Object> MNAndValue = new HashMap<>();
                MNAndValue.put(mn, monitorvalue);
                timeAndMNAndValue.put(monitorTimeString, MNAndValue);
            }
            if (monitorvalue != null) {
                if (mnAndTimeAndValue.containsKey(mn)) {
                    mnAndTimeAndValue.get(mn).put(monitorTimeString, monitorvalue);
                } else {
                    Map<String, Double> timeAndValue = new HashMap<>();
                    timeAndValue.put(monitorTimeString, monitorvalue);
                    mnAndTimeAndValue.put(mn, timeAndValue);
                }
            }
        }
        if (mnAndTimeAndValue.size() > 0) {
            Map<String, Map<String, Integer>> mnAndTimeAndIndex = new HashMap<>();
            for (String mnKey : mnAndTimeAndValue.keySet()) {
                Map<String, Double> timeAndValue = mnAndTimeAndValue.get(mnKey);
                Map<String, Double> sortMap = timeAndValue.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

                Map<String, Integer> timeAndIndex = new HashMap<>();

                Map<Double, Integer> valueAndIndex = new HashMap<>();
                int num = 1;

                for (String time : sortMap.keySet()) {
                    if (valueAndIndex.containsKey(sortMap.get(time))) {
                        num = valueAndIndex.get(sortMap.get(time));
                    } else {
                        num++;
                    }
                    valueAndIndex.put(sortMap.get(time), num);
                    timeAndIndex.put(time, num);
                }
                mnAndTimeAndIndex.put(mnKey, timeAndIndex);
            }
            for (String time : timeAndMNAndValue.keySet()) {
                Map<String, Object> map = new LinkedHashMap<>();

                map.put("monitortime", time);
                List<Map<String, Object>> datalist = new ArrayList<>();
                for (String mnKey : dgimns) {
                    Map<String, Object> mnData = new LinkedHashMap<>();
                    mnData.put("dgimn", mnKey);
                    if (timeAndMNAndValue.get(time).get(mnKey) != null) {
                        mnData.put("value", timeAndMNAndValue.get(time).get(mnKey));
                        mnData.put("valuesort", mnAndTimeAndIndex.get(mnKey).get(time));
                    } else {
                        mnData.put("value", "");
                        mnData.put("valuesort", "");
                    }
                    datalist.add(mnData);
                }
                map.put("datalist", datalist);
                dataList.add(map);
            }
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/8/7 0007 下午 3:44
     * @Description: 根据集合名称获取污染物值key
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getValueKeyByCollection(String collection) {
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            valueKey = "AvgStrength";
        } else if ("MonthData".equals(collection)) {
            valueKey = "AvgStrength";
        } else if ("YearData".equals(collection)) {
            valueKey = "AvgStrength";
        }
        return valueKey;
    }

    /**
     * @author: lip
     * @date: 2019/8/7 0007 下午 3:44
     * @Description: 根据集合名称获取污染物值key
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getTimeFByCollection(String collection) {
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            valueKey = "yyyy-MM-dd HH:mm:ss";
        } else if ("MinuteData".equals(collection)) {
            valueKey = "yyyy-MM-dd HH:mm";
        } else if ("HourData".equals(collection)) {
            valueKey = "yyyy-MM-dd HH";
        } else if ("DayData".equals(collection)) {
            valueKey = "yyyy-MM-dd";
        }
        return valueKey;
    }

    /**
     * @author: lip
     * @date: 2019/8/7 0007 下午 3:44
     * @Description: 根据集合名称获取污染物数据key
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getPollutantDataKeyByCollection(String collection) {
        String pollutantDataKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
        } else if ("MonthData".equals(collection)) {
            pollutantDataKey = "MonthDataList";
        } else if ("YearData".equals(collection)) {
            pollutantDataKey = "YearDataList";
        }
        return pollutantDataKey;
    }

    /**
     * @author: lip
     * @date: 2019/8/8 0008 下午 2:05
     * @Description: 组装多个监测点图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyMNChartData(List<String> dgimns, List<Document> monitorData, String collection, String pollutantcode) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = getPollutantDataKeyByCollection(collection);
        String valueKey = getValueKeyByCollection(collection);
        String mn;
        Date monitortime;
        String monitorTimeString;
        Double monitorvalue = null;
        String pollutantcodekey;
        Map<String, List<Map<String, Object>>> mnAndTimeAndValue = new LinkedHashMap<>();

        Map<String, List<String>> mnAndOverTimeList = new HashMap<>();
        List<String> overTimeList;
        List<Map<String, Object>> pollutantList;
        List<Map<String, Object>> timeAndValueList;
        boolean IsOverStandard = false;
        Map<String, Boolean> mnAndIsOver = new HashMap<>();
        for (Document document : monitorData) {
            pollutantList = (List<Map<String, Object>>) document.get(pollutantDataKey);
            for (Map<String, Object> pollutant : pollutantList) {
                pollutantcodekey = pollutant.get("PollutantCode").toString();
                if (pollutantcode.contains(pollutantcodekey)) {
                    monitorvalue = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.get(valueKey).toString()) : null;
                    IsOverStandard = pollutant.get("IsOverStandard") != null ? Boolean.parseBoolean(pollutant.get("IsOverStandard").toString()) : false;
                    break;
                }
            }
            mn = document.getString("DataGatherCode");
            monitortime = document.getDate("MonitorTime");
            monitorTimeString = getMonitorTimeByCollection(collection, monitortime);
            Map<String, Object> timeAndValue = new LinkedHashMap<>();
            timeAndValue.put("monitortime", monitorTimeString);
            timeAndValue.put("monitorvalue", monitorvalue);
            timeAndValue.put("isoverstandard", IsOverStandard);
            if (IsOverStandard) {
                if (mnAndOverTimeList.containsKey(mn)) {
                    overTimeList = mnAndOverTimeList.get(mn);
                } else {
                    overTimeList = new ArrayList<>();
                }
                overTimeList.add(monitorTimeString);
                mnAndOverTimeList.put(mn, overTimeList);
            }


            if (mnAndTimeAndValue.containsKey(mn)) {
                timeAndValueList = mnAndTimeAndValue.get(mn);
            } else {
                timeAndValueList = new ArrayList<>();
            }
            timeAndValueList.add(timeAndValue);
            mnAndTimeAndValue.put(mn, timeAndValueList);
            if (IsOverStandard) {
                mnAndIsOver.put(mn, IsOverStandard);
            }

        }
        for (String mnKey : dgimns) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("dgimn", mnKey);
            map.put("isoverstandard", mnAndIsOver.get(mnKey) != null ? mnAndIsOver.get(mnKey) : false);
            map.put("monitordatalist", mnAndTimeAndValue.get(mnKey));
            map.put("overtimelist", mnAndOverTimeList.get(mnKey));
            dataList.add(map);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/8/9 0009 上午 10:20
     * @Description: 企业日排放量汇总统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> sumPollutionDayFlowData(List<Document> documents, String collection, String pollutantcode, Map<String, Set<String>> pollutionIdAndMN) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<String> mns;
        String mn;
        List<Map<String, Object>> pollutantList;
        Double totalFlow;
        Double subFlow;
        for (String pollutionId : pollutionIdAndMN.keySet()) {
            totalFlow = 0d;
            mns = pollutionIdAndMN.get(pollutionId);
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                if (mns.contains(mn)) {
                    pollutantList = (List<Map<String, Object>>) document.get("DayFlowDataList");
                    for (Map<String, Object> map : pollutantList) {
                        if (pollutantcode.equals(map.get("PollutantCode"))) {
                            subFlow = map.get("CorrectedFlow") != null ? Double.parseDouble(map.get("CorrectedFlow").toString()) : 0d;
                            totalFlow += subFlow;
                            break;
                        }
                    }
                }
            }
            Map<String, Object> pollutionIdAndFlow = new HashMap<>();
            if (totalFlow > 0) {
                pollutionIdAndFlow.put("pollutionid", pollutionId);
                pollutionIdAndFlow.put("totalflow", totalFlow);
                dataList.add(pollutionIdAndFlow);
            }

        }
        return dataList;
    }

    public static List<Map<String, Object>> getStationHourDataList(List<Document> documents, List<Map<String, Object>> pollutants) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //风向
        String directionCode = WindDirectionEnum.getCode();
        String directionName = WindDirectionEnum.getName();
        List<String> allCodes = CommonTypeEnum.WeatherPollutionEnum.getAllCodes();

        for (Document document : documents) {
            List<Map<String, Object>> hourDataList = (List<Map<String, Object>>) document.get("HourDataList");
            for (Map<String, Object> map : hourDataList) {
                String pollutantcode = map.get("PollutantCode") == null ? "" : map.get("PollutantCode").toString();
                if (!allCodes.contains(pollutantcode)) {
                    continue;
                }
                Double avgStrength = map.get("AvgStrength") == null ? null : Double.valueOf(map.get("AvgStrength").toString());
                String unit = "";
                String pollutantname = "";
                Optional<String> unitmax = pollutants.stream().filter(m -> m.get("code") != null && m.get("unit") != null && pollutantcode.equals(m.get("code").toString())).
                        map(m -> m.get("unit").toString()).max(Comparator.comparing(m -> m));
                Optional<String> namemax = pollutants.stream().filter(m -> m.get("code") != null && m.get("name") != null && pollutantcode.equals(m.get("code").toString())).
                        map(m -> m.get("name").toString()).max(Comparator.comparing(m -> m));
                if (unitmax.isPresent()) {
                    unit = unitmax.get();
                }
                if (namemax.isPresent()) {
                    pollutantname = namemax.get();
                }
                map.clear();
                //风向
                if (avgStrength != null && pollutantcode.equals(directionCode)) {
                    String name = DataFormatUtil.windDirectionSwitch(avgStrength, "name");
                    map.put("pollutantcode", pollutantcode);
                    map.put("pollutantname", directionName);
                    map.put("value", name);
                } else {
                    map.put("pollutantcode", pollutantcode);
                    map.put("pollutantname", pollutantname);
                    map.put("value", avgStrength + unit);
                }
                resultList.add(map);
            }
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }
}