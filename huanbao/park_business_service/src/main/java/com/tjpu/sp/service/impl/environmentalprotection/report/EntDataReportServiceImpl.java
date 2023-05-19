package com.tjpu.sp.service.impl.environmentalprotection.report;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.SessionUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.service.environmentalprotection.report.EntDataReportService;
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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EntDataReportServiceImpl implements EntDataReportService {

    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private WaterOutputInfoMapper waterOutPutInfoMapper;
    @Autowired
    private KeyMonitorPollutantMapper keyMonitorPollutantMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private PollutionMapper pollutionMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;
    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 8:42
     * @Description:根据报表类型和自定义参数获取某个企业的企业报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getEntDataReportByParamMap(Map<String, Object> paramMap) {
        try {
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            //监测点类型
            String pointtype = paramMap.get("pointtype").toString();
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            //标题名称
            String titlename = "";
            if ("2".equals(tabletitletype)) {
                titlename = paramMap.get("pollutionname").toString();
            }
            //去mongo中查询折算数据
            Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
            Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
            Integer watermonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            List<String> lltypes = new ArrayList<>();
            lltypes.add(gasmonitortype + "");
            lltypes.add(smokemonitortype + "");
            lltypes.add(watermonitortype + "");
            //数据展示类型
            List<Integer> showtypes = new ArrayList<>();
            if (paramMap.get("showtypes") != null) {
                //数据展示类型
                showtypes = (List<Integer>) paramMap.get("showtypes");
            }
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            String monitortime = paramMap.get("monitortime") != null ? paramMap.get("monitortime").toString() : "";
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<String> flowpollutantlist = new ArrayList<>();
            Date startDate = null;
            Date endDate = null;
            String collection = "";
            String datalistname = "";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateTime = null;
            List<String> flags = new ArrayList<>();

            if ("day".equals(reporttype)) {//日报
                collection = "HourData";
                datalistname = "HourDataList";
                startDate = DataFormatUtil.getDateYMDH(monitortime + " 00");
                endDate = DataFormatUtil.getDateYMDH(monitortime + " 23");
            } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
                collection = "DayData";
                datalistname = "DayDataList";
                dateTime = simpleDateFormat.parse(monitortime + "-01 00:00:00");
                List<String> timelist = DataFormatUtil.getDayListOfMonth(dateTime);
                startDate = DataFormatUtil.getDateYMD(timelist.get(0));
                endDate = DataFormatUtil.getDateYMD(timelist.get(timelist.size() - 1));

                flags = Arrays.asList("N", "n");

            } else if ("year".equals(reporttype)) {//年报
                collection = "MonthData";
                datalistname = "MonthDataList";
                startDate = DataFormatUtil.getDateYMD(monitortime + "-01-01");
                endDate = DataFormatUtil.getDateYMD(monitortime + "-12-31");
            } else if ("custom".equals(reporttype)) {//自定义
                collection = "DayData";
                datalistname = "DayDataList";
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
            }
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //获取点位信息
            getKeyPollutantAndPointData(pollutions, paramMap, pointtype);
            //根据类型获取重点监测污染物
            if (paramMap.get("pollutantcodes") == null) {
                keypollutants = keyMonitorPollutantMapper.selectByPollutanttype(pointtype);
            }
            if (!CommonTypeEnum.getExcludeAuthTypeList().contains(pointtype)) {
                /*-----设置权限--------*/
                //设置权限 查询用户拥有权限的监测点dgimn
                String sessionID = SessionUtil.getSessionID();
                List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
                paramMap.put("categorys", categorys);
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                paramMap.put("userid", userid);
                List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
                List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

                //从查询出的监测点里筛选拥有权限的监测点
                pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
                /*-----设置权限end--------*/
            }

            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") == null ? map.get("dgimn").toString() : map.get("DGIMN").toString());
                if ("2".equals(tabletitletype)) {
                    titlename += map.get("OutputName") != null ? map.get("OutputName") : (map.get("outputname") != null ? map.get("outputname") : "");
                }
            }

            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    pollutantlist.add(map.get("FK_PollutantCode").toString());
                }
            }
            //获取因子标准值  判断页面数据是否超过标准值 显示颜色
            Map<String, Object> param = new HashMap<>();
            param.put("mnlist", mnlist);
            param.put("pollutantlist", pollutantlist);
            param.put("monitorpointtype", pointtype);
            List<Map<String, Object>> standlist = pollutantFactorMapper.getPollutantStandarddataByParam(param);
            //排放量污染物
            flowpollutantlist = getFlowPollutantListByParamMap(pollutantlist, pointtype);
            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getTableTitleForEntDataReport(pollutantlist, pointtype, flowpollutantlist, reporttype, showtypes, standlist);
            } else {
                tables = getExportTableTitleForEntDataReport(pollutantlist, pointtype, flowpollutantlist, reporttype, showtypes, standlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            if (flags.size() > 0) {
                query.addCriteria(Criteria.where(datalistname + ".Flag").in(flags));
            }
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);

            Map<String, Object> flag_codeAndName = new HashMap<>();

            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", Arrays.asList(pointtype));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
            //去mongo中查询浓度数据
            Map<String, Object> concentrations = getAllOutPutConcentrationData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
            Map<String, Object> flowmap = new HashMap<>();
            Map<String, Object> convertedvalues = new HashMap<>();
            //获取累计流量
            Map<String, Object> ll_map = new HashMap<>();
            if (lltypes.contains(pointtype)) {
                ll_map = getPointTotalFlowDataForSummaryReport(startDate, endDate, mnlist, reporttype);
            }
            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (Integer showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        flowmap = getAllOutPutFlowData(startDate, endDate, mnlist, flowpollutantlist, reporttype);
                    }
                    if (showtype == 1) {//折算值
                        if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气或烟气的时候，有折算值
                            convertedvalues = getAllOutPutConvertedValueData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
                        }
                    }
                }
            }
            Map<String, Object> datamap = new HashMap<>();
            datamap.put("concentrations", concentrations);
            datamap.put("convertedvalues", convertedvalues);
            datamap.put("flowmap", flowmap);
            datamap.put("monitortime", monitortime);
            datamap.put("standlist", standlist);
            datamap.put("tabletitletype", tabletitletype);
            datamap.put("starttime", starttime);
            datamap.put("endtime", endtime);
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            if ("day".equals(reporttype)) {//日报
                if ("2".equals(tabletitletype)) {
                    titlename += "日数据(" + monitortime + ")";
                }
                result = getEntDataDayReportData(datamap, mnlist, pollutantlist, pollutions, pointtype, ll_map);
            } else if ("week".equals(reporttype)) {//周报-
                if ("2".equals(tabletitletype)) {
                    titlename += "周数据(" + monitortime + ")";
                }
                result = getEntDataWeekReportData(datamap, mnlist, pollutantlist, pollutions, pointtype, ll_map);
            } else if ("month".equals(reporttype)) {//月报
                if ("2".equals(tabletitletype)) {
                    titlename += "月数据(" + monitortime + ")";
                }
                result = getEntDataMonthReportData(datamap, mnlist, pollutantlist, pollutions, pointtype, ll_map);
            } else if ("year".equals(reporttype)) {//年报
                if ("2".equals(tabletitletype)) {
                    titlename += "年数据(" + monitortime + ")";
                }
                result = getEntDataYearReportData(datamap, mnlist, pollutantlist, pollutions, pointtype, ll_map);
            } else if ("custom".equals(reporttype)) {//自定义
                if ("2".equals(tabletitletype)) {
                    titlename += "日数据(" + starttime + "到" + endtime + ")";
                }
                result = getEntCustomReportData(datamap, mnlist, pollutantlist, pollutions, pointtype, ll_map);
            }
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            if ("2".equals(tabletitletype)) {
                resultmap.put("titlename", titlename);
            }
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author: xsm
     * @date: 2019/9/3 0003 下午 1:39
     * @Description: 获取排放量污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<String> getFlowPollutantListByParamMap(List<String> pollutantlist, String pointtype) {
        List<String> codes = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutantcodes", pollutantlist);
        paramMap.put("pollutanttype", pointtype);
        List<Map<String, Object>> result = pollutantFactorMapper.getPollutantInfoByPollutanTypeAndCodes(paramMap);
        for (Map<String, Object> map : result) {
            codes.add(map.get("code").toString());
        }
        return codes;
    }


    /**
     * @param pollutantlist
     * @param showtypes
     * @param standlist
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取企业报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getTableTitleForEntDataReport(List<String> pollutantlist, String pointtype, List<String> flowpollutantlist, String reporttype, List<Integer> showtypes, List<Map<String, Object>> standlist) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //获取所有关联企业的监测点类型
        List<Integer> pollutiontypes = CommonTypeEnum.getOutPutTypeList();

        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        String flowunit = "";
        if ("day".equals(reporttype)) {//日报
            flowunit = "千克.时";
        } else if ("week".equals(reporttype)) {//周报
            flowunit = "千克.周";
        } else if ("month".equals(reporttype)) {//月报
            flowunit = "千克.日";
        } else if ("year".equals(reporttype)) {//年报
            flowunit = "吨.月";
        } else if ("custom".equals(reporttype)) {//自定义
            flowunit = "千克.日";
        } else if ("hours".equals(reporttype)) {//小时时段
            flowunit = "千克.时";
        } else if ("days".equals(reporttype)) {//日时段
            flowunit = "千克.日";
        }
        if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
            stationname.put("label", "排口名称");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {
            stationname.put("label", "储罐");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {
            stationname.put("label", "生产装置");
        } else {
            stationname.put("label", "监测点名称");
        }
        stationname.put("prop", "outputname");
        stationname.put("minwidth", "150px");
        stationname.put("headeralign", "center");
        stationname.put("fixed", "left");
        stationname.put("align", "center");
        stationname.put("showhide", true);
        dataList.add(stationname);

        Map<String, Object> counttimenum = new HashMap<>();
        counttimenum.put("label", "监测时间");
        counttimenum.put("prop", "monitortime");
        counttimenum.put("width", "120px");
        counttimenum.put("headeralign", "center");
        counttimenum.put("fixed", "left");
        counttimenum.put("align", "center");
        counttimenum.put("showhide", true);
        dataList.add(counttimenum);


        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            boolean isshasconvertdata = false;
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                    if (obj.get("IsHasConvertData") != null && "1".equals(obj.get("IsHasConvertData").toString())) {
                        isshasconvertdata = true;
                    }
                }
            }
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//若监测类型为储罐 则无排放量和折算值
                Map<String, Object> map = new HashMap<>();
                map.put("label", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                map.put("prop", code + "concentration");
                map.put("width", "100px");
                map.put("headeralign", "center");
                map.put("align", "center");
                map.put("showhide", true);
                dataList.add(map);
            } else {
                String str = "";
                if (standlist != null && standlist.size() > 0) {
                    for (Map<String, Object> standmap : standlist) {
                        if (standmap.get("Code") != null && code.equals(standmap.get("Code").toString())) {
                            Object type = standmap.get("AlarmType");
                            Object StandardMinValue = standmap.get("StandardMinValue");
                            Object StandardMaxValue = standmap.get("StandardMaxValue");
                            if (type != null) {
                                if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(type.toString())) {//上限报警
                                    if (StandardMaxValue != null && !"".equals(StandardMaxValue.toString())) {
                                        StandardMaxValue = DataFormatUtil.subZeroAndDot(StandardMaxValue.toString());
                                        str = "≤" + StandardMaxValue;
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(type.toString())) {//下限报警
                                    if (StandardMinValue != null && !"".equals(StandardMinValue.toString())) {
                                        StandardMinValue = DataFormatUtil.subZeroAndDot(StandardMinValue.toString());
                                        str = "≥" + StandardMinValue;
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(type.toString())) {//区间报警
                                    if (StandardMinValue != null && !"".equals(StandardMinValue.toString())) {
                                        StandardMinValue = DataFormatUtil.subZeroAndDot(StandardMinValue.toString());
                                        str = "(" + StandardMinValue + "～";
                                    }
                                    if (StandardMaxValue != null && !"".equals(StandardMaxValue.toString())) {
                                        StandardMaxValue = DataFormatUtil.subZeroAndDot(StandardMaxValue.toString());
                                        str += StandardMaxValue + ")";
                                    }
                                }
                            }
                        }
                    }
                }
                Map<String, Object> map = new HashMap<>();
                List<String> monitortypes = pollutiontypes.stream().map(x -> x + "").collect(Collectors.toList());
                if (monitortypes.contains(pointtype)) {//若为关联企业的监测点类型  取企业名称
                    //map.put("label", name);
                    map.put("label", name + str);//标准值放在污染物上
                    map.put("prop", code);
                    map.put("headeralign", "center");
                    map.put("align", "center");
                    map.put("showhide", true);
                    List<Map<String, Object>> chlidheader = new ArrayList<>();

                    Map<String, Object> map2 = new HashMap<>();
                    //标准值不放在实测值单元格上
                    map2.put("label", "实测值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                   /* if (isshasconvertdata == true) {//有折算值 不显示标准
                        map2.put("label", "实测值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                    } else {//无折算值  显示标准值
                        map2.put("label", "实测值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                    }*/
                    map2.put("prop", code + "concentration");
                    map2.put("width", "100px");
                    map2.put("type", "concentratecolor");
                    map2.put("headeralign", "center");
                    map2.put("align", "center");
                    map2.put("showhide", true);
                    chlidheader.add(map2);
                    //判断当前污染物是否为流量
                    if (llpollutants.contains(code)) {
                        Map<String, Object> ll_map = new HashMap<>();
                        if ("b02".equals(code)) {
                            ll_map.put("label", "累计(m3)");
                        } else {
                            ll_map.put("label", "累计(t)");
                        }
                        ll_map.put("prop", code + "concentration_ljll");
                        ll_map.put("width", "100px");
                        ll_map.put("headeralign", "center");
                        ll_map.put("align", "center");
                        ll_map.put("showhide", true);
                        chlidheader.add(ll_map);
                    }
                    if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                        boolean flag = false;
                        for (String strcode : flowpollutantlist) {
                            if (code.equals(strcode)) {
                                flag = true;
                                break;
                            }
                        }
                        for (int showtype : showtypes) {
                            if (showtype == 0) {//排放量
                                //去mongo中查询排放量数据
                                if (flag == true) {
                                    Map<String, Object> map4 = new HashMap<>();
                                    map4.put("label", "排放量" + ("".equals(flowunit) ? "" : "(" + flowunit + ")"));
                                    map4.put("prop", code + "flow");
                                    map4.put("width", "100px");
                                    map4.put("headeralign", "center");
                                    map4.put("align", "center");
                                    map4.put("showhide", true);
                                    chlidheader.add(map4);
                                }
                            }
                            if (showtype == 1 && isshasconvertdata == true) {//折算值
                                //去mongo中查询折算数据
                                if (flag == true) {
                                    if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                                        Map<String, Object> map3 = new HashMap<>();
                                        //标准值不放在折算值单元格上
                                        //map3.put("label", "折算值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                                        map3.put("label", "折算值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                                        map3.put("prop", code + "converted");
                                        map3.put("width", "100px");
                                        map3.put("type", "concentratecolor");
                                        map3.put("headeralign", "center");
                                        map3.put("align", "center");
                                        map3.put("showhide", true);
                                        chlidheader.add(map3);
                                    }
                                }
                            }
                        }
                    }
                    dataList.add(map);
                    map.put("children", chlidheader);
                } else {
                    map.put("label", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                    map.put("prop", code + "concentration");
                    map.put("width", "100px");
                    map.put("headeralign", "center");
                    map.put("align", "center");
                    map.put("showhide", true);
                    dataList.add(map);
                }
            }
        }
        return dataList;

    }

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取企业报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getExportTableTitleForEntDataReport(List<String> pollutantlist, String pointtype, List<String> flowpollutantlist, String reporttype, List<Integer> showtypes, List<Map<String, Object>> standlist) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //获取所有关联企业的监测点类型
        List<Integer> pollutiontypes = CommonTypeEnum.getOutPutTypeList();
        String flowunit = "";
        if ("day".equals(reporttype)) {//日报
            flowunit = "千克.时";
        } else if ("week".equals(reporttype)) {//周报
            flowunit = "千克.周";
        } else if ("month".equals(reporttype)) {//月报
            flowunit = "千克.日";
        } else if ("year".equals(reporttype)) {//年报
            flowunit = "吨.月";
        } else if ("custom".equals(reporttype)) {//自定义
            flowunit = "千克.日";
        } else if ("hours".equals(reporttype)) {//小时时段
            flowunit = "千克.时";
        } else if ("days".equals(reporttype)) {//日时段
            flowunit = "千克.日";
        }
        Map<String, Object> stationname = new HashMap<>();
        String rownum = "2";
        if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
            stationname.put("headername", "排口名称");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {
            stationname.put("headername", "储罐名称");
            rownum = "1";
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {
            stationname.put("headername", "生产装置");
            rownum = "1";
        } else {
            rownum = "1";
            stationname.put("headername", "监测点名称");
        }
        stationname.put("headercode", "outputname");
        stationname.put("rownum", rownum);
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);

        Map<String, Object> counttimenum = new HashMap<>();
        counttimenum.put("headername", "监测时间");
        counttimenum.put("headercode", "monitortimes");
        counttimenum.put("rownum", rownum);
        counttimenum.put("columnnum", "1");
        counttimenum.put("chlidheader", new ArrayList<>());
        dataList.add(counttimenum);

        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            boolean isshasconvertdata = false;
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                    if (obj.get("IsHasConvertData") != null && "1".equals(obj.get("IsHasConvertData").toString())) {
                        isshasconvertdata = true;
                    }
                }
            }
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//若监测类型为储罐 则无排放量和折算值
                Map<String, Object> map = new HashMap<>();
                map.put("headername", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                map.put("headercode", code + "concentration");
                map.put("rownum", "1");
                map.put("columnnum", "1");
                map.put("chlidheader", new ArrayList<>());
                dataList.add(map);
            } else {
                String str = "";
                if (standlist != null && standlist.size() > 0) {
                    for (Map<String, Object> standmap : standlist) {
                        if (standmap.get("Code") != null && code.equals(standmap.get("Code").toString())) {
                            Object type = standmap.get("AlarmType");
                            Object StandardMinValue = standmap.get("StandardMinValue");
                            Object StandardMaxValue = standmap.get("StandardMaxValue");
                            if (type != null) {
                                if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(type.toString())) {//上限报警
                                    if (StandardMaxValue != null && !"".equals(StandardMaxValue.toString())) {
                                        StandardMaxValue = DataFormatUtil.subZeroAndDot(StandardMaxValue.toString());
                                        str = "≤" + StandardMaxValue;
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(type.toString())) {//下限报警
                                    if (StandardMinValue != null && !"".equals(StandardMinValue.toString())) {
                                        StandardMinValue = DataFormatUtil.subZeroAndDot(StandardMinValue.toString());
                                        str = "≥" + StandardMinValue;
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(type.toString())) {//区间报警
                                    if (StandardMinValue != null && !"".equals(StandardMinValue.toString())) {
                                        StandardMinValue = DataFormatUtil.subZeroAndDot(StandardMinValue.toString());
                                        str = "(" + StandardMinValue + "～";
                                    }
                                    if (StandardMaxValue != null && !"".equals(StandardMaxValue.toString())) {
                                        StandardMaxValue = DataFormatUtil.subZeroAndDot(StandardMaxValue.toString());
                                        str += StandardMaxValue + ")";
                                    }
                                }
                            }
                        }
                    }
                }
                boolean flag = false;
                for (String strcode : flowpollutantlist) {
                    if (code.equals(strcode)) {
                        flag = true;
                        break;
                    }
                }

                Map<String, Object> map = new HashMap<>();
                List<String> monitortypes = pollutiontypes.stream().map(x -> x + "").collect(Collectors.toList());
                if (monitortypes.contains(pointtype)) {//若为关联企业的监测点类型  取企业名称
                    //map.put("headername", name);将标准值放在污染物单元格上
                    map.put("headername", name + str);
                    map.put("headercode", code);
                    map.put("rownum", "1");
                    if (flag == true) {
                        int num = 1;
                        if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                            num = num + showtypes.size();
                        }
                        if (llpollutants.contains(code)) {
                            num = num + 1;
                        }
                        map.put("columnnum", num);
                    } else {
                        if (llpollutants.contains(code)) {
                            map.put("columnnum", 2);
                        } else {
                            map.put("columnnum", 1);
                        }
                    }
                    List<Map<String, Object>> chlidheader = new ArrayList<>();

                    Map<String, Object> windtimenum = new HashMap<>();
                   /* if (isshasconvertdata == true) {//有折算值  不拼接标准值
                        windtimenum.put("headername", "实测值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                    } else {//无折算值
                        windtimenum.put("headername", "实测值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                    }*/
                    windtimenum.put("headername", "实测值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                    windtimenum.put("headercode", code + "concentration");
                    windtimenum.put("rownum", "1");
                    windtimenum.put("columnnum", "1");
                    windtimenum.put("chlidheader", new ArrayList<>());
                    chlidheader.add(windtimenum);

                    //判断当前污染物是否为流量
                    if (llpollutants.contains(code)) {
                        Map<String, Object> ll_map = new HashMap<>();
                        if ("b02".equals(code)) {
                            ll_map.put("headername", "累计(m3)");
                        } else {
                            ll_map.put("headername", "累计(t)");
                        }
                        ll_map.put("headercode", code + "concentration_ljll");
                        ll_map.put("rownum", "1");
                        ll_map.put("columnnum", "1");
                        ll_map.put("chlidheader", new ArrayList<>());
                        chlidheader.add(ll_map);
                    }

                    if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                        for (int showtype : showtypes) {
                            if (showtype == 0) {//排放量
                                if (flag == true) {
                                    Map<String, Object> windpercent = new HashMap<>();
                                    windpercent.put("headername", "排放量" + ("".equals(flowunit) ? "" : "(" + flowunit + ")"));
                                    windpercent.put("headercode", code + "flow");
                                    windpercent.put("rownum", "1");
                                    windpercent.put("columnnum", "1");
                                    windpercent.put("chlidheader", new ArrayList<>());
                                    chlidheader.add(windpercent);
                                }
                            }
                            if (showtype == 1 && isshasconvertdata == true) {//折算值
                                if (flag == true) {
                                    if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                                        Map<String, Object> map2 = new HashMap<>();
                                        //map2.put("headername", "折算值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                                        map2.put("headername", "折算值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                                        map2.put("headercode", code + "converted");
                                        map2.put("rownum", "1");
                                        map2.put("columnnum", "1");
                                        map2.put("chlidheader", new ArrayList<>());
                                        chlidheader.add(map2);
                                    }
                                }
                            }
                        }
                    }
                    dataList.add(map);
                    map.put("chlidheader", chlidheader);
                } else {
                    map.put("headername", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                    map.put("headercode", code + "concentration");
                    map.put("rownum", "1");
                    map.put("columnnum", "1");
                    map.put("chlidheader", new ArrayList<>());
                    dataList.add(map);
                }
            }
        }
        return dataList;

    }


    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 11:25
     * @Description:获取企业下某类型所有在线排口的浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getAllOutPutConcentrationData(Date startDate, Date endDate, List<String> mnlist,
                                                              List<String> pollutantlist, String reporttype, List<Document> documents, Map<String, Object> flag_codeAndName) {
        Map<String, Object> map = new HashMap<>();
        Object dataFlag;
        for (String mn : mnlist) {
            List<Map<String, Object>> listmap = new ArrayList<>();
            Map<String, Object> countmap = new HashMap<>();
            for (Document document : documents) {
                String monitorDate = "";
                String datalistname = "";
                if ("day".equals(reporttype)) {//日报
                    datalistname = "HourDataList";
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
                    datalistname = "DayDataList";
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                } else if ("year".equals(reporttype)) {//年报
                    datalistname = "MonthDataList";
                    monitorDate = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                } else if ("custom".equals(reporttype)) {//自定义
                    datalistname = "DayDataList";
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitorDate);

                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                    for (String pollutantcode : pollutantlist) {
                        String value = "";
                        dataFlag = "";
                        for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                            if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                if (dataMap.get("AvgStrength") != null && !"".equals(dataMap.get("AvgStrength").toString())) {
                                    value = dataMap.get("AvgStrength").toString();
                                    dataFlag = flag_codeAndName.get(dataMap.get("Flag") != null ? dataMap.get("Flag").toString().toLowerCase() : "");
                                }
                                break;
                            }
                        }

                        //判断map中是否有该污染物的数据
                        boolean flag = countmap.containsKey(pollutantcode);
                        if (flag == true) {//有
                            Map<String, Double> themap = (Map<String, Double>) countmap.get(pollutantcode);
                            if (!"".equals(value)) {//判断是否有该因子的监测值
                                if (!"".equals(themap.get("totalconcentration"))) {//总浓度
                                    Double totalconcentration = themap.get("totalconcentration");
                                    totalconcentration = totalconcentration + Double.parseDouble(value);
                                    themap.put("totalconcentration", totalconcentration);
                                }
                                if (!"".equals(themap.get("minconcentration"))) {//最小浓度
                                    Double minconcentration = themap.get("minconcentration");
                                    if (minconcentration > Double.parseDouble(value)) {
                                        minconcentration = Double.parseDouble(value);
                                    }
                                    themap.put("minconcentration", minconcentration);
                                }
                                if (!"".equals(themap.get("maxconcentration"))) {//最大浓度
                                    Double maxconcentration = themap.get("maxconcentration");
                                    if (maxconcentration < Double.parseDouble(value)) {
                                        maxconcentration = Double.parseDouble(value);
                                    }
                                    themap.put("maxconcentration", maxconcentration);
                                }
                                if (!"".equals(themap.get("concentrationnum"))) {//次数
                                    Double concentrationnum = themap.get("concentrationnum");
                                    concentrationnum += 1;
                                    themap.put("concentrationnum", concentrationnum);
                                }
                                countmap.put(pollutantcode, themap);
                            }
                        } else {//无
                            Map<String, Double> themap = new HashMap<>();
                            if (!"".equals(value)) {//判断是否有该因子的监测值
                                themap.put("totalconcentration", Double.parseDouble(value));
                                themap.put("minconcentration", Double.parseDouble(value));
                                themap.put("maxconcentration", Double.parseDouble(value));
                                themap.put("concentrationnum", 1.0);
                                countmap.put(pollutantcode, themap);
                            }
                        }
                        value = "".equals(value) ? "" : value + "," + dataFlag;
                        resultmap.put(pollutantcode, value);
                    }
                    listmap.add(resultmap);
                }
                //resultmap.put("dgmin", mn);

            }
            Map<String, Object> theavgmap = new HashMap<>();
            theavgmap.put("monitortime", "平均值");
            Map<String, Object> theminmap = new HashMap<>();
            theminmap.put("monitortime", "最小值");
            Map<String, Object> themaxmap = new HashMap<>();
            themaxmap.put("monitortime", "最大值");
            for (String pollutantcode : pollutantlist) {
                Map<String, Double> themap = (Map<String, Double>) countmap.get(pollutantcode);
                if (themap != null) {
                    Double totalconcentration = 0d;
                    Double concentrationnum = 0d;
                    if (!"".equals(themap.get("totalconcentration"))) {//总浓度
                        totalconcentration = themap.get("totalconcentration");
                    }
                    if (!"".equals(themap.get("minconcentration"))) {//最小浓度
                        Double minconcentration = themap.get("minconcentration");
                        theminmap.put(pollutantcode, minconcentration);
                    }
                    if (!"".equals(themap.get("maxconcentration"))) {//最大浓度
                        Double maxconcentration = themap.get("maxconcentration");
                        themaxmap.put(pollutantcode, maxconcentration);

                    }
                    if (!"".equals(themap.get("concentrationnum"))) {//次数
                        concentrationnum = themap.get("concentrationnum");

                    }
                    if (concentrationnum > 0) {
                        theavgmap.put(pollutantcode, DataFormatUtil.formatDoubleSaveTwo(totalconcentration / concentrationnum));
                    }
                } else {
                    theminmap.put(pollutantcode, "");
                    themaxmap.put(pollutantcode, "");
                    theavgmap.put(pollutantcode, "");
                }
            }
            listmap.add(theavgmap);
            listmap.add(theminmap);
            listmap.add(themaxmap);
            map.put(mn, listmap);
        }
        return map;
    }

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 11:25
     * @Description:获取企业下某类型所有在线排口的折算数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getAllOutPutConvertedValueData(Date startDate, Date endDate, List<String> mnlist, List<String> pollutantlist, String reporttype, List<Document> documents, Map<String, Object> flag_codeAndName) {
        Map<String, Object> map = new HashMap<>();


        Object dataFlag;
        for (String mn : mnlist) {
            List<Map<String, Object>> listmap = new ArrayList<>();
            Map<String, Object> countmap = new HashMap<>();
            for (Document document : documents) {
                String monitorDate = "";
                String datalistname = "";
                if ("day".equals(reporttype)) {//日报
                    datalistname = "HourDataList";
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
                    datalistname = "DayDataList";
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                } else if ("year".equals(reporttype)) {//年报
                    datalistname = "MonthDataList";
                    monitorDate = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                } else if ("custom".equals(reporttype)) {//自定义
                    datalistname = "DayDataList";
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitorDate);

                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                    for (String pollutantcode : pollutantlist) {
                        String value = "";
                        dataFlag = "";
                        for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                            if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                if (dataMap.get("AvgConvertStrength") != null && !"".equals(dataMap.get("AvgConvertStrength").toString())) {
                                    value = dataMap.get("AvgConvertStrength").toString();
                                    dataFlag = flag_codeAndName.get(dataMap.get("Flag") != null ? dataMap.get("Flag").toString().toLowerCase() : "");
                                }
                                break;
                            }
                        }

                        //判断map中是否有该污染物的数据
                        boolean flag = countmap.containsKey(pollutantcode);
                        if (flag == true) {//有
                            Map<String, Double> themap = (Map<String, Double>) countmap.get(pollutantcode);
                            if (!"".equals(value)) {//判断是否有该因子的监测值
                                if (!"".equals(themap.get("totalconverted"))) {//总折算
                                    Double totalconverted = themap.get("totalconverted");
                                    totalconverted = totalconverted + Double.parseDouble(value);
                                    themap.put("totalconverted", totalconverted);
                                }
                                if (!"".equals(themap.get("minconverted"))) {//最小浓度
                                    Double minconverted = themap.get("minconverted");
                                    if (minconverted > Double.parseDouble(value)) {
                                        minconverted = Double.parseDouble(value);
                                    }
                                    themap.put("minconverted", minconverted);
                                }
                                if (!"".equals(themap.get("maxconverted"))) {//最大浓度
                                    Double maxconverted = themap.get("maxconverted");
                                    if (maxconverted < Double.parseDouble(value)) {
                                        maxconverted = Double.parseDouble(value);
                                    }
                                    themap.put("maxconverted", maxconverted);
                                }
                                if (!"".equals(themap.get("convertednum"))) {//次数
                                    Double convertednum = themap.get("convertednum");
                                    convertednum += 1;
                                    themap.put("convertednum", convertednum);
                                }
                                countmap.put(pollutantcode, themap);
                            }
                        } else {//无
                            Map<String, Double> themap = new HashMap<>();
                            if (!"".equals(value)) {//判断是否有该因子的监测值
                                themap.put("totalconverted", Double.parseDouble(value));
                                themap.put("minconverted", Double.parseDouble(value));
                                themap.put("maxconverted", Double.parseDouble(value));
                                themap.put("convertednum", 1.0);
                                countmap.put(pollutantcode, themap);
                            }
                        }
                        value = "".equals(value) ? "" : value + "," + dataFlag;
                        resultmap.put(pollutantcode, value);
                    }
                    listmap.add(resultmap);
                }
                //resultmap.put("dgmin", mn);

            }
            Map<String, Object> theavgmap = new HashMap<>();
            theavgmap.put("monitortime", "平均值");
            Map<String, Object> theminmap = new HashMap<>();
            theminmap.put("monitortime", "最小值");
            Map<String, Object> themaxmap = new HashMap<>();
            themaxmap.put("monitortime", "最大值");
            for (String pollutantcode : pollutantlist) {
                Map<String, Double> themap = (Map<String, Double>) countmap.get(pollutantcode);
                if (themap != null) {
                    Double totalconverted = 0d;
                    Double convertednum = 0d;
                    if (!"".equals(themap.get("totalconverted"))) {//总浓度
                        totalconverted = themap.get("totalconverted");
                    }
                    if (!"".equals(themap.get("minconverted"))) {//最小浓度
                        Double minconverted = themap.get("minconverted");
                        theminmap.put(pollutantcode, minconverted);
                    }
                    if (!"".equals(themap.get("maxconverted"))) {//最大浓度
                        Double maxconverted = themap.get("maxconverted");
                        themaxmap.put(pollutantcode, maxconverted);

                    }
                    if (!"".equals(themap.get("convertednum"))) {//次数
                        convertednum = themap.get("convertednum");

                    }
                    if (convertednum > 0) {
                        theavgmap.put(pollutantcode, DataFormatUtil.formatDoubleSaveTwo(totalconverted / convertednum));
                    }
                } else {
                    theminmap.put(pollutantcode, "");
                    themaxmap.put(pollutantcode, "");
                    theavgmap.put(pollutantcode, "");
                }
            }
            listmap.add(theavgmap);
            listmap.add(theminmap);
            listmap.add(themaxmap);
            map.put(mn, listmap);
        }
        return map;
    }

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 11:51
     * @Description:获取企业下某类型所有在线排口的排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getAllOutPutFlowData(Date startDate, Date endDate, List<String> mnlist, List<String> pollutantlist, String reporttype) {
        String collection = "";
        String datalistname = "";
        if ("day".equals(reporttype)) {//日报
            collection = "HourFlowData";
            datalistname = "HourFlowDataList";
        } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
            collection = "DayFlowData";
            datalistname = "DayFlowDataList";
        } else if ("year".equals(reporttype)) {//年报
            collection = "MonthFlowData";
            datalistname = "MonthFlowDataList";
        } else if ("custom".equals(reporttype)) {//自定义
            collection = "DayFlowData";
            datalistname = "DayFlowDataList";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        Map<String, Object> map = new HashMap<>();
        for (String mn : mnlist) {
            List<Map<String, Object>> listmap = new ArrayList<>();
            Map<String, Object> countmap = new HashMap<>();
            for (Document document : documents) {
                String monitorDate = "";
                if ("day".equals(reporttype)) {//日报
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                } else if ("year".equals(reporttype)) {//年报
                    monitorDate = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                } else if ("custom".equals(reporttype)) {//自定义
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitorDate);

                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                    for (String pollutantcode : pollutantlist) {
                        String value = "";
                        for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                            if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                if ("year".equals(reporttype)) {
                                    if (dataMap.get("PollutantFlow") != null && !"".equals(dataMap.get("PollutantFlow").toString())) {
                                   /* if ((Double.parseDouble(dataMap.get("AvgFlow").toString()) > 0 || Double.parseDouble(dataMap.get("AvgFlow").toString()) < 0)) {
                                        value = dataMap.get("AvgFlow").toString();
                                    }*/
                                        value = dataMap.get("PollutantFlow").toString();
                                    }
                                } else {
                                    if (dataMap.get("AvgFlow") != null && !"".equals(dataMap.get("AvgFlow").toString())) {
                                   /* if ((Double.parseDouble(dataMap.get("AvgFlow").toString()) > 0 || Double.parseDouble(dataMap.get("AvgFlow").toString()) < 0)) {
                                        value = dataMap.get("AvgFlow").toString();
                                    }*/
                                        value = dataMap.get("AvgFlow").toString();
                                    }
                                }
                                break;
                            }
                        }

                        //判断map中是否有该污染物的数据
                        boolean flag = countmap.containsKey(pollutantcode);
                        if (flag == true) {//有
                            Map<String, Double> themap = (Map<String, Double>) countmap.get(pollutantcode);
                            //if (!"".equals(value) && (Double.parseDouble(value) > 0 || Double.parseDouble(value) < 0)) {//判断是否有该因子的监测值
                            if (!"".equals(value)) {
                                if (!"".equals(themap.get("totalflow"))) {//总排放量
                                    Double totalflow = themap.get("totalflow");
                                    totalflow = totalflow + Double.parseDouble(value);
                                    themap.put("totalflow", DataFormatUtil.formatDoubleSaveThree(totalflow));
                                }
                                if (!"".equals(themap.get("minflow"))) {//最小排放量
                                    Double minflow = themap.get("minflow");
                                    if (minflow > Double.parseDouble(value)) {
                                        minflow = Double.parseDouble(value);
                                    }
                                    themap.put("minflow", minflow);
                                }
                                if (!"".equals(themap.get("maxflow"))) {//最大排放量
                                    Double maxflow = themap.get("maxflow");
                                    if (maxflow < Double.parseDouble(value)) {
                                        maxflow = Double.parseDouble(value);
                                    }
                                    themap.put("maxflow", maxflow);
                                }
                                if (!"".equals(themap.get("flownum"))) {//次数
                                    Double flownum = themap.get("flownum");
                                    flownum += 1;
                                    themap.put("flownum", flownum);
                                }
                                countmap.put(pollutantcode, themap);
                            }
                        } else {//无
                            Map<String, Double> themap = new HashMap<>();
                            // if (!"".equals(value) && (Double.parseDouble(value) > 0 || Double.parseDouble(value) < 0)) {//判断是否有该因子的监测值
                            if (!"".equals(value)) {
                                themap.put("totalflow", Double.parseDouble(value));
                                themap.put("minflow", Double.parseDouble(value));
                                themap.put("maxflow", Double.parseDouble(value));
                                themap.put("flownum", 1.0);
                                countmap.put(pollutantcode, themap);
                            }
                        }

                        resultmap.put(pollutantcode, value != null ? value : "");
                    }
                    listmap.add(resultmap);
                }
                //resultmap.put("dgmin", mn);

            }
            Map<String, Object> theavgmap = new HashMap<>();
            theavgmap.put("monitortime", "平均值");
            Map<String, Object> theminmap = new HashMap<>();
            theminmap.put("monitortime", "最小值");
            Map<String, Object> themaxmap = new HashMap<>();
            themaxmap.put("monitortime", "最大值");
            Map<String, Object> thetotalmap = new HashMap<>();
            thetotalmap.put("monitortime", "总排放量");
            for (String pollutantcode : pollutantlist) {
                Map<String, Double> themap = (Map<String, Double>) countmap.get(pollutantcode);
                if (themap != null) {
                    Double totalflow = 0d;
                    Double flownum = 0d;
                    if (!"".equals(themap.get("totalflow"))) {//总浓度
                        totalflow = themap.get("totalflow");
                        thetotalmap.put(pollutantcode, totalflow);
                    }
                    if (!"".equals(themap.get("minflow"))) {//最小浓度
                        Double minflow = themap.get("minflow");
                        theminmap.put(pollutantcode, minflow);
                    }
                    if (!"".equals(themap.get("maxflow"))) {//最大浓度
                        Double maxflow = themap.get("maxflow");
                        themaxmap.put(pollutantcode, maxflow);

                    }
                    if (!"".equals(themap.get("flownum"))) {//次数
                        flownum = themap.get("flownum");

                    }
                    if (flownum > 0) {
                        theavgmap.put(pollutantcode, DataFormatUtil.formatDoubleSaveTwo(totalflow / flownum));
                    }
                } else {
                    theminmap.put(pollutantcode, "");
                    themaxmap.put(pollutantcode, "");
                    theavgmap.put(pollutantcode, "");
                    thetotalmap.put(pollutantcode, "");
                }
            }
            listmap.add(theavgmap);
            listmap.add(theminmap);
            listmap.add(themaxmap);
            listmap.add(thetotalmap);
            map.put(mn, listmap);
        }

        return map;
    }

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 下午 1:14
     * @Description:获取企业小时报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getEntDataDayReportData(Map<String, Object> datamap, List<String> mnlist, List<String> pollutantlist, List<Map<String, Object>> pollutions, String pointtype, Map<String, Object> ll_map) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        //String ll_unit = "";//获取流量污染物单位
        //获取时间范围
        double daynum = 24;
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> concentrations = (Map<String, Object>) datamap.get("concentrations");
        Map<String, Object> convertedvalues = (Map<String, Object>) datamap.get("convertedvalues");
        Map<String, Object> flowmap = (Map<String, Object>) datamap.get("flowmap");
        String monitortime = (String) datamap.get("monitortime");
        List<Map<String, Object>> standlist = (List<Map<String, Object>>) datamap.get("standlist");
        String tabletitletype = "1";//(String) datamap.get("tabletitletype");
        int num = 1;
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //废气、烟气查污染物数据判断是否有折算
        Map<String, Object> codeandflag = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        /*if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> map : pollutants) {
                //获取流量污染物单位
                if (map.get("code") != null && llpollutants.contains(map.get("code").toString())) {
                    ll_unit = map.get("PollutantUnit") != null ? map.get("PollutantUnit").toString() : "";
                    break;
                }
            }
        }*/
        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
        }
        for (String mn : mnlist) {//遍历企业下排口MN号
            String outputname = "";
            for (Map<String, Object> obj : pollutions) {
                if (mn.equals(obj.get("DGIMN").toString())) {
                    outputname = obj.get("outputname") != null ? obj.get("outputname").toString() : (obj.get("OutputName") != null ? obj.get("OutputName").toString() : "");
                }
            }
            List<Map<String, Object>> concentlist = (List<Map<String, Object>>) concentrations.get(mn);
            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(mn);
            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(mn);

            String ll_min = "";
            Double ll_total = 0d;
            String ll_max = "";
            String ll_avg = "";
            Double ll_num = 0d;
            for (int i = 0; i < 24; i++) {
                Map<String, Object> resultmap = new HashMap<>();
                Date someTime = DataFormatUtil.getDateYMDH(monitortime + " 00");
                String thetime = "";
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(someTime);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + i);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
                thetime = df.format(calendar.getTime());
                String convalue = "";
                resultmap.put("outputname", outputname);//
                resultmap.put("monitortime", i + "时");//
                resultmap.put("monitortimes", i + "时");//用于导出时取值
                boolean flag = false;
                boolean flag2 = false;
                boolean flag3 = false;
                //浓度
                for (Map<String, Object> map : concentlist) {
                    if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                        for (String code : pollutantlist) {
                            if ("1".equals(tabletitletype)) {
                                String str = "";
                                if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                    str = "#false";
                                } else {
                                    str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                }
                                resultmap.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                            } else {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                            }
                            if (llpollutants.contains(code)) {
                                //if (!"".equals(ll_unit)) {
                                //String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                //resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                //}
                                if (ll_map.get(mn) != null) {
                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(mn);
                                    for (Map<String, Object> ljllmap : lllist) {
                                        if (thetime.equals(ljllmap.get("monitortime").toString())) {//当时间相等时
                                            resultmap.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                            if (!"".equals(ljllmap.get("ljll").toString())) {
                                                ll_num += 1;
                                                //总累计流量
                                                ll_total += Double.valueOf(ljllmap.get("ljll").toString());
                                                //累计流量最小值
                                                if (ll_min.equals("")) {
                                                    ll_min = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_min) > Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_min = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                                //累计流量最大值
                                                if (ll_max.equals("")) {
                                                    ll_max = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_max) < Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_max = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                resultmap.put(code + "concentration" + "_ljll", null);
                            }
                        }

                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    for (String code : pollutantlist) {
                        if ("1".equals(tabletitletype)) {
                            resultmap.put(code + "concentration#false", null);
                        } else {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                }
                //排放量
                if (flowlist != null && flowlist.size() > 0) {
                    for (Map<String, Object> map : flowlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", getDoubleValueForStringValue(map, code));
                            }
                            flag2 = true;
                            break;
                        }
                    }
                    if (flag2 == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "flow", null);
                        }
                    }
                }

                if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                    //折算值
                    for (Map<String, Object> map : convertedlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                if (codeandflag != null && codeandflag.get(code) != null) {//污染物有折算值
                                    if ("1".equals(tabletitletype)) {
                                        String str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                        resultmap.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                    } else {
                                        resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                    }
                                }
                            }
                            flag3 = true;
                            break;
                        }
                    }
                    if (flag3 == false) {
                        for (String code : pollutantlist) {
                            if (codeandflag != null && codeandflag.get(code) != null) {//污染物有折算值
                                if ("1".equals(tabletitletype)) {
                                    resultmap.put(code + "converted#false", null);
                                } else {
                                    resultmap.put(code + "converted", null);
                                }
                            }
                        }
                    }
                }
                result.add(resultmap);
            }
            //计算累计流量平均值
            if (ll_num > 0) {
                ll_avg = DataFormatUtil.formatDoubleSaveTwo(ll_total / ll_num) + "";
            }
            boolean flag = false;
            boolean flag2 = false;
            boolean flag3 = false;
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                String[] strs = new String[]{"平均值", "最小值", "最大值", "总排放量"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                if (llpollutants.contains(code)) {
                                   /* if (!"".equals(ll_unit)) {
                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    }*/
                                    if (str.equals("平均值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_avg) ? Double.valueOf(ll_avg) : null);
                                    } else if (str.equals("最小值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_min) ? Double.valueOf(ll_min) : null);
                                    } else if (str.equals("最大值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_max) ? Double.valueOf(ll_max) : null);
                                    }
                                }
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    //排放量
                    if (flowlist != null && flowlist.size() > 0) {
                        for (Map<String, Object> map : flowlist) {
                            if (str.equals(map.get("monitortime").toString())) {//当时间相等时
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "flow", getDoubleValueForStringValue(map, code));
                                }
                                flag2 = true;
                                break;
                            }
                        }
                        if (flag2 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", null);
                            }
                        }
                    }
                    if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                        //折算值
                        for (Map<String, Object> map : convertedlist) {
                            if (str.equals(map.get("monitortime").toString())) {//平均值
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                }
                                flag3 = true;
                                break;
                            }
                        }
                        if (flag3 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "converted", null);
                            }
                        }
                    }
                    result.add(resultmap);
                }
            } else {
                String[] strs = new String[]{"平均值", "最小值", "最大值"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                /*if (llpollutants.contains(code)) {
                                    if (!"".equals(ll_unit)) {
                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    }
                                } else {
                                    resultmap.put(code + "concentration" + "_ljll", null);
                                }*/
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    result.add(resultmap);
                }
            }
            if (num < mnlist.size()) {
                result.add(new HashMap<>());
            }
            num++;
        }
        return result;
    }

    private Object getDoubleValueForStringValue(Map<String, Object> map, String code) {
        Object value = null;
        value = map.get(code) != null ? map.get(code).toString() : "";

        if (((String) value).contains(",")) {
            value = value.toString().split(",")[0];
        }

        value = !"".equals(value.toString()) ? Double.valueOf(value.toString()) : null;
        return value;
    }

    private Object getDoubleFlowValueForStringValue(Map<String, Object> map, String code) {
        Double value = null;
        String onevalue = map.get(code) != null ? map.get(code).toString() : "";
        value = !"".equals(onevalue.toString()) ? Double.valueOf(onevalue.toString()) : null;
        if (value != null) {
            BigDecimal bigDecimal = BigDecimal.valueOf(value);
            value = bigDecimal.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
            value = Double.valueOf(DataFormatUtil.subZeroAndDot(value + ""));
        }
        return value;
    }

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 下午 1:14
     * @Description:获取企业周报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getEntDataWeekReportData(Map<String, Object> datamap, List<String> mnlist, List<String> pollutantlist, List<Map<String, Object>> pollutions, String pointtype, Map<String, Object> ll_map) throws Exception {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        String ll_unit = "";//流量单位
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> concentrations = (Map<String, Object>) datamap.get("concentrations");
        Map<String, Object> convertedvalues = (Map<String, Object>) datamap.get("convertedvalues");
        Map<String, Object> flowmap = (Map<String, Object>) datamap.get("flowmap");
        String monitortime = (String) datamap.get("monitortime");
        List<Map<String, Object>> standlist = (List<Map<String, Object>>) datamap.get("standlist");
        String tabletitletype = "1";//(String) datamap.get("tabletitletype");
        //获取该月所有日期
        Date someTime = DataFormatUtil.getDateYM(monitortime);
        List<String> timelist = DataFormatUtil.getDayListOfMonth(someTime);
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //废气、烟气查污染物数据判断是否有折算
        Map<String, Object> codeandflag = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        /*if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> map : pollutants) {
                //获取流量污染物单位
                if (map.get("code") != null && llpollutants.contains(map.get("code").toString())) {
                    ll_unit = map.get("PollutantUnit") != null ? map.get("PollutantUnit").toString() : "";
                    break;
                }
            }
        }*/
        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
        }
        int num = 1;
        for (String mn : mnlist) {//遍历企业下排口MN号
            String outputname = "";
            for (Map<String, Object> obj : pollutions) {
                if (mn.equals(obj.get("DGIMN").toString())) {
                    outputname = obj.get("outputname") != null ? obj.get("outputname").toString() : (obj.get("OutputName") != null ? obj.get("OutputName").toString() : "");
                }
            }
            List<Map<String, Object>> concentlist = (List<Map<String, Object>>) concentrations.get(mn);
            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(mn);
            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(mn);
            Set<String> set = new HashSet<>();
            Map<String, Object> countmap = new HashMap<>();
            String ll_min = "";
            Double ll_total = 0d;
            String ll_max = "";
            Double ll_avg = 0d;
            Double ll_num = 0d;
            for (String thetime : timelist) {
                Object obj = getWeekTimeByTimeStr(thetime);
                String value_flag;
                boolean flags = set.contains(obj);
                if (flags == false) {//没有重复
                    set.add(obj.toString());
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", outputname);//
                    resultmap.put("monitortime", obj);//
                    resultmap.put("monitortimes", obj);//用于导出时取值
                    Map<String, Object> themap = new HashMap<>();
                    Double weekdaynum = 0.0;
                    for (String time : timelist) {
                        Object obj2 = getWeekTimeByTimeStr(time);
                        if ((obj.toString()).equals(obj2.toString())) {//同一周时
                            weekdaynum += 1;
                            for (Map<String, Object> map : concentlist) {
                                if (time.equals(map.get("monitortime").toString())) {//当时间相等时
                                    for (String code : pollutantlist) {
                                        if (themap.get(code + "concentration") != null && !"".equals(themap.get(code + "concentration").toString())) {
                                            if (map.get(code) != null && !"".equals(map.get(code).toString())) {

                                                value_flag = map.get(code).toString();
                                                if (value_flag.contains(",")) {
                                                    value_flag = value_flag.split(",")[0];
                                                }
                                                Double value = Double.parseDouble(value_flag);
                                                Double value2 = Double.parseDouble(themap.get(code + "concentration").toString());
                                                value2 = value2 + value;
                                                themap.put(code + "concentration", value2);
                                            }
                                        } else {
                                            themap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                        }
                                        if (themap.get(code + "concentrationnum") != null && !"".equals(themap.get(code + "concentrationnum").toString())) {
                                            if (map.get(code) != null && !"".equals(map.get(code).toString())) {
                                                int i = Integer.parseInt(themap.get(code + "concentrationnum").toString());
                                                i += 1;
                                                themap.put(code + "concentrationnum", i);
                                            }
                                        } else {
                                            themap.put(code + "concentrationnum", map.get(code) != null ? 1 : 0);
                                        }
                                    }
                                    break;

                                }
                            }
                            //排放量
                            if (flowlist != null && flowlist.size() > 0) {
                                for (Map<String, Object> map : flowlist) {
                                    if (time.equals(map.get("monitortime").toString())) {//当时间相等时
                                        for (String code : pollutantlist) {
                                            if (themap.get(code + "flow") != null && !"".equals(themap.get(code + "flow").toString())) {
                                                if (map.get(code) != null && !"".equals(map.get(code).toString())) {
                                                    Double value = Double.parseDouble(map.get(code).toString());
                                                    Double value2 = Double.parseDouble(themap.get(code + "flow").toString());
                                                    value2 = value2 + value;
                                                    themap.put(code + "flow", value2);
                                                }
                                            } else {
                                                themap.put(code + "flow", getDoubleValueForStringValue(map, code));
                                            }
                                            if (themap.get(code + "flownum") != null && !"".equals(themap.get(code + "flownum").toString())) {
                                                if (map.get(code) != null && !"".equals(map.get(code).toString())) {
                                                    int i = Integer.parseInt(themap.get(code + "flownum").toString());
                                                    i += 1;
                                                    themap.put(code + "flownum", i);
                                                }
                                            } else {
                                                themap.put(code + "flownum", map.get(code) != null ? 1 : 0);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                                //折算值
                                for (Map<String, Object> map : convertedlist) {
                                    if (time.equals(map.get("monitortime").toString())) {//当时间相等时
                                        for (String code : pollutantlist) {
                                            if (themap.get(code + "converted") != null && !"".equals(themap.get(code + "converted").toString())) {
                                                if (map.get(code) != null && !"".equals(map.get(code).toString())) {
                                                    value_flag = map.get(code).toString();
                                                    if (value_flag.contains(",")) {
                                                        value_flag = value_flag.split(",")[0];
                                                    }
                                                    Double value = Double.parseDouble(value_flag);
                                                    Double value2 = Double.parseDouble(themap.get(code + "converted").toString());
                                                    value2 = value2 + value;
                                                    themap.put(code + "converted", value2);
                                                }
                                            } else {
                                                themap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                            }
                                            if (themap.get(code + "convertednum") != null && !"".equals(themap.get(code + "convertednum").toString())) {
                                                if (map.get(code) != null && !"".equals(map.get(code).toString())) {
                                                    int i = Integer.parseInt(themap.get(code + "convertednum").toString());
                                                    i += 1;
                                                    themap.put(code + "convertednum", i);
                                                }
                                            } else {
                                                themap.put(code + "convertednum", map.get(code) != null ? 1 : 0);
                                            }
                                        }
                                        break;

                                    }
                                }
                            }
                        }
                    }
                    for (String code : pollutantlist) {
                        String concentration = "";
                        String flow = "";
                        String converted = "";
                        if (themap.get(code + "concentration") != null && !"".equals(themap.get(code + "concentration").toString()) && themap.get(code + "concentrationnum") != null && !"".equals(themap.get(code + "concentrationnum").toString())) {
                            Double codeconcentration = Double.parseDouble(themap.get(code + "concentration").toString());
                            int codeconcentrationnum = Integer.parseInt(themap.get(code + "concentrationnum").toString());
                            if (codeconcentrationnum > 0) {
                                concentration = DataFormatUtil.formatDoubleSaveTwo(codeconcentration / codeconcentrationnum);
                            }
                            if (llpollutants.contains(code)) {
                               /* if (!"".equals(ll_unit)) {
                                    String ll_value = countFlowPollutantValue(concentration, ll_unit, codeconcentrationnum * 24);
                                    resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                }*/
                                String week_value = "";
                                if (ll_map.get(mn) != null) {
                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(mn);
                                    for (Map<String, Object> ljllmap : lllist) {
                                        Object obj3 = getWeekTimeByTimeStr(ljllmap.get("monitortime").toString());
                                        if ((obj.toString()).equals(obj3.toString())) {//同一周时
                                            if (!"".equals(ljllmap.get("ljll").toString())) {
                                                if ("".equals(week_value)) {
                                                    week_value = Double.valueOf(ljllmap.get("ljll").toString()) + "";
                                                } else {
                                                    week_value = Double.valueOf(week_value) + Double.valueOf(ljllmap.get("ljll").toString()) + "";
                                                }

                                            }
                                        }
                                    }
                                }
                                if (!"".equals(week_value)) {
                                    ll_num += 1;
                                    //总累计流量
                                    ll_total += Double.valueOf(week_value);
                                    //累计流量最小值
                                    if ("".equals(ll_min)) {
                                        ll_min = week_value + "";
                                    } else {
                                        if (Double.valueOf(ll_min) > Double.valueOf(week_value)) {
                                            ll_min = week_value + "";
                                        }
                                    }
                                    //累计流量最大值
                                    if ("".equals(ll_max)) {
                                        ll_max = week_value + "";
                                    } else {
                                        if (Double.valueOf(ll_max) < Double.valueOf(week_value)) {
                                            ll_max = week_value + "";
                                        }
                                    }
                                }
                                resultmap.put(code + "concentration" + "_ljll", week_value);
                            } else {
                                resultmap.put(code + "concentration" + "_ljll", null);
                            }
                        }
                        if (themap.get(code + "flow") != null && !"".equals(themap.get(code + "flow").toString()) && themap.get(code + "flownum") != null && !"".equals(themap.get(code + "flownum").toString())) {
                            Double codeflow = Double.parseDouble(themap.get(code + "flow").toString());
                            int codeflownum = Integer.parseInt(themap.get(code + "flownum").toString());
                            if (codeflownum > 0) {
                                //flow = DataFormatUtil.formatDoubleSaveTwo(codeflow / codeflownum);
                                //排放量算总和  不算平均
                                flow = DataFormatUtil.formatDoubleSaveTwo(codeflow);
                            }
                        }
                        if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                            if (themap.get(code + "converted") != null && !"".equals(themap.get(code + "converted").toString()) && themap.get(code + "convertednum") != null && !"".equals(themap.get(code + "convertednum").toString())) {
                                Double codeconverted = Double.parseDouble(themap.get(code + "converted").toString());
                                int codeconvertednum = Integer.parseInt(themap.get(code + "convertednum").toString());
                                if (codeconvertednum > 0) {
                                    converted = DataFormatUtil.formatDoubleSaveTwo(codeconverted / codeconvertednum);
                                }
                            }
                        }
                        if ("1".equals(tabletitletype)) {
                            String str = "";
                            if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                str = "#false";
                            } else {
                                str = isExceedStandardValue(code, concentration, mn, standlist);
                            }
                            resultmap.put(code + "concentration" + str, !"".equals(concentration) ? Double.valueOf(concentration) : null);
                        } else {
                            resultmap.put(code + "concentration", !"".equals(concentration) ? Double.valueOf(concentration) : null);
                        }
                        if (flowlist != null && flowlist.size() > 0) {
                            resultmap.put(code + "flow", !"".equals(flow) ? Double.valueOf(flow) : null);
                        }
                        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                            if (codeandflag != null && codeandflag.get(code) != null) {
                                if ("1".equals(tabletitletype)) {
                                    String str = isExceedStandardValue(code, converted, mn, standlist);
                                    resultmap.put(code + "converted" + str, !"".equals(converted) ? Double.valueOf(converted) : null);
                                } else {
                                    resultmap.put(code + "converted", !"".equals(converted) ? Double.valueOf(converted) : null);
                                }
                            }
                        }
                        //判断map中是否有该污染物的数据
                        boolean flag = countmap.containsKey(code);
                        if (flag == true) {//有
                            Map<String, Double> onemap = (Map<String, Double>) countmap.get(code);

                            if (!"".equals(flow)) {//判断是否有该因子的监测值
                                if (!"".equals(onemap.get("totalflow"))) {//总排放量
                                    Double totalflow = onemap.get("totalflow");
                                    totalflow = totalflow + Double.parseDouble(flow);
                                    onemap.put("totalflow", Double.valueOf(DataFormatUtil.formatDoubleSaveThree(totalflow)));
                                }
                                if (!"".equals(onemap.get("minflow"))) {//最小排放量
                                    Double minflow = onemap.get("minflow");
                                    if (minflow > Double.parseDouble(flow)) {
                                        minflow = Double.parseDouble(flow);
                                    }
                                    onemap.put("minflow", Double.valueOf(DataFormatUtil.formatDoubleSaveThree(minflow)));
                                }
                                if (!"".equals(onemap.get("maxflow"))) {//最大排放量
                                    Double maxflow = onemap.get("maxflow");
                                    if (maxflow < Double.parseDouble(flow)) {
                                        maxflow = Double.parseDouble(flow);
                                    }
                                    onemap.put("maxflow", Double.valueOf(DataFormatUtil.formatDoubleSaveThree(maxflow)));
                                }
                                if (!"".equals(onemap.get("flownum"))) {//次数
                                    Double flownum = onemap.get("flownum");
                                    flownum += 1;
                                    onemap.put("flownum", flownum);
                                }
                            }

                            if (!"".equals(concentration)) {//判断是否有该因子的监测值
                                if (!"".equals(onemap.get("totalconcentration"))) {//总浓度
                                    Double totalconcentration = onemap.get("totalconcentration");
                                    totalconcentration = totalconcentration + Double.parseDouble(concentration);
                                    onemap.put("totalconcentration", totalconcentration);
                                    //总天数
                                    //onemap.put("weekday_totalnum", onemap.get("weekday_totalnum")+weekdaynum);
                                }
                                if (!"".equals(onemap.get("minconcentration"))) {//最小浓度
                                    Double minconcentration = onemap.get("minconcentration");
                                    Double weekday_minnum = onemap.get("weekday_minnum");
                                    if (minconcentration > Double.parseDouble(concentration)) {
                                        minconcentration = Double.parseDouble(concentration);
                                        weekday_minnum = weekdaynum;
                                    }
                                    onemap.put("minconcentration", minconcentration);
                                    onemap.put("weekday_minnum", weekday_minnum);
                                }
                                if (!"".equals(onemap.get("maxconcentration"))) {//最大浓度
                                    Double maxconcentration = onemap.get("maxconcentration");
                                    Double weekday_maxnum = onemap.get("weekday_maxnum");
                                    if (maxconcentration < Double.parseDouble(concentration)) {
                                        maxconcentration = Double.parseDouble(concentration);
                                        weekday_maxnum = weekdaynum;
                                    }
                                    onemap.put("maxconcentration", maxconcentration);
                                    onemap.put("weekday_maxnum", weekday_maxnum);
                                }
                                if (!"".equals(onemap.get("concentrationnum"))) {//次数
                                    Double concentrationnum = onemap.get("concentrationnum");
                                    concentrationnum += 1;
                                    onemap.put("concentrationnum", concentrationnum);
                                }

                            }
                            if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                                if (!"".equals(converted)) {//判断是否有该因子的折算值
                                    if (!"".equals(onemap.get("totalconverted"))) {//总排放量
                                        Double totalconverted = onemap.get("totalconverted");
                                        totalconverted = totalconverted + Double.parseDouble(converted);
                                        onemap.put("totalconverted", totalconverted);
                                    }
                                    if (!"".equals(onemap.get("minconverted"))) {//最小排放量
                                        Double minconverted = onemap.get("minconverted");
                                        if (minconverted > Double.parseDouble(converted)) {
                                            minconverted = Double.parseDouble(converted);
                                        }
                                        onemap.put("minconverted", minconverted);
                                    }
                                    if (!"".equals(onemap.get("maxconverted"))) {//最大排放量
                                        Double maxconverted = onemap.get("maxconverted");
                                        if (maxconverted < Double.parseDouble(converted)) {
                                            maxconverted = Double.parseDouble(converted);
                                        }
                                        onemap.put("maxconverted", maxconverted);
                                    }
                                    if (!"".equals(onemap.get("convertednum"))) {//次数
                                        Double convertednum = onemap.get("convertednum");
                                        convertednum += 1;
                                        onemap.put("convertednum", convertednum);
                                    }
                                }
                            }
                            if (onemap.size() > 0) {
                                countmap.put(code, onemap);
                            }
                        } else {//无
                            Map<String, Double> twomap = new HashMap<>();
                            if (!"".equals(flow)) {//判断是否有该因子的排放量值
                                twomap.put("totalflow", Double.parseDouble(flow));
                                twomap.put("minflow", Double.parseDouble(flow));
                                twomap.put("maxflow", Double.parseDouble(flow));
                                twomap.put("flownum", 1.0);

                            }
                            if (!"".equals(concentration)) {//判断是否有该因子的监测值
                                twomap.put("totalconcentration", Double.parseDouble(concentration));
                                twomap.put("minconcentration", Double.parseDouble(concentration));
                                twomap.put("maxconcentration", Double.parseDouble(concentration));
                                twomap.put("concentrationnum", 1.0);
                                // twomap.put("weekday_totalnum",weekdaynum);
                                twomap.put("weekday_minnum", weekdaynum);
                                twomap.put("weekday_maxnum", weekdaynum);

                            }
                            if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                                if (!"".equals(converted)) {//判断是否有该因子的折算值
                                    twomap.put("totalconverted", Double.parseDouble(converted));
                                    twomap.put("minconverted", Double.parseDouble(converted));
                                    twomap.put("maxconverted", Double.parseDouble(converted));
                                    twomap.put("convertednum", 1.0);
                                }
                            }
                            if (twomap.size() > 0) {
                                countmap.put(code, twomap);
                            }
                        }
                    }
                    result.add(resultmap);

                } else {
                    continue;
                }
            }
            Map<String, Object> theavgmap = new HashMap<>();
            theavgmap.put("outputname", "平均值");//
            theavgmap.put("monitortime", "平均值");
            theavgmap.put("monitortimes", "-");//用于导出时取值
            Map<String, Object> theminmap = new HashMap<>();
            theminmap.put("outputname", "最小值");//
            theminmap.put("monitortime", "最小值");
            theminmap.put("monitortimes", "-");//用于导出时取值
            Map<String, Object> themaxmap = new HashMap<>();
            themaxmap.put("outputname", "最大值");//
            themaxmap.put("monitortime", "最大值");
            themaxmap.put("monitortimes", "-");//用于导出时取值
            Map<String, Object> thetotalmap = new HashMap<>();
            thetotalmap.put("outputname", "总排放量");//
            thetotalmap.put("monitortime", "总排放量");
            thetotalmap.put("monitortimes", "-");//用于导出时取值
            for (String pollutantcode : pollutantlist) {
                Map<String, Double> valuemap = (Map<String, Double>) countmap.get(pollutantcode);
                if (valuemap != null && valuemap.size() > 0) {
                    Double totalconcentration = 0d;
                    Double concentrationnum = 0d;
                    Double totalflow = 0d;
                    Double flownum = 0d;
                    Double totalconverted = 0d;
                    Double convertednum = 0d;
                    Double min_nd = 0d;
                    Double max_nd = 0d;
                    if (valuemap.get("totalconcentration") != null && !"".equals(valuemap.get("totalconcentration"))) {//总浓度
                        totalconcentration = valuemap.get("totalconcentration");
                    }
                    if (valuemap.get("minconcentration") != null && !"".equals(valuemap.get("minconcentration"))) {//最小浓度
                        Double minconcentration = valuemap.get("minconcentration");
                        min_nd = minconcentration;
                        theminmap.put(pollutantcode + "concentration", minconcentration);
                    } else {
                        theminmap.put(pollutantcode + "concentration", null);
                    }

                    if (valuemap.get("maxconcentration") != null && !"".equals(valuemap.get("maxconcentration"))) {//最大浓度
                        Double maxconcentration = valuemap.get("maxconcentration");
                        max_nd = maxconcentration;
                        themaxmap.put(pollutantcode + "concentration", maxconcentration);
                    } else {
                        themaxmap.put(pollutantcode + "concentration", null);
                    }

                    if (valuemap.get("concentrationnum") != null && !"".equals(valuemap.get("concentrationnum"))) {//次数
                        concentrationnum = valuemap.get("concentrationnum");
                    }
                    if (concentrationnum > 0) {
                        theavgmap.put(pollutantcode + "concentration", Double.valueOf(DataFormatUtil.formatDoubleSaveTwo(totalconcentration / concentrationnum)));
                    } else {
                        theavgmap.put(pollutantcode + "concentration", null);
                    }
                    //判断流量污染物
                    if (llpollutants.contains(pollutantcode)) {
                        //if (!"".equals(ll_unit)) {
                           /* Double avgvalue = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(totalconcentration / concentrationnum));
                            //Double ll_totalnum = valuemap.get("weekday_totalnum");
                            Double ll_minnum = valuemap.get("weekday_minnum");
                            Double ll_maxnum = valuemap.get("weekday_maxnum");
                            String ll_value = countFlowPollutantValue(avgvalue, ll_unit, 7 * 24);
                            theavgmap.put(pollutantcode + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                            String ll_minvalue = countFlowPollutantValue(min_nd, ll_unit, ll_minnum * 24);
                            theminmap.put(pollutantcode + "concentration" + "_ljll", !"".equals(ll_minvalue) ? Double.valueOf(ll_minvalue) : null);
                            String ll_maxvalue = countFlowPollutantValue(max_nd, ll_unit, ll_maxnum * 24);
                            themaxmap.put(pollutantcode + "concentration" + "_ljll", !"".equals(ll_maxvalue) ? Double.valueOf(ll_maxvalue) : null);*/
                        String avgvalue = "";
                        if (ll_num > 0) {
                            avgvalue = DataFormatUtil.formatDoubleSaveTwo(ll_total / ll_num) + "";
                        }
                        //Double ll_totalnum = valuemap.get("weekday_totalnum");
                        Double ll_minnum = ll_min != "" ? Double.valueOf(ll_min) : null;
                        Double ll_maxnum = ll_max != "" ? Double.valueOf(ll_max) : null;
                        theavgmap.put(pollutantcode + "concentration" + "_ljll", !"".equals(avgvalue) ? Double.valueOf(avgvalue) : null);
                        theminmap.put(pollutantcode + "concentration" + "_ljll", ll_minnum);
                        themaxmap.put(pollutantcode + "concentration" + "_ljll", ll_maxnum);
                        //}
                    }
                    thetotalmap.put(pollutantcode + "concentration", "");
                    if (valuemap.get("totalflow") != null && !"".equals(valuemap.get("totalflow"))) {//总排放量
                        totalflow = valuemap.get("totalflow");
                        BigDecimal bg = new BigDecimal(totalflow + "");
                        thetotalmap.put(pollutantcode + "flow", !"".equals(bg.toString()) ? Double.valueOf(bg.toString()) : null);
                    } else {
                        thetotalmap.put(pollutantcode + "flow", null);
                    }

                    if (valuemap.get("minflow") != null && !"".equals(valuemap.get("minflow"))) {//最小排放量
                        Double minflow = valuemap.get("minflow");
                        BigDecimal bg = new BigDecimal(minflow + "");
                        theminmap.put(pollutantcode + "flow", !"".equals(bg.toString()) ? Double.valueOf(bg.toString()) : null);
                    } else {
                        theminmap.put(pollutantcode + "flow", null);
                    }

                    if (valuemap.get("maxflow") != null && !"".equals(valuemap.get("maxflow"))) {//最大排放量
                        Double maxflow = valuemap.get("maxflow");
                        BigDecimal bg = new BigDecimal(maxflow + "");
                        themaxmap.put(pollutantcode + "flow", !"".equals(bg.toString()) ? Double.valueOf(bg.toString()) : null);
                    } else {
                        themaxmap.put(pollutantcode + "flow", null);
                    }

                    if (valuemap.get("flownum") != null && !"".equals(valuemap.get("flownum"))) {//次数
                        flownum = valuemap.get("flownum");

                    }
                    if (flownum > 0) {
                        theavgmap.put(pollutantcode + "flow", Double.valueOf(DataFormatUtil.formatDoubleSaveTwo(totalflow / flownum)));
                    } else {
                        theavgmap.put(pollutantcode + "flow", null);
                    }

                    if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                        if (valuemap.get("totalconverted") != null && !"".equals(valuemap.get("totalconverted"))) {//总浓度
                            totalconverted = valuemap.get("totalconverted");
                        }
                        if (valuemap.get("minconverted") != null && !"".equals(valuemap.get("minconverted"))) {//最小浓度
                            Double minconverted = valuemap.get("minconverted");
                            theminmap.put(pollutantcode + "converted", minconverted);
                        } else {
                            theminmap.put(pollutantcode + "converted", null);
                        }

                        if (valuemap.get("maxconverted") != null && !"".equals(valuemap.get("maxconverted"))) {//最大浓度
                            Double maxconverted = valuemap.get("maxconverted");
                            themaxmap.put(pollutantcode + "converted", maxconverted);
                        } else {
                            themaxmap.put(pollutantcode + "converted", null);
                        }

                        if (valuemap.get("convertednum") != null && !"".equals(valuemap.get("convertednum"))) {//次数
                            convertednum = valuemap.get("convertednum");
                        }
                        if (convertednum > 0) {
                            theavgmap.put(pollutantcode + "converted", Double.valueOf(DataFormatUtil.formatDoubleSaveTwo(totalconverted / convertednum)));
                        } else {
                            theavgmap.put(pollutantcode + "converted", null);
                        }
                    }
                } else {
                    theminmap.put(pollutantcode + "concentration", null);
                    theminmap.put(pollutantcode + "flow", null);
                    themaxmap.put(pollutantcode + "concentration", null);
                    themaxmap.put(pollutantcode + "flow", null);
                    theavgmap.put(pollutantcode + "concentration", null);
                    theavgmap.put(pollutantcode + "flow", null);
                    thetotalmap.put(pollutantcode + "concentration", null);
                    thetotalmap.put(pollutantcode + "flow", null);
                    if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                        theminmap.put(pollutantcode + "converted", null);
                        themaxmap.put(pollutantcode + "converted", null);
                        theavgmap.put(pollutantcode + "converted", null);
                        thetotalmap.put(pollutantcode + "converted", null);
                    }
                }
            }
            result.add(theavgmap);
            result.add(theminmap);
            result.add(themaxmap);
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                result.add(thetotalmap);
            }
            if (num < mnlist.size()) {
                result.add(new HashMap<>());
            }
            num++;
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/31 0031 下午 1:14
     * @Description:获取企业月报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getEntDataMonthReportData(Map<String, Object> datamap, List<String> mnlist, List<String> pollutantlist, List<Map<String, Object>> pollutions, String pointtype, Map<String, Object> ll_map) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        String ll_unit = "";//获取流量污染物单位
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> concentrations = (Map<String, Object>) datamap.get("concentrations");
        Map<String, Object> convertedvalues = (Map<String, Object>) datamap.get("convertedvalues");
        Map<String, Object> flowmap = (Map<String, Object>) datamap.get("flowmap");
        String monitortime = (String) datamap.get("monitortime");
        List<Map<String, Object>> standlist = (List<Map<String, Object>>) datamap.get("standlist");
        String tabletitletype = "1";//(String) datamap.get("tabletitletype");
        //获取该月所有日期
        Date someTime = DataFormatUtil.getDateYM(monitortime);
        List<String> timelist = DataFormatUtil.getDayListOfMonth(someTime);
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //废气、烟气查污染物数据判断是否有折算
        Map<String, Object> codeandflag = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
       /* if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> map : pollutants) {
                //获取流量污染物单位
                if (map.get("code") != null && llpollutants.contains(map.get("code").toString())) {
                    ll_unit = map.get("PollutantUnit") != null ? map.get("PollutantUnit").toString() : "";
                    break;
                }
            }
        }*/
        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
        }
        int num = 1;
        for (String mn : mnlist) {//遍历企业下排口MN号
            String outputname = "";
            for (Map<String, Object> obj : pollutions) {
                if (mn.equals(obj.get("DGIMN").toString())) {
                    outputname = obj.get("outputname") != null ? obj.get("outputname").toString() : (obj.get("OutputName") != null ? obj.get("OutputName").toString() : "");
                }
            }
            List<Map<String, Object>> concentlist = (List<Map<String, Object>>) concentrations.get(mn);
            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(mn);
            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(mn);

            String ll_min = "";
            Double ll_total = 0d;
            String ll_max = "";
            String ll_avg = "";
            Double ll_num = 0d;
            for (String thetime : timelist) {
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("outputname", outputname);//
                resultmap.put("monitortime", thetime);//
                resultmap.put("monitortimes", thetime);//用于导出时取值
                boolean flag = false;
                boolean flag2 = false;
                boolean flag3 = false;
                //浓度
                for (Map<String, Object> map : concentlist) {
                    if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                        for (String code : pollutantlist) {
                            if ("1".equals(tabletitletype)) {
                                String str = "";
                                if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                    str = "#false";
                                } else {
                                    str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                }
                                resultmap.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                            } else {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                            }
                            if (llpollutants.contains(code)) {
                                /*if (!"".equals(ll_unit)) {
                                    String ll_value = countFlowPollutantValue(map.get(code), ll_unit, 24);
                                    resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                }*/
                                if (ll_map.get(mn) != null) {
                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(mn);
                                    for (Map<String, Object> ljllmap : lllist) {
                                        if (thetime.equals(ljllmap.get("monitortime").toString())) {//当时间相等时
                                            resultmap.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                            if (!"".equals(ljllmap.get("ljll").toString())) {
                                                ll_num += 1;
                                                //总累计流量
                                                ll_total += Double.valueOf(ljllmap.get("ljll").toString());
                                                //累计流量最小值
                                                if (ll_min.equals("")) {
                                                    ll_min = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_min) > Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_min = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                                //累计流量最大值
                                                if (ll_max.equals("")) {
                                                    ll_max = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_max) < Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_max = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    for (String code : pollutantlist) {
                        if ("1".equals(tabletitletype)) {
                            resultmap.put(code + "concentration#false", null);
                        } else {
                            resultmap.put(code + "concentration", null);
                        }

                    }
                }
                //排放量
                if (flowlist != null && flowlist.size() > 0) {
                    for (Map<String, Object> map : flowlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", getDoubleFlowValueForStringValue(map, code));
                            }
                            flag2 = true;
                            break;
                        }
                    }
                    if (flag2 == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "flow", null);
                        }
                    }
                }
                if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                    //折算值
                    for (Map<String, Object> map : convertedlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                if (codeandflag != null && codeandflag.get(code) != null) {
                                    if ("1".equals(tabletitletype)) {
                                        String str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                        resultmap.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                    } else {
                                        resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                    }
                                }
                            }
                            flag3 = true;
                            break;
                        }
                    }
                    if (flag3 == false) {
                        for (String code : pollutantlist) {
                            if (codeandflag != null && codeandflag.get(code) != null) {
                                if ("1".equals(tabletitletype)) {
                                    resultmap.put(code + "converted#false", null);
                                } else {
                                    resultmap.put(code + "converted", null);
                                }
                            }
                        }
                    }
                }
                result.add(resultmap);
            }
            //计算累计流量平均值
            if (ll_num > 0) {
                ll_avg = DataFormatUtil.formatDoubleSaveTwo(ll_total / ll_num) + "";
            }
            boolean flag = false;
            boolean flag2 = false;
            boolean flag3 = false;
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                String[] strs = new String[]{"平均值", "最小值", "最大值", "总排放量"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//用于导出时取值
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                if (llpollutants.contains(code)) {
                                    /*if (!"".equals(ll_unit)) {
                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, 24);
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    }*/
                                    if (str.equals("平均值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_avg) ? Double.valueOf(ll_avg) : null);
                                    } else if (str.equals("最小值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_min) ? Double.valueOf(ll_min) : null);
                                    } else if (str.equals("最大值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_max) ? Double.valueOf(ll_max) : null);
                                    }
                                }
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    //排放量
                    if (flowlist != null && flowlist.size() > 0) {
                        for (Map<String, Object> map : flowlist) {
                            if (str.equals(map.get("monitortime").toString())) {//当时间相等时
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "flow", getDoubleFlowValueForStringValue(map, code));
                                }
                                flag2 = true;
                                break;
                            }
                        }
                        if (flag2 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", null);
                            }
                        }
                    }
                    if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                        for (Map<String, Object> map : convertedlist) {
                            if (str.equals(map.get("monitortime").toString())) {//平均值
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                }
                                flag3 = true;
                                break;
                            }
                        }
                        if (flag3 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "converted", null);
                            }
                        }
                    }
                    result.add(resultmap);
                }
            } else {
                String[] strs = new String[]{"平均值", "最小值", "最大值"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//用于导出时取值
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", map.get(code) != null ? map.get(code).toString() : "");
                                /*if (llpollutants.contains(code)) {
                                    if (!"".equals(ll_unit)) {
                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, 24);
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    }
                                }*/
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    result.add(resultmap);
                }
            }
            if (num < mnlist.size()) {
                result.add(new HashMap<>());
            }
            num++;
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/31 0031 下午 1:14
     * @Description:获取企业年报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getEntDataYearReportData(Map<String, Object> datamap, List<String> mnlist, List<String> pollutantlist, List<Map<String, Object>> pollutions, String pointtype, Map<String, Object> ll_map) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        String ll_unit = "";//获取流量污染物单位
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> concentrations = (Map<String, Object>) datamap.get("concentrations");
        Map<String, Object> convertedvalues = (Map<String, Object>) datamap.get("convertedvalues");
        Map<String, Object> flowmap = (Map<String, Object>) datamap.get("flowmap");
        String monitortime = (String) datamap.get("monitortime");
        List<Map<String, Object>> standlist = (List<Map<String, Object>>) datamap.get("standlist");
        String tabletitletype = "1";//(String) datamap.get("tabletitletype");
        //获取该年所有月
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //废气、烟气查污染物数据判断是否有折算
        Map<String, Object> codeandflag = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
       /* if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> map : pollutants) {
                //获取流量污染物单位
                if (map.get("code") != null && llpollutants.contains(map.get("code").toString())) {
                    ll_unit = map.get("PollutantUnit") != null ? map.get("PollutantUnit").toString() : "";
                    break;
                }
            }
        }*/
        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
        }
        List<String> timelist = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String thetime = "";
            if (i < 10) {
                thetime = monitortime + "-0" + i;
            } else {
                thetime = monitortime + "-" + i;
            }
            timelist.add(thetime);
        }
        int num = 1;
        for (String mn : mnlist) {//遍历企业下排口MN号
            String outputname = "";
            for (Map<String, Object> obj : pollutions) {
                if (mn.equals(obj.get("DGIMN").toString())) {
                    outputname = obj.get("outputname") != null ? obj.get("outputname").toString() : (obj.get("OutputName") != null ? obj.get("OutputName").toString() : "");
                }
            }
            List<Map<String, Object>> concentlist = (List<Map<String, Object>>) concentrations.get(mn);
            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(mn);
            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(mn);
            double minvalue = 0d;
            double maxvalue = 0d;
            String ll_min = "";
            Double ll_total = 0d;
            String ll_max = "";
            String ll_avg = "";
            Double ll_num = 0d;
            for (String thetime : timelist) {
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("outputname", outputname);//
                resultmap.put("monitortime", thetime);//
                resultmap.put("monitortimes", thetime);//用于导出时取值
                boolean flag = false;
                boolean flag2 = false;
                boolean flag3 = false;
                //浓度
                for (Map<String, Object> map : concentlist) {
                    if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                        for (String code : pollutantlist) {
                            if ("1".equals(tabletitletype)) {
                                String str = "";
                                if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                    str = "#false";
                                } else {
                                    str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                }
                                resultmap.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                            } else {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                            }
                            if (llpollutants.contains(code)) {
                                if (ll_map.get(mn) != null) {
                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(mn);
                                    for (Map<String, Object> ljllmap : lllist) {
                                        if (thetime.equals(ljllmap.get("monitortime").toString())) {//当时间相等时
                                            resultmap.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                            if (!"".equals(ljllmap.get("ljll").toString())) {
                                                ll_num += 1;
                                                //总累计流量
                                                ll_total += Double.valueOf(ljllmap.get("ljll").toString());
                                                //累计流量最小值
                                                if (ll_min.equals("")) {
                                                    ll_min = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_min) > Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_min = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                                //累计流量最大值
                                                if (ll_max.equals("")) {
                                                    ll_max = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_max) < Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_max = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                               /* if (!"".equals(ll_unit)) {
                                    //判断当前月份是否和查询月份相同
                                    String newym = "";
                                    if (month < 10) {
                                        newym = year + "-0" + month;
                                    } else {
                                        newym = year + "-" + month;
                                    }
                                    String ll_value = "";
                                    if (thetime.equals(newym)) {
                                        ll_value = countFlowPollutantValue(map.get(code), ll_unit, day * 24);
                                    } else {
                                        //获取当前年月的天数
                                        String ym_lastday = DataFormatUtil.getLastDayOfMonth(thetime);
                                        String[] ymstrs = ym_lastday.split("-");
                                        ll_value = countFlowPollutantValue(map.get(code), ll_unit, Double.parseDouble(ymstrs[2]) * 24);
                                    }
                                    resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    if (!"".equals(ll_value)) {
                                        if (minvalue == 0) {
                                            minvalue = !"".equals(ll_value) ? Double.parseDouble(ll_value) : null;
                                        } else {
                                            if (minvalue > Double.parseDouble(ll_value)) {
                                                minvalue = Double.parseDouble(ll_value);
                                            }
                                        }
                                        if (maxvalue == 0) {
                                            maxvalue = !"".equals(ll_value) ? Double.parseDouble(ll_value) : null;
                                        } else {
                                            if (maxvalue < Double.parseDouble(ll_value)) {
                                                maxvalue = Double.parseDouble(ll_value);
                                            }
                                        }
                                    }
                                }*/
                            }
                        }
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    for (String code : pollutantlist) {
                        if ("1".equals(tabletitletype)) {
                            resultmap.put(code + "concentration#false#-#-#-#正常", null);
                        } else {
                            resultmap.put(code + "concentration", null);
                        }

                    }
                }
                //排放量
                if (flowlist != null && flowlist.size() > 0) {
                    for (Map<String, Object> map : flowlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", getDoubleFlowValueForStringValue(map, code));
                            }
                            flag2 = true;
                            break;
                        }
                    }
                    if (flag2 == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "flow", null);
                        }
                    }
                }
                if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                    //折算值
                    for (Map<String, Object> map : convertedlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                if ("1".equals(tabletitletype)) {
                                    String str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                    resultmap.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                } else {
                                    resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                }
                            }
                            flag3 = true;
                            break;
                        }
                    }
                    if (flag3 == false) {
                        for (String code : pollutantlist) {
                            if ("1".equals(tabletitletype)) {
                                resultmap.put(code + "converted#false#-#-#-#正常", null);
                            } else {
                                resultmap.put(code + "converted", null);
                            }

                        }
                    }

                }
                result.add(resultmap);
            }
            //计算累计流量平均值
            if (ll_num > 0) {
                ll_avg = DataFormatUtil.formatDoubleSaveTwo(ll_total / ll_num) + "";
            }
            boolean flag = false;
            boolean flag2 = false;
            boolean flag3 = false;
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                String[] strs = new String[]{"平均值", "最小值", "最大值", "总排放量"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//用于导出时取值
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                if (llpollutants.contains(code)) {
                                    /*if (!"".equals(ll_unit)) {
                                        if ("最小值".equals(str)) {
                                            resultmap.put(code + "concentration" + "_ljll", minvalue);
                                        } else if ("最大值".equals(str)) {
                                            resultmap.put(code + "concentration" + "_ljll", maxvalue);
                                        } else {
                                            String ll_value = countFlowPollutantValue(map.get(code), ll_unit, 30 * 24);
                                            resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                        }
                                    }*/
                                    if (str.equals("平均值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_avg) ? Double.valueOf(ll_avg) : null);
                                    } else if (str.equals("最小值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_min) ? Double.valueOf(ll_min) : null);
                                    } else if (str.equals("最大值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_max) ? Double.valueOf(ll_max) : null);
                                    }
                                }
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    //排放量
                    if (flowlist != null && flowlist.size() > 0) {
                        for (Map<String, Object> map : flowlist) {
                            if (str.equals(map.get("monitortime").toString())) {//当时间相等时
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "flow", getDoubleFlowValueForStringValue(map, code));
                                }
                                flag2 = true;
                                break;
                            }
                        }
                        if (flag2 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", null);
                            }
                        }
                    }
                    if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                        for (Map<String, Object> map : convertedlist) {
                            if (str.equals(map.get("monitortime").toString())) {//平均值
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                }
                                flag3 = true;
                                break;
                            }
                        }
                        if (flag3 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "converted", null);
                            }
                        }
                    }
                    result.add(resultmap);
                }
            } else {
                String[] strs = new String[]{"平均值", "最小值", "最大值"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//用于导出时取值
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                /*if (llpollutants.contains(code)) {
                                    if (!"".equals(ll_unit)) {
                                        if ("最小值".equals(str)) {
                                            resultmap.put(code + "concentration" + "_ljll", minvalue);
                                        } else if ("最大值".equals(str)) {
                                            resultmap.put(code + "concentration" + "_ljll", maxvalue);
                                        } else {
                                            String ll_value = countFlowPollutantValue(map.get(code), ll_unit, 30 * 24);
                                            resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                        }
                                    }
                                }*/
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    result.add(resultmap);
                }
            }
            if (num < mnlist.size()) {
                result.add(new HashMap<>());
            }
            num++;
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/31 0031 下午 1:14
     * @Description:获取企业自定义报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getEntCustomReportData(Map<String, Object> datamap, List<String> mnlist, List<String> pollutantlist, List<Map<String, Object>> pollutions, String pointtype, Map<String, Object> ll_map) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        String ll_unit = "";//获取流量污染物单位
        String starttime = (String) datamap.get("starttime");
        String endtime = (String) datamap.get("endtime");
        double daynum = 24;
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> concentrations = (Map<String, Object>) datamap.get("concentrations");
        Map<String, Object> convertedvalues = (Map<String, Object>) datamap.get("convertedvalues");
        Map<String, Object> flowmap = (Map<String, Object>) datamap.get("flowmap");

        List<Map<String, Object>> standlist = (List<Map<String, Object>>) datamap.get("standlist");
        String tabletitletype = "1";//(String) datamap.get("tabletitletype");
        //获取该月所有日期
        List<String> timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
        timelist.add(endtime);
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //废气、烟气查污染物数据判断是否有折算
        Map<String, Object> codeandflag = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        /*if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> map : pollutants) {
                //获取流量污染物单位
                if (map.get("code") != null && llpollutants.contains(map.get("code").toString())) {
                    ll_unit = map.get("PollutantUnit") != null ? map.get("PollutantUnit").toString() : "";
                    break;
                }
            }
        }*/
        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
        }
        int num = 1;
        for (String mn : mnlist) {//遍历企业下排口MN号
            String outputname = "";
            for (Map<String, Object> obj : pollutions) {
                String objmn = obj.get("DGIMN") != null ? obj.get("DGIMN").toString() : obj.get("dgimn") != null ? obj.get("dgimn").toString() : "";
                if (mn.equals(objmn)) {
                    outputname = obj.get("outputname") != null ? obj.get("outputname").toString() : (obj.get("OutputName") != null ? obj.get("OutputName").toString() : "");
                }
            }
            List<Map<String, Object>> concentlist = (List<Map<String, Object>>) concentrations.get(mn);
            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(mn);
            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(mn);

            String ll_min = "";
            Double ll_total = 0d;
            String ll_max = "";
            String ll_avg = "";
            Double ll_num = 0d;
            for (String thetime : timelist) {
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("outputname", outputname);//
                resultmap.put("monitortime", thetime);//
                resultmap.put("monitortimes", thetime);//用于导出时取值
                boolean flag = false;
                boolean flag2 = false;
                boolean flag3 = false;
                //浓度
                for (Map<String, Object> map : concentlist) {
                    if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                        for (String code : pollutantlist) {
                            if ("1".equals(tabletitletype)) {
                                String str = "";
                                if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                    str = "#false";
                                } else {
                                    str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                }
                                resultmap.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                            } else {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                            }
                            if (llpollutants.contains(code)) {
                                /*if (!"".equals(ll_unit)) {
                                    String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                    resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                }*/
                                if (ll_map.get(mn) != null) {
                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(mn);
                                    for (Map<String, Object> ljllmap : lllist) {
                                        if (thetime.equals(ljllmap.get("monitortime").toString())) {//当时间相等时
                                            resultmap.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                            if (!"".equals(ljllmap.get("ljll").toString())) {
                                                ll_num += 1;
                                                //总累计流量
                                                ll_total += Double.valueOf(ljllmap.get("ljll").toString());
                                                //累计流量最小值
                                                if (ll_min.equals("")) {
                                                    ll_min = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_min) > Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_min = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                                //累计流量最大值
                                                if (ll_max.equals("")) {
                                                    ll_max = ljllmap.get("ljll").toString();
                                                } else {
                                                    if (Double.valueOf(ll_max) < Double.valueOf(ljllmap.get("ljll").toString())) {
                                                        ll_max = ljllmap.get("ljll").toString();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    for (String code : pollutantlist) {
                        if ("1".equals(tabletitletype)) {
                            resultmap.put(code + "concentration#false", null);
                        } else {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                }
                //排放量
                if (flowlist != null && flowlist.size() > 0) {
                    for (Map<String, Object> map : flowlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                //resultmap.put(code + "flow", map.get(code) != null ? map.get(code) : "");
                                resultmap.put(code + "flow", getDoubleValueForStringValue(map, code));
                            }
                            flag2 = true;
                            break;
                        }
                    }
                    if (flag2 == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "flow", null);
                        }
                    }
                }
                if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                    //折算值
                    for (Map<String, Object> map : convertedlist) {
                        if (thetime.equals(map.get("monitortime").toString())) {//当时间相等时
                            for (String code : pollutantlist) {
                                if (codeandflag != null && codeandflag.get(code) != null) {
                                    if ("1".equals(tabletitletype)) {
                                        String str = isExceedStandardValue(code, map.get(code), mn, standlist);
                                        resultmap.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                    } else {
                                        resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                    }
                                }
                            }
                            flag3 = true;
                            break;
                        }
                    }
                    if (flag3 == false) {
                        for (String code : pollutantlist) {
                            if (codeandflag != null && codeandflag.get(code) != null) {
                                if ("1".equals(tabletitletype)) {
                                    resultmap.put(code + "converted#false", null);
                                } else {
                                    resultmap.put(code + "converted", null);
                                }
                            }
                        }
                    }
                }
                result.add(resultmap);
            }
            //计算累计流量平均值
            if (ll_num > 0) {
                ll_avg = (ll_total / ll_num) + "";
            }
            boolean flag = false;
            boolean flag2 = false;
            boolean flag3 = false;
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                String[] strs = new String[]{"平均值", "最小值", "最大值", "总排放量"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//用于导出时取值
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                                if (llpollutants.contains(code)) {
                                   /* if (!"".equals(ll_unit)) {
                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    }*/
                                    if (str.equals("平均值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_avg) ? Double.valueOf(ll_avg) : null);
                                    } else if (str.equals("最小值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_min) ? Double.valueOf(ll_min) : null);
                                    } else if (str.equals("最大值")) {
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_max) ? Double.valueOf(ll_max) : null);
                                    }
                                }
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    //排放量
                    if (flowlist != null && flowlist.size() > 0) {
                        for (Map<String, Object> map : flowlist) {
                            if (str.equals(map.get("monitortime").toString())) {//当时间相等时
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "flow", getDoubleValueForStringValue(map, code));
                                }
                                flag2 = true;
                                break;
                            }
                        }
                        if (flag2 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "flow", null);
                            }
                        }
                    }
                    if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                        for (Map<String, Object> map : convertedlist) {
                            if (str.equals(map.get("monitortime").toString())) {//平均值
                                for (String code : pollutantlist) {
                                    resultmap.put(code + "converted", getDoubleValueForStringValue(map, code));
                                }
                                flag3 = true;
                                break;
                            }
                        }
                        if (flag3 == false) {
                            for (String code : pollutantlist) {
                                resultmap.put(code + "converted", null);
                            }
                        }
                    }
                    result.add(resultmap);
                }
            } else {
                String[] strs = new String[]{"平均值", "最小值", "最大值"};
                //将平均值、最大值、最小值等数据存入集合
                for (String str : strs) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("outputname", str);//
                    resultmap.put("monitortime", str);//
                    resultmap.put("monitortimes", "-");//用于导出时取值
                    for (Map<String, Object> map : concentlist) {
                        if (str.equals(map.get("monitortime").toString())) {//平均值
                            for (String code : pollutantlist) {
                                resultmap.put(code + "concentration", getDoubleValueForStringValue(map, code));
                              /*  if (llpollutants.contains(code)) {
                                    if (!"".equals(ll_unit)) {
                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                        resultmap.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                    }
                                } else {
                                    resultmap.put(code + "concentration" + "_ljll", null);
                                }*/
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            resultmap.put(code + "concentration", null);
                        }
                    }
                    result.add(resultmap);
                }
            }
            if (num < mnlist.size()) {
                result.add(new HashMap<>());
            }
            num++;
        }
        return result;
    }

    private String countFlowPollutantValue(Object o, String ll_unit, double hournum) {
        String flowvalue = "";
        if (o != null && !"".equals(o.toString())) {
            double value = 0d;
            if ("l/s".equals(ll_unit)) {
                value = ((Double.parseDouble(o.toString()) * 3600 * hournum) / 1000);
            } else if ("l/h".equals(ll_unit)) {
                value = ((Double.parseDouble(o.toString()) * hournum) / 1000);
            } else if ("m3/h".equals(ll_unit) || "m³/h".equals(ll_unit)) {
                value = ((Double.parseDouble(o.toString()) * hournum));
            } else if ("m3/s".equals(ll_unit) || "m³/s".equals(ll_unit)) {
                value = ((Double.parseDouble(o.toString()) * 3600 * hournum));
            }
            if (value == 0) {
                flowvalue = DataFormatUtil.formatDoubleSaveTwo(value);
            } else {
                flowvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(value));
            }
            //if (value>0) {

            // }
        }
        return flowvalue;
    }


    /**
     * @author: xsm
     * @date: 2019/8/01 0001 上午 11:34
     * @Description:根据报表类型和自定义参数获取企业汇总报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getSummaryEntDataReportByParamMap(Map<String, Object> paramMap) {
        try {
            List<String> llpollutants = Arrays.asList("b01", "b02");
            String ll_unit = "";//获取流量污染物单位
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DATE);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            int yearDay = cal.get(Calendar.DAY_OF_YEAR);
            //获取时间范围
            double daynum = 0d;
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            //监测点类型
            String pointtype = paramMap.get("pointtype").toString();
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
            Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
            Integer watermonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            List<String> lltypes = new ArrayList<>();
            lltypes.add(gasmonitortype + "");
            lltypes.add(smokemonitortype + "");
            lltypes.add(watermonitortype + "");
            //标题名称
            String titlename = "";
            //数据展示类型
            List<Integer> showtypes = new ArrayList<>();
            if (paramMap.get("showtypes") != null) {
                //数据展示类型
                showtypes = (List<Integer>) paramMap.get("showtypes");
            }
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            List<String> pollutionids = (List<String>) paramMap.get("pkids");
            //gasOutPutInfoMapper.selectByPollutionid(paramMap);
            String monitortime = paramMap.get("monitortime") != null ? paramMap.get("monitortime").toString() : "";
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<String> flowpollutantlist = new ArrayList<>();
            Date startDate = null;
            Date endDate = null;
            String collection = "";
            String datalistname = "";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateTime = null;
            List<String> flags = new ArrayList<>();

            if ("day".equals(reporttype)) {//日报
                collection = "HourData";
                datalistname = "HourDataList";
                startDate = DataFormatUtil.getDateYMDH(monitortime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDH(monitortime + " 23:59:59");
                daynum = 24;
                titlename = DataFormatUtil.getDateYMD1(DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00"));
            } else if ("month".equals(reporttype)) {//周报-月报
                flags = Arrays.asList("n", "N");
                collection = "DayData";
                datalistname = "DayDataList";
                dateTime = simpleDateFormat.parse(monitortime + "-01 00:00:00");
                List<String> timelist = DataFormatUtil.getDayListOfMonth(dateTime);
                startDate = DataFormatUtil.getDateYMD(timelist.get(0));
                endDate = DataFormatUtil.getDateYMD(timelist.get(timelist.size() - 1));
                String newym = "";
                if (month < 10) {
                    newym = year + "-0" + month;
                } else {
                    newym = year + "-" + month;
                }
                if (monitortime.equals(newym)) {
                    daynum = day * 24;
                } else {
                    //获取小时数
                    long beforeTime = startDate.getTime();
                    long afterTime = endDate.getTime();
                    daynum = (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
                    daynum = (daynum + 1) * 24;
                }
                String[] ymdate = monitortime.split("-");
                titlename = ymdate[0] + "年" + Integer.valueOf(ymdate[1]) + "月";
            } else if ("year".equals(reporttype)) {//年报
                flags = Arrays.asList("n", "N");
                collection = "MonthData";
                datalistname = "MonthDataList";
                startDate = DataFormatUtil.getDateYMD(monitortime + "-01-01");
                endDate = DataFormatUtil.getDateYMD(monitortime + "-12-31");
                if (Integer.valueOf(monitortime) < year) {
                    daynum = 365 * 24;
                } else {
                    if (monitortime.equals(year + "")) {
                        daynum = yearDay * 24;
                    } else {
                        daynum = 0;
                    }
                }
                titlename = monitortime + "年";
            } else if ("custom".equals(reporttype)) {//自定义

                flags = Arrays.asList("n", "N");

                collection = "DayData";
                datalistname = "DayDataList";
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
                //获取小时数
                long beforeTime = (DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).getTime();
                long afterTime = (DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00")).getTime();
                daynum = (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
                daynum = (daynum + 1) * 24;
                titlename = DataFormatUtil.getDateYMD1(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")) + "至" + DataFormatUtil.getDateYMD1(DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00"));
            }
            titlename = titlename + "污染物排放报表";
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //获取主要污染物和点位信息
            getKeyPollutantAndPointData(pollutions, paramMap, pointtype);
            //根据类型获取重点监测污染物
            if (paramMap.get("pollutantcodes") == null) {
                keypollutants = keyMonitorPollutantMapper.selectByPollutanttype(pointtype);
            }
            if (!CommonTypeEnum.getExcludeAuthTypeList().contains(pointtype)) {
                /*-----设置权限--------*/
                //设置权限 查询用户拥有权限的监测点dgimn
                List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
                paramMap.put("categorys", categorys);
                String sessionID = SessionUtil.getSessionID();
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                paramMap.put("userid", userid);
                List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
                List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

                //从查询出的监测点里筛选拥有权限的监测点
                pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
                /*-----设置权限end--------*/
            }

            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") != null ? map.get("DGIMN").toString() : map.get("dgimn") != null ? map.get("dgimn").toString() : "");
            }
            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    pollutantlist.add(map.get("FK_PollutantCode").toString());
                }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("codes", pollutantlist);
            param.put("pollutanttype", pointtype);
            List<Map<String, Object>> ll_pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
            if (ll_pollutants != null && ll_pollutants.size() > 0) {
                for (Map<String, Object> ll_map : ll_pollutants) {
                    //获取流量污染物单位
                    if (ll_map.get("code") != null && llpollutants.contains(ll_map.get("code").toString())) {
                        ll_unit = ll_map.get("PollutantUnit") != null ? ll_map.get("PollutantUnit").toString() : "";
                        break;
                    }
                }
            }
            //废气、烟气查污染物数据判断是否有折算
            Map<String, Object> codeandflag = new HashMap<>();
            if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
                if (pollutantlist != null && pollutantlist.size() > 0) {
                    for (Map<String, Object> map : ll_pollutants) {
                        if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                            codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                        }
                    }
                }
            }
            //获取因子标准值
            param.clear();
            param.put("mnlist", mnlist);
            param.put("pollutantlist", pollutantlist);
            param.put("monitorpointtype", pointtype);
            List<Map<String, Object>> standlist = pollutantFactorMapper.getPollutantStandarddataByParam(param);
            //排放量污染物
            flowpollutantlist = getFlowPollutantListByParamMap(pollutantlist, pointtype);
            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getTableTitleForSummaryEntDataReport(pollutantlist, pointtype, showtypes, standlist, flowpollutantlist, reporttype);
            } else {
                tables = getExportTableTitleForSummaryEntDataReport(pollutantlist, pointtype, flowpollutantlist, reporttype, showtypes, standlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            if (flags.size() > 0) {
                query.addCriteria(Criteria.where(datalistname + ".Flag").in(flags));
            }
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);


            Map<String, Object> flag_codeAndName = new HashMap<>();

            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", Arrays.asList(pointtype));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }


            //去mongo中查询浓度数据
            Map<String, Object> concentrations = getAllOutPutConcentrationData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
            Map<String, Object> flowmap = new HashMap<>();
            Map<String, Object> convertedvalues = new HashMap<>();
            //获取累计流量
            Map<String, Object> ll_map = new HashMap<>();
            if (lltypes.contains(pointtype)) {
                ll_map = getPointTotalFlowDataForSummaryReport(startDate, endDate, mnlist, reporttype);
            }
            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (Integer showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        flowmap = getAllOutPutFlowData(startDate, endDate, mnlist, flowpollutantlist, reporttype);
                    }
                    if (showtype == 1) {//折算值
                        //去mongo中查询折算数据
                        if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气或烟气的时候，有折算值
                            convertedvalues = getAllOutPutConvertedValueData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
                        }
                    }
                }
            }
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (String pollutionid : pollutionids) {
                String pollutionname = "";
                String outputname = "";
                for (Map<String, Object> obj : pollutions) {
                    String key = "";
                    if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "")
                            || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "")
                            || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")
                            || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "")
                            || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")
                            || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")
                            || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode() + "")) {
                        key = "pollutionid";
                    } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {
                        key = "PK_PollutionID";
                    } else {
                        key = "pkid";
                    }
                    if (obj.get(key) != null && pollutionid.equals(obj.get(key).toString())) {
                        pollutionname = obj.get("PollutionName") != null ? obj.get("PollutionName").toString() : obj.get("pollutionname") != null ? obj.get("pollutionname").toString() : "";
                        outputname = obj.get("OutputName") != null ? obj.get("OutputName").toString() : (obj.get("outputname") != null ? obj.get("outputname").toString() : "");
                        String MN = obj.get("DGIMN") != null ? obj.get("DGIMN").toString() : obj.get("dgimn") != null ? obj.get("dgimn").toString() : "";
                        boolean flag = false;
                        boolean flag2 = false;
                        boolean flag3 = false;
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("pollutionname", pollutionname);
                        map2.put("outputname", outputname);
                        if (concentrations.get(MN) != null) {
                            List<Map<String, Object>> concentralist = (List<Map<String, Object>>) concentrations.get(MN);
                            for (Map<String, Object> map : concentralist) {
                                if ("平均值".equals(map.get("monitortime").toString())) {
                                    for (String code : pollutantlist) {
                                        String str = "";
                                        if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                            str = "#false";
                                        } else {
                                            str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                        }
                                        map2.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                                        if (llpollutants.contains(code)) {
                                            /*if (!"".equals(ll_unit)) {
                                                String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                                map2.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                            }*/
                                            String ll_total = "";
                                            if (ll_map.get(MN) != null) {
                                                List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(MN);
                                                for (Map<String, Object> ljllmap : lllist) {
                                                    if ("".equals(ll_total)) {
                                                        ll_total = Double.valueOf(ljllmap.get("ljll").toString()) + "";
                                                    } else {
                                                        ll_total = Double.valueOf(ll_total) + Double.valueOf(ljllmap.get("ljll").toString()) + "";
                                                    }
                                                }
                                            }
                                            map2.put(code + "concentration" + "_ljll", !"".equals(ll_total) ? Double.valueOf(DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(ll_total)))) : null);
                                        }
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag == false) {
                            for (String code : pollutantlist) {
                                // if ("1".equals(tabletitletype)) {
                                map2.put(code + "concentration#false", null);
                                    /*} else {
                                        map2.put(code + "concentration", "");
                                    }*/
                                //map2.put(code + "concentration", "");
                            }
                        }
                        //排放量
                        if (flowmap.get(MN) != null) {
                            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(MN);
                            if (flowlist != null && flowlist.size() > 0) {
                                for (Map<String, Object> map : flowlist) {
                                    if ("总排放量".equals(map.get("monitortime").toString())) {
                                        for (String code : pollutantlist) {
                                            map2.put(code + "flow", getDoubleValueForStringValue(map, code));
                                            //map2.put(code + "concentration", map.get(code));
                                        }
                                        flag2 = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (flag2 == false) {
                            for (String code : pollutantlist) {
                                map2.put(code + "flow", null);
                            }
                        }
                        //折算值
                        if (convertedvalues.get(MN) != null) {
                            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(MN);
                            if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                                //折算值
                                for (Map<String, Object> map : convertedlist) {
                                    if ("平均值".equals(map.get("monitortime").toString())) {
                                        for (String code : pollutantlist) {
                                            if (codeandflag != null && codeandflag.get(code) != null) {
                                                String str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                                map2.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                            }
                                        }
                                        flag3 = true;
                                        break;
                                    }
                                }
                                if (flag3 == false) {
                                    for (String code : pollutantlist) {
                                        //if ("1".equals(tabletitletype)) {
                                        map2.put(code + "converted#false", null);
                                        /*} else {
                                            map2.put(code + "converted", "");
                                        }*/
                                        //map2.put(code + "converted", "");
                                    }
                                }
                            }
                        }
                        result.add(map2);
                    }
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            resultmap.put("titlename", titlename);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author: xsm
     * @date: 2020/12/15 0015 上午 09:06
     * @Description:获取合并恶臭的汇总报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getStinkSummaryEntDataReportByParamMap(Map<String, Object> paramMap) {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            Boolean isstink = false;//针对 恶臭（环境恶臭、厂界恶臭）
            if (paramMap.get("isstink") != null) {
                isstink = (Boolean) paramMap.get("isstink");
            }
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            List<String> pollutionids = (List<String>) paramMap.get("pkids");
            //gasOutPutInfoMapper.selectByPollutionid(paramMap);
            String monitortime = paramMap.get("monitortime") != null ? paramMap.get("monitortime").toString() : "";
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<Map<String, Object>> standlist = new ArrayList<>();
            Date startDate = null;
            Date endDate = null;
            //去mongo中查询浓度数据
            String collection = "";
            String datalistname = "";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateTime = null;
            if ("day".equals(reporttype)) {//日报
                collection = "HourData";
                datalistname = "HourDataList";
                startDate = DataFormatUtil.getDateYMDH(monitortime + " 00");
                endDate = DataFormatUtil.getDateYMDH(monitortime + " 23");
            } else if ("month".equals(reporttype)) {//周报-月报
                collection = "DayData";
                datalistname = "DayDataList";
                dateTime = simpleDateFormat.parse(monitortime + "-01 00:00:00");
                List<String> timelist = DataFormatUtil.getDayListOfMonth(dateTime);
                startDate = DataFormatUtil.getDateYMD(timelist.get(0));
                endDate = DataFormatUtil.getDateYMD(timelist.get(timelist.size() - 1));
            } else if ("year".equals(reporttype)) {//年报
                collection = "MonthData";
                datalistname = "MonthDataList";
                startDate = DataFormatUtil.getDateYMD(monitortime + "-01-01");
                endDate = DataFormatUtil.getDateYMD(monitortime + "-12-31");
            } else if ("custom".equals(reporttype)) {//自定义
                collection = "DayData";
                datalistname = "DayDataList";
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
            }
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //判断是否查合并恶臭数据
            if (isstink == true) {
                //获取恶臭点位信息及主要污染物信息（合并）
                paramMap.put("userid", userid);
                getStinkMonitorPointInfoAndKeyPollutantData(pollutions, keypollutants, paramMap);
                //获取因子标准值
            }
            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") != null ? map.get("DGIMN").toString() : map.get("dgimn") != null ? map.get("dgimn").toString() : "");
            }
            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    pollutantlist.add(map.get("FK_PollutantCode").toString());
                }
            }

            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getStinkTableTitleForSummaryEntDataReport(pollutantlist);
            } else {
                tables = getStinkExportTableTitleForSummaryEntDataReport(pollutantlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);

            Map<String, Object> flag_codeAndName = new HashMap<>();
            if (documents.size() > 0) {
                Map<String, Object> f_map = new HashMap<>();
                int type = Integer.parseInt(paramMap.get("pointtype").toString());
                f_map.put("monitorpointtypes", Arrays.asList(type));
                List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }
            }

            //去mongo中查询浓度数据
            Map<String, Object> concentrations = getAllOutPutConcentrationData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (String pollutionid : pollutionids) {
                String pollutionname = "";
                String outputname = "";
                for (Map<String, Object> obj : pollutions) {
                    String key = "";
                    String fktype = obj.get("FK_MonitorPointTypeCode") != null ? obj.get("FK_MonitorPointTypeCode").toString() : "";
                    if (fktype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "")) {
                        key = "pollutionid";
                    } else {
                        key = "pkid";
                    }
                    if (pollutionid.equals(obj.get(key).toString())) {
                        pollutionname = obj.get("PollutionName") != null ? obj.get("PollutionName").toString() : obj.get("pollutionname") != null ? obj.get("pollutionname").toString() : "";
                        outputname = obj.get("OutputName") != null ? obj.get("OutputName").toString() : (obj.get("outputname") != null ? obj.get("outputname").toString() : "");
                        String MN = obj.get("DGIMN") != null ? obj.get("DGIMN").toString() : obj.get("dgimn") != null ? obj.get("dgimn").toString() : "";
                        boolean flag = false;
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("pollutionname", pollutionname);
                        map2.put("outputname", outputname);
                        if (concentrations.get(MN) != null) {
                            List<Map<String, Object>> concentralist = (List<Map<String, Object>>) concentrations.get(MN);
                            for (Map<String, Object> map : concentralist) {
                                if ("平均值".equals(map.get("monitortime").toString())) {
                                    for (String code : pollutantlist) {
                                        map2.put(code + "concentration", map.get(code) != null ? map.get(code) : "");
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag == false) {
                            for (String code : pollutantlist) {
                                map2.put(code + "concentration", "");
                            }
                        }
                        result.add(map2);
                    }
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getStinkMonitorPointInfoAndKeyPollutantData(List<Map<String, Object>> pollutions, List<Map<String, Object>> keypollutants, Map<String, Object> paramMap) {
        //获取恶臭监测点MN号
        pollutions.addAll(otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap));
        //获取厂界恶臭监测点MN号
        pollutions.addAll(unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap));
        //根据类型获取主要监测污染物  默认环境恶臭
        keypollutants = keyMonitorPollutantMapper.selectByPollutanttype(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() + "");
    }

    /**
     * @author: xsm
     * @date: 2020/12/15 0015 上午 09:38
     * @Description: 获取恶臭（合并）汇总报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getStinkTableTitleForSummaryEntDataReport(List<String> pollutantlist) {
        List<Integer> list = new ArrayList<>();
        List<Map<String, Object>> pollutants = new ArrayList<>();
        List<String> postrs = new ArrayList<>();
        list.add(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        list.add(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("monitorpointtypes", list);
        List<Map<String, Object>> polist = pollutantFactorMapper.getPollutantsByPollutantType(param);
        if (polist != null && polist.size() > 0) {
            for (Map<String, Object> map : polist) {
                String pocode = map.get("code") != null ? map.get("code").toString() : "";
                if (!"".equals(pocode) && !postrs.contains(pocode)) {
                    pollutants.add(map);
                    postrs.add(pocode);
                }
            }
        }
        //获取所有关联企业的监测点类型
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        Map<String, Object> counttimenum = new HashMap<>();
        stationname.put("label", "企业名称");
        stationname.put("prop", "pollutionname");
        stationname.put("minwidth", "150px");
        stationname.put("headeralign", "center");
        stationname.put("fixed", "left");
        stationname.put("align", "center");
        stationname.put("showhide", true);
        dataList.add(stationname);

        counttimenum.put("label", "监测点名称");
        counttimenum.put("prop", "outputname");
        counttimenum.put("minwidth", "120px");
        counttimenum.put("headeralign", "center");
        counttimenum.put("fixed", "left");
        counttimenum.put("align", "center");
        counttimenum.put("showhide", true);
        dataList.add(counttimenum);


        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("label", name + ("".equals(unit) ? "" : "(" + unit + ")"));
            map.put("prop", code + "concentration");
            map.put("width", "100px");
            map.put("headeralign", "center");
            map.put("align", "center");
            map.put("showhide", true);
            dataList.add(map);


        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2020/12/15 0015 上午 11:32
     * @Description: 获取恶臭（合并）导出报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getStinkExportTableTitleForSummaryEntDataReport(List<String> pollutantlist) {
        List<Integer> list = new ArrayList<>();
        List<Map<String, Object>> pollutants = new ArrayList<>();
        List<String> postrs = new ArrayList<>();
        list.add(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        list.add(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("monitorpointtypes", list);
        List<Map<String, Object>> polist = pollutantFactorMapper.getPollutantsByPollutantType(param);
        if (polist != null && polist.size() > 0) {
            for (Map<String, Object> map : polist) {
                String pocode = map.get("code") != null ? map.get("code").toString() : "";
                if (!"".equals(pocode) && !postrs.contains(pocode)) {
                    pollutants.add(map);
                    postrs.add(pocode);
                }
            }
        }
        //获取所有关联企业的监测点类型
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        Map<String, Object> counttimenum = new HashMap<>();
        String rownum = "2";
        stationname.put("headername", "企业名称");
        stationname.put("headercode", "pollutionname");
        stationname.put("rownum", rownum);
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);

        counttimenum.put("headername", "监测点名称");
        counttimenum.put("headercode", "outputname");
        counttimenum.put("rownum", rownum);
        counttimenum.put("columnnum", "1");
        counttimenum.put("chlidheader", new ArrayList<>());
        dataList.add(counttimenum);

        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("headername", name + ("".equals(unit) ? "" : "(" + unit + ")"));
            map.put("headercode", code + "concentration");
            map.put("rownum", "1");
            map.put("columnnum", "1");
            map.put("chlidheader", new ArrayList<>());
            dataList.add(map);
        }
        return dataList;

    }

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取企业汇总报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getTableTitleForSummaryEntDataReport(List<String> pollutantlist, String pointtype, List<Integer> showtypes, List<Map<String, Object>> standlist, List<String> flowpollutantlist, String reporttype) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //获取所有关联企业的监测点类型
        List<Integer> pollutiontypes = CommonTypeEnum.getOutPutTypeList();
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        Map<String, Object> counttimenum = new HashMap<>();
        Map<String, Object> timestrs = new HashMap<>();
        String flowunit = "";
        if ("day".equals(reporttype)) {//日报
            flowunit = "千克.日";
        } else if ("week".equals(reporttype)) {//周报
            flowunit = "千克.周";
        } else if ("month".equals(reporttype)) {//月报
            flowunit = "千克.日";
        } else if ("year".equals(reporttype)) {//年报
            flowunit = "吨.月";
        } else if ("custom".equals(reporttype)) {//自定义
            flowunit = "千克";
        } else if ("hours".equals(reporttype)) {//小时时段
            flowunit = "千克.时";
        } else if ("days".equals(reporttype)) {//日时段
            flowunit = "千克.日";
        }
        if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
            stationname.put("label", "企业名称");
            stationname.put("prop", "pollutionname");
            stationname.put("minwidth", "150px");
            stationname.put("headeralign", "center");
            stationname.put("fixed", "left");
            stationname.put("align", "center");
            stationname.put("showhide", true);
            dataList.add(stationname);

            counttimenum.put("label", "排口名称");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")) {//厂界小型站
            stationname.put("label", "企业名称");
            stationname.put("prop", "pollutionname");
            stationname.put("minwidth", "150px");
            stationname.put("headeralign", "center");
            stationname.put("fixed", "left");
            stationname.put("align", "center");
            stationname.put("showhide", true);
            dataList.add(stationname);

            counttimenum.put("label", "监测点名称");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {//储罐
            stationname.put("label", "企业名称");
            stationname.put("prop", "pollutionname");
            stationname.put("minwidth", "150px");
            stationname.put("headeralign", "center");
            stationname.put("fixed", "left");
            stationname.put("align", "center");
            stationname.put("showhide", true);
            dataList.add(stationname);

            counttimenum.put("label", "储罐");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//生产装置
            stationname.put("label", "企业名称");
            stationname.put("prop", "pollutionname");
            stationname.put("minwidth", "150px");
            stationname.put("headeralign", "center");
            stationname.put("fixed", "left");
            stationname.put("align", "center");
            stationname.put("showhide", true);
            dataList.add(stationname);

            counttimenum.put("label", "生产装置");
        } else {
            counttimenum.put("label", "监测点名称");
        }
        counttimenum.put("prop", "outputname");
        counttimenum.put("minwidth", "120px");
        counttimenum.put("headeralign", "center");
        counttimenum.put("fixed", "left");
        counttimenum.put("align", "center");
        counttimenum.put("showhide", true);
        dataList.add(counttimenum);

        if ("hours".equals(reporttype) || "days".equals(reporttype)) {//小时时段、日时段
            timestrs.put("label", "监测时间");
            timestrs.put("prop", "timestr");
            timestrs.put("width", "120px");
            timestrs.put("headeralign", "center");
            timestrs.put("fixed", "left");
            timestrs.put("align", "center");
            timestrs.put("showhide", true);
            dataList.add(timestrs);
        }

        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                }
            }
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//若监测类型为储罐 则无排放量和折算值
                Map<String, Object> map = new HashMap<>();
                map.put("label", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                map.put("prop", code + "concentration");
                map.put("width", "100px");
                map.put("headeralign", "center");
                map.put("align", "center");
                map.put("showhide", true);
                dataList.add(map);
            } else {
                String str = "";
                Map<String, Object> map = new HashMap<>();
                List<String> monitortypes = pollutiontypes.stream().map(x -> x + "").collect(Collectors.toList());
                if (monitortypes.contains(pointtype)) {//若为关联企业的监测点类型  取企业名称
                    map.put("label", name);
                    map.put("prop", code);
                    map.put("headeralign", "center");
                    map.put("align", "center");
                    map.put("showhide", true);
                    List<Map<String, Object>> chlidheader = new ArrayList<>();

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("label", "实测值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                    map2.put("prop", code + "concentration");
                    map2.put("width", "100px");
                    map2.put("type", "concentratecolor");
                    map2.put("headeralign", "center");
                    map2.put("align", "center");
                    map2.put("showhide", true);
                    chlidheader.add(map2);

                    //判断当前污染物是否为流量
                    if (llpollutants.contains(code)) {
                        Map<String, Object> ll_map = new HashMap<>();
                        if ("b02".equals(code)) {
                            ll_map.put("label", "累计(m3)");
                        } else {
                            ll_map.put("label", "累计(t)");
                        }
                        ll_map.put("prop", code + "concentration_ljll");
                        ll_map.put("width", "100px");
                        ll_map.put("headeralign", "center");
                        ll_map.put("align", "center");
                        ll_map.put("showhide", true);
                        chlidheader.add(ll_map);
                    }

                    if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                        boolean flag = false;
                        for (String strcode : flowpollutantlist) {
                            if (code.equals(strcode)) {
                                flag = true;
                                break;
                            }
                        }
                        for (int showtype : showtypes) {
                            if (showtype == 0) {//排放量
                                //去mongo中查询排放量数据
                                if (flag == true) {
                                    Map<String, Object> map4 = new HashMap<>();
                                    map4.put("label", "排放量" + ("".equals(flowunit) ? "" : "(" + flowunit + ")"));
                                    map4.put("prop", code + "flow");
                                    map4.put("width", "100px");
                                    map4.put("headeralign", "center");
                                    map4.put("align", "center");
                                    map4.put("showhide", true);
                                    chlidheader.add(map4);
                                }
                            }
                            if (showtype == 1) {//折算值
                                //去mongo中查询折算数据
                                if (flag == true) {
                                    if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                                        Map<String, Object> map3 = new HashMap<>();
                                        map3.put("label", "折算值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                                        map3.put("prop", code + "converted");
                                        map3.put("width", "100px");
                                        map3.put("type", "concentratecolor");
                                        map3.put("headeralign", "center");
                                        map3.put("align", "center");
                                        map3.put("showhide", true);
                                        chlidheader.add(map3);
                                    }
                                }
                            }
                        }
                    }
                    dataList.add(map);
                    map.put("children", chlidheader);
                } else {
                    map.put("label", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                    map.put("prop", code + "concentration");
                    map.put("width", "100px");
                    map.put("headeralign", "center");
                    map.put("align", "center");
                    map.put("showhide", true);
                    dataList.add(map);
                }
            }
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取企业报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getExportTableTitleForSummaryEntDataReport(List<String> pollutantlist, String pointtype, List<String> flowpollutantlist, String reporttype, List<Integer> showtypes, List<Map<String, Object>> standlist) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
        Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
        //获取所有关联企业的监测点类型
        List<Integer> pollutiontypes = CommonTypeEnum.getOutPutTypeList();
        String flowunit = "";
        if ("day".equals(reporttype)) {//日报
            flowunit = "千克.日";
        } else if ("week".equals(reporttype)) {//周报
            flowunit = "千克.周";
        } else if ("month".equals(reporttype)) {//月报
            flowunit = "千克.日";
        } else if ("year".equals(reporttype)) {//年报
            flowunit = "吨.月";
        } else if ("custom".equals(reporttype)) {//自定义
            flowunit = "千克";
        } else if ("hours".equals(reporttype)) {//小时时段
            flowunit = "千克.时";
        } else if ("days".equals(reporttype)) {//日时段
            flowunit = "千克.日";
        }
        Map<String, Object> stationname = new HashMap<>();
        Map<String, Object> counttimenum = new HashMap<>();
        Map<String, Object> timestrs = new HashMap<>();
        String rownum = "2";
        if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
            stationname.put("headername", "企业名称");
            stationname.put("headercode", "pollutionname");
            stationname.put("rownum", rownum);
            stationname.put("columnnum", "1");
            stationname.put("chlidheader", new ArrayList<>());
            dataList.add(stationname);

            counttimenum.put("headername", "排口名称");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")) {//厂界小型站
            stationname.put("headername", "企业名称");
            stationname.put("headercode", "pollutionname");
            stationname.put("rownum", rownum);
            stationname.put("columnnum", "1");
            stationname.put("chlidheader", new ArrayList<>());
            dataList.add(stationname);

            counttimenum.put("headername", "监测点名称");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {//储罐
            stationname.put("headername", "企业名称");
            stationname.put("headercode", "pollutionname");
            stationname.put("rownum", rownum);
            stationname.put("columnnum", "1");
            stationname.put("chlidheader", new ArrayList<>());
            dataList.add(stationname);
            rownum = "1";
            counttimenum.put("headername", "储罐");
        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//生产装置
            stationname.put("headername", "企业名称");
            stationname.put("headercode", "pollutionname");
            stationname.put("rownum", rownum);
            stationname.put("columnnum", "1");
            stationname.put("chlidheader", new ArrayList<>());
            dataList.add(stationname);
            rownum = "1";
            counttimenum.put("headername", "生产装置");
        } else {
            rownum = "1";
            counttimenum.put("headername", "监测点名称");
        }
        counttimenum.put("headercode", "outputname");
        counttimenum.put("rownum", rownum);
        counttimenum.put("columnnum", "1");
        counttimenum.put("chlidheader", new ArrayList<>());
        dataList.add(counttimenum);

        if ("hours".equals(reporttype) || "days".equals(reporttype)) {//小时时段、日时段
            timestrs.put("headername", "监测时间");
            timestrs.put("headercode", "timestr");
            timestrs.put("rownum", rownum);
            timestrs.put("columnnum", "1");
            timestrs.put("chlidheader", new ArrayList<>());
            dataList.add(timestrs);
        }

        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                }
            }
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//若监测类型为储罐 则无排放量和折算值
                Map<String, Object> map = new HashMap<>();
                map.put("headername", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                map.put("headercode", code + "concentration");
                map.put("rownum", "1");
                map.put("columnnum", "1");
                map.put("chlidheader", new ArrayList<>());
                dataList.add(map);
            } else {
                String str = "";
                boolean flag = false;
                for (String strcode : flowpollutantlist) {
                    if (code.equals(strcode)) {
                        flag = true;
                        break;
                    }
                }

                Map<String, Object> map = new HashMap<>();
                List<String> monitortypes = pollutiontypes.stream().map(x -> x + "").collect(Collectors.toList());
                if (monitortypes.contains(pointtype)) {//若为关联企业的监测点类型  取企业名称
                    map.put("headername", name);
                    map.put("headercode", code);
                    map.put("rownum", "1");
                    if (flag == true) {
                        int num = 1;
                        if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                            num = num + showtypes.size();
                        }
                        if (llpollutants.contains(code)) {
                            num = num + 1;
                        }
                        map.put("columnnum", num);
                    } else {
                        if (llpollutants.contains(code)) {
                            map.put("columnnum", 2);
                        } else {
                            map.put("columnnum", 1);
                        }
                    }
                    List<Map<String, Object>> chlidheader = new ArrayList<>();

                    Map<String, Object> windtimenum = new HashMap<>();
                    windtimenum.put("headername", "实测值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                    windtimenum.put("headercode", code + "concentration");
                    windtimenum.put("rownum", "1");
                    windtimenum.put("columnnum", "1");
                    windtimenum.put("chlidheader", new ArrayList<>());
                    chlidheader.add(windtimenum);

                    //判断当前污染物是否为流量
                    if (llpollutants.contains(code)) {
                        Map<String, Object> ll_map = new HashMap<>();
                        if ("b02".equals(code)) {
                            ll_map.put("headername", "累计(m3)");
                        } else {
                            ll_map.put("headername", "累计(t)");
                        }
                        ll_map.put("headercode", code + "concentration_ljll");
                        ll_map.put("rownum", "1");
                        ll_map.put("columnnum", "1");
                        ll_map.put("chlidheader", new ArrayList<>());
                        chlidheader.add(ll_map);
                    }

                    if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                        for (int showtype : showtypes) {
                            if (showtype == 0) {//排放量
                                if (flag == true) {
                                    Map<String, Object> windpercent = new HashMap<>();
                                    windpercent.put("headername", "排放量" + ("".equals(flowunit) ? "" : "(" + flowunit + ")"));
                                    windpercent.put("headercode", code + "flow");
                                    windpercent.put("rownum", "1");
                                    windpercent.put("columnnum", "1");
                                    windpercent.put("chlidheader", new ArrayList<>());
                                    chlidheader.add(windpercent);
                                }
                            }
                            if (showtype == 1) {//折算值
                                if (flag == true) {
                                    if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气的时候，有折算值
                                        Map<String, Object> map2 = new HashMap<>();
                                        map2.put("headername", "折算值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                                        map2.put("headercode", code + "converted");
                                        map2.put("rownum", "1");
                                        map2.put("columnnum", "1");
                                        map2.put("chlidheader", new ArrayList<>());
                                        chlidheader.add(map2);
                                    }
                                }
                            }
                        }
                    }
                    dataList.add(map);
                    map.put("chlidheader", chlidheader);
                } else {
                    map.put("headername", name + ("".equals(unit) ? "" : "(" + unit + ")"));
                    map.put("headercode", code + "concentration");
                    map.put("rownum", "1");
                    map.put("columnnum", "1");
                    map.put("chlidheader", new ArrayList<>());
                    dataList.add(map);
                }
            }
        }
        return dataList;

    }

    /**
     * @author: xsm
     * @date: 2019/8/01 0001 下午 2:4
     * @Description:根据类型获取关联该类型在线排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getSelectPollutionInfoByPointType(Map<String, Object> paramMap) {
        //监测点类型
        String pointtype = paramMap.get("pointtype").toString();
        List<Map<String, Object>> pollutions = new ArrayList<>();
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (notothertypes.contains(Integer.valueOf(pointtype))) {
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(pointtype))) {
                case WasteWaterEnum: //废水
                    paramMap.put("flag", "water");
                    pollutions = pollutionMapper.getSelectPollutionAndWaterOutputInfo(paramMap);
                    break;
                case WasteGasEnum: //废气
                    pollutions = pollutionMapper.getSelectPollutionInfo(paramMap);
                    break;
                case SmokeEnum: //烟气
                    pollutions = pollutionMapper.getSelectPollutionInfo(paramMap);
                    break;
                case RainEnum: //雨水
                    paramMap.put("flag", "rain");
                    pollutions = pollutionMapper.getSelectPollutionAndWaterOutputInfo(paramMap);
                    break;
                case AirEnum: //空气
                    pollutions = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case WaterQualityEnum: //水质
                    pollutions = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    pollutions = unorganizedMonitorPointInfoMapper.getAllMonitorUnstenchAndStatusInfo();
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    pollutions = unorganizedMonitorPointInfoMapper.getAllMonitorUnMINIAndStatusInfo();
                    break;
            }
        } else {//其它监测点表类型
            paramMap.put("monitorPointType", pointtype);
            pollutions = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        }
        if (!CommonTypeEnum.getExcludeAuthTypeList().contains(pointtype)) {
            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);
            String sessionID = SessionUtil.getSessionID();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

            //从查询出的监测点里筛选拥有权限的监测点
            pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
            /*-----设置权限end--------*/
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        if (pollutions != null && pollutions.size() > 0) {
            for (Map<String, Object> map : pollutions) {
                if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水，烟气
                    boolean flag = set.contains(map.get("Pollutionid").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("PollutionName"));
                        objmap.put("id", map.get("Pollutionid"));
                        objmap.put("type", pointtype);
                        result.add(objmap);
                        set.add(map.get("Pollutionid").toString());
                    } else {
                        continue;
                    }
                } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")) {//厂界小型站
                    boolean flag = set.contains(map.get("FK_Pollutionid").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("pollutionname"));
                        objmap.put("id", map.get("FK_Pollutionid"));
                        objmap.put("type", pointtype);
                        result.add(objmap);
                        set.add(map.get("FK_Pollutionid").toString());
                    } else {
                        continue;
                    }
                } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {//储罐
                    boolean flag = set.contains(map.get("pollutionid").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("PollutionName"));
                        objmap.put("id", map.get("pollutionid"));
                        objmap.put("type", pointtype);
                        result.add(objmap);
                        set.add(map.get("pollutionid").toString());
                    } else {
                        continue;
                    }
                } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {//生产装置
                    boolean flag = set.contains(map.get("PK_PollutionID").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("pollutionname"));
                        objmap.put("id", map.get("PK_PollutionID"));
                        objmap.put("type", pointtype);
                        result.add(objmap);
                        set.add(map.get("PK_PollutionID").toString());
                    } else {
                        continue;
                    }
                } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode() + "")) {
                    boolean flag = set.contains(map.get("pollutionid").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("PollutionName"));
                        objmap.put("id", map.get("pollutionid"));
                        objmap.put("type", pointtype);
                        result.add(objmap);
                        set.add(map.get("pollutionid").toString());
                    } else {
                        continue;
                    }
                } else {
                    boolean flag = set.contains(map.get("pk_id").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("monitorpointname"));
                        objmap.put("id", map.get("pk_id"));
                        objmap.put("dgimn", map.get("dgimn"));
                        objmap.put("type", pointtype);
                        result.add(objmap);
                        set.add(map.get("pk_id").toString());
                    } else {
                        continue;
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/8/01 0001 下午 2:4
     * @Description:根据类型获取关联该类型在线排口的企业信息（报表弹窗带查询条件）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getSelectEntOrPointDataByParam(Map<String, Object> paramMap) {
        //监测点类型
        String pointtype = paramMap.get("pointtype").toString();
        String customname = paramMap.get("customname") != null ? paramMap.get("customname").toString() : "";
        paramMap.put("customname", customname);
        List<Map<String, Object>> pollutions = new ArrayList<>();
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (notothertypes.contains(Integer.valueOf(pointtype))) {
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(pointtype))) {
                case WasteWaterEnum: //废水
                    paramMap.put("flag", "water");
                    pollutions = pollutionMapper.getSelectPollutionAndWaterOutputInfo(paramMap);
                    break;
                case WasteGasEnum: //废气
                    pollutions = pollutionMapper.getSelectPollutionInfo(paramMap);
                    break;
                case SmokeEnum: //烟气
                    pollutions = pollutionMapper.getSelectPollutionInfo(paramMap);
                    break;
                case RainEnum: //雨水
                    paramMap.put("flag", "rain");
                    pollutions = pollutionMapper.getSelectPollutionAndWaterOutputInfo(paramMap);
                    break;
                case AirEnum: //空气
                    pollutions = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case WaterQualityEnum: //水质
                    pollutions = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    paramMap.put("monitorpointtype", pointtype);
                    pollutions = pollutionMapper.getSelectFactoryBoundaryPointInfo(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    paramMap.put("monitorpointtype", pointtype);
                    pollutions = pollutionMapper.getSelectFactoryBoundaryPointInfo(paramMap);
                    break;
            }
        } else {
            paramMap.put("monitorPointType", pointtype);
            pollutions = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        }


        if (!CommonTypeEnum.getExcludeAuthTypeList().contains(pointtype)) {
            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);
            String sessionID = SessionUtil.getSessionID();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

            //从查询出的监测点里筛选拥有权限的监测点
            pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
            /*-----设置权限end--------*/
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        String pollutionid;
        if (pollutions != null && pollutions.size() > 0) {
            for (Map<String, Object> map : pollutions) {
                if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")) {//废气,废水，雨水，烟气,厂界，小型站
                    boolean flag = true;
                    pollutionid = map.get("Pollutionid") != null ? map.get("Pollutionid").toString() : (map.get("FK_Pollutionid") != null ? map.get("FK_Pollutionid").toString() : "");
                    flag = set.contains(pollutionid);
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("pollutionname", map.get("PollutionName") != null ? map.get("PollutionName").toString() : (map.get("pollutionname") != null ? map.get("pollutionname").toString() : ""));
                        objmap.put("pollutionid", pollutionid);
                        objmap.put("shortername", map.get("ShorterName"));
                        objmap.put("address", map.get("Address"));
                        result.add(objmap);
                        set.add(pollutionid);
                    } else {
                        continue;
                    }
                } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode() + "") ||
                        pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode() + "")) {//储罐
                    boolean flag = set.contains(map.get("pollutionid").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("pollutionname", map.get("PollutionName"));
                        objmap.put("pollutionid", map.get("pollutionid"));
                        objmap.put("shortername", map.get("ShorterName"));
                        objmap.put("address", map.get("address"));
                        result.add(objmap);
                        set.add(map.get("pollutionid").toString());
                    } else {
                        continue;
                    }
                } else {
                    boolean flag = set.contains(map.get("pk_id").toString());
                    if (flag == false) {//没有重复
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("monitorpointname", map.get("monitorpointname"));
                        objmap.put("monitorpointid", map.get("pk_id"));
                        objmap.put("dgimn", map.get("dgimn"));
                        objmap.put("monitorpointtypename", map.get("monitorpointtypename"));
                        result.add(objmap);
                        set.add(map.get("pk_id").toString());
                    } else {
                        continue;
                    }
                }
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 判断值是否超标
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private String isExceedStandardValue(String code, Object value, String mn, List<Map<String, Object>> standlist) {
        String str = "#false"; //默认都为false  不变色
        String flag = "";
        if (value != null && !"".equals(value)) {
            String value_flag = value.toString();
            if (value_flag.contains(",")) {
                value = value_flag.split(",")[0];
                flag = value_flag.split(",")[1];
            }
            if (standlist != null && standlist.size() > 0) {
                for (Map<String, Object> standmap : standlist) {
                    if (standmap.get("DGIMN") != null && mn.equals(standmap.get("DGIMN").toString())) {
                        if (standmap.get("Code") != null && code.equals(standmap.get("Code").toString())) {//当Code相同
                            Object type = standmap.get("AlarmType");
                            Object StandardMinValue = standmap.get("StandardMinValue");
                            Object StandardMaxValue = standmap.get("StandardMaxValue");
                            if (type != null) {
                                if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(type.toString())) {//上限报警
                                    if (StandardMaxValue != null && !"".equals(StandardMaxValue.toString())) {
                                        if (Double.parseDouble(value.toString()) > Double.parseDouble(StandardMaxValue.toString())) {
                                            //当超过标准值
                                            str = "#true";
                                            break;
                                        }
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(type.toString())) {//下限报警
                                    if (StandardMinValue != null && !"".equals(StandardMinValue.toString())) {
                                        if (Double.parseDouble(value.toString()) < Double.parseDouble(StandardMinValue.toString())) {
                                            //当超过标准值
                                            str = "#true";
                                            break;
                                        }
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(type.toString())) {//区间报警
                                    if (StandardMinValue != null && !"".equals(StandardMinValue.toString())) {
                                        if (Double.parseDouble(value.toString()) < Double.parseDouble(StandardMinValue.toString())) {
                                            //当超过标准值
                                            str = "#true";
                                            break;
                                        }
                                    }
                                    if (StandardMaxValue != null && !"".equals(StandardMaxValue.toString())) {
                                        if (Double.parseDouble(value.toString()) > Double.parseDouble(StandardMaxValue.toString())) {
                                            //当超过标准值
                                            str = "#true";
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(flag)) {
            str = str + "#-#-#-#" + flag;
        }

        return str;

    }

    /**
     * @author: xsm
     * @date: 2019/8/01 0001 下午 3:26
     * @Description:根据类型获取关联该类型在线排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getSelectPollutantInfoByPointtype(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getNotKeyPollutantsByMonitorPointType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/10/20 18:39
     * @Description: 获取时间在该月哪一周 返回格式 2019年7月1周
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public Object getWeekTimeByTimeStr(String time) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);//设置星期一为一周的第一天。
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        int year = Integer.parseInt(time.substring(0, 4));
        int month = Integer.parseInt(time.substring(5, 7));
        return year + "年" + month + "月第" + week + "周";
    }


    @Override
    public List<Map<String, Object>> getAllStenchPointInfoByDataAuthor(Map<String, Object> map) {
        return otherMonitorPointMapper.getAllStenchMonitorPointInfoByParamMap(map);
    }

    /**
     * @author: xsm
     * @date: 2021/7/24 0024 上午 11:10
     * @Description:根据报表类型和自定义参数获取企业汇总报表数据（自定义小时时段、自定义日时段）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getEntSummaryReportDataByParamMap(Map<String, Object> paramMap) {
        try {
            List<String> llpollutants = Arrays.asList("b01", "b02");
            String ll_unit = "";//获取流量污染物单位

            int total = 0;
            //获取时间范围
            double daynum = 0d;
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            //监测点类型
            String pointtype = paramMap.get("pointtype").toString();
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
            Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
            Integer watermonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            List<String> lltypes = new ArrayList<>();
            lltypes.add(gasmonitortype + "");
            lltypes.add(smokemonitortype + "");
            lltypes.add(watermonitortype + "");
            //标题名称
            String titlename = "";
            //数据展示类型
            List<Integer> showtypes = new ArrayList<>();
            if (paramMap.get("showtypes") != null) {
                //数据展示类型
                showtypes = (List<Integer>) paramMap.get("showtypes");
            }
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            List<String> pollutionids = (List<String>) paramMap.get("pkids");
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<String> flowpollutantlist = new ArrayList<>();
            Date startDate = null;
            Date endDate = null;
            //去mongo中查询浓度数据
            String collection = "";
            String datalistname = "";
            List<String> timelist = new ArrayList<>();
            long beforeTime = (DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).getTime();
            long afterTime = (DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00")).getTime();
            total = Integer.valueOf(((afterTime - beforeTime) / (1000 * 60 * 60 * 24)) + "") + 1;
            if (paramMap.get("pagenum") != null) {
                //根据pagenum 查第几天的数据
                int pagenum = paramMap.get("pagenum") != null ? Integer.valueOf(paramMap.get("pagenum").toString()) : 1;
                String postponeDate = DataFormatUtil.getDateYMD(DataFormatUtil.getPostponeDate(DataFormatUtil.getDateYMDH(starttime + " 00:00:00"), pagenum - 1));
                starttime = postponeDate;
                endtime = postponeDate;
            }
            if ("hours".equals(reporttype)) {//查多个小时
                collection = "HourData";
                datalistname = "HourDataList";
                timelist = DataFormatUtil.getYMDHBetween(starttime + " 00", endtime + " 23");
                timelist.add(endtime + " 23");
                startDate = DataFormatUtil.getDateYMDH(starttime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDH(endtime + " 23:00:00");
                //获取小时数
                daynum = 1;
                if (starttime.equals(endtime)) {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "污染物小时排放报表";
                } else {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "至" + DataFormatUtil.getDateYMD1(endDate) + "污染物小时排放报表";
                }
            } else if ("days".equals(reporttype)) {//查多天
                collection = "DayData";
                datalistname = "DayDataList";
                timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
                timelist.add(endtime);
                startDate = DataFormatUtil.getDateYMD(starttime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMD(endtime + " 00:00:00");
                daynum = 24;
                if (starttime.equals(endtime)) {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "污染物日排放报表";
                } else {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "至" + DataFormatUtil.getDateYMD1(endDate) + "污染物日排放报表";
                }
            }
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //获取主要污染物和点位信息
            getKeyPollutantAndPointData(pollutions, paramMap, pointtype);
            //根据类型获取重点监测污染物
            if (paramMap.get("pollutantcodes") == null) {
                keypollutants = keyMonitorPollutantMapper.selectByPollutanttype(pointtype);
            }
            if (!CommonTypeEnum.getExcludeAuthTypeList().contains(pointtype)) {
                /*-----设置权限--------*/
                //设置权限 查询用户拥有权限的监测点dgimn
                List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
                paramMap.put("categorys", categorys);
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                paramMap.put("userid", userid);
                List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
                List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

                //从查询出的监测点里筛选拥有权限的监测点
                pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
                /*-----设置权限end--------*/
            }

            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") != null ? map.get("DGIMN").toString() : map.get("dgimn") != null ? map.get("dgimn").toString() : "");
            }
            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    pollutantlist.add(map.get("FK_PollutantCode").toString());
                }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("codes", pollutantlist);
            param.put("pollutanttype", pointtype);
            List<Map<String, Object>> ll_pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
                /*if (ll_pollutants != null && ll_pollutants.size() > 0) {
                    for (Map<String, Object> ll_map : ll_pollutants) {
                        //获取流量污染物单位
                        if (ll_map.get("code") != null && llpollutants.contains(ll_map.get("code").toString())) {
                            ll_unit = ll_map.get("PollutantUnit") != null ? ll_map.get("PollutantUnit").toString() : "";
                            break;
                        }
                    }
                }*/
            //废气、烟气查污染物数据判断是否有折算
            Map<String, Object> codeandflag = new HashMap<>();
            if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
                if (pollutants != null && pollutants.size() > 0) {
                    for (Map<String, Object> map : ll_pollutants) {
                        if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                            codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                        }
                    }
                }
            }
            //获取因子标准值
            param.clear();
            param.put("mnlist", mnlist);
            param.put("pollutantlist", pollutantlist);
            param.put("monitorpointtype", pointtype);
            List<Map<String, Object>> standlist = pollutantFactorMapper.getPollutantStandarddataByParam(param);
            //排放量污染物
            flowpollutantlist = getFlowPollutantListByParamMap(pollutantlist, pointtype);
            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getTableTitleForSummaryEntDataReport(pollutantlist, pointtype, showtypes, standlist, flowpollutantlist, reporttype);
            } else {
                tables = getExportTableTitleForSummaryEntDataReport(pollutantlist, pointtype, flowpollutantlist, reporttype, showtypes, standlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);

            Map<String, Object> flag_codeAndName = new HashMap<>();
            if (documents.size() > 0) {
                Map<String, Object> f_map = new HashMap<>();
                int type = Integer.parseInt(pointtype);
                f_map.put("monitorpointtypes", Arrays.asList(type));
                List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }
            }
            Map<String, Object> concentrations = getPointConcentrationDataForSummaryReport(mnlist, pollutantlist, reporttype, documents, "AvgStrength", flag_codeAndName);
            Map<String, Object> flowmap = new HashMap<>();
            Map<String, Object> convertedvalues = new HashMap<>();
            //获取累计流量
            Map<String, Object> ll_map = new HashMap<>();
            if (lltypes.contains(pointtype)) {
                ll_map = getPointTotalFlowDataForSummaryReport(startDate, endDate, mnlist, reporttype);
            }
            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (Integer showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        flowmap = getPointFlowDataForSummaryReport(startDate, endDate, mnlist, flowpollutantlist, reporttype);
                    }
                    if (showtype == 1) {//折算值
                        //去mongo中查询折算数据
                        if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气或烟气的时候，有折算值
                            convertedvalues = getPointConcentrationDataForSummaryReport(mnlist, pollutantlist, reporttype, documents, "AvgConvertStrength", flag_codeAndName);
                        }
                    }
                }
            }
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (String thetime : timelist) {
                for (String pollutionid : pollutionids) {
                    String pollutionname = "";
                    String outputname = "";
                    for (Map<String, Object> obj : pollutions) {
                        String key = "";
                        if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "")
                                || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "")
                                || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")
                                || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "")
                                || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")
                                || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")
                                || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode() + "")) {
                            key = "pollutionid";
                        } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() + "")) {
                            key = "PK_PollutionID";
                        } else {
                            key = "pkid";
                        }
                        if (obj.get(key) != null && pollutionid.equals(obj.get(key).toString())) {
                            pollutionname = obj.get("PollutionName") != null ? obj.get("PollutionName").toString() : obj.get("pollutionname") != null ? obj.get("pollutionname").toString() : "";
                            outputname = obj.get("OutputName") != null ? obj.get("OutputName").toString() : (obj.get("outputname") != null ? obj.get("outputname").toString() : "");
                            String MN = obj.get("DGIMN") != null ? obj.get("DGIMN").toString() : obj.get("dgimn") != null ? obj.get("dgimn").toString() : "";
                            boolean flag = false;
                            boolean flag2 = false;
                            boolean flag3 = false;
                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("pollutionname", pollutionname);
                            map2.put("outputname", outputname);
                            map2.put("timestr", thetime);
                            if (concentrations.get(MN) != null) {
                                List<Map<String, Object>> concentralist = (List<Map<String, Object>>) concentrations.get(MN);
                                for (Map<String, Object> map : concentralist) {
                                    if (thetime.equals(map.get("monitortime").toString())) {
                                        for (String code : pollutantlist) {
                                            String str = "";
                                            if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                                str = "#false";
                                            } else {
                                                str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                            }
                                            map2.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                                            if (llpollutants.contains(code)) {
                                                   /* if (!"".equals(ll_unit)) {
                                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                                        map2.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                                    }*/
                                                if (ll_map.get(MN) != null) {
                                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(MN);
                                                    for (Map<String, Object> ljllmap : lllist) {
                                                        if (thetime.equals(ljllmap.get("monitortime").toString())) {//当时间相等时
                                                            map2.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                            if (flag == false) {
                                for (String code : pollutantlist) {
                                    map2.put(code + "concentration#false", null);
                                }
                            }
                            //排放量
                            if (flowmap.get(MN) != null) {
                                List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(MN);
                                if (flowlist != null && flowlist.size() > 0) {
                                    for (Map<String, Object> map : flowlist) {
                                        if (thetime.equals(map.get("monitortime").toString())) {
                                            for (String code : pollutantlist) {
                                                map2.put(code + "flow", getDoubleValueForStringValue(map, code));
                                            }
                                            flag2 = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (flag2 == false) {
                                for (String code : pollutantlist) {
                                    map2.put(code + "flow", null);
                                }
                            }
                            //折算值
                            if (convertedvalues.get(MN) != null) {
                                List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(MN);
                                if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                                    //折算值
                                    for (Map<String, Object> map : convertedlist) {
                                        if (thetime.equals(map.get("monitortime").toString())) {
                                            for (String code : pollutantlist) {
                                                if (codeandflag != null && codeandflag.get(code) != null) {
                                                    String str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                                    map2.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                                }
                                            }
                                            flag3 = true;
                                            break;
                                        }
                                    }
                                    if (flag3 == false) {
                                        for (String code : pollutantlist) {
                                            map2.put(code + "converted#false", null);
                                        }
                                    }
                                }
                            }
                            if (flag == true) {
                                result.add(map2);
                            }
                        }
                    }
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            if (paramMap.get("pagenum") != null) {
                resultmap.put("total", total);
            }
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            resultmap.put("titlename", titlename);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 11:25
     * @Description:组装浓度和折算值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getPointConcentrationDataForSummaryReport(List<String> mnlist, List<String> pollutantlist, String reporttype, List<Document> documents, String keystr,
                                                                          Map<String, Object> flag_codeAndName) {
        Map<String, Object> map = new HashMap<>();

        Object flag;
        for (String mn : mnlist) {
            List<Map<String, Object>> listmap = new ArrayList<>();
            for (Document document : documents) {
                String monitorDate = "";
                String datalistname = "";
                if ("hours".equals(reporttype)) {//小时时段
                    datalistname = "HourDataList";
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("days".equals(reporttype)) {//日时段
                    datalistname = "DayDataList";
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitorDate);

                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                    for (String pollutantcode : pollutantlist) {
                        String value = "";
                        for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                            if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                if (dataMap.get(keystr) != null && !"".equals(dataMap.get(keystr).toString())) {
                                    flag = flag_codeAndName.get(dataMap.get("Flag") != null ? dataMap.get("Flag").toString().toLowerCase() : "");
                                    value = dataMap.get(keystr) + "," + flag;
                                }
                                break;
                            }
                        }
                        resultmap.put(pollutantcode, value != null ? value : "");
                    }
                    listmap.add(resultmap);
                }
            }
            map.put(mn, listmap);
        }
        return map;
    }

    /**
     * @author: xsm
     * @date: 2021/09/26 0026 上午 09:45
     * @Description:组装流量总量值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getPointTotalFlowDataForSummaryReport(Date startDate, Date endDate, List<String> mnlist, String reporttype) {
        Map<String, Object> map = new HashMap<>();
        String collection = "";
        if ("hours".equals(reporttype)) {//小时时段
            collection = "HourFlowData";
        } else if ("days".equals(reporttype)) {//日时段
            collection = "DayFlowData";
        } else if ("day".equals(reporttype)) {//日报
            collection = "HourFlowData";
        } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
            collection = "DayFlowData";
        } else if ("year".equals(reporttype)) {//年报
            collection = "MonthFlowData";
        } else if ("custom".equals(reporttype)) {//自定义
            collection = "DayFlowData";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        for (String mn : mnlist) {
            List<Map<String, Object>> listmap = new ArrayList<>();
            for (Document document : documents) {
                String monitorDate = "";
                if ("hours".equals(reporttype)) {//小时时段
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("days".equals(reporttype)) {//日时段
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                } else if ("day".equals(reporttype)) {//日报
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("week".equals(reporttype) || "month".equals(reporttype)) {//周报-月报
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                } else if ("year".equals(reporttype)) {//年报
                    monitorDate = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                } else if ("custom".equals(reporttype)) {//自定义
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitorDate);
                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    resultmap.put("ljll", document.get("TotalFlow").toString());
                    listmap.add(resultmap);
                }
            }
            map.put(mn, listmap);
        }

        return map;
    }

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 11:51
     * @Description:组装点位排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getPointFlowDataForSummaryReport(Date startDate, Date endDate, List<String> mnlist, List<String> pollutantlist, String reporttype) {
        String collection = "";
        String datalistname = "";
        if ("hours".equals(reporttype)) {//小时时段
            collection = "HourFlowData";
            datalistname = "HourFlowDataList";
        } else if ("days".equals(reporttype)) {//日时段
            collection = "DayFlowData";
            datalistname = "DayFlowDataList";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        Map<String, Object> map = new HashMap<>();
        for (String mn : mnlist) {
            List<Map<String, Object>> listmap = new ArrayList<>();
            for (Document document : documents) {
                String monitorDate = "";
                if ("hours".equals(reporttype)) {//小时时段
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("days".equals(reporttype)) {//日时段
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitorDate);

                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                    for (String pollutantcode : pollutantlist) {
                        String value = "";
                        for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                            if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                if (dataMap.get("AvgFlow") != null && !"".equals(dataMap.get("AvgFlow").toString())) {
                                    value = dataMap.get("AvgFlow").toString();
                                }
                                break;
                            }
                        }
                        resultmap.put(pollutantcode, value != null ? value : "");
                    }
                    listmap.add(resultmap);
                }
            }
            map.put(mn, listmap);
        }

        return map;
    }


    /**
     * @author: xsm
     * @date: 2021/7/27 0027 下午 15:35
     * @Description:根据报表类型和自定义参数获取某个企业某时段内的企业报表(小时时段、日时段)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getEntReportTimeSlotDataByParamMap(Map<String, Object> paramMap) {
        try {
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            //监测点类型
            String pointtype = paramMap.get("pointtype").toString();
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            //标题名称
            String titlename = "";
            if ("2".equals(tabletitletype)) {
                titlename = paramMap.get("pollutionname").toString();
            }
            //数据展示类型
            List<Integer> showtypes = new ArrayList<>();
            if (paramMap.get("showtypes") != null) {
                //数据展示类型
                showtypes = (List<Integer>) paramMap.get("showtypes");
            }
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<String> flowpollutantlist = new ArrayList<>();
            List<String> llpollutants = Arrays.asList("b01", "b02");
            Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
            Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
            Integer watermonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            List<String> lltypes = new ArrayList<>();
            lltypes.add(gasmonitortype + "");
            lltypes.add(smokemonitortype + "");
            lltypes.add(watermonitortype + "");
            String ll_unit = "";//获取流量污染物单位
            Date startDate = null;
            Date endDate = null;
            String collection = "";
            String datalistname = "";
            Map<String, Object> mnandname = new HashMap<>();
            List<String> timelist = new ArrayList<>();
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //获取点位信息
            getKeyPollutantAndPointData(pollutions, paramMap, pointtype);
            //根据类型获取重点监测污染物
            if (paramMap.get("pollutantcodes") == null) {
                keypollutants = keyMonitorPollutantMapper.selectByPollutanttype(pointtype);
            }
            if (!CommonTypeEnum.getExcludeAuthTypeList().contains(pointtype)) {
                /*-----设置权限--------*/
                //设置权限 查询用户拥有权限的监测点dgimn
                List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
                paramMap.put("categorys", categorys);
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                paramMap.put("userid", userid);
                List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
                List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
                //从查询出的监测点里筛选拥有权限的监测点
                pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
                /*-----设置权限end--------*/
            }

            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") == null ? map.get("dgimn").toString() : map.get("DGIMN").toString());
                String outputname = map.get("OutputName") != null ? map.get("OutputName").toString() : (map.get("outputname") != null ? map.get("outputname").toString() : "");
                mnandname.put(map.get("DGIMN") == null ? map.get("dgimn").toString() : map.get("DGIMN").toString(), outputname);
                if ("2".equals(tabletitletype)) {
                    titlename += outputname;
                }
            }
            if ("hours".equals(reporttype)) {//小时时间段
                collection = "HourData";
                datalistname = "HourDataList";
                timelist = DataFormatUtil.getYMDHBetween(starttime + " 00", endtime + " 23");
                timelist.add(endtime + " 23");
                startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
                endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
                titlename += "小时时段数据(" + starttime + " 00" + "至" + endtime + " 23" + ")";
            } else if ("days".equals(reporttype) || "month".equals(reporttype)) {//日时间段
                collection = "DayData";
                datalistname = "DayDataList";
                timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
                timelist.add(endtime);
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
                titlename += "日时段数据(" + starttime + "至" + endtime + ")";
            }
            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    pollutantlist.add(map.get("FK_PollutantCode").toString());
                }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("codes", pollutantlist);
            param.put("pollutanttype", pointtype);
            List<Map<String, Object>> ll_pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
            if (ll_pollutants != null && ll_pollutants.size() > 0) {
                for (Map<String, Object> ll_map : ll_pollutants) {
                    //获取流量污染物单位
                    if (ll_map.get("code") != null && llpollutants.contains(ll_map.get("code").toString())) {
                        ll_unit = ll_map.get("PollutantUnit") != null ? ll_map.get("PollutantUnit").toString() : "";
                        break;
                    }
                }
            }
            //废气、烟气查污染物数据判断是否有折算
            Map<String, Object> codeandflag = new HashMap<>();
            if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString()))) {
                if (pollutantlist != null && pollutantlist.size() > 0) {
                    for (Map<String, Object> map : ll_pollutants) {
                        if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                            codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                        }
                    }
                }
            }
            //获取因子标准值  判断页面数据是否超过标准值 显示颜色
            param.clear();
            param.put("mnlist", mnlist);
            param.put("pollutantlist", pollutantlist);
            param.put("monitorpointtype", pointtype);
            List<Map<String, Object>> standlist = pollutantFactorMapper.getPollutantStandarddataByParam(param);
            //排放量污染物
            flowpollutantlist = getFlowPollutantListByParamMap(pollutantlist, pointtype);
            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getTableTitleForEntDataReport(pollutantlist, pointtype, flowpollutantlist, reporttype, showtypes, standlist);
            } else {
                tables = getExportTableTitleForEntDataReport(pollutantlist, pointtype, flowpollutantlist, reporttype, showtypes, standlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);
            Map<String, Object> flag_codeAndName = new HashMap<>();
            if (documents.size() > 0) {
                Map<String, Object> f_map = new HashMap<>();
                int type = Integer.parseInt(pointtype);
                f_map.put("monitorpointtypes", Arrays.asList(type));
                List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }
            }
            //去mongo中查询浓度数据
            Map<String, Object> concentrations = getPointConcentrationDataForSummaryReport(mnlist, pollutantlist, reporttype, documents, "AvgStrength", flag_codeAndName);
            //获取累计流量值
            Map<String, Object> ll_map = new HashMap<>();
            if (lltypes.contains(pointtype)) {//是带有流量污染物的监测类型
                ll_map = getPointTotalFlowDataForSummaryReport(startDate, endDate, mnlist, reporttype);
            }
            Map<String, Object> flowmap = new HashMap<>();
            Map<String, Object> convertedvalues = new HashMap<>();
            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (Integer showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        flowmap = getPointFlowDataForSummaryReport(startDate, endDate, mnlist, flowpollutantlist, reporttype);
                    }
                    if (showtype == 1) {//折算值
                        //去mongo中查询折算数据
                        if (pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) {//当类型为废气或烟气的时候，有折算值
                            convertedvalues = getPointConcentrationDataForSummaryReport(mnlist, pollutantlist, reporttype, documents, "AvgConvertStrength", flag_codeAndName);
                        }
                    }
                }
            }
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (String thetime : timelist) {
                for (String MN : mnlist) {
                    boolean flag = false;
                    boolean flag2 = false;
                    boolean flag3 = false;
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("outputname", mnandname.get(MN));
                    if ("1".equals(tabletitletype)) {
                        map2.put("monitortime", thetime);
                    } else {
                        map2.put("monitortimes", thetime);
                    }
                    if (concentrations.get(MN) != null) {
                        List<Map<String, Object>> concentralist = (List<Map<String, Object>>) concentrations.get(MN);
                        for (Map<String, Object> map : concentralist) {
                            if (thetime.equals(map.get("monitortime").toString())) {
                                for (String code : pollutantlist) {
                                    String str = "";
                                    if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                        str = "#false";
                                    } else {
                                        str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                    }
                                    map2.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                                    if (llpollutants.contains(code)) {
                                        if (ll_map.get(MN) != null) {
                                            List<Map<String, Object>> ljlllist = (List<Map<String, Object>>) ll_map.get(MN);
                                            for (Map<String, Object> ljllmap : ljlllist) {
                                                if (thetime.equals(ljllmap.get("monitortime").toString())) {
                                                    map2.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                                    break;
                                                }
                                            }
                                        }
                                        //计算流量总量
                                        /*if (!"".equals(ll_unit)) {

                                            String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                            map2.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                        }*/
                                    }
                                }
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (flag == false) {
                        for (String code : pollutantlist) {
                            map2.put(code + "concentration#false", null);
                        }
                    }
                    //排放量
                    if (flowmap.get(MN) != null) {
                        List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(MN);
                        if (flowlist != null && flowlist.size() > 0) {
                            for (Map<String, Object> map : flowlist) {
                                if (thetime.equals(map.get("monitortime").toString())) {
                                    for (String code : pollutantlist) {
                                        map2.put(code + "flow", getDoubleValueForStringValue(map, code));
                                    }
                                    flag2 = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (flag2 == false) {
                        for (String code : pollutantlist) {
                            map2.put(code + "flow", null);
                        }
                    }
                    //折算值
                    if (convertedvalues.get(MN) != null) {
                        List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(MN);
                        if ((pointtype.equals(gasmonitortype.toString()) || pointtype.equals(smokemonitortype.toString())) && convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                            //折算值
                            for (Map<String, Object> map : convertedlist) {
                                if (thetime.equals(map.get("monitortime").toString())) {
                                    for (String code : pollutantlist) {
                                        if (codeandflag != null && codeandflag.get(code) != null) {
                                            String str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                            map2.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                        }
                                    }
                                    flag3 = true;
                                    break;
                                }
                            }
                            if (flag3 == false) {
                                for (String code : pollutantlist) {
                                    map2.put(code + "converted#false", null);
                                }
                            }
                        }
                    }
                    if (flag == true) {
                        result.add(map2);
                    }
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            if ("2".equals(tabletitletype)) {
                resultmap.put("titlename", titlename);
            }
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void getKeyPollutantAndPointData(List<Map<String, Object>> pollutions, Map<String, Object> paramMap, String pointtype) {
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (notothertypes.contains(Integer.parseInt(pointtype))) {
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(pointtype))) {
                case WasteWaterEnum: //废水
                    //获取企业下废水在线排口MN号
                    paramMap.put("flag", "water");
                    pollutions.addAll(waterOutPutInfoMapper.getWaterOutPutDgimnsByParamMap(paramMap));
                    break;
                case WasteGasEnum: //废气
                    //获取企业下废气在线排口MN号
                    pollutions.addAll(gasOutPutInfoMapper.getGasOutPutDgimnsByParamMap(paramMap));
                    break;
                case SmokeEnum: //烟气
                    //获取企业下烟气在线排口MN号
                    pollutions.addAll(gasOutPutInfoMapper.getGasOutPutDgimnsByParamMap(paramMap));
                    break;
                case RainEnum: //雨水
                    //获取雨水在线排口MN号
                    paramMap.put("flag", "rain");
                    pollutions.addAll(waterOutPutInfoMapper.getWaterOutPutDgimnsByParamMap(paramMap));
                    break;
                case AirEnum: //空气
                    //获取空气监测点MN号
                    pollutions.addAll(airMonitorStationMapper.getAllAirMonitorStation(paramMap));
                    break;
                case WaterQualityEnum: //水质
                    //获取水质监测点MN号
                    pollutions.addAll(waterStationMapper.getAllWaterStationInfoByParamMap(paramMap));
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    //获取厂界恶臭监测点MN号
                    pollutions.addAll(unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap));
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    //获取厂界小型站MN号
                    pollutions.addAll(unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap));
                    break;
            }
        } else {
            pollutions.addAll(otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap));
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/25 0025 下午 3:00
     * @Description:根据监测类型获取在线废气、烟气排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getGasSelectPollutionInfoByPointtype(Map<String, Object> paramMap) {
        //监测点类型
        List<Map<String, Object>> pollutions = new ArrayList<>();
        pollutions = pollutionMapper.getSelectPollutionInfo(paramMap);
        /*-----设置权限--------*/
        //设置权限 查询用户拥有权限的监测点dgimn
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        paramMap.put("categorys", categorys);
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("userid", userid);
        List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
        List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
        //从查询出的监测点里筛选拥有权限的监测点
        pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
        /*-----设置权限end--------*/
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        if (pollutions != null && pollutions.size() > 0) {
            for (Map<String, Object> map : pollutions) {
                boolean flag = set.contains(map.get("Pollutionid").toString());
                if (flag == false) {//没有重复
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("PollutionName"));
                    objmap.put("id", map.get("Pollutionid"));
                    objmap.put("type", map.get("FK_MonitorPointTypeCode"));
                    result.add(objmap);
                    set.add(map.get("Pollutionid").toString());
                } else {
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/09 0009 上午 10:42
     * @Description: 获取废气企业汇总报表列表表头数据（废气、烟气汇总）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getGasTableTitleForSummaryEntDataReport(List<String> pollutantlist, List<Integer> pointtypes, List<Integer> showtypes, List<Map<String, Object>> standlist, List<String> flowpollutantlist, String reporttype) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("monitorpointtypes", pointtypes);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        //获取所有关联企业的监测点类型

        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        Map<String, Object> counttimenum = new HashMap<>();
        Map<String, Object> timestrs = new HashMap<>();
        String flowunit = "";
        if ("day".equals(reporttype)) {//日报
            flowunit = "千克.日";
        } else if ("week".equals(reporttype)) {//周报
            flowunit = "千克.周";
        } else if ("month".equals(reporttype)) {//月报
            flowunit = "千克.日";
        } else if ("year".equals(reporttype)) {//年报
            flowunit = "吨.月";
        } else if ("custom".equals(reporttype)) {//自定义
            flowunit = "千克";
        } else if ("hours".equals(reporttype)) {//小时时段
            flowunit = "千克.时";
        } else if ("days".equals(reporttype)) {//日时段
            flowunit = "千克.日";
        }
        stationname.put("label", "企业名称");
        stationname.put("prop", "pollutionname");
        stationname.put("minwidth", "150px");
        stationname.put("headeralign", "center");
        stationname.put("fixed", "left");
        stationname.put("align", "center");
        stationname.put("showhide", true);
        dataList.add(stationname);

        counttimenum.put("label", "排口名称");

        counttimenum.put("prop", "outputname");
        counttimenum.put("minwidth", "120px");
        counttimenum.put("headeralign", "center");
        counttimenum.put("fixed", "left");
        counttimenum.put("align", "center");
        counttimenum.put("showhide", true);
        dataList.add(counttimenum);

        if ("hours".equals(reporttype) || "days".equals(reporttype)) {//小时时段、日时段
            timestrs.put("label", "监测时间");
            timestrs.put("prop", "timestr");
            timestrs.put("width", "120px");
            timestrs.put("headeralign", "center");
            timestrs.put("fixed", "left");
            timestrs.put("align", "center");
            timestrs.put("showhide", true);
            dataList.add(timestrs);
        }

        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                }
            }
            String str = "";
            Map<String, Object> map = new HashMap<>();

            map.put("label", name);
            map.put("prop", code);
            map.put("headeralign", "center");
            map.put("align", "center");
            map.put("showhide", true);
            List<Map<String, Object>> chlidheader = new ArrayList<>();

            Map<String, Object> map2 = new HashMap<>();
            map2.put("label", "实测值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
            map2.put("prop", code + "concentration");
            map2.put("width", "100px");
            map2.put("type", "concentratecolor");
            map2.put("headeralign", "center");
            map2.put("align", "center");
            map2.put("showhide", true);
            chlidheader.add(map2);

            //判断当前污染物是否为流量
            if (llpollutants.contains(code)) {
                Map<String, Object> ll_map = new HashMap<>();
                if ("b02".equals(code)) {
                    ll_map.put("label", "累计(m3)");
                } else {
                    ll_map.put("label", "累计(t)");
                }
                ll_map.put("prop", code + "concentration_ljll");
                ll_map.put("width", "100px");
                ll_map.put("headeralign", "center");
                ll_map.put("align", "center");
                ll_map.put("showhide", true);
                chlidheader.add(ll_map);
            }

            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                boolean flag = false;
                for (String strcode : flowpollutantlist) {
                    if (code.equals(strcode)) {
                        flag = true;
                        break;
                    }
                }
                for (int showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        if (flag == true) {
                            Map<String, Object> map4 = new HashMap<>();
                            map4.put("label", "排放量" + ("".equals(flowunit) ? "" : "(" + flowunit + ")"));
                            map4.put("prop", code + "flow");
                            map4.put("width", "100px");
                            map4.put("headeralign", "center");
                            map4.put("align", "center");
                            map4.put("showhide", true);
                            chlidheader.add(map4);
                        }
                    }
                    if (showtype == 1) {//折算值
                        //去mongo中查询折算数据
                        if (flag == true) {
                            Map<String, Object> map3 = new HashMap<>();
                            map3.put("label", "折算值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
                            map3.put("prop", code + "converted");
                            map3.put("width", "100px");
                            map3.put("type", "concentratecolor");
                            map3.put("headeralign", "center");
                            map3.put("align", "center");
                            map3.put("showhide", true);
                            chlidheader.add(map3);
                        }
                    }
                }
            }
            dataList.add(map);
            map.put("children", chlidheader);

        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取企业报表列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> getExportTableTitleForGasSummaryEntDataReport(List<String> pollutantlist, List<Integer> pointtypes, List<String> flowpollutantlist, String reporttype, List<Integer> showtypes, List<Map<String, Object>> standlist) {
        List<String> llpollutants = Arrays.asList("b01", "b02");
        Map<String, Object> param = new HashMap<>();
        param.put("codes", pollutantlist);
        param.put("monitorpointtypes", pointtypes);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
        List<Map<String, Object>> dataList = new ArrayList<>();
        //获取所有关联企业的监测点类型
        List<Integer> pollutiontypes = CommonTypeEnum.getOutPutTypeList();
        String flowunit = "";
        if ("day".equals(reporttype)) {//日报
            flowunit = "千克.日";
        } else if ("week".equals(reporttype)) {//周报
            flowunit = "千克.周";
        } else if ("month".equals(reporttype)) {//月报
            flowunit = "千克.日";
        } else if ("year".equals(reporttype)) {//年报
            flowunit = "吨.月";
        } else if ("custom".equals(reporttype)) {//自定义
            flowunit = "千克";
        } else if ("hours".equals(reporttype)) {//小时时段
            flowunit = "千克.时";
        } else if ("days".equals(reporttype)) {//日时段
            flowunit = "千克.日";
        }
        Map<String, Object> stationname = new HashMap<>();
        Map<String, Object> counttimenum = new HashMap<>();
        Map<String, Object> timestrs = new HashMap<>();
        String rownum = "2";
        stationname.put("headername", "企业名称");
        stationname.put("headercode", "pollutionname");
        stationname.put("rownum", rownum);
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);
        counttimenum.put("headername", "排口名称");
        counttimenum.put("headercode", "outputname");
        counttimenum.put("rownum", rownum);
        counttimenum.put("columnnum", "1");
        counttimenum.put("chlidheader", new ArrayList<>());
        dataList.add(counttimenum);

        if ("hours".equals(reporttype) || "days".equals(reporttype)) {//小时时段、日时段
            timestrs.put("headername", "监测时间");
            timestrs.put("headercode", "timestr");
            timestrs.put("rownum", rownum);
            timestrs.put("columnnum", "1");
            timestrs.put("chlidheader", new ArrayList<>());
            dataList.add(timestrs);
        }

        for (String code : pollutantlist) {
            String unit = "";
            String name = "";
            for (Map<String, Object> obj : pollutants) {
                if (code.equals(obj.get("code").toString())) {
                    name = obj.get("name").toString();
                    if (obj.get("PollutantUnit") != null) {
                        unit = obj.get("PollutantUnit").toString();
                    }
                }
            }
            String str = "";
            boolean flag = false;
            for (String strcode : flowpollutantlist) {
                if (code.equals(strcode)) {
                    flag = true;
                    break;
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("headername", name);
            map.put("headercode", code);
            map.put("rownum", "1");
            if (flag == true) {
                int num = 1;
                if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                    num = num + showtypes.size();
                }
                if (llpollutants.contains(code)) {
                    num = num + 1;
                }
                map.put("columnnum", num);
            } else {
                if (llpollutants.contains(code)) {
                    map.put("columnnum", 2);
                } else {
                    map.put("columnnum", 1);
                }
            }
            List<Map<String, Object>> chlidheader = new ArrayList<>();

            Map<String, Object> windtimenum = new HashMap<>();
            windtimenum.put("headername", "实测值" + str + ("".equals(unit) ? "" : "(" + unit + ")"));
            windtimenum.put("headercode", code + "concentration");
            windtimenum.put("rownum", "1");
            windtimenum.put("columnnum", "1");
            windtimenum.put("chlidheader", new ArrayList<>());
            chlidheader.add(windtimenum);

            //判断当前污染物是否为流量
            if (llpollutants.contains(code)) {
                Map<String, Object> ll_map = new HashMap<>();
                if ("b02".equals(code)) {
                    ll_map.put("headername", "累计(m3)");
                } else {
                    ll_map.put("headername", "累计(t)");
                }
                ll_map.put("headercode", code + "concentration_ljll");
                ll_map.put("rownum", "1");
                ll_map.put("columnnum", "1");
                ll_map.put("chlidheader", new ArrayList<>());
                chlidheader.add(ll_map);
            }

            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (int showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        if (flag == true) {
                            Map<String, Object> windpercent = new HashMap<>();
                            windpercent.put("headername", "排放量" + ("".equals(flowunit) ? "" : "(" + flowunit + ")"));
                            windpercent.put("headercode", code + "flow");
                            windpercent.put("rownum", "1");
                            windpercent.put("columnnum", "1");
                            windpercent.put("chlidheader", new ArrayList<>());
                            chlidheader.add(windpercent);
                        }
                    }
                    if (showtype == 1) {//折算值
                        if (flag == true) {
                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("headername", "折算值" + ("".equals(unit) ? "" : "(" + unit + ")"));
                            map2.put("headercode", code + "converted");
                            map2.put("rownum", "1");
                            map2.put("columnnum", "1");
                            map2.put("chlidheader", new ArrayList<>());
                            chlidheader.add(map2);
                        }
                    }
                }
            }
            dataList.add(map);
            map.put("chlidheader", chlidheader);
        }

        return dataList;

    }

    /**
     * @author: xsm
     * @date: 2022/02/08 0008 上午 11:42
     * @Description:根据报表类型和自定义参数获取废气企业汇总报表（烟气、废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getGasSummaryEntDataReportByParamMap(Map<String, Object> paramMap) throws ParseException {
        try {
            List<String> llpollutants = Arrays.asList("b02");
            String ll_unit = "";//获取流量污染物单位
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DATE);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            int yearDay = cal.get(Calendar.DAY_OF_YEAR);
            //获取时间范围
            double daynum = 0d;
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            //监测点类型
            List<Integer> pointtypes = (List<Integer>) paramMap.get("pointtypes");
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
            Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
            List<String> lltypes = new ArrayList<>();
            lltypes.add(gasmonitortype + "");
            lltypes.add(smokemonitortype + "");
            //标题名称
            String titlename = "";
            //数据展示类型
            List<Integer> showtypes = new ArrayList<>();
            if (paramMap.get("showtypes") != null) {
                //数据展示类型
                showtypes = (List<Integer>) paramMap.get("showtypes");
            }
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            List<String> pollutionids = (List<String>) paramMap.get("pkids");
            String monitortime = paramMap.get("monitortime") != null ? paramMap.get("monitortime").toString() : "";
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<String> flowpollutantlist = new ArrayList<>();
            Date startDate = null;
            Date endDate = null;
            String collection = "";
            String datalistname = "";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateTime = null;
            if ("day".equals(reporttype)) {//日报
                collection = "HourData";
                datalistname = "HourDataList";
                startDate = DataFormatUtil.getDateYMDH(monitortime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDH(monitortime + " 23:59:59");
                daynum = 24;
                titlename = DataFormatUtil.getDateYMD1(DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00"));
            } else if ("month".equals(reporttype)) {//周报-月报
                collection = "DayData";
                datalistname = "DayDataList";
                dateTime = simpleDateFormat.parse(monitortime + "-01 00:00:00");
                List<String> timelist = DataFormatUtil.getDayListOfMonth(dateTime);
                startDate = DataFormatUtil.getDateYMD(timelist.get(0));
                endDate = DataFormatUtil.getDateYMD(timelist.get(timelist.size() - 1));
                String newym = "";
                if (month < 10) {
                    newym = year + "-0" + month;
                } else {
                    newym = year + "-" + month;
                }
                if (monitortime.equals(newym)) {
                    daynum = day * 24;
                } else {
                    //获取小时数
                    long beforeTime = startDate.getTime();
                    long afterTime = endDate.getTime();
                    daynum = (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
                    daynum = (daynum + 1) * 24;
                }
                String[] ymdate = monitortime.split("-");
                titlename = ymdate[0] + "年" + Integer.valueOf(ymdate[1]) + "月";
            } else if ("year".equals(reporttype)) {//年报
                collection = "MonthData";
                datalistname = "MonthDataList";
                startDate = DataFormatUtil.getDateYMD(monitortime + "-01-01");
                endDate = DataFormatUtil.getDateYMD(monitortime + "-12-31");
                if (Integer.valueOf(monitortime) < year) {
                    daynum = 365 * 24;
                } else {
                    if (monitortime.equals(year + "")) {
                        daynum = yearDay * 24;
                    } else {
                        daynum = 0;
                    }
                }
                titlename = monitortime + "年";
            } else if ("custom".equals(reporttype)) {//自定义
                collection = "DayData";
                datalistname = "DayDataList";
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
                //获取小时数
                long beforeTime = (DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).getTime();
                long afterTime = (DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00")).getTime();
                daynum = (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
                daynum = (daynum + 1) * 24;
                titlename = DataFormatUtil.getDateYMD1(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")) + "至" + DataFormatUtil.getDateYMD1(DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00"));
            }
            titlename = titlename + "污染物排放报表";
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //获取废气、烟气主要污染物和点位信息
            pollutions = gasOutPutInfoMapper.getGasOutPutDgimnsByParamMap(paramMap);
            //根据类型获取重点监测污染物
            if (paramMap.get("pollutantcodes") == null) {
                paramMap.put("pollutanttypes", pointtypes);
                keypollutants = keyMonitorPollutantMapper.selectByPollutanttypes(paramMap);
            }
            //设置权限 查询用户拥有权限的监测点dgimn
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

            //从查询出的监测点里筛选拥有权限的监测点
            pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));

            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") != null ? map.get("DGIMN").toString() : map.get("dgimn") != null ? map.get("dgimn").toString() : "");
            }
            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    if (!pollutantlist.contains(map.get("Code").toString())) {
                        pollutantlist.add(map.get("Code").toString());
                    }
                }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("codes", pollutantlist);
            param.put("monitorpointtypes", pointtypes);
            List<Map<String, Object>> ll_pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
            if (ll_pollutants != null && ll_pollutants.size() > 0) {
                for (Map<String, Object> ll_map : ll_pollutants) {
                    //获取流量污染物单位
                    if (ll_map.get("code") != null && llpollutants.contains(ll_map.get("code").toString())) {
                        ll_unit = ll_map.get("PollutantUnit") != null ? ll_map.get("PollutantUnit").toString() : "";
                        break;
                    }
                }
            }
            //废气、烟气查污染物数据判断是否有折算
            Map<String, Object> codeandflag = new HashMap<>();
            if (pollutantlist != null && pollutantlist.size() > 0) {
                for (Map<String, Object> map : ll_pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
            //获取因子标准值
            param.clear();
            param.put("mnlist", mnlist);
            param.put("pollutantlist", pollutantlist);
            List<Map<String, Object>> standlist = new ArrayList<>();
            for (Integer i : pointtypes) {
                param.put("monitorpointtype", i);
                standlist.addAll(pollutantFactorMapper.getPollutantStandarddataByParam(param));
                //排放量污染物
                flowpollutantlist.addAll(getFlowPollutantListByParamMap(pollutantlist, i.toString()));
            }
            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getGasTableTitleForSummaryEntDataReport(pollutantlist, pointtypes, showtypes, standlist, flowpollutantlist, reporttype);
            } else {
                tables = getExportTableTitleForGasSummaryEntDataReport(pollutantlist, pointtypes, flowpollutantlist, reporttype, showtypes, standlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);


            Map<String, Object> flag_codeAndName = new HashMap<>();

            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", pointtypes);
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
            //去mongo中查询浓度数据
            Map<String, Object> concentrations = getAllOutPutConcentrationData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
            Map<String, Object> flowmap = new HashMap<>();
            Map<String, Object> convertedvalues = new HashMap<>();
            //获取累计流量
            Map<String, Object> ll_map = new HashMap<>();
            ll_map = getPointTotalFlowDataForSummaryReport(startDate, endDate, mnlist, reporttype);
            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (Integer showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        flowmap = getAllOutPutFlowData(startDate, endDate, mnlist, flowpollutantlist, reporttype);
                    }
                    if (showtype == 1) {//折算值
                        //去mongo中查询折算数据
                        convertedvalues = getAllOutPutConvertedValueData(startDate, endDate, mnlist, pollutantlist, reporttype, documents, flag_codeAndName);
                    }
                }
            }
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (String pollutionid : pollutionids) {
                String pollutionname = "";
                String outputname = "";
                for (Map<String, Object> obj : pollutions) {
                    String key = "pollutionid";
                    if (obj.get(key) != null && pollutionid.equals(obj.get(key).toString())) {
                        pollutionname = obj.get("PollutionName") != null ? obj.get("PollutionName").toString() : obj.get("pollutionname") != null ? obj.get("pollutionname").toString() : "";
                        outputname = obj.get("OutputName") != null ? obj.get("OutputName").toString() : (obj.get("outputname") != null ? obj.get("outputname").toString() : "");
                        String MN = obj.get("DGIMN") != null ? obj.get("DGIMN").toString() : obj.get("dgimn") != null ? obj.get("dgimn").toString() : "";
                        boolean flag = false;
                        boolean flag2 = false;
                        boolean flag3 = false;
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("pollutionname", pollutionname);
                        map2.put("outputname", outputname);
                        if (concentrations.get(MN) != null) {
                            List<Map<String, Object>> concentralist = (List<Map<String, Object>>) concentrations.get(MN);
                            for (Map<String, Object> map : concentralist) {
                                if ("平均值".equals(map.get("monitortime").toString())) {
                                    for (String code : pollutantlist) {
                                        String str = "";
                                        if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                            str = "#false";
                                        } else {
                                            str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                        }
                                        map2.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                                        if (llpollutants.contains(code)) {
                                            /*if (!"".equals(ll_unit)) {
                                                String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                                map2.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                            }*/
                                            String ll_total = "";
                                            if (ll_map.get(MN) != null) {
                                                List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(MN);
                                                for (Map<String, Object> ljllmap : lllist) {
                                                    if ("".equals(ll_total)) {
                                                        ll_total = Double.valueOf(ljllmap.get("ljll").toString()) + "";
                                                    } else {
                                                        ll_total = Double.valueOf(ll_total) + Double.valueOf(ljllmap.get("ljll").toString()) + "";
                                                    }
                                                }
                                            }
                                            map2.put(code + "concentration" + "_ljll", !"".equals(ll_total) ? Double.valueOf(ll_total) : null);
                                        }
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag == false) {
                            for (String code : pollutantlist) {
                                map2.put(code + "concentration#false", null);
                            }
                        }
                        //排放量
                        if (flowmap.get(MN) != null) {
                            List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(MN);
                            if (flowlist != null && flowlist.size() > 0) {
                                for (Map<String, Object> map : flowlist) {
                                    if ("总排放量".equals(map.get("monitortime").toString())) {
                                        for (String code : pollutantlist) {
                                            map2.put(code + "flow", getDoubleValueForStringValue(map, code));
                                            //map2.put(code + "concentration", map.get(code));
                                        }
                                        flag2 = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (flag2 == false) {
                            for (String code : pollutantlist) {
                                map2.put(code + "flow", null);
                            }
                        }
                        //折算值
                        if (convertedvalues.get(MN) != null) {
                            List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(MN);
                            //折算值
                            for (Map<String, Object> map : convertedlist) {
                                if ("平均值".equals(map.get("monitortime").toString())) {
                                    for (String code : pollutantlist) {
                                        if (codeandflag != null && codeandflag.get(code) != null) {
                                            String str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                            map2.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                        }
                                    }
                                    flag3 = true;
                                    break;
                                }
                            }
                            if (flag3 == false) {
                                for (String code : pollutantlist) {
                                    //if ("1".equals(tabletitletype)) {
                                    map2.put(code + "converted#false", null);
                                }
                            }
                        }
                        result.add(map2);
                    }
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            resultmap.put("titlename", titlename);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author: xsm
     * @date: 2022/02/10 0010 上午 10:08
     * @Description:根据报表类型和自定义参数获取废气企业汇总报表数据（自定义小时时段、自定义日时段）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getGasEntSummaryReportDataByParamMap(Map<String, Object> paramMap) {
        try {
            List<String> llpollutants = Arrays.asList("b02");
            String ll_unit = "";//获取流量污染物单位
            int total = 0;
            //获取时间范围
            double daynum = 0d;
            //报表类型
            String reporttype = paramMap.get("reporttype").toString();
            //监测点类型
            List<Integer> pointtypes = (List<Integer>) paramMap.get("pointtypes");
            //自定义表头类型
            String tabletitletype = paramMap.get("tabletitletype").toString();
            Integer gasmonitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
            Integer smokemonitortype = CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode();
            List<String> lltypes = new ArrayList<>();
            lltypes.add(gasmonitortype + "");
            lltypes.add(smokemonitortype + "");
            //标题名称
            String titlename = "";
            //数据展示类型
            List<Integer> showtypes = new ArrayList<>();
            if (paramMap.get("showtypes") != null) {
                //数据展示类型
                showtypes = (List<Integer>) paramMap.get("showtypes");
            }
            List<String> pollutants = new ArrayList<>();
            if (paramMap.get("pollutantcodes") != null) {
                //污染物
                pollutants = (List<String>) paramMap.get("pollutantcodes");
            }
            List<String> pollutionids = (List<String>) paramMap.get("pkids");
            String starttime = paramMap.get("starttime") != null ? paramMap.get("starttime").toString() : "";
            String endtime = paramMap.get("endtime") != null ? paramMap.get("endtime").toString() : "";
            List<String> mnlist = new ArrayList<>();
            List<String> pollutantlist = new ArrayList<>();
            List<String> flowpollutantlist = new ArrayList<>();
            Date startDate = null;
            Date endDate = null;
            //去mongo中查询浓度数据
            String collection = "";
            String datalistname = "";
            List<String> timelist = new ArrayList<>();
            long beforeTime = (DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).getTime();
            long afterTime = (DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00")).getTime();
            total = Integer.valueOf(((afterTime - beforeTime) / (1000 * 60 * 60 * 24)) + "") + 1;
            if (paramMap.get("pagenum") != null) {
                //根据pagenum 查第几天的数据
                int pagenum = paramMap.get("pagenum") != null ? Integer.valueOf(paramMap.get("pagenum").toString()) : 1;
                String postponeDate = DataFormatUtil.getDateYMD(DataFormatUtil.getPostponeDate(DataFormatUtil.getDateYMDH(starttime + " 00:00:00"), pagenum - 1));
                starttime = postponeDate;
                endtime = postponeDate;
            }
            if ("hours".equals(reporttype)) {//查多个小时
                collection = "HourData";
                datalistname = "HourDataList";
                timelist = DataFormatUtil.getYMDHBetween(starttime + " 00", endtime + " 23");
                timelist.add(endtime + " 23");
                startDate = DataFormatUtil.getDateYMDH(starttime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDH(endtime + " 23:00:00");
                //获取小时数
                daynum = 1;
                if (starttime.equals(endtime)) {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "污染物小时排放报表";
                } else {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "至" + DataFormatUtil.getDateYMD1(endDate) + "污染物小时排放报表";
                }
            } else if ("days".equals(reporttype)) {//查多天
                collection = "DayData";
                datalistname = "DayDataList";
                timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
                timelist.add(endtime);
                startDate = DataFormatUtil.getDateYMD(starttime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMD(endtime + " 00:00:00");
                daynum = 24;
                if (starttime.equals(endtime)) {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "污染物日排放报表";
                } else {
                    titlename = DataFormatUtil.getDateYMD1(startDate) + "至" + DataFormatUtil.getDateYMD1(endDate) + "污染物日排放报表";
                }
            }
            //根据污染源ID和污染类型 获取对应的排口MN号
            List<Map<String, Object>> pollutions = new ArrayList<>();
            //根据污染类型获取重点污染物
            List<Map<String, Object>> keypollutants = new ArrayList<>();
            //获取废气、烟气主要污染物和点位信息
            pollutions = gasOutPutInfoMapper.getGasOutPutDgimnsByParamMap(paramMap);
            //根据类型获取重点监测污染物
            if (paramMap.get("pollutantcodes") == null) {
                paramMap.put("pollutanttypes", pointtypes);
                keypollutants = keyMonitorPollutantMapper.selectByPollutanttypes(paramMap);
            }
            /*-----设置权限--------*/
            //设置权限 查询用户拥有权限的监测点dgimn
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
            List<String> collect = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            //从查询出的监测点里筛选拥有权限的监测点
            pollutions.removeIf(m -> !collect.contains(m.get("DGIMN") == null ? m.get("dgimn").toString() : m.get("DGIMN").toString()));
            /*-----设置权限end--------*/
            //筛选MN号
            for (Map<String, Object> map : pollutions) {
                mnlist.add(map.get("DGIMN") != null ? map.get("DGIMN").toString() : map.get("dgimn") != null ? map.get("dgimn").toString() : "");
            }
            //将页面要查询的污染物和重点污染物合并到一起
            if (pollutants.size() > 0) {
                for (String str : pollutants) {
                    pollutantlist.add(str);
                }
            } else {
                for (Map<String, Object> map : keypollutants) {
                    if (!pollutantlist.contains(map.get("Code").toString())) {
                        pollutantlist.add(map.get("Code").toString());
                    }
                }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("codes", pollutantlist);
            param.put("monitorpointtypes", pointtypes);
            List<Map<String, Object>> ll_pollutants = pollutantFactorMapper.getPollutantsByPollutantType(param);
            //废气、烟气查污染物数据判断是否有折算
            Map<String, Object> codeandflag = new HashMap<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : ll_pollutants) {
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        codeandflag.put(map.get("code").toString(), map.get("IsHasConvertData"));
                    }
                }
            }
            //获取因子标准值
            //获取因子标准值
            param.clear();
            param.put("mnlist", mnlist);
            param.put("pollutantlist", pollutantlist);
            List<Map<String, Object>> standlist = new ArrayList<>();
            for (Integer i : pointtypes) {
                param.put("monitorpointtype", i);
                standlist.addAll(pollutantFactorMapper.getPollutantStandarddataByParam(param));
                //排放量污染物
                flowpollutantlist.addAll(getFlowPollutantListByParamMap(pollutantlist, i.toString()));
            }
            //拼接表头数据
            List<Map<String, Object>> tables = new ArrayList<>();
            if ("1".equals(tabletitletype)) {
                tables = getGasTableTitleForSummaryEntDataReport(pollutantlist, pointtypes, showtypes, standlist, flowpollutantlist, reporttype);
            } else {
                tables = getExportTableTitleForGasSummaryEntDataReport(pollutantlist, pointtypes, flowpollutantlist, reporttype, showtypes, standlist);
            }
            //构建Mongdb查询条件  查询浓度值和折算值
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, collection);

            Map<String, Object> flag_codeAndName = new HashMap<>();
            if (documents.size() > 0) {
                Map<String, Object> f_map = new HashMap<>();
                f_map.put("monitorpointtypes", pointtypes);
                List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }
            }

            Map<String, Object> concentrations = getPointConcentrationDataForSummaryReport(mnlist, pollutantlist, reporttype, documents, "AvgStrength", flag_codeAndName);
            Map<String, Object> flowmap = new HashMap<>();
            Map<String, Object> convertedvalues = new HashMap<>();
            //获取累计流量
            Map<String, Object> ll_map = new HashMap<>();
            ll_map = getPointTotalFlowDataForSummaryReport(startDate, endDate, mnlist, reporttype);
            if (showtypes != null && showtypes.size() > 0) {//判断展示数据类型是否为空
                for (Integer showtype : showtypes) {
                    if (showtype == 0) {//排放量
                        //去mongo中查询排放量数据
                        flowmap = getPointFlowDataForSummaryReport(startDate, endDate, mnlist, flowpollutantlist, reporttype);
                    }
                    if (showtype == 1) {//折算值
                        //去mongo中查询折算数据
                        convertedvalues = getPointConcentrationDataForSummaryReport(mnlist, pollutantlist, reporttype, documents, "AvgConvertStrength", flag_codeAndName);
                    }
                }
            }
            //根据报表类型 组装成对应报表数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (String thetime : timelist) {
                for (String pollutionid : pollutionids) {
                    String pollutionname = "";
                    String outputname = "";
                    for (Map<String, Object> obj : pollutions) {
                        String key = "pollutionid";
                        if (obj.get(key) != null && pollutionid.equals(obj.get(key).toString())) {
                            pollutionname = obj.get("PollutionName") != null ? obj.get("PollutionName").toString() : obj.get("pollutionname") != null ? obj.get("pollutionname").toString() : "";
                            outputname = obj.get("OutputName") != null ? obj.get("OutputName").toString() : (obj.get("outputname") != null ? obj.get("outputname").toString() : "");
                            String MN = obj.get("DGIMN") != null ? obj.get("DGIMN").toString() : obj.get("dgimn") != null ? obj.get("dgimn").toString() : "";
                            boolean flag = false;
                            boolean flag2 = false;
                            boolean flag3 = false;
                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("pollutionname", pollutionname);
                            map2.put("outputname", outputname);
                            map2.put("timestr", thetime);
                            if (concentrations.get(MN) != null) {
                                List<Map<String, Object>> concentralist = (List<Map<String, Object>>) concentrations.get(MN);
                                for (Map<String, Object> map : concentralist) {
                                    if (thetime.equals(map.get("monitortime").toString())) {
                                        for (String code : pollutantlist) {
                                            String str = "";
                                            if (codeandflag != null && codeandflag.get(code) != null) {//有折算值  不按实测值去判断超标
                                                str = "#false";
                                            } else {
                                                str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                            }
                                            map2.put(code + "concentration" + str, getDoubleValueForStringValue(map, code));
                                            if (llpollutants.contains(code)) {
                                                   /* if (!"".equals(ll_unit)) {
                                                        String ll_value = countFlowPollutantValue(map.get(code), ll_unit, daynum);
                                                        map2.put(code + "concentration" + "_ljll", !"".equals(ll_value) ? Double.valueOf(ll_value) : null);
                                                    }*/
                                                if (ll_map.get(MN) != null) {
                                                    List<Map<String, Object>> lllist = (List<Map<String, Object>>) ll_map.get(MN);
                                                    for (Map<String, Object> ljllmap : lllist) {
                                                        if (thetime.equals(ljllmap.get("monitortime").toString())) {//当时间相等时
                                                            map2.put(code + "concentration" + "_ljll", !"".equals(ljllmap.get("ljll").toString()) ? Double.valueOf(ljllmap.get("ljll").toString()) : null);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                            if (flag == false) {
                                for (String code : pollutantlist) {
                                    map2.put(code + "concentration#false", null);
                                }
                            }
                            //排放量
                            if (flowmap.get(MN) != null) {
                                List<Map<String, Object>> flowlist = (List<Map<String, Object>>) flowmap.get(MN);
                                if (flowlist != null && flowlist.size() > 0) {
                                    for (Map<String, Object> map : flowlist) {
                                        if (thetime.equals(map.get("monitortime").toString())) {
                                            for (String code : pollutantlist) {
                                                map2.put(code + "flow", getDoubleValueForStringValue(map, code));
                                            }
                                            flag2 = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (flag2 == false) {
                                for (String code : pollutantlist) {
                                    map2.put(code + "flow", null);
                                }
                            }
                            //折算值
                            if (convertedvalues.get(MN) != null) {
                                List<Map<String, Object>> convertedlist = (List<Map<String, Object>>) convertedvalues.get(MN);
                                if (convertedlist != null && convertedlist.size() > 0) {//当类型为废气的时候，有折算值
                                    //折算值
                                    for (Map<String, Object> map : convertedlist) {
                                        if (thetime.equals(map.get("monitortime").toString())) {
                                            for (String code : pollutantlist) {
                                                if (codeandflag != null && codeandflag.get(code) != null) {
                                                    String str = isExceedStandardValue(code, map.get(code), MN, standlist);
                                                    map2.put(code + "converted" + str, getDoubleValueForStringValue(map, code));
                                                }
                                            }
                                            flag3 = true;
                                            break;
                                        }
                                    }
                                    if (flag3 == false) {
                                        for (String code : pollutantlist) {
                                            map2.put(code + "converted#false", null);
                                        }
                                    }
                                }
                            }
                            if (flag == true) {
                                result.add(map2);
                            }
                        }
                    }
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            if (paramMap.get("pagenum") != null) {
                resultmap.put("total", total);
            }
            resultmap.put("tabletitledata", tables);
            resultmap.put("selectpollutants", pollutantlist);
            resultmap.put("tablelistdata", result);
            resultmap.put("titlename", titlename);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}