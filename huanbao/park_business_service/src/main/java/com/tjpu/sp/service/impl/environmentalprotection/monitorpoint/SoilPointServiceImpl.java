package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.SoilPointMapper;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.SoilPointService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author: liyc
 * @date:2019/12/17 0017 11:38
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class SoilPointServiceImpl implements SoilPointService {
    @Autowired
    private SoilPointMapper soilPointMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: liyc
     * @date: 2019/12/17 0017 11:42
     * @Description: 导出土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     **/
    @Override
    public List<Map<String, Object>> getTableTitleForSafety() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"监测点名称", "数采仪MN号", "测点控制级别", "中心经度", "中心纬度", "点位类型"};
        String[] titlefiled = new String[]{"MonitorPointName", "DGIMN", "controllevename", "Longitude", "Latitude", "SoilPointTypeName"};
        for (int i = 0; i < titlefiled.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", titlefiled[i]);
            map.put("label", titlename[i]);
            map.put("align", "center");
            tableTitleData.add(map);
        }
        return tableTitleData;
    }


    /**
     * @author: liyc
     * @date: 2019/12/17 0017 11:56
     * @Description: 通过自定义参数获取地下水监测点信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonObject]
     * @throws:
     **/
    @Override
    public List<Map<String, Object>> getSoilPointByParamMap(Map<String, Object> paramMap) {
        return soilPointMapper.getSoilPointByParamMap(paramMap);
    }

    /**
     * @author: lip
     * @date: 2020/3/24 0024 上午 8:36
     * @Description: 获取所有土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllSoilPointInfo() {
        return soilPointMapper.getAllSoilPointInfo();
    }

    /**
     * @author: lip
     * @date: 2020/3/24 0024 上午 9:11
     * @Description: 自定义查询条件获取土壤污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getSoilPollutantsByParam(Map<String, Object> paramMap) {
        return soilPointMapper.getSoilPollutantsByParam(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 2:41
     * @Description: 通过自定义参数获取土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getSoilPointInfoByParamMap(Map<String, Object> paramMap) {
        return soilPointMapper.getSoilPointInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/05/20 0020 下午 2:29
     * @Description: 通过自定义参数获取土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getSoilPointsAndPollutantStandard(Map<String, Object> paramMap) {
        return soilPointMapper.getSoilPointsAndPollutantStandard(paramMap);
    }

    /**
     * @author: lip
     * @date: 2020/6/2 0002 下午 1:54
     * @Description: 土壤相关性分析
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getRelationListDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (outPuts.size() > 0) {
            Map<String, Object> mnAndOutputid = new HashMap<>();
            Map<String, Object> mnAndOutputname = new HashMap<>();
            Map<String, Object> mnAndPollutionname = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String mnKey = "";
            for (Map<String, Object> output : outPuts) {
                if (output.get("dgimn") != null) {
                    mnKey = output.get("dgimn").toString();
                    mnAndOutputid.put(mnKey, output.get("pk_id"));
                    mnAndOutputname.put(mnKey, output.get("outputname"));
                    mnAndPollutionname.put(mnKey, output.get("pollutionname"));
                    mns.add(mnKey);
                }
            }
            paramMap.put("mns", mns);
            Map<String, Object> pointTimeAndValue = new LinkedHashMap<>();
            Map<String, Map<String, Object>> mnAndTimeAndValue = new LinkedHashMap<>();
            setTimeAndValue(pointTimeAndValue, mnAndTimeAndValue, paramMap);
            Set<String> mnOutputSet = mnAndTimeAndValue.keySet();
            Double relationpercent;
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (pointTimeAndValue.size() > 0) {
                for (String mnkey : mnOutputSet) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutionname", mnAndPollutionname.get(mnkey));
                    dataMap.put("outputname", mnAndOutputname.get(mnkey));
                    dataMap.put("outputid", mnAndOutputid.get(mnkey));
                    relationpercent = getRelationPercent(pointTimeAndValue, mnAndTimeAndValue.get(mnkey), paramMap);
                    if (relationpercent != null) {
                        dataMap.put("relationpercent", DataFormatUtil.SaveTwoAndSubZero(relationpercent));
                        dataList.add(dataMap);
                    }
                }
            }
            //根据相关度倒序
            Collections.sort(dataList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Double one = Double.parseDouble(o1.get("relationpercent").toString());
                    Double other = Double.parseDouble(o2.get("relationpercent").toString());
                    Double one1 = Math.abs(one);
                    Double other1 = Math.abs(other);
                    return other1.compareTo(one1);
                }
            });
            //处理分页数据
            int total = dataList.size();
            if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
                dataList = getPageData(dataList, Integer.parseInt(paramMap.get("pagenum").toString()),
                        Integer.parseInt(paramMap.get("pagesize").toString()));
            }
            resultMap.put("tablelistdata", dataList);
            resultMap.put("total", total);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getSoilRelationChartDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (outPuts.size() > 0) {
            Map<String, Object> mnAndOutputid = new HashMap<>();
            Map<String, Object> mnAndOutputname = new HashMap<>();
            Map<String, Object> mnAndPollutionname = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String mnKey = "";
            for (Map<String, Object> output : outPuts) {
                if (output.get("dgimn") != null) {
                    mnKey = output.get("dgimn").toString();
                    mnAndOutputid.put(mnKey, output.get("pk_id"));
                    mnAndOutputname.put(mnKey, output.get("outputname"));
                    mnAndPollutionname.put(mnKey, output.get("pollutionname"));
                    mns.add(mnKey);
                }
            }
            paramMap.put("mns", mns);
            Map<String, Object> pointTimeAndValue = new LinkedHashMap<>();
            Map<String, Map<String, Object>> mnAndTimeAndValue = new LinkedHashMap<>();
            setTimeAndValue(pointTimeAndValue, mnAndTimeAndValue, paramMap);
            List<Map<String, Object>> xListData = new ArrayList<>();
            List<Map<String, Object>> yListData = new ArrayList<>();
            Map<String, Object> outputTimeAndValue = mnAndTimeAndValue.get(mnKey);
            List<Double> xData = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            for (String time : pointTimeAndValue.keySet()) {
                if (outputTimeAndValue.get(time) != null) {
                    Map<String, Object> xMap = new LinkedHashMap<>();
                    Map<String, Object> yMap = new LinkedHashMap<>();
                    xMap.put("monitortime", time);
                    xMap.put("value", Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    yMap.put("monitortime", time);
                    yMap.put("value", Double.parseDouble(outputTimeAndValue.get(time).toString()));
                    yData.add(Double.parseDouble(outputTimeAndValue.get(time).toString()));
                    xData.add(Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    xListData.add(xMap);
                    yListData.add(yMap);
                }
            }
            if (xData.size()>0&&yData.size()>0){
                Double xMax = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(Collections.max(xData)));
                Double yMax = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(Collections.max(yData)));
                Double slope = DataFormatUtil.getRelationSlope(xData, yData);
                Double constant = DataFormatUtil.getRelationConstant(xData, yData, slope);
                resultMap.put("slope", slope);
                resultMap.put("constant", constant);
                if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
                    resultMap.put("total", xListData.size());
                    Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
                    Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
                    xListData = getPageData(xListData, pagenum, pagesize);
                    yListData = getPageData(yListData, pagenum, pagesize);
                }
                resultMap.put("xlistdata", xListData);
                resultMap.put("ylistdata", yListData);
                resultMap.put("startPointData", Arrays.asList(0, yMax));
                Double y = slope * xMax + constant;
                y = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(y));
                resultMap.put("endPointData", Arrays.asList(xMax, y));
            }


        }
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getSoilPointsAndPollutantStandardInfo(Map<String, Object> paramMap) {
        return soilPointMapper.getSoilPointsAndPollutantStandardInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getEntSoilPointByParamMap(Map<String, Object> paramMap) {
        return soilPointMapper.getEntSoilPointByParamMap(paramMap);
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
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 上午 11:26
     * @Description: 计算两个数组相关度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Double getRelationPercent(Map<String, Object> pointTimeAndValue, Map<String, Object> outputTimeAndValue, Map<String, Object> paramMap) {
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        for (String time : pointTimeAndValue.keySet()) {
            if (outputTimeAndValue.get(time) != null) {
                xData.add(Double.parseDouble(outputTimeAndValue.get(time).toString()));
                yData.add(Double.parseDouble(pointTimeAndValue.get(time).toString()));
            }
        }
        Double relationpercent = DataFormatUtil.getRelationPercent(xData, yData);
        return relationpercent;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 下午 2:31
     * @Description: 设置监测点的时间+值，设置mn+时间+值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    private void setTimeAndValue(Map<String, Object> pointTimeAndValue, Map<String, Map<String, Object>> mnAndTimeAndValue, Map<String, Object> paramMap) {
        //获取废气mn数组、废气排口名称，企业名称对照关系
        String mnKey = "";
        String collection = paramMap.get("collection").toString();
        //获取废气前n个时间的指定污染物监测数据
        Map<String, Object> gasParam = new HashMap<>();
        gasParam.put("starttime", paramMap.get("starttime"));
        gasParam.put("endtime", paramMap.get("endtime"));
        gasParam.put("mns", paramMap.get("mns"));
        gasParam.put("collection", collection);
        gasParam.put("sort", "asc");
        gasParam.put("pollutantcodes", Arrays.asList(paramMap.get("outputpollutant")));
        Query query = setNoGroupQuery(gasParam);
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        if (documents.size() > 0) {
            String monitortime;
            String pollutantDataKey = "DayDataList";
            String valueKey = "AvgStrength";
            Map<String, Object> timeAndValue;
            List<Document> pollutantData;
            String outputpollutant;
            for (Document document : documents) {
                mnKey = document.getString("DataGatherCode");
                monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (mnAndTimeAndValue.get(mnKey) != null) {
                    timeAndValue = mnAndTimeAndValue.get(mnKey);
                } else {
                    timeAndValue = new LinkedHashMap<>();
                }
                pollutantData = document.get(pollutantDataKey, List.class);
                for (Document pollutant : pollutantData) {
                    if (pollutant.get(valueKey) != null && !"".equals(pollutant.get(valueKey))) {
                        timeAndValue.put(monitortime, pollutant.get(valueKey));
                    }
                    break;
                }
                mnAndTimeAndValue.put(mnKey, timeAndValue);
            }
            //获取监测点指定时间指定污染物监测数据
            Map<String, Object> pointParam = new HashMap<>();
            pointParam.put("starttime", paramMap.get("starttime"));
            pointParam.put("endtime", paramMap.get("endtime"));
            pointParam.put("mns", Arrays.asList(paramMap.get("monitorpointid")));
            pointParam.put("collection", collection);
            pointParam.put("sort", "asc");
            pointParam.put("pollutantcodes", Arrays.asList(paramMap.get("monitorpointpollutant")));
            query = setNoGroupQuery(pointParam);
            List<Document> pointDocuments = mongoTemplate.find(query, Document.class, collection);
            if (pointDocuments.size() > 0) {
                outputpollutant = paramMap.get("monitorpointpollutant").toString();
                for (Document point : pointDocuments) {
                    monitortime = DataFormatUtil.getDateYMD(point.getDate("MonitorTime"));
                    pollutantData = (List<Document>) point.get(pollutantDataKey);
                    for (Document pollutant : pollutantData) {
                        if (pollutant.get("PollutantCode").equals(outputpollutant)) {
                            if (pollutant.get(valueKey) != null && !"".equals(pollutant.get(valueKey))) {
                                pointTimeAndValue.put(monitortime, pollutant.get(valueKey));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param paramMap
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:11
     * @Description: 处理不分组查询条件的私有方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Query setNoGroupQuery(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                query.addCriteria(Criteria.where("MonitorTime").gte(startDate));
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("MonitorTime").lte(endDate));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("DayDataList.PollutantCode").in(pollutantcodes));
        }

        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime","DataGatherCode"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime","DataGatherCode"));
        }


        return query;
    }


}
