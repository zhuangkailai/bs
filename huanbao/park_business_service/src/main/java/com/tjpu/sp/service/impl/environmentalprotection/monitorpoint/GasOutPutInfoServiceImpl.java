package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GasOutPutInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GasOutPutPollutantSetMapper;
import com.tjpu.sp.dao.environmentalprotection.particularpollutants.ParticularPollutantsMapper;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GASOutPutInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class GasOutPutInfoServiceImpl implements GasOutPutInfoService {


    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;

    @Autowired
    private PollutionMapper pollutionMapper;

    @Autowired
    private ParticularPollutantsMapper particularPollutantsMapper;

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    private final String dayflow = "DayFlowData";

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 下午 2:10
     * @Description: 通过id查询废气排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public GASOutPutInfoVO selectByPrimaryKey(String pkId) {
        return gasOutPutInfoMapper.selectByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 下午 2:10
     * @Description: 通过id删除废气排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        return gasOutPutInfoMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 下午 2:10
     * @Description: 通过废气排口实体添加废气排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insertSelective(GASOutPutInfoVO record) {
        return gasOutPutInfoMapper.insertSelective(record);
    }


    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.countTotalByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description:获取废气污染源下排放口及状态信息，并组合成污染源排口树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     * @param paramMap
     */
    @Override
    public List<Map<String, Object>> getPollutionGasOuputsAndStatus(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        if (dataList.size() > 0) {
            List<Map<String, Object>> dataListTemp = new ArrayList<>();
            Set<String> tempSet = new HashSet<>();
            for (Map<String, Object> map : dataList) {
                if (!tempSet.contains(map.get("pk_pollutionid").toString())) {
                    tempSet.add(map.get("pk_pollutionid").toString());
                    Map<String, Object> mapTemp = new HashMap<>();
                    mapTemp.put("shortername", map.get("shortername"));
                    mapTemp.put("pk_pollutionid", map.get("pk_pollutionid"));
                    List<Map<String, Object>> outputdata = new ArrayList<>();
                    Map<String, Object> outputMap = new HashMap<>();
                    outputMap.put("pk_id", map.get("pk_id"));
                    outputMap.put("outputname", map.get("outputname"));
                    outputMap.put("dgimn", map.get("dgimn"));
                    outputMap.put("monitorpointtype", map.get("monitorpointtype"));
                    outputMap.put("onlinestatus", map.get("onlinestatus"));
                    outputMap.put("longitude", map.get("Longitude"));
                    outputMap.put("latitude", map.get("Latitude"));
                    outputMap.put("stackheight", map.get("StackHeight"));
                    outputMap.put("status", map.get("status"));
                    outputdata.add(outputMap);
                    mapTemp.put("outputdata", outputdata);
                    dataListTemp.add(mapTemp);
                } else {
                    for (Map<String, Object> mapTemp : dataListTemp) {
                        if (mapTemp.get("pk_pollutionid").equals(map.get("pk_pollutionid"))) {
                            List<Map<String, Object>> outputdata = (List<Map<String, Object>>) mapTemp.get("outputdata");
                            Map<String, Object> outputMap = new HashMap<>();
                            outputMap.put("pk_id", map.get("pk_id"));
                            outputMap.put("outputname", map.get("outputname"));
                            outputMap.put("dgimn", map.get("dgimn"));
                            outputMap.put("monitorpointtype", map.get("monitorpointtype"));
                            outputMap.put("onlinestatus", map.get("onlinestatus"));
                            outputMap.put("longitude", map.get("Longitude"));
                            outputMap.put("latitude", map.get("Latitude"));
                            outputMap.put("stackheight", map.get("StackHeight"));
                            outputMap.put("status", map.get("status"));
                            outputdata.add(outputMap);
                            mapTemp.put("outputdata", outputdata);
                            break;
                        }
                    }
                }

            }
            dataList = dataListTemp;
        }
        return dataList;
    }



//    /**
//     * @author: chengzq
//     * @date: 2019/5/24 0024 下午 1:57
//     * @Description: 通过自定义参数获取废气排口列表信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [paramMap]
//     * @throws:
//     */
//    @Override
//    public List<Map<String, Object>> getGasOutPutListByParamMap(Map<String, Object> paramMap) {
//        return gasOutPutInfoMapper.getGasOutPutListByParamMap(paramMap);
//    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 8:04
     * @Description: 获取在线废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineGasOutPutInfoByParamMap(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 9:09
     * @Description: 获取所有dgimn
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllOutPutInfo(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.getAllOutPutInfo(paramMap);
    }

//    /**
//     * @author: lip
//     * @date: 2019/6/13 0013 下午 5:28
//     * @Description: 获取废气排口分页数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    @Override
//    public PageInfo<Map<String, Object>> getOnlineGasOutPutInfoByParamMapForPage(Integer pageSize, Integer pageNum, Map<String, Object> paramMap) {
//        if (pageSize != null && pageNum != null) {
//            PageHelper.startPage(pageNum, pageSize);
//        }
//        List<Map<String, Object>> listData = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
//        return new PageInfo<>(listData);
//    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:29
     * @Description: 组装污染源、排口、监测污染物、特征污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> setGasOutPutAndPollutantDetail(List<Map<String, Object>> detailDataTemp, String id) {

        List<Map<String, Object>> detailData = new ArrayList<>();
        String pollutionid = "";
        for (Map<String, Object> map : detailDataTemp) {
            if ("fk_pollutionid".equals(map.get("fieldname").toString().toLowerCase())) {
                pollutionid = (String) map.get("value");
            }
        }
        //添加污染源信息
        detailData.add(pollutionMap(pollutionid));
        int num = 1;
        List<String> noInList = Arrays.asList("updatetime", "updateuser", "remark");
        //添加排口信息
        for (Map<String, Object> map : detailDataTemp) {
            if (!noInList.contains(map.get("fieldname").toString().toLowerCase())) {
                num = num + 1;
                if (num == 4) {
                    map.put("width", "50%");
                }
                map.put("ordernum", num);
                detailData.add(map);
            }
        }
        //添加特征污染物信息
        num = num + 1;
        detailData.add(getParticularPollutant(pollutionid, id, num));
        //添加监测污染物信息
        num = num + 1;
        detailData.add(getMonitorPollutant(pollutionid, id, num));
        return detailData;
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:52
     * @Description: 获取监测污染物信息详情格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getMonitorPollutant(String pollutionid, String id, int num) {
        Map<String, Object> paramMap = new HashMap<>();
        String monitorpollutants = "";
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("outputid", id);
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
        List<Map<String, Object>> monitorPollutant = pollutantFactorMapper.getPollutantSetInfoByParamMap(paramMap);
        if (monitorPollutant.size() > 0) {
            for (Map<String, Object> pollutant : monitorPollutant) {
                monitorpollutants += pollutant.get("pollutantname") + "、";
            }
            if (StringUtils.isNotBlank(monitorpollutants)) {
                monitorpollutants = monitorpollutants.substring(0, monitorpollutants.length() - 1);
            }
        } else {
            monitorpollutants = "";
        }
        paramMap.clear();
        paramMap.put("fieldname", "monitorpollutants");
        paramMap.put("controltype", "");
        paramMap.put("width", "50%");
        paramMap.put("showhide", true);
        paramMap.put("ordernum", num);
        paramMap.put("label", "监测污染物");
        paramMap.put("type", "string");
        paramMap.put("value", monitorpollutants);
        return paramMap;

    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:48
     * @Description: 获取特征污染物信息详情格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getParticularPollutant(String pollutionid, String id, int num) {
        Map<String, Object> paramMap = new HashMap<>();
        String particularpollutants = "";
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("outputid", id);
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
        List<Map<String, Object>> particularPollutant = particularPollutantsMapper.getLastVersionPollutantInfoByParamMap(paramMap);
        if (particularPollutant.size() > 0) {
            for (Map<String, Object> pollutant : particularPollutant) {
                particularpollutants += pollutant.get("pollutantname") + "、";
            }
            if (StringUtils.isNotBlank(particularpollutants)) {
                particularpollutants = particularpollutants.substring(0, particularpollutants.length() - 1);
            }
        } else {
            particularpollutants = "";
        }
        paramMap.clear();
        paramMap.put("fieldname", "particularpollutants");
        paramMap.put("controltype", "");
        paramMap.put("width", "50%");
        paramMap.put("showhide", true);
        paramMap.put("ordernum", num);
        paramMap.put("label", "特征污染物");
        paramMap.put("type", "string");
        paramMap.put("value", particularpollutants);
        return paramMap;
    }

    private Map<String, Object> pollutionMap(String pollutionid) {
        PollutionVO pollutionVO = pollutionMapper.selectByPrimaryKey(pollutionid);
        Map<String, Object> map = new HashMap<>();
        map.put("fieldname", "pollutionname");
        map.put("controltype", "");
        map.put("width", "100%");
        map.put("showhide", true);
        map.put("ordernum", 1);
        map.put("label", "企业名称");
        map.put("type", "string");
        map.put("value", pollutionVO.getPollutionname());
        return map;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:21
     * @Description: 获取所有已监测废气排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorGasOutPutAndStatusInfo() {
        return gasOutPutInfoMapper.getAllMonitorGasOutPutAndStatusInfo();
    }

//    /**
//     * @author: chengzq
//     * @date: 2019/6/25 0025 下午 2:56
//     * @Description: 通过排口名称，污染源id查询排口
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [params]
//     * @throws:
//     */
//    @Override
//    public Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> params) {
//        return gasOutPutInfoMapper.selectByPollutionidAndOutputName(params);
//    }

    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 4:00
     * @Description: 通过污染源id获取排口名称和id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectByPollutionid(String pollutionid) {
        return gasOutPutInfoMapper.selectByPollutionid(pollutionid);
    }

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 5:03
     * @Description: 获取废气排放量许可预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [allflowvalues，allMN，year]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getFlowPermitListData(List<Map<String, Object>> allflowvalues, List<Map<String, Object>> allMN, String year) {
        List<String> mnlist = new ArrayList<String>();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        for (Map<String, Object> mnmap : allMN) {
            String mn = mnmap.get("DGIMN") != null ? mnmap.get("DGIMN").toString() : "";
            if (!"".equals(mn)) {
                mnlist.add(mn);
            }
        }
        Calendar cale = Calendar.getInstance();
        int newyear = cale.get(Calendar.YEAR);
        Date startDate = null;
        Date endDate = null;
        String surplusday = "";
        //判断年份是否为当前年
        if (newyear - Integer.parseInt(year) > 0) {//历史数据
            startDate = DataFormatUtil.getDateYMDHMS(year + "-01-01  00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(year + "-12-31  00:00:00");
        } else if (newyear - Integer.parseInt(year) == 0) {//当前年
            startDate = DataFormatUtil.getDateYMDHMS(year + "-01-01  00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(year + "-" + cale.get(Calendar.MONTH) + 1 + "-" + cale.get(Calendar.DATE) + "  00:00:00");
            //获取今年剩余天数
            Calendar calendar = Calendar.getInstance();
            LocalDate localDate = LocalDate.ofYearDay(calendar.get(Calendar.YEAR), 1);
            int dayCount = localDate.isLeapYear() ? 366 : 365;
            surplusday = dayCount - calendar.get(Calendar.DAY_OF_YEAR) + "";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, dayflow);
        Set set = new HashSet();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Map<String, Object> flowmap : allflowvalues) {
                String pollutionid = flowmap.get("FK_Pollutionid").toString();
                set.add(pollutionid);
                String pollutantcode = flowmap.get("FK_PollutantCode").toString();
                String totalFlow = flowmap.get("TotalFlow").toString();
                Map<String, Object> resultmap = new HashMap<>();
                resultmap = getFlowValueData(pollutionid, allMN, documents, pollutantcode);
                resultmap.put("pollutionname", flowmap.get("pollutionname"));
                resultmap.put("surplusday", surplusday);
                resultmap.put("pollutantname", flowmap.get("pollutantname"));
                ;
                resultmap.put("totalFlow", DataFormatUtil.subZeroAndDot(totalFlow));
                String dayvalue = "";
                if (newyear - Integer.parseInt(year) > 0) {//历史数据
                    resultmap.put("surplusflowday", "");
                    resultlist.add(resultmap);
                } else if (newyear - Integer.parseInt(year) == 0) {//当前年
                    if (!"".equals(resultmap.get("umulativeflow").toString())) {
                        int flowpermitday = (int) ((Double.parseDouble(totalFlow) - Double.parseDouble(resultmap.get("umulativeflow").toString())) / Double.parseDouble(resultmap.get("avgdayflow").toString()));
                        dayvalue = flowpermitday + "";
                        resultmap.put("surplusflowday", flowpermitday + "");
                        if (flowpermitday <= Integer.parseInt(surplusday)) {
                            resultlist.add(resultmap);
                        }
                    }/*else{//如果该公司今年还未未排放该因子
                        resultmap.put("surplusflowday",surplusday);
                    }*/

                }

            }
        }
        return resultlist;
    }

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取废气排放量许可预警列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForGasPermittedFlowEarly() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Map<String, Object> monitormap = new HashMap<>();
        monitormap.put("minwidth", "180px");
        monitormap.put("headeralign", "center");
        monitormap.put("fixed", "left");
        monitormap.put("showhide", true);
        monitormap.put("prop", "pollutionname");
        monitormap.put("label", "企业名称");
        monitormap.put("align", "center");
        tableTitleData.add(monitormap);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("minwidth", "180px");
        map2.put("headeralign", "center");
        map2.put("fixed", "left");
        map2.put("showhide", true);
        map2.put("prop", "pollutantname");
        map2.put("label", "污染物");
        map2.put("align", "center");
        tableTitleData.add(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("minwidth", "180px");
        map3.put("headeralign", "center");
        map3.put("fixed", "left");
        map3.put("showhide", true);
        map3.put("prop", "umulativeflow");
        map3.put("label", "当前累计排放量(t)");
        map3.put("align", "center");
        tableTitleData.add(map3);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("minwidth", "180px");
        map4.put("headeralign", "center");
        map4.put("fixed", "left");
        map4.put("showhide", true);
        map4.put("prop", "totalFlow");
        map4.put("label", "许可排放量(t)");
        map4.put("align", "center");
        tableTitleData.add(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("minwidth", "180px");
        map5.put("headeralign", "center");
        map5.put("fixed", "left");
        map5.put("showhide", true);
        map5.put("prop", "avgdayflow");
        map5.put("label", "日均排放量(t)");
        map5.put("align", "center");
        tableTitleData.add(map5);
        Map<String, Object> map6 = new HashMap<>();
        map6.put("minwidth", "180px");
        map6.put("headeralign", "center");
        map6.put("fixed", "left");
        map6.put("showhide", true);
        map6.put("prop", "surplusflowday");
        map6.put("label", "剩余可排放天数");
        map6.put("align", "center");
        tableTitleData.add(map6);
        Map<String, Object> map7 = new HashMap<>();
        map7.put("minwidth", "180px");
        map7.put("headeralign", "center");
        map7.put("fixed", "left");
        map7.put("showhide", true);
        map7.put("prop", "surplusday");
        map7.put("label", "日历剩余天数");
        map7.put("align", "center");
        tableTitleData.add(map7);
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 1:10
     * @Description: 根据监测点类型获取浓度阈值预警列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForGasConcentrationThresholdEarly(Integer type) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = null;
        String[] titlefiled = null;
        if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode())) {//废气,废水，雨水
            titlename = new String[]{"企业名称", "排口名称", "数据类型", "监测时间", "污染物", "监测值", "预警限值", "标准值"};
            titlefiled = new String[]{"pollutionname", "outputname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "concenalarmmaxvalue", "standardmaxvalue"};
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode())) {//厂界小型站
            titlename = new String[]{"企业名称", "监测点名称", "数据类型", "监测时间", "污染物", "监测值", "预警限值", "标准值"};
            titlefiled = new String[]{"pollutionname", "monitorpointname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "concenalarmmaxvalue", "standardmaxvalue"};
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode())) {//空气站
            titlename = new String[]{"监测点名称", "数据类型", "监测时间", "污染物", "监测值", "预警限值", "标准值"};
            titlefiled = new String[]{"monitorpointname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "concenalarmmaxvalue", "standardmaxvalue"};
        }
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

//    /**
//     * @param paramMap
//     * @author: zhangzc
//     * @date: 2019/7/10 14:00
//     * @Description: 获取废气排口相关污染物和企业信息(废气排放量突变预警涉及的企业排口污染物信息)
//     * @param:
//     * @return:
//     */
//    @Override
//    public List<Map<String, Object>> getPollutionGasOutPutPollutants(Map<String, Object> paramMap) {
//        return gasOutPutInfoMapper.getPollutionGasOutPutPollutants(paramMap);
//    }

    @Override
    public List<Map<String, Object>> getGasOutPutAndStatusByParamMap(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2019/7/9 8:43
     * @Description: 统计计算排放量许可信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getFlowValueData(String pollutionid, List<Map<String, Object>> allMN, List<Document> documents, String pollutantcode) {
        Map<String, Object> resultmap = new HashMap<>();
        int num = 0;
        Double total = 0d;
        for (Map<String, Object> mnmap : allMN) {
            //找到相同企业
            if (mnmap.get("FK_Pollutionid") != null && pollutionid.equals(mnmap.get("FK_Pollutionid").toString())) {
                //遍历MongoDB查询结果
                for (Document document : documents) {
                    //找到相同MN号
                    if (mnmap.get("DGIMN").equals(document.getString("DataGatherCode"))) {//当MN号相等时
                        //获取污染物集合
                        List<Map<String, Object>> pollutantDataList = document.get("DayFlowDataList", List.class);
                        //遍历污染物集合
                        if (pollutantDataList != null && pollutantDataList.size() > 0) {
                            for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                                if (pollutantcode.equals(dataMap.get("PollutantCode")) && dataMap.get("CorrectedFlow") != null) {
                                    Double correctedFlow = Double.parseDouble(dataMap.get("CorrectedFlow").toString());
                                    num += 1;
                                    total += correctedFlow;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (num > 0) {
            resultmap.put("avgdayflow", total / num);
            resultmap.put("umulativeflow", total);
        } else {
            resultmap.put("avgdayflow", "");
            resultmap.put("umulativeflow", "");
        }
        return resultmap;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 上午 11:35
     * @Description: 查询所有废水废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllWaterOutputAndGasOutputInfo() {
        return gasOutPutInfoMapper.getAllWaterOutputAndGasOutputInfo();
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:25
     * @Description: gis-获取所有废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllMonitorGasOutPutInfo(Map<String, Object> pollutionMap) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderfield", "status");
        List<Map<String, Object>> gasdata = gasOutPutInfoMapper.getAllMonitorGasOutPutInfo(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (gasdata != null && gasdata.size() > 0) {
            for (Map<String, Object> map : gasdata) {
                int status = 0;
                if (map.get("OnlineStatus") != null && !"".equals(map.get("OnlineStatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("OnlineStatus").toString())) {//有在线排口，有一个在线排口，则该企业为在线企业
                        status = 1;
                    } else {
                        if (status < Integer.parseInt(map.get("OnlineStatus").toString())) {
                            status = Integer.parseInt(map.get("OnlineStatus").toString());
                        }
                    }
                }
                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                map.put("OnlineStatus", status);
            }
        }
        result.put("total", (gasdata != null && gasdata.size() > 0) ? gasdata.size() : 0);
        result.put("pollutiontotal", pollutionMap.get("total"));
        result.put("gaspollution", pollutionMap.get("gaspollution"));
        result.put("onlinepollution", pollutionMap.get("onlinepollution"));
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", gasdata);
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 3:22
     * @Description: 通过自定义参数查询排口和污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOutPutAndPollutionInfoByParams(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.getOutPutAndPollutionInfoByParams(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 2:58
     * @Description: 通过味道code和mn号集合查询包含这种味道的企业和排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectGasInfoBySmellCodeAndMns(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.selectGasInfoBySmellCodeAndMns(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:23
     * @Description: 删除垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public int deleteGarbageData() {
        return gasOutPutInfoMapper.deleteGarbageData();
    }


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 4:59
     * @Description: 获取企业和监测点树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, String>> getMonitorPointAndPollutionTree(Map<String,Object> paramMap) {
        return gasOutPutInfoMapper.getMonitorPointAndPollutionTree(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/2/14 0014 下午 1:30
     * @Description: 获取企业和监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, String>> getMonitorPointAndPollutionInfo(Map<String, Object> paramMap) {
        return gasOutPutInfoMapper.getMonitorPointAndPollutionInfo(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/2/17 0017 下午 14:53
     * @Description: 获取所有烟气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorSmokeOutPutAndStatusInfo() {
        return gasOutPutInfoMapper.getAllMonitorSmokeOutPutAndStatusInfo();
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 上午 09:57
     * @Description: 根据监测点ID删除该点位设置的报警污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteGasOutPutPollutantByID(String id) {
        //删除污染物
        gasOutPutPollutantSetMapper.deleteGasOutPutPollutantByID(id);
        //删除配置的限值
        gasOutPutPollutantSetMapper.deleteGasOutPutEarlyWarningSetByID(id);
    }
}
