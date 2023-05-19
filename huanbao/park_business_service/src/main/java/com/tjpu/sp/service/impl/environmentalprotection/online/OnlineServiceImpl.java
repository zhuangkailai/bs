package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.google.common.base.Joiner;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.AlarmLevelMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DeviceDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.online.OnlineMapper;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.dao.environmentalprotection.watercorrelation.WaterCorrelationMapper;
import com.tjpu.sp.dao.environmentalprotection.watercorrelation.WaterCorrelationPollutantSetMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.watercorrelation.WaterCorrelationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.watercorrelation.WaterCorrelationVO;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.ProductInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AlarmMenus.getNameByString;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AlarmMonitorTypeMenu.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MongodbDataTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineServiceImpl implements OnlineService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;


    //超标数据表
    private final String overData_db = "OverData";
    //预警数据表
    private final String earlyWarnData_db = "EarlyWarnData";
    //异常数据表
    private final String exceptionData_db = "ExceptionData";
    //    实时数据
//    private final String realTimeCollection = "RealTimeData";
    //小时在线
    private final String hourCollection = "HourData";
    //小时在线
    private final String hourFlowCollection = "HourFlowData";
    //日在线
    private final String dayCollection = "DayData";
    //分钟在线
    private final String minuteCollection = "MinuteData";
    //日在线
    private final String monthCollection = "MonthData";
    private final String groundWaterCollection = "GroundWaterData";

    //分钟在线
    private final String realTimeCollection = "RealTimeData";

    private final String pollutantAlarminfoCollection = "PollutantAlarmInfo";

    private final String exceptionModelCollection = "ExceptionModel";
    private final String overModelCollection = "OverModel";

    private final String emissionPeriodCollection = "EmissionPeriodData";

    @Autowired
    private DeviceDevOpsInfoMapper deviceDevOpsInfoMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;
    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;
    @Autowired
    private ProductInfoService productInfoService;
    @Autowired
    private WaterCorrelationMapper waterCorrelationMapper;
    @Autowired
    private WaterCorrelationPollutantSetMapper waterCorrelationPollutantSetMapper;


    private final PollutionMapper pollutionMapper;
    private final WaterOutputInfoMapper waterOutputInfoMapper;
    private final GasOutPutInfoMapper gasOutPutInfoMapper;
    private final PollutantFactorMapper pollutantFactorMapper;
    private final GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;
    private final UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    private final WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;
    private final AirMonitorStationMapper airMonitorStationMapper;
    private final AirStationPollutantSetMapper airStationPollutantSetMapper;
    private final OtherMonitorPointMapper otherMonitorPointMapper;
    private final OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper;
    private final AlarmLevelMapper alarmLevelMapper;


    private final OnlineMapper onlineMapper;
    private static final String latestData_db = "LatestData";


    public OnlineServiceImpl(PollutionMapper pollutionMapper, WaterOutputInfoMapper waterOutputInfoMapper,
                             GasOutPutInfoMapper gasOutPutInfoMapper, PollutantFactorMapper pollutantFactorMapper,
                             GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper,
                             UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper,
                             WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper,
                             AirMonitorStationMapper airMonitorStationMapper,
                             AirStationPollutantSetMapper airStationPollutantSetMapper,
                             OtherMonitorPointMapper otherMonitorPointMapper,
                             OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper,
                             AlarmLevelMapper alarmLevelMapper, OnlineMapper onlineMapper) {
        this.pollutionMapper = pollutionMapper;
        this.waterOutputInfoMapper = waterOutputInfoMapper;
        this.gasOutPutInfoMapper = gasOutPutInfoMapper;
        this.pollutantFactorMapper = pollutantFactorMapper;
        this.gasOutPutPollutantSetMapper = gasOutPutPollutantSetMapper;
        this.unorganizedMonitorPointInfoMapper = unorganizedMonitorPointInfoMapper;
        this.waterOutPutPollutantSetMapper = waterOutPutPollutantSetMapper;
        this.airMonitorStationMapper = airMonitorStationMapper;
        this.airStationPollutantSetMapper = airStationPollutantSetMapper;
        this.otherMonitorPointMapper = otherMonitorPointMapper;
        this.otherMonitorPointPollutantSetMapper = otherMonitorPointPollutantSetMapper;
        this.alarmLevelMapper = alarmLevelMapper;
        this.onlineMapper = onlineMapper;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 13:58
     * @Description: 获取数据一览查询条件
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getQueryDataForRealTime(Integer pointType) {
        Map<String, Object> querydata = new HashMap<>();
        List<Map<String, Object>> querycontroldata = new ArrayList<>();
        Map<String, Object> queryformdata = new HashMap<>();
        //废水监测数据一览查询条件
        if (CommonTypeEnum.getOutPutTypeList().contains(pointType)) {
            Map<String, Object> pollutionMap = new HashMap<>();
            pollutionMap.put("clearable", true);
            pollutionMap.put("width", "360px");
            pollutionMap.put("showhide", true);
            pollutionMap.put("disabled", false);
            pollutionMap.put("type", "autocomplete");
            pollutionMap.put("name", "shortername");
            pollutionMap.put("placeholder", "请输入企业名称");
            pollutionMap.put("label", "企业名称");
            //污染源
            List<Map<String, String>> pollutionNameMaps = new ArrayList<>();
            List<String> pollutions = pollutionMapper.getPollutionNames();
            for (String pollution : pollutions) {
                Map<String, String> map2 = new HashMap<>();
                map2.put("value", pollution);
                pollutionNameMaps.add(map2);
            }
            pollutionMap.put("option", pollutionNameMaps);
            querycontroldata.add(pollutionMap);
            queryformdata.put("shortername", "");
            //排口名称
            Map<String, Object> outputMap = new HashMap<>();
            outputMap.put("clearable", true);
            outputMap.put("width", "360px");
            outputMap.put("showhide", true);
            outputMap.put("disabled", false);
            outputMap.put("type", "text");
            outputMap.put("name", "outputname");
            outputMap.put("placeholder", "请输入排口名称");
            outputMap.put("label", "排口名称");
            querycontroldata.add(outputMap);
            queryformdata.put("outputname", "");


            //排口属性（废水、废气、烟气）
            if (pointType == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()
                    || pointType == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()
                    || pointType == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()
            ) {
                //排口属性
                Map<String, Object> outputType = new HashMap<>();
                outputType.put("clearable", true);
                outputType.put("name", "outputpropertys");
                outputType.put("width", "360px");
                outputType.put("showhide", true);
                outputType.put("disabled", false);
                outputType.put("placeholder", "请选择排口类型");
                outputType.put("type", "select");
                outputType.put("multiple", true);

                Map<String, Object> paramMap = new HashMap<>();

                if (pointType == SmokeEnum.getCode()) {
                    paramMap.put("pollutanttype", WasteGasEnum.getCode());
                } else {
                    paramMap.put("pollutanttype", pointType);
                }

                List<Map<String, Object>> dataList = pollutantFactorMapper.getOutPutPropertyByParam(paramMap);
                outputType.put("option", dataList);
                outputType.put("label", "排口类型");
                querycontroldata.add(outputType);
                queryformdata.put("outputpropertys", "");
            }


        } else if (CommonTypeEnum.getMonitorPointTypeList().contains(pointType)) {
            Map<String, Object> outputMap = new HashMap<>();
            outputMap.put("clearable", true);
            outputMap.put("width", "360px");
            outputMap.put("showhide", true);
            outputMap.put("disabled", false);
            outputMap.put("type", "text");
            outputMap.put("name", "monitorpointname");
            outputMap.put("placeholder", "请输入监测点名称");
            outputMap.put("label", "监测点名称");
            querycontroldata.add(outputMap);
            queryformdata.put("monitorpointname", "");

            if (AirEnum.getCode() == pointType) {//控制级别
                Map<String, Object> outputType = new HashMap<>();
                outputType.put("clearable", true);
                outputType.put("name", "controllevels");
                outputType.put("width", "360px");
                outputType.put("showhide", true);
                outputType.put("disabled", false);
                outputType.put("placeholder", "请选择控制级别");
                outputType.put("type", "select");
                outputType.put("multiple", true);

                Map<String, Object> paramMap = new HashMap<>();
                List<Map<String, Object>> dataList = pollutantFactorMapper.getAirControlLevelByParam(paramMap);
                outputType.put("option", dataList);
                outputType.put("label", "控制级别");
                querycontroldata.add(outputType);
                queryformdata.put("controllevels", "");
            }


        } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(pointType)) {
            Map<String, Object> pollutionMap = new HashMap<>();
            pollutionMap.put("clearable", true);
            pollutionMap.put("width", "360px");
            pollutionMap.put("showhide", true);
            pollutionMap.put("disabled", false);
            pollutionMap.put("type", "autocomplete");
            pollutionMap.put("name", "shortername");
            pollutionMap.put("placeholder", "请输入企业名称");
            pollutionMap.put("label", "企业名称");
            //污染源
            List<Map<String, String>> pollutionNameMaps = new ArrayList<>();
            List<String> pollutions = pollutionMapper.getPollutionNames();
            for (String pollution : pollutions) {
                Map<String, String> map2 = new HashMap<>();
                map2.put("value", pollution);
                pollutionNameMaps.add(map2);
            }
            pollutionMap.put("option", pollutionNameMaps);
            querycontroldata.add(pollutionMap);
            queryformdata.put("shortername", "");
            //排口名称
            Map<String, Object> outputMap = new HashMap<>();
            outputMap.put("clearable", true);
            outputMap.put("width", "360px");
            outputMap.put("showhide", true);
            outputMap.put("disabled", false);
            outputMap.put("type", "text");
            outputMap.put("name", "monitorpointname");
            outputMap.put("placeholder", "请输入监测点名称");
            outputMap.put("label", "监测点名称");
            querycontroldata.add(outputMap);
            queryformdata.put("monitorpointname", "");
        } else if (StorageTankAreaEnum.getCode() == pointType) {
            Map<String, Object> pollutionMap = new HashMap<>();
            pollutionMap.put("clearable", true);
            pollutionMap.put("width", "360px");
            pollutionMap.put("showhide", true);
            pollutionMap.put("disabled", false);
            pollutionMap.put("type", "text");
            pollutionMap.put("name", "storagetankareaname");
            pollutionMap.put("placeholder", "请输入区域位置");
            pollutionMap.put("label", "区域位置");

            querycontroldata.add(pollutionMap);
            queryformdata.put("storagetankareaname", "");
            //储罐编号
            Map<String, Object> outputMap = new HashMap<>();
            outputMap.put("clearable", true);
            outputMap.put("width", "360px");
            outputMap.put("showhide", true);
            outputMap.put("disabled", false);
            outputMap.put("type", "text");
            outputMap.put("name", "outputname");
            outputMap.put("placeholder", "请输入储罐编号");
            outputMap.put("label", "储罐编号");
            querycontroldata.add(outputMap);
            queryformdata.put("outputname", "");
        } else if (ProductionSiteEnum.getCode() == pointType) {
            Map<String, Object> pollutionMap = new HashMap<>();
            pollutionMap.put("clearable", true);
            pollutionMap.put("width", "360px");
            pollutionMap.put("showhide", true);
            pollutionMap.put("disabled", false);
            pollutionMap.put("type", "text");
            pollutionMap.put("name", "majorhazardsourcesname");
            pollutionMap.put("placeholder", "请输入生产场所名称");
            pollutionMap.put("label", "生产场所名称");

            querycontroldata.add(pollutionMap);
            queryformdata.put("majorhazardsourcesname", "");
            //储罐编号
            Map<String, Object> outputMap = new HashMap<>();
            outputMap.put("clearable", true);
            outputMap.put("width", "360px");
            outputMap.put("showhide", true);
            outputMap.put("disabled", false);
            outputMap.put("type", "text");
            outputMap.put("name", "outputname");
            outputMap.put("placeholder", "请输入生产装置名称");
            outputMap.put("label", "生产装置名称");
            querycontroldata.add(outputMap);
            queryformdata.put("outputname", "");
        }

        //放入排扣状态数据
        Map<String, Object> outputOnlineStatu = new HashMap<>();
        outputOnlineStatu.put("width", "360px");
        outputOnlineStatu.put("showhide", true);
        outputOnlineStatu.put("disabled", false);
        outputOnlineStatu.put("type", "checkbox");
        outputOnlineStatu.put("name", "onlineoutputstatus");
        outputOnlineStatu.put("label", "排口状态");
        //checkboxdata
        List<Map<String, Object>> checkboxData = new ArrayList<>();
        CommonTypeEnum.OnlineStatusEnum[] values = CommonTypeEnum.OnlineStatusEnum.values();
        for (CommonTypeEnum.OnlineStatusEnum value : values) {
            String code = value.getCode();
            String name = value.getName();
            Map<String, Object> map = new HashMap<>();
            map.put("labelname", name);
            map.put("value", code);
            checkboxData.add(map);
        }
        outputOnlineStatu.put("checkboxchildren", checkboxData);
        queryformdata.put("onlineoutputstatus", new ArrayList<>());
        querycontroldata.add(outputOnlineStatu);

        querydata.put("queryformdata", queryformdata);
        querydata.put("querycontroldata", querycontroldata);
        querydata.put("dualcontrolskey", new ArrayList());
        return querydata;
    }


    /**
     * @author: zhangzc
     * @date: 2019/6/11 9:40
     * @Description: 获取数据预警及超标报警的查询条件
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getQueryCriteriaForEarlyAndOverAlarm(Integer pointType) {
        try {
            Map<String, Object> querydata = new HashMap<>();
            List<Map<String, Object>> querycontroldata = new ArrayList<>();
            Map<String, Object> queryformdata = new HashMap<>();
            List<String> dualcontrolskey = new ArrayList<>();
            Map<String, Object> dataTypeMap = new HashMap<>();
            dataTypeMap.put("clearable", true);
            dataTypeMap.put("name", "datatype");
            dataTypeMap.put("showhide", true);
            dataTypeMap.put("disabled", false);
            dataTypeMap.put("placeholder", "请选择数据类型");
            dataTypeMap.put("label", "数据类型");
            dataTypeMap.put("type", "select");
            dataTypeMap.put("option", null);
            List<Map<String, Object>> dataTypeOption = new ArrayList<>();
            String[] labelNames = {"实时数据", "分钟数据", "小时数据", "日数据"};
            String[] values = {"RealTimeData", "MinuteData", "HourData", "DayData"};
            for (int i = 0; i < labelNames.length; i++) {
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("labelname", labelNames[i]);
                optionMap.put("value", values[i]);
                dataTypeOption.add(optionMap);
            }
            dataTypeMap.put("option", dataTypeOption);
            queryformdata.put("datatype", "RealTimeData");
            querycontroldata.add(dataTypeMap);
            Map<String, Object> countTimeMap = new HashMap<>();
            countTimeMap.put("clearable", false);
            countTimeMap.put("datetype", "date");
            countTimeMap.put("endplaceholder", "请选择结束值");
            countTimeMap.put("format", "yyyy-MM-dd");
            countTimeMap.put("format1", "yyyy-MM-dd");
            countTimeMap.put("showhide", true);
            countTimeMap.put("label", "统计时间");
            countTimeMap.put("valueformat", "yyyy-MM-dd");
            countTimeMap.put("type", "DatePickerScope");
            countTimeMap.put("startPlaceholder", "请选择起始值");
            countTimeMap.put("characteristic", "26196aa2-8223-4271-9f6c-4718dwsdssdsd");
            countTimeMap.put("control", true);
            countTimeMap.put("valueformat1", "yyyy-MM-dd");
            countTimeMap.put("pickeroptionsend", "pickerOptionsEnd26196aa2-8223-4271-9f6c-4718dwsdssdsd");
            countTimeMap.put("name", "counttime");
            countTimeMap.put("width", "400px");
            countTimeMap.put("pickeroptionsstart", new HashMap<>());
            countTimeMap.put("disabled", false);
            dualcontrolskey.add("counttime");
            LocalDate today = LocalDate.now();
           /* //本月的第一天
            LocalDate firstday = LocalDate.of(today.getYear(), today.getMonth(), 1);
            String start = firstday.toString();*/
            String end = today.toString();
            String[] counttimes = {end, end};
            queryformdata.put("counttime", counttimes);
            querycontroldata.add(countTimeMap);
            //废水监测数据一览查询条件
            if (CommonTypeEnum.getOutPutTypeList().contains(pointType)) {
                Map<String, Object> pollutionMap = new HashMap<>();
                pollutionMap.put("clearable", true);
                pollutionMap.put("width", "360px");
                pollutionMap.put("showhide", true);
                pollutionMap.put("disabled", false);
                pollutionMap.put("type", "autocomplete");
                pollutionMap.put("name", "shortername");
                pollutionMap.put("placeholder", "请输入企业名称");
                pollutionMap.put("label", "企业名称");
                //污染源
                List<Map<String, String>> pollutionNameMaps = new ArrayList<>();
                List<String> pollutions = pollutionMapper.getPollutionNames();
                for (String pollution : pollutions) {
                    Map<String, String> map2 = new HashMap<>();
                    map2.put("value", pollution);
                    pollutionNameMaps.add(map2);
                }
                pollutionMap.put("option", pollutionNameMaps);
                querycontroldata.add(pollutionMap);
                queryformdata.put("shortername", "");
                //排口名称
                Map<String, Object> outputMap = new HashMap<>();
                outputMap.put("clearable", true);
                outputMap.put("width", "360px");
                outputMap.put("showhide", true);
                outputMap.put("disabled", false);
                outputMap.put("type", "text");
                outputMap.put("name", "outputname");
                outputMap.put("placeholder", "请输入排口名称");
                outputMap.put("label", "排口名称");
                querycontroldata.add(outputMap);
                queryformdata.put("outputname", "");
            } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(pointType)) {
                Map<String, Object> pollutionMap = new HashMap<>();
                pollutionMap.put("clearable", true);
                pollutionMap.put("width", "360px");
                pollutionMap.put("showhide", true);
                pollutionMap.put("disabled", false);
                pollutionMap.put("type", "autocomplete");
                pollutionMap.put("name", "shortername");
                pollutionMap.put("placeholder", "请输入企业名称");
                pollutionMap.put("label", "企业名称");
                //污染源
                List<Map<String, String>> pollutionNameMaps = new ArrayList<>();
                List<String> pollutions = pollutionMapper.getPollutionNames();
                for (String pollution : pollutions) {
                    Map<String, String> map2 = new HashMap<>();
                    map2.put("value", pollution);
                    pollutionNameMaps.add(map2);
                }
                pollutionMap.put("option", pollutionNameMaps);
                querycontroldata.add(pollutionMap);
                queryformdata.put("shortername", "");

                Map<String, Object> outputMap = new HashMap<>();
                outputMap.put("clearable", true);
                outputMap.put("width", "360px");
                outputMap.put("showhide", true);
                outputMap.put("disabled", false);
                outputMap.put("type", "text");
                outputMap.put("name", "monitorpointname");
                outputMap.put("placeholder", "请输入监测点名称");
                outputMap.put("label", "监测点名称");
                querycontroldata.add(outputMap);
                queryformdata.put("monitorpointname", "");
            } else if (CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() == pointType) {//储罐
                Map<String, Object> pollutionMap = new HashMap<>();
                pollutionMap.put("clearable", true);
                pollutionMap.put("width", "360px");
                pollutionMap.put("showhide", true);
                pollutionMap.put("disabled", false);
                pollutionMap.put("type", "autocomplete");
                pollutionMap.put("name", "storagetankareaname");
                pollutionMap.put("placeholder", "请输入区域名称");
                pollutionMap.put("label", "区域名称");
                queryformdata.put("storagetankareaname", "");

                Map<String, Object> outputMap = new HashMap<>();
                outputMap.put("clearable", true);
                outputMap.put("width", "360px");
                outputMap.put("showhide", true);
                outputMap.put("disabled", false);
                outputMap.put("type", "text");
                outputMap.put("name", "monitorpointname");
                outputMap.put("placeholder", "请输入储罐编号");
                outputMap.put("label", "储罐编号");
                querycontroldata.add(outputMap);
                queryformdata.put("monitorpointname", "");
            } else if (CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode() == pointType) {//生产装置
                Map<String, Object> pollutionMap = new HashMap<>();
                pollutionMap.put("clearable", true);
                pollutionMap.put("width", "360px");
                pollutionMap.put("showhide", true);
                pollutionMap.put("disabled", false);
                pollutionMap.put("type", "autocomplete");
                pollutionMap.put("name", "majorhazardsourcesname");
                pollutionMap.put("placeholder", "请输入生产场所名称");
                pollutionMap.put("label", "生产场所名称");
                queryformdata.put("majorhazardsourcesname", "");

                Map<String, Object> outputMap = new HashMap<>();
                outputMap.put("clearable", true);
                outputMap.put("width", "360px");
                outputMap.put("showhide", true);
                outputMap.put("disabled", false);
                outputMap.put("type", "text");
                outputMap.put("name", "monitorpointname");
                outputMap.put("placeholder", "请输入生产装置名称");
                outputMap.put("label", "生产装置名称");
                querycontroldata.add(outputMap);
                queryformdata.put("monitorpointname", "");
            } else {//其它监测点类型
                Map<String, Object> outputMap = new HashMap<>();
                outputMap.put("clearable", true);
                outputMap.put("width", "360px");
                outputMap.put("showhide", true);
                outputMap.put("disabled", false);
                outputMap.put("type", "text");
                outputMap.put("name", "monitorpointname");
                outputMap.put("placeholder", "请输入监测点名称");
                outputMap.put("label", "监测点名称");
                querycontroldata.add(outputMap);
                queryformdata.put("monitorpointname", "");
            }
            querydata.put("queryformdata", queryformdata);
            querydata.put("querycontroldata", querycontroldata);
            querydata.put("dualcontrolskey", dualcontrolskey);
            return querydata;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 4:17
     * @Description: 自定义查询条件查询mongodb实时数据信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getMonitorDataByParamMap(Map<String, Object> paramMap) {
        Query query = setNoGroupQuery(paramMap);
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }


    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 3:11
     * @Description: 从mongodb中获取数据，并组装成数据表格形式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTableListDataForReport(Map<String, Object> paramMap) {
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String collection = paramMap.get("collection").toString();


        Integer pollutantType = Integer.parseInt(paramMap.get("pollutantType").toString());
        Integer reportType = Integer.parseInt(paramMap.get("reportType").toString());
        List<String> outputids = (List<String>) paramMap.get("outputids");
        Query query = setNoGroupQuery(paramMap);
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        switch (reportType) {
            case 1:  //实时数据一览
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutantType)) {
                    case WasteWaterEnum: //废水
                        tableListData = getTableListData(documents, collection);
                        break;
                    case WasteGasEnum: //废气
                    case SmokeEnum: //烟气
                        tableListData = getTableListData(documents, collection);
                        break;
                    case RainEnum: //雨水
                        tableListData = getTableListData(documents, collection);
                        break;
                    case AirEnum: //空气关联站点查询
                        PageEntity<Document> documentsPage = getAirLookupData(paramMap);

                        paramMap.put("total", documentsPage.getTotalCount());
                        paramMap.put("pages", documentsPage.getPageCount());
                        tableListData = getAirStationTableListData(documentsPage.getListItems(), collection);
                        break;
                    case EnvironmentalVocEnum: //voc
                        tableListData = getTableListData(documents, collection);
                        break;
                    case WaterQualityEnum: //水质
                        tableListData = getTableListData(documents, collection);
                        break;
                    case EnvironmentalStinkEnum: //恶臭
                    case MicroStationEnum://微站
                    case EnvironmentalDustEnum://扬尘
                        tableListData = getTableListData(documents, collection);
                        break;
                    case meteoEnum: //气象
                        tableListData = getTableListData(documents, collection);
                        break;
                    case FactoryBoundaryStinkEnum:

                        tableListData = getTableListData(documents, collection);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        tableListData = getTableListData(documents, collection);
                        break;
                }
                break;
            case 2: //数据报表
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutantType)) {
                    case WasteWaterEnum: //废水
                        tableListData = getWaterPollutionOutPutTableListData(outputids, documents, collection);
                        break;
                    case WasteGasEnum: //废气
                    case SmokeEnum: //烟气
                        tableListData = getGasPollutionOutPutTableListData(outputids, documents, collection);
                        break;
                    case RainEnum: //雨水
                        tableListData = getWaterPollutionOutPutTableListData(outputids, documents, collection);
                        break;
                    case AirEnum: //空气
                        PageEntity<Document> documentsPage = getAirLookupData(paramMap);
                        paramMap.put("total", documentsPage.getTotalCount());
                        paramMap.put("pages", documentsPage.getPageCount());
                        tableListData = getAirPollutionOutPutTableListData(outputids, documentsPage.getListItems(), collection);
                        break;
                    case EnvironmentalVocEnum: //voc
                        tableListData = getVocPollutionOutPutTableListData(outputids, documents, collection);
                        break;
                    case WaterQualityEnum: //水质
                        tableListData = getWaterStationPollutionOutPutTableListData(outputids, documents, collection);
                        break;
                    case EnvironmentalStinkEnum: //恶臭
                    case MicroStationEnum://微站
                    case EnvironmentalDustEnum://扬尘
                        tableListData = getOtherPollutionOutPutTableListData(outputids, documents, collection, pollutantType);
                        break;
                    case meteoEnum: //气象
                        tableListData = getOtherPollutionOutPutTableListData(outputids, documents, collection, pollutantType);
                        break;
                    case FactoryBoundaryStinkEnum:

                        tableListData = getUNGasPollutionOutPutTableListData(outputids, documents, collection, pollutantType);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        tableListData = getUNGasPollutionOutPutTableListData(outputids, documents, collection, pollutantType);
                        break;
                }
                break;
        }

        return tableListData;
    }


    /**
     * @author: lip
     * @date: 2019/7/4 0004 下午 4:07
     * @Description: 获取空气站点和空气监测关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    private PageEntity<Document> getAirLookupData(Map<String, Object> paramMap) {
        int pageNum = 1;
        int pageSize = 10000000;
        if (paramMap.get("pagenum") != null) {
            pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
        }
        if (paramMap.get("pagesize") != null) {
            pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        pageEntity.setPageNum(pageNum);
        pageEntity.setPageSize(pageSize);
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
        } else {
            if (paramMap.get("starttime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate)));
            }
            if (paramMap.get("endtime") != null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("MonitorTime").lte(endDate)));
            }
        }
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        //排序条件
        String orderBy = "MonitorTime";
        if (paramMap.get("orderBy") != null) {
            orderBy = paramMap.get("orderBy").toString();
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if (paramMap.get("direction") != null && "asc".equals(paramMap.get("direction"))) {
            direction = Sort.Direction.ASC;
        }

        String collection = paramMap.get("collection").toString();
        String leftCollection = paramMap.get("leftCollection").toString();
        LookupOperation lookupOperation = LookupOperation.newLookup().from(leftCollection).localField("MonitorTime").foreignField("MonitorTime").as("StationData");
        operations.add(lookupOperation);

        long totalCount = 0;
        Aggregation aggregationCount = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
        totalCount = resultsCount.getMappedResults().size();
        pageEntity.setTotalCount(totalCount);
        int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
        pageEntity.setPageCount(pageCount);
        operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
        operations.add(Aggregation.limit(pageEntity.getPageSize()));
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;

    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 上午 9:47
     * @Description: 获取空气站点表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getAirStationTableListData(List<Document> documents, String collection) {
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);

        List<Document> pollutantDataList;
        List<Document> stationDataList;
        String isOver;
        String isException;
        String isSuddenChange;
        boolean isOverStandard;
        String pollutantCode;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            stationDataList = (List<Document>) document.get("StationData");
            for (Document station : stationDataList) {
                if (station.get("StationCode").equals(document.get("DataGatherCode"))) {
                    listDataMap.put("aqi", station.get("AQI"));
                    break;
                }
            }
            pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }

            }
            tableListData.add(listDataMap);
        }
        return tableListData;
    }

    private List<Map<String, Object>> getProductionSitePollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Integer pollutantType) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        paramMap.put("monitorpointtype", pollutantType);
        List<Map<String, Object>> gasOutputs = productInfoService.getProductInfoAndPollutionInfoByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);

        String isOver;
        String isException;
        String isSuddenChange;
        String pollutantCode;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : gasOutputs) {
                if (mn.equals(map.get("dgimn"))) {
                    String shortername = map.get("shortername") == null ? "" : map.get("shortername").toString();
                    String MajorHazardSourcesName = map.get("MajorHazardSourcesName") == null ? "" : map.get("MajorHazardSourcesName").toString();
                    listDataMap.put("MajorHazardSourcesName", Joiner.on("-").join(new String[]{shortername, MajorHazardSourcesName}));
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {

                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
    }

    private List<Map<String, Object>> getUNGasPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Integer pollutantType) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        paramMap.put("monitorpointtype", pollutantType);
        List<Map<String, Object>> gasOutputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);

        String isOver;
        String isException;
        String isSuddenChange;
        String pollutantCode;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : gasOutputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("shortername", map.get("shortername"));
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {

                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;

                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
    }

    private List<Map<String, Object>> getOtherPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Integer pollutantType) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        paramMap.put("monitorPointType", pollutantType);
        List<Map<String, Object>> outputs = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String isSuddenChange;
        String pollutantCode;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : outputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    listDataMap.put("outputid", map.get("pk_id"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;

    }

    private List<Map<String, Object>> getWaterStationPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        List<Map<String, Object>> outputs = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);

        String isOver;
        String isException;
        String isSuddenChange;
        String pollutantCode;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : outputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;

    }

    private List<Map<String, Object>> getVocPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
        List<Map<String, Object>> outputs = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String isSuddenChange;
        String pollutantCode;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : outputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
    }

    /**
     * @author: lip
     * @date: 2019/10/25 0025 上午 10:07
     * @Description: 组装大气监测点列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getAirPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("airids", outputids);
        List<Map<String, Object>> outputs = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        List<Document> pollutantDataList;
        List<Document> stationDataList;
        String isOver;
        String isException;
        String isSuddenChange;
        String pollutantCode;
        boolean isOverStandard;



        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (documents.size() > 0) {
            //获取mongodb的flag标记
            Map<String,Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes",Arrays.asList(AirEnum.getCode()));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }



        //待处理：添加IAQI
        Object IAQI;
        Document thisDoc;
        for (Document document : documents) {
            thisDoc = null;
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            stationDataList = (List<Document>) document.get("StationData");
            for (Document station : stationDataList) {
                if (station.get("StationCode").equals(document.get("DataGatherCode"))) {
                    thisDoc = station;
                    listDataMap.put("aqi", station.get("AQI"));

                    break;
                }
            }
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : outputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    listDataMap.put("outputid", map.get("pk_id"));
                    break;
                }
            }
            pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {

                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {

                    IAQI = getIAQI(thisDoc,pollutantCode);

                    isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                    isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                    isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                    isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                    //isOverStandard = pollutant.getBoolean("IsOverStandard");
                    if (isOverStandard) {
                        isOver = "4";
                    }

                    if (!"true".equals(isSuddenChange)) {
                        isSuddenChange = "false";
                    }


                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange
                            +"#-#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            +"#"+IAQI,
                             pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
    }

    private Object getIAQI( Document station, String pollutantCode) {
        Object IAQI = null;
        if(station!=null){
            List<Document> pollutants = station.get("DataList",List.class);
            for (Document pollutant:pollutants){
                if (pollutantCode.equals(pollutant.get("PollutantCode"))){
                    IAQI = pollutant.get("IAQI");
                }
            }
        }
        return IAQI;

    }


    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 5:06
     * @Description: 获取分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPageMapForReport(Map<String, Object> paramMap) {
        String collection = paramMap.get("collection").toString();
        Query query = setNoGroupQuery(paramMap);
        long totalSize = mongoTemplate.count(query, collection);
        Map<String, Object> pageMap = new HashMap<>();
        Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
        pageMap.put("total", totalSize);
        int pageCount = ((int) totalSize + pagesize - 1) / pagesize;
        pageMap.put("pages", pageCount);
        pageMap.put("pagesize", paramMap.get("pagesize"));
        pageMap.put("pagenum", paramMap.get("pagenum"));
        return pageMap;
    }

    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 5:06
     * @Description: 获取分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getExceptionPageMap(Map<String, Object> paramMap) {

        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("MN").in(mns));
        }
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            query.addCriteria(Criteria.where("FirstExceptionTime").gte(startDate).and("LastExceptionTime").lte(endDate));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        if (paramMap.get("dataTypes") != null) {
            List<String> dataTypes = (List<String>) paramMap.get("dataTypes");
            query.addCriteria(Criteria.where("DataType").in(dataTypes));
        }
        long totalSize = mongoTemplate.count(query, exceptionModelCollection);
        Map<String, Object> pageMap = new HashMap<>();
        Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
        pageMap.put("total", totalSize);
        int pageCount = ((int) totalSize + pagesize - 1) / pagesize;
        pageMap.put("pages", pageCount);
        pageMap.put("pagesize", paramMap.get("pagesize"));
        pageMap.put("pagenum", paramMap.get("pagenum"));
        return pageMap;
    }


    @Override
    public List<Document> getExceptionModelData(Map<String, Object> paramMap) {

        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("MN").in(mns));
        }
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            query.addCriteria(Criteria.where("FirstExceptionTime").gte(startDate).and("LastExceptionTime").lte(endDate));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        List<Document> documentList = mongoTemplate.find(query, Document.class, exceptionModelCollection);
        return documentList;
    }

    @Override
    public List<Document> getOverModelData(Map<String, Object> paramMap) {

        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("MN").in(mns));
        }
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            query.addCriteria(Criteria.where("FirstOverTime").gte(startDate).and("LastOverTime").lte(endDate));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        List<Document> documentList = mongoTemplate.find(query, Document.class, overModelCollection);
        return documentList;
    }


    /**
     * @author: lip
     * @date: 2019/10/25 0025 上午 9:12
     * @Description: 处理列表数据（将是否报警，是否异常添加到code中）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    private List<Map<String, Object>> getTableListData(List<Document> documents, String collection) {
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);


        String isOver;
        String isException;
        String pollutantCode;
        String isSuddenChange;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();

            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }

            }
            if (listDataMap.size() > 0) {
                listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                tableListData.add(listDataMap);
            }

        }
        return tableListData;
    }

    private String getPollutantDataKey(String collection) {
        String pollutantDataKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
        } else if ("MonthFlowData".equals(collection)) {
            pollutantDataKey = "MonthFlowDataList";
        } else if ("MonthData".equals(collection)) {
            pollutantDataKey = "MonthDataList";
        } else if ("YearFlowData".equals(collection)) {
            pollutantDataKey = "YearFlowDataList";
        }
        return pollutantDataKey;
    }

    private String getValueKey(String collection) {
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
        } else if ("MonthFlowData".equals(collection)) {
            valueKey = "PollutantFlow";
        } else if ("YearFlowData".equals(collection)) {
            valueKey = "PollutantFlow";
        }
        return valueKey;
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 下午 6:57
     * @Description: 废气有组织表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getGasPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        List<Map<String, Object>> gasOutputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String pollutantCode;
        String isSuddenChange;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : gasOutputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("shortername", map.get("shortername"));
                    listDataMap.put("outputname", map.get("outputname"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {

                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                //isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }

                if (pollutant.get("PollutantCode") != null) {
                    listDataMap.put(pollutant.getString("PollutantCode"), pollutant.get(valueKey));
                }

            }
            tableListData.add(listDataMap);
        }
        return tableListData;

    }

    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 3:41
     * @Description: 组装表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getWaterPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        List<Map<String, Object>> waterOutputs = waterOutputInfoMapper.getWaterOutPutsByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String isSuddenChange;
        boolean isOverStandard;
        String pollutantCode;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : waterOutputs) {
                if (mn.equals(map.get("mn"))) {
                    listDataMap.put("shortername", map.get("shortername"));
                    listDataMap.put("outputname", map.get("outputname"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange, pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
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
        if (paramMap.get("type") != null) {
            String type = paramMap.get("type").toString();
            query.addCriteria(Criteria.where("Type").is(type));
        }
        if (paramMap.get("Addendum") != null) {
            query.addCriteria(Criteria.where("HourDataList.Addendum").is(paramMap.get("Addendum")));
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
            if ("RealTimeData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
            } else if ("MinuteData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("MinuteDataList.PollutantCode").in(pollutantcodes));
            } else if ("HourData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourDataList.PollutantCode").in(pollutantcodes));
            } else if ("DayData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayDataList.PollutantCode").in(pollutantcodes));
            } else if ("MonthData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("MonthDataList.PollutantCode").in(pollutantcodes));
            }
        }
        if (paramMap.get("issuddenchange") != null) {
            if ("RealTimeData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("RealDataList.IsSuddenChange").is(paramMap.get("issuddenchange")));
            } else if ("MinuteData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("MinuteDataList.IsSuddenChange").is(paramMap.get("issuddenchange")));
            } else if ("HourData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourDataList.IsSuddenChange").is(paramMap.get("issuddenchange")));
            } else if ("DayData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayDataList.IsSuddenChange").is(paramMap.get("issuddenchange")));
            }
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        }


        return query;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 9:20
     * @Description: 获取排口或者监测点最新监测数据
     * @updateUser:xsm
     * @updateDate:2020/3/05 17:29
     * @updateDescription:修改排口停产判断
     * @param:
     * @return:
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOutPutLastDatasByParamMap(List<Map<String, Object>> outputs, Integer
            pointType, Map<String, Object> paramMap) {
        List<Map<String, Object>> outPutInfos = outputs.stream().distinct().collect(Collectors.toList());
        Map<String, Integer> countData = new LinkedHashMap<>();
        Set<Object> tempPollution = new HashSet<>();
        Set<Object> tempNormal = new HashSet<>();
        Set<Object> tempoffline = new HashSet<>();
        Set<Object> tempover = new HashSet<>();
        Set<Object> tempexception = new HashSet<>();
        //mn号
        List<String> mns = new ArrayList<>();
        for (Map<String, Object> outPut : outPutInfos) {
            tempPollution.add(outPut.get("fk_pollutionid"));
            //正常1
            if (outPut.get("onlinestatus") != null &&
                    CommonTypeEnum.OnlineStatusEnum.NormalStatusEnum.getCode().equals(outPut.get("onlinestatus").toString())) {
                tempNormal.add(outPut.get("pk_id"));
            }
            //离线0
            if (outPut.get("onlinestatus") == null ||
                    CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode().equals(outPut.get("onlinestatus").toString())) {
                tempoffline.add(outPut.get("pk_id"));
            }
            //超标2
            if (outPut.get("onlinestatus") != null &&
                    CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode().equals(outPut.get("onlinestatus").toString())) {
                tempover.add(outPut.get("pk_id"));
            }
            //异常3
            if (outPut.get("onlinestatus") != null &&
                    CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum.getCode().equals(outPut.get("onlinestatus").toString())) {
                tempexception.add(outPut.get("pk_id"));
            }

            if (outPut.get("dgimn") != null) {
                mns.add(outPut.get("dgimn").toString());
            }
        }
        if (CommonTypeEnum.getOutPutTypeList().contains(pointType) || CommonTypeEnum.getEntMonitorPointTypeList().contains(pointType)) {
            countData.put("pollutioncount", tempPollution.size());
        }
        //统计各状态监测点个数
        countData.put("outputcount", outPutInfos.size());
        countData.put("normalcount", tempNormal.size());
        countData.put("offlinecount", tempoffline.size());
        countData.put("overcount", tempover.size());
        countData.put("exceptioncount", tempexception.size());
        paramMap.put("monitorpointtypes", Arrays.asList(pointType));
        //获取当前时间运维的污染物信息
        List<Map<String, Object>> devOpsData = deviceDevOpsInfoMapper.getIsDevOpsDeviceByParams(paramMap);
        Map<String, String[]> idAndCodes = new HashMap<>();
        String monitorpointid;
        String[] pollutantcodes;
        if (devOpsData.size() > 0) {
            for (Map<String, Object> devOps : devOpsData) {
                monitorpointid = devOps.get("monitorpointid").toString();
                if (devOps.get("pollutantcodes") != null) {
                    pollutantcodes = devOps.get("pollutantcodes").toString().split(",");
                    idAndCodes.put(monitorpointid, pollutantcodes);
                }
            }
        }
        //获取mongdb数据
        List<Document> documents = getRealTimeDataByMNsAndDataType(mns);
        //获取点位及污染物标准值数据
        Map<String, Map<String, Object>> mnCodeAndStandardMap = new HashMap<>();
        String mnCode;
        if (documents.size() > 0) {
            paramMap.put("mnlist", mns);
            paramMap.put("monitorpointtype", pointType);
            List<Map<String, Object>> pollutantStandardDataList = pollutantFactorMapper.getPollutantStandarddataByParam(paramMap);
            if (pollutantStandardDataList.size() > 0) {
                for (Map<String, Object> pollutantStandard : pollutantStandardDataList) {
                    mnCode = pollutantStandard.get("DGIMN") + "," + pollutantStandard.get("Code");
                    Map<String, Object> standardMap = new HashMap<>();
                    standardMap.put("StandardMinValue", pollutantStandard.get("StandardMinValue"));
                    standardMap.put("StandardMaxValue", pollutantStandard.get("StandardMaxValue"));
                    standardMap.put("AlarmType", pollutantStandard.get("AlarmType"));
                    mnCodeAndStandardMap.put(mnCode, standardMap);
                }
            }
        }
        //表头数据
        List<Map<String, Object>> tabletitledata = getTableTitleDataForRealTime(pointType);
        Set<String> pollutantCodes = new HashSet<>();
        for (Document document : documents) {
            List<Document> dataList = (List<Document>) document.get("DataList");
            for (Document document1 : dataList) {
                if (document1.get("PollutantCode") != null && document1.get("PollutantCode") != "") {
                    pollutantCodes.add(document1.get("PollutantCode").toString());
                }
            }
        }
        List<Map<String, Object>> pollutants = new ArrayList<>();
        if (pollutantCodes.size() > 0) {
            Map<String, Object> paramMap2 = new HashMap<>();
            paramMap2.put("codes", pollutantCodes);
            //获取污染物类型
            paramMap2.put("pollutanttype", pointType);
            pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap2);
            //有对应污染物列表中就有数据
            if (pollutants.size() > 0) {
                //获取污染物表头
                List<Map<String, Object>> pollutantstabletitledata = getPollutantTableTitleData(pollutants);
                //将污染物表头数据放入表头数据中
                tabletitledata.addAll(pollutantstabletitledata);
            }
        }
        Map<String, List<Map<String, Object>>> idAndRTSP = getRTSPData(pointType);
        //列表数据
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        for (Map<String, Object> outPut : outPutInfos) {
            monitorpointid = outPut.get("monitorpointid").toString();
            String dgimn = outPut.get("dgimn").toString();
            Map<String, Object> mapdata = formatRealTimeListDataByPointType(pointType, outPut);
            String monitorTime = "";
            String startCode = CommonTypeEnum.MonitorPointStatusEnum.StartEnum.getCode() + "";
            for (Map<String, Object> pollutant : pollutants) {

                Map<String, Object> map2 = new HashMap<>();
                Object value = null;
                Object IsOver = null;
                Object IsException = null;
                boolean IsOverStandard = false;
                String code = pollutant.get("code").toString();
                pollutantcodes = idAndCodes.get(monitorpointid);
                if (pollutantcodes != null && Arrays.asList(pollutantcodes).contains(code)) {
                    map2.put("IsStop", "1");
                } else {
                    if (pointType != WasteGasEnum.getCode() &&
                            pointType != WasteWaterEnum.getCode() && pointType != RainEnum.getCode()
                    ) {//处理非废气  废水 雨水的其它点位  根据点位表状态判断点位是否停用
                        if (outPut.get("status") == null || outPut.get("status").toString().equals(startCode)) {
                            for (Document document : documents) {
                                String dataGatherCode = document.get("DataGatherCode").toString();
                                if (dgimn.equals(dataGatherCode)) {
                                    try {
                                        monitorTime = document.get("MonitorTime") != null ? DataFormatUtil.formatCST(document.get("MonitorTime").toString()) : "";
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    //获取value
                                    List<Document> dataList = (List<Document>) document.get("DataList");
                                    for (Document document1 : dataList) {
                                        if (document1.get("PollutantCode") != null && document1.get("PollutantCode") != "") {
                                            String pollutantCode = document1.get("PollutantCode").toString();
                                            if (code.equals(pollutantCode)) {
                                                value = document1.get("AvgStrength");
                                                IsOver = document1.get("IsOver");
                                                IsOverStandard = document1.get("IsOverStandard") != null ? document1.getBoolean("IsOverStandard") : false;
                                                if (IsOverStandard) {
                                                    IsOver = "4";
                                                }
                                                IsException = document1.get("IsException");
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            monitorTime = getMarkName(pointType);
                        }
                    } else {
                        //判断排口点位是否停产 处理废气 废水 雨水 未停产的排口信息
                        if (outPut.get("onlinestatusname") != null && !outPut.get("onlinestatusname").toString().equals("停产")) {
                         /*   if (pointType == RainEnum.getCode()) {//雨水 判断排放中
                                Map<String, Object> param = new HashMap<>();
                                param.put("monitorpointtype", RainEnum.getCode());
                                List<Map<String, Object>> stoplist = monitorPointMonitorControlMapper.getCurrentTimeMonitorControlInfoByParamMap(param);
                                if (stoplist != null && stoplist.size() > 0) {
                                    for (Map<String, Object> stopmap : stoplist) {
                                        if ((outPut.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                            mapdata.put("outputname", outPut.get("outputname") + "【排放中】");
                                        }
                                    }
                                }
                            }*/
                        } else {
                            mapdata.put("outputname", outPut.get("outputname") + "【" + getMarkName(pointType) + "】");
                        }
                        for (Document document : documents) {
                            String dataGatherCode = document.get("DataGatherCode").toString();
                            if (dgimn.equals(dataGatherCode)) {
                                try {
                                    monitorTime = document.get("MonitorTime") != null ? DataFormatUtil.formatCST(document.get("MonitorTime").toString()) : "";
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                //获取value
                                List<Document> dataList = (List<Document>) document.get("DataList");
                                for (Document document1 : dataList) {
                                    if (document1.get("PollutantCode") != null && document1.get("PollutantCode") != "") {
                                        String pollutantCode = document1.get("PollutantCode").toString();
                                        if (code.equals(pollutantCode)) {
                                            value = document1.get("AvgStrength");
                                            IsOver = document1.get("IsOver");
                                            IsOverStandard = document1.get("IsOverStandard") != null ? document1.getBoolean("IsOverStandard") : false;
                                            if (IsOverStandard) {
                                                IsOver = "4";
                                            }
                                            IsException = document1.get("IsException");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    map2.put("IsStop", "0");
                }
                mnCode = outPut.get("dgimn") + "," + pollutant.get("code");
                if (mnCodeAndStandardMap.containsKey(mnCode)) {
                    map2.putAll(mnCodeAndStandardMap.get(mnCode));
                }
                map2.put("value", value);
                map2.put("IsOver", IsOver);
                map2.put("IsException", IsException);
                //放入污染物code以及value
                mapdata.put(code, map2);
            }
            mapdata.put("monitortime", monitorTime);
            mapdata.put("rtsplist", idAndRTSP.get(monitorpointid));
            tablelistdata.add(mapdata);
        }
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> tabledata = new HashMap<>();
        //根据监测点类型和mn号获取各个mn号监测的污染物
        List<Map<String, String>> mnPollutant = getMonitorPollutantByParam(mns, pointType);
        Map<String, Set<String>> mncodes = new HashMap<>();
        for (Map<String, String> map : mnPollutant) {
            String mn = map.get("DGIMN");
            String code = map.get("Code");
            if (mncodes.containsKey(mn)) {
                mncodes.get(mn).add(code);
            } else {
                Set<String> codes = new HashSet<>();
                codes.add(code);
                mncodes.put(mn, codes);
            }
        }
        for (Map<String, Object> map : tablelistdata) {
            String mn = map.get("mn") == null ? "" : map.get("mn").toString();
            map.put("pollutants", mncodes.get(mn) != null ? mncodes.get(mn) : new ArrayList<>());
            if (map.get("onlinestatus") == null) {
                map.replace("onlinestatus", "");
                map.put("orderindex", 10);
            } else {
                String onlineStatus = map.get("onlinestatus").toString();
                map.put("orderindex", CommonTypeEnum.onlinePCStatusOrderEnum.getIndexByCode(onlineStatus));
            }
        }

        //排序
        if (tablelistdata.size() > 0) {

            if (paramMap.get("sortdata") != null && !"".equals(paramMap.get("sortdata"))) {
                Map<String, Object> sortdata = (Map<String, Object>) paramMap.get("sortdata");

                if (sortdata.size() > 0) {
                    String sortkey = sortdata.get("sortkey").toString();
                    List<String> stringSort = Arrays.asList("monitortime");
                    if (stringSort.contains(sortkey)) {
                        if ("desc".equals(sortdata.get("sorttype"))) {//倒叙
                            tablelistdata = tablelistdata.stream().sorted(
                                    Comparator.comparing(m -> comparingStringByKey(((Map<String, Object>) m), sortkey)).reversed()
                            ).collect(Collectors.toList());
                        } else {//正序
                            tablelistdata = tablelistdata.stream().sorted(
                                    Comparator.comparing(m -> comparingStringByKey(((Map<String, Object>) m), sortkey))
                            ).collect(Collectors.toList());
                        }
                    } else {
                        if ("desc".equals(sortdata.get("sorttype"))) {//倒叙
                            tablelistdata = tablelistdata.stream().sorted(
                                    Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), sortkey)).reversed()
                            ).collect(Collectors.toList());
                        } else {//正序
                            tablelistdata = tablelistdata.stream().sorted(
                                    Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), sortkey))
                            ).collect(Collectors.toList());
                        }
                    }
                }

            } else {
                tablelistdata = tablelistdata.stream().sorted(Comparator.comparing(m -> ((Map) m).get("orderindex") == null ? "" : ((Map) m).get("orderindex").toString())).collect(Collectors.toList());
            }
        }

        resultMap.put("tablelistdata", tablelistdata);
        resultMap.put("tabletitledata", tabletitledata);
        tabledata.put("tabledata", resultMap);
        tabledata.put("datacount", countData);
        return tabledata;
    }

    private static String comparingStringByKey(Map<String, Object> map, String key) {
        if (map.get(key) instanceof Map) {
            return ((Map) map.get(key)).get("value") + "";
        } else {
            return map.get(key) + "";
        }
    }

    private static Double comparingDoubleByKey(Map<String, Object> map, String key) {
        Object value;
        if (map.get(key) instanceof Map) {
            value = ((Map) map.get(key)).get("value");
        } else {
            value = map.get(key);
        }
        return value != null ? Double.parseDouble(value.toString()) : -1;
    }

    /**
     * @author: lip
     * @date: 2020/1/6 0006 下午 6:29
     * @Description: 获取视频RTSP数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, List<Map<String, Object>>> getRTSPData(Integer pointType) {

        Map<String, Object> paramMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> idAndList = new HashMap<>();
        List<Map<String, Object>> rtspList;
        paramMap.put("monitorpointtypes", Arrays.asList(pointType));
        List<Map<String, Object>> RTSPDataList = onlineMapper.getMonitorPointRTSPDataByParam(paramMap);
        if (RTSPDataList.size() > 0) {
            String monitorPointId;
            for (Map<String, Object> RTSPData : RTSPDataList) {
                if (RTSPData.get("rtsp") != null) {
                    monitorPointId = RTSPData.get("monitorpointid").toString();
                    if (idAndList.containsKey(monitorPointId)) {
                        rtspList = idAndList.get(monitorPointId);
                    } else {
                        rtspList = new ArrayList<>();
                    }
                    Map<String, Object> rtspMap = new HashMap<>();
                    rtspMap.put("rtsp", RTSPData.get("rtsp"));
                    rtspMap.put("id", RTSPData.get("id"));
                    rtspMap.put("name", RTSPData.get("name"));
                    rtspMap.put("vediomanufactor", RTSPData.get("vediomanufactor"));
                    rtspList.add(rtspMap);
                    idAndList.put(monitorPointId, rtspList);
                }
            }
        }
        return idAndList;
    }


    /**
     * @author: lip
     * @date: 2019/12/26 0026 下午 1:47
     * @Description: 根据监测点类型设置标记名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    private String getMarkName(Integer pointType) {
        String markName = "已停用";
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointType)) {
            case WasteGasEnum:
            case WasteWaterEnum:
                markName = "已停产";
                break;
            case RainEnum:
                markName = "未排放";
                break;
        }
        return markName;
    }

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据监测点类型和mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    private List<Map<String, String>> getMonitorPollutantByParam(List<String> mns, Integer pointType) {
        CommonTypeEnum.MonitorPointTypeEnum PointTypeEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointType);
        List<Map<String, String>> maps = new ArrayList<>();
        switch (PointTypeEnum) {
            case AirEnum:
                maps = airMonitorStationMapper.getMonitorPollutantByParam(mns);
                break;
            case WasteGasEnum:
            case SmokeEnum:
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", mns);
                paramMap.put("monitorpointtype", pointType);
                maps = gasOutPutInfoMapper.getMonitorPollutantByParam(paramMap);
                break;
            case WasteWaterEnum:
            case RainEnum:
                maps = waterOutputInfoMapper.getMonitorPollutantByParam(mns);
                break;
            case FactoryBoundaryStinkEnum:
            case FactoryBoundarySmallStationEnum:
                maps = unorganizedMonitorPointInfoMapper.getMonitorPollutantByParam(mns);
                break;
            case EnvironmentalStinkEnum:
            case EnvironmentalVocEnum:
                maps = otherMonitorPointMapper.getMonitorPollutantByParam(mns);
                break;
            case WaterQualityEnum:
                maps = waterStationMapper.getMonitorPollutantByParam(mns);
                break;
        }
        return maps;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/29 11:24
     * @Description: 根据点位类型组装数据 实时数据
     * @updateUser:xsm
     * @updateDate:2019/6/13
     * @updateDescription: 修改列表返回结果，替换企业名称所显示的内容为企业简称字段的值
     * @param:
     * @return:
     */
    private Map<String, Object> formatRealTimeListDataByPointType(Integer pointType, Map<String, Object> outPut) {
        Map<String, Object> resultMap = new HashMap<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointType)) {
            //废水排口
            case WasteWaterEnum:
                resultMap.put("shortername", outPut.get("shortername"));//企业名称显示企业简称字段的值
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("outputname", outPut.get("outputname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //废气排口
            case WasteGasEnum:
            case SmokeEnum:
                resultMap.put("shortername", outPut.get("shortername"));//企业名称显示企业简称字段的值
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("outputname", outPut.get("outputname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //雨水排口
            case RainEnum:
                resultMap.put("shortername", outPut.get("shortername"));//企业名称显示企业简称字段的值
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("outputname", outPut.get("outputname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //空气监测点
            case AirEnum:
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //水质监测点
            case WaterQualityEnum:
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //VOC监测点
            case EnvironmentalVocEnum:
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //恶臭监测点
            case EnvironmentalStinkEnum:
            case MicroStationEnum://微站
            case EnvironmentalDustEnum://扬尘
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //恶臭监测点
            case meteoEnum:
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //厂界恶臭
            case FactoryBoundaryStinkEnum:

                resultMap.put("shortername", outPut.get("shortername"));//企业名称显示企业简称字段的值
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                resultMap.put("monitorpointtype", outPut.get("monitorpointtype"));
                break;
            //厂界小型站
            case FactoryBoundarySmallStationEnum:
                resultMap.put("shortername", outPut.get("shortername"));//企业名称显示企业简称字段的值
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //储罐
            case StorageTankAreaEnum:
                resultMap.put("shortername", outPut.get("pollutionname"));//企业名称显示企业简称字段的值
                resultMap.put("storagetankareaname", outPut.get("shortername"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            //生产装置
            case ProductionSiteEnum:

                resultMap.put("shortername", outPut.get("shortername") == null ? "" : outPut.get("shortername").toString());//企业名称显示企业简称字段的值
                resultMap.put("majorhazardsourcesname", outPut.get("MajorHazardSourcesName"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            case SecurityLeakageMonitor:
            case SecurityCombustibleMonitor: //可燃易爆气体监测点类型
            case SecurityToxicMonitor: //有毒有害气体监测点类型
                resultMap.put("shortername", outPut.get("PollutionName").toString());//企业名称显示企业简称字段的值
                resultMap.put("storagetankareaname", outPut.get("ShorterName"));
                resultMap.put("outputid", outPut.get("outputid"));
                resultMap.put("pollutionid", outPut.get("Pk_PollutionID"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("monitorpointname", outPut.get("outputname"));
                resultMap.put("onlinestatus", outPut.get("Status"));
                break;
        }
        return resultMap;

    }


    /**
     * @author: zhangzc
     * @date: 2019/6/11 15:35
     * @Description: 获取表头数据数据 - 预警及超标报警
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getTableTitleDataForOverAlarm(Integer type) {
        /*
         * type
         * 1 为废水在线监测实时数据一览表头
         * 2 为废气在线监测实时数据一览表头
         * 37 为雨水在线监测实时数据一览表头
         * 5 为空气在线监测实时数据一览表头
         * 10 为VOC在线监测实时数据一览表头
         * 9 为恶臭在线监测实时数据一览表头
         * */
        List<Map<String, Object>> maps = new ArrayList<>();
        List<String> props = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
            case WasteWaterEnum:
                props.add("shortername");
                props.add("outputname");
                labels.add("企业名称");
                labels.add("排口名称");
                break;
            case WasteGasEnum:
            case SmokeEnum:
                props.add("shortername");
                props.add("outputname");
                labels.add("企业名称");
                labels.add("排口名称");
                break;
            case RainEnum:
                props.add("shortername");
                props.add("outputname");
                labels.add("企业名称");
                labels.add("排口名称");
                break;
            case AirEnum:
                props.add("monitorpointname");
                labels.add("监测点名称");
                break;
            case WaterQualityEnum:
                props.add("monitorpointname");
                labels.add("监测点名称");
                break;
            case EnvironmentalStinkEnum:
            case MicroStationEnum:
            case EnvironmentalDustEnum://扬尘
                props.add("monitorpointname");
                labels.add("监测点名称");
                break;
            case EnvironmentalVocEnum:
                props.add("monitorpointname");
                labels.add("监测点名称");
                break;
            case FactoryBoundaryStinkEnum:

                props.add("shortername");
                props.add("monitorpointname");
                labels.add("企业名称");
                labels.add("监测点名称");
                break;
            case FactoryBoundarySmallStationEnum:
                props.add("shortername");
                props.add("monitorpointname");
                labels.add("企业名称");
                labels.add("监测点名称");
                break;
            case StorageTankAreaEnum:
                props.add("shortername");
                props.add("monitorpointname");
                labels.add("区域名称");
                labels.add("储罐编号");
                break;
            case ProductionSiteEnum:
                props.add("shortername");
                props.add("monitorpointname");
                labels.add("生产场所名称");
                labels.add("生产装置名称");
                break;
            case SecurityLeakageMonitor: //安全泄露
            case SecurityCombustibleMonitor: //可燃易爆气体监测点类型
            case SecurityToxicMonitor: //有毒有害气体监测点类型
                props.add("shortername");
                props.add("monitorpointname");
                labels.add("企业名称");
                labels.add("监测点名称");
                break;
        }
        for (int i = 0; i < props.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            if (props.get(i).equals("onlinestatus")) {
                map.put("type", "element");
            }
            if (props.get(i).equals("monitortime")) {
                map.put("minwidth", "200px");
            }
            if (props.get(i).equals("outputname")) {
                map.put("minwidth", "200px");
            }
            if (props.get(i).equals("monitorpointname")) {
                map.put("minwidth", "120px");
            }
            if (props.get(i).equals("shortername")) {
                map.put("minwidth", "300px");
            }
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", props.get(i));
            map.put("label", labels.get(i));
            map.put("align", "center");
            maps.add(map);
        }
        return maps;

    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 10:39
     * @Description: 获取实时数据一览表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getTableTitleDataForRealTime(Integer type) {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<String> props = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        CommonTypeEnum.MonitorPointTypeEnum codeByInt = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type);
        switch (codeByInt) {
            case WasteWaterEnum:
                props.add("shortername");
                props.add("outputname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("排口名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case SmokeEnum:
                props.add("shortername");
                props.add("outputname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("排口名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case WasteGasEnum:
                props.add("shortername");
                props.add("outputname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("排口名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case RainEnum:
                props.add("shortername");
                props.add("outputname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("排口名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case AirEnum:
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("onlinestatus");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case WaterQualityEnum:
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("onlinestatus");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case EnvironmentalVocEnum:
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("onlinestatus");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case EnvironmentalStinkEnum:
            case MicroStationEnum:
            case EnvironmentalDustEnum:
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("onlinestatus");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case meteoEnum:
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("onlinestatus");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case FactoryBoundaryStinkEnum:
                props.add("shortername");
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case FactoryBoundarySmallStationEnum:

                props.add("shortername");
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case StorageTankAreaEnum:
                props.add("storagetankareaname");
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("区域位置");
                labels.add("储罐编号");
                labels.add("储罐状态");
                labels.add("监测时间");
                break;
            case ProductionSiteEnum:
                props.add("majorhazardsourcesname");
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("生产场所名称");
                labels.add("生产装置名称");
                labels.add("装置状态");
                labels.add("监测时间");
                break;
        }
        for (int i = 0; i < props.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            if (props.get(i).equals("onlinestatus")) {
                map.put("type", "element");
            }
            if (props.get(i).equals("monitortime")) {
                map.put("minwidth", "165px");
            }
            if (props.get(i).equals("outputname")) {
                map.put("minwidth", "160px");
            }
            if (props.get(i).equals("shortername") || props.get(i).equals("monitorpointname")) {
                map.put("minwidth", "160px");
            }
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", props.get(i));
            map.put("label", labels.get(i));
            map.put("align", "center");
            maps.add(map);
        }
        return maps;
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 10:32
     * @Description: 获取报表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getTableTitleDataForReport(Integer type) {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<String> props = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        switch (type) {
            case 201:
                props.add("monitortime");
                labels.add("监测时间");
                break;
            case 202:
                props.add("monitortime");
                labels.add("监测时间");
                props.add("aqi");
                labels.add("AQI");
                break;
            case 101:
                props.add("shortername");
                props.add("outputname");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("排口名称");
                labels.add("监测时间");
                break;
            case 102:
                props.add("monitorpointname");
                props.add("monitortime");
                labels.add("监测点名称");
                labels.add("监测时间");
                break;
            case 104:
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("aqi");
                labels.add("监测点名称");
                labels.add("监测时间");
                labels.add("AQI");
                break;

            case 103:
                props.add("shortername");
                props.add("monitorpointname");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("监测点名称");
                labels.add("监测时间");
                break;
            case 105:
                props.add("storagetankareaname");
                props.add("monitorpointname");
                props.add("monitortime");
                labels.add("区域位置");
                labels.add("储罐编号");
                labels.add("监测时间");
                break;
            case 106:
                props.add("majorhazardsourcesname");
                props.add("monitorpointname");
                props.add("monitortime");
                labels.add("生产场所");
                labels.add("生产装置");
                labels.add("监测时间");
                break;

        }
        for (int i = 0; i < props.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            if (props.get(i).equals("monitortime")) {
                map.put("minwidth", "165px");
            }
            if (props.get(i).equals("outputname")) {
                map.put("minwidth", "160px");
            }
            if (props.get(i).equals("shortername") || props.get(i).equals("monitorpointname")) {
                map.put("minwidth", "160px");
            }
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", props.get(i));
            map.put("label", labels.get(i));
            map.put("align", "center");
            maps.add(map);
        }
        return maps;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 14:04
     * @Description: 组件污染物表头
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPollutantTableTitleData(List<Map<String, Object>> pollutants) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Map<String, Object> pollutant : pollutants) {
            Map<String, Object> map = new HashMap<>();
            map.put("headeralign", "center");
            map.put("showhide", true);
            map.put("prop", pollutant.get("code"));
            String label = pollutant.get("name").toString();
            if (pollutant.get("unit") != null && pollutant.get("unit") != "") {
                label += "(" + pollutant.get("unit").toString() + ")";
            }
            map.put("label", label);
            map.put("type", "contaminated");
            map.put("align", "center");
            maps.add(map);
        }
        return maps;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/28 14:03
     * @Description: 获取实时数据mongdb
     * @param:
     * @return:
     */
    private List<Document> getRealTimeDataByMNsAndDataType(List<String> mns) {
        List<Document> list = new ArrayList<>();
        Bson bson = Filters.and(eq("Type", "RealTimeData"), in("DataGatherCode", mns));
        FindIterable<Document> documents = mongoTemplate.getCollection(latestData_db).find(bson);
        for (Document document : documents) {
            list.add(document);
        }
        return list;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 14:05
     * @Description: 数据报表表头获取
     * @param: reportType
     * 1 为实时数据一览 2 为数据报表 ；
     * pointType 1 为排口 2 为监测点 ；
     * polltantType 点位业务类型用于获取污染物 1 废水 2 废气 3 雨水
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForReport(Map<String, Object> titleMap) {


        Integer reportType = Integer.parseInt(titleMap.get("reportType").toString());
        Integer pointType = Integer.parseInt(titleMap.get("pointType").toString());
        Integer pollutantType = Integer.parseInt(titleMap.get("pollutantType").toString());
        List<String> pointIDs = (List<String>) titleMap.get("outputids");
        //污染物信息
        List<Map<String, Object>> pollutants = new ArrayList<>();
        if (pointIDs.size() > 0) {
            Map<String, Object> paramMap = new HashMap<>();
            Set<String> codes;
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutantType)) {
                case WasteWaterEnum:     //废水
                    paramMap.put("pollutanttype", pollutantType);
                    paramMap.put("outputids", pointIDs);
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    List<WaterOutPutPollutantSetVO> waterPollutants = waterOutPutPollutantSetMapper.getWaterOrRainPollutantsByParamMap(paramMap);
                    codes = new HashSet<>();
                    for (WaterOutPutPollutantSetVO pollutant : waterPollutants) {
                        if (!codes.contains(pollutant.getPollutantFactorVO().getCode())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", pollutant.getPollutantFactorVO().getCode());
                            map.put("name", pollutant.getPollutantFactorVO().getName());
                            if ("show".equals(titleMap.get("isShowFlow"))) {
                                map.put("unit", "t");
                            } else {
                                map.put("unit", pollutant.getPollutantFactorVO().getPollutantunit());
                            }
                            pollutants.add(map);
                            codes.add(pollutant.getPollutantFactorVO().getCode());
                        }
                    }
                    break;
                case WasteGasEnum://废气
                case SmokeEnum://烟气
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    List<GasOutPutPollutantSetVO> gasPollutants = gasOutPutPollutantSetMapper.getGasPollutantsByParamMap(paramMap);
                    codes = new HashSet<>();
                    for (GasOutPutPollutantSetVO pollutant : gasPollutants) {
                        if (!codes.contains(pollutant.getPollutantFactorVO().getCode())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", pollutant.getPollutantFactorVO().getCode());
                            map.put("name", pollutant.getPollutantFactorVO().getName());
                            if ("show".equals(titleMap.get("isShowFlow"))) {
                                map.put("unit", "t");
                            } else {
                                map.put("unit", pollutant.getPollutantFactorVO().getPollutantunit());
                            }
                            pollutants.add(map);
                            codes.add(pollutant.getPollutantFactorVO().getCode());
                        }
                    }
                    break;
                case RainEnum:     //雨水
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutanttype", pollutantType);
                    paramMap.put("outputids", pointIDs);
                    List<WaterOutPutPollutantSetVO> rainPollutants = waterOutPutPollutantSetMapper.getWaterOrRainPollutantsByParamMap(paramMap);
                    codes = new HashSet<>();
                    for (WaterOutPutPollutantSetVO pollutant : rainPollutants) {
                        if (!codes.contains(pollutant.getPollutantFactorVO().getCode())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", pollutant.getPollutantFactorVO().getCode());
                            map.put("name", pollutant.getPollutantFactorVO().getName());
                            if ("show".equals(titleMap.get("isShowFlow"))) {
                                map.put("unit", "t");
                            } else {
                                map.put("unit", pollutant.getPollutantFactorVO().getPollutantunit());
                            }
                            pollutants.add(map);
                            codes.add(pollutant.getPollutantFactorVO().getCode());
                        }

                    }
                    break;
                case AirEnum:     //空气站点
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> airPollutants = pollutantFactorMapper.getAirPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : airPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case EnvironmentalVocEnum:     //voc
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutantType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> vocPollutants = pollutantFactorMapper.getOtherPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : vocPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case WaterQualityEnum:     //水质
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutantType", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> waterStationPollutants = pollutantFactorMapper.getWaterStationPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : waterStationPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case EnvironmentalStinkEnum:
                    //恶臭
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutantType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> stinkPollutants = pollutantFactorMapper.getOtherPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : stinkPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case MicroStationEnum:
                    //微站
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutantType", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> microPollutants = pollutantFactorMapper.getOtherPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : microPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case EnvironmentalDustEnum:
                    //扬尘
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutantType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode());
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> dustPollutants = pollutantFactorMapper.getOtherPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : dustPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case meteoEnum:
                    //气象
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("pollutantType", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> weatherPollutants = pollutantFactorMapper.getOtherPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : weatherPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("Code"));
                        map.put("name", pollutant.get("Name"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("Unit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case FactoryBoundaryStinkEnum:     //厂界恶臭
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    List<GasOutPutPollutantSetVO> entStinkPollutants = gasOutPutPollutantSetMapper.getGasPollutantsByParamMap(paramMap);
                    for (GasOutPutPollutantSetVO pollutant : entStinkPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.getPollutantFactorVO().getCode());
                        map.put("name", pollutant.getPollutantFactorVO().getName());
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.getPollutantFactorVO().getPollutantunit());
                        }
                        pollutants.add(map);
                    }
                    break;
                case FactoryBoundarySmallStationEnum:     //厂界恶臭
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    List<GasOutPutPollutantSetVO> entSmallStationPollutants = gasOutPutPollutantSetMapper.getGasPollutantsByParamMap(paramMap);
                    for (GasOutPutPollutantSetVO pollutant : entSmallStationPollutants) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.getPollutantFactorVO().getCode());
                        map.put("name", pollutant.getPollutantFactorVO().getName());
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.getPollutantFactorVO().getPollutantunit());
                        }
                        pollutants.add(map);
                    }
                    break;

                case StorageTankAreaEnum:     //储罐
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode());
                    List<Map<String, Object>> storageTankPollutantSetsByOutputIds = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
                    for (Map<String, Object> pollutant : storageTankPollutantSetsByOutputIds) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("pollutantcode"));
                        map.put("name", pollutant.get("pollutantname"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("PollutantUnit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case ProductionSiteEnum:     //生产装置
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode());
                    List<Map<String, Object>> productDevicePollutantSetsByOutputIds = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
                    for (Map<String, Object> pollutant : productDevicePollutantSetsByOutputIds) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("pollutantcode"));
                        map.put("name", pollutant.get("pollutantname"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("PollutantUnit"));
                        }
                        pollutants.add(map);
                    }
                    break;
                case SecurityLeakageMonitor:     //风险区域监测点
                case SecurityCombustibleMonitor: //可燃易爆气体监测点类型
                case SecurityToxicMonitor: //有毒有害气体监测点类型
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode());
                    List<Map<String, Object>> riskAreaMonitorPollutantSetsByOutputIds = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
                    for (Map<String, Object> pollutant : riskAreaMonitorPollutantSetsByOutputIds) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", pollutant.get("pollutantcode"));
                        map.put("name", pollutant.get("pollutantname"));
                        if ("show".equals(titleMap.get("isShowFlow"))) {
                            map.put("unit", "t");
                        } else {
                            map.put("unit", pollutant.get("PollutantUnit"));
                        }
                        pollutants.add(map);
                    }
                    break;
            }
        }
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        switch (reportType) {
            case 1:  //实时数据一览
                if (CommonTypeEnum.getOutPutTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(201);
                } else if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(202);
                } else if (CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(201);
                } else if (CommonTypeEnum.getMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(201);
                } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(201);
                } else if (CommonTypeEnum.getRiskAreaMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(103);
                } else if (ProductionSiteEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(106);
                } else if (StorageTankAreaEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(105);
                }
                break;
            case 2: //数据报表
                if (CommonTypeEnum.getOutPutTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(101);
                } else if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(104);
                } else if (CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(102);
                } else if (CommonTypeEnum.getMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(102);
                } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(103);
                } else if (CommonTypeEnum.getRiskAreaMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(102);
                } else if (StorageTankAreaEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(105);
                } else if (ProductionSiteEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(106);
                }
                break;
        }
        //根据污染物获取表头
        if (pollutants.size() > 0) {
            List<Map<String, Object>> pollutantTableTitleData = getPollutantTableTitleData(pollutants);
            tableTitleData.addAll(pollutantTableTitleData);
        } else {
            tableTitleData.clear();
        }
        return tableTitleData;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/31 14:00
     * @Description: 根据监测点类型以及污染物Code获取该类型监测点此污染物24小时排放小时数据
     * @param: code 污染物编码；monitorPointType 监测点类型 9 是恶臭、 10 是VOC
     * @return:
     */
    @Override
    public Map<String, Object> get24HourMonitorDataByPollutantCodeForMonitorPoint(String code, Integer monitorPointType, String startTime, String endTime, String userid) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        //获取用户数据权限
        paramMap.put("monitorpointtype", monitorPointType);
        paramMap.put("userid", userid);
        List<Map<String, Object>> relationdata = userMonitorPointRelationDataMapper.getUserMonitorPointRelationDataByParams(paramMap);
        if (relationdata != null && relationdata.size() > 0) {
            Set outputids = new HashSet();
            for (Map<String, Object> map : relationdata) {
                outputids.add(map.get("FK_MonitorPointID"));
            }
            paramMap.put("outputids", outputids);
            paramMap.put("monitorPointType", monitorPointType);
            List<Map<String, Object>> monitorPoints = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
            //mn号
            String mnCommon;

            List<String> mns = new ArrayList<>();
            for (Map<String, Object> outPut : monitorPoints) {
                if (outPut.get("dgimn") != null) {
                    mnCommon = outPut.get("dgimn").toString();
                    mns.add(mnCommon);
                }
            }
            //获取风关联数据
            paramMap.put("dgimns", mns);
            Set<String> airMns = new HashSet<>();
            Map<String, Object> otherMNAndAir = new HashMap<>();
            List<Map<String, Object>> airAndOtherData = otherMonitorPointMapper.getAirMNAndOtherMNByParam(paramMap);
            if (airAndOtherData.size() > 0) {
                for (Map<String, Object> mapIndex : airAndOtherData) {
                    mnCommon = mapIndex.get("airmn").toString();
                    if (!mns.contains(mnCommon)) {
                        mns.add(mnCommon);
                    }
                    airMns.add(mnCommon);
                    otherMNAndAir.put(mapIndex.get("pointmn").toString(), mnCommon);
                }
            }
            startTime = startTime + ":00:00";
            endTime = endTime + ":00:00";
            List<String> times = new ArrayList<>();
            for (String time : DataFormatUtil.separateTimeForHour(startTime, endTime, 1)) {
                times.add(time + ":00:00");
            }
            times.add(endTime);
            List<Date> timesDate = times.stream().map(DataFormatUtil::getDateYMDHMS).collect(Collectors.toList());
            resultMap.put("times", times.stream().map(hour -> hour.substring(11, 13)).collect(Collectors.toList()));
            List<Map<String, Object>> monitordata = new ArrayList<>();
            String windcode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
            List<String> pollutantCodes = Arrays.asList(windcode, code);

            if (monitorPoints.size() > 0) {
                Aggregation aggregation = newAggregation(
                        match(Criteria.where("DataGatherCode").in(mns)
                                .and("MonitorTime").in(timesDate)
                                .and("HourDataList.PollutantCode").in(pollutantCodes)),
                        unwind("HourDataList"),
                        match(Criteria.where("HourDataList.PollutantCode").in(pollutantCodes)),
                        project("MonitorTime", "DataGatherCode")
                                .and("HourDataList.AvgStrength").as("AvgStrength")
                                .and("HourDataList.PollutantCode").as("PollutantCode")

                );
                AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
                List<Document> documents = aggregationResults.getMappedResults();
                if (documents.size() > 0) {
                    String monitorTime;
                    String pollutantCode;
                    Object value;
                    Map<String, Object> timeAndValue;
                    Map<String, Map<String, Object>> mnAirAndTimeAndValue = new HashMap<>();
                    Map<String, Map<String, Object>> mnOtherAndTimeAndValue = new HashMap<>();
                    for (Document document : documents) {
                        mnCommon = document.getString("DataGatherCode");
                        pollutantCode = document.getString("PollutantCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                        value = document.getString("AvgStrength");
                        if (airMns.contains(mnCommon) && pollutantCode.equals(windcode)) {//风向数据
                            if (mnAirAndTimeAndValue.containsKey(mnCommon)) {
                                timeAndValue = mnAirAndTimeAndValue.get(mnCommon);
                            } else {
                                timeAndValue = new HashMap<>();
                            }
                            timeAndValue.put(monitorTime, value);
                            mnAirAndTimeAndValue.put(mnCommon, timeAndValue);
                        } else {
                            if (mnOtherAndTimeAndValue.containsKey(mnCommon)) {
                                timeAndValue = mnOtherAndTimeAndValue.get(mnCommon);
                            } else {
                                timeAndValue = new HashMap<>();
                            }
                            timeAndValue.put(monitorTime, value);
                            mnOtherAndTimeAndValue.put(mnCommon, timeAndValue);
                        }
                    }
                    for (Map<String, Object> monitorPoint : monitorPoints) {
                        mnCommon = monitorPoint.get("dgimn").toString();
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("name", monitorPoint.get("monitorpointname"));
                        List<Object> values = new ArrayList<>();
                        timeAndValue = mnOtherAndTimeAndValue.get(mnCommon);
                        for (String time : times) {
                            if (timeAndValue != null) {
                                Object AvgStrength = timeAndValue.get(time);
                                values.add(AvgStrength);
                            } else {
                                values.add(null);
                            }
                        }
                        dataMap.put("values", values);
                        List<Map<String, Object>> speeddata = new ArrayList<>();
                        timeAndValue = mnAirAndTimeAndValue.get(otherMNAndAir.get(mnCommon));
                        for (String time : times) {
                            Map<String, Object> map = new HashMap<>();
                            value = null;
                            if (timeAndValue != null) {
                                value = timeAndValue.get(time);
                            }
                            map.put("speedvalue", value);
                            if (value != null) {
                                map.put("speedname", DataFormatUtil.windDirectionSwitch(Double.parseDouble(value.toString()), "name"));
                            } else {
                                map.put("speedname", null);
                            }
                            speeddata.add(map);
                        }
                        dataMap.put("speeddata", speeddata);
                        monitordata.add(dataMap);
                    }
                }
            }
            resultMap.put("monitordata", monitordata);
        }
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2020/7/7 10:17
     * @Description: 根据污染物编码和监测类型获取该类型监测点（恶臭、Voc、扬尘）近24小时该污染物浓度数据
     * @param: code 污染物编码；monitorPointType 监测点类型 9 是恶臭、 10 是VOC  12扬尘  40 厂界恶臭
     * @return:
     */
    @Override
    public Map<String, Object> get24HourMonitorDataByPollutantCodeAndMonitorType(String code, Integer monitortype, String startTime, String endTime, String userid) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        //获取用户数据权限
        paramMap.put("userid", userid);
        paramMap.put("monitorPointType", monitortype);
        List<Map<String, Object>> monitorPoints = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        //mn号
        String mnCommon;
        List<String> mns = new ArrayList<>();
        for (Map<String, Object> outPut : monitorPoints) {
            if (outPut.get("dgimn") != null) {
                mnCommon = outPut.get("dgimn").toString();
                mns.add(mnCommon);
            }
        }
        startTime = startTime + ":00:00";
        endTime = endTime + ":00:00";
        List<String> times = new ArrayList<>();
        for (String time : DataFormatUtil.separateTimeForHour(startTime, endTime, 1)) {
            times.add(time + ":00:00");
        }
        times.add(endTime);
        List<Date> timesDate = times.stream().map(DataFormatUtil::getDateYMDHMS).collect(Collectors.toList());
        resultMap.put("times", times.stream().map(hour -> hour.substring(11, 13)).collect(Collectors.toList()));
        List<Map<String, Object>> monitordata = new ArrayList<>();
        List<String> pollutantCodes = Arrays.asList(code);
        if (monitorPoints.size() > 0) {
            Aggregation aggregation = newAggregation(
                    match(Criteria.where("DataGatherCode").in(mns)
                            .and("MonitorTime").in(timesDate)
                            .and("HourDataList.PollutantCode").in(pollutantCodes)),
                    unwind("HourDataList"),
                    match(Criteria.where("HourDataList.PollutantCode").in(pollutantCodes)),
                    project("MonitorTime", "DataGatherCode")
                            .and("HourDataList.AvgStrength").as("AvgStrength")
                            .and("HourDataList.PollutantCode").as("PollutantCode")

            );
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
            List<Document> documents = aggregationResults.getMappedResults();
            if (documents.size() > 0) {
                String monitorTime;
                Object value;
                Map<String, Object> timeAndValue;
                Map<String, Map<String, Object>> mnOtherAndTimeAndValue = new HashMap<>();
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    monitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                    value = document.getString("AvgStrength");
                    if (mnOtherAndTimeAndValue.containsKey(mnCommon)) {
                        timeAndValue = mnOtherAndTimeAndValue.get(mnCommon);
                    } else {
                        timeAndValue = new HashMap<>();
                    }
                    timeAndValue.put(monitorTime, value);
                    mnOtherAndTimeAndValue.put(mnCommon, timeAndValue);
                }
                for (Map<String, Object> monitorPoint : monitorPoints) {
                    mnCommon = monitorPoint.get("dgimn").toString();
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("name", monitorPoint.get("monitorpointname"));
                    List<Object> values = new ArrayList<>();
                    timeAndValue = mnOtherAndTimeAndValue.get(mnCommon);
                    for (String time : times) {
                        if (timeAndValue != null) {
                            Object AvgStrength = timeAndValue.get(time);
                            values.add(AvgStrength);
                        } else {
                            values.add(null);
                        }
                    }
                    dataMap.put("values", values);
                    monitordata.add(dataMap);
                }
            }
        }
        resultMap.put("monitordata", monitordata);
        return resultMap;
    }

    /**
     * @author: zhangzc
     * @date: 2019/6/11 10:43
     * @Description: 获取超标报警数据
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPollutantEarlyAndOverAlarmsByParamMap(Integer pageNum, Integer pageSize, List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
        Map<String, Object> resultTableDataMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        //点位类型
        Integer pointType = (Integer) paramMap.get("pointtype");
        //表头数据
        List<Map<String, Object>> tabletitledata = getTableTitleDataForOverAlarm(pointType);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(pointType);
        Set<Object> pollutantCodes = new HashSet<>();
        //污染物多级表头
        List<Map<String, Object>> pollutantTitle = new ArrayList<>();
        for (Map<String, Object> pollutant : pollutants) {
            //污染物Code
            String propValue = pollutant.get("Code").toString();
            pollutantCodes.add(propValue);
            String labelValue = pollutant.get("Name").toString() + "（次）";
            Map<String, Object> fatherTitle = getSingleTitle(propValue, labelValue, "");
            //子表头
            List<Map<String, Object>> childrenList = new ArrayList<>();
            childrenList.add(getSingleTitle(propValue + "_earlywarnnum", "预警", ""));
            childrenList.add(getSingleTitle(propValue + "_exceptionnum", "异常", ""));
            childrenList.add(getSingleTitle(propValue + "_overnum", "超标", ""));
            fatherTitle.put("children", childrenList);
            pollutantTitle.add(fatherTitle);
        }
        //将污染物表头数据放入表头数据中
        tabletitledata.addAll(pollutantTitle);

        //列表数据
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        if (outPuts.size() > 0) {
            //数据类型
            String dataType = paramMap.get("datatype").toString();
            //mn号
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> outPut : outPuts) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                }
            }

            List<String> countTimes = (List<String>) paramMap.get("counttime");
            Date startDate = DataFormatUtil.getDateYMDHMS(countTimes.get(0) + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(countTimes.get(1) + " 23:59:59");
            GroupOperation group = group("DataGatherCode", "PollutantCode").count().as("num");
            //异常
            Aggregation exceptionAggregation = newAggregation(
                    match(Criteria.where("DataType").is(dataType).andOperator(
                            Criteria.where("DataGatherCode").in(mns),
                            Criteria.where("ExceptionTime").gte(startDate).lte(endDate),
                            Criteria.where("PollutantCode").in(pollutantCodes)
                    )),
                    group
            );
            //超标
            Aggregation overAggregation = newAggregation(
                    match(Criteria.where("DataType").is(dataType).andOperator(
                            Criteria.where("DataGatherCode").in(mns),
                            Criteria.where("OverTime").gte(startDate).lte(endDate),
                            Criteria.where("PollutantCode").in(pollutantCodes)
                    )),
                    group
            );
            //预警
            Aggregation earlyWarnAggregation = newAggregation(
                    match(Criteria.where("DataType").is(dataType).andOperator(
                            Criteria.where("DataGatherCode").in(mns),
                            Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate),
                            Criteria.where("PollutantCode").in(pollutantCodes)
                    )),
                    group
            );
            AggregationResults<Document> exceptionData = mongoTemplate.aggregate(exceptionAggregation, exceptionData_db, Document.class);
            AggregationResults<Document> overData = mongoTemplate.aggregate(overAggregation, overData_db, Document.class);
            AggregationResults<Document> earlyWarnData = mongoTemplate.aggregate(earlyWarnAggregation, earlyWarnData_db, Document.class);
            List<Document> exceptionDocuments = exceptionData.getMappedResults();
            List<Document> overDocuments = overData.getMappedResults();
            List<Document> earlyWarnDocuments = earlyWarnData.getMappedResults();

            for (Map<String, Object> outPut : outPuts) {
                String mn = outPut.get("dgimn").toString();
                Map<String, Object> tableData = formatRealTimeListDataByPointType(pointType, outPut);
                boolean flag = false;
                for (Map<String, Object> pollutant : pollutants) {
                    boolean flag2 = false;
                    String pollutantCode = pollutant.get("Code").toString();
                    Map<String, Object> pollutantDataMap = new HashMap<>();
                    Object earlyWarnnum = getNumForOverOrAlarmOrException(earlyWarnDocuments, mn, pollutantCode);
                    Object exceptionnum = getNumForOverOrAlarmOrException(exceptionDocuments, mn, pollutantCode);
                    Object ovenum = getNumForOverOrAlarmOrException(overDocuments, mn, pollutantCode);
                    if (earlyWarnnum != null) {
                        flag2 = true;
                        pollutantDataMap.put(pollutantCode + "_earlywarnnum", earlyWarnnum);
                    }
                    if (exceptionnum != null) {
                        flag2 = true;
                        pollutantDataMap.put(pollutantCode + "_exceptionnum", exceptionnum);
                    }
                    if (ovenum != null) {
                        flag2 = true;
                        pollutantDataMap.put(pollutantCode + "_overnum", ovenum);
                    }
                    if (flag2) {
                        flag = true;
                        tableData.putAll(pollutantDataMap);
                    }
                }
                if (flag) {
                    tablelistdata.add(tableData);
                }
            }
        }
        //分页
        resultMap.put("total", tablelistdata.size());
        if (pageNum != null && pageSize != null) {
            List<Map<String, Object>> collect = tablelistdata.stream().skip((pageNum - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
            resultMap.put("tablelistdata", collect);
        } else {
            resultMap.put("tablelistdata", tablelistdata);
        }
        resultMap.put("tabletitledata", tabletitledata);
        resultTableDataMap.put("tabledata", resultMap);
        return resultTableDataMap;
    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 7:54
     * @Description: 根据统计类型获取污染物超标预警异常详情表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(boolean isoverstandard, Integer counttype, List<String> pollutantcodes, Integer pointtype) {
        Map<String, Object> paramMap2 = new HashMap<>();
        paramMap2.put("codes", pollutantcodes);
        //获取污染物类型
        paramMap2.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap2);
        String pollutantUnit = "";
        if (pollutants.size() > 0) {
            pollutantUnit = pollutants.get(0).get("unit") != null ? pollutants.get(0).get("unit").toString() : null;
        }
        String monitorvalue = "监测值";
        if (pollutantUnit != null && !"".equals(pollutantUnit)) {
            monitorvalue = monitorvalue + "(" + pollutantUnit + ")";
        }
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        tableTitleData.add(getSingleTitle("monitortime", "监测时间", "180px"));
        tableTitleData.add(getSingleTitle("pollutantname", "污染物", "120px"));
        if (counttype == 1) {//预警
            tableTitleData.add(getSingleTitle("datatypename", "数据类型", ""));
            tableTitleData.add(getSingleTitle("monitorvalue", monitorvalue, ""));
            tableTitleData.add(getSingleTitle("concenalarmmaxvalue", "预警限值", ""));
            //tableTitleData.add(getSingleTitle("alarmlevel", "报警级别", ""));
            //tableTitleData.add(getSingleTitle("alarmtype", "报警类型", ""));
        } else if (counttype == 2) {//超标
            tableTitleData.add(getSingleTitle("datatypename", "数据类型", ""));
            tableTitleData.add(getSingleTitle("monitorvalue", monitorvalue, ""));
            tableTitleData.add(getSingleTitle("overmultiple", "超标倍数", ""));
            tableTitleData.add(getSingleTitle("alarmlevel", "报警类别", ""));
            if (isoverstandard == true) {
                tableTitleData.add(getSingleTitle("standardmaxvalue", "标准值", ""));
            } else {
                tableTitleData.add(getSingleTitle("alarmlevelvalue", "报警限值", ""));
            }
        } else if (counttype == 3) {//异常
            tableTitleData.add(getSingleTitle("datatypename", "数据类型", ""));
            tableTitleData.add(getSingleTitle("monitorvalue", monitorvalue, ""));
            tableTitleData.add(getSingleTitle("exceptionvalue", "超限异常范围", ""));
            tableTitleData.add(getSingleTitle("exceptiontype", "异常类型", ""));
        } else if (counttype == 4) {
            tableTitleData.add(getSingleTitle("monitorvalue", monitorvalue, ""));
            tableTitleData.add(getSingleTitle("changemultiple", "突增幅度", ""));
        }
        return tableTitleData;
    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 7:54
     * @Description: 根据统计类型获取污染物超标预警异常详情表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutantEarlyAndOverAlarmsTableListDataByParamMap(Map<String, Object> paramMap) {
        Integer counttype = Integer.parseInt(paramMap.get("counttype").toString());
        Integer pointtype = Integer.parseInt(paramMap.get("pointtype").toString());
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        paramMap.put("monitorpointtype", paramMap.get("pointtype"));
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("codes", pollutantcodes);
        //获取污染物信息
        tempMap.put("pollutanttype", pointtype);
        List<Map<String, Object>> codeAndStandardValue = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(tempMap);
        tempMap.clear();
        for (Map<String, Object> map : pollutants) {
            tempMap.put(map.get("code").toString(), map.get("name"));
        }
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();

        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }

        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        if (counttype == 1) {//预警
            tablelistdata = getEarlyTableListData(tempMap, codeAndStandardValue, codeAndLevel, paramMap);
        } else if (counttype == 2) {//超标
            tablelistdata = getOverTableListData(tempMap, codeAndStandardValue, codeAndLevel, paramMap);
        } else if (counttype == 3) {//异常
            tablelistdata = getExceptionTableListData(tempMap, codeAndStandardValue, paramMap);
        } else if (counttype == 4) {//浓度突变
            paramMap.put("collection", minuteCollection);
            tablelistdata = getHourDataListData(tempMap, paramMap);
        } else if (counttype == 5) {//浓度排放量突变
            paramMap.put("collection", hourFlowCollection);
            tablelistdata = getHourFlowDataListData(tempMap, paramMap);
        }
        return tablelistdata;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:49
     * @Description: 自定义查询条件统计预警超标数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countEarlyAndOverDataByParamMap(Map<String, Object> paramMap) {


        Map<String, Integer> mnAndType = new HashMap<>();
        List<String> mns = new ArrayList<>();
        String mnCommon;
        Integer type;
        Map<String, Object> paramTemp = new HashMap<>();
        //环保监测类型
        List<Integer> monitorpointtypes = new ArrayList<>(CommonTypeEnum.getAllEnvMonitorPointTypeList());
        paramTemp.put("monitorpointtypes", monitorpointtypes);
        List<Map<String, Object>> deviceStatus = onlineMapper.getDeviceStatusDataByParam(paramTemp);
        List<String> watermns = new ArrayList<>();
        for (Map<String, Object> mapIndex : deviceStatus) {
            mnCommon = mapIndex.get("dgimn").toString();
            type = Integer.parseInt(mapIndex.get("fk_monitorpointtypecode").toString());
            mnAndType.put(mnCommon, type);
            mns.add(mnCommon);
        }
        paramMap.put("mns", mns);
        //超阈值
        Map<String, Integer> earlyData = countEarlyDataByParamMap(paramMap, mnAndType);
        //浓度突变
        earlyData = countConcentrationChangeOrFlowChangeDataByParamMap(paramMap, mnAndType, earlyData, CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
        //排放量突变
        earlyData = countConcentrationChangeOrFlowChangeDataByParamMap(paramMap, mnAndType, earlyData, CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        //超限
        Map<String, Integer> overData = countOverDataByParamMap(paramMap, mnAndType);
        //异常
        Map<String, Integer> exceptionData = countExceptionDataByParamMap(paramMap, mnAndType);
        paramMap.clear();
        paramMap.put("earlydata", earlyData);
        paramMap.put("overdata", overData);
        paramMap.put("exceptiondata", exceptionData);
        return paramMap;
    }


    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:44
     * @Description: 获取预警表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: 污染源名称、排口名称、污染物、预警时间、预警值、标准值、报警级别、报警类型
     */
    @Override
    public List<Map<String, Object>> getEarlyTableTitleDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Integer pollutantType = Integer.parseInt(paramMap.get("pollutanttype").toString());
        if (CommonTypeEnum.getOutPutTypeList().contains(pollutantType)) {
            tableTitleData.add(getSingleTitle("shortername", "企业名称", "180px"));
            tableTitleData.add(getSingleTitle("outputname", "排口名称", "120px"));
        } else {
            tableTitleData.add(getSingleTitle("outputname", "监测点名称", "120px"));
        }
        tableTitleData.add(getSingleTitle("pollutantname", "污染物", "120px"));
        tableTitleData.add(getSingleTitle("pollutantunit", "单位", "100px"));
        tableTitleData.add(getSingleTitle("monitortime", "预警时间", "150px"));
        tableTitleData.add(getSingleTitle("monitorvalue", "预警值", ""));
        tableTitleData.add(getSingleTitle("standardvalue", "标准值", ""));
        tableTitleData.add(getSingleTitle("alarmlevel", "报警级别", ""));
        tableTitleData.add(getSingleTitle("alarmtype", "报警类型", ""));
        return tableTitleData;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:44
     * @Description: 获取预警表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEarlyTableListDataByParamMap(Map<String, Object> paramMap) {

        Map<String, Object> codeAndName = new HashMap<>();
        Map<String, Object> codeAndUnit = new HashMap<>();
        Integer pointtype = Integer.parseInt(paramMap.get("pollutanttype").toString());
        //获取污染物信息
        codeAndName.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(codeAndName);
        codeAndName.clear();
        for (Map<String, Object> map : pollutants) {
            codeAndName.put(map.get("code").toString(), map.get("name"));
            if (map.get("unit") != null && !"".equals(map.get("unit"))) {
                codeAndUnit.put(map.get("code").toString(), map.get("unit"));
            }
        }
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        Map<String, Object> outPutAndMNData = getOutPutAndMNDataByPointType(pointtype);
        List<String> mns = (List<String>) outPutAndMNData.get("mnset");
        paramMap.put("mns", mns);
        PageEntity<Document> pageEntity = setEarlyWarnPageData(paramMap, earlyWarnData_db);
        List<Document> documents = pageEntity.getListItems();
        paramMap.put("total", pageEntity.getTotalCount());
        paramMap.put("pages", pageEntity.getPageCount());
        paramMap.put("pagesize", pageEntity.getPageSize());
        paramMap.put("pagenum", pageEntity.getPageNum());

        Map<String, Object> mnAndShorterName = (Map<String, Object>) outPutAndMNData.get("mnandshortername");
        Map<String, Object> mnAndOutputName = (Map<String, Object>) outPutAndMNData.get("mnandoutputname");
        Map<String, Object> mnAndOutputId = (Map<String, Object>) outPutAndMNData.get("mnandoutputid");
        Map<String, Object> codeAndStandardValue;
        Integer pollutantType = Integer.parseInt(paramMap.get("pollutanttype").toString());
        String DataGatherCode = "";
        String PollutantCode = "";
        for (Document document : documents) {
            DataGatherCode = document.getString("DataGatherCode");
            PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
            Map<String, Object> map = new HashMap<>();
            if (CommonTypeEnum.getOutPutTypeList().contains(pollutantType)) {
                map.put("shortername", mnAndShorterName.get(DataGatherCode));
            }
            map.put("outputname", mnAndOutputName.get(DataGatherCode));
            map.put("pollutantname", codeAndName.get(PollutantCode));
            map.put("pollutantunit", codeAndUnit.get(PollutantCode) != null ? codeAndUnit.get(PollutantCode) : "");
            map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("EarlyWarnTime")));
            map.put("monitorvalue", document.get("MonitorValue"));
            codeAndStandardValue = getPollutantStandardValue(mnAndOutputId.get(DataGatherCode).toString(), Arrays.asList(PollutantCode), pointtype);
            map.put("standardvalue", codeAndStandardValue.get(PollutantCode));
            map.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
            map.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel") != null ? document.get("AlarmLevel").toString() : null));
            tablelistdata.add(map);
        }
        return tablelistdata;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:44
     * @Description: 获取超标表头数据
     * @updateUser:xsm
     * @updateDate: 2019/9/11 0011 下午 3:24
     * @updateDescription:获取超标表头数据,修改部分字段KEY
     * @param:
     * @return: 污染源名称、排口名称、污染物、超标时间、超标值、标准值、报警类型
     */
    @Override
    public List<Map<String, Object>> getOverTableTitleDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Integer pollutantType = Integer.parseInt(paramMap.get("pollutanttype").toString());
        if (CommonTypeEnum.getOutPutTypeList().contains(pollutantType)) {
            tableTitleData.add(getSingleTitle("pollutionname", "企业名称", "180px"));
            tableTitleData.add(getSingleTitle("outputname", "排口名称", "120px"));
        } else {
            tableTitleData.add(getSingleTitle("monitorpointname", "监测点名称", "120px"));
        }
        tableTitleData.add(getSingleTitle("datatypename", "数据类型", "120px"));
        tableTitleData.add(getSingleTitle("monitortime", "监测时间", "150px"));
        tableTitleData.add(getSingleTitle("pollutantname", "污染物", "120px"));
        // tableTitleData.add(getSingleTitle("pollutantunit", "单位", "100px"));
        tableTitleData.add(getSingleTitle("monitorvalue", "监测值", ""));
        tableTitleData.add(getSingleTitle("alarmlevel", "报警类别", ""));
        tableTitleData.add(getSingleTitle("alarmlevelvalue", "报警限值", ""));
        tableTitleData.add(getSingleTitle("standardmaxvalue", "标准值", ""));

        return tableTitleData;
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 8:47
     * @Description: 根据自定义查询条件获取超标表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOverTableListDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> codeAndName = new HashMap<>();
        Map<String, Object> codeAndUnit = new HashMap<>();
        Integer pointtype = Integer.parseInt(paramMap.get("pollutanttype").toString());
        //获取污染物信息
        codeAndName.put("pollutanttype", pointtype);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(codeAndName);
        codeAndName.clear();
        for (Map<String, Object> map : pollutants) {
            codeAndName.put(map.get("code").toString(), map.get("name"));
            if (map.get("unit") != null && !"".equals(map.get("unit"))) {
                codeAndUnit.put(map.get("code").toString(), map.get("unit"));
            }

        }
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        Map<String, Object> outPutAndMNData = getOutPutAndMNDataByPointType(pointtype);
        List<String> mns = (List<String>) outPutAndMNData.get("mnset");
        paramMap.put("mns", mns);
        PageEntity<Document> pageEntity = setOverPageData(paramMap, overData_db);
        List<Document> documents = pageEntity.getListItems();
        paramMap.put("total", pageEntity.getTotalCount());
        paramMap.put("pages", pageEntity.getPageCount());
        paramMap.put("pagesize", pageEntity.getPageSize());
        paramMap.put("pagenum", pageEntity.getPageNum());


        Map<String, Object> mnAndShorterName = (Map<String, Object>) outPutAndMNData.get("mnandshortername");
        Map<String, Object> mnAndOutputName = (Map<String, Object>) outPutAndMNData.get("mnandoutputname");
        Map<String, Object> mnAndOutputId = (Map<String, Object>) outPutAndMNData.get("mnandoutputid");
        Map<String, Object> codeAndStandardValue;
        String DataGatherCode = "";
        String PollutantCode = "";
        Integer pollutantType = Integer.parseInt(paramMap.get("pollutanttype").toString());
        for (Document document : documents) {
            DataGatherCode = document.getString("DataGatherCode");
            PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
            Map<String, Object> map = new HashMap<>();
            if (CommonTypeEnum.getOutPutTypeList().contains(pollutantType)) {
                map.put("shortername", mnAndShorterName.get(DataGatherCode));
            }
            map.put("outputname", mnAndOutputName.get(DataGatherCode));
            map.put("pollutantname", codeAndName.get(PollutantCode));
            map.put("pollutantunit", codeAndUnit.get(PollutantCode) != null ? codeAndUnit.get(PollutantCode) : "");
            map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
            map.put("monitorvalue", document.get("MonitorValue"));
            codeAndStandardValue = getPollutantStandardValue(mnAndOutputId.get(DataGatherCode).toString(), Arrays.asList(PollutantCode), pointtype);
            map.put("standardvalue", codeAndStandardValue.get(PollutantCode));
            map.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
            map.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel") != null ? document.get("AlarmLevel").toString() : null));
            tablelistdata.add(map);
        }
        return tablelistdata;
    }


    private Map<String, Object> getOutPutAndMNDataByPointType(Integer pointtype) {
        List<String> mns = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> tempMap = new HashMap<>();
        Map<String, Object> mnAndOutputName = new HashMap<>();
        Map<String, Object> mnAndOutputId = new HashMap<>();
        Map<String, Object> mnAndShortername = new HashMap<>();
        String tempString = "";
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointtype)) {
            case WasteWaterEnum://废水
                tempMap.put("outputtype", "water");
                dataList = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(tempMap);
                break;
            case WasteGasEnum: //废气
                dataList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(tempMap);
                break;
            case RainEnum: //雨水
                tempMap.put("outputtype", "rain");
                dataList = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(tempMap);
                break;
            case AirEnum: //大气
                dataList = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(tempMap);
                break;
            case EnvironmentalStinkEnum: //恶臭
                tempMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                dataList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(tempMap);
                break;
            case EnvironmentalVocEnum: //voc
                tempMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                dataList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(tempMap);
                break;
            case FactoryBoundaryStinkEnum: //厂界恶臭
                tempMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                dataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(tempMap);
                break;
            case FactoryBoundarySmallStationEnum: //厂界小型站
                tempMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                dataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(tempMap);
                break;
        }
        for (Map<String, Object> outPut : dataList) {
            if (outPut.get("dgimn") != null) {
                tempString = outPut.get("dgimn").toString();
                mns.add(tempString);
                if (CommonTypeEnum.getOutPutTypeList().contains(pointtype)) {
                    mnAndOutputName.put(tempString, outPut.get("outputname"));
                    mnAndShortername.put(tempString, outPut.get("shortername"));
                } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(pointtype)) {
                    mnAndOutputName.put(tempString, outPut.get("monitorpointname"));
                    mnAndShortername.put(tempString, outPut.get("shortername"));
                } else {
                    mnAndOutputName.put(tempString, outPut.get("monitorpointname"));
                }
                mnAndOutputId.put(tempString, outPut.get("pk_id"));
            }
        }
        tempMap.clear();
        tempMap.put("mnset", mns);
        tempMap.put("mnandoutputname", mnAndOutputName);
        tempMap.put("mnandoutputid", mnAndOutputId);
        tempMap.put("mnandshortername", mnAndShortername);
        return tempMap;
    }

    /**
     * @author: lip
     * @date: 2019/6/22 0022 下午 5:19
     * @Description: 统计各监测点类型的预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countEarlyDataByParamMap(Map<String, Object> paramMap, Map<String, Integer> mnAndType) {
        Map<String, Integer> earlyData = new HashMap<>();
        earlyData.put("waternum", 0);
        earlyData.put("gasnum", 0);
        earlyData.put("smokenum", 0);
        earlyData.put("rainnum", 0);
        earlyData.put("airnum", 0);
        earlyData.put("stinknum", 0);
        earlyData.put("vocnum", 0);
        earlyData.put("entstinknum", 0);
        earlyData.put("entsmallstationnum", 0);
        earlyData.put("waterstationnum", 0);
        earlyData.put("totalnum", 0);
        List<AggregationOperation> operations = new ArrayList<>();

        List<String> mns = (List<String>) paramMap.get("mns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        operations.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("EarlyWarnTime").gte(startDate).lte(endDate)
                )
        );
        GroupOperation group = group("DataGatherCode").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, earlyWarnData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        if (documents.size() > 0) {
            sumCountData(mnAndType, documents, earlyData);
        }
        return earlyData;
    }

    /**
     * @author: lip
     * @date: 2019/10/30 0030 下午 5:19
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sumCountData(Map<String, Integer> mnAndType, List<Document> documents, Map<String, Integer> earlyData) {
        String tempCode;
        Integer type;
        for (Document document : documents) {
            tempCode = document.getString("_id");
            earlyData.put("totalnum", earlyData.get("totalnum") + 1);
            type = mnAndType.get(tempCode);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                //废水
                case WasteWaterEnum:
                    earlyData.put("waternum", earlyData.get("waternum") + 1);
                    break;
                //废气
                case WasteGasEnum:
                    earlyData.put("gasnum", earlyData.get("gasnum") + 1);
                    break;
                //烟气
                case SmokeEnum:
                    earlyData.put("smokenum", earlyData.get("smokenum") + 1);
                    break;
                //雨水
                case RainEnum:
                    earlyData.put("rainnum", earlyData.get("rainnum") + 1);
                    break;
                //空气
                case AirEnum:
                    earlyData.put("airnum", earlyData.get("airnum") + 1);
                    break;
                //voc
                case EnvironmentalVocEnum:
                    earlyData.put("vocnum", earlyData.get("vocnum") + 1);
                    break;
                //恶臭
                case EnvironmentalStinkEnum:
                    earlyData.put("stinknum", earlyData.get("stinknum") + 1);
                    break;
                case FactoryBoundaryStinkEnum:
                    earlyData.put("entstinknum", earlyData.get("entstinknum") + 1);

                    break;
                case FactoryBoundarySmallStationEnum:
                    earlyData.put("entsmallstationnum", earlyData.get("entsmallstationnum") + 1);
                    break;
                case WaterQualityEnum://水质
                    earlyData.put("waterstationnum", earlyData.get("waterstationnum") + 1);
                    break;
            }
        }
    }


    /**
     * @author: xsm
     * @date: 2019/09/06 0006 上午 8:58
     * @Description: 统计各监测点类型的浓度突变或排放量突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countConcentrationChangeOrFlowChangeDataByParamMap(Map<String, Object> paramMap, Map<String, Integer> mnAndType, Map<String, Integer> earlyData, Integer alarmtype) {
        //查询条件
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns);
        if (alarmtype == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {//浓度突变
            criteria.and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
        } else if (alarmtype == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {// 排放量突变
            criteria.and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
        }
        List<Document> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria),
                project("DataGatherCode"),
                group("DataGatherCode").count().as("num")),
                hourCollection,
                Document.class).getMappedResults();
        if (mappedResults != null && mappedResults.size() > 0) {
            sumCountData(mnAndType, mappedResults, earlyData);
        }
        return earlyData;
    }


    /**
     * @author: xsm
     * @date: 2019/09/06 0006 上午 8:48
     * @Description: 统计各监测点类型的异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countExceptionDataByParamMap(Map<String, Object> paramMap, Map<String, Integer> mnAndType) {
        Map<String, Integer> exceptionData = new HashMap<>();
        exceptionData.put("waternum", 0);
        exceptionData.put("gasnum", 0);
        exceptionData.put("smokenum", 0);
        exceptionData.put("rainnum", 0);
        exceptionData.put("airnum", 0);
        exceptionData.put("stinknum", 0);
        exceptionData.put("vocnum", 0);
        exceptionData.put("entstinknum", 0);
        exceptionData.put("entsmallstationnum", 0);
        exceptionData.put("waterstationnum", 0);
        exceptionData.put("totalnum", 0);
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        List<String> mns = (List<String>) paramMap.get("mns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        operations.add(
                Aggregation.match(//剔除掉无流量异常数据 分离出来单独查询
                        Criteria.where("DataGatherCode").in(mns).and("ExceptionTime").gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode())
                )
        );
        GroupOperation group = group("DataGatherCode").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, exceptionData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        sumCountData(mnAndType, documents, exceptionData);
        return exceptionData;
    }


    private Map<String, Integer> countOverDataByParamMap(Map<String, Object> paramMap, Map<String, Integer> mnAndType) {
        Map<String, Integer> earlyData = new HashMap<>();
        earlyData.put("waternum", 0);
        earlyData.put("gasnum", 0);
        earlyData.put("smokenum", 0);
        earlyData.put("rainnum", 0);
        earlyData.put("airnum", 0);
        earlyData.put("stinknum", 0);
        earlyData.put("vocnum", 0);
        earlyData.put("entstinknum", 0);
        earlyData.put("entsmallstationnum", 0);
        earlyData.put("waterstationnum", 0);
        earlyData.put("totalnum", 0);
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        List<String> mns = (List<String>) paramMap.get("mns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        operations.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("OverTime").gte(startDate).lte(endDate)
                )
        );
        if (paramMap.get("mns") != null) {
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        }
        GroupOperation group = group("DataGatherCode").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, overData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        sumCountData(mnAndType, documents, earlyData);
        return earlyData;
    }


    //获取排口下污染物标准值
    private Map<String, Object> getPollutantStandardValue(String outputid, List<String> pollutantcodes, Integer pointtype) {
        Map<String, Object> codeAndStandardValue = new HashMap<>();
        List<Map<String, Object>> tempList = new ArrayList<>();

        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("monitorpointtype", pointtype);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointtype)) {
            //废水
            case WasteWaterEnum:
                tempMap.put("outputid", outputid);
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("datamark", "1");
                tempList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(tempMap);
                break;
            //废气
            case WasteGasEnum:
                tempMap.put("outputid", outputid);
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("unorgflag", false);
                tempList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(tempMap);
                break;
            //雨水
            case RainEnum:
                tempMap.put("outputid", outputid);
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("datamark", "3");
                tempList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(tempMap);
                break;
            //空气
            case AirEnum:
                tempMap.put("outputids", Arrays.asList(outputid));
                tempMap.put("pollutantcodes", pollutantcodes);
                tempList = pollutantFactorMapper.getAirPollutantsByParamMap(tempMap);
                break;
            case WaterQualityEnum:
                tempMap.put("outputids", Arrays.asList(outputid));
                tempMap.put("pollutantcodes", pollutantcodes);
                tempList = pollutantFactorMapper.getWaterQualityPollutantsByParamMap(tempMap);
                break;
            //voc
            case EnvironmentalVocEnum:
                tempMap.put("outputids", Arrays.asList(outputid));
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("pollutantType", pointtype);
                tempList = pollutantFactorMapper.getOtherPollutantsByParamMap(tempMap);
                break;
            //恶臭
            case EnvironmentalStinkEnum:
                tempMap.put("outputids", Arrays.asList(outputid));
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("pollutantType", pointtype);
                tempList = pollutantFactorMapper.getOtherPollutantsByParamMap(tempMap);
                break;
            case FactoryBoundaryStinkEnum:
                tempMap.put("outputid", outputid);
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("unorgflag", true);
                tempMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                tempList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(tempMap);
                break;
            case FactoryBoundarySmallStationEnum:
                tempMap.put("outputid", outputid);
                tempMap.put("pollutantcodes", pollutantcodes);
                tempMap.put("unorgflag", true);
                tempMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                tempList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(tempMap);
                break;
        }
        String standardValue = "";
        for (Map<String, Object> map : tempList) {
            if (map.get("pollutantcode") != null) {
                standardValue = formatStandardValue(map.get("standardminvalue"), map.get("standardmaxvalue"));
                codeAndStandardValue.put(map.get("pollutantcode").toString(), standardValue);
            } else {
                standardValue = formatStandardValue(map.get("StandardMinValue"), map.get("StandardMaxValue"));
                codeAndStandardValue.put(map.get("Code").toString(), standardValue);
            }
        }
        return codeAndStandardValue;
    }

    private String formatStandardValue(Object standardminvalue, Object standardmaxvalue) {
        String standardValue = "";
        if (standardminvalue != null && !"".equals(standardminvalue) && standardmaxvalue != null && !"".equals(standardmaxvalue)) {
            standardValue = standardminvalue + "-" + standardmaxvalue;
        } else {
            if (standardminvalue != null && !"".equals(standardminvalue)) {
                standardValue = ">" + standardminvalue;
            } else if (standardmaxvalue != null && !"".equals(standardmaxvalue)) {
                standardValue = "<" + standardmaxvalue;
            }
        }
        return standardValue;
    }


    //获取预警表格数据
    private List<Map<String, Object>> getEarlyTableListData(Map<String, Object> codeAndName, List<Map<String, Object>> codeAndStandardValue, Map<String, Object> codeAndLevel, Map<String, Object> paramMap) {
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        List<String> mnlist = (List<String>) paramMap.get("mns");
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        List<String> datatype = (List<String>) paramMap.get("datatype");
        int monitorpointtype = Integer.parseInt(paramMap.get("pointtype").toString());
        Date startDate;
        Date endDate;
        if (starttime.length() == 13 && endtime.length() == 13) {   //小时
            startDate = DataFormatUtil.parseDate(starttime + ":00:00");
            endDate = DataFormatUtil.parseDate(endtime + ":59:59");
        } else {        //天
            startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        operations.add(Aggregation.match(Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate)));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        List<String> datatypelist = new ArrayList<>();
        if (datatype.size() > 0) {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                for (String str : datatype) {
                    if ("1".equals(str)) {
                        datatypelist.add("HourData");
                    } else if ("2".equals(str)) {
                        datatypelist.add("DayData");
                    }
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            } else {
                for (String str : datatype) {
                    datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        } else {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                datatypelist.add("HourData");
                datatypelist.add("DayData");
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        }
        //总条数
        long totalCount = mongoTemplate.count(query, earlyWarnData_db);
        pageEntity.setTotalCount(totalCount);
        //排序条件
        String orderBy = "DataGatherCode,EarlyWarnTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "EarlyWarnTime", "DataType", "AlarmType", "AlarmLevel", "MonitorValue"));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, earlyWarnData_db, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();

        paramMap.put("total", pageEntity.getTotalCount());
        paramMap.put("pages", pageEntity.getPageCount());
        String PollutantCode = "";
        for (Document document : documents) {
            Map<String, Object> map = new HashMap<>();
            PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
            map.put("pollutantname", codeAndName.get(PollutantCode));
            map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("EarlyWarnTime")));
            map.put("monitorvalue", document.get("MonitorValue"));
            map.put("concenalarmmaxvalue", getAlarmLevelValue(document.getString("DataGatherCode"), PollutantCode, document.get("AlarmLevel"), codeAndStandardValue));
            map.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
            //map.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel") != null ? document.get("AlarmLevel").toString() : null));
            if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                map.put("datatypecode", "1");
                map.put("datatype", RealTimeDataEnum.getName());
                map.put("datatypename", "实时数据");
                map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("EarlyWarnTime")));
            } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                map.put("datatypecode", "2");
                map.put("datatype", MinuteDataEnum.getName());
                map.put("datatypename", "分钟数据");
                map.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("EarlyWarnTime")));
            } else if ("HourData".equals(document.getString("DataType"))) {//小时
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                    map.put("datatypecode", "1");
                    map.put("datatype", HourDataEnum.getName());
                    map.put("datatypename", "小时数据");
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("EarlyWarnTime")));
                } else {
                    map.put("datatypecode", "3");
                    map.put("datatype", HourDataEnum.getName());
                    map.put("datatypename", "小时数据");
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("EarlyWarnTime")));
                }
            } else if ("DayData".equals(document.getString("DataType"))) {//日
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                    map.put("datatypecode", "2");
                    map.put("datatype", DayDataEnum.getName());
                    map.put("datatypename", "日数据");
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("EarlyWarnTime")));
                } else {
                    map.put("datatypecode", "4");
                    map.put("datatype", DayDataEnum.getName());
                    map.put("datatypename", "日数据");
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("EarlyWarnTime")));
                }
            }
            tablelistdata.add(map);
        }
        return tablelistdata;


    }

    //获取浓度突变表格数据

    /**
     * @author: xsm
     * @date: 2021/09/23 0023 下午 3:17
     * @Description: 获取浓度突变数据（改成查分钟突变数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getHourDataListData(Map<String, Object> codeAndName, Map<String, Object> paramMap) {
        String collection = paramMap.get("collection").toString();
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        List<String> mnlist = (List<String>) paramMap.get("mns");
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        Date startDate;
        Date endDate;
        if (starttime.length() == 13 && endtime.length() == 13) {   //小时
            startDate = DataFormatUtil.parseDate(starttime + ":00:00");
            endDate = DataFormatUtil.parseDate(endtime + ":59:59");
        } else {        //天
            startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
        operations.add(Aggregation.unwind("MinuteDataList"));
        operations.add(Aggregation.match(Criteria.where("MinuteDataList.IsSuddenChange").is(true)));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            operations.add(Aggregation.match(Criteria.where("MinuteDataList.PollutantCode").in(pollutantcodes)));
        }

        String orderBy = "DataGatherCode,MonitorTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, collection, Document.class);

        //查询总条数
        operations.add(Aggregation.count().as("total"));
        operations.add(Aggregation.project("total"));
        List<Map> results = mongoTemplate.aggregate(newAggregation(operations), collection, Map.class).getMappedResults();
        if (results.size() > 0) {
            paramMap.put("total", results.get(0).get("total"));
        }
        List<Document> documents = resultdocument.getMappedResults();
        String PollutantCode = "";
        for (Document datas : documents) {
            Document document = (Document) datas.get("MinuteDataList");
            for (String pollutantcode : pollutantcodes) {
                Map<String, Object> map = new HashMap<>();
                PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
                if (pollutantcode.equals(PollutantCode)) {
                    map.put("pollutantname", codeAndName.get(PollutantCode));
                    map.put("monitortime", DataFormatUtil.getDateYMDHMS(datas.getDate("MonitorTime")));
                    map.put("monitorvalue", document.get("AvgStrength"));
                    if (document.get("ChangeMultiple") != null) {
                        map.put("ChangeMultiple", decimalFormat.format(Double.valueOf(document.get("ChangeMultiple").toString()) * 100) + "%");
                    } else {
                        map.put("ChangeMultiple", "");
                    }
                }
                tablelistdata.add(map);
            }
        }
        return tablelistdata;
    }

    //获取排放量突变表格数据
    private List<Map<String, Object>> getHourFlowDataListData(Map<String, Object> codeAndName, Map<String, Object> paramMap) {
        String collection = paramMap.get("collection").toString();
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        List<String> mnlist = (List<String>) paramMap.get("mns");
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        Date startDate;
        Date endDate;
        if (starttime.length() == 13 && endtime.length() == 13) {   //小时
            startDate = DataFormatUtil.parseDate(starttime + ":00:00");
            endDate = DataFormatUtil.parseDate(endtime + ":59:59");
        } else {        //天
            startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
        operations.add(Aggregation.unwind("HourFlowDataList"));
        operations.add(Aggregation.match(Criteria.where("HourFlowDataList.IsSuddenChange").is(true)));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            operations.add(Aggregation.match(Criteria.where("HourFlowDataList.PollutantCode").in(pollutantcodes)));
        }

        String orderBy = "DataGatherCode,MonitorTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, collection, Document.class);

        //查询总条数
        operations.add(Aggregation.count().as("total"));
        operations.add(Aggregation.project("total"));
        List<Map> results = mongoTemplate.aggregate(newAggregation(operations), collection, Map.class).getMappedResults();
        if (results.size() > 0) {
            paramMap.put("total", results.get(0).get("total"));
        }
        List<Document> documents = resultdocument.getMappedResults();
        String PollutantCode = "";
        for (Document datas : documents) {
            Document document = (Document) datas.get("HourFlowDataList");
            for (String pollutantcode : pollutantcodes) {
                Map<String, Object> map = new HashMap<>();
                PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
                if (pollutantcode.equals(PollutantCode)) {
                    map.put("pollutantname", codeAndName.get(PollutantCode));
                    map.put("monitortime", DataFormatUtil.getDateYMDHMS(datas.getDate("MonitorTime")));
                    map.put("monitorvalue", document.get("AvgStrength"));
                    if (document.get("ChangeMultiple") != null) {
                        map.put("ChangeMultiple", decimalFormat.format(Double.valueOf(document.get("ChangeMultiple").toString()) * 100) + "%");
                    } else {
                        map.put("ChangeMultiple", "");
                    }
                }
                tablelistdata.add(map);
            }
        }
        return tablelistdata;
    }


    //获取超限表格数据
    private List<Map<String, Object>> getOverTableListData(Map<String, Object> codeAndName, List<Map<String, Object>> codeAndStandardValue, Map<String, Object> codeAndLevel, Map<String, Object> paramMap) {
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        List<String> mnlist = (List<String>) paramMap.get("mns");
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        List<String> datatype = (List<String>) paramMap.get("datatype");
        int monitorpointtype = Integer.parseInt(paramMap.get("pointtype").toString());
        Date startDate;
        Date endDate;
        if (starttime.length() == 13 && endtime.length() == 13) {   //小时
            startDate = DataFormatUtil.parseDate(starttime + ":00:00");
            endDate = DataFormatUtil.parseDate(endtime + ":59:59");
        } else {        //天
            startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        operations.add(Aggregation.match(Criteria.where("OverTime").gte(startDate).lte(endDate)));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        List<String> datatypelist = new ArrayList<>();
        if (datatype.size() > 0) {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                for (String str : datatype) {
                    if ("1".equals(str)) {
                        datatypelist.add("HourData");
                    } else if ("2".equals(str)) {
                        datatypelist.add("DayData");
                    }
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            } else {
                for (String str : datatype) {
                    datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        } else {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                datatypelist.add("HourData");
                datatypelist.add("DayData");
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        }
        //总条数
        long totalCount = mongoTemplate.count(query, overData_db);
        pageEntity.setTotalCount(totalCount);
        String orderBy = "DataGatherCode,OverTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "OverTime", "AlarmType", "AlarmLevel", "DataType", "MonitorValue", "IsOverStandard", "OverMultiple"));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, overData_db, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        paramMap.put("total", pageEntity.getTotalCount());
        paramMap.put("pages", pageEntity.getPageCount());
        String PollutantCode = "";
        for (Document document : documents) {
            String mn = document.getString("DataGatherCode");
            Map<String, Object> map = new HashMap<>();
            PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
            map.put("pollutantname", codeAndName.get(PollutantCode));
            map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
            map.put("monitorvalue", document.get("MonitorValue"));
            Object standardmaxvalue = null;
            for (Map<String, Object> objmap : codeAndStandardValue) {
                if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                    if (objmap.get("Code") != null && PollutantCode.equals(objmap.get("Code").toString())) {
                        standardmaxvalue = objmap.get("StandardMaxValue");
                        break;
                    }
                }
            }
            map.put("standardmaxvalue", standardmaxvalue);//标准值
            if (document.get("OverMultiple") != null && !"".equals(document.get("OverMultiple").toString())) {//超标倍数
                map.put("overmultiple", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo((double) (document.get("OverMultiple")) * 100)) + "%");
            } else {
                map.put("overmultiple", "");
            }
            map.put("isoverstandard", document.getBoolean("IsOverStandard"));//是否超标
            if (document.getBoolean("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {//判断是否超标报警
                //判断是否即超标又超限
                if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                    map.put("alarmlevel", (codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "、" : "") + "超标报警");
                    map.put("alarmlevelvalue", getAlarmLevelValue(document.getString("DataGatherCode"), PollutantCode, document.get("AlarmLevel"), codeAndStandardValue));
                } else {
                    map.put("alarmlevel", "超标报警");
                    map.put("alarmlevelvalue", "");
                }

            } else {
                if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                    map.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) : "");
                    map.put("alarmlevelvalue", getAlarmLevelValue(document.getString("DataGatherCode"), PollutantCode, document.get("AlarmLevel"), codeAndStandardValue));
                }
            }
            if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                map.put("datatypecode", "1");
                map.put("datatype", RealTimeDataEnum.getName());
                map.put("datatypename", "实时数据");
                map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
            } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                map.put("datatypecode", "2");
                map.put("datatype", MinuteDataEnum.getName());
                map.put("datatypename", "分钟数据");
                map.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("OverTime")));
            } else if ("HourData".equals(document.getString("DataType"))) {//小时
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//当为空气小时数据
                    map.put("datatypecode", "1");
                    map.put("datatype", HourDataEnum.getName());
                    map.put("datatypename", "小时数据");
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("OverTime")));
                } else {
                    map.put("datatypecode", "3");
                    map.put("datatype", HourDataEnum.getName());
                    map.put("datatypename", "小时数据");
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("OverTime")));
                }
            } else if ("DayData".equals(document.getString("DataType"))) {//日
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//当为空气日数据
                    map.put("datatypecode", "2");
                    map.put("datatype", DayDataEnum.getName());
                    map.put("datatypename", "日数据");
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("OverTime")));
                } else {
                    map.put("datatypecode", "4");
                    map.put("datatype", DayDataEnum.getName());
                    map.put("datatypename", "日数据");
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("OverTime")));
                }
            }
            tablelistdata.add(map);
        }
        return tablelistdata;


    }


    //获取异常表格数据
    private List<Map<String, Object>> getExceptionTableListData(Map<String, Object> codeAndName, List<Map<String, Object>> codeAndStandardValue, Map<String, Object> paramMap) {
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        List<String> mnlist = (List<String>) paramMap.get("mns");
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        List<String> datatype = (List<String>) paramMap.get("datatype");
        int monitorpointtype = Integer.parseInt(paramMap.get("pointtype").toString());
        Date startDate;
        Date endDate;
        if (starttime.length() == 13 && endtime.length() == 13) {   //小时
            startDate = DataFormatUtil.parseDate(starttime + ":00:00");
            endDate = DataFormatUtil.parseDate(endtime + ":59:59");
        } else {        //天
            startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("ExceptionTime").gte(startDate).lte(endDate));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        operations.add(Aggregation.match(Criteria.where("ExceptionTime").gte(startDate).lte(endDate)));
        if (pollutantcodes != null && pollutantcodes.size() > 0) {
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        List<String> datatypelist = new ArrayList<>();
        if (datatype.size() > 0) {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                for (String str : datatype) {
                    if ("1".equals(str)) {
                        datatypelist.add("HourData");
                    } else if ("2".equals(str)) {
                        datatypelist.add("DayData");
                    }
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            } else {
                for (String str : datatype) {
                    datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        } else {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                datatypelist.add("HourData");
                datatypelist.add("DayData");
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        }
        //总条数
        long totalCount = mongoTemplate.count(query, exceptionData_db);
        pageEntity.setTotalCount(totalCount);

        String orderBy = "DataGatherCode,ExceptionTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "ExceptionTime", "DataType", "ExceptionType", "MonitorValue"));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, exceptionData_db, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        paramMap.put("total", pageEntity.getTotalCount());
        paramMap.put("pages", pageEntity.getPageCount());
        String PollutantCode = "";
        for (Document document : documents) {
            String exceptionvalue = "";
            for (Map<String, Object> objmap : codeAndStandardValue) {
                if (objmap.get("DGIMN") != null && (document.getString("DataGatherCode")).equals(objmap.get("DGIMN").toString())) {//当MN号相同时
                    if (objmap.get("Code") != null && (document.getString("PollutantCode")).equals(objmap.get("Code").toString())) {
                        if (objmap.get("ExceptionMinValue") != null && !"".equals(objmap.get("ExceptionMinValue").toString())) {
                            exceptionvalue = exceptionvalue + "<" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMinValue").toString());
                        }
                        if (objmap.get("ExceptionMaxValue") != null && !"".equals(objmap.get("ExceptionMaxValue").toString())) {
                            if (!"".equals(exceptionvalue)) {
                                exceptionvalue = exceptionvalue + "或>" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                            } else {
                                exceptionvalue = exceptionvalue + ">" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                            }
                        }
                        break;
                    }
                }
            }
            Map<String, Object> map = new HashMap<>();
            PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
            map.put("pollutantname", codeAndName.get(PollutantCode));
            map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
            map.put("monitorvalue", document.get("MonitorValue"));
            map.put("exceptiontype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType")));
            if ((document.getString("ExceptionType")).equals(CommonTypeEnum.ExceptionTypeEnum.OverExceptionEnum.getCode())) {
                map.put("exceptionvalue", exceptionvalue);
            } else {
                map.put("exceptionvalue", "");
            }
            if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                map.put("datatypecode", "1");
                map.put("datatype", RealTimeDataEnum.getName());
                map.put("datatypename", "实时数据");
                map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
            } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                map.put("datatypecode", "2");
                map.put("datatype", MinuteDataEnum.getName());
                map.put("datatypename", "分钟数据");
                map.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("ExceptionTime")));
            } else if ("HourData".equals(document.getString("DataType"))) {//小时
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//当为空气小时数据
                    map.put("datatypecode", "1");
                    map.put("datatype", HourDataEnum.getName());
                    map.put("datatypename", "小时数据");
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("ExceptionTime")));
                } else {
                    map.put("datatypecode", "3");
                    map.put("datatype", HourDataEnum.getName());
                    map.put("datatypename", "小时数据");
                    map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("ExceptionTime")));
                }
            } else if ("DayData".equals(document.getString("DataType"))) {//日
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//当为空气日数据
                    map.put("datatypecode", "2");
                    map.put("datatype", DayDataEnum.getName());
                    map.put("datatypename", "日数据");
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("ExceptionTime")));
                } else {
                    map.put("datatypecode", "4");
                    map.put("datatype", DayDataEnum.getName());
                    map.put("datatypename", "日数据");
                    map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("ExceptionTime")));
                }
            }
            tablelistdata.add(map);
        }
        return tablelistdata;
    }


    //计算超标倍数
    private Object getOvermultiple(Object standardValue, Object monitorValue) {
        String overMultiple = "";
        if (standardValue != null && !"".equals(standardValue) && monitorValue != null && !"".equals(monitorValue)) {
            String standard = standardValue.toString();
            Double value = Double.parseDouble(monitorValue.toString());
            if (standard.indexOf("-") >= 0) {//区间值
                Double min = Double.parseDouble(standard.split("-")[0]);
                Double max = Double.parseDouble(standard.split("-")[1]);
                if (value < min) {
                    overMultiple = DataFormatUtil.formatDoubleSaveTwo((min - value) / min);
                } else if (value > max) {
                    overMultiple = DataFormatUtil.formatDoubleSaveTwo((value - max) / max);
                }
            } else if (standard.indexOf(">") >= 0) {//下限值
                Double min = Double.parseDouble(standard.replace(">", ""));
                if (value < min) {
                    overMultiple = DataFormatUtil.formatDoubleSaveTwo((min - value) / min);
                }
            } else if (standard.indexOf("<") >= 0) {//上限限值
                Double max = Double.parseDouble(standard.replace("<", ""));
                if (value > max) {
                    overMultiple = DataFormatUtil.formatDoubleSaveTwo((value - max) / max);
                }
            }
        }
        return overMultiple;
    }

    /**
     * @author: zhangzc
     * @date: 2019/6/11 14:16
     * @Description: 获取超标or报警 or 异常次数
     * @param:
     * @return:
     */
    private Object getNumForOverOrAlarmOrException(List<Document> documents, String mn, String pollutantCode) {
        for (Document document : documents) {
            String dataGatherCode = document.get("DataGatherCode").toString();
            String PollutantCode = document.get("PollutantCode") != null ? document.getString("PollutantCode") : "";
            if (mn.equals(dataGatherCode) && PollutantCode.equals(pollutantCode)) {
                return document.get("num");
            }
        }
        return null;
    }

    /**
     * @author: zhangzc
     * @date: 2019/6/11 13:53
     * @Description: 构建表头Map
     * @param:
     * @return:
     */
    private Map<String, Object> getSingleTitle(String propValue, String labelValue, String minWidth) {

        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(minWidth)) {
            map.put("minwidth", minWidth);
        }
        map.put("headeralign", "center");
        map.put("showhide", true);
        map.put("prop", propValue);
        map.put("label", labelValue);
        map.put("align", "center");
        return map;
    }


    private PageEntity<Document> setEarlyWarnPageData(Map<String, Object> paramMap, String collection) {

        boolean isPage = true;
        int pageNum = 1;
        int pageSize = 100000;
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
        //查询条件
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            operations.add(Aggregation.match(Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate)));
        } else {
            if (paramMap.get("starttime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                operations.add(Aggregation.match(Criteria.where("EarlyWarnTime").gte(startDate)));
            }
            if (paramMap.get("endtime") != null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("EarlyWarnTime").lte(endDate)));
            }
        }
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        if (paramMap.get("datatype") != null) {
            String datatype = paramMap.get("datatype").toString();
            operations.add(Aggregation.match(Criteria.where("DataType").is(datatype)));
        }
        //分组条件
        String groupBy = "DataGatherCode,PollutantCode,EarlyWarnTime,DataType,AlarmType,AlarmLevel,MonitorValue";
        if (paramMap.get("groupBy") != null) {
            groupBy = paramMap.get("groupBy").toString();
        }
        //GroupOperation groupOperation = Aggregation.group(groupBy.split(",")).count().as("groupLists");
        //operations.add(groupOperation);

        //排序条件
        String orderBy = "DataGatherCode,EarlyWarnTime";
        if (paramMap.get("orderBy") != null) {
            orderBy = paramMap.get("orderBy").toString();
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if (paramMap.get("direction") != null && "asc".equals(paramMap.get("direction"))) {
            direction = Sort.Direction.ASC;
        }
        if (isPage) {
            long totalCount = 0;
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);

            totalCount = resultsCount.getMappedResults().size();
            pageEntity.setTotalCount(totalCount);
            int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
            pageEntity.setPageCount(pageCount);

            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }


        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project(groupBy.split(",")));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;
    }

    private PageEntity<Document> setOverPageData(Map<String, Object> paramMap, String collection) {
        int pageNum = 1;
        int pageSize = 1000000;
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
        //查询条件
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            operations.add(Aggregation.match(Criteria.where("OverTime").gte(startDate).lte(endDate)));
        } else {
            if (paramMap.get("starttime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                operations.add(Aggregation.match(Criteria.where("OverTime").gte(startDate)));
            }
            if (paramMap.get("endtime") != null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("OverTime").lte(endDate)));
            }
        }
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        if (paramMap.get("datatype") != null) {
            String datatype = paramMap.get("datatype").toString();
            operations.add(Aggregation.match(Criteria.where("DataType").is(datatype)));
        }
        //分组条件
        String groupBy = "DataGatherCode,PollutantCode,OverTime,DataType,AlarmType,AlarmLevel,MonitorValue";
        if (paramMap.get("groupBy") != null) {
            groupBy = paramMap.get("groupBy").toString();
        }
        GroupOperation groupOperation = Aggregation.group(groupBy.split(",")).count().as("groupLists");
        //operations.add(groupOperation);

        //排序条件
        String orderBy = "DataGatherCode,OverTime";
        if (paramMap.get("orderBy") != null) {
            orderBy = paramMap.get("orderBy").toString();
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if (paramMap.get("direction") != null && "asc".equals(paramMap.get("direction"))) {
            direction = Sort.Direction.ASC;
        }
        if (isPage) {
            long totalCount = 0;
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
            totalCount = resultsCount.getMappedResults().size();
            pageEntity.setTotalCount(totalCount);
            int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
            pageEntity.setPageCount(pageCount);
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }

        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project(groupBy.split(",")));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;
    }

    private PageEntity<Document> setExceptionPageData(Map<String, Object> paramMap, String collection) {
        int pageNum = 1;
        int pageSize = 1000000;
        if (paramMap.get("pagenum") != null) {
            pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
        }
        if (paramMap.get("pagesize") != null) {
            pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        pageEntity.setPageNum(pageNum);
        pageEntity.setPageSize(pageSize);
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            operations.add(Aggregation.match(Criteria.where("ExceptionTime").gte(startDate).lte(endDate)));
        } else {
            if (paramMap.get("starttime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                operations.add(Aggregation.match(Criteria.where("ExceptionTime").gte(startDate)));
            }
            if (paramMap.get("endtime") != null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("ExceptionTime").lte(endDate)));
            }
        }
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantcodes)));
        }
        if (paramMap.get("datatype") != null) {
            String datatype = paramMap.get("datatype").toString();
            operations.add(Aggregation.match(Criteria.where("DataType").is(datatype)));
        }
        //分组条件
        String groupBy = "DataGatherCode,PollutantCode,ExceptionTime,DataType,ExceptionType,MonitorValue";
        if (paramMap.get("groupBy") != null) {
            groupBy = paramMap.get("groupBy").toString();
        }
        //GroupOperation groupOperation = Aggregation.group(groupBy.split(",")).count().as("groupLists");
        //operations.add(groupOperation);

        //排序条件
        String orderBy = "DataGatherCode,ExceptionTime";
        if (paramMap.get("orderBy") != null) {
            orderBy = paramMap.get("orderBy").toString();
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if (paramMap.get("direction") != null && "asc".equals(paramMap.get("direction"))) {
            direction = Sort.Direction.ASC;
        }
        long totalCount = 0;
        Aggregation aggregationCount = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
        totalCount = resultsCount.getMappedResults().size();
        pageEntity.setTotalCount(totalCount);
        int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
        pageEntity.setPageCount(pageCount);
        operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
        operations.add(Aggregation.limit(pageEntity.getPageSize()));

        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project(groupBy.split(",")));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:17
     * @Description: 通过排口数组获取mn号数组，设置mn号和排口id对照的map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: outputids：排口id数组，outPutIdAndMn：（outputid+mn）,monitorPointType：监测点类型
     * @return:
     */

    @Override
    public List<String> getMNsAndSetOutPutIdAndMnByOutPutIds(List<String> outputids, Integer monitorPointType,
                                                             Map<String, String> outPutIdAndMn) {
        List<String> mns = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (outputids != null) {
            String outputkey = "";
            List<Map<String, Object>> outPuts = new ArrayList<>();
            if (notothertypes.contains(monitorPointType)) {
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                    case WasteWaterEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("outputtype", "water");
                        outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                        break;
                    case WasteGasEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("monitorpointtype", monitorPointType);
                        outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                        break;
                    case SmokeEnum:
                        paramMap.put("monitorpointtype", monitorPointType);
                        paramMap.put("outputids", outputids);
                        outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                        break;
                    case RainEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("outputtype", "rain");
                        outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                        break;
                    case AirEnum:
                        if (outputids.size() > 0) {
                            paramMap.put("airids", outputids);
                        }
                        outputkey = "monitorpointname";
                        outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                        break;
                    case WaterQualityEnum:
                        if (outputids.size() > 0) {
                            paramMap.put("outputids", outputids);
                        }
                        outputkey = "monitorpointname";
                        outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                        break;
                    case FactoryBoundaryStinkEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("monitorpointtype", monitorPointType);
                        outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("monitorpointtype", monitorPointType);
                        outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;
                }
            } else {
                paramMap.put("monitorPointType", monitorPointType);
                paramMap.put("outputids", outputids);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
            }

            for (Map<String, Object> outPut : outPuts) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                    outPutIdAndMn.put(outPut.get("pk_id").toString(), outPut.get("dgimn").toString());
                    if (!outputkey.equals("")) {
                        outPutIdAndMn.put(outPut.get("dgimn").toString(), outPut.get(outputkey).toString());
                    }
                }
            }

        }
        return mns;
    }

    /**
     * @author: xsm
     * @date: 2020/12/24 0024 下午 5:04
     * @Description: 根据敏感点传输点标记类型获取恶臭点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @Override
    public List<String> getMNsAndSetStinkIdAndMnByCategorys(Map<String, String> outPutIdAndMn, List<Integer> monitorPointCategorys) {
        List<String> mns = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorPointCategorys", monitorPointCategorys);
        List<Map<String, Object>> outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        String outputkey = "";
        for (Map<String, Object> outPut : outPuts) {
            if (outPut.get("dgimn") != null) {
                mns.add(outPut.get("dgimn").toString());
                outPutIdAndMn.put(outPut.get("pk_id").toString(), outPut.get("dgimn").toString());
                if (!outputkey.equals("")) {
                    outPutIdAndMn.put(outPut.get("dgimn").toString(), outPut.get(outputkey).toString());
                }
            }
        }
        return mns;
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 上午 8:57
     * @Description: 自定义查询条件获取空气监测数据（小时，日）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getAirMonitorDataByParamMap(Map<String, Object> paramMap) {
        Query query = setNoAirGroupQuery(paramMap);
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    /**
     * @param paramMap
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:11
     * @Description: 大气监测数据表查询条件组装
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Query setNoAirGroupQuery(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("StationCode").in(mns));
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
            query.addCriteria(Criteria.where("DataList.PollutantCode").in(pollutantcodes));
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        }


        return query;
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 3:14
     * @Description: 自定义查询条件获取小时排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getHourFlowMonitorDataByParamMap(Map<String, Object> paramMap) {
        Query query = setFlowQuery(paramMap);
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 上午 8:39
     * @Description: 自定义查询条件获取日排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getDayFlowMonitorDataByParamMap(Map<String, Object> paramMap) {
        Query query = setFlowQuery(paramMap);
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 11:00
     * @Description: 获取重点监控污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getKeyPollutantMonitorDataByParamMap(List<Map<String, Object>> outPuts, int monitorPointTypeCode, Map<String, Object> paramMap) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> keyPollutants = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(monitorPointTypeCode);
        List<String> pollutantcodes = new ArrayList<>();
        if (keyPollutants != null && keyPollutants.size() > 0) {

            for (Map<String, Object> map : keyPollutants) {
                pollutantcodes.add(map.get("Code").toString());
            }
            paramMap.put("pollutantcodes", pollutantcodes);
        }
        if (outPuts.size() > 0) {
            List<Map<String, Object>> tablelistdata = new ArrayList<>();

            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndOutputName = new HashMap<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            String mn = "";
            for (Map<String, Object> map : outPuts) {
                mn = map.get("dgimn").toString();
                mns.add(mn);
                mnAndOutputName.put(mn, map.get("outputname"));
                mnAndPollutionName.put(mn, map.get("pollutionname"));
                paramMap.put("mns", mns);
            }
            if (paramMap.get("sortdata") != null) {
                Map<String, Object> sortdata = (Map<String, Object>) paramMap.get("sortdata");
                for (String key : sortdata.keySet()) {
                    if (key.equals("monitortime") && sortdata.get(key).equals("descending")) {
                        paramMap.put("sort", "desc");
                    } else if (key.equals("monitortime") && sortdata.get(key).equals("ascending")) {
                        paramMap.put("sort", "asc");
                    }
                }
            }
            String collection = paramMap.get("collection").toString();
            String pollutantDataKey = getPollutantDataKey(collection);
            String valueKey = getValueKey(collection);
            Query query = setNoGroupQuery(paramMap);
            List<Document> documents = mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("pollutionname", mnAndPollutionName.get(mn));
                map.put("outputname", mnAndOutputName.get(mn));
                map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
                for (Document pollutant : pollutantDataList) {
                    if (pollutant.get("PollutantCode") != null && pollutantcodes.contains(pollutant.get("PollutantCode"))) {
                        map.put(pollutant.getString("PollutantCode"), pollutant.get(valueKey));
                    }
                }
                tablelistdata.add(map);
            }

            Map<String, Object> pageMap = this.getPageMapForReport(paramMap);
            dataMap.put("total", pageMap.get("total"));
            dataMap.put("tablelistdata", tablelistdata);
        }
        return dataMap;
    }

    /**
     * @author: lip
     * @date: 2019/7/2 0002 下午 4:19
     * @Description: 自定义查询条件获取污染物突增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPollutantUpRushDataByParams(List<Map<String, Object>> outPuts, int monitorPointTypeCode, Map<String, Object> paramMap) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DecimalFormat format1 = new DecimalFormat("0.##");
        if (outPuts.size() > 0) {
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndOutputName = new HashMap<>();
            Map<String, Object> mnAndOutputId = new HashMap<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            setOutputMap(outPuts, mnAndOutputName, mnAndOutputId, mnAndPollutionName, mns, monitorPointTypeCode);
            String collection = paramMap.get("collection").toString();
            String pollutantDataKey = getPollutantDataKey(collection);
            String starttime = null;
            String endtime = null;
            if (paramMap.get("starttime") != null) {
                starttime = paramMap.get("starttime").toString() + " 00:00:00";
            }
            if (paramMap.get("endtime") != null) {
                endtime = paramMap.get("endtime").toString() + " 23:59:59";
            }
            paramMap.put("mns", mns);

            Criteria criteria = new Criteria();
            Criteria criteria1 = new Criteria();
            criteria.and("DataGatherCode").in(mns).and(pollutantDataKey + ".IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            criteria1.and(pollutantDataKey + ".IsSuddenChange").is(true);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(Aggregation.unwind(pollutantDataKey));
            operations.add(Aggregation.match(criteria1));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d %H:%M:%S")).as("MonitorTime")
                    .and(pollutantDataKey + ".PollutantCode").as("PollutantCode")
                    .and(pollutantDataKey + ".AvgStrength").as("AvgStrength")
                    .and(pollutantDataKey + ".ChangeMultiple").as("ChangeMultiple")
            );
            Integer total = mongoTemplate.aggregate(newAggregation(operations), collection, Map.class).getMappedResults().size();
            //插入分页、排序条件
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                Long pagenum = Long.valueOf(paramMap.get("pagenum").toString());
                Long pagesize = Long.valueOf(paramMap.get("pagesize").toString());
                operations.add(Aggregation.skip((pagenum - 1) * pagesize));
                operations.add(Aggregation.limit(pagesize));
            }
            //返回的数据
            List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(operations), collection, Map.class).getMappedResults();
            List<Map<String, Object>> pollutantList;
            List<String> pollutantCodes = new ArrayList<>();
            Map<String, Object> codeAndName = new HashMap<>();
            for (Map mappedResult : mappedResults) {
                String mnkey = mappedResult.get("DataGatherCode") == null ? "" : mappedResult.get("DataGatherCode").toString();
                String outputid = mnAndOutputId.get(mnkey).toString();
                pollutantList = getPollutantListByOutputIdAndMonitorPointType(outputid, monitorPointTypeCode);
                pollutantCodes.clear();
                for (Map<String, Object> pollutant : pollutantList) {
                    pollutantCodes.add(pollutant.get("pollutantcode").toString());
                    codeAndName.put(pollutant.get("pollutantcode").toString(), pollutant.get("pollutantname"));
                }
                Map<String, Object> dataMapTemp = new HashMap<>();
                if (mnAndPollutionName.size() > 0) {
                    dataMapTemp.put("pollutionname", mnAndPollutionName.get(mnkey));
                }
                dataMapTemp.put("dgimn", mnkey);
                dataMapTemp.put("outputname", mnAndOutputName.get(mnkey));
                dataMapTemp.put("outputid", mnAndOutputId.get(mnkey));
                dataMapTemp.put("pollutantname", codeAndName.get(mappedResult.get("PollutantCode")));
                dataMapTemp.put("pollutantcode", mappedResult.get("PollutantCode"));
                String monitorTime = DataFormatUtil.getBeforeByHourTimeHMS(-8,mappedResult.get("MonitorTime").toString());
                dataMapTemp.put("monitortime",  monitorTime);
                dataMapTemp.put("changetime", mappedResult.get("MonitorTime") == null ? "" :  monitorTime.substring(11, 13) + "时");
                dataMapTemp.put("uprushpercent", mappedResult.get("ChangeMultiple") == null ? "" : format1.format(Float.valueOf(mappedResult.get("ChangeMultiple").toString()) * 100) + "%");
                dataMapTemp.put("currenthour", mappedResult.get("AvgStrength"));
                tablelistdata.add(dataMapTemp);
            }
            dataMap.put("total", total);
            dataMap.put("tablelistdata", tablelistdata);
        }
        return dataMap;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 上午 9:46
     * @Description: 自定义查询条件查询废气相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getGasRelationListDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
        //获取废气mn数组、废气排口名称，企业名称对照关系
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
                    dataMap.put("relationstarttime", paramMap.get("relationstarttime"));
                    dataMap.put("relationendtime", paramMap.get("relationendtime"));
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


    /**
     * @author: lip
     * @date: 2019/7/5 0005 下午 2:43
     * @Description: getGasRelationChartDataByParamMap
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getGasRelationChartDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
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
            String collection = paramMap.get("collection").toString();
            String beforeTimeKey = "";
            Integer beforeTimeNum = Integer.parseInt(paramMap.get("beforetime").toString());
            for (String time : pointTimeAndValue.keySet()) {
                if (collection.indexOf("Hour") > -1) {//小时数据
                    beforeTimeKey = DataFormatUtil.getBeforeByHourTime(beforeTimeNum, time);
                } else if (collection.indexOf("Minute") > -1) {//分钟数据
                    beforeTimeKey = DataFormatUtil.getBeforeByMinuteTime(beforeTimeNum, time);
                }
                if (outputTimeAndValue.get(beforeTimeKey) != null) {
                    Map<String, Object> xMap = new LinkedHashMap<>();
                    Map<String, Object> yMap = new LinkedHashMap<>();
                    xMap.put("monitortime", time);
                    xMap.put("value", Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    yMap.put("monitortime", beforeTimeKey);
                    yMap.put("value", Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    yData.add(Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    xData.add(Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    xListData.add(xMap);
                    yListData.add(yMap);
                }
            }
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
        return resultMap;
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
        gasParam.put("starttime", paramMap.get("beforestarttime"));
        gasParam.put("endtime", paramMap.get("beforeendtime"));
        gasParam.put("mns", paramMap.get("mns"));
        gasParam.put("collection", collection);
        gasParam.put("sort", "asc");
        gasParam.put("pollutantcodes", Arrays.asList(paramMap.get("outputpollutant")));
        Query query = setNoGroupQuery(gasParam);
        List<Document> gasDocuments = mongoTemplate.find(query, Document.class, collection);
        if (gasDocuments.size() > 0) {
            String monitortime = "";
            String pollutantDataKey = getPollutantDataKey(collection);
            String valueKey = getValueKey(collection);
            Map<String, Object> timeAndValue;
            List<Document> pollutantData;
            String outputpollutant;
            for (Document gasDocument : gasDocuments) {
                mnKey = gasDocument.getString("DataGatherCode");
                monitortime = getMonitorTimeByCollection(collection, gasDocument.getDate("MonitorTime"));
                if (mnAndTimeAndValue.get(mnKey) != null) {
                    timeAndValue = mnAndTimeAndValue.get(mnKey);
                } else {
                    timeAndValue = new LinkedHashMap<>();
                }
                pollutantData = (List<Document>) gasDocument.get(pollutantDataKey);
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
            pointParam.put("mns", Arrays.asList(paramMap.get("monitorpointmn")));
            pointParam.put("collection", collection);
            pointParam.put("sort", "asc");
            pointParam.put("pollutantcodes", Arrays.asList(paramMap.get("monitorpointpollutant")));
            query = setNoGroupQuery(pointParam);
            List<Document> pointDocuments = mongoTemplate.find(query, Document.class, collection);
            if (pointDocuments.size() > 0) {
                outputpollutant = paramMap.get("monitorpointpollutant").toString();
                for (Document point : pointDocuments) {
                    monitortime = getMonitorTimeByCollection(collection, point.getDate("MonitorTime"));
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
        String collection = paramMap.get("collection").toString();
        String beforeTimeKey = "";
        Integer beforeTimeNum = Integer.parseInt(paramMap.get("beforetime").toString());
        for (String time : pointTimeAndValue.keySet()) {
            if (collection.indexOf("Hour") > -1) {//小时数据
                beforeTimeKey = DataFormatUtil.getBeforeByHourTime(beforeTimeNum, time);
            } else if (collection.indexOf("Minute") > -1) {//分钟数据
                beforeTimeKey = DataFormatUtil.getBeforeByMinuteTime(beforeTimeNum, time);
            }
            if (outputTimeAndValue.get(beforeTimeKey) != null) {
                xData.add(Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                yData.add(Double.parseDouble(pointTimeAndValue.get(time).toString()));
            }
        }
        Double relationpercent = DataFormatUtil.getRelationPercent(xData, yData);
        return relationpercent;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 上午 10:22
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getMonitorTimeByCollection(String collection, Date monitorTime) {
        String monitortime = "";
        if (collection.indexOf("Hour") > -1) {//小时数据
            monitortime = DataFormatUtil.getDateYMDH(monitorTime);
        } else if (collection.indexOf("Minute") > -1) {//分钟数据
            monitortime = DataFormatUtil.getDateYMDHM(monitorTime);
        } else if (collection.indexOf("Day") > -1) {//分钟数据
            monitortime = DataFormatUtil.getDateYMD(monitorTime);
        }
        return monitortime;
    }


    /**
     * @author: lip
     * @date: 2019/7/2 0002 下午 8:07
     * @Description: 设置排口关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setOutputMap(List<Map<String, Object>> outPuts, Map<String, Object> mnAndOutputName, Map<String, Object> mnAndOutputId, Map<String, Object> mnAndPollutionName, List<String> mns, int monitorPointTypeCode) {
        String mn = "";
        for (Map<String, Object> map : outPuts) {
            mn = map.get("dgimn").toString();
            mns.add(mn);
            if (CommonTypeEnum.getOutPutTypeList().contains(monitorPointTypeCode)) {
                mnAndOutputName.put(mn, map.get("outputname"));
                mnAndPollutionName.put(mn, map.get("pollutionname"));
            } else if (CommonTypeEnum.getMonitorPointTypeList().contains(monitorPointTypeCode)) {
                mnAndOutputName.put(mn, map.get("monitorpointname"));
            } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(monitorPointTypeCode)) {
                mnAndOutputName.put(mn, map.get("monitorpointname"));
                mnAndPollutionName.put(mn, map.get("pollutionname"));
            } else if (monitorPointTypeCode == StinkEnum.getCode()) {
                mnAndOutputName.put(mn, map.get("monitorpointname"));
                mnAndPollutionName.put(mn, map.get("pollutionname"));
            }
            mnAndOutputId.put(mn, map.get("pk_id"));
        }

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
     * @date: 2019/7/2 0002 下午 7:06
     * @Description: 根据监测点id和监测点类型获取设置污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPollutantListByOutputIdAndMonitorPointType(String outputid, int monitorPointTypeCode) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputid", outputid);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointTypeCode)) {
            case WasteWaterEnum:
                paramMap.put("datamark", 1);
                dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(paramMap);
                break;
            case WasteGasEnum:
                paramMap.put("unorgflag", false);
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(paramMap);
                break;
            case RainEnum:
                paramMap.put("datamark", 3);
                dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(paramMap);
                break;
            case AirEnum:
                dataList = airStationPollutantSetMapper.getAirStationPollutantSetsByMonitorId(paramMap);

                break;
            case EnvironmentalStinkEnum:
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() + "");
                dataList = otherMonitorPointPollutantSetMapper.getOtherMonitorPollutantSetsByMonitorId(paramMap);
                break;
            case EnvironmentalVocEnum:
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() + "");
                dataList = otherMonitorPointPollutantSetMapper.getOtherMonitorPollutantSetsByMonitorId(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("unorgflag", true);
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                paramMap.put("unorgflag", true);
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(paramMap);
                break;
            case StinkEnum:
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() + "");
                dataList = otherMonitorPointPollutantSetMapper.getOtherMonitorPollutantSetsByMonitorId(paramMap);
                paramMap.put("unorgflag", true);
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                dataList.addAll(gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(paramMap));
                break;

        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 3:17
     * @Description: 组装小时排放量查询条件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Query setFlowQuery(Map<String, Object> paramMap) {
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
            if ("HourFlowData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourFlowDataList.PollutantCode").in(pollutantcodes));
            } else if ("DayFlowData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayFlowDataList.PollutantCode").in(pollutantcodes));
            }
        }

        if (paramMap.get("issuddenchange") != null) {

            if ("HourFlowData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourFlowDataList.IsSuddenChange").is(paramMap.get("issuddenchange")));
            } else if ("DayFlowData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayFlowDataList.IsSuddenChange").is(paramMap.get("issuddenchange")));
            }
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        }
        return query;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/11 17:12
     * @Description: 突增污染物信息
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUpRushPollutantInfo(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultMap = new ArrayList<>();
        List<Map<String, Object>> outputs = getMonitorAndPollutants(paramMap);
        Comparator<Map<String, Object>> comparator = Comparator.comparing(a -> Integer.parseInt(a.get("OrderIndex") == null ? "-1" : a.get("OrderIndex").toString()));
        String collectiontype = paramMap.get("collectiontype") == null ? "1" : paramMap.get("collectiontype").toString();
        String collection = hourCollection;
        String datalistkey = "HourDataList";
        //默认查询小时浓度数据，2为查询分钟浓度数据
        if ("2".equals(collectiontype)) {
            datalistkey = "MinuteDataList";
            collection = minuteCollection;
        }
        outputs.sort(comparator);
        if (outputs.size() == 0) {
            return resultMap;
        }
        String mn = paramMap.get("mn").toString();
        int remindtype = (int) paramMap.get("remindtype");
        Date starttime = DataFormatUtil.parseDate(paramMap.get("starttime") + " 00:00:00");
        Date endtime = DataFormatUtil.parseDate(paramMap.get("endtime") + " 23:59:59");
        Set<String> pollutantCodes = outputs.stream().filter(m -> m.get("PollutantCode") != null).map(m -> m.get("PollutantCode").toString()).collect(Collectors.toSet());
        //查询条件
        Criteria criteria = Criteria.where("DataGatherCode").is(mn)
                .and("PollutantCode").in(pollutantCodes)
                .and("IsSuddenChange").is(true);
        if (starttime != null && endtime != null) {
            criteria.and("MonitorTime").gte(starttime).lte(endtime);
        }
        List<Document> mappedResults = new ArrayList<>();
        if (remindtype == FlowChangeEnum.getCode()) {  //排放量
            Fields fields = fields("DataGatherCode", "MonitorTime", "HourFlowDataList.PollutantCode", "HourFlowDataList.CorrectedFlow", "HourFlowDataList.ChangeMultiple", "HourFlowDataList.IsSuddenChange", "_id");
            Aggregation hourFlowDataList = newAggregation(
                    unwind("HourFlowDataList"),
                    project(fields),
                    match(criteria));
            mappedResults = mongoTemplate.aggregate(hourFlowDataList, "HourFlowData", Document.class).getMappedResults();
        } else if (remindtype == ConcentrationChangeEnum.getCode()) {    //浓度
            Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", "HourDataList.AvgStrength", datalistkey + ".IsSuddenChange", datalistkey + ".ChangeMultiple", "_id");
            Aggregation hourDataList = newAggregation(
                    unwind(datalistkey),
                    project(fields),
                    match(criteria));
            mappedResults = mongoTemplate.aggregate(hourDataList, collection, Document.class).getMappedResults();
        }
        for (Map<String, Object> output : outputs) {
            Map<String, Object> mapInfo = new HashMap<>();
            String pollutantCode = output.get("PollutantCode").toString();
            Object pollutantUnit = output.get("PollutantUnit");
            Object IsHasConvertData = output.get("IsHasConvertData");
            Object rate = output.get("Rate");
            String pollutantName = output.get("PollutantName").toString();
            mapInfo.put("pollutantname", pollutantName);
            mapInfo.put("pollutantcode", pollutantCode);
            mapInfo.put("ishasconvertdata", IsHasConvertData != null ? IsHasConvertData : 0);
            mapInfo.put("ChangeBaseValue", output.get("ChangeBaseValue"));
            mapInfo.put("standardvalue", getStandardValue(output));
            mapInfo.put("flowrate", rate);
            mapInfo.put("pollutantunit", pollutantUnit);
            for (Document mappedResult : mappedResults) {
                String pollutantCodeInfo = mappedResult.getString("PollutantCode");
                if (pollutantCode.equals(pollutantCodeInfo)) {
                    mapInfo.put("ischangewarn", true);
                    break;
                }
            }
            resultMap.add(mapInfo);
        }
        return resultMap;
    }

    /**
     * @author: lip
     * @date: 2020/3/9 0009 下午 2:06
     * @Description: 处理标准值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object getStandardValue(Map<String, Object> output) {
        String standardvalue = "";
        if (output.get("AlarmType") != null) {

            String AlarmType = output.get("AlarmType").toString();

            if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(AlarmType)) {//上限报警
                standardvalue = output.get("StandardMaxValue") != null ? output.get("StandardMaxValue").toString() : "";
            } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(AlarmType)) {//下限报警
                standardvalue = output.get("StandardMinValue") != null ? output.get("StandardMinValue").toString() : "";
            } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(AlarmType)) {//区间报警
                String minValue = output.get("StandardMinValue") != null ? output.get("StandardMinValue").toString() : "";
                String maxValue = output.get("StandardMaxValue") != null ? output.get("StandardMaxValue").toString() : "";
                if (StringUtils.isNotBlank(minValue) && StringUtils.isNotBlank(maxValue)) {
                    standardvalue = minValue + "-" + maxValue;
                }
            }
        }

        return standardvalue;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/17 0017 下午 4:18
     * @Description: 格式化日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [date]
     * @throws:
     */
    public static String format(String date, String parttern) {
        if (StringUtils.isNotBlank(date)) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US);
                Date parse = format.parse(date);
                SimpleDateFormat format1 = new SimpleDateFormat(parttern);
                String format2 = format1.format(parse);
                return format2;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:08
     * @Description: 根据时间、监测点类型和污染物Code统计该污染物排放排放数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Map<String, Object>> countDischargeByParam(Map<String, Object> paramMap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        final String mongdb_moth_pfl = "MonthFlowData";
        Date starttime = DataFormatUtil.parseDate(paramMap.get("starttime") + " 00:00:00");
        Date endtime = DataFormatUtil.parseDate(paramMap.get("endtime") + " 00:00:00");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        List<Map<String, Object>> pfl_outputs = getOutPutsAndPollutants(paramMap);
        List<String> pfl_mns = pfl_outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toList());
        Bson pfl_bson = Filters.and(
                in("DataGatherCode", pfl_mns),
                lte("MonitorTime", endtime),
                gte("MonitorTime", starttime),
                eq("MonthFlowDataList.PollutantCode", pollutantcode));
        FindIterable<Document> pfl_documents = mongoTemplate.getCollection(mongdb_moth_pfl).find(pfl_bson);
        List<Map<String, Object>> pfldata = new ArrayList<>();  //排放量的数据
        for (Document nongdu_document : pfl_documents) {
            List<Document> MonthDataList = nongdu_document.get("MonthFlowDataList", new ArrayList<Document>());
            String monitorTime = DataFormatUtil.getDateYM((Date) nongdu_document.get("MonitorTime"));
            for (Document document : MonthDataList) {
                if (document.get("PollutantCode").equals(pollutantcode) && document.get("PollutantFlow") != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time", monitorTime);
                    map.put("value", decimalFormat.format(Double.valueOf(document.getString("PollutantFlow"))));
                    pfldata.add(map);
                    break;
                }
            }
        }
        Map<String, List<Map<String, Object>>> pfl_data = pfldata.stream().collect(Collectors.groupingBy(m -> m.get("time").toString()));
        formatDischargeAndDensityResultByKey(resultMap, pfl_data, "pfl_value", true);
        Map<String, Map<String, Object>> resultMapInfo = new LinkedHashMap<>();
        resultMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(m -> resultMapInfo.put(m.getKey(), m.getValue()));
        return resultMapInfo;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/10 15:52
     * @Description: 根据监测点类型获取重点污染物排放量情况（废水废气雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getKeyPollutionsDischargeByMonitorPointType(Date starttime, Date endtime, Integer monitorPointType, Integer inputoroutput) {
        //重点监测排放量污染物
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(monitorPointType);
        List<Map<String, Object>> keyPollutants = pollutants.stream().filter(m -> m.get("IsShowFlow") != null && Integer.parseInt(m.get("IsShowFlow").toString()) == 1).collect(Collectors.toList());
        List<String> pollutantCodes = keyPollutants.stream().map(map -> map.get("Code").toString()).collect(Collectors.toList());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitortype", monitorPointType);
        paramMap.put("pollutantcodes", pollutantCodes);
        paramMap.put("inputoroutput", inputoroutput);
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        List<Map<String, Object>> outputs = getOutPutsAndPollutants(paramMap);
        Set<String> mns = outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        Criteria criteria = Criteria.where("MonitorTime").gte(starttime).lte(endtime).and("DataGatherCode").in(mns).and("PollutantCode").in(pollutantCodes);
        Fields fields = fields("DataGatherCode", "MonitorTime", "HourFlowDataList.PollutantCode", "HourFlowDataList.AvgFlow", "_id");
        Aggregation aggregation = newAggregation(
                unwind("HourFlowDataList"),
                project(fields),
                match(criteria));
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, "HourFlowData", Document.class).getMappedResults();
        //根据污染物分组
        Map<String, List<Document>> listMap = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
        for (Map<String, Object> keyPollutant : keyPollutants) {
            String code = keyPollutant.get("Code").toString();
            double flow = 0;
            if (listMap.containsKey(code)) {
                List<Document> maps = listMap.get(code);
                for (Document document : maps) {
                    flow += Double.parseDouble(document.getString("AvgFlow"));
                }
            }
            keyPollutant.put("flow", BigDecimal.valueOf(flow).setScale(2, ROUND_HALF_DOWN).doubleValue());
        }
        return keyPollutants;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/12 15:39
     * @Description: 根据监测点类型获取监测点信息以及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOutPutsAndPollutantsByParam(Map<String, Object> paramMap) {
        return onlineMapper.getOutPutsAndPollutantsByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 1:09
     * @Description: 获取排口ID和污染源对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, String> getOutPutIdAndPollution(List<String> outputids, int monitorPointType) {

        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> outPutIdAndPollution = new HashMap<>();
        if (outputids != null) {
            String outputkey = "";
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                case WasteWaterEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case AirEnum:
                    if (outputids.size() > 0) {
                        paramMap.put("airids", outputids);
                    }
                    outputkey = "monitorpointname";
                    outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case WaterQualityEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                case MicroStationEnum:
                case EnvironmentalDustEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case meteoEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case EnvironmentalVocEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;

                case FactoryBoundaryStinkEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }


            if (outPuts.size() > 0) {
                String outputid;
                String name;
                for (Map<String, Object> outPut : outPuts) {
                    name = "";
                    outputid = outPut.get("pk_id").toString();
                    if (outPut.get("shortername") != null) {
                        name = outPut.get("shortername") + "-";
                    }
                    name += outPut.get(outputkey);
                    outPutIdAndPollution.put(outputid, name);
                }
            }
        }
        return outPutIdAndPollution;
    }


    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 1:09
     * @Description: 获取MN和污染源对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, String> getMNAndPollution(List<String> outputids, int monitorPointType) {

        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndPollution = new HashMap<>();
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        //paramMap.put("userid", userid);
        if (outputids != null) {
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                case WasteWaterEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }
            if (outPuts.size() > 0) {
                String mn;
                for (Map<String, Object> outPut : outPuts) {
                    mn = outPut.get("dgimn").toString();
                    if (outPut.get("pollutionname") != null) {
                        MNAndPollution.put(mn, outPut.get("pollutionname").toString());
                    } else {
                        MNAndPollution.put(mn, "");
                    }
                }
            }
        }
        return MNAndPollution;
    }

    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 1:09
     * @Description: 获取MN和污染源对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Map<String, String>> getMNAndShortName(List<String> outputids, int monitorPointType) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndPollution = new HashMap<>();
        Map<String, String> MNAndPollutionId = new HashMap<>();
        if (outputids != null) {
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                case WasteWaterEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }
            if (outPuts.size() > 0) {
                String mn;
                for (Map<String, Object> outPut : outPuts) {
                    mn = outPut.get("dgimn") != null ? outPut.get("dgimn").toString() : (outPut.get("DGIMN") != null ? outPut.get("DGIMN").toString() : "");
                    if (outPut.get("shortername") != null || outPut.get("ShorterName") != null) {
                        MNAndPollution.put(mn, outPut.get("shortername") != null ? outPut.get("shortername").toString() : (outPut.get("ShorterName") != null ? outPut.get("ShorterName").toString() : ""));
                    } else {
                        MNAndPollution.put(mn, "");
                    }
                    if (outPut.get("pk_pollutionid") != null) {
                        MNAndPollutionId.put(mn, outPut.get("pk_pollutionid").toString());
                    }
                }
            }
            result.put("mnandpollution", MNAndPollution);
            result.put("mnandpollutionid", MNAndPollutionId);
        }
        return result;
    }

    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 1:19
     * @Description: 获取污染物code和name对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, String> getPollutantCodeAndName(List<String> pollutantcodes, int monitorPointTypeCode) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", monitorPointTypeCode);
        paramMap.put("codes", pollutantcodes);
        Map<String, String> codeAndName = new HashMap<>();
        List<Map<String, Object>> pollutant = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : pollutant) {
            codeAndName.put(map.get("code").toString(), map.get("name").toString());
        }
        return codeAndName;
    }

    @Override
    public List<Map<String, Object>> getGasMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getGasMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAirMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getAirMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getWaterMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getWaterMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getRainMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getRainMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getStinkMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getStinkMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getVOCMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getVOCMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getFactorySmallStationMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getFactorySmallStationMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getFactoryStinkMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getFactoryStinkMNSByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/8/19 0019 下午 5:21
     * @Description: 自定义查询条件获取预警、超限、异常数据表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getEarlyOrOverOrExceptionDataByParamMap(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null && paramMap.get("monitortimekey") != null) {
            String monitortimekey = paramMap.get("monitortimekey").toString();
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where(monitortimekey).gte(startDate).lte(endDate));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                query.addCriteria(Criteria.where(monitortimekey).gte(startDate));
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where(monitortimekey).lte(endDate));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));

        }
        if (paramMap.get("datatypes") != null) {
            List<String> datatypes = (List<String>) paramMap.get("datatypes");
            query.addCriteria(Criteria.where("DataType").in(datatypes));
        }

        if (paramMap.get("exceptiontype") != null) {
            String exceptionType = paramMap.get("exceptiontype").toString();
            if (CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum.getCode().equals(exceptionType)) {
                query.addCriteria(Criteria.where("ExceptionType").is(exceptionType));
            } else {
                query.addCriteria(Criteria.where("ExceptionType").ne(exceptionType));
            }

        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    /**
     * @author: lip
     * @date: 2019/8/19 0019 下午 5:21
     * @Description: 自定义查询条件获取最新预警、超限、异常数据表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getLastEarlyOrOverOrExceptionDataByParamMap(Map<String, Object> paramMap) {

        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (paramMap.get("exceptiontype") != null) {
            String exceptionType = paramMap.get("exceptiontype").toString();
            if (CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum.getCode().equals(exceptionType)) {
                criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            } else {
                criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
            }

        } else {
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
        }
        Map<String, Object> pollutantList = new HashMap<>();

        pollutantList.put("PollutantCode", "$PollutantCode");
        pollutantList.put("MonitorValue", "$MonitorValue");
        pollutantList.put("DataType", "$DataType");
        if (!timefield.equals("ExceptionTime")) {
            pollutantList.put("AlarmLevel", "$AlarmLevel");
        }
        pollutantList.put("MonitorTime", "$" + timefield);
        operations.add(Aggregation.match(criteria));
        if (timefield.equals("ExceptionTime")) {
            pollutantList.put("ExceptionType", "$ExceptionType");
            operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield, "PollutantCode", "MonitorValue", "DataType", "ExceptionType"));
        } else {
            operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield, "PollutantCode", "MonitorValue", "AlarmLevel", "DataType"));
        }
        operations.add(Aggregation.group("DataGatherCode")
                .max(timefield).as("lasttime")
                .push(pollutantList).as("pollutantList")

        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }

    @Override
    public Map<String, Object> setIntegrationAlarmData(Integer remindCode, Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String end = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(end);
        String timefield = "";
        String collection = "";
        List<Document> listItems = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remindCode == EarlyAlarmEnum.getCode()) {
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "PollutantCode", "LastMonitorValue", "FirstOverTime", "LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MN", timefield));
        } else if (remindCode == OverAlarmEnum.getCode()) {
            //超限报警
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "PollutantCode", "LastMonitorValue", "FirstOverTime", "LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MN", timefield));
        } else if (remindCode == ExceptionAlarmEnum.getCode()) {
            //异常
            timefield = "FirstExceptionTime";
            collection = "ExceptionModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "PollutantCode", "MonitorValue", "FirstExceptionTime", "LastExceptionTime", "ExceptionTime", "ExceptionType")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MN", timefield));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();

        if (listItems != null && listItems.size() > 0) {
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("MN").toString()));
            for (String mn : mapDocuments.keySet()) {
                List<Document> documents = mapDocuments.get(mn);
                Map<String, Object> codeanddata = new HashMap<>();
                Map<String, Map<String, Object>> levelmap = new HashMap<>();
                String firsttime = "";
                String endtime = "";
                String cb_or_yc_times = "";
                Set<String> codes = new HashSet<>();
                for (Document document : documents) {
                    //报警时间
                    if (remindCode == ExceptionAlarmEnum.getCode()) {
                        firsttime = DataFormatUtil.getDateHM(document.getDate("FirstExceptionTime"));
                        endtime = DataFormatUtil.getDateHM(document.getDate("LastExceptionTime"));
                        cb_or_yc_times = document.getString("ExceptionTime");
                    } else {
                        firsttime = DataFormatUtil.getDateHM(document.getDate("FirstOverTime"));
                        endtime = DataFormatUtil.getDateHM(document.getDate("LastOverTime"));
                        cb_or_yc_times = document.getString("OverTime");
                    }
                    String timestr = "";
                    if (firsttime.equals(endtime)) {
                        timestr = firsttime;
                    } else {
                        timestr = firsttime + "-" + endtime;
                    }
                    //污染物
                    String code = document.getString("PollutantCode");
                    codes.add(code);
                    //按污染物和报警级别分组
                    if (levelmap.get(code) != null) {
                        Map<String, Object> levelone = levelmap.get(code);
                        if (remindCode == OverAlarmEnum.getCode()) {
                            String level = document.getInteger("AlarmLevel").toString();
                            String overstr = "";
                            if ("-1".equals(level)) {
                                overstr = "超标";
                            } else {
                                overstr = "超限";
                            }
                            if (levelone.get(overstr) != null) {//判断是否有该级别的数据
                                if (Double.valueOf(cb_or_yc_times).intValue() > 0) {
                                    levelone.put(overstr, levelone.get(overstr) + "、" + timestr + "【" + countHourMinuteTime(Double.valueOf(cb_or_yc_times).intValue()) + "】");
                                } else {
                                    levelone.put(overstr, levelone.get(overstr) + "、" + timestr);
                                }
                            } else {
                                if (Double.valueOf(cb_or_yc_times).intValue() > 0) {
                                    levelone.put(overstr, timestr + "【" + countHourMinuteTime(Double.valueOf(cb_or_yc_times).intValue()) + "】");
                                } else {
                                    levelone.put(overstr, timestr);
                                }
                            }
                        } else if (remindCode == ExceptionAlarmEnum.getCode()) {
                            String overstr = "";
                            overstr = CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"));
                            if (levelone.get(overstr) != null) {//判断是否有该级别的数据
                                if (Double.valueOf(cb_or_yc_times).intValue() > 0) {
                                    levelone.put(overstr, levelone.get(overstr) + "、" + timestr + "【" + countHourMinuteTime(Double.valueOf(cb_or_yc_times).intValue()) + "】");
                                } else {
                                    levelone.put(overstr, levelone.get(overstr) + "、" + timestr);
                                }
                            } else {
                                if (Double.valueOf(cb_or_yc_times).intValue() > 0) {
                                    levelone.put(overstr, timestr + "【" + countHourMinuteTime(Double.valueOf(cb_or_yc_times).intValue()) + "】");
                                } else {
                                    levelone.put(overstr, timestr);
                                }
                            }
                        }
                    } else {
                        Map<String, Object> levelone = new HashMap<>();
                        if (remindCode == OverAlarmEnum.getCode()) {
                            String level = document.getInteger("AlarmLevel").toString();
                            String overstr = "";
                            if ("-1".equals(level)) {
                                overstr = "超标";
                            } else {
                                overstr = "超限";
                            }
                            if (Double.valueOf(cb_or_yc_times).intValue() > 0) {
                                levelone.put(overstr, timestr + "【" + countHourMinuteTime(Double.valueOf(cb_or_yc_times).intValue()) + "】");
                            } else {
                                levelone.put(overstr, timestr);
                            }
                        } else if (remindCode == ExceptionAlarmEnum.getCode()) {
                            String overstr = "";
                            overstr = CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"));
                            if (Double.valueOf(cb_or_yc_times).intValue() > 0) {
                                levelone.put(overstr, timestr + "【" + countHourMinuteTime(Double.valueOf(cb_or_yc_times).intValue()) + "】");
                            } else {
                                levelone.put(overstr, timestr);
                            }
                        }
                        levelmap.put(code, levelone);
                    }
                }
                if (codes != null && codes.size() > 0) {
                    for (String code : codes) {
                        String codestr = "";
                        if (levelmap.get(code) != null) {
                            Map<String, Object> onemap = levelmap.get(code);
                            for (String key : onemap.keySet()) {
                                codestr += key + onemap.get(key) + ";";
                            }
                        }
                        codeanddata.put(code, codestr);
                    }
                }
                result.put(mn, codeanddata);
                //获取点位 总报警时长
                String totaltime = coutOnePointAlarmTimes(documents, remindCode);
                result.put(mn + "_totaltime", totaltime);
            }
        }
        return result;
    }

    /**
     * 统计单个点报警总时长
     * */
    private String coutOnePointAlarmTimes(List<Document> documents, Integer remindCode) {
        String alarmtotal = "";
        int alarmtimenum = 0;
        Date firsttime = null;
        Date lasttime = null;
        String startkey = "";
        String endkey = "";
        if (remindCode == ExceptionAlarmEnum.getCode()) {
            startkey = "FirstExceptionTime";
            endkey = "LastExceptionTime";
        } else {
            startkey = "FirstOverTime";
            endkey = "LastOverTime";
        }
        for (Document podo : documents) {
            //比较时间 获取报警时段
            if (podo.get(startkey) != null && podo.get(endkey) != null) {
                if (firsttime == null && lasttime == null) {
                    firsttime = podo.getDate(startkey);
                    lasttime = podo.getDate(endkey);
                } else {
                    //比较时间段
                    //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                    if (DataFormatUtil.getDateYMDHMS(podo.getDate(startkey)).equals(DataFormatUtil.getDateYMDHMS(lasttime)) ||
                            podo.getDate(startkey).before(lasttime)) {
                        //若被包含 比较两个结束时间
                        if (lasttime.before(podo.getDate(endkey))) {
                            //若第一次报警的结束时间 小于 第二个报警时段的结束时间
                            //则进行赋值
                            lasttime = podo.getDate(endkey);
                        }
                    } else {
                        //第二次报警时段不被包含于第一个报警时段中
                        if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
                            alarmtimenum += 1;
                        } else {
                            long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
                            alarmtimenum += Integer.valueOf(timenum + "");
                        }
                        //将重新赋值开始 结束时间
                        firsttime = podo.getDate(startkey);
                        lasttime = podo.getDate(endkey);
                    }
                }
            }
        }
        //将最后一次超标时段 或一直连续报警的超标时段 拼接
        if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
            alarmtimenum += 1;
        } else {
            long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
            alarmtimenum += Integer.valueOf(timenum + "");
        }
        if (alarmtimenum > 0) {
            alarmtotal = countHourMinuteTime(Double.valueOf(alarmtimenum).intValue());
        }
        return alarmtotal;
    }

    @Override
    public List<Map<String, Object>> getSecurityRiskAreaMonitorPointMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getSecurityRiskAreaMonitorPointMNSByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getStorageTankAreaMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getStorageTankAreaMNSByParam(paramMap);
    }

    @Override
    public Map<String, String> getMNAndMonitorPoint(List<String> outputids, Integer type) {

        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndMonitorPoint = new HashMap<>();
        if (outputids != null) {
            String outputkey = "";
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                case WasteWaterEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                case SmokeEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case AirEnum:
                    if (outputids.size() > 0) {
                        paramMap.put("airids", outputids);
                    }
                    outputkey = "monitorpointname";
                    outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", type);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case EnvironmentalVocEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", type);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }


            for (Map<String, Object> outPut : outPuts) {
                if (outPut.get("dgimn") != null) {
                    if (!outputkey.equals("")) {
                        MNAndMonitorPoint.put(outPut.get("dgimn").toString(), outPut.get(outputkey).toString());
                    }
                }
            }

        }
        return MNAndMonitorPoint;
    }


    @Override
    public List<Map<String, Object>> getMNAndMonitorPointByParam(Map<String, Object> paramMap) {
        Integer type = Integer.parseInt(paramMap.get("monitorpointtypecode").toString());
        List<Map<String, Object>> outPuts = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
            case WasteWaterEnum:
                paramMap.put("outputtype", "water");
                outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                break;
            case WasteGasEnum:
                paramMap.put("monitorpointtype", type);
                outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                break;
            case SmokeEnum:
                paramMap.put("monitorpointtype", type);
                outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                break;
            case RainEnum:
                paramMap.put("outputtype", "rain");
                outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                break;
            case AirEnum:
                outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                break;
            case MicroStationEnum:
                paramMap.put("monitorPointType", type);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalStinkEnum:
                paramMap.put("monitorPointType", type);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalVocEnum:
                paramMap.put("monitorPointType", type);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("monitorpointtype", type);
                outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
            case WaterQualityEnum:
                outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                paramMap.put("monitorpointtype", type);
                outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
            /*case StorageTankAreaEnum: //储罐
                outPuts = storageTankInfoMapper.getStorageTankInfoAndMnByParam(paramMap);
                break;
            case SecurityLeakageMonitor: //安全泄露监测点
            case SecurityCombustibleMonitor: //可燃易爆气体监测点类型
            case SecurityToxicMonitor: //有毒有害气体监测点类型
                paramMap.put("monitorpointtype", type);
                outPuts = riskAreaMonitorPointMapper.getPollutionAndMonitorInfoByParamMap(paramMap);
                break;
            case ProductionSiteEnum://生产装置
                outPuts =  hazardSourceProductDeviceMapper.getHazardSourceProductDeviceInfoByParamMap(paramMap);
                break;*/
        }
        return outPuts;
    }

    @Override
    public List<Document> getLastChangeDataByParamMap(Map<String, Object> paramMap) {

        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("HourDataList.IsSuddenChange").is(true);
        operations.add(Aggregation.unwind("HourDataList"));
        operations.add(Aggregation.match(criteria));
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("PollutantCode", "$PollutantCode");
        pollutantList.put("MonitorValue", "$AvgStrength");
        pollutantList.put("IsSuddenChange", "$IsSuddenChange");
        pollutantList.put("ChangeMultiple", "$ChangeMultiple");
        pollutantList.put("MonitorTime", "$" + timefield);
        operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield)
                .and("HourDataList.PollutantCode").as("PollutantCode")
                .and("HourDataList.AvgStrength").as("AvgStrength")
                .and("HourDataList.IsSuddenChange").as("IsSuddenChange")
                .and("HourDataList.ChangeMultiple").as("ChangeMultiple")
                .and(timefield).as(timefield));
        operations.add(Aggregation.group("DataGatherCode")
                .max(timefield).as("lasttime")
                .push(pollutantList).as("HourDataList")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }

    @Override
    public List<Document> getMinuteLastChangeDataByParamMap(Map<String, Object> paramMap) {
        //分钟浓度突变
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("MinuteDataList.IsSuddenChange").is(true);
        operations.add(Aggregation.unwind("MinuteDataList"));
        operations.add(Aggregation.match(criteria));
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("PollutantCode", "$PollutantCode");
        pollutantList.put("MonitorValue", "$AvgStrength");
        pollutantList.put("IsSuddenChange", "$IsSuddenChange");
        pollutantList.put("ChangeMultiple", "$ChangeMultiple");
        pollutantList.put("MonitorTime", "$" + timefield);
        operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield)
                .and("MinuteDataList.PollutantCode").as("PollutantCode")
                .and("MinuteDataList.AvgStrength").as("AvgStrength")
                .and("MinuteDataList.IsSuddenChange").as("IsSuddenChange")
                .and("MinuteDataList.ChangeMultiple").as("ChangeMultiple")
                .and(timefield).as(timefield));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        operations.add(Aggregation.group("DataGatherCode")
                .max(timefield).as("lasttime")
                .push(pollutantList).as("MinuteDataList")
        );

        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }

    @Override
    public List<Document> getChangeAlarmDataByParamMap(Map<String, Object> paramMap) {
        //分钟浓度突变
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").is("MinuteData");
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("PollutantCode", "$PollutantCode");
        pollutantList.put("MonitorValue", "$MonitorValue");
        pollutantList.put("ChangeMultiple", "$ChangeMultiple");
        pollutantList.put("ChangeTime", "$" + timefield);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield, "PollutantCode", "MonitorValue", "ChangeMultiple"));
        operations.add(Aggregation.group("DataGatherCode")
                .max(timefield).as("lasttime")
                .push(pollutantList).as("pollutantList")

        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }


    @Override
    public List<Document> getUnWindMonitorDataByParamMap(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        String unWindKey = paramMap.get("unwindkey").toString();
        List<String> pollutantCodes = (List<String>) paramMap.get("pollutantCodes");
        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and(unWindKey + ".PollutantCode").in(pollutantCodes);
        operations.add(Aggregation.unwind(unWindKey));
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", timefield)
                .and(unWindKey + ".PollutantCode").as("PollutantCode")
                .and(unWindKey + ".AvgStrength").as("AvgStrength")
                .and(timefield).as(timefield));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }


    @Override
    public Map<String, String> getMNAndPollutionId(List<String> outputids, Integer type) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndPollutionId = new HashMap<>();
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        //paramMap.put("userid", userid);
        if (outputids != null) {
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                case WasteWaterEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }
            if (outPuts.size() > 0) {
                String mn;
                for (Map<String, Object> outPut : outPuts) {
                    mn = outPut.get("dgimn") != null ? outPut.get("dgimn").toString() : (outPut.get("DGIMN") != null ? outPut.get("DGIMN").toString() : "");
                    if (outPut.get("pk_pollutionid") != null || outPut.get("pollutionid") != null) {
                        MNAndPollutionId.put(mn, outPut.get("pk_pollutionid") != null ? outPut.get("pk_pollutionid").toString() : (outPut.get("pollutionid") != null ? outPut.get("pollutionid").toString() : ""));
                    }
                }
            }
        }
        return MNAndPollutionId;
    }


    @Override
    public Map<String, String> getMNAndPId(List<String> outputids, Integer type) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndPollutionId = new HashMap<>();
        if (outputids != null) {
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                case WasteWaterEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", type);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }
            if (outPuts.size() > 0) {
                String mn;
                for (Map<String, Object> outPut : outPuts) {
                    mn = outPut.get("dgimn").toString();
                    if (outPut.get("pk_pollutionid") != null) {
                        MNAndPollutionId.put(mn, outPut.get("pk_pollutionid").toString());
                    }
                }
            }
        }
        return MNAndPollutionId;
    }


    @Override
    public Map<String, String> getMNAndMonitorPointName(List<String> outputids, Integer monitorPointType) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndMonitorPointName = new HashMap<>();
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("userid", userid);
        if (outputids != null) {
            String outputkey = "";
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                case WasteWaterEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    outputkey = "outputname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case AirEnum:
                    outputkey = "monitorpointname";
                    if (outputids.size() > 0) {
                        paramMap.put("airids", outputids);
                    }
                    outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;

                case WaterQualityEnum:
                    outputkey = "monitorpointname";
                    if (outputids.size() > 0) {
                        paramMap.put("outputids", outputids);
                    }
                    outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                case MicroStationEnum:
                case EnvironmentalDustEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case EnvironmentalVocEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:

                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    outputkey = "monitorpointname";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }
            if (outPuts.size() > 0) {
                String mn;
                for (Map<String, Object> outPut : outPuts) {
                    mn = outPut.get("dgimn").toString();
                    MNAndMonitorPointName.put(mn, outPut.get(outputkey).toString());

                }
            }
        }
        return MNAndMonitorPointName;
    }

    /**
     * @author: lip
     * @date: 2019/10/30 0030 上午 11:49
     * @Description: 获取监测点ID和mn号的关联关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @Override
    public Map<String, String> getMNAndMonitorPointId(List<String> outputids, Integer monitorPointType) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> MNAndMonitorPointName = new HashMap<>();
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        //paramMap.put("userid", userid);
        if (outputids != null) {
            String outputkey = "";
            List<Map<String, Object>> outPuts = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                case WasteWaterEnum:
                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "water");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    paramMap.put("outputtype", "rain");
                    outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case AirEnum:
                    outputkey = "pk_id";
                    if (outputids.size() > 0) {
                        paramMap.put("airids", outputids);
                    }
                    outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case WaterQualityEnum:
                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                case MicroStationEnum:
                case EnvironmentalDustEnum:
                    outputkey = "pk_id";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case EnvironmentalVocEnum:
                    outputkey = "pk_id";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:

                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    outputkey = "pk_id";
                    paramMap.put("outputids", outputids);
                    paramMap.put("monitorpointtype", monitorPointType);
                    outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
            }
            if (outPuts.size() > 0) {
                String mn;
                for (Map<String, Object> outPut : outPuts) {
                    mn = outPut.get("dgimn").toString();
                    MNAndMonitorPointName.put(mn, outPut.get(outputkey).toString());

                }
            }
        }
        return MNAndMonitorPointName;
    }


    /**
     * @author: lip
     * @date: 2019/10/30 0030 上午 11:49
     * @Description: 获取监测点数据和mn号的关联关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointDataByParam(Map<String, Object> param) {
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> outPuts = new ArrayList<>();
        if (param.get("userid") != null) {
            paramMap.put("userid", param.get("userid"));
        }
        if (param.get("pollutionname") != null) {
            paramMap.put("pollutionname", param.get("pollutionname"));
        }
        if (param.get("monitorpointname") != null) {
            paramMap.put("monitorpointname", param.get("monitorpointname"));
        }
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (param.get("outputids") != null) {
            List<String> outputids = (List<String>) param.get("outputids");
            int monitorPointType = Integer.parseInt(param.get("monitorpointtype").toString());
            if (notothertypes.contains(monitorPointType)) {
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
                    case WasteWaterEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("outputtype", "water");
                        outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                        break;
                    case WasteGasEnum:
                    case SmokeEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("monitorpointtype", monitorPointType);
                        outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                        break;
                    case RainEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("outputtype", "rain");
                        outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                        break;
                    case AirEnum:
                        if (outputids.size() > 0) {
                            paramMap.put("airids", outputids);
                        }
                        outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                        break;
                    case WaterQualityEnum:
                        if (outputids.size() > 0) {
                            paramMap.put("outputids", outputids);
                        }
                        outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                        break;
                    case FactoryBoundaryStinkEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("monitorpointtype", monitorPointType);
                        outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        paramMap.put("outputids", outputids);
                        paramMap.put("monitorpointtype", monitorPointType);
                        outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;
                }
            } else {
                if (param.get("monitorPointCategory") != null) {
                    paramMap.put("monitorPointCategorys", Arrays.asList(param.get("monitorPointCategory")));
                }
                paramMap.put("monitorPointType", monitorPointType);
                paramMap.put("outputids", outputids);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
            }

        }
        return outPuts;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/9 15:36
     * @Description: 企业排放量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countEntDischargeRankByParam(String pollutantcode, Date starttime, Date endtime, List<Integer> monitorPointTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        final String mongdb_moth_pfl = "MonthFlowData";
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        Criteria criteria = Criteria.where("MonitorTime").gte(starttime).lte(endtime);
        if (!pollutantcode.equals("totalflow")) {
            paramMap.put("pollutantcode", pollutantcode);
            criteria.and("MonthFlowDataList.PollutantCode").is(pollutantcode);
        }
        //添加数据权限
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("datauserid", userid);
        List<Map<String, Object>> outputs = new ArrayList<>();
        for (Integer i : monitorPointTypes) {
            paramMap.put("monitortype", i);
            outputs.addAll(getOutPutsAndPollutants(paramMap));
        }
        Set<String> mns = outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        criteria.and("DataGatherCode").in(mns);
        Aggregation aggregation = newAggregation(match(criteria));
        List<Document> documents = mongoTemplate.aggregate(aggregation, mongdb_moth_pfl, Document.class).getMappedResults();
        //企业id mn号去重
        List<Map<String, Object>> list = outputs.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.get("PollutionID") + "#"
                        + o.get("MN")))),
                ArrayList::new));
        //根据企业id+名称分组
        Map<String, List<Map<String, Object>>> listMap = list.stream().collect(Collectors.groupingBy(m -> m.get("PollutionID").toString()
                + "#!-!#" + m.get("PollutionName").toString()
                + "#!-!#" + m.get("shortername").toString()));
        for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
            List<Map<String, Object>> oneEnt = entry.getValue();
            double flow = 0;
            for (Map<String, Object> objectMap : oneEnt) {
                String mn = objectMap.get("MN").toString();
                for (Document document : documents) {
                    if (pollutantcode.equals("totalflow")) {    //总排放量
                        if (document.getString("DataGatherCode").equals(mn)) {
                            flow += Double.parseDouble(document.get("TotalFlow").toString());
                        }
                    } else {     //单个污染物排放量
                        if (document.getString("DataGatherCode").equals(mn)) {
                            List<Document> MonthDataList = document.get("MonthFlowDataList", new ArrayList<Document>());
                            for (Document document1 : MonthDataList) {
                                if (document1.get("PollutantCode").equals(pollutantcode)) {
                                    if (document1.get("PollutantFlow") != null) {
                                        flow += Double.parseDouble(document1.get("PollutantFlow").toString());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            String[] split = entry.getKey().split("#!-!#");
            Map<String, Object> mapInfo = new HashMap<>();
            mapInfo.put("pollutionid", split[0]);
            mapInfo.put("pollutionname", split[1]);
            mapInfo.put("shortername", split[2]);
            BigDecimal bigDecimal = BigDecimal.valueOf(flow);
            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            mapInfo.put("flow", value);
            result.add(mapInfo);
        }
        return result.stream().filter(m -> m.get("flow") != null).sorted(Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
    }


    /**
     * @author: mmt
     * @date: 2022/11/14 11:36
     * @Description: 企日累计日排放量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countEntDayDischargeRankByParam(String pollutantcode, Date starttime, Date endtime, List<Integer> monitorPointTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        final String mongdb_day_pfl = "DayFlowData";
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        Criteria criteria = Criteria.where("MonitorTime").gte(starttime).lte(endtime);
        if (!pollutantcode.equals("totalflow")) {
            paramMap.put("pollutantcode", pollutantcode);
            criteria.and("DayFlowDataList.PollutantCode").is(pollutantcode);
        }
        //添加数据权限
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("datauserid", userid);
        List<Map<String, Object>> outputs = new ArrayList<>();
        for (Integer i : monitorPointTypes) {
            paramMap.put("monitortype", i);
            outputs.addAll(getOutPutsAndPollutants(paramMap));
        }
        Set<String> mns = outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        criteria.and("DataGatherCode").in(mns);
        Aggregation aggregation = newAggregation(match(criteria));
        List<Document> documents = mongoTemplate.aggregate(aggregation, mongdb_day_pfl, Document.class).getMappedResults();
        //企业id mn号去重
        List<Map<String, Object>> list = outputs.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.get("PollutionID") + "#"
                        + o.get("MN")))),
                ArrayList::new));
        //根据企业id+名称分组
        Map<String, List<Map<String, Object>>> listMap = list.stream().collect(Collectors.groupingBy(m -> m.get("PollutionID").toString()
                + "#!-!#" + m.get("PollutionName").toString()
                + "#!-!#" + m.get("shortername").toString()));
        for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
            List<Map<String, Object>> oneEnt = entry.getValue();
            double flow = 0;
            for (Map<String, Object> objectMap : oneEnt) {
                String mn = objectMap.get("MN").toString();
                for (Document document : documents) {
                    if (pollutantcode.equals("totalflow")) {    //总排放量
                        if (document.getString("DataGatherCode").equals(mn)) {
                            flow += Double.parseDouble(document.get("TotalFlow").toString());
                        }
                    } else {     //单个污染物排放量
                        if (document.getString("DataGatherCode").equals(mn)) {
                            List<Document> MonthDataList = document.get("DayFlowDataList", new ArrayList<Document>());
                            for (Document document1 : MonthDataList) {
                                if (document1.get("PollutantCode").equals(pollutantcode)) {
                                    if (document1.get("AvgFlow") != null) {
                                        flow += Double.parseDouble(document1.get("AvgFlow").toString());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            String[] split = entry.getKey().split("#!-!#");
            Map<String, Object> mapInfo = new HashMap<>();
            mapInfo.put("pollutionid", split[0]);
            mapInfo.put("pollutionname", split[1]);
            mapInfo.put("shortername", split[2]);
            BigDecimal bigDecimal = BigDecimal.valueOf(flow);
            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            mapInfo.put("flow", value);
            result.add(mapInfo);
        }
        return result.stream().filter(m -> m.get("flow") != null && (Double)m.get("flow") > 0d).sorted(Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/26 11:04
     * @Description: 根据mns、污染物code 以及时间获取排放量和浓度在此时间内的变化趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Map<String, Object>> countDischargeAndDensityByCodeAndMns(String pollutantcode, String
            starttimeInfo, String endTimeInfo, String pollutionid, Integer monitorType) {
        final String mongdb_moth_pfl = "MonthFlowData";
        final String mongdb_moth_nongdu = "MonthData";
        Date starttime = DataFormatUtil.parseDate(starttimeInfo + " 00:00:00");
        Date endtime = DataFormatUtil.parseDate(endTimeInfo + " 23:59:59");
        Map<String, Map<String, Object>> resultInfo = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtils.isNotBlank(pollutionid)) {
            paramMap.put("pollutionid", pollutionid);
        }
        //添加数据权限
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("datauserid", userid);
        paramMap.put("monitortype", monitorType);
        if (pollutantcode.equals("totalflow")) { //只有排放量
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
            List<Map<String, Object>> monitorAndPollutants = getOutPutsAndPollutants(paramMap);
            List<String> pfl_mns = monitorAndPollutants.stream().map(output -> output.get("MN").toString()).collect(Collectors.toList());
            Bson all_pfl_bson = Filters.and(
                    in("DataGatherCode", pfl_mns),
                    lte("MonitorTime", endtime),
                    gte("MonitorTime", starttime));
            FindIterable<Document> pfl_documents = mongoTemplate.getCollection(mongdb_moth_pfl).find(all_pfl_bson);
            List<Map<String, Object>> count_pfl_data = new ArrayList<>();  //排放量的数据
            for (Document document : pfl_documents) {
                String time = DataFormatUtil.getDateYM((Date) document.get("MonitorTime"));
                Map<String, Object> map = new HashMap<>();
                map.put("time", time);
                map.put("value", document.get("TotalFlow"));
                count_pfl_data.add(map);
            }
            Map<String, List<Map<String, Object>>> group_count_pfl_data = count_pfl_data.stream().collect(Collectors.groupingBy(m -> m.get("time").toString()));
            formatDischargeAndDensityResultByKey(resultInfo, group_count_pfl_data, "pfl_value", true);
        } else {
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
            List<Map<String, Object>> pfl_outputs = getOutPutsAndPollutants(paramMap);
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
            List<Map<String, Object>> nongdu_outputs = getOutPutsAndPollutants(paramMap);
            List<String> pfl_mns = pfl_outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toList());
            List<String> nongdu_mns = nongdu_outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toList());
            Bson nongdu_bson = Filters.and(
                    in("DataGatherCode", nongdu_mns),
                    lte("MonitorTime", endtime),
                    gte("MonitorTime", starttime),
                    eq("MonthDataList.PollutantCode", pollutantcode));
            Bson pfl_bson = Filters.and(
                    in("DataGatherCode", pfl_mns),
                    lte("MonitorTime", endtime),
                    gte("MonitorTime", starttime),
                    eq("MonthFlowDataList.PollutantCode", pollutantcode));
            FindIterable<Document> nongdu_documents = mongoTemplate.getCollection(mongdb_moth_nongdu).find(nongdu_bson);
            FindIterable<Document> pfl_documents = mongoTemplate.getCollection(mongdb_moth_pfl).find(pfl_bson);
            List<Map<String, Object>> nongdudata = new ArrayList<>();  //浓度的数据
            List<Map<String, Object>> pfldata = new ArrayList<>();  //排放量的数据
            for (Document nongdu_document : nongdu_documents) {
                List<Document> MonthDataList = nongdu_document.get("MonthDataList", new ArrayList<Document>());
                String monitorTime = DataFormatUtil.getDateYM((Date) nongdu_document.get("MonitorTime"));
                for (Document document : MonthDataList) {
                    if (document.get("PollutantCode").equals(pollutantcode) && document.get("AvgStrength") != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("time", monitorTime);
                        map.put("value", document.get("AvgStrength"));
                        nongdudata.add(map);
                        break;
                    }
                }
            }
            for (Document pfl_document : pfl_documents) {
                List<Document> MonthDataList = pfl_document.get("MonthFlowDataList", new ArrayList<Document>());
                String monitorTime = DataFormatUtil.getDateYM((Date) pfl_document.get("MonitorTime"));
                for (Document document : MonthDataList) {
                    if (document.get("PollutantCode").equals(pollutantcode) && document.get("PollutantFlow") != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("time", monitorTime);
                        map.put("value", document.get("PollutantFlow"));
                        pfldata.add(map);
                        break;
                    }
                }
            }
            Map<String, List<Map<String, Object>>> nongdu_data = nongdudata.stream().collect(Collectors.groupingBy(m -> m.get("time").toString()));
            Map<String, List<Map<String, Object>>> pfl_data = pfldata.stream().collect(Collectors.groupingBy(m -> m.get("time").toString()));
            formatDischargeAndDensityResultByKey(resultInfo, nongdu_data, "nongdu_value", false);
            formatDischargeAndDensityResultByKey(resultInfo, pfl_data, "pfl_value", false);
        }
        Map<String, Map<String, Object>> resultMap = new LinkedHashMap<>();
        resultInfo.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(m -> resultMap.put(m.getKey(), m.getValue()));
        return resultMap;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 17:34
     * @Description: 根据企业ID获取其排口信息以及主要污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOutPutAndPollutantsByPollutionID(String pollutionid, List<CommonTypeEnum.MonitorPointTypeEnum> enums) {
        return enums.stream().map(anEnum -> getOutPutAndPollutantsByPollutionIDandMonitorType(pollutionid, anEnum)).collect(Collectors.toList());
    }


    /**
     * @author: lip
     * @date: 2019/7/30 0030 上午 11:44
     * @Description: 自定义查询条件获取污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Set<Map<String, Object>> getPollutantSetDataByParamMap(Map<String, Object> paramMap) {
        Integer pollutanttype = Integer.parseInt(paramMap.get("pollutanttype").toString());
        Set<Map<String, Object>> pollutantSetData = new LinkedHashSet<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (notothertypes.contains(pollutanttype)) {
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutanttype)) {
                case WasteWaterEnum:
                    paramMap.put("datamark", 1);
                    dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(paramMap);
                    break;
                case WasteGasEnum:
                case SmokeEnum:
                    paramMap.put("unorgflag", false);
                    dataList = gasOutPutPollutantSetMapper.getOutPutPollutantSetsByParams(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("datamark", 3);
                    dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(paramMap);
                    break;
                case AirEnum:
                    dataList = gasOutPutPollutantSetMapper.getOutPutPollutantSetsByParams(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    dataList = gasOutPutPollutantSetMapper.getOutPutPollutantSetsByParams(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    dataList = gasOutPutPollutantSetMapper.getOutPutPollutantSetsByParams(paramMap);
                    break;
                case WaterQualityEnum:
                    dataList = gasOutPutPollutantSetMapper.getOutPutPollutantSetsByParams(paramMap);
                    break;
            }
        } else {
            paramMap.put("pollutanttype", pollutanttype);
            dataList = gasOutPutPollutantSetMapper.getOtherPointPollutantSetsByParams(paramMap);
        }

        if (dataList.size() > 0) {
            for (Map<String, Object> mapTemp : dataList) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollutantcode", mapTemp.get("pollutantcode"));
                map.put("pollutantname", mapTemp.get("pollutantname"));
                map.put("orderindex", mapTemp.get("orderindex"));
                map.put("standardminvalue", mapTemp.get("standardminvalue"));
                map.put("standardmaxvalue", mapTemp.get("standardmaxvalue"));
                map.put("ishasconvertdata", mapTemp.get("IsHasConvertData"));
                if (mapTemp.get("PollutantUnit") != null) {
                    map.put("pollutantunit", mapTemp.get("PollutantUnit"));
                } else {
                    map.put("pollutantunit", mapTemp.get("pollutantunit"));
                }
                pollutantSetData.add(map);
            }
        }


        return pollutantSetData;
    }

    /**
     * @author: lip
     * @date: 2019/7/30 0030 下午 1:21
     * @Description: 自定义查询条件mongodb最新数据集合中的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getMongodbLatestDataByParamMap(Map<String, Object> paramMap) {

        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        }
        if (paramMap.get("types") != null) {
            List<String> types = (List<String>) paramMap.get("types");
            query.addCriteria(Criteria.where("Type").in(types));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("DataList.PollutantCode").in(pollutantcodes));
        }
        return mongoTemplate.find(query, Document.class, "LatestData");


    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 17:56
     * @Description: 根据污染源id和监测点类型获取排口和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getOutPutAndPollutantsByPollutionIDandMonitorType(String pollutionid, CommonTypeEnum.MonitorPointTypeEnum monitorTypeEnum) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        switch (monitorTypeEnum) {
            case RainEnum:
                data = waterOutputInfoMapper.getRainOutPutAndPollutantsByID(pollutionid);
                break;
            case SmokeEnum:
                data = gasOutPutInfoMapper.getGasOutPutAndPollutantsByID(pollutionid, SmokeEnum.getCode());
                break;
            case WasteGasEnum:
                data = gasOutPutInfoMapper.getGasOutPutAndPollutantsByID(pollutionid, WasteGasEnum.getCode());
                break;
            case WasteWaterEnum:
                data = waterOutputInfoMapper.getWaterOutPutAndPollutantsByID(pollutionid);
                break;
        }
        Map<String, String> countMap = new HashMap<>();
        Map<String, String> onlineMap = new HashMap<>();
        Map<String, String> noOnline = new HashMap<>();
        Map<String, String> yc_Online = new HashMap<>();
        Map<String, String> cb_Online = new HashMap<>();
        Map<String, String> pollutants = new HashMap<>();
        for (Map<String, Object> datum : data) {
            String outPutID = datum.get("OutPutID").toString();
            String Status = datum.get("Status").toString();
            String PollutantName = datum.get("PollutantName").toString();
            String PollutantCode = datum.get("PollutantCode").toString();
            if (!pollutants.containsKey(PollutantCode)) {
                pollutants.put(PollutantCode, PollutantName);
            }
            if (!countMap.containsKey(outPutID)) {
                countMap.put(outPutID, Status);
            }
            if (!onlineMap.containsKey(outPutID) && Status.equals("在线")) {
                onlineMap.put(outPutID, Status);
            }
            if (!noOnline.containsKey(outPutID) && Status.equals("离线")) {
                noOnline.put(outPutID, Status);
            }
            if (!yc_Online.containsKey(outPutID) && Status.equals("异常")) {
                yc_Online.put(outPutID, Status);
            }
            if (!cb_Online.containsKey(outPutID) && Status.equals("超标")) {
                cb_Online.put(outPutID, Status);
            }
        }
        resultMap.put("monitorName", monitorTypeEnum.getName());
        resultMap.put("monitortype", monitorTypeEnum.getCode());
        resultMap.put("count", countMap.size());
        resultMap.put("online", onlineMap.size());
        resultMap.put("noonline", noOnline.size());
        resultMap.put("yichang", yc_Online.size());
        resultMap.put("chaobiao", cb_Online.size());
        if (pollutants.size() > 0) {
            StringBuffer keyPollutant = new StringBuffer();
            pollutants.forEach((key, value) -> keyPollutant.append(value).append("、"));
            String Pollutants = keyPollutant.subSequence(0, keyPollutant.length() - 1).toString();
            resultMap.put("pollutants", Pollutants);
        } else {
            resultMap.put("pollutants", "");
        }
        return resultMap;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/26 15:06
     * @Description: 格式化排放量和浓度在此时间内的变化趋势返回数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void formatDischargeAndDensityResultByKey(Map<String, Map<String, Object>> resultMap, Map<String, List<Map<String, Object>>> dataList, String
            keyName, boolean iscount) {
        for (Map.Entry<String, List<Map<String, Object>>> entry : dataList.entrySet()) {
            String time = entry.getKey();
            double f1 = 0;
            List<Map<String, Object>> valuelist = entry.getValue();
            for (Map<String, Object> map : valuelist) {
                if (map.get("value") != null) {
                    f1 += Double.valueOf(map.get("value").toString());
                }
            }
            BigDecimal bigDecimal = BigDecimal.valueOf(f1);
            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            //如果已经包含这个月份
            if (resultMap.containsKey(time)) {
                Map<String, Object> oldmap = resultMap.get(time);
                oldmap.put(keyName, value);
            } else {
                Map<String, Object> newmap = new HashMap<>();
                if (!iscount) {
                    newmap.put("nongdu_value", 0);
                    newmap.put("pfl_value", 0);
                }
                newmap.put(keyName, value);
                resultMap.put(time, newmap);
            }
        }
    }

    private List<Map<String, Object>> getOutPutsAndPollutants(Map<String, Object> paramMap) {
        CommonTypeEnum.MonitorPointTypeEnum monitorEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt((Integer) paramMap.get("monitortype"));
        List<Map<String, Object>> listData = new ArrayList<>();
        if (monitorEnum == null) {
            return listData;
        }
        List<Map<String, Object>> outPuts = new ArrayList<>();
        switch (monitorEnum) {
            case WasteWaterEnum:
            case RainEnum:
                outPuts = waterOutputInfoMapper.getWaterOutPutPollutants(paramMap);
                break;
            case SmokeEnum:
            case WasteGasEnum:
                outPuts = gasOutPutInfoMapper.getGasOutPutPollutants(paramMap);
                break;
        }
        return outPuts;
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 1:15
     * @Description: 根据监测点类型和自定义参数获取各企业下各排口监测的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllOutputDgimnAndPollutantInfosByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> outputs = new ArrayList<>();
        if (paramMap.get("monitorpointtype") != null && !"".equals(paramMap.get("monitorpointtype").toString())) {
            int type = Integer.parseInt(paramMap.get("monitorpointtype").toString());
            if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//废气
                outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//烟气
                outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {//废水
                paramMap.put("outputtype", 1);
                outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//雨水
                paramMap.put("outputtype", 3);
                outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
                outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
                outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
                outputs = airMonitorStationMapper.getAirMonitorStationDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {//微站
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质
                outputs = waterStationMapper.getWaterStationDgimnAndPollutantInfosByParam(paramMap);
            } else {
                outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
            }
        }
        return outputs;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/23 13:51
     * @Description: 条件查询浓度和排放量突增的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorAndPollutants(Map<String, Object> paramMap) {
        CommonTypeEnum.MonitorPointTypeEnum monitorEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt((Integer) paramMap.get("monitortype"));
        List<Map<String, Object>> listData = new ArrayList<>();
        if (monitorEnum == null) {
            return listData;
        }
        List<Map<String, Object>> outPuts = new ArrayList<>();
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (notothertypes.contains((Integer) paramMap.get("monitortype"))) {
            switch (monitorEnum) {
                case WasteWaterEnum:  //废水浓度或者排放量突变预警
                    outPuts = waterOutputInfoMapper.getPollutionWaterOutPutPollutants(paramMap);
                    break;
                case SmokeEnum:
                case WasteGasEnum: //废气浓度或者排放量突变预警
                    outPuts = gasOutPutInfoMapper.getPollutionGasOutPutPollutants(paramMap);
                    break;
                case RainEnum: //雨水浓度突变预警
                    outPuts = waterOutputInfoMapper.getPollutionWaterOutPutPollutants(paramMap);
                    break;
                case AirEnum:  //空气浓度突变预警
                    outPuts = airMonitorStationMapper.getAirStationPollutants(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:    //厂界恶臭浓度突变预警
                    outPuts = unorganizedMonitorPointInfoMapper.getUnorganizedPollutionOutPutPollutants(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站浓度突变预警
                    outPuts = unorganizedMonitorPointInfoMapper.getUnorganizedPollutionOutPutPollutants(paramMap);
                    break;
                case WaterQualityEnum: //水质浓度突变预警
                    outPuts = waterStationMapper.getWaterStationPollutants(paramMap);
                    break;
            }
        } else {
            outPuts = otherMonitorPointMapper.getOtherMonitorPollutionOutPutPollutants(paramMap);
        }

        return outPuts;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/11 17:30
     * @Description: 获取单个突增污染物排放信息
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPollutantUpRushDischargeInfo(String starttimeInfo, String endtimeInfo, int remindtype, String mn, String pollutantCode, Integer collectiontype) {
        Map<String, Object> resultMap = new HashMap<>();
        Date starttime;
        Date endtime;
        String collection = hourCollection;
        String datalistkey = "HourDataList";
        //默认查询小时浓度数据，2为查询分钟浓度数据
        if (collectiontype != null && collectiontype == 2) {
            datalistkey = "MinuteDataList";
            collection = minuteCollection;
        }
        if (starttimeInfo.length() < 11) {
            starttime = DataFormatUtil.parseDate(starttimeInfo + " 00:00:00");
            endtime = DataFormatUtil.parseDate(endtimeInfo + " 23:59:59");
        } else {
            starttime = DataFormatUtil.parseDate(starttimeInfo);
            endtime = DataFormatUtil.parseDate(endtimeInfo);
        }
        Criteria criteria = Criteria.where("DataGatherCode").is(mn)
                .and("PollutantCode").is(pollutantCode);
        if (starttime != null && endtime != null) {
            criteria.and("MonitorTime").gte(starttime).lte(endtime);
        }
        List<Document> mappedResults = new ArrayList<>();
        String valueKeyName = "";
        if (remindtype == FlowChangeEnum.getCode()) {
            valueKeyName = "CorrectedFlow";
            Fields fields = fields("DataGatherCode", "MonitorTime", "HourFlowDataList.PollutantCode", "HourFlowDataList.CorrectedFlow", "HourFlowDataList.ChangeMultiple", "HourFlowDataList.IsSuddenChange", "_id");
            mappedResults = mongoTemplate.aggregate(newAggregation(
                    unwind("HourFlowDataList"),
                    project(fields),
                    match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), "HourFlowData", Document.class).getMappedResults();
        } else if (remindtype == ConcentrationChangeEnum.getCode()) {
            valueKeyName = "AvgStrength";
            Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", datalistkey + ".AvgStrength", datalistkey + ".ChangeMultiple", datalistkey + ".IsSuddenChange", "_id");
            mappedResults = mongoTemplate.aggregate(newAggregation(
                    unwind(datalistkey),
                    project(fields),
                    match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), collection, Document.class).getMappedResults();
        }
        if (mappedResults.size() == 0) {
            return resultMap;
        }

        if (remindtype == ConcentrationChangeEnum.getCode() && collectiontype != null && collectiontype == 2) {
            setOnePollutantMinuteSuddenChange(resultMap, mappedResults, valueKeyName);
        } else {
            setOnePollutantHourSuddenChange(resultMap, mappedResults, valueKeyName);
        }
        return resultMap;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/12 16:19
     * @Description: 条件查询排口下各个污染物排放量信息列表
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAbruptPollutantsDischargeInfoByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> outputs = getMonitorAndPollutants(paramMap);
        if (outputs.size() == 0) {
            return result;
        }
        String mn = paramMap.get("mn").toString();
        int remindtype = (int) paramMap.get("remindtype");
        Date starttime;
        Date endtime;
        String collectiontype = paramMap.get("collectiontype") == null ? "1" : paramMap.get("collectiontype").toString();
        String collection = hourCollection;
        String datalistkey = "HourDataList";
        //默认查询小时浓度数据，2为查询分钟浓度数据
        if ("2".equals(collectiontype)) {
            datalistkey = "MinuteDataList";
            collection = minuteCollection;
        }
        if (paramMap.get("starttime").toString().length() < 11) {
            starttime = DataFormatUtil.parseDate(paramMap.get("starttime") + " 00:00:00");
            endtime = DataFormatUtil.parseDate(paramMap.get("endtime") + " 23:59:59");
        } else {
            starttime = DataFormatUtil.parseDate(paramMap.get("starttime").toString());
            endtime = DataFormatUtil.parseDate(paramMap.get("endtime").toString());
        }
        Set<String> pollutantCodes = outputs.stream().filter(m -> m.get("PollutantCode") != null).map(m -> m.get("PollutantCode").toString()).collect(Collectors.toSet());
        List<Document> mappedResults = new ArrayList<>();
        //查询条件
        Criteria criteria = Criteria.where("DataGatherCode").is(mn)
                .and("PollutantCode").in(pollutantCodes);
        if (starttime != null && endtime != null) {
            criteria.and("MonitorTime").gte(starttime).lte(endtime);
        }
        String valueKeyName = "";
        if (remindtype == FlowChangeEnum.getCode()) {  //排放量
            valueKeyName = "CorrectedFlow";
            Fields fields = fields("DataGatherCode", "MonitorTime", "HourFlowDataList.PollutantCode", "HourFlowDataList.CorrectedFlow", "HourFlowDataList.ChangeMultiple", "HourFlowDataList.IsSuddenChange", "_id");
            mappedResults = mongoTemplate.aggregate(newAggregation(
                    unwind("HourFlowDataList"),
                    project(fields),
                    match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), "HourFlowData", Document.class).getMappedResults();
        } else if (remindtype == ConcentrationChangeEnum.getCode()) {
            valueKeyName = "AvgStrength";
            if (paramMap.get("monitortype") != null && CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() ==
                    Integer.parseInt(paramMap.get("monitortype").toString())) {
                valueKeyName = "AvgConvertStrength";
            }
            //浓度
            Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", datalistkey + ".AvgStrength", datalistkey + ".AvgConvertStrength", datalistkey + ".ChangeMultiple", datalistkey + ".IsSuddenChange", "_id");
            mappedResults = mongoTemplate.aggregate(newAggregation(
                    unwind(datalistkey),
                    project(fields),
                    match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), collection, Document.class).getMappedResults();
        }
        if (mappedResults.size() == 0) {
            return result;
        }
        Object PollutionName = outputs.get(0).get("PollutionName");
        Object shortername = outputs.get(0).get("shortername");
        Object AreaName = outputs.get(0).get("RiskAreaName");
        Object outputname = outputs.get(0).get("OutPutName");
        String pollutantCode;
        Integer IsHasConvertData;
        Map<String, Integer> codeAndIs = new HashMap<>();
        if (paramMap.get("monitortype") != null && CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() ==
                Integer.parseInt(paramMap.get("monitortype").toString())) {
            Map<String, Object> paramMap1 = new HashMap<>();
            paramMap1.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap1);
            for (Map<String, Object> pollutant : pollutants) {
                IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
                codeAndIs.put(pollutant.get("code").toString(), IsHasConvertData);
            }

        }

        if (remindtype == ConcentrationChangeEnum.getCode() && "2".equals(collectiontype)) {
            setPollutantMinuteSuddenChange(result, mappedResults, valueKeyName, PollutionName, shortername, AreaName, outputname, codeAndIs);
        } else {
            setPollutantHourSuddenChange(result, mappedResults, valueKeyName, PollutionName, shortername, AreaName, outputname, codeAndIs);
        }
        Comparator<Map<String, Object>> comparator = Comparator.comparing(a -> a.get("monitortime").toString());
        result.sort(comparator);
        return result;
    }

    private void setPollutantHourSuddenChange(List<Map<String, Object>> result, List<Document> mappedResults, String valueKeyName, Object pollutionName, Object shortername, Object areaName, Object outputname, Map<String, Integer> codeAndIs) {
        String pollutantCode;
        Map<String, List<Document>> mapData = mappedResults.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDH(m.getDate("MonitorTime"))));
        for (Map.Entry<String, List<Document>> entry : mapData.entrySet()) {
            Map<String, Object> mapInfo = new HashMap<>();
            String time = entry.getKey();
            List<Map<String, Object>> hourdatalist = new ArrayList<>();
            List<Document> value = entry.getValue();
            for (Document document : value) {
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                if (valueKeyName.indexOf("Strength") >= 0) {
                    if (codeAndIs.containsKey(pollutantCode) && codeAndIs.get(pollutantCode) == 1) {
                        valueKeyName = "AvgConvertStrength";
                    } else {
                        valueKeyName = "AvgStrength";
                    }
                }
                pollutantMap.put("pollutantcode", pollutantCode);
                pollutantMap.put("avgstrength", document.get(valueKeyName));
                pollutantMap.put("ischangewarn", document.get("IsSuddenChange"));
                hourdatalist.add(pollutantMap);
            }
            mapInfo.put("monitortime", time);
            mapInfo.put("outputname", outputname);
            mapInfo.put("pollutionname", pollutionName);
            mapInfo.put("RiskAreaName", (shortername == null ? "" : shortername.toString()) + "-" + (areaName == null ? "" : areaName.toString()));
            mapInfo.put("hourdatalist", hourdatalist);
            result.add(mapInfo);
        }
    }


    private void setPollutantMinuteSuddenChange(List<Map<String, Object>> result, List<Document> mappedResults, String valueKeyName, Object pollutionName, Object shortername, Object areaName, Object outputname, Map<String, Integer> codeAndIs) {
        String pollutantCode;
        Map<String, List<Document>> mapData = mappedResults.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("MonitorTime"))));
        for (Map.Entry<String, List<Document>> entry : mapData.entrySet()) {
            Map<String, Object> mapInfo = new HashMap<>();
            String time = entry.getKey();
            List<Map<String, Object>> hourdatalist = new ArrayList<>();
            List<Document> value = entry.getValue();
            for (Document document : value) {
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                if (valueKeyName.indexOf("Strength") >= 0) {
                    if (codeAndIs.containsKey(pollutantCode) && codeAndIs.get(pollutantCode) == 1) {
                        valueKeyName = "AvgConvertStrength";
                    } else {
                        valueKeyName = "AvgStrength";
                    }
                }
                pollutantMap.put("pollutantcode", pollutantCode);
                pollutantMap.put("avgstrength", document.get(valueKeyName));
                pollutantMap.put("ischangewarn", document.get("IsSuddenChange"));
                hourdatalist.add(pollutantMap);
            }
            mapInfo.put("monitortime", time);
            mapInfo.put("outputname", outputname);
            mapInfo.put("pollutionname", pollutionName);
            mapInfo.put("RiskAreaName", (shortername == null ? "" : shortername.toString()) + "-" + (areaName == null ? "" : areaName.toString()));
            mapInfo.put("hourdatalist", hourdatalist);
            result.add(mapInfo);
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 1:15
     * @Description: 根据监测点类型和自定义参数获取各企业下各排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public List<Map<String, Object>> getAllPollutionOutputDgimnInfoByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> outputs = new ArrayList<>();
        if (paramMap.get("monitorpointtype") != null && !"".equals(paramMap.get("monitorpointtype").toString())) {
            int type = Integer.parseInt(paramMap.get("monitorpointtype").toString());
            if (type == (CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode())) {//废气
                outputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode())) {//烟气
                outputs = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode())) {//废水
                paramMap.put("outputtype", "water");
                outputs = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode())) {//雨水
                paramMap.put("outputtype", "rain");
                outputs = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode())) {//厂界小型站
                outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode())) {
                outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);//扬尘
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode())) {
                outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);//气象
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode())) {//厂界恶臭
                outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//空气站
                outputs = airMonitorStationMapper.getALLAirStationInfoByParamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode())) {//恶臭
                outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode())) {//微站
                outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode())) {//voc
                outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            } else if (type == (CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode())) {//水质
                outputs = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
            } else if (type == CommonTypeEnum.MonitorPointTypeEnum.StinkEnum.getCode()) {//环境恶臭+厂界恶臭
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                outputs = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                outputs.addAll(otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap));
            } else {
                outputs = otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            }
        }
        return outputs;
    }

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 下午 7:16
     * @Description: 根据自定义参数获取排口浓度阈值预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getConcentrationThresholdEarlyListData
    (Map<String, Object> paramMap, String starttime, String endtime, List<String> datatype, Integer
            monitorpointtype, String usercode, Integer pagenum, Integer pagesize) {
        String dataflag = paramMap.get("dataflag") != null ? paramMap.get("dataflag").toString() : "";
        List<Map<String, Object>> allpoints = getAllPollutionOutputDgimnInfoByParam(paramMap);
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        Set<String> mnlist = new HashSet<String>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> codeunit = new HashMap<>();
        boolean flag = false;
        if ("".equals(dataflag)) {
            if (paramMap.get("pollutantname") != null && !"".equals(paramMap.get("pollutantname").toString())) {
                flag = true;
            }
            if (paramMap.get("codes") != null && !"".equals(paramMap.get("codes").toString())) {
                flag = true;
            }
        } else {
            if (paramMap.get("pollutantcode") != null && !"".equals(paramMap.get("pollutantcode").toString())) {
                flag = true;
            }
        }
        for (Map<String, Object> obj : allpoints) {
            if (obj.get("dgimn") != null) {
                mnlist.add(obj.get("dgimn").toString());
            }
        }
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                codeunit.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
            }
        }
        Date startDate;
        Date endDate;
        if ("".equals(dataflag)) {
            if (starttime.length() == 13 && endtime.length() == 13) {   //小时
                startDate = DataFormatUtil.parseDate(starttime + ":00:00");
                endDate = DataFormatUtil.parseDate(endtime + ":59:59");
            } else {        //天
                startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
                endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
            }
        } else {
            startDate = DataFormatUtil.parseDate(starttime);
            endDate = DataFormatUtil.parseDate(endtime);
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (pagenum != null && pagesize != null) {
            pageEntity.setPageNum(pagenum);
            pageEntity.setPageSize(pagesize);
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        if (flag == true) {
            query.addCriteria(Criteria.where("PollutantCode").in(codelist));
        }
        query.addCriteria(Criteria.where("AlarmLevel").is(0));//预警数据
        //异常  if判断  若用户ID存在于已读用户数组中 则返回isread 1
        ArrayOperators.In isContainsUser = ArrayOperators.In.arrayOf("$ReadUserIds").containsValue(usercode);
        ConditionalOperators.Cond condOperation = ConditionalOperators.when(isContainsUser)
                .thenValueOf("1")
                .otherwise("0");
        DataTypeOperators.Type $ReadUserIds = DataTypeOperators.typeOf("$ReadUserIds");
        ConditionalOperators.Cond condOperation1 = ConditionalOperators.when(ComparisonOperators.Eq.valueOf($ReadUserIds).equalToValue("array"))
                .thenValueOf(condOperation)
                .otherwise("0");
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate)));
        if (flag == true) {
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(codelist)));
        }
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        List<String> datatypelist = new ArrayList<>();
        if (datatype.size() > 0) {
            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//空气站只有小时日数据
                for (String str : datatype) {
                    if ("1".equals(str)) {
                        datatypelist.add("HourData");
                    } else if ("2".equals(str)) {
                        datatypelist.add("DayData");
                    }
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            } else {
                for (String str : datatype) {
                    datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        } else {
            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//空气站只有小时日数据
                datatypelist.add("HourData");
                datatypelist.add("DayData");
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        }
        query.addCriteria(Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "EarlyWarnTime"));
        //总条数
        long totalCount = mongoTemplate.count(query, earlyWarnData_db);
        pageEntity.setTotalCount(totalCount);
        //排序条件
        String orderBy = "DataGatherCode,EarlyWarnTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "EarlyWarnTime", "DataType", "AlarmType", "AlarmLevel", "MonitorValue").and(condOperation1).as("isread"));
        //插入分页、排序条件
        if (pagenum != null && pagesize != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, earlyWarnData_db, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                String isread = document.getString("isread");
                Object standardmaxvalue = null;
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            break;
                        }
                    }
                }
                for (Map<String, Object> objmap : allpoints) {
                    if (objmap.get("dgimn") != null && mn.equals(objmap.get("dgimn"))) {//当MN号相同时
                        Map<String, Object> result = getPointInfoByPointtype(objmap, monitorpointtype);
                        result.put("isread", isread);
                        result.put("remindtype", CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode());
                        result.put("multiple", document.getString("OverMultiple"));
                        result.put("standardmaxvalue", standardmaxvalue);
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantunit", codeunit.get(pollutantcode) != null ? codeunit.get(pollutantcode) : "");
                        result.put("pollutantcode", pollutantcode);
                        result.put("monitorpointtype", monitorpointtype);
                        result.put("dgimn", objmap.get("dgimn"));
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        result.put("concenalarmmaxvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                        if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                            result.put("datatypecode", "1");
                            result.put("datatype", RealTimeDataEnum.getName());
                            result.put("datatypename", "实时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("EarlyWarnTime")));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", "2");
                            result.put("datatype", MinuteDataEnum.getName());
                            result.put("datatypename", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("EarlyWarnTime")));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {
                                result.put("datatypecode", "1");
                                result.put("datatype", HourDataEnum.getName());
                                result.put("datatypename", "小时数据");
                                result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("EarlyWarnTime")));
                            } else {
                                result.put("datatypecode", "3");
                                result.put("datatype", HourDataEnum.getName());
                                result.put("datatypename", "小时数据");
                                result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("EarlyWarnTime")));
                            }
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {
                                result.put("datatypecode", "2");
                                result.put("datatype", DayDataEnum.getName());
                                result.put("datatypename", "日数据");
                                result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("EarlyWarnTime")));
                            } else {
                                result.put("datatypecode", "4");
                                result.put("datatype", DayDataEnum.getName());
                                result.put("datatypename", "日数据");
                                result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("EarlyWarnTime")));
                            }
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        Map<String, Object> resultmap = new HashMap<>();
        resultmap.put("total", totalCount);
        resultmap.put("datalist", resultlist);
        return resultmap;
    }


    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 2:08
     * @Description: 根据监测点类型和自定义参数获取异常报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getExceptionListDataByParam(Map<String, Object> paramMap, String
            starttime, String endtime, List<String> datatypes, List<String> exceptiontype, Integer monitorpointtype, String usercode, Integer pagenum, Integer pagesize) {
        String dataflag = paramMap.get("dataflag") != null ? paramMap.get("dataflag").toString() : "";
        List<Map<String, Object>> allpoints = getAllPollutionOutputDgimnInfoByParam(paramMap);
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        Set<String> mnlist = new HashSet<String>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> codeunit = new HashMap<>();
        boolean flag = false;
        if ("".equals(dataflag)) {
            if (paramMap.get("pollutantname") != null && !"".equals(paramMap.get("pollutantname").toString())) {
                flag = true;
            }
            if (paramMap.get("codes") != null && !"".equals(paramMap.get("codes").toString())) {
                flag = true;
            }
        } else {
            if (paramMap.get("pollutantcode") != null && !"".equals(paramMap.get("pollutantcode").toString())) {
                flag = true;
            }
        }
        for (Map<String, Object> obj : allpoints) {
            if (obj.get("dgimn") != null) {
                mnlist.add(obj.get("dgimn").toString());
            }
        }
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                codeunit.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
            }
        }
        Date startDate;
        Date endDate;
        if ("".equals(dataflag)) {
            if (starttime.length() == 13 && endtime.length() == 13) {   //小时
                startDate = DataFormatUtil.parseDate(starttime + ":00:00");
                endDate = DataFormatUtil.parseDate(endtime + ":59:59");
            } else {        //天
                startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
                endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
            }
        } else {
            startDate = DataFormatUtil.parseDate(starttime);
            endDate = DataFormatUtil.parseDate(endtime);
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (pagenum != null && pagesize != null) {
            pageEntity.setPageNum(pagenum);
            pageEntity.setPageSize(pagesize);
        }
        //构建Mongdb查询条件-总条数查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        if (flag == true) {
            query.addCriteria(Criteria.where("PollutantCode").in(codelist));
        }
        //构建Mongdb查询条件 --列表查询条件
        //异常  if判断  若用户ID存在于已读用户数组中 则返回isread 1
        ArrayOperators.In isContainsUser = ArrayOperators.In.arrayOf("$ReadUserIds").containsValue(usercode);
        ConditionalOperators.Cond condOperation = ConditionalOperators.when(isContainsUser)
                .thenValueOf("1")
                .otherwise("0");
        DataTypeOperators.Type $ReadUserIds = DataTypeOperators.typeOf("$ReadUserIds");
        ConditionalOperators.Cond condOperation1 = ConditionalOperators.when(ComparisonOperators.Eq.valueOf($ReadUserIds).equalToValue("array"))
                .thenValueOf(condOperation)
                .otherwise("0");
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("ExceptionTime").gte(startDate).lte(endDate)));
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        if (flag == true) {
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(codelist)));
        }
        List<String> datatypelist = new ArrayList<>();
        //判断要查询的数据类型  空气只有小时、日数据
        if (datatypes.size() > 0) {
            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//空气站只有小时日数据
                for (String str : datatypes) {
                    if ("1".equals(str)) {
                        datatypelist.add("HourData");
                    } else if ("2".equals(str)) {
                        datatypelist.add("DayData");
                    }
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            } else {
                for (String str : datatypes) {
                    datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        } else {
            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//空气站只有小时日数据
                datatypelist.add("HourData");
                datatypelist.add("DayData");
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        }
        if (exceptiontype != null && exceptiontype.size() > 0) {
            query.addCriteria(Criteria.where("ExceptionType").in(exceptiontype));
            operations.add(Aggregation.match(Criteria.where("ExceptionType").in(exceptiontype)));
        }
        query.addCriteria(Criteria.where("ExceptionTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "ExceptionTime"));
        //总条数
        long totalCount = mongoTemplate.count(query, exceptionData_db);
        pageEntity.setTotalCount(totalCount);

        String orderBy = "DataGatherCode,ExceptionTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "ExceptionTime", "DataType", "ExceptionType", "MonitorValue").and(condOperation1).as("isread"));
        //插入分页、排序条件
        if (pagenum != null && pagesize != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, exceptionData_db, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        //遍历结果
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                String isread = document.getString("isread");
                Object standardmaxvalue = null;
                String exceptionvalue = "";
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            if (objmap.get("ExceptionMinValue") != null && !"".equals(objmap.get("ExceptionMinValue").toString())) {
                                exceptionvalue = exceptionvalue + "<" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMinValue").toString());
                            }
                            if (objmap.get("ExceptionMaxValue") != null && !"".equals(objmap.get("ExceptionMaxValue").toString())) {
                                if (!"".equals(exceptionvalue)) {
                                    exceptionvalue = exceptionvalue + "或>" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                                } else {
                                    exceptionvalue = exceptionvalue + ">" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                                }
                            }
                            break;
                        }
                    }
                }
                for (Map<String, Object> objmap : allpoints) {
                    if (objmap.get("dgimn") != null && mn.equals(objmap.get("dgimn"))) {//当MN号相同时
                        Map<String, Object> result = getPointInfoByPointtype(objmap, monitorpointtype);
                        result.put("remindtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());
                        result.put("exceptiontype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType")));
                        if ((document.getString("ExceptionType")).equals(CommonTypeEnum.ExceptionTypeEnum.OverExceptionEnum.getCode())) {
                            result.put("exceptionvalue", exceptionvalue);
                        } else {
                            result.put("exceptionvalue", "");
                        }
                        result.put("isread", isread);
                        result.put("multiple", document.getString("OverMultiple"));
                        result.put("standardmaxvalue", standardmaxvalue);
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantunit", codeunit.get(pollutantcode) != null ? codeunit.get(pollutantcode) : "");
                        result.put("pollutantcode", pollutantcode);
                        result.put("monitorpointtype", monitorpointtype);
                        result.put("dgimn", objmap.get("dgimn"));
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                            result.put("datatypecode", "1");
                            result.put("datatype", RealTimeDataEnum.getName());
                            result.put("datatypename", "实时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", "2");
                            result.put("datatype", MinuteDataEnum.getName());
                            result.put("datatypename", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("ExceptionTime")));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//当为空气小时数据
                                result.put("datatypecode", "1");
                                result.put("datatype", HourDataEnum.getName());
                                result.put("datatypename", "小时数据");
                                result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("ExceptionTime")));
                            } else {
                                result.put("datatypecode", "3");
                                result.put("datatype", HourDataEnum.getName());
                                result.put("datatypename", "小时数据");
                                result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("ExceptionTime")));
                            }
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//当为空气日数据
                                result.put("datatypecode", "2");
                                result.put("datatype", DayDataEnum.getName());
                                result.put("datatypename", "日数据");
                                result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("ExceptionTime")));
                            } else {
                                result.put("datatypecode", "4");
                                result.put("datatype", DayDataEnum.getName());
                                result.put("datatypename", "日数据");
                                result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("ExceptionTime")));
                            }
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        Map<String, Object> resultmap = new HashMap<>();
        resultmap.put("total", totalCount);
        resultmap.put("datalist", resultlist);
        return resultmap;
    }


    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 3:45
     * @Description: 根据监测点类型和自定义参数获取超标（超限）报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getOverListDataByParam(Map<String, Object> paramMap, String
            starttime, String endtime, List<String> datatypes, Integer monitorpointtype, String usercode, Integer pagenum, Integer pagesize) {
        String dataflag = paramMap.get("dataflag") != null ? paramMap.get("dataflag").toString() : "";
        List<Map<String, Object>> allpoints = getAllPollutionOutputDgimnInfoByParam(paramMap);
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        Set<String> mnlist = new HashSet<String>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> codeunit = new HashMap<>();
        boolean flag = false;
        if ("".equals(dataflag)) {
            if (paramMap.get("pollutantname") != null && !"".equals(paramMap.get("pollutantname").toString())) {
                flag = true;
            }
            if (paramMap.get("codes") != null && !"".equals(paramMap.get("codes").toString())) {
                flag = true;
            }
        } else {
            if (paramMap.get("pollutantcode") != null && !"".equals(paramMap.get("pollutantcode").toString())) {
                flag = true;
            }
        }
        for (Map<String, Object> obj : allpoints) {
            if (obj.get("dgimn") != null) {
                mnlist.add(obj.get("dgimn").toString());
            }
        }
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                codeunit.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
            }
        }
        Date startDate;
        Date endDate;
        if ("".equals(dataflag)) {
            if (starttime.length() == 13 && endtime.length() == 13) {   //小时
                startDate = DataFormatUtil.parseDate(starttime + ":00:00");
                endDate = DataFormatUtil.parseDate(endtime + ":59:59");
            } else {        //天
                startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
                endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
            }
        } else {
            startDate = DataFormatUtil.parseDate(starttime);
            endDate = DataFormatUtil.parseDate(endtime);
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (pagenum != null && pagesize != null) {
            pageEntity.setPageNum(pagenum);
            pageEntity.setPageSize(pagesize);
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        if (flag == true) {
            query.addCriteria(Criteria.where("PollutantCode").in(codelist));
        }
        //异常  if判断  若用户ID存在于已读用户数组中 则返回isread 1
        ArrayOperators.In isContainsUser = ArrayOperators.In.arrayOf("$ReadUserIds").containsValue(usercode);
        ConditionalOperators.Cond condOperation = ConditionalOperators.when(isContainsUser)
                .thenValueOf("1")
                .otherwise("0");
        DataTypeOperators.Type $ReadUserIds = DataTypeOperators.typeOf("$ReadUserIds");
        ConditionalOperators.Cond condOperation1 = ConditionalOperators.when(ComparisonOperators.Eq.valueOf($ReadUserIds).equalToValue("array"))
                .thenValueOf(condOperation)
                .otherwise("0");
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("OverTime").gte(startDate).lte(endDate)));
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        if (flag == true) {
            operations.add(Aggregation.match(Criteria.where("PollutantCode").in(codelist)));
        }
        List<String> datatypelist = new ArrayList<>();
        if (datatypes.size() > 0) {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站只有小时日数据
                for (String str : datatypes) {
                    if ("1".equals(str)) {
                        datatypelist.add("HourData");
                    } else if ("2".equals(str)) {
                        datatypelist.add("DayData");
                    }
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            } else {
                for (String str : datatypes) {
                    datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
                }
                operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
                query.addCriteria(Criteria.where("DataType").in(datatypelist));
            }
        }
        /*operations.add(Aggregation.match(Criteria.where("AlarmLevel").gte(1)));
        query.addCriteria(Criteria.where("AlarmLevel").gte(1));*/
        query.addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "OverTime"));
        //总条数
        long totalCount = mongoTemplate.count(query, overData_db);
        pageEntity.setTotalCount(totalCount);
        String orderBy = "DataGatherCode,OverTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质类型返回水质级别
            operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "OverTime", "AlarmType", "AlarmLevel", "DataType", "MonitorValue", "IsOverStandard", "OverMultiple", "WaterLevel").and(condOperation1).as("isread"));
        } else {
            operations.add(Aggregation.project("DataGatherCode", "PollutantCode", "OverTime", "AlarmType", "AlarmLevel", "DataType", "MonitorValue", "IsOverStandard", "OverMultiple").and(condOperation1).as("isread"));
        }
        //插入分页、排序条件
        if (pagenum != null && pagesize != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, overData_db, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        if (documents.size() > 0) {//判断查询数据是否为空
            Map<String, Object> waterqualitymap = new HashMap<>();
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质类型返回水质级别
                List<Map<String, Object>> qulitylevels = waterStationMapper.getAllWaterQualityLevel();
                if (qulitylevels != null && qulitylevels.size() > 0) {
                    for (Map<String, Object> qulity : qulitylevels) {
                        waterqualitymap.put(qulity.get("code").toString(), qulity.get("name"));
                    }
                }
            }
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                String isread = document.getString("isread");
                Object standardmaxvalue = null;
                Object standardminvalue = null;
                Object alarmtype = null;
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                            alarmtype = objmap.get("AlarmType");
                            standardminvalue = objmap.get("StandardMinValue");
                            standardmaxvalue = objmap.get("StandardMaxValue");
                            break;
                        }
                    }
                }
                for (Map<String, Object> objmap : allpoints) {
                    if (objmap.get("dgimn") != null && mn.equals(objmap.get("dgimn"))) {//当MN号相同时
                        Map<String, Object> result = getPointInfoByPointtype(objmap, monitorpointtype);
                        result.put("isread", isread);
                        result.put("remindtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode());
                        result.put("multiple", document.getDouble("OverMultiple") != null ? DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(document.getDouble("OverMultiple") * 100)) + "%" : "");
                        result.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
                        result.put("alarmlevelcode", document.get("AlarmLevel"));
                        result.put("standardmaxvalue", "");
                        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质类型返回水质级别
                            result.put("waterquality", "");
                            if (waterqualitymap != null && document.get("WaterLevel") != null) {
                                result.put("waterquality", waterqualitymap.get(document.getString("WaterLevel")));
                            }
                        }
                        if (alarmtype != null) {
                            if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype.toString())) {//上限报警
                                if (standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                }
                            } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype.toString())) {//下限报警
                                if (standardminvalue != null && !"".equals(standardminvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()));
                                }
                            } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(alarmtype.toString())) {//区间报警
                                if (standardminvalue != null && !"".equals(standardminvalue.toString()) && standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                    result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()) + "-" + DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                }
                            }
                        }
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantcode", pollutantcode);
                        result.put("pollutantunit", codeunit.get(pollutantcode) != null ? codeunit.get(pollutantcode) : "");
                        result.put("monitorpointtype", monitorpointtype);
                        result.put("dgimn", objmap.get("dgimn"));
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        if (document.getBoolean("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {//判断是否超标报警
                            //判断是否即超标又超限
                            if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                result.put("alarmlevel", (codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "、" : "") + "超标报警");
                                result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                            } else {
                                result.put("alarmlevel", "超标报警");
                                result.put("alarmlevelvalue", "");
                            }

                        } else {
                            if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                result.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) : "");
                                result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                            }
                        }
                        if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                            result.put("datatypecode", "1");
                            result.put("datatype", RealTimeDataEnum.getName());
                            result.put("datatypename", "实时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", "2");
                            result.put("datatype", MinuteDataEnum.getName());
                            result.put("datatypename", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("OverTime")));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//当为空气小时数据
                                result.put("datatypecode", "1");
                                result.put("datatype", HourDataEnum.getName());
                                result.put("datatypename", "小时数据");
                                result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("OverTime")));
                            } else {
                                result.put("datatypecode", "3");
                                result.put("datatype", HourDataEnum.getName());
                                result.put("datatypename", "小时数据");
                                result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("OverTime")));
                            }
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//当为空气日数据
                                result.put("datatypecode", "2");
                                result.put("datatype", DayDataEnum.getName());
                                result.put("datatypename", "日数据");
                                result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("OverTime")));
                            } else {
                                result.put("datatypecode", "4");
                                result.put("datatype", DayDataEnum.getName());
                                result.put("datatypename", "日数据");
                                result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("OverTime")));
                            }
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        Map<String, Object> resultmap = new HashMap<>();
        resultmap.put("total", totalCount);
        resultmap.put("datalist", resultlist);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 3:45
     * @Description: 根据监测点类型返回对应的监测点返回信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private Map<String, Object> getPointInfoByPointtype(Map<String, Object> objmap, Integer monitorpointtype) {
        Map<String, Object> result = new HashMap<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteWaterEnum://废水
                result.put("pollutionname", objmap.get("pollutionname"));
                result.put("outputname", objmap.get("outputname"));
                result.put("outputid", objmap.get("pk_id"));
                break;
            case WasteGasEnum: //废气
            case SmokeEnum: //废气
                result.put("pollutionname", objmap.get("pollutionname"));
                result.put("outputname", objmap.get("outputname"));
                result.put("outputid", objmap.get("pk_id"));
                break;
            case RainEnum: //雨水
                result.put("pollutionname", objmap.get("pollutionname"));
                result.put("outputname", objmap.get("outputname"));
                result.put("outputid", objmap.get("pk_id"));
                break;
            case AirEnum: //大气
                result.put("monitorpointname", objmap.get("monitorpointname"));
                result.put("monitorpointid", objmap.get("pk_id"));
                break;
            case EnvironmentalStinkEnum: //恶臭
            case EnvironmentalDustEnum: //扬尘
            case MicroStationEnum: //微站
                result.put("monitorpointname", objmap.get("monitorpointname"));
                result.put("monitorpointid", objmap.get("pk_monitorpointid"));
                break;
            case EnvironmentalVocEnum: //voc
                result.put("monitorpointname", objmap.get("monitorpointname"));
                result.put("monitorpointid", objmap.get("pk_monitorpointid"));
                break;
            case FactoryBoundaryStinkEnum: //厂界恶臭
                result.put("pollutionname", objmap.get("pollutionname"));
                result.put("monitorpointname", objmap.get("outputname"));
                result.put("monitorpointid", objmap.get("pk_id"));
                break;
            case FactoryBoundarySmallStationEnum: //厂界小型站
                result.put("pollutionname", objmap.get("pollutionname"));
                result.put("monitorpointname", objmap.get("outputname"));
                result.put("monitorpointid", objmap.get("pk_id"));
                break;
            case WaterQualityEnum: //水质
                result.put("monitorpointname", objmap.get("monitorpointname"));
                result.put("monitorpointid", objmap.get("pk_id"));
                break;
            case StorageTankAreaEnum: //贮罐
                result.put("pollutionname", objmap.get("pollutionname"));
                result.put("monitorpointname", objmap.get("outputname"));
                result.put("monitorpointid", objmap.get("pk_id"));
                break;
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 3:45
     * @Description: 换算报警级别
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private Object getAlarmLevelValue(String mn, String pollutantcode, Object
            alarmLevel, List<Map<String, Object>> outputlist) {
        Object alarmLevelvalue = "";
        for (Map<String, Object> objmap : outputlist) {
            if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {//当污染物相同时
                    if (objmap.get("FK_AlarmLevelCode") != null && (alarmLevel.toString()).equals(objmap.get("FK_AlarmLevelCode").toString())) {
                        alarmLevelvalue = objmap.get("ConcenAlarmMaxValue");
                    }
                }
            }
        }
        return alarmLevelvalue;
    }


    /**
     * @author: xsm
     * @date: 2019/7/13 0013 下午 3:45
     * @Description: 根据监测点类型获取异常报警表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForException(Integer type) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = null;
        String[] titlefiled = null;
        if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode())) {//废气,废水，雨水
            titlename = new String[]{"企业名称", "排口名称", "数据类型", "监测时间", "污染物", "监测值", "异常标准值", "异常类型"};
            titlefiled = new String[]{"pollutionname", "outputname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "exceptionvalue", "exceptiontype"};
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode())) {//厂界小型站
            titlename = new String[]{"企业名称", "监测点名称", "数据类型", "监测时间", "污染物", "监测值", "异常标准值", "异常类型"};
            titlefiled = new String[]{"pollutionname", "monitorpointname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "exceptionvalue", "exceptiontype"};
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode())) {//空气站
            titlename = new String[]{"监测点名称", "数据类型", "监测时间", "污染物", "监测值", "异常标准值", "异常类型"};
            titlefiled = new String[]{"monitorpointname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "exceptionvalue", "exceptiontype"};
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

    /**
     * @author: xsm
     * @date: 2019/7/13 0013 下午 3:56
     * @Description: 根据监测点类型获取超限报警表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForOverAlarm(Integer type) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = null;
        String[] titlefiled = null;
        if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode())) {//废气,废水，雨水
            titlename = new String[]{"企业名称", "排口名称", "数据类型", "监测时间", "污染物", "监测值", "报警限值", "报警级别", "标准值"};
            titlefiled = new String[]{"pollutionname", "outputname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "alarmlevelvalue", "alarmlevel", "standardmaxvalue"};
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode())) {//厂界小型站
            titlename = new String[]{"企业名称", "监测点名称", "数据类型", "监测时间", "污染物", "监测值", "报警限值", "报警级别", "标准值"};
            titlefiled = new String[]{"pollutionname", "monitorpointname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "alarmlevelvalue", "alarmlevel", "standardmaxvalue"};
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode())) {//空气站
            titlename = new String[]{"监测点名称", "数据类型", "监测时间", "污染物", "监测值", "报警限值", "报警级别", "标准值"};
            titlefiled = new String[]{"monitorpointname", "datatypename", "monitortime", "pollutantname", "monitorvalue", "alarmlevelvalue", "alarmlevel", "standardmaxvalue"};
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


    /**
     * @author: xsm
     * @date: 2019/8/02 9:19
     * @Description: 根据自定义参数统计恶臭监测点在线浓度和风向、风速数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countStenchOnlineDataAndWeatherDataByParams(Map<String, Object> paramMap) {
        try {
            //获取参数
            String dgimn = paramMap.get("dgimn").toString();
            String datetype = paramMap.get("datetype").toString();
            String pollutantcode = paramMap.get("pollutantcode").toString();
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            //根据污染物编码和监测点类型去获取对应的污染物信息
            Date startDate = null;
            Date endDate = null;
            if ("hour".equals(datetype)) {//查询小时数据
                startDate = DataFormatUtil.getDateYMDH(starttime);
                endDate = DataFormatUtil.getDateYMDH(endtime);
            } else if ("day".equals(datetype)) {//查询日数据
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
            }
            //获取恶臭点位关联的空气站MN号  用来获取风向风速信息

            //通过MN号获取对应空气站MN号
            List<String> airDgimns = otherMonitorPointMapper.getAirDgimnByStinkMonitorDgimn(dgimn);
            //获取点位下某时段因子浓度
            List<Map<String, Object>> concentrationlist = getMonitorPointConcentrationData(dgimn, datetype, pollutantcode, startDate, endDate);
            List<Map<String, Object>> result = new ArrayList<>();
            if (airDgimns != null && airDgimns.size() > 0) {
                String airmn = airDgimns.get(0).toString();
                //获取点位下各时段的气候信息
                List<Map<String, Object>> weathermap = getMonitorPointWeatherData(airmn, datetype, startDate, endDate);
                //获取以风向分组的因子平均浓度统计数据
                result = countAvgConcentrationGroupByWindDirection(concentrationlist, weathermap, pollutantcode);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/26 7:52
     * @Description: 获取监测点下因子监测浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointConcentrationData(String mn, String timetype, String pollutantcode, Date startDate, Date endDate) {
        //去MongoDB中查询数据
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String collectionname = "";
        String datalistname = "";
        if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(mn));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").is(pollutantcode));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                Map<String, Object> map = new LinkedHashMap<>();
                String monitortime = "";
                if ("hour".equals(timetype)) {//根据类型判断出  是查询小时数据
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    datalistname = "HourDataList";
                } else if ("day".equals(timetype)) {//根据类型判断出  是查询日数据
                    monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    datalistname = "DayDataList";
                }
                List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                Object value = "";
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                        value = dataMap.get("AvgStrength");
                        break;
                    }
                }
                if (!"".equals(value)) {
                    map.put("monitortime", monitortime);
                    map.put("pollutantcode", pollutantcode);
                    map.put("monitorvalue", value);
                    result.add(map);
                }

            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/6/26 7:52
     * @Description: 获取监测点气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointWeatherData(String airmn, String timetype, Date startDate, Date endDate) {
        String collectionname = "";
        String datalistname = "";
        if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
        }
        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        List<Map<String, Object>> datas = MongoDataUtils.getWindDataList(documents, collectionname);
        return datas;
    }

    /**
     * @author: xsm
     * @date: 2019/6/27 13:11
     * @Description: 按风向统计各污染物的平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> countAvgConcentrationGroupByWindDirection(List<Map<String, Object>> concentrationlist, List<Map<String, Object>> weathermap, String pollutantcode) {
        String[] names = DataFormatUtil.directName;
        String[] codes = DataFormatUtil.directCode;
        List<Map<String, Object>> resultlist = new ArrayList<>();
        for (String str : names) {
            List<String> timelist = new ArrayList<>();
            Double windspeed = 0d;
            int num = 0;
            String winddirectioncode = "";
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(str)) {
                    winddirectioncode = codes[i];
                    break;
                }
            }
            for (Map<String, Object> objmap : weathermap) {
                if (str.equals(objmap.get("winddirectionname").toString())) {
                    timelist.add(objmap.get("monitortime").toString());
                    if (objmap.get("windspeed") != null) {
                        num += 1;
                        windspeed = windspeed + Double.parseDouble(objmap.get("windspeed").toString());
                    }
                }
            }
            String formatvalue = "";
            String value = "";
            Map<String, Object> valuemap = new HashMap<>();
            if (timelist != null && timelist.size() > 0) {
                value = countOnePollutantAvgConcentration(timelist, pollutantcode, concentrationlist);
            }
            if (num > 0) {
                formatvalue = String.format("%.2f", windspeed / (float) num);
            }
            valuemap.put("winddirectionname", str);
            valuemap.put("winddirectioncode", winddirectioncode);
            valuemap.put("winddirectionnum", timelist.size());
            valuemap.put("windspeedavg", "".equals(formatvalue) ? 0 : formatvalue);
            valuemap.put("pollutantcode", pollutantcode);
            valuemap.put("pollutantvalue", "".equals(value) ? 0 : value);
            resultlist.add(valuemap);
        }
        return resultlist;
    }

    /**
     * @author: xsm
     * @date: 2019/6/28 10:48
     * @Description: 统计单个污染物的平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String countOnePollutantAvgConcentration(List<String> timelist, String code, List<Map<String, Object>> concentrationlist) {
        int num = 0;
        double valuenum = 0d;
        for (String timestr : timelist) {
            for (Map<String, Object> objmap2 : concentrationlist) {
                //当时间相同时
                if (timestr.equals(objmap2.get("monitortime").toString())) {
                    num += 1;
                    valuenum += Double.parseDouble(objmap2.get("monitorvalue").toString());
                    break;
                } else {
                    continue;
                }
            }
        }
        String value = "";
        if (num > 0) {
            value = String.format("%.2f", valuenum / (float) num);
        }
        return value;
    }

    /**
     * @author: xsm
     * @date: 2019/8/07 3:34
     * @Description: 根据自定义参数获取监测点点位相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointRelationByParamMap(List<Map<String, Object>> listdatas, List<Map<String, Object>> comparelistdatas, Map<String, Object> paramMap) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        //String collection = paramMap.get("collection").toString();
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();

        //构建Mongdb查询条件
        if (comparelistdatas.size() > 0) {//判断查询数据是否为空
            for (Map<String, Object> mnmap : comparelistdatas) {
                String mn = mnmap.get("dgimn").toString();
                String monitorpointname = "";
                String pollutionname = "";
                for (Map<String, Object> map : monitorpointlist) {
                    if (mn.equals(map.get("DGIMN").toString())) {
                        monitorpointname = map.get("MonitorPointName").toString();
                        if (map.get("pollutionname") != null) {
                            pollutionname = map.get("pollutionname").toString();
                        }
                        break;
                    }
                }
                List<Map<String, Object>> thelist = (List<Map<String, Object>>) mnmap.get("monitordatalist");
                List<Double> listone = new ArrayList<>();
                List<Double> listtwo = new ArrayList<>();
                for (Map<String, Object> objmap : thelist) {
                    String monitortime = objmap.get("monitortime").toString();
                    for (Map<String, Object> objmap2 : listdatas) {
                        if (monitortime.equals(objmap2.get("monitortime").toString())) {
                            listone.add(Double.parseDouble(objmap2.get("monitorvalue").toString()));
                            listtwo.add(Double.parseDouble(objmap.get("monitorvalue").toString()));
                            break;
                        }
                    }
                }
                //两组要对比的数据都不能为空
                if (listone != null && listone.size() > 0 && listtwo != null && listtwo.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pollutionname", pollutionname);
                    map.put("monitorpointname", monitorpointname);
                    map.put("dgimn", mn);
                    // listone  左侧点位数据    listtwo：右侧点位数据
                    Double value = DataFormatUtil.getRelationPercent(listone, listtwo);
                    if (value > -0.000001 && value < +0.000001) {//Double类型最小负数和最小正数，判断为0
                        value = 0.0;
                    }
                    map.put("similarity", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(value)));
                    result.add(map);
                }
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 1:43
     * @Description:根据自定义参数获取多个监测点的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getMorePointMonitorDataByParamMap(String datamark, Date startDate, Date endDate, String pollutantcode, List<String> mnlist) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        String datalistname = "";
        String collection = "";
        if ("hour".equals(datamark)) {
            datalistname = "HourDataList";
            collection = "HourData";
        } else if ("day".equals(datamark)) {
            datalistname = "DayDataList";
            collection = "DayData";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").is(pollutantcode));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        Map<String, Object> resultmap = new HashMap<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                //MN号
                String mnnum = document.getString("DataGatherCode");
                String monitorDate = "";
                if ("hour".equals(datamark)) {
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("day".equals(datamark)) {
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Object value = "";
                List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                        if (dataMap.get("AvgStrength") != null && !"null".equals(dataMap.get("AvgStrength").toString())) {
                            value = dataMap.get("AvgStrength");
                        }
                        break;
                    }
                }
                //判断map中是否有该mn的数据
                boolean flag = resultmap.containsKey(mnnum);
                if (flag == true) {//有
                    List<Map<String, Object>> thelist = (List<Map<String, Object>>) resultmap.get(mnnum);
                    if (!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitorDate);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                    }
                } else {//无
                    List<Map<String, Object>> thelist = new ArrayList<>();
                    if (!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitorDate);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                        resultmap.put(mnnum, thelist);
                    }
                }
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 1:43
     * @Description:根据自定义参数获取多个监测点按周日期分组的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private Map<String, Object> getMorePointMonitorDataGroupByWeekDateByParamMap(Date startDate, Date endDate, String pollutantcode, List<String> mnlist) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        String datalistname = "";
        datalistname = "HourDataList";
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").is(pollutantcode));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, "HourData");
        Map<String, Object> resultmap = new HashMap<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                //MN号
                String mnnum = document.getString("DataGatherCode");
                String monitorDay = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                String monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                Object value = "";
                List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                        if (dataMap.get("AvgStrength") != null && !"null".equals(dataMap.get("AvgStrength").toString())) {
                            value = dataMap.get("AvgStrength");
                        }
                        break;
                    }
                }
                //判断map中是否有该mn的数据
                boolean flag = resultmap.containsKey(monitorDay);
                if (flag == true) {//有
                    Map<String, Object> mnmap = (Map<String, Object>) resultmap.get(monitorDay);
                    boolean flag2 = mnmap.containsKey(mnnum);
                    if (flag2 == true) {//有
                        List<Map<String, Object>> thelist = (List<Map<String, Object>>) mnmap.get(mnnum);
                        if (!"".equals(value)) {//判断是否有该因子的监测值
                            Map<String, Object> objmap = new HashMap<>();
                            objmap.put("monitortime", monitorDate);
                            objmap.put("monitorvalue", value);
                            thelist.add(objmap);
                        }
                    } else {
                        List<Map<String, Object>> thelist = new ArrayList<>();
                        if (!"".equals(value)) {//判断是否有该因子的监测值
                            Map<String, Object> objmap = new HashMap<>();
                            objmap.put("monitortime", monitorDate);
                            objmap.put("monitorvalue", value);
                            thelist.add(objmap);
                            mnmap.put(mnnum, thelist);
                        }
                    }

                } else {//无
                    Map<String, Object> mnmap = new HashMap<>();
                    List<Map<String, Object>> thelist = new ArrayList<>();
                    if (!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitorDate);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                        mnmap.put(mnnum, thelist);
                        resultmap.put(monitorDay, mnmap);
                    }
                }
            }
        }
        return resultmap;
    }


    /**
     * @author: xsm
     * @date: 2019/9/24 0024 上午 8:46
     * @Description: 根据自定义参数筛选符合相似度条件的厂界恶臭信息（日）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEntStinkHourRelationDataByParamMap(String starttime, String endtime, String pollutantcode, List<String> dgimns) {
        Date startDate = null;
        Date endDate = null;
        startDate = DataFormatUtil.getDateYMDH(starttime);
        endDate = DataFormatUtil.getDateYMDH(endtime);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        List<String> mnlist = new ArrayList<>();
        for (Map<String, Object> map : monitorpointlist) {
            if (map.get("dgimn") != null) {
                mnlist.add(map.get("dgimn").toString());
            }
        }
        Map<String, Object> envstinks = getMorePointMonitorDataByParamMap("hour", startDate, endDate, pollutantcode, mnlist);
        Map<String, Object> entstinks = getMorePointMonitorDataByParamMap("hour", startDate, endDate, pollutantcode, dgimns);
        //根据污染物编码和监测点类型去获取对应的污染物信息
        paramMap.clear();
        paramMap.put("dgimnlist", dgimns);
        List<Map<String, Object>> entmonitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();
        result = countEntStinkRelationData(result, envstinks, entstinks, monitorpointlist, entmonitorpointlist);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/9/24 0024 上午 8:46
     * @Description: 根据自定义参数筛选符合相似度条件的厂界恶臭信息(周)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEntStinkRelationDataGroupByWeekDateByParamMap(String starttime, String endtime, String pollutantcode, List<String> dgimns) {
        Date startDate = null;
        Date endDate = null;
        List<String> daylist = DataFormatUtil.getYMDBetween(starttime, endtime);
        daylist.add(endtime);
        startDate = DataFormatUtil.getDateYMD(starttime);
        endDate = DataFormatUtil.getDateYMD(endtime);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        List<String> mnlist = new ArrayList<>();
        for (Map<String, Object> map : monitorpointlist) {
            if (map.get("dgimn") != null) {
                mnlist.add(map.get("dgimn").toString());
            }
        }
        Map<String, Object> envstinks = getMorePointMonitorDataGroupByWeekDateByParamMap(startDate, endDate, pollutantcode, mnlist);
        Map<String, Object> entstinks = getMorePointMonitorDataGroupByWeekDateByParamMap(startDate, endDate, pollutantcode, dgimns);
        //根据污染物编码和监测点类型去获取对应的污染物信息
        paramMap.clear();
        dgimns.removeAll(mnlist);
        /*List<String> dgimnlist = new ArrayList<>();
        for (String mnIndex:dgimns){
            if (){

            }
        }*/

        paramMap.put("dgimnlist", dgimns);
        List<Map<String, Object>> entmonitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();
        if (envstinks.size() > 0 && entstinks.size() > 0) {//判断查询数据是否为空
            for (String day : daylist) {
                Map<String, Object> themap = new HashMap<>();
                themap.put("monitortime", day);
                List<Map<String, Object>> datalist = new ArrayList<>();
                if (envstinks.get(day) != null && entstinks.get(day) != null) {
                    datalist = countEntStinkRelationData(datalist, (Map<String, Object>) envstinks.get(day), (Map<String, Object>) entstinks.get(day), monitorpointlist, entmonitorpointlist);
                }
                themap.put("datalist", datalist);
                result.add(themap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/9/24 0024 上午 8:46
     * @Description: 根据自定义参数筛选符合相似度条件的厂界恶臭信息（月）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEntStinkDayRelationDataByParamMap(String starttime, String endtime, String pollutantcode, List<String> dgimns) {
        Date startDate = null;
        Date endDate = null;
        startDate = DataFormatUtil.getDateYMD(starttime);
        endDate = DataFormatUtil.getDateYMD(endtime);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        List<String> mnlist = new ArrayList<>();
        for (Map<String, Object> map : monitorpointlist) {
            if (map.get("dgimn") != null) {
                mnlist.add(map.get("dgimn").toString());
            }
        }
        Map<String, Object> envstinks = getMorePointMonitorDataByParamMap("day", startDate, endDate, pollutantcode, mnlist);
        Map<String, Object> entstinks = getMorePointMonitorDataByParamMap("day", startDate, endDate, pollutantcode, dgimns);
        //根据污染物编码和监测点类型去获取对应的污染物信息
        paramMap.clear();
        dgimns.removeAll(mnlist);
        paramMap.put("dgimnlist", dgimns);
        List<Map<String, Object>> entmonitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();
        result = countEntStinkRelationData(result, envstinks, entstinks, monitorpointlist, entmonitorpointlist);
        return result;
    }

    /**
     * @author: lip
     * @date: 2019/9/30 0030 上午 10:43
     * @Description: 自定义查询条件获取分组监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getAggregationMonitorDataByParamMap(Map<String, Object> paramMap) {
        List<AggregationOperation> operations = new ArrayList<>();
        Fields fields = fields("DataGatherCode", "MonitorTime");
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
            operations.add(Aggregation.project("DataGatherCode"));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate)));
            }
            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("MonitorTime").lte(endDate)));
            }
            operations.add(Aggregation.project("MonitorTime"));
        }
        if (paramMap.get("issuddenchange") != null) {
            if (paramMap.get("datalistkey") != null) {
                String datalistkey = paramMap.get("datalistkey").toString();
                operations.add(Aggregation.match(Criteria.where(datalistkey + ".IsSuddenChange").is(paramMap.get("issuddenchange"))));
            } else {
                operations.add(Aggregation.match(Criteria.where("IsSuddenChange").is(paramMap.get("issuddenchange"))));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            if (paramMap.get("datalistkey") != null) {
                String datalistkey = paramMap.get("datalistkey").toString();
                operations.add(Aggregation.match(Criteria.where(datalistkey + ".DataGatherCode").in(pollutantcodes)));
                operations.add(Aggregation.project(datalistkey + ".PollutantCode"));
                //fields.and("PollutantCode", datalistkey + ".PollutantCode");
            } else {
                operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(pollutantcodes)));
                operations.add(Aggregation.project("PollutantCode"));

            }
        }

        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            UnwindOperation unwindOperation = unwind(unwindkey);
            operations.add(unwindOperation);
            operations.add(Aggregation.project(unwindkey + ".PollutantCode"));

        }




        /*Map<String, Object> map2 = new HashMap<>();
        map2.put("PollutantCode", "$PollutantCode");
        map2.put("AvgStrength", "$AvgStrength");*/
        /*GroupOperation groupOperation = group("DataGatherCode", "MonitorTime").push(map2).as(unWindName);
        aggregations.add(groupOperation);*/
        Aggregation aggregation = newAggregation(operations);
        AggregationResults<Document> minuteData = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        return minuteData.getMappedResults();

    }

    /**
     * @author: xsm
     * @date: 2019/9/24 0024 下午 2:58
     * @Description:计算厂界恶臭点位和所有敏感点的相似度，返回相似度大于50的厂界点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> countEntStinkRelationData(List<Map<String, Object>> result, Map<String, Object> envstinks, Map<String, Object> entstinks, List<Map<String, Object>> monitorpointlist, List<Map<String, Object>> entmonitorpointlist) {
        if (envstinks.size() > 0 && entstinks.size() > 0) {//判断查询数据是否为空
            for (Map.Entry<String, Object> entryone : envstinks.entrySet()) {
                String envmn = entryone.getKey();
                List<Map<String, Object>> envlist = (List<Map<String, Object>>) entryone.getValue();
                String envmonitorpointname = "";
                for (Map<String, Object> map : monitorpointlist) {
                    if (envmn.equals(map.get("dgimn").toString())) {
                        envmonitorpointname = map.get("monitorpointname").toString();
                        break;
                    }
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("pointname", envmonitorpointname);
                List<Map<String, Object>> entresult = new ArrayList<>();
                for (Map.Entry<String, Object> entry : entstinks.entrySet()) {
                    String mn = entry.getKey();
                    String monitorpointname = "";
                    for (Map<String, Object> map : entmonitorpointlist) {
                        if (mn.equals(map.get("DGIMN").toString())) {
                            monitorpointname = map.get("MonitorPointName").toString();
                            break;
                        }
                    }
                    List<Map<String, Object>> thelist = (List<Map<String, Object>>) entry.getValue();
                    List<Double> listone = new ArrayList<>();
                    List<Double> listtwo = new ArrayList<>();
                    for (Map<String, Object> objmap : thelist) {
                        String monitortime = objmap.get("monitortime").toString();
                        for (Map<String, Object> objmap2 : envlist) {
                            if (monitortime.equals(objmap2.get("monitortime").toString())) {
                                listone.add(Double.parseDouble(objmap2.get("monitorvalue").toString()));
                                listtwo.add(Double.parseDouble(objmap.get("monitorvalue").toString()));
                                break;
                            }
                        }
                    }
                    //两组要对比的数据都不能为空
                    if (listone != null && listone.size() > 0 && listtwo != null && listtwo.size() > 0) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pointname", monitorpointname);
                        //map.put("dgimn", mn);
                        //获取点位相似度
                        // listone  左侧点位数据    listtwo：右侧点位数据
                        Double value = DataFormatUtil.getRelationPercent(listone, listtwo);
                        if (value > -0.000001 && value < +0.000001) {//Double类型最小负数和最小正数，判断为0
                            value = 0.0;
                        }
                        Double num = Double.parseDouble(DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(value)));
                        if (num > 0.50) {//当相似度大于50%时
                            map.put("similarity", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(value * 100)));
                            entresult.add(map);
                        }
                    }
                }
                resultmap.put("pointname", envmonitorpointname);
                resultmap.put("similaritydata", entresult);
                result.add(resultmap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/8/07 3:34
     * @Description: 根据自定义参数获取监测点点位相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorPointRelationChartDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        if (monitorpointlist.size() > 0) {
            String mnKey = monitorpointlist.get(0).get("DGIMN").toString();
            Map<String, Object> pointTimeAndValue = new LinkedHashMap<>();
            Map<String, Map<String, Object>> mnAndTimeAndValue = new LinkedHashMap<>();
            setStinkAndVocMonitorPointTimeValueData(pointTimeAndValue, mnAndTimeAndValue, paramMap);
            List<Map<String, Object>> xListData = new ArrayList<>();
            List<Map<String, Object>> yListData = new ArrayList<>();
            Map<String, Object> outputTimeAndValue = mnAndTimeAndValue.get(mnKey);
            List<Double> xData = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            String collection = paramMap.get("collection").toString();
            String beforeTimeKey = "";
            Integer beforeTimeNum = Integer.parseInt(paramMap.get("beforetime").toString());
            for (String time : pointTimeAndValue.keySet()) {
                if (collection.indexOf("Hour") > -1) {//小时数据
                    beforeTimeKey = DataFormatUtil.getBeforeByHourTime(beforeTimeNum, time);
                } else if (collection.indexOf("Minute") > -1) {//分钟数据
                    beforeTimeKey = DataFormatUtil.getBeforeByMinuteTime(beforeTimeNum, time);
                } else if (collection.indexOf("Day") > -1) {
                    beforeTimeKey = DataFormatUtil.getBeforeByDayTime(beforeTimeNum, time);
                }
                if (outputTimeAndValue.get(beforeTimeKey) != null) {
                    Map<String, Object> xMap = new LinkedHashMap<>();
                    Map<String, Object> yMap = new LinkedHashMap<>();
                    xMap.put("monitortime", time);
                    xMap.put("value", Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    yMap.put("monitortime", beforeTimeKey);
                    yMap.put("value", Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    yData.add(Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    xData.add(Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    xListData.add(xMap);
                    yListData.add(yMap);
                }
            }
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
        return resultMap;
    }

    /**
     * @author: mmt
     * @date: 2022/9/16 3:34
     * @Description: 根据自定义参数获取监测点点位相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorPointRelationTableDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        if (monitorpointlist.size() > 0) {
            String mnKey = monitorpointlist.get(0).get("DGIMN").toString();
            Map<String, Object> pointTimeAndValue = new LinkedHashMap<>();
            Map<String, Map<String, Object>> mnAndTimeAndValue = new LinkedHashMap<>();
            setStinkAndVocMonitorPointTimeValueData(pointTimeAndValue, mnAndTimeAndValue, paramMap);
            List<Map<String, Object>> xListData = new ArrayList<>();
            Map<String, Object> outputTimeAndValue = mnAndTimeAndValue.get(mnKey);
            List<Double> xData = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            String collection = paramMap.get("collection").toString();
            String beforeTimeKey = "";
            Integer beforeTimeNum = Integer.parseInt(paramMap.get("beforetime").toString());
            for (String time : pointTimeAndValue.keySet()) {
                if (collection.indexOf("Hour") > -1) {//小时数据
                    beforeTimeKey = DataFormatUtil.getBeforeByHourTime(beforeTimeNum, time);
                } else if (collection.indexOf("Minute") > -1) {//分钟数据
                    beforeTimeKey = DataFormatUtil.getBeforeByMinuteTime(beforeTimeNum, time);
                } else if (collection.indexOf("Day") > -1) {
                    beforeTimeKey = DataFormatUtil.getBeforeByDayTime(beforeTimeNum, time);
                }
                if (outputTimeAndValue.get(beforeTimeKey) != null) {
                    Map<String, Object> xMap = new LinkedHashMap<>();
                    Map<String, Object> yMap = new LinkedHashMap<>();
                    xMap.put("monitortime", time);
                    xMap.put("xmonitorvalue", Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    xMap.put("ymonitorvalue", Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    xListData.add(xMap);
                }
            }

            if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
                resultMap.put("total", xListData.size());
                Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
                Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
                xListData = getPageData(xListData, pagenum, pagesize);
            }
            resultMap.put("listdata", xListData);
        }
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2019/8/09 10:47
     * @Description: 根据站点MN号获取最新一条站点小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAirStationLatelyHourDataByAirMn(String airmn) {
        Query query = new Query();
        query.addCriteria(Criteria.where("StationCode").is(airmn));
        query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        Document document = mongoTemplate.findOne(query, Document.class, "StationHourAQIData");
        Map<String, Object> result = new HashMap<>();
        String monthSO2 = "";//二氧化硫月平均浓度
        String monthNO2 = "";//二氧化氮月平均浓度
        String monthCO = "";//一氧化碳月平均浓度
        String monthO3 = "";//臭氧月平均浓度
        String monthPM10 = "";//二PM10月平均浓度
        String monthPM25 = "";//PM2.5月平均浓度
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", 5);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        paramMap.clear();
        for (Map<String, Object> map : pollutants) {
            paramMap.put(map.get("code").toString(), map.get("name"));
        }
        if (document != null && document.size() > 0) {
            result.put("aqi", document.getInteger("AQI"));
            result.put("primarypollutant", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
            result.put("airquality", document.getString("AirQuality"));
            result.put("airlevel", document.getString("AirLevel"));
            result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
            List<Map<String, Object>> pollutantDataList = document.get("DataList", List.class);
            for (Map<String, Object> map : pollutantDataList) {
                if ("a21026".equals(map.get("PollutantCode").toString())) {//二氧化硫
                    monthSO2 = map.get("Strength") != null ? map.get("Strength").toString() : "";
                }
                if ("a21004".equals(map.get("PollutantCode").toString())) {
                    monthNO2 = map.get("Strength") != null ? map.get("Strength").toString() : "";
                }
                if ("a21005".equals(map.get("PollutantCode").toString())) {
                    monthCO = map.get("Strength") != null ? map.get("Strength").toString() : "";
                }
                if ("a05024".equals(map.get("PollutantCode").toString())) {
                    monthO3 = map.get("Strength") != null ? map.get("Strength").toString() : "";
                }
                if ("a34002".equals(map.get("PollutantCode").toString())) {
                    monthPM10 = map.get("Strength") != null ? map.get("Strength").toString() : "";
                }
                if ("a34004".equals(map.get("PollutantCode").toString())) {
                    monthPM25 = map.get("Strength") != null ? map.get("Strength").toString() : "";
                }
            }
        } else {
            result.put("aqi", "");
            result.put("primarypollutant", "");
            result.put("airquality", "");
            result.put("airlevel", "");
        }
        result.put("SO2", monthSO2);
        result.put("NO2", monthNO2);
        result.put("CO", monthCO);
        result.put("O3", monthO3);
        result.put("PM10", monthPM10);
        result.put("PM25", monthPM25);
        return result;
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 9:28
     * @Description: 由code转换name
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getPrimaryPollutant(Map<String, Object> paramMap, Object primaryPollutant) {
        String primaryPollutants = "";
        if (primaryPollutant != null) {
            String tempValue = primaryPollutant.toString();
            if (tempValue.indexOf(",") > -1) {
                String[] tempValues = tempValue.split(",");
                for (int i = 0; i < tempValues.length; i++) {
                    primaryPollutants += paramMap.get(tempValues[i]) + ",";
                }
                primaryPollutants = primaryPollutants.substring(0, primaryPollutants.length() - 1);
            } else {
                primaryPollutants = paramMap.get(tempValue) != null ? paramMap.get(tempValue).toString() : "";
            }
        }
        return primaryPollutants;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 上午 9:25
     * @Description: 自定义条件查询在线小时突增数据（通过mn号和日时间分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public Map<String, Object> getOnlineDataGroupMmAndMonitortime(Map<String, Object> paramMap) throws ParseException {
        Criteria criteria = new Criteria();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        Long pagenum = null;
        Long pagesize = null;
        Integer remindtype = null;
        String collectiontype = paramMap.get("collectiontype") == null ? "1" : paramMap.get("collectiontype").toString();
        String collection = hourCollection;
        String datalistkey = "HourDataList";
        Integer total = 0;
        //默认查询小时浓度数据，2为查询分钟浓度数据
        if ("2".equals(collectiontype)) {
            datalistkey = "MinuteDataList";
            collection = minuteCollection;
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("remindtype") != null) {
            remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pagenum = Long.valueOf(paramMap.get("pagenum").toString());
            pagesize = Long.valueOf(paramMap.get("pagesize").toString());
        }
        List<Map> mappedResults = new ArrayList<>();

        if (remindtype == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime"));
            //插入分页、排序条件
            if (pagenum != null && pagesize != null) {
                operations.add(Aggregation.skip((pagenum - 1) * pagesize));
                operations.add(Aggregation.limit(pagesize));
            }
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            mappedResults = mongoTemplate.aggregate(newAggregation(operations), hourFlowCollection, Map.class).getMappedResults();
            List<Map> data = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime"), count().as("count")), hourFlowCollection, Map.class).getMappedResults();
            if (data.size() > 0) {
                String count = data.get(0).get("count").toString();
                total = Integer.valueOf(count);
            }
        } else if (remindtype == ConcentrationChangeEnum.getCode()) { //浓度
            criteria.and("DataGatherCode").in(dgimns).and(datalistkey + ".IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            //插入分页、排序条件
            if (pagenum != null && pagesize != null) {
                operations.add(Aggregation.skip((pagenum - 1) * pagesize));
                operations.add(Aggregation.limit(pagesize));
            }
            mappedResults = mongoTemplate.aggregate(newAggregation(operations), collection, Map.class).getMappedResults();
            List<Map> data = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime"), count().as("count")), collection, Map.class).getMappedResults();
            if (data.size() > 0) {
                String count = data.get(0).get("count").toString();
                total = Integer.valueOf(count);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", mappedResults);
        if (mappedResults.size() == 0) {
            return result;
        }
        getOnlineHourData(mappedResults, remindtype, paramMap, datalistkey, collection);
        return result;
    }


    @Override
    public Map<String, Object> countOnlineDataGroupMmAndMonitortime(Map<String, Object> paramMap) throws ParseException {
        Criteria criteria = new Criteria();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        Integer remindtype = null;
        Integer total = 0;
        Integer monitortype = 0;
        if (paramMap.get("monitortype") != null) {
            monitortype = Integer.valueOf(paramMap.get("monitortype").toString());
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("remindtype") != null) {
            remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString() + " 00:00:00";
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString() + " 23:59:59";
        }

        if (remindtype == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            List<Map> data = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime"), count().as("count")), hourFlowCollection, Map.class).getMappedResults();
            if (data.size() > 0) {
                String count = data.get(0).get("count").toString();
                total = Integer.valueOf(count);
            }
        } else if (remindtype == ConcentrationChangeEnum.getCode()) { //浓度
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            List<Map> data = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime"), count().as("count")), hourCollection, Map.class).getMappedResults();
            if (data.size() > 0) {
                String count = data.get(0).get("count").toString();
                total = Integer.valueOf(count);
            }
        }

        Map<String, Object> excelTitleInfo = getOnlineChangeExcelTitle(monitortype, remindtype);
        excelTitleInfo.put("total", total);
        return excelTitleInfo;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/27 16:43
     * @Description: 获取气象相关监测点mn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMnForWeatherByParam(Integer monitorPointType) {
        return onlineMapper.getMnForWeatherByParam(monitorPointType);
    }

    /**
     * @author: xsm
     * @date: 2019/8/28 11:30
     * @Description: 获取某点位污染物的预警、异常、超限标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPollutantEarlyAlarmStandardDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();
        Object type = paramMap.get("monitorpointtype");
        if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode())) {//废气
            outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode())) {//废水
            paramMap.put("outputtype", 1);
            outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode())) {//雨水
            paramMap.put("outputtype", 3);
            outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode())) {//厂界小型站
            outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode())) {//厂界恶臭
            outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode())) {//扬尘
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode())) {//微站
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode())) {//空气站
            outputs = airMonitorStationMapper.getAirMonitorStationDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode())) {//水质
            outputs = waterStationMapper.getWaterStationDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode())) {//恶臭
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode())) {//voc
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        }
        String early = "";
        String exception = "";
        String overLevel1 = "";
        String overLevel2 = "";
        String overLevel3 = "";
        String standardvalue = "";
        if (outputs != null && outputs.size() > 0) {
            for (Map<String, Object> map : outputs) {
                if ("".equals(exception)) {
                    exception = map.get("ExceptionMaxValue") != null ? map.get("ExceptionMaxValue").toString() : "";
                }
                if ("".equals(standardvalue)) {
                    standardvalue = map.get("StandardMaxValue") != null ? map.get("StandardMaxValue").toString() : "";
                }
                if (map.get("FK_AlarmLevelCode") != null) {
                    if ("0".equals(map.get("FK_AlarmLevelCode").toString())) {
                        early = map.get("ConcenAlarmMaxValue") != null ? map.get("ConcenAlarmMaxValue").toString() : "";
                    } else if ("1".equals(map.get("FK_AlarmLevelCode").toString())) {
                        overLevel1 = map.get("ConcenAlarmMaxValue") != null ? map.get("ConcenAlarmMaxValue").toString() : "";
                    } else if ("2".equals(map.get("FK_AlarmLevelCode").toString())) {
                        overLevel2 = map.get("ConcenAlarmMaxValue") != null ? map.get("ConcenAlarmMaxValue").toString() : "";
                    } else if ("3".equals(map.get("FK_AlarmLevelCode").toString())) {
                        overLevel3 = map.get("ConcenAlarmMaxValue") != null ? map.get("ConcenAlarmMaxValue").toString() : "";
                    }
                } else {
                    exception = map.get("ExceptionMaxValue") != null ? map.get("ExceptionMaxValue").toString() : "";
                    standardvalue = map.get("StandardMaxValue") != null ? map.get("StandardMaxValue").toString() : "";
                }
            }
        }
        result.put("early", early);
        result.put("exception", exception);
        result.put("overLevel1", overLevel1);
        result.put("overLevel2", overLevel2);
        result.put("overLevel3", overLevel3);
        result.put("standardvalue", standardvalue);
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 上午 9:54
     * @Description: 通过mn号和日时间集合查询小时突增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [mappedResults]
     * @throws:
     */
    private void getOnlineHourData(List<Map> mappedResults, Integer remindtype, Map<String, Object> paramMap, String datalistkey, String collection) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = mappedResults.stream().filter(m -> m.get("DataGatherCode") != null).map(m -> m.get("DataGatherCode").toString()).distinct().collect(Collectors.toList());

        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        for (Map mappedResult : mappedResults) {
            if (mappedResult.get("MonitorTime") != null) {
                String monitorTime = mappedResult.get("MonitorTime").toString();
                String begin = monitorTime + " 00:00:00";
                String end = monitorTime + " 23:59:59";
                criterialist.add(Criteria.where("MonitorTime").gte(format.parse(begin)).lte(format.parse(end)));
            }
        }
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));

        List<Map> result = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remindtype == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).andOperator(timecriteria);
            result = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode", "MonitorTime", "HourFlowDataList", "ReadUserIds")), hourFlowCollection, Map.class).getMappedResults();
        } else if (remindtype == ConcentrationChangeEnum.getCode()) {    //浓度
            criteria.and("DataGatherCode").in(dgimns).and(datalistkey + ".IsSuddenChange").is(true).andOperator(timecriteria);
            result = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode", "MonitorTime", datalistkey, "ReadUserIds")), collection, Map.class).getMappedResults();
        }
        AssembleData(mappedResults, result, paramMap, datalistkey);
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 上午 10:06
     * @Description: 组装数据，在分组数据中加入突增时段，突增幅度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [mappedResults, listByParam]
     * @throws:
     */
    private void AssembleData(List<Map> mappedResults, List<Map> result, Map<String, Object> paramMap, String datalistkey) {
        DecimalFormat format = new DecimalFormat("0.##");
        DecimalFormat df = new DecimalFormat("00");
        Integer usercode = paramMap.get("usercode") == null ? -1 : Integer.valueOf(paramMap.get("usercode").toString());
        Integer monitortype = paramMap.get("monitortype") == null ? -1 : Integer.valueOf(paramMap.get("monitortype").toString());
        String collectiontype = paramMap.get("collectiontype") == null ? "1" : paramMap.get("collectiontype").toString();

        Integer remindtype = 0;
        if (paramMap.get("remindtype") != null) {
            remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
        }
        //获取污染物
        List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = new ArrayList<>();
        if (paramMap.get("pollutanttypes") != null) {
            List<Integer> pollutanttypes = (List<Integer>) paramMap.get("pollutanttypes");
            for (Integer type : pollutanttypes) {
                paramMap.put("pollutanttype", type);
                gasOutPutPollutantSetsByOutputIds.addAll(gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap));
            }
        } else {
            gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
        }

        Map<String, String> pollutants = new HashMap<>();
        gasOutPutPollutantSetsByOutputIds.stream().filter(m -> m.get("pollutantname") != null && m.get("pollutantcode") != null).map(m -> pollutants.put(m.get("pollutantcode").toString(), m.get("pollutantname").toString())).collect(Collectors.toList());
        for (int i = 0; i < mappedResults.size(); i++) {
            Map mappedResult = mappedResults.get(i);
            if (mappedResult.get("DataGatherCode") != null) {
                String dataGatherCode = mappedResult.get("DataGatherCode").toString();
                if (mappedResult.get("MonitorTime") != null) {
                    String monitorTime = mappedResult.get("MonitorTime").toString();
                    List<Map<String, Object>> datas = new ArrayList<>();
//                    result.stream().map(m -> datas.add(((List)m.get("HourDataList")).stream().filter(m1->((boolean)((Map)m1).get("IsSuddenChange")))).collect(Collectors.toList());
                    if (remindtype == 1) {
                        result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && DataFormatUtil.getDateYMD((Date) m.get("MonitorTime")).equals(monitorTime)
                                && m.get("DataGatherCode").toString().equals(dataGatherCode)).map(m -> (List) (m.get(datalistkey))).map(m -> m.stream().filter(m1 -> ((Map) m1).
                                get("IsSuddenChange") != null && Boolean.valueOf(((Map) m1).get("IsSuddenChange").toString())).map(m2 -> datas.add((Map) m2)).
                                collect(Collectors.toList())).collect(Collectors.toList());
                    } else if (remindtype == 2) {
                        result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && DataFormatUtil.getDateYMD((Date) m.get("MonitorTime")).equals(monitorTime)
                                && m.get("DataGatherCode").toString().equals(dataGatherCode)).map(m -> (List) (m.get("HourFlowDataList"))).map(m -> m.stream().filter(m1 -> ((Map) m1).
                                get("IsSuddenChange") != null && Boolean.valueOf(((Map) m1).get("IsSuddenChange").toString())).map(m2 -> datas.add((Map) m2)).
                                collect(Collectors.toList())).collect(Collectors.toList());
                    }
                    Optional<Float> max1 = datas.stream().filter(m -> m.get("ChangeMultiple") != null && Float.valueOf(m.get("ChangeMultiple").toString()) >= 0).map(m -> Float.valueOf(m.get("ChangeMultiple").toString())).max(Float::compareTo);
                    Optional<Float> min1 = datas.stream().filter(m -> m.get("ChangeMultiple") != null && Float.valueOf(m.get("ChangeMultiple").toString()) >= 0).map(m -> Float.valueOf(m.get("ChangeMultiple").toString())).min(Float::compareTo);
                    String pollutantStr = datas.stream().filter(m -> m.get("PollutantCode") != null).map(m -> pollutants.get(m.get("PollutantCode").toString())).distinct().collect(Collectors.joining("、"));
                    Float ChangeMultipleMin = null;
                    Float ChangeMultipleMax = null;
                    if (max1.isPresent()) {
                        ChangeMultipleMax = max1.get();
                    }
                    if (min1.isPresent()) {
                        ChangeMultipleMin = min1.get();
                    }
                    if (ChangeMultipleMax.floatValue() != ChangeMultipleMin.floatValue()) {
                        mappedResult.put("flowrate", format.format(ChangeMultipleMin * 100) + "%" + "-" + format.format(ChangeMultipleMax * 100) + "%");
                    } else if (ChangeMultipleMax != null) {
                        mappedResult.put("flowrate", format.format(ChangeMultipleMax * 100) + "%");
                    }

                    //分钟数据 并且是分钟数据   设置分钟时间
                    if ("2".equals(collectiontype) && remindtype == 1) {
                        setMinuteSuddenChangeTimeStr(monitortype, result, usercode, mappedResult, dataGatherCode, monitorTime, pollutantStr);
                    } else {
                        //设置小时时间
                        setHourSuddenChangeTimeStr(result, df, usercode, mappedResult, dataGatherCode, monitorTime, pollutantStr);
                    }
                }
            }
        }
    }

    private void setHourSuddenChangeTimeStr(List<Map> result, DecimalFormat df, Integer usercode, Map mappedResult, String dataGatherCode, String monitorTime, String pollutantStr) {
        List<Integer> allpoint = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
        ).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH")).map(m -> Integer.valueOf(m.substring(m.length() - 2, m.length())))
                .sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());
        List<List<Integer>> line = groupIntegerList(allpoint);

        List<Integer> readed = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
                && m.get("ReadUserIds") != null && ((List) (m.get("ReadUserIds"))).contains(usercode.toString())).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH")).map(m -> Integer.valueOf(m.substring(m.length() - 2, m.length())))
                .sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());
        List<List<Integer>> readline = groupIntegerList(readed);

        List<Integer> noread = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
                && (m.get("ReadUserIds") == null || !((List) (m.get("ReadUserIds"))).contains(usercode.toString()))).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH")).map(m -> Integer.valueOf(m.substring(m.length() - 2, m.length())))
                .sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());
        List<List<Integer>> noreadline = groupIntegerList(noread);

        String readline1 = getLine(readline);
        String noreadline1 = getLine(noreadline);
        String line1 = getLine(line);
        if (readline1.length() > 0) {
            readline1 = readline1.substring(0, readline1.length() - 1);
        }
        if (noreadline1.length() > 0) {
            noreadline1 = noreadline1.substring(0, noreadline1.length() - 1);
        }
        if (line1.length() > 0) {
            line1 = line1.substring(0, line1.length() - 1);
        }
        LinkedList<Integer> integers = new LinkedList<>(allpoint);
        mappedResult.put("readed", readline1);
        mappedResult.put("noread", noreadline1);
        mappedResult.put("allpoint", line1);
        mappedResult.put("pollutantStr", pollutantStr);
        mappedResult.put("starttime", monitorTime + " " + df.format(integers.getFirst() == null ? 0 : Integer.valueOf(integers.getFirst().toString())));
        mappedResult.put("endtime", monitorTime + " " + df.format(integers.getLast() == null ? 0 : Integer.valueOf(integers.getLast().toString())));
    }


    private void setMinuteSuddenChangeTimeStr(Integer monitortype, List<Map> result, Integer usercode, Map mappedResult, String dataGatherCode, String monitorTime, String pollutantStr) {

        List<String> allpoint = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
        ).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm")).map(m -> m.substring(m.length() - 5, m.length())).sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());

        List<String> readed = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
                && m.get("ReadUserIds") != null && ((List) (m.get("ReadUserIds"))).contains(usercode.toString())).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm")).map(m -> m.substring(m.length() - 5, m.length()))
                .sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());

        List<String> noread = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
                && (m.get("ReadUserIds") == null || !((List) (m.get("ReadUserIds"))).contains(usercode.toString()))).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm")).map(m -> m.substring(m.length() - 5, m.length()))
                .sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());

        LinkedList<String> integers = new LinkedList<>(allpoint);
        mappedResult.put("readed", readed.stream().collect(Collectors.joining("、")));
        mappedResult.put("noread", noread.stream().collect(Collectors.joining("、")));
        mappedResult.put("allpoint", allpoint.stream().collect(Collectors.joining("、")));
        mappedResult.put("pollutantStr", pollutantStr);
        mappedResult.put("starttime", monitorTime + " " + integers.getFirst());
        mappedResult.put("endtime", monitorTime + " " + integers.getLast());
    }

    /**
     * @author: xsm
     * @date: 2020/10/10 0010 下午 2:45
     * @Description: 自定义条件查询在线小时突增数据（通过mn号和日时间分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map> getOnlineConcentrationFlowChangeData(Integer remind, List<String> mns, Date startDate, Date endDate, List<Integer> monitorpointtypes) {
        Criteria criteria = new Criteria();
        List<Map> mappedResults = new ArrayList<>();
        if (remind == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(mns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            mappedResults = mongoTemplate.aggregate(newAggregation(operations), hourFlowCollection, Map.class).getMappedResults();
        } else if (remind == ConcentrationChangeEnum.getCode()) { //浓度
            criteria.and("DataGatherCode").in(mns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            mappedResults = mongoTemplate.aggregate(newAggregation(operations), hourCollection, Map.class).getMappedResults();
        }
        try {
            if (mappedResults.size() > 0) {
                getOnlineHourDatatwo(mappedResults, remind, monitorpointtypes);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mappedResults;
    }

    private void getOnlineHourDatatwo(List<Map> mappedResults, Integer remind, List<Integer> monitorpointtypes) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = mappedResults.stream().filter(m -> m.get("DataGatherCode") != null).map(m -> m.get("DataGatherCode").toString()).distinct().collect(Collectors.toList());

        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        for (Map mappedResult : mappedResults) {
            if (mappedResult.get("MonitorTime") != null) {
                String monitorTime = mappedResult.get("MonitorTime").toString();
                String begin = monitorTime + " 00:00:00";
                String end = monitorTime + " 23:59:59";
                criterialist.add(Criteria.where("MonitorTime").gte(format.parse(begin)).lte(format.parse(end)));
            }
        }
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));

        List<Map> result = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remind == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).andOperator(timecriteria);
            result = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode", "MonitorTime", "HourFlowDataList", "ReadUserIds")), hourFlowCollection, Map.class).getMappedResults();
        } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).andOperator(timecriteria);
            result = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode", "MonitorTime", "HourDataList", "ReadUserIds")), hourCollection, Map.class).getMappedResults();
        }
        AssembleDatatwo(mappedResults, result, remind, monitorpointtypes);
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 上午 10:06
     * @Description: 组装数据，在分组数据中加入突增时段，突增幅度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [mappedResults, listByParam]
     * @throws:
     */
    private void AssembleDatatwo(List<Map> mappedResults, List<Map> result, Integer remind, List<Integer> monitorpointtypes) {
        DecimalFormat format = new DecimalFormat("0.##");
        DecimalFormat df = new DecimalFormat("00");
        //获取污染物
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = new ArrayList<>();
        for (Integer monitorpointtype : monitorpointtypes) {
            paramMap.put("pollutanttype", monitorpointtype);
            gasOutPutPollutantSetsByOutputIds.addAll(gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap));
        }
        Map<String, String> pollutants = new HashMap<>();
        gasOutPutPollutantSetsByOutputIds.stream().filter(m -> m.get("pollutantname") != null && m.get("pollutantcode") != null).map(m -> pollutants.put(m.get("pollutantcode").toString(), m.get("pollutantname").toString())).collect(Collectors.toList());
        result = result.stream().sorted(Comparator.comparing((Map m) -> m.get("MonitorTime").toString()).reversed()).collect(Collectors.toList());
        for (int i = 0; i < mappedResults.size(); i++) {
            Map mappedResult = mappedResults.get(i);
            if (mappedResult.get("DataGatherCode") != null) {
                String dataGatherCode = mappedResult.get("DataGatherCode").toString();
                if (mappedResult.get("MonitorTime") != null) {
                    String monitorTime = mappedResult.get("MonitorTime").toString();
                    List<Map<String, Object>> datas = new ArrayList<>();
//                    result.stream().map(m -> datas.add(((List)m.get("HourDataList")).stream().filter(m1->((boolean)((Map)m1).get("IsSuddenChange")))).collect(Collectors.toList());
                    if (remind == 1) {
                        result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && DataFormatUtil.getDateYMD((Date) m.get("MonitorTime")).equals(monitorTime)
                                && m.get("DataGatherCode").toString().equals(dataGatherCode)).map(m -> (List) (m.get("HourDataList"))).map(m -> m.stream().filter(m1 -> ((Map) m1).
                                get("IsSuddenChange") != null && Boolean.valueOf(((Map) m1).get("IsSuddenChange").toString())).map(m2 -> datas.add((Map) m2)).
                                collect(Collectors.toList())).collect(Collectors.toList());
                    } else if (remind == 2) {
                        result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && DataFormatUtil.getDateYMD((Date) m.get("MonitorTime")).equals(monitorTime)
                                && m.get("DataGatherCode").toString().equals(dataGatherCode)).map(m -> (List) (m.get("HourFlowDataList"))).map(m -> m.stream().filter(m1 -> ((Map) m1).
                                get("IsSuddenChange") != null && Boolean.valueOf(((Map) m1).get("IsSuddenChange").toString())).map(m2 -> datas.add((Map) m2)).
                                collect(Collectors.toList())).collect(Collectors.toList());
                    }
                    List<Map<String, Object>> pollutantdata = new ArrayList<>();
                    if (datas != null && datas.size() > 0) {
                        //按污染源分组排口信息
                        Map<String, List<Map<String, Object>>> pomap = datas.stream().filter(m -> m.get("PollutantCode") != null).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                        for (Map.Entry<String, List<Map<String, Object>>> entry : pomap.entrySet()) {
                            String pocode = entry.getKey();
                            List<Map<String, Object>> polist = entry.getValue();
                            Map<String, Object> polluntmap = new HashMap<>();
                            polluntmap.put("pollutantcode", pocode);
                            polluntmap.put("pollutantname", pollutants.get(pocode));
                            polluntmap.put("pollutantvalue", "");
                            polluntmap.put("rate", "");
                            if (polist != null && polist.size() > 0) {
                                polluntmap.put("pollutantvalue", polist.get(0).get("AvgStrength"));
                                double ratevalue = polist.get(0).get("ChangeMultiple") != null ? Double.parseDouble(polist.get(0).get("ChangeMultiple").toString()) : 0;
                                polluntmap.put("rate", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(ratevalue * 100)) + "%");
                            }
                            pollutantdata.add(polluntmap);
                        }
                    }
                    Optional<Float> max1 = datas.stream().filter(m -> m.get("ChangeMultiple") != null && Float.valueOf(m.get("ChangeMultiple").toString()) >= 0).map(m -> Float.valueOf(m.get("ChangeMultiple").toString())).max(Float::compareTo);
                    Optional<Float> min1 = datas.stream().filter(m -> m.get("ChangeMultiple") != null && Float.valueOf(m.get("ChangeMultiple").toString()) >= 0).map(m -> Float.valueOf(m.get("ChangeMultiple").toString())).min(Float::compareTo);
                    String pollutantStr = datas.stream().filter(m -> m.get("PollutantCode") != null).map(m -> pollutants.get(m.get("PollutantCode").toString())).distinct().collect(Collectors.joining("、"));
                    Float ChangeMultipleMin = null;
                    Float ChangeMultipleMax = null;
                    if (max1.isPresent()) {
                        ChangeMultipleMax = max1.get();
                    }
                    if (min1.isPresent()) {
                        ChangeMultipleMin = min1.get();
                    }
                    if (ChangeMultipleMax.floatValue() != ChangeMultipleMin.floatValue()) {
                        mappedResult.put("flowrate", format.format(ChangeMultipleMin * 100) + "%" + "-" + format.format(ChangeMultipleMax * 100) + "%");
                    } else if (ChangeMultipleMax != null) {
                        mappedResult.put("flowrate", format.format(ChangeMultipleMax * 100) + "%");
                    }
                    List<Integer> allpoint = result.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dataGatherCode.equals(m.get("DataGatherCode")) && monitorTime.equals(format(m.get("MonitorTime").toString(), "yyyy-MM-dd"))
                    ).map(m -> format(m.get("MonitorTime").toString(), "yyyy-MM-dd HH")).map(m -> Integer.valueOf(m.substring(m.length() - 2, m.length())))
                            .sorted(Comparator.comparing(m -> m)).collect(Collectors.toList());
                    List<List<Integer>> line = groupIntegerList(allpoint);
                    int count = 0;
                    for (List<Integer> intlist : line) {
                        count += intlist.size();
                    }
                    String line1 = getLine(line);
                    if (line1.length() > 0) {
                        line1 = line1.substring(0, line1.length() - 1);
                    }
                    LinkedList<Integer> integers = new LinkedList<>(allpoint);
                    if (remind == FlowChangeEnum.getCode()) {  //排放量
                        mappedResult.put("remindcode", remind);
                        mappedResult.put("remindname", FlowChangeEnum.getName());
                    } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
                        mappedResult.put("remindcode", remind);
                        mappedResult.put("remindname", ConcentrationChangeEnum.getName());
                    }
                    mappedResult.put("count", count);
                    mappedResult.put("pollutantdata", pollutantdata);
                    mappedResult.put("allpoint", line1);
                    mappedResult.put("pollutantStr", pollutantStr);
                    mappedResult.put("starttime", monitorTime + " " + df.format(integers.getFirst() == null ? 0 : Integer.valueOf(integers.getFirst().toString())));
                    mappedResult.put("endtime", monitorTime + " " + df.format(integers.getLast() == null ? 0 : Integer.valueOf(integers.getLast().toString())));
                }
            }
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/11 0011 下午 2:39
     * @Description: 通过自定义参数获取已读用户列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> getReadUserIdsByParamMap(Map<String, Object> paramMap) {
        List<String> dgimns = (List) paramMap.get("dgimns");
        String collect = paramMap.get("collect") == null ? "" : paramMap.get("collect").toString();
        String datalist = paramMap.get("datalist") == null ? "" : paramMap.get("datalist").toString();
        String monitorTime = paramMap.get("monitortime") == null ? "" : paramMap.get("monitortime").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and(paramMap.get("monitortimetype").toString()).is(DataFormatUtil.parseDate(monitorTime));
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if (StringUtils.isNotBlank(datalist)) {
            Criteria criteria1 = new Criteria();
            criteria1.and(datalist + ".IsSuddenChange").is(true);
            operations.add(Aggregation.match(criteria1));
        }
        operations.add(Aggregation.project("ReadUserIds", "DataGatherCode"));
        List<Map> readUserIds = mongoTemplate.aggregate(newAggregation(
                operations), collect, Map.class).getMappedResults();

        return readUserIds;

    }

    /**
     * @author: chengzq
     * @date: 2019/7/11 0011 上午 11:45
     * @Description: 将一个集合中的所有数字根据是否连续分成多个集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    public static List<List<Integer>> groupIntegerList(List<Integer> list) {
        list = list.stream().sorted(Integer::compareTo).collect(Collectors.toList());
        List<List<Integer>> datas = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        Integer temp = null;
        for (int i = 0; i < list.size(); i++) {
            Integer integer = list.get(i);
            if (temp != null && integer - temp > 1) {
                datas.add(data);
                data = new ArrayList<>();
            }
            if (list.size() - 1 == i) {
                datas.add(data);
            }

            data.add(integer);
            temp = integer;
        }
        return datas;
    }


    /**
     * @author: chengzq
     * @date: 2019/7/11 0011 上午 11:55
     * @Description: 转换字符串
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    public static String getLine(List<List<Integer>> list) {
        String line = "";
        for (List<Integer> integers : list) {
            if (integers.size() > 0) {
                List<String> data = new ArrayList<>();
                Integer integer = integers.get(0);
                Integer integer1 = integers.get(integers.size() - 1);
                data.add(integer + "时");
                data.add(integer1 + "时");
                String collect = data.stream().distinct().collect(Collectors.joining("-"));
                line += collect + "、";
            }
        }
        return line;
    }


    private Map<String, Object> getOnlineChangeExcelTitle(Integer monitortype, Integer remindtype) {
        Map<String, Object> excelTitleInfo = new HashMap<>();
        LinkedList<String> headers = new LinkedList<>();
        headers.add("日期");
        headers.add("突增时段");
        headers.add("突增幅度");
        LinkedList<String> headersField = new LinkedList<>();
        headersField.add("monitortime");
        headersField.add("allpoint");
        headersField.add("flowrate");
        String excelname = "";
        String partname = "";

        switch (getObjectByCode(remindtype)) {
            case ConcentrationChangeEnum: //浓度
                partname = ConcentrationChangeEnum.getName() + "预警";
                break;
            case FlowChangeEnum: //排放量
                partname = FlowChangeEnum.getName() + "预警";
                break;
        }
        switch (getCodeByInt(monitortype)) {
            case WasteWaterEnum:
                excelname = "废水" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case WasteGasEnum:
                excelname = "废气" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case SmokeEnum:
                excelname = "烟气" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case RainEnum:
                excelname = "雨水" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case unOrganizationWasteGasEnum:
                excelname = "无组织废气" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case FactoryBoundarySmallStationEnum:
                excelname = "厂界小型站" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case FactoryBoundaryStinkEnum:
                excelname = "厂界恶臭" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;

            case AirEnum:
                excelname = "大气" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case EnvironmentalVocEnum:
                excelname = "VOC" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case EnvironmentalStinkEnum:
                excelname = "恶臭" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case MicroStationEnum:
                excelname = "微站" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;

            case EnvironmentalDustEnum:
                excelname = "扬尘" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case WaterQualityEnum:
                excelname = "水质" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
            case StorageTankAreaEnum:
                excelname = "储罐" + partname;
                headers.addFirst("储罐编码");
                headers.addFirst("区域名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("storagetankareaname");
            case SecurityLeakageMonitor:
                excelname = "安全泄露" + partname;
                headers.addFirst("监测点名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case SecurityCombustibleMonitor:
                excelname = "可燃易爆气体" + partname;
                headers.addFirst("监测点名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case SecurityToxicMonitor:
                excelname = "有毒有害气体" + partname;
                headers.addFirst("监测点名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
        }

        excelTitleInfo.put("headers", headers);
        excelTitleInfo.put("headersfield", headersField);
        excelTitleInfo.put("excelname", excelname);
        return excelTitleInfo;
    }

    /**
     * @author: xsm
     * @date: 2019/09/04 0004 下午 3:29
     * @Description: 统计监测点在开始时间到结束时间范围内的预警（浓度突变：分钟数据，超阈值）和超标（数据超限）总次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countMonitorPointEarlyAndOverDataByParamMap(String starttime, String endtime, List<String> dgimns) {
        List<Map<String, Object>> result = new ArrayList<>();
        //浓度突变
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).
                and("MinuteDataList.IsSuddenChange").is(true).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime + ":00")).lte(DataFormatUtil.getDateYMDHMS(endtime + ":59"));
        criteria1.and("MinuteDataList.IsSuddenChange").is(true);
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), unwind("MinuteDataList"), match(criteria1), project("DataGatherCode", "count").and(DateOperators.DateToString
                        .dateOf("MonitorTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode").count().as("count")), "MinuteData", Map.class).getMappedResults();
        //超阈值
        criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).
                and("EarlyWarnTime").gte(DataFormatUtil.getDateYMDHMS(starttime + ":00")).lte(DataFormatUtil.getDateYMDHMS(endtime + ":59"));
        List<Map> earlyResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "count").and(DateOperators.DateToString
                        .dateOf("EarlyWarnTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode").count().as("count")), "EarlyWarnData", Map.class).getMappedResults();
        //超限（超标）
        criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(DataFormatUtil.getDateYMDHMS(starttime + ":00")).lte(DataFormatUtil.getDateYMDHMS(endtime + ":59"));
        List<Map> overResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "count").and(DateOperators.DateToString
                        .dateOf("OverTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode").count().as("count")), "OverData", Map.class).getMappedResults();
        //按MN号进行分组 得到对应的预警超标数据
        for (String mn : dgimns) {
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("dgimn", mn);
            resultmap.put("concentrationchange", "");
            for (Map<String, Object> map : mappedResults) {
                if (mn.equals(map.get("_id"))) {
                    resultmap.put("concentrationchange", map.get("count"));
                    break;
                }
            }
            resultmap.put("early", "");
            for (Map<String, Object> map : earlyResults) {
                if (mn.equals(map.get("_id"))) {
                    resultmap.put("early", map.get("count"));
                    break;
                }
            }
            resultmap.put("over", "");
            for (Map<String, Object> map : overResults) {
                if (mn.equals(map.get("_id"))) {
                    resultmap.put("over", map.get("count"));
                    break;
                }
            }
            result.add(resultmap);
        }
        return result;
    }

    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 下午 1:25
     * @Description: 缓存报警提醒信息到redis中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [mqmessage, sessionid]
     * @throws:
     */
    @Override
    public Object cacheAlarmRemindDataInRedis(String mqmessage, String userId,
                                              List<JSONObject> objectList) throws ParseException {
        Map<String, Object> data = new HashMap<>();
        //1将消息解析获取企业名称，时间
        JSONObject jsonObject = JSONObject.fromObject(mqmessage);


        if (jsonObject.get("MQMessage") != null && !"".equals(jsonObject.getString("OnlineStatus"))) {//点位状态消息

            JSONObject resultJson = getResultJson(jsonObject, userId);

            return resultJson;
        }
        boolean isSend = false;
        Object IsContinueOver = jsonObject.get("IsContinueOver");
        Object IsContinueException = jsonObject.get("IsContinueException");
        Object IsContinueChange = jsonObject.get("IsContinueChange");

        if (IsContinueOver == null && IsContinueException == null && IsContinueChange == null) {//三个字段同时不存在
            isSend = true;
        } else if (JSONNull.getInstance().equals(IsContinueOver)
                && JSONNull.getInstance().equals(IsContinueException)
                && JSONNull.getInstance().equals(IsContinueChange)) {//三个字段同时为null
            isSend = true;
        } else if (!JSONNull.getInstance().equals(IsContinueOver) && !jsonObject.getBoolean("IsContinueOver")) {
            isSend = true;
        } else if (!JSONNull.getInstance().equals(IsContinueException) && !jsonObject.getBoolean("IsContinueException")) {
            isSend = true;
        } else if (!JSONNull.getInstance().equals(IsContinueChange) && !jsonObject.getBoolean("IsContinueChange")) {
            isSend = true;
        }


        if (!isSend) {
            return null;
        }


        Object mqMessage = jsonObject.get("MQMessage");
        if (mqMessage == null) {
            return mqMessage;
        }
        List<Map<String, Object>> mqMessageMap = (List) mqMessage;


        String pollutionName = jsonObject.get("PollutionName") == null ? "" : jsonObject.get("PollutionName").toString();
        String OutPutName = jsonObject.get("OutPutName") == null ? "" : jsonObject.get("OutPutName").toString();
        String MN = jsonObject.get("MN") == null ? "" : jsonObject.get("MN").toString();
        String monitorPointTypeCode = jsonObject.get("MonitorPointTypeCode") == null ? "" : jsonObject.get("MonitorPointTypeCode").toString();
        String dateTime = jsonObject.get("DateTime") == null ? "" : jsonObject.get("DateTime").toString();

        String ConcentrationChange = WaterConcentrationChangeMessage.getMonitorAlarmType().substring(WaterConcentrationChangeMessage.getMonitorAlarmType().indexOf("_") + 1);
        String FlowChange = WaterFlowChangeMessage.getMonitorAlarmType().substring(WaterFlowChangeMessage.getMonitorAlarmType().indexOf("_") + 1);

        if (StringUtils.isBlank(pollutionName)) {
            pollutionName = OutPutName;
        } else {
            pollutionName = pollutionName + "_" + OutPutName;
        }


        //获取当日23:59:59时间戳
        Long unixTimeInMillis = getUnixTimeInMillis();
        //2从redis取出缓存的数据对比时间是不是相同   rediskey：alarmremind
        JSONArray resultData = new JSONArray();
        String finalPollutionName = pollutionName;
        for (Map<String, Object> dataMap : mqMessageMap) {
            List<String> alarmType = new ArrayList<>();

            JSONArray array = RedisTemplateUtil.getCache("alarmremind", JSONArray.class);
            resultData = array == null ? new JSONArray() : array;
            String pollutantName = dataMap.get("PollutantName") == null ? "" : dataMap.get("PollutantName").toString();
            String alarmTypes = dataMap.get("AlarmType") == null ? "" : dataMap.get("AlarmType").toString();
            alarmType.add(alarmTypes);
            if (jsonObject.get("DataType") != null && !jsonObject.get("DataType").toString().equals("HourData") && (alarmTypes.equals(ConcentrationChange) || alarmTypes.equals(FlowChange))) {
                continue;
            }

            //如果redis没有查到数据将数据缓存
            List<Map<String, Object>> remindData = new ArrayList<>();
            if (array == null) {
                List<Map<String, Object>> AlarmData = new ArrayList<>();
                AssembleDataAndCacheInRedis(data, pollutionName, dateTime, pollutantName, monitorPointTypeCode, alarmType, remindData, AlarmData, MN);
                if (DataFormatUtil.parseDateYMD(new Date()).getTime() != DataFormatUtil.parseDateYMD(dateTime.substring(0, 10)).getTime()) {
                    continue;
                }
                RedisTemplateUtil.putCacheWithExpireAtTime("alarmremind", JSONArray.fromObject(remindData), unixTimeInMillis);
                continue;
            }

            //如果没有相同污染源的报警数据直接将该数据添加到redis中
            List<Map<String, Object>> collect = (List) array.stream().filter(m -> ((Map) m).get("PollutionName") != null && ((Map) m).get("PollutionName").toString().equals(finalPollutionName)).collect(Collectors.toList());
            if (collect.size() == 0) {
                List<Map<String, Object>> AlarmData = new ArrayList<>();
                AssembleDataAndCacheInRedis(data, pollutionName, dateTime, pollutantName, monitorPointTypeCode, alarmType, remindData, AlarmData, MN);
                array.add(data);
                if (DataFormatUtil.parseDateYMD(new Date()).getTime() != DataFormatUtil.parseDateYMD(dateTime.substring(0, 10)).getTime()) {
                    continue;
                }
                RedisTemplateUtil.putCacheWithExpireAtTime("alarmremind", JSONArray.fromObject(array), unixTimeInMillis);
                continue;
            }


            array.stream().filter(m -> ((Map) m).get("PollutionName") != null && ((Map) m).get("PollutionName").toString().equals(finalPollutionName)).peek(m -> {
                String DateTime = ((Map) m).get("DateTime") == null ? "" : ((Map) m).get("DateTime").toString();
                List<Map<String, Object>> AlarmData = (List) ((Map) m).get("AlarmData");
                //如果新传入的数据时间比redis中的时间早  直接返回
                if (DataFormatUtil.parseDate(DateTime).getTime() > DataFormatUtil.parseDate(dateTime).getTime() || DataFormatUtil.parseDateYMD(new Date()).getTime() != DataFormatUtil.parseDateYMD(dateTime.substring(0, 10)).getTime()) {
                    return;
                }
                //如果相同将污染物以及告警类型合并
                if (StringUtils.isNotBlank(DateTime) && dateTime.equals(DateTime)) {
//                    String PollutantName = alarmData.get("PollutantName") == null ? "" : alarmData.get("PollutantName").toString();
                    int size = AlarmData.stream().filter(map -> map.get("PollutantName") != null && map.get("PollutantName").toString().equals(pollutantName)).collect(Collectors.toList()).size();
                    if (size > 0) {
                        AlarmData.stream().filter(map -> map.get("PollutantName") != null && map.get("PollutantName").toString().equals(pollutantName)).peek(map -> {
                            String monitorPointType = map.get("MonitorPointTypeCode") == null ? "" : map.get("MonitorPointTypeCode").toString();
                            if (monitorPointType.equals(monitorPointTypeCode)) {
                                alarmType.addAll((List) map.get("AlarmType"));
                                map.put("AlarmType", alarmType.stream().distinct().collect(Collectors.toList()));
                            }
                        }).collect(Collectors.toList());
                    }
                }
                //如果不相同将最新数据缓存起来
                else if (StringUtils.isNotBlank(DateTime) && !dateTime.equals(DateTime)) {
                    data.put("PollutionName", finalPollutionName);
                    data.put("DateTime", dateTime);
                    AlarmData.clear();
                }
                List<String> collect1 = alarmType.stream().distinct().collect(Collectors.toList());
                AssembleDataAndCacheInRedis(data, finalPollutionName, dateTime, pollutantName, monitorPointTypeCode, collect1, remindData, AlarmData, MN);
                int i = array.indexOf(m);
                if (i > 0) {
                    array.remove(i);
                    array.add(i, data);
                    RedisTemplateUtil.putCacheWithExpireAtTime("alarmremind", JSONArray.fromObject(array), unixTimeInMillis);
                }
            }).collect(Collectors.toList());
        }

        //将消息鉴权后返回
        for (Object resultDatum : resultData) {
            if (((Map) resultDatum).get("PollutionName") != null
                    && finalPollutionName.equals(((Map) resultDatum).get("PollutionName").toString())) {
                String DateTime = ((Map) resultDatum).get("DateTime") == null ? "" : ((Map) resultDatum).get("DateTime").toString();
                if (DataFormatUtil.parseDate(DateTime).getTime() > DataFormatUtil.parseDate(dateTime).getTime()
                        || DataFormatUtil.parseDateYMD(new Date()).getTime() != DataFormatUtil.parseDateYMD(dateTime.substring(0, 10)).getTime()) {
                    return null;
                }
            }
        }
        List<String> pollutants = new ArrayList<>();
        List<String> alarms = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            //对消息进行鉴权
            Map<String, Object> map = dataAuthentication(objectList, mqMessageMap, pollutants, alarms, monitorPointTypeCode, jsonObject, ConcentrationChange, FlowChange);
            List<Integer> remindType = new ArrayList<>();
            mqMessageMap.stream().filter(m -> m.get("AlarmType") != null && !"".equals(m.get("AlarmType").toString())).peek(m -> {
                String s = m.get("AlarmType").toString();
                remindType.add(CommonTypeEnum.RemindTypeCodeEnum.getCodeByString(s));
            }).collect(Collectors.toList());
            if (map.size() > 0) {
                Map<String, Object> paramTemp = new HashMap<>();
                paramTemp.put("outputids", Arrays.asList());
                paramTemp.put("monitorpointtype", monitorPointTypeCode);
                List<Map<String, Object>> monitorPoints = getMonitorPointDataByParam(paramTemp);
                Map<String, Object> mnAndPollutionName = new HashMap<>();
                Map<String, Object> mnAndMonitorPointName = new HashMap<>();
                Map<String, Object> mnAndMonitorPointId = new HashMap<>();
                Map<String, Object> mnAndPollutionId = new HashMap<>();
                String mnCommon;
                for (Map<String, Object> mapIndex : monitorPoints) {
                    mnCommon = mapIndex.get("dgimn").toString();
                    mnAndPollutionName.put(mnCommon, mapIndex.get("pollutionname"));
                    mnAndMonitorPointName.put(mnCommon, mapIndex.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, mapIndex.get("monitorpointid"));
                    mnAndPollutionId.put(mnCommon, mapIndex.get("pk_pollutionid"));
                }

                map.put("PollutionName", finalPollutionName);
                map.put("remindType", remindType.stream().distinct().collect(Collectors.toList()));
                map.put("dgimns", Arrays.asList(MN));
                map.put("dgimn", MN);
                //map.put("pollutionname", mnAndPollutionName.get(MN));
                map.put("monitorpointid", mnAndMonitorPointId.get(MN));
                map.put("pollutionid", mnAndPollutionId.get(MN));
                map.put("DateTime", dateTime);
                map.put("monitorpointtype", monitorPointTypeCode);
                datalist.add(map);
                List<String> alarmNameList = (List<String>) map.get("AlarmType");


                String messageTitle = dateTime + "，"
                        + finalPollutionName + "，"
                        + map.get("PollutantName") + "，" + DataFormatUtil.FormatListToString(alarmNameList, "、");
                resultMap.put("userid", userId);
                resultMap.put("messagetitle", messageTitle);
                resultMap.put("datalist", datalist);
                JSONObject resultJson = JSONObject.fromObject(resultMap);
                return resultJson;
            }
        }

        return null;

    }

    /**
     * @author: lip
     * @date: 2020/3/17 0017 上午 10:54
     * @Description: 返回点位状态数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private JSONObject getResultJson(JSONObject jsonObject, String userId) {
        Map<String, Object> dataMap = new HashMap<>();
        String PollutionName = jsonObject.getString("PollutionName");
        String OutPutName = jsonObject.getString("OutPutName");
        if (StringUtils.isNotBlank(PollutionName)) {
            OutPutName = PollutionName + "-" + OutPutName;
        }
        String alarmType = CommonTypeEnum.OnlineStatusEnum.getNameByCode(jsonObject.getString("OnlineStatus"));
        if (StringUtils.isNotBlank(alarmType)) {
            dataMap.put("PollutionName", OutPutName);
            dataMap.put("alarmtype", Arrays.asList(alarmType));
            dataMap.put("dgimn", jsonObject.get("MN"));
            //dataMap.put("monitorpointid", mnAndMonitorPointId.get(MN));
            //dataMap.put("pollutionid", mnAndPollutionId.get(MN));
            dataMap.put("DateTime", jsonObject.get("DateTime"));
            dataMap.put("monitorpointtype", jsonObject.get("MonitorPointTypeCode"));
            List<Map<String, Object>> datalist = new ArrayList<>();
            datalist.add(dataMap);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userid", userId);
            resultMap.put("messagetitle", userId);
            resultMap.put("datalist", datalist);
            return JSONObject.fromObject(resultMap);
        } else {
            return null;
        }

    }

    /**
     * @author: chengzq
     * @date: 2019/9/3 0003 下午 7:58
     * @Description: 获取当日23:59:59时间戳
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Long getUnixTimeInMillis() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar instance = Calendar.getInstance();
        String time = instance.get(Calendar.YEAR) + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(Calendar.DATE) + " 23:59:59";
        long unixTimeInMillis = format.parse(time).getTime();
        return unixTimeInMillis;
    }

    /**
     * @author: chengzq
     * @date: 2019/9/3 0003 下午 8:02
     * @Description: 组装数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [data, pollutionName, dateTime, pollutantName, alarmType, remindData, unixTimeInMillis]
     * @throws:
     */
    private void AssembleDataAndCacheInRedis(Map<String, Object> data, String pollutionName, String dateTime, String pollutantName, String monitorPointTypeCode, List<String> alarmType, List<Map<String, Object>> remindData, List<Map<String, Object>> AlarmData, String MN) {
        data.put("PollutionName", pollutionName);
        data.put("DateTime", dateTime);
        Map<String, Object> map = new HashMap<>();
        map.put("PollutantName", pollutantName);
        map.put("MN", MN);
        map.put("AlarmType", alarmType);
        map.put("MonitorPointTypeCode", monitorPointTypeCode);
        data.put("AlarmData", AlarmData);
        if (!AlarmData.contains(map)) {
            AlarmData.add(map);
        }
        remindData.add(data);
    }


    /**
     * @author: chengzq
     * @date: 2019/9/9 0009 下午 1:15
     * @Description: 鉴定用户是否有数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Map<String, Object> dataAuthentication(List<JSONObject> objectList, List<Map<String, Object>> dataList, List<String> pollutants, List<String> alarms, String monitorPointTypeCode, JSONObject jsonObject, String ConcentrationChange, String FlowChange) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map<String, Object> dataMap : dataList) {
            String alarmTypes = dataMap.get("AlarmType") == null ? "" : dataMap.get("AlarmType").toString();
            Integer AlarmLevel = dataMap.get("AlarmLevel") == null ? -2 : "null".equals(dataMap.get("AlarmLevel").toString()) ? -2 : Integer.valueOf(dataMap.get("AlarmLevel").toString());
            String ExceptionType = dataMap.get("ExceptionType") == null ? "" : dataMap.get("ExceptionType").toString();
            /*if (jsonObject.get("DataType") != null && !jsonObject.get("DataType").toString().equals("HourData") && (alarmTypes.equals(ConcentrationChange) || alarmTypes.equals(FlowChange))) {
                continue;
            }*/
            //乐平需要推送小时报警
            if (jsonObject.get("DataType") != null && (alarmTypes.equals(ConcentrationChange) || alarmTypes.equals(FlowChange))) {
                continue;
            }
            String alarmType = dataMap.get("AlarmType") == null ? "" : dataMap.get("AlarmType").toString();
            if (objectList != null && objectList.size() > 0) {
                boolean isHaveRight = isHaveRight(objectList, getMenuCodeByType(monitorPointTypeCode + "_" + alarmType));
                if (true) {
                    if (dataMap.get("PollutantName") != null && StringUtils.isNotBlank(dataMap.get("PollutantName").toString())) {
                        if ("Online_Exception".equals(alarmType)) {
                            pollutants.add(dataMap.get("PollutantName").toString() + "[" + CommonTypeEnum.ExceptionTypeEnum.getNameByCode(ExceptionType).replaceAll("异常", "") + "]");
                            alarms.add(getNameByString(getMenuCodeByType(monitorPointTypeCode + "_" + alarmType).get(0)));
                        } else if ("Online_OverStandard".equals(alarmType)) {
                            pollutants.add(dataMap.get("PollutantName").toString());
                            alarms.add(getNameByString(getMenuCodeByType(monitorPointTypeCode + "_" + alarmType).get(0)));
                            //} else if ("Online_Over".equals(alarmType)) {
                        } else if ("Online_OverLimit".equals(alarmType)) {
                            pollutants.add(dataMap.get("PollutantName").toString() + "[" + CommonTypeEnum.SecurityAlarmTypeEnum.getNameByCode(AlarmLevel).replaceAll("超标", "") + "]");
                            alarms.add(getNameByString(getMenuCodeByType(monitorPointTypeCode + "_" + alarmType).get(0)));
                        }

                    }
                }
            }
        }
        if (pollutants.size() > 0 && alarms.size() > 0) {
            resultMap.put("PollutantName", pollutants.stream().distinct().collect(Collectors.joining("、")));
            resultMap.put("AlarmType", alarms.stream().distinct().collect(Collectors.toList()));
        }
        return resultMap;
    }

    /**
     * @author: chengzq
     * @date: 2019/9/4 0004 上午 10:43
     * @Description: 递归菜单权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [objectList, menuCode]
     * @throws:
     */
    public static boolean isHaveRight(List<JSONObject> objectList, List<String> menuCodes) {
        boolean isHaveRight = false;
        if (menuCodes != null && menuCodes.size() > 0) {
            for (JSONObject jsonObject : objectList) {
                if (menuCodes.contains(jsonObject.get("menucode"))) {
                    isHaveRight = true;
                    break;
                } else if (jsonObject.get("datalistchildren") != null) {
                    isHaveRight = isHaveRight(jsonObject.getJSONArray("datalistchildren"), menuCodes);
                    if (isHaveRight) {
                        break;
                    }
                } else {
                    isHaveRight = false;
                }

            }
        }
        return isHaveRight;
    }


    /**
     * @author: xsm
     * @date: 2019/10/25 0025 下午 12:07
     * @Description: 自定义条件查询在线超阈值、超限、异常数据（通过mn号和日时间分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public Map<String, Object> getOnlineContinuityDataGroupMmAndMonitortime(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            List<String> exceptionlist = (List<String>) paramMap.get("exceptionlist");
            int monitortype = Integer.parseInt(paramMap.get("monitortype").toString());
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString() + " 00:00:00";
            String endtime = paramMap.get("endtime").toString() + " 23:59:59";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            Integer remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            String timefield = "";
            String collection = "";
            if (remindtype == EarlyAlarmEnum.getCode()) {  //阈值
                timefield = "EarlyWarnTime";
                collection = earlyWarnData_db;
            } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                timefield = "ExceptionTime";
                collection = exceptionData_db;
            } else if (remindtype == OverAlarmEnum.getCode()) { //超标
                timefield = "OverTime";
                collection = overData_db;
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
                if (exceptionlist != null && exceptionlist.size() > 0) {
                    criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").in(exceptionlist);
                } else {
                    if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() && remindtype == ExceptionAlarmEnum.getCode()) {//若为废水异常  则无流量异常不参与查询
                        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
                    } else {
                        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
                    }
                }
            } else {
                criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
            }
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timefield)
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime")
            );
            long totalCount = 0;
            //获取分组总数
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Map.class);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                totalCount = resultsCount.getMappedResults().size();
                pageEntity.setTotalCount(totalCount);
                int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
                pageEntity.setPageCount(pageCount);
            }
            //获取全部列表数据
            ArrayOperators.In isContainsUser = ArrayOperators.In.arrayOf("$ReadUserIds").containsValue(paramMap.get("usercode"));
            ConditionalOperators.Cond condOperation = ConditionalOperators.when(isContainsUser)
                    .thenValueOf("1")
                    .otherwise("0");
            DataTypeOperators.Type $ReadUserIds = DataTypeOperators.typeOf("$ReadUserIds");
            ConditionalOperators.Cond condOperation1 = ConditionalOperators.when(ComparisonOperators.Eq.valueOf($ReadUserIds).equalToValue("array"))
                    .thenValueOf(condOperation)
                    .otherwise("0");
            operations.clear();
            criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
                if (exceptionlist != null && exceptionlist.size() > 0) {
                    criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").in(exceptionlist);
                } else {
                    if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() && remindtype == ExceptionAlarmEnum.getCode()) {//若为废水异常  则无流量异常不参与查询
                        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
                    } else {
                        criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
                    }
                }
            } else {
                criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
            }

            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "OverMultiple", "maxvalue", "minvalue", "firsttime", "lasttime", timefield, "PollutantCode", "ExceptionType")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(condOperation1).as("isread")
            );
            Map<String, Object> timeAndRead = new HashMap<>();
            timeAndRead.put("time", "$" + timefield);
            timeAndRead.put("isread", "$isread");

            if (remindtype == ExceptionAlarmEnum.getCode()) {    //若查异常数据 则需返回异常类型（零值异常、连续值异常、超限异常、无流量异常等）
                Map<String, Object> exceptiontypeandread = new HashMap<>();
                exceptiontypeandread.put("pollutantcode", "$PollutantCode");
                exceptiontypeandread.put("exceptiontype", "$ExceptionType");
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime")
                                .max("OverMultiple").as("maxvalue")
                                .min("OverMultiple").as("minvalue")
                                .min(timefield).as("firsttime")
                                .max(timefield).as("lasttime")
                                .push(exceptiontypeandread).as("ExceptionTypeList")
                                .push(timeAndRead).as("timeList")
                                .push("PollutantCode").as("pollutantCodes")
                );
            } else { //超标 超阈值
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime")
                                .max("OverMultiple").as("maxvalue")
                                .min("OverMultiple").as("minvalue")
                                .min(timefield).as("firsttime")
                                .max(timefield).as("lasttime")
                                .push(timeAndRead).as("timeList")
                                .push("PollutantCode").as("pollutantCodes")
                );
            }
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "DataGatherCode"));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("total", pageEntity.getTotalCount());
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Document> timeList;

            List<String> pollutantCodes;
            List<String> exceptiontypes;
            List<String> dateList;
            List<String> noReadDate;
            List<String> noReadList;
            List<String> allList;
            List<String> hasReadList;
            String ymdhms;
            String continuityvalue;
            String ymd = DataFormatUtil.getDateYMD(new Date());
            Date nowTime;
            String nowString;
            Date startTime;
            Date endTime;
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("datagathercode", document.get("DataGatherCode"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                dataMap.put("maxvalue", document.get("maxvalue"));
                dataMap.put("minvalue", document.get("minvalue"));
                if (document.get("firsttime") != null && document.getDate("lasttime") != null) {
                    String first_time = DataFormatUtil.getDateYMDHMS(document.getDate("firsttime"));
                    String last_time = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                    if (first_time.equals(last_time)) {
                        dataMap.put("firsttime", DataFormatUtil.getDateYMD(document.getDate("firsttime")) + " 00:00:00");
                    } else {
                        dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                    }
                    dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                } else {
                    dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                    dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                }
                dataMap.put("exceptiontype", document.get("ExceptionType"));
                timeList = (List<Document>) document.get("timeList");
                pollutantCodes = ((List<String>) document.get("pollutantCodes")).stream().distinct().collect(Collectors.toList());
                if (remindtype == ExceptionAlarmEnum.getCode()) {
                    exceptiontypes = ((List<String>) document.get("ExceptionTypeList")).stream().distinct().collect(Collectors.toList());
                    dataMap.put("exceptiontypes", exceptiontypes);
                }
                dataMap.put("pollutantCodes", pollutantCodes);
                dateList = new ArrayList<>();
                noReadDate = new ArrayList<>();
                for (Document time : timeList) {
                    ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                    if (!dateList.contains(ymdhms)) {
                        dateList.add(ymdhms);
                        if ("0".equals(time.get("isread"))) {
                            noReadDate.add(ymdhms);
                        }
                    }
                }
                int interval;
                //获取配置的各类型排口的间隔时间
                if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//废气
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
                } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//废水、雨水
                    if (remindtype == ExceptionAlarmEnum.getCode()) {
                        interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
                    } else {
                        interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutputover.minute"));
                    }
                } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
                } else {//其它类型监测点
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
                }
                continuityvalue = DataFormatUtil.mergeContinueDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                dataMap.put("continuityvalue", continuityvalue);
                allList = Arrays.asList(continuityvalue.split("、"));
                noReadList = new ArrayList<>();
                hasReadList = new ArrayList<>();
                if (allList.size() > 0) {
                    for (String startAndEnd : allList) {
                        if (startAndEnd.indexOf("-") >= 0) {
                            startTime = DataFormatUtil.getDateYMDHMS(ymd + " " + startAndEnd.split("-")[0] + ":00");
                            endTime = DataFormatUtil.getDateYMDHMS(ymd + " " + startAndEnd.split("-")[1] + ":59");
                            for (String noRead : noReadDate) {
                                nowTime = DataFormatUtil.getDateYMDHMS(ymd + " " + noRead.split(" ")[1]);
                                if (DataFormatUtil.belongCalendar(nowTime, startTime, endTime)) {
                                    noReadList.add(startAndEnd);
                                    break;
                                }
                            }
                        } else {
                            for (String noRead : noReadDate) {
                                nowString = DataFormatUtil.FormatDateOneToOther(noRead, "yyyy-MM-dd HH:mm:ss", "HH:mm");
                                if (nowString.equals(startAndEnd)) {
                                    noReadList.add(startAndEnd);
                                    break;
                                }
                            }
                        }


                    }
                    //已读
                    for (String hasRead : allList) {
                        if (!noReadList.contains(hasRead)) {
                            hasReadList.add(hasRead);
                        }
                    }
                }
                dataMap.put("readed", DataFormatUtil.FormatListToString(hasReadList, "、"));
                dataMap.put("noread", DataFormatUtil.FormatListToString(noReadList, "、"));
                dataList.add(dataMap);
            }
            resultMap.put("datalist", dataList);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/28 0028 下午 3:09
     * @Description: 获取预警、报警、异常列表表头信息(导出)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public List<Map<String, Object>> getTableTitleForConcentrationContinuity(Integer remindtype, Integer monitorpointtype, List<Integer> monitortypes) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        if (monitortypes.contains(monitorpointtype)) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "企业名称");
            map.put("align", "center");
            tableTitleData.add(map);
            if (monitorpointtype == WasteWaterEnum.getCode() || monitorpointtype == WasteGasEnum.getCode() || monitorpointtype == RainEnum.getCode() || monitorpointtype == SmokeEnum.getCode()) {
                Map<String, Object> map2 = new HashMap<>();
                map2.put("minwidth", "180px");
                map2.put("headeralign", "center");
                map2.put("fixed", "left");
                map2.put("showhide", true);
                map2.put("prop", "monitorpointname");
                map2.put("label", "排口名称");
                map2.put("align", "center");
                tableTitleData.add(map2);
            } else {
                Map<String, Object> map2 = new HashMap<>();
                map2.put("minwidth", "180px");
                map2.put("headeralign", "center");
                map2.put("fixed", "left");
                map2.put("showhide", true);
                map2.put("prop", "monitorpointname");
                map2.put("label", "监测点名称");
                map2.put("align", "center");
                tableTitleData.add(map2);
            }
        } else if (monitorpointtype == AirEnum.getCode() ||
                monitorpointtype == EnvironmentalVocEnum.getCode() ||
                monitorpointtype == EnvironmentalStinkEnum.getCode() ||
                monitorpointtype == EnvironmentalDustEnum.getCode() ||
                monitorpointtype == WaterQualityEnum.getCode() ||
                monitorpointtype == MicroStationEnum.getCode()) {//大气
            Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "monitorpointname");
            map2.put("label", "监测点名称");
            map2.put("align", "center");
            tableTitleData.add(map2);
        } else if (monitorpointtype == StorageTankAreaEnum.getCode()) {//贮罐
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "区域名称");
            map.put("align", "center");
            tableTitleData.add(map);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "monitorpointname");
            map2.put("label", "储罐编号");
            map2.put("align", "center");
            tableTitleData.add(map2);
        } else if (monitorpointtype == SecurityLeakageMonitor.getCode()
                || monitorpointtype == SecurityCombustibleMonitor.getCode()
                || monitorpointtype == SecurityToxicMonitor.getCode()) {//安全泄露、可燃易爆气体、有毒有害气体
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "风险区域名称");
            map.put("align", "center");
            tableTitleData.add(map);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "monitorpointname");
            map2.put("label", "监测点名称");
            map2.put("align", "center");
            tableTitleData.add(map2);
        }
        Map<String, Object> map3 = new HashMap<>();
        map3.put("minwidth", "180px");
        map3.put("headeralign", "center");
        map3.put("fixed", "left");
        map3.put("showhide", true);
        map3.put("prop", "monitortime");
        map3.put("label", "日期");
        map3.put("align", "center");
        tableTitleData.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("minwidth", "180px");
        map4.put("headeralign", "center");
        map4.put("fixed", "left");
        map4.put("showhide", true);
        map4.put("prop", "pollutantnames");
        map4.put("label", "污染物");
        map4.put("align", "center");
        tableTitleData.add(map4);

        Map<String, Object> map5 = new HashMap<>();
        map5.put("minwidth", "180px");
        map5.put("headeralign", "center");
        map5.put("fixed", "left");
        map5.put("showhide", true);
        map5.put("prop", "continuityvalue");
        if (remindtype == EarlyAlarmEnum.getCode()) {    //超阈值
            map5.put("label", "超阈值时段");
        } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //超限
            map5.put("label", "异常时段");
        } else if (remindtype == OverAlarmEnum.getCode()) {    //超限
            map5.put("label", "超限时段");
        }
        map5.put("align", "center");
        tableTitleData.add(map5);

        if (remindtype == OverAlarmEnum.getCode()) {    //超限
            Map<String, Object> map6 = new HashMap<>();
            map6.put("minwidth", "180px");
            map6.put("headeralign", "center");
            map6.put("fixed", "left");
            map6.put("showhide", true);
            map6.put("prop", "overmultiple");
            map6.put("label", "超标倍数");
            map6.put("align", "center");
            tableTitleData.add(map6);
        }

        return tableTitleData;
    }


    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 6:37
     * @Description: 自定义参数查询分钟在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> getMinuteDataByParams(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        List<String> dgimns = paramMap.get("dgimns") == null ? new ArrayList<>() : (List<String>) paramMap.get("dgimns");
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHM(startDate)).lte(DataFormatUtil.getDateYMDHM(endDate));
        criteria1.and("MinuteDataList.PollutantCode").in((List) paramMap.get("pollutantcodes"));
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), unwind("MinuteDataList"), match(criteria1), project("DataGatherCode",
                        "MinuteDataList.PollutantCode", "MinuteDataList.AvgStrength").and(DateOperators.DateToString.dateOf("MonitorTime").
                        toString("%Y-%m-%d %H:%M").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")), minuteCollection, Map.class).getMappedResults();

        return mappedResults;
    }


    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 8:01
     * @Description: 自定义参数查询小时排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> getHourFlowDataByParams(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        List<String> dgimns = paramMap.get("dgimns") == null ? new ArrayList<>() : (List<String>) paramMap.get("dgimns");

        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDH(startDate)).lte(DataFormatUtil.getDateYMDH(endDate));
        criteria1.and("HourFlowDataList.PollutantCode").in((List) paramMap.get("pollutantcodes"));
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), unwind("HourFlowDataList"), match(criteria1), project("DataGatherCode",
                        "HourFlowDataList.PollutantCode", "HourFlowDataList.AvgFlow")), hourFlowCollection, Map.class).getMappedResults();

        return mappedResults;
    }

    /**
     * @author: lip
     * @date: 2019/11/2 0002 上午 10:53
     * @Description: 自定义查询条件获取设备状态表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDeviceStatusDataByParam(Map<String, Object> paramMap) {
        return onlineMapper.getDeviceStatusDataByParam(paramMap);
    }


    /**
     * @author: lip
     * @date: 2019/11/4 0004 上午 11:36
     * @Description: 自定义查询条件获取重点监控污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getKeyPollutantsByParam(Map<String, Object> paramTemp) {
        return pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(paramTemp);
    }

    /**
     * @author: lip
     * @date: 2019/11/15 0015 上午 9:11
     * @Description: 自定义查询条件获取水质站点数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWaterStationMNSByParam(Map<String, Object> paramMap) {
        return onlineMapper.getWaterStationMNSByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 4:43
     * @Description: 自定义查询条件获取水质评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getWaterEvaluateDataByParamMap(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("EvaluateTime").gte(startDate).lte(endDate));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                query.addCriteria(Criteria.where("EvaluateTime").gte(startDate));
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("EvaluateTime").lte(endDate));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("OverDataList.PollutantCode").in(pollutantcodes));
        }

        if (paramMap.get("dataTypes") != null) {
            List<String> dataTypes = (List<String>) paramMap.get("dataTypes");
            query.addCriteria(Criteria.where("DataType").in(dataTypes));
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "EvaluateTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "EvaluateTime"));
        }
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 9:00
     * @Description: 获取废水、废气、雨水、厂界恶臭和环境恶臭所有点位MN号和基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutionOutputMnAndStinkMonitorPointMn(Map<String, Object> ParamMap) {
        return onlineMapper.getPollutionOutputMnAndStinkMonitorPointMn(ParamMap);
    }

    /**
     * @author: lip
     * @date: 2019/11/25 0025 上午 9:33
     * @Description: 自定义查询获取实时数据最新的一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getLatestRealTimeDataByParamMap(Map<String, Object> paramMap) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Date endtime = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endtime);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String starttime = df.format(calendar.getTime()) + " 00:00:00";
        Criteria criteria = new Criteria();

        List<AggregationOperation> aggregations = new ArrayList<>();

        List<String> mns = (List<String>) paramMap.get("mns");
        List<String> pollutantCodes = (List<String>) paramMap.get("pollutantcodes");
        criteria.and("DataGatherCode").in(mns).and("RealDataList.PollutantCode").in(pollutantCodes).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(endtime);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "maxtime", "MonitorTime"));
        GroupOperation groupOperation = group("DataGatherCode").max("MonitorTime").as("maxtime");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, realTimeCollection, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;

    }

    /**
     * @author: lip
     * @date: 2019/11/25 0025 上午 9:33
     * @Description: 自定义查询获取小时数据最新的一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getLastHourDataByParamMap(Map<String, Object> paramMap) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date endtime = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endtime);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String starttime = df.format(calendar.getTime()) + " 00:00:00";
        Criteria criteria = new Criteria();

        List<AggregationOperation> aggregations = new ArrayList<>();

        List<String> mns = (List<String>) paramMap.get("mns");
        List<String> pollutantCodes = (List<String>) paramMap.get("pollutantcodes");
        criteria.and("DataGatherCode").in(mns).and("HourDataList.PollutantCode").in(pollutantCodes).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(endtime);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "maxtime", "MonitorTime"));
        GroupOperation groupOperation = group("DataGatherCode").max("MonitorTime").as("maxtime");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;

    }


    @Override
    public List<Map<String, Object>> getOutPutsAndPollutantAlarmSetByParam(Map<String, Object> paramMap) {
        return onlineMapper.getOutPutsAndPollutantAlarmSetByParam(paramMap);
    }

    @Override
    public Map<String, Object> getPollutantAlarmTypes(String dgimn, List<String> pollutantcodes, String ymd) {
        Map<String, Object> codeAndPollutantAlarmType = new HashMap<>();
        String pollutantCode;

        Date startDate = DataFormatUtil.getDateYMDHMS(ymd + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(ymd + " 23:59:59");

        List<AggregationOperation> aggregations = new ArrayList<>();

        Criteria criteria = Criteria.where("DataGatherCode").in(dgimn).and("PollutantCode").in(pollutantcodes).and("MonitorTime").gte(startDate).lte(endDate);
        aggregations.add(match(criteria));

        Aggregation aggregation = newAggregation(aggregations);

        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, pollutantAlarminfoCollection, Document.class).getMappedResults();


        for (Document conChange : mappedResults) {
            pollutantCode = conChange.getString("PollutantCode");
            Integer ExceptionType = conChange.get("ExceptionType") == null ? -1 : Integer.valueOf(conChange.get("ExceptionType").toString());
            Integer AlarmLevel = conChange.get("AlarmLevel") == null ? -1 : Integer.valueOf(conChange.get("AlarmLevel").toString());
            Boolean IsOverStandard = conChange.get("IsOverStandard") == null ? false : Boolean.valueOf(conChange.get("IsOverStandard").toString());

            if (ExceptionType > 0 || AlarmLevel > 0 || IsOverStandard) {
                codeAndPollutantAlarmType.put(pollutantCode, "alarmdata");
            } else {
                codeAndPollutantAlarmType.put(pollutantCode, "earlydata");
            }
        }


        return codeAndPollutantAlarmType;
    }


    /**
     * @author: chengzq
     * @date: 2020/1/9 0009 下午 3:40
     * @Description: 获取最新两条实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> getLatestTwoRealTimeDataByParamMap(Map<String, Object> paramMap) {
        Date now = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(now);
        instance.add(Calendar.HOUR, -2);
        Date starttime = instance.getTime();
        instance.setTime(now);
        instance.add(Calendar.HOUR, 2);
        Date endtime = instance.getTime();

        List<AggregationOperation> aggregations = new ArrayList<>();

        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(starttime).lte(endtime);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime"));
        aggregations.add(sort(Sort.Direction.DESC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, realTimeCollection, Map.class);
        List<Map> resultDocument = results.getMappedResults();


        List<Map> result = new ArrayList<>();
        for (String mn : mns) {
            Optional<Map> first = resultDocument.stream().filter(m -> m.get("DataGatherCode") != null && mn.equals(m.get("DataGatherCode").toString())).findFirst();
            if (first.isPresent()) {
                Map map = first.get();
                Optional<Map> first1 = resultDocument.stream().filter(m -> m.get("DataGatherCode") != null && mn.equals(m.get("DataGatherCode").toString()) && m != map).findFirst();
                if (first1.isPresent()) {
                    Map map1 = first1.get();
                    result.add(map);
                    result.add(map1);
                }
            }
        }
        return result;

    }

    /**
     * @author: chengzq
     * @date: 2020/2/10 0010 下午 1:00
     * @Description: 通过自定义条件查询水质在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map> getWaterStationOnlineDataByParamMap(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        String datetype = paramMap.get("datetype") == null ? "" : paramMap.get("datetype").toString();
        List<String> alarmtypes = paramMap.get("alarmtypes") == null ? new ArrayList<>() : (ArrayList) paramMap.get("alarmtypes");
        String collection = "";
        List<String> dgimns = (List) paramMap.get("dgimns");
        String datalist = "";

        Criteria criteria = new Criteria();
        if ("minute".equals(datetype)) {
            collection = minuteCollection;
            datalist = "MinuteDataList";
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate + ":00")).lte(DataFormatUtil.getDateYMDHMS(endDate + ":59"));
        } else if ("hour".equals(datetype)) {
            collection = hourCollection;
            datalist = "HourDataList";
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate + ":00:00")).lte(DataFormatUtil.getDateYMDHMS(endDate + ":59:59"));
        } else if ("day".equals(datetype)) {
            collection = dayCollection;
            datalist = "DayDataList";
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endDate + " 23:59:59"));
        } else if ("month".equals(datetype)) {
            collection = monthCollection;
            datalist = "MonthDataList";
            String lastDay = DataFormatUtil.getLastDayOfMonth(endDate);
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate + "-01 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(lastDay + " 23:59:59"));
        }


        Criteria alarmCriteria = new Criteria();
        List<Criteria> orOperator = new ArrayList<>();

        //正常数据
        if (alarmtypes.contains("normal")) {
            Criteria criteria1 = new Criteria();
            criteria1.and(datalist + ".IsOver").is(-1).and(datalist + ".IsException").is(0).and(datalist + ".IsOverStandard").is(false).and(datalist + ".IsSuddenChange").is(false);

            List<Criteria> orOperator1 = new ArrayList<>();
            //预警
            orOperator1.add(Criteria.where(datalist + ".IsOver").is(0));
            //超标
            orOperator1.add(Criteria.where(datalist + ".IsOver").gt(0));
            orOperator1.add(Criteria.where(datalist + ".IsOverStandard").is(true));
            //异常
            orOperator1.add(Criteria.where(datalist + ".IsException").gt(0));
            //突变
            orOperator1.add(Criteria.where(datalist + ".IsSuddenChange").is(true));
            Criteria criteria2 = new Criteria();
            criteria2.orOperator(orOperator1.toArray(new Criteria[orOperator1.size()]));
            criteria1.norOperator(criteria2);

            orOperator.add(criteria1);
        }
        //预警
        if (alarmtypes.contains("earlywarn")) {
            orOperator.add(Criteria.where(datalist + ".IsOver").is(0));
        }
        //超标
        if (alarmtypes.contains("overdata")) {
            orOperator.add(Criteria.where(datalist + ".IsOverStandard").is(true));
            orOperator.add(Criteria.where(datalist + ".IsOver").gt(0));
        }
        //异常
        if (alarmtypes.contains("exception")) {
            orOperator.add(Criteria.where(datalist + ".IsException").gt(0));
        }
        //突变
        if (alarmtypes.contains("suddenchange")) {
            orOperator.add(Criteria.where(datalist + ".IsSuddenChange").is(true));
        }
        alarmCriteria.orOperator(orOperator.toArray(new Criteria[orOperator.size()]));
        if (alarmtypes.size() > 0) {
            criteria.andOperator(alarmCriteria);
        }
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(match(criteria)), collection, Map.class).getMappedResults();

        return mappedResults;
    }

    @Override
    public List<Map> getGroundWaterOnlineDataByParamMap(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        List<String> alarmtypes = paramMap.get("alarmtypes") == null ? new ArrayList<>() : (ArrayList) paramMap.get("alarmtypes");
        String collection = "";
        List<String> dgimns = (List) paramMap.get("dgimns");
        String datalist = "";
        Criteria criteria = new Criteria();
        collection = groundWaterCollection;
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endDate + " 23:59:59"));
        Criteria alarmCriteria = new Criteria();
        List<Criteria> orOperator = new ArrayList<>();
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(match(criteria)), collection, Map.class).getMappedResults();
        return mappedResults;
    }

    /**
     * @author: chengzq
     * @date: 2020/2/11 0011 下午 5:08
     * @Description: 通过自定义条件查询超标数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> getOverDataByParamMap(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        String datetype = paramMap.get("datetype") == null ? "" : paramMap.get("datetype").toString();


        List<String> dgimns = (List) paramMap.get("dgimns");
        Criteria criteria = new Criteria();

        if ("day".equals(datetype)) {
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(DataFormatUtil.getDateYMDHMS(startDate + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endDate + " 23:59:59"));
        } else {
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(DataFormatUtil.getDateYMDHMS(startDate + ":00:00")).lte(DataFormatUtil.getDateYMDHMS(endDate + ":59:59"));
        }


        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(match(criteria)), overData_db, Map.class).getMappedResults();

        return mappedResults;
    }

    @Override
    public List<Map> getExceptionDataByParamMap(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();


        List<String> dgimns = (List) paramMap.get("dgimns");

        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(DataFormatUtil.getDateYMDHMS(startDate + ":00:00")).lte(DataFormatUtil.getDateYMDHMS(endDate + ":59:59"));

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(match(criteria)), exceptionData_db, Map.class).getMappedResults();

        return mappedResults;
    }

    @Override
    public Map<String, Object> getAlarmsDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        Map<String, Object> resultMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = new ArrayList<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (List) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);

        List<Map> earlyWarn = new ArrayList<>();
        List<Map> concentrationchange = new ArrayList<>();
        List<Map> flowchange = new ArrayList<>();
        List<Map> exception = new ArrayList<>();
        List<Map> overdata = new ArrayList<>();


        if (remindTypes.contains(ConcentrationChangeEnum.getCode())) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourData = mongoTemplate.aggregate(newAggregation(match(criteria)), hourCollection, Map.class).getMappedResults();
            concentrationchange.addAll(hourData);
            resultMap.put("concentrationchange", concentrationchange);
        }

        if (remindTypes.contains(FlowChangeEnum.getCode())) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourFlowData = mongoTemplate.aggregate(newAggregation(match(criteria)), hourFlowCollection, Map.class).getMappedResults();
            flowchange.addAll(hourFlowData);
            resultMap.put("flowchange", flowchange);
        }

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria)), earlyWarnData_db, Map.class).getMappedResults();
            earlyWarn.addAll(early);
            resultMap.put("earlywarn", earlyWarn);
        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria)), exceptionData_db, Map.class).getMappedResults();
            exception.addAll(exceptiondata);
            resultMap.put("exception", exception);
        }
        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria)), overData_db, Map.class).getMappedResults();
            overdata.addAll(exceptiondata);
            resultMap.put("overdata", overdata);
        }


        return resultMap;
    }

    /**
     * @author: chengzq
     * @date: 2020/3/10 0010 下午 3:13
     * @Description: 查询单污染物突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantSuddenChangeDataByParamMap(Map<String, Object> paramMap) {
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        List<String> pollutantcodes = paramMap.get("pollutantcodes") == null ? new ArrayList<>() : (List<String>) paramMap.get("pollutantcodes");

        List<Map<String, Object>> pollutantinfo = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);

        List<String> dgimns = (List) paramMap.get("dgimns");

        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate)).lte(DataFormatUtil.getDateYMDHMS(endDate));
        List<Document> mappedResults = mongoTemplate.aggregate(newAggregation(match(criteria)), hourCollection, Document.class).getMappedResults();

        List<Map<String, Object>> resultList = new ArrayList<>();
        mappedResults.stream().filter(m -> m.get("HourDataList") != null && m.get("MonitorTime") != null).peek(m -> {
            List<Map<String, Object>> hourDataList = (List<Map<String, Object>>) m.get("HourDataList");
            hourDataList.stream().filter(n -> n.get("IsSuddenChange") != null && (boolean) n.get("IsSuddenChange") && n.get("PollutantCode") != null && pollutantcodes.contains(n.get("PollutantCode").toString()))
                    .forEach(n -> {
                        try {
                            Map<String, Object> data = new HashMap<>();
                            String PollutantCode = n.get("PollutantCode") == null ? "" : n.get("PollutantCode").toString();
                            Optional<Map<String, Object>> first = pollutantinfo.stream().filter(map -> map.get("pollutantcode") != null && map.get("pollutantcode").toString().equals(PollutantCode)).findFirst();
                            if (first.isPresent()) {
                                data.put("pollutantname", first.get().get("pollutantname"));
                            }
                            data.put("timepoint", DataFormatUtil.formatCST(m.get("MonitorTime").toString()).substring(11, 13));
                            data.put("pollutantcode", PollutantCode);
                            resultList.add(data);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    });
        }).collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> collect = resultList.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
        List<Map<String, Object>> datas = new ArrayList<>();
        for (String pollutantname : collect.keySet()) {
            List<Map<String, Object>> list = collect.get(pollutantname);
            Map<String, Object> data = new HashMap<>();
            list.stream().filter(m -> m.get("timepoint") != null).forEach(m -> {
                List<Integer> timepoints = new ArrayList<>();
                data.put("pollutantcode", m.get("pollutantcode"));
                data.put("pollutantname", m.get("pollutantname"));
                timepoints.add(Integer.valueOf(m.get("timepoint").toString()));
                data.put("timepoints", timepoints);
            });
            datas.add(data);
        }

        datas.stream().filter(m -> m.get("timepoints") != null).forEach(m -> {
            List<Integer> timepoints = (List<Integer>) m.get("timepoints");
            List<List<Integer>> line = groupIntegerList(timepoints);
            String line1 = getLine(line);
            if (line1.length() > 0) {
                line1 = line1.substring(0, line1.length() - 1);
            }
            m.put("timepoints", line1);
        });
        return datas;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 5:28
     * @Description: 补全时间以及设置时间格式化表达式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datetype, starttime, endtime, pattern]
     * @throws:
     */
    private Map<String, String> getDateFormate(String datetype, String starttime, String endtime, String pattern) throws ParseException {
        Map<String, String> data = new HashMap<>();
        if ("day".equals(datetype)) {
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            pattern = "%Y-%m-%d";
        } else if ("month".equals(datetype)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            starttime = starttime + "-01 00:00:00";
            Calendar instance = Calendar.getInstance();
            instance.setTime(format.parse(endtime));
            int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
            endtime = endtime + "-" + actualMaximum + " 23:59:59";
            pattern = "%Y-%m";
        } else if ("year".equals(datetype)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            starttime = starttime + "-01-01 00:00:00";
            Calendar instance = Calendar.getInstance();
            instance.setTime(format.parse(endtime + "-12"));
            int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
            endtime = endtime + "-12-" + actualMaximum + " 23:59:59";
            pattern = "%Y";
        } else if ("hour".equals(datetype)) {
            starttime = starttime + ":00:00";
            endtime = endtime + ":59:59";
            pattern = "%Y-%m-%d %H";
        } else if ("realtime".equals(datetype)) {
            pattern = "%Y-%m-%d %H:%M:%S";
        } else if ("minute".equals(datetype)) {
            starttime = starttime + ":00";
            endtime = endtime + ":59";
            pattern = "%Y-%m-%d %H:%M";
        }
        data.put("starttime", starttime);
        data.put("endtime", endtime);
        data.put("pattern", pattern);
        return data;
    }

    /**
     * @author: xsm
     * @date: 2020/3/12 0012 下午 12:49
     * @Description: 获取废水无流量异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getWasteWaterNoFlowExceptionListDataByParams(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            int monitortype = Integer.parseInt(paramMap.get("monitortype").toString());
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString() + " 00:00:00";
            String endtime = paramMap.get("endtime").toString() + " 23:59:59";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            String timefield = "ExceptionTime";
            String collection = exceptionData_db;
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            //只查0值异常数据
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timefield)
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime")
            );
            long totalCount = 0;
            //获取分组总数
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Map.class);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                totalCount = resultsCount.getMappedResults().size();
                pageEntity.setTotalCount(totalCount);
                int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
                pageEntity.setPageCount(pageCount);
            }
            //获取全部列表数据
            ArrayOperators.In isContainsUser = ArrayOperators.In.arrayOf("$ReadUserIds").containsValue(paramMap.get("usercode"));
            ConditionalOperators.Cond condOperation = ConditionalOperators.when(isContainsUser)
                    .thenValueOf("1")
                    .otherwise("0");
            DataTypeOperators.Type $ReadUserIds = DataTypeOperators.typeOf("$ReadUserIds");
            ConditionalOperators.Cond condOperation1 = ConditionalOperators.when(ComparisonOperators.Eq.valueOf($ReadUserIds).equalToValue("array"))
                    .thenValueOf(condOperation)
                    .otherwise("0");
            operations.clear();
            criteria = new Criteria();
            //只查0值异常数据
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "OverMultiple", "maxvalue", "minvalue", "firsttime", "lasttime", timefield, "PollutantCode", "ExceptionType")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(condOperation1).as("isread")
            );
            Map<String, Object> timeAndRead = new HashMap<>();
            timeAndRead.put("time", "$" + timefield);
            timeAndRead.put("isread", "$isread");
            Map<String, Object> exceptiontypeandread = new HashMap<>();
            exceptiontypeandread.put("pollutantcode", "$PollutantCode");
            exceptiontypeandread.put("exceptiontype", "$ExceptionType");
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime")
                            .max("OverMultiple").as("maxvalue")
                            .min("OverMultiple").as("minvalue")
                            .min(timefield).as("firsttime")
                            .max(timefield).as("lasttime")
                            .push(exceptiontypeandread).as("ExceptionTypeList")
                            .push(timeAndRead).as("timeList")
                            .push("PollutantCode").as("pollutantCodes")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "DataGatherCode"));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("total", pageEntity.getTotalCount());
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Document> timeList;

            List<String> pollutantCodes;
            List<String> exceptiontypes;
            List<String> dateList;
            List<String> noReadDate;
            List<String> noReadList;
            List<String> allList;
            List<String> hasReadList;
            String ymdhms;
            String continuityvalue;
            String ymd = DataFormatUtil.getDateYMD(new Date());
            Date nowTime;
            String nowString;
            Date startTime;
            Date endTime;
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("datagathercode", document.get("DataGatherCode"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                dataMap.put("maxvalue", document.get("maxvalue"));
                dataMap.put("minvalue", document.get("minvalue"));
                dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                dataMap.put("exceptiontype", document.get("ExceptionType"));
                timeList = (List<Document>) document.get("timeList");
                pollutantCodes = ((List<String>) document.get("pollutantCodes")).stream().distinct().collect(Collectors.toList());
                exceptiontypes = ((List<String>) document.get("ExceptionTypeList")).stream().distinct().collect(Collectors.toList());
                dataMap.put("exceptiontypes", exceptiontypes);
                dataMap.put("pollutantCodes", pollutantCodes);
                dateList = new ArrayList<>();
                noReadDate = new ArrayList<>();
                for (Document time : timeList) {
                    ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                    if (!dateList.contains(ymdhms)) {
                        dateList.add(ymdhms);
                        if ("0".equals(time.get("isread"))) {
                            noReadDate.add(ymdhms);
                        }
                    }
                }
                int interval;
                //获取配置的各类型排口的间隔时间
                interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
                continuityvalue = DataFormatUtil.mergeContinueDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                dataMap.put("continuityvalue", continuityvalue);
                allList = Arrays.asList(continuityvalue.split("、"));
                noReadList = new ArrayList<>();
                hasReadList = new ArrayList<>();
                if (allList.size() > 0) {
                    for (String startAndEnd : allList) {
                        if (startAndEnd.indexOf("-") >= 0) {
                            startTime = DataFormatUtil.getDateYMDHMS(ymd + " " + startAndEnd.split("-")[0] + ":00");
                            endTime = DataFormatUtil.getDateYMDHMS(ymd + " " + startAndEnd.split("-")[1] + ":59");
                            for (String noRead : noReadDate) {
                                nowTime = DataFormatUtil.getDateYMDHMS(ymd + " " + noRead.split(" ")[1]);
                                if (DataFormatUtil.belongCalendar(nowTime, startTime, endTime)) {
                                    noReadList.add(startAndEnd);
                                    break;
                                }
                            }
                        } else {
                            for (String noRead : noReadDate) {
                                nowString = DataFormatUtil.FormatDateOneToOther(noRead, "yyyy-MM-dd HH:mm:ss", "HH:mm");
                                if (nowString.equals(startAndEnd)) {
                                    noReadList.add(startAndEnd);
                                    break;
                                }
                            }
                        }


                    }
                    //已读
                    for (String hasRead : allList) {
                        if (!noReadList.contains(hasRead)) {
                            hasReadList.add(hasRead);
                        }
                    }
                }
                dataMap.put("readed", DataFormatUtil.FormatListToString(hasReadList, "、"));
                dataMap.put("noread", DataFormatUtil.FormatListToString(noReadList, "、"));
                dataList.add(dataMap);
            }
            resultMap.put("datalist", dataList);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/12 0012 下午 14:57
     * @Description: 统计无流量异常和非无流量异常类型的数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> countNoFlowAndOtherExceptionTypeDataNum(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        //查询条件
        List<String> mns = (List<String>) paramMap.get("dgimns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString() + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString() + " 23:59:59");
        //无流量异常
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("ExceptionTime").gte(startDate).lte(endDate).and("ExceptionType").is(NoFlowExceptionEnum.getCode())
                )
        );
        GroupOperation group = group("DataGatherCode").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, exceptionData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        //非无流量异常
        List<AggregationOperation> operationss = new ArrayList<>();
        operationss.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("ExceptionTime").gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode())
                )
        );
        GroupOperation grouptwo = group("DataGatherCode").count().as("num");
        operationss.add(grouptwo);
        Aggregation aggregationtwo = Aggregation.newAggregation(operationss);
        AggregationResults<Document> resultstwo = mongoTemplate.aggregate(aggregationtwo, exceptionData_db, Document.class);
        List<Document> documentstwo = resultstwo.getMappedResults();
        if (documents.size() > 0) {
            result.put("noflownum", documents.size());
        } else {
            result.put("noflownum", 0);
        }
        if (documentstwo.size() > 0) {
            result.put("otherexceptionnum", documentstwo.size());
        } else {
            result.put("otherexceptionnum", 0);
        }
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 10:51
     * @Description: 查询异常范围表中监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Document> getExceptoinModelDataByParamMap(Map<String, Object> paramMap) {
        //查询条件
        List<String> mns = (List<String>) paramMap.get("dgimns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());

        Criteria criteria = Criteria.where("MN").in(mns).and("FirstExceptionTime").gte(startDate).and("LastExceptionTime").lte(endDate);
        Aggregation aggregation = Aggregation.newAggregation(match(criteria));
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, exceptionModelCollection, Document.class);
        List<Document> documentstwo = results.getMappedResults();

        return documentstwo;
    }


    /**
     * @author: lip
     * @date: 2020/6/23 0023 下午 3:27
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getExceptionDetailDataByParamMap(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("MN").in(mns));
        }
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            query.addCriteria(Criteria.where("FirstExceptionTime").gte(startDate).and("LastExceptionTime").lte(endDate));
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        if (paramMap.get("dataTypes") != null) {
            List<String> dataTypes = (List<String>) paramMap.get("dataTypes");
            query.addCriteria(Criteria.where("DataType").in(dataTypes));
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "FirstExceptionTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "FirstExceptionTime"));
        }
        return mongoTemplate.find(query, Document.class, exceptionModelCollection);
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 10:51
     * @Description: 统计异常范围表中监测个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Document> countExceptoinModelDataByParamMap(Map<String, Object> paramMap) {
        //查询条件
        List<String> mns = (List<String>) paramMap.get("dgimns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());

        Criteria criteria = Criteria.where("MN").in(mns).and("FirstExceptionTime").gte(startDate).and("LastExceptionTime").lte(endDate);
        Aggregation aggregation = Aggregation.newAggregation(match(criteria), project("MN"), group("MN").count().as("count"));
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, exceptionModelCollection, Document.class);
        List<Document> documentstwo = results.getMappedResults();

        return documentstwo;
    }

    @Override
    public List<Document> countDayExceptoinModelDataByParamMap(Map<String, Object> paramMap) {
        //查询条件
        List<String> mns = (List<String>) paramMap.get("dgimns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());

        Criteria criteria = Criteria.where("MN").in(mns).and("FirstExceptionTime").gte(startDate).and("LastExceptionTime").lte(endDate);
        Aggregation aggregation = Aggregation.newAggregation(match(criteria), project("MN").and(DateOperators.DateToString.dateOf("FirstExceptionTime").toString("%Y-%m-%d")
                .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("MonitorTime").count().as("count"));
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, exceptionModelCollection, Document.class);
        List<Document> documentstwo = results.getMappedResults();
        return documentstwo;
    }

    /**
     * @author: xsm
     * @date: 2020/8/18 17:08
     * @Description: 根据站点MN号获取最新一条点位监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Document getOneMonitorPointLastDataByParam(String dgimn, String datatypestr) {
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        Document document = mongoTemplate.findOne(query, Document.class, datatypestr);
        return document;
    }


    /**
     * @author: chengzq
     * @date: 2020/9/5 0005 下午 4:16
     * @Description: 统计企业下废水，废气，烟气的流量排放时段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointFlowDataByParam(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> resultlist = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        List<String> dgimns = (List) paramMap.get("dgimns");
        List<Map<String, Object>> outPutInfos = paramMap.get("outPutInfos") == null ? new ArrayList<>() : (List<Map<String, Object>>) paramMap.get("outPutInfos");
        Map<String, String> outputMap = outPutInfos.stream().filter(m -> m.get("DGIMN") != null && m.get("OutputName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("OutputName").toString()));
        Map<String, String> pollutionMap = outPutInfos.stream().filter(m -> m.get("DGIMN") != null && m.get("PollutionName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("PollutionName").toString()));
        Map<String, String> pkpollutionidmap = outPutInfos.stream().filter(m -> m.get("DGIMN") != null && m.get("PK_PollutionID") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("PK_PollutionID").toString()));
        Map<String, String> outputidMap = outPutInfos.stream().filter(m -> m.get("DGIMN") != null && m.get("outputid") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("outputid").toString()));
        Map<String, String> FK_MonitorPointTypeCodeMap = outPutInfos.stream().filter(m -> m.get("DGIMN") != null && m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("FK_MonitorPointTypeCode").toString()));
        String starttime = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorBeginTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime));
        Aggregation aggregation = Aggregation.newAggregation(match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorBeginTime").toString("%Y-%m-%d %H:%M")
                .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorBeginTime").and(DateOperators.DateToString.dateOf("MonitorEndTime").toString("%Y-%m-%d %H:%M")
                .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorEndTime"));
        List<Document> list = mongoTemplate.aggregate(aggregation, emissionPeriodCollection, Document.class).getMappedResults();

        Map<String, List<Document>> collect = list.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorBeginTime") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString() + "_"
                + DataFormatUtil.getDateYMD(DataFormatUtil.getDateYMDHM(m.get("MonitorBeginTime").toString())), Collectors.collectingAndThen(Collectors.toList(), n -> n.stream()
                .sorted(Comparator.comparing(data -> data.get("MonitorBeginTime").toString())).collect(Collectors.toList()))));

        for (String mnAndBegintime : collect.keySet()) {
            Map<String, Object> data = new HashMap<>();
            String[] split = mnAndBegintime.split("_");
            String dgimn = split[0];
            String begintime = split[1];
            List<Document> documents = collect.get(mnAndBegintime);
            String timepoint = "";
            for (Document document : documents) {
                String monitorBeginTime = document.get("MonitorBeginTime") == null ? "" : document.get("MonitorBeginTime").toString();
                String monitorEndTime = document.get("MonitorEndTime") == null ? "" : document.get("MonitorEndTime").toString();
                String begintimepoint = format.format(DataFormatUtil.getDateYMDHM(monitorBeginTime));
                if ("".equals(monitorEndTime)) {
                    timepoint += begintimepoint + "-正在排放、";
                } else {
                    String endtimepoint = format.format(DataFormatUtil.getDateYMDHM(monitorEndTime));
                    timepoint += begintimepoint + "-" + endtimepoint + "、";
                }
            }
            if (documents.size() > 0) {
                Integer FK_MonitorPointTypeCode = FK_MonitorPointTypeCodeMap.get(dgimn) == null ? 0 : Integer.valueOf(FK_MonitorPointTypeCodeMap.get(dgimn));

                data.put("outputname", outputMap.get(dgimn));
                data.put("pollutionname", pollutionMap.get(dgimn));
                data.put("outputid", outputidMap.get(dgimn));
                data.put("fkmonitorpointtypecode", FK_MonitorPointTypeCode);
                data.put("fkmonitorpointtypename", getNameByCode(FK_MonitorPointTypeCode).replace("点类型", ""));
                data.put("pkpollutionid", pkpollutionidmap.get(dgimn));
                data.put("dgimn", dgimn);
                data.put("monitortime", begintime);
                data.put("timepoint", timepoint.substring(0, timepoint.length() - 1));
                data.put("num", documents.size());
                resultlist.add(data);
            }
        }
        return resultlist;
    }

    @Override
    public List<Document> getMonitorPointFlowProblemByParam(Map<String, Object> paramMap) throws Exception {
        //排口流量因子代码集合（废水，废气因子代码不同）
        List<String> flowCode = Arrays.asList("b01", "b02");
        Map<String, List<Document>> alarmmap = paramMap.get("alarmmap") == null ? new HashMap<>() : (Map<String, List<Document>>) paramMap.get("alarmmap");
        Map<String, String> outputdgimnMap = paramMap.get("outputdgimnmap") == null ? new HashMap<>() : (Map<String, String>) paramMap.get("outputdgimnmap");
        List<Document> resultList = new ArrayList<>();
        for (String dgimn : alarmmap.keySet()) {
            List<Document> documentList = alarmmap.get(dgimn);
            for (Document document : documentList) {
                String firstExceptionTime = document.get("FirstExceptionTime").toString();
                String lastExceptionTime = document.get("LastExceptionTime").toString();
                String outputdgimn = outputdgimnMap.get(dgimn);
                Criteria criteria = new Criteria();
                criteria.and("DataGatherCode").is(outputdgimn).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(firstExceptionTime)).lte(DataFormatUtil.getDateYMDHMS(lastExceptionTime))
                        .and("RealDataList.PollutantCode").in(flowCode).and("RealDataList.MonitorValue").ne("0");
                Aggregation aggregation = Aggregation.newAggregation(match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d")
                        .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("DataGatherCode", "MonitorTime").count().as("count"));
                List<Document> mappedResults = mongoTemplate.aggregate(aggregation, realTimeCollection, Document.class).getMappedResults();
                mappedResults.stream().forEach(m -> {
                    m.put("FirstExceptionTime", firstExceptionTime);
                    m.put("LastExceptionTime", lastExceptionTime);
                    m.put("dgimn", dgimn);
                });
                resultList.addAll(mappedResults);
            }
        }

        return resultList;
    }

    /**
     * @author: xsm
     * @date: 2020/10/12 0012 上午 9:38
     * @Description: 统计企业下各类型点位报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Integer> countEntPointEarlyAndOverDataByParamMap(Date startDate, Date endDate, List<String> dgimns, List<Integer> reminds, Map<String, Integer> mnAndType) {
        Map<String, Integer> earlyData = new HashMap<>();
        earlyData.put("waternum", 0);
        earlyData.put("gasnum", 0);
        earlyData.put("smokenum", 0);
        earlyData.put("rainnum", 0);
        earlyData.put("entstinknum", 0);
        earlyData.put("entsmallstationnum", 0);
        earlyData.put("totalnum", 0);
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        if (categorys != null && categorys.size() > 0) {
            if (categorys.contains("2")) {//安全
                earlyData.put("leakagenum", 0);
                earlyData.put("combustiblenum", 0);
                earlyData.put("securitytoxicnum", 0);
                earlyData.put("productionsitenum", 0);
                earlyData.put("storagetanknum", 0);
            }
        }
        for (Integer remind : reminds) {
            if (remind == EarlyAlarmEnum.getCode()) {
                //超阈值
                earlyData = countEntPointEarlyDataByParamMap(startDate, endDate, dgimns, mnAndType, earlyData);
            } else if (remind == OverAlarmEnum.getCode()) {
                //超限
                earlyData = countEntPointOverDataByParamMap(startDate, endDate, dgimns, mnAndType, earlyData);
            } else if (remind == ExceptionAlarmEnum.getCode()) {
                //异常
                earlyData = countEntPointExceptionDataByParamMap(startDate, endDate, dgimns, mnAndType, earlyData);
            } else if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {//浓度突变
                //浓度突变
                earlyData = countEntPointChangeDataByParamMap(startDate, endDate, dgimns, mnAndType, earlyData, CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
            } else if (remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {// 排放量突变
                //排放量突变
                earlyData = countEntPointChangeDataByParamMap(startDate, endDate, dgimns, mnAndType, earlyData, CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
            }
        }
        return earlyData;

    }


    /**
     * @author: xsm
     * @date: 2020/10/12 0012 上午 10:11
     * @Description: 统计企业下各监测点类型的预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countEntPointEarlyDataByParamMap(Date startDate, Date endDate, List<String> mns, Map<String, Integer> mnAndType, Map<String, Integer> earlyData) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("EarlyWarnTime").gte(startDate).lte(endDate)
                )
        );
        operations.add(Aggregation.project("DataGatherCode", "EarlyWarnTime")
                .and(DateOperators.DateToString.dateOf("EarlyWarnTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("Daytime")
        );
        GroupOperation group = group("DataGatherCode", "Daytime").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, earlyWarnData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        if (documents.size() > 0) {
            sumEntPointCountData(mnAndType, documents, earlyData);
        }
        return earlyData;
    }

    /**
     * @author: xsm
     * @date: 2020/10/12 0012 上午 10:12
     * @Description: 统计企业下各监测点类型的浓度突变或排放量突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countEntPointChangeDataByParamMap(Date startDate, Date endDate, List<String> mns, Map<String, Integer> mnAndType, Map<String, Integer> earlyData, Integer alarmtype) {
        //查询条件
        Criteria criteria = Criteria.where("DataGatherCode").in(mns);
        if (alarmtype == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {//浓度突变
            criteria.and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
        } else if (alarmtype == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {// 排放量突变
            criteria.and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
        }
        List<Document> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria),
                project("DataGatherCode", "MonitorTime")
                        .and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("Daytime"),
                group("DataGatherCode", "Daytime").count().as("num")),
                hourCollection,
                Document.class).getMappedResults();
        if (mappedResults != null && mappedResults.size() > 0) {
            sumEntPointCountData(mnAndType, mappedResults, earlyData);
        }
        return earlyData;
    }

    /**
     * @author: xsm
     * @date: 2020/10/12 0012 上午 10:11
     * @Description: 统计企业下各监测点类型的超标数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countEntPointOverDataByParamMap(Date startDate, Date endDate, List<String> mns, Map<String, Integer> mnAndType, Map<String, Integer> earlyData) {
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        operations.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("OverTime").gte(startDate).lte(endDate)
                )
        );
        operations.add(Aggregation.project("DataGatherCode", "OverTime")
                .and(DateOperators.DateToString.dateOf("OverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("Daytime")
        );
        GroupOperation group = group("DataGatherCode", "Daytime").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, overData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        sumEntPointCountData(mnAndType, documents, earlyData);
        return earlyData;
    }

    /**
     * @author: xsm
     * @date: 2020/10/12 0012 上午 10:16
     * @Description: 统计企业下各监测点类型的异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> countEntPointExceptionDataByParamMap(Date startDate, Date endDate, List<String> mns, Map<String, Integer> mnAndType, Map<String, Integer> earlyData) {
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        operations.add(
                Aggregation.match(
                        Criteria.where("DataGatherCode").in(mns).and("ExceptionTime").gte(startDate).lte(endDate)
                )
        );
        operations.add(Aggregation.project("DataGatherCode", "ExceptionTime")
                .and(DateOperators.DateToString.dateOf("ExceptionTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("Daytime")
        );
        GroupOperation group = group("DataGatherCode", "Daytime").count().as("num");
        operations.add(group);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, exceptionData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        sumEntPointCountData(mnAndType, documents, earlyData);
        return earlyData;
    }


    @Override
    public Map<String, Object> getSmokePollutantUpRushDischargeInfo(String starttimeInfo, String endtimeInfo, Integer remindtype, String mn, Map<String, Integer> codeAndIs, Integer collectiontype) {
        Map<String, Object> resultMap = new HashMap<>();
        Date starttime;
        Date endtime;
        String collection = hourCollection;
        String datalistkey = "HourDataList";
        //默认查询小时浓度数据，2为查询分钟浓度数据
        if (collectiontype != null && collectiontype == 2) {
            datalistkey = "MinuteDataList";
            collection = minuteCollection;
        }
        if (starttimeInfo.length() < 11) {
            starttime = DataFormatUtil.parseDate(starttimeInfo + " 00:00:00");
            endtime = DataFormatUtil.parseDate(endtimeInfo + " 23:59:59");
        } else {
            starttime = DataFormatUtil.parseDate(starttimeInfo);
            endtime = DataFormatUtil.parseDate(endtimeInfo);
        }

        Criteria criteria = Criteria.where("DataGatherCode").is(mn)
                .and("PollutantCode").in(codeAndIs.keySet());
        if (starttime != null && endtime != null) {
            criteria.and("MonitorTime").gte(starttime).lte(endtime);
        }
        List<Document> mappedResults = new ArrayList<>();
        String valueKeyName = "";
        if (remindtype == FlowChangeEnum.getCode()) {
            valueKeyName = "CorrectedFlow";
            Fields fields = fields("DataGatherCode", "MonitorTime", "HourFlowDataList.PollutantCode", "HourFlowDataList.CorrectedFlow", "HourFlowDataList.ChangeMultiple", "HourFlowDataList.IsSuddenChange", "_id");
            mappedResults = mongoTemplate.aggregate(newAggregation(
                    unwind("HourFlowDataList"),
                    project(fields),
                    match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), "HourFlowData", Document.class).getMappedResults();
        } else if (remindtype == ConcentrationChangeEnum.getCode()) {
            valueKeyName = "AvgStrength";
            for (String code : codeAndIs.keySet()) {
                if (codeAndIs.get(code) > 0) {
                    valueKeyName = "AvgConvertStrength";
                    break;
                }
            }
            Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", datalistkey + ".AvgStrength", datalistkey + ".AvgConvertStrength", datalistkey + ".ChangeMultiple", datalistkey + ".IsSuddenChange", "_id");
            mappedResults = mongoTemplate.aggregate(newAggregation(
                    unwind(datalistkey),
                    project(fields),
                    match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), collection, Document.class).getMappedResults();
        }
        if (mappedResults.size() == 0) {
            return resultMap;
        }

        if (remindtype == ConcentrationChangeEnum.getCode() && collectiontype != null && collectiontype == 2) {
            setOnePollutantMinuteSuddenChange(resultMap, mappedResults, valueKeyName);
        } else {
            setOnePollutantHourSuddenChange(resultMap, mappedResults, valueKeyName);
        }
        return resultMap;
    }

    private void setOnePollutantHourSuddenChange(Map<String, Object> resultMap, List<Document> mappedResults, String valueKeyName) {
        List<Map<String, Object>> datalist = new ArrayList<>();
        Map<Object, Object> starts = new HashMap<>();   //起始点
        Map<Object, List<Map<String, Object>>> tuzengTimes = new HashMap<>();
        Object initValue = null;
        Object startPointTime = null;
        for (Document mappedResult : mappedResults) {
            Date monitortime = mappedResult.getDate("MonitorTime");
            String dateYMDH = DataFormatUtil.getDateYMDH(monitortime);
            Object value = mappedResult.get(valueKeyName);
            if (value != null) {
                Map<String, Object> datalistMap = new HashMap<>();
                datalistMap.put("monitorvalue", value);
                datalistMap.put("monitortime", dateYMDH);
                datalistMap.put("issuddenchange", mappedResult.get("IsSuddenChange"));
                datalist.add(datalistMap);
            }
            boolean IsSuddenChange = mappedResult.get("IsSuddenChange") == null ? false : Boolean.valueOf(mappedResult.get("IsSuddenChange").toString());
            if (initValue != null && startPointTime != null && value != null) {
                if (IsSuddenChange) {
                    Map<String, Object> mapInfo = new HashMap<>();
                    mapInfo.put("monitortime", dateYMDH);
                    mapInfo.put("monitorvalue", value);
                    if (tuzengTimes.containsKey(startPointTime)) {
                        List<Map<String, Object>> list = tuzengTimes.get(startPointTime);
                        list.add(mapInfo);
                    } else {
                        List<Map<String, Object>> list = new ArrayList<>();
                        list.add(mapInfo);
                        tuzengTimes.put(startPointTime, list);
                    }
                } else {
                    startPointTime = dateYMDH;
                    starts.put(startPointTime, value);
                }
                initValue = value;
            }
            if (initValue == null && value != null) {
                initValue = value;
                startPointTime = dateYMDH;
                starts.put(startPointTime, value);
            }
        }
        List<Map<String, Object>> timepoint = new ArrayList<>();
        if (tuzengTimes.size() > 0) {
            for (Map.Entry<Object, Object> entry : starts.entrySet()) {
                Object key = entry.getKey();
                if (tuzengTimes.containsKey(key)) {
                    Map<String, Object> timepointMap = new HashMap<>();
                    List<Map<String, Object>> startpoints = new ArrayList<>();
                    List<Map<String, Object>> endpoint = new ArrayList<>();
                    Map<String, Object> map = new HashMap<>();
                    Object value = entry.getValue();
                    map.put("monitortime", key);
                    map.put("monitorvalue", value);
                    startpoints.add(map);
                    for (Map.Entry<Object, List<Map<String, Object>>> listEntry : tuzengTimes.entrySet()) {
                        if (listEntry.getKey().equals(key)) {
                            endpoint = listEntry.getValue();
                        }
                    }
                    timepointMap.put("endpoint", endpoint);
                    timepointMap.put("startpoint", startpoints);
                    timepoint.add(timepointMap);
                }
            }
        }
        resultMap.put("timepoint", timepoint);
        resultMap.put("datalist", datalist);
    }

    private void setOnePollutantMinuteSuddenChange(Map<String, Object> resultMap, List<Document> mappedResults, String valueKeyName) {
        List<Map<String, Object>> datalist = new ArrayList<>();
        Map<Object, Object> starts = new HashMap<>();   //起始点
        Map<Object, List<Map<String, Object>>> tuzengTimes = new HashMap<>();
        Object initValue = null;
        Object startPointTime = null;
        for (Document mappedResult : mappedResults) {
            Date monitortime = mappedResult.getDate("MonitorTime");
            String dateYMDH = DataFormatUtil.getDateYMDHM(monitortime);
            Object value = mappedResult.get(valueKeyName);
            if (value != null) {
                Map<String, Object> datalistMap = new HashMap<>();
                datalistMap.put("monitorvalue", value);
                datalistMap.put("monitortime", dateYMDH);
                datalistMap.put("issuddenchange", mappedResult.get("IsSuddenChange"));
                datalist.add(datalistMap);
            }
            boolean IsSuddenChange = mappedResult.get("IsSuddenChange") == null ? false : Boolean.valueOf(mappedResult.get("IsSuddenChange").toString());
            if (initValue != null && startPointTime != null && value != null) {
                if (IsSuddenChange) {
                    Map<String, Object> mapInfo = new HashMap<>();
                    mapInfo.put("monitortime", dateYMDH);
                    mapInfo.put("monitorvalue", value);
                    if (tuzengTimes.containsKey(startPointTime)) {
                        List<Map<String, Object>> list = tuzengTimes.get(startPointTime);
                        list.add(mapInfo);
                    } else {
                        List<Map<String, Object>> list = new ArrayList<>();
                        list.add(mapInfo);
                        tuzengTimes.put(startPointTime, list);
                    }
                } else {
                    startPointTime = dateYMDH;
                    starts.put(startPointTime, value);
                }
                initValue = value;
            }
            if (initValue == null && value != null) {
                initValue = value;
                startPointTime = dateYMDH;
                starts.put(startPointTime, value);
            }
        }
        List<Map<String, Object>> timepoint = new ArrayList<>();
        if (tuzengTimes.size() > 0) {
            for (Map.Entry<Object, Object> entry : starts.entrySet()) {
                Object key = entry.getKey();
                if (tuzengTimes.containsKey(key)) {
                    Map<String, Object> timepointMap = new HashMap<>();
                    List<Map<String, Object>> startpoints = new ArrayList<>();
                    List<Map<String, Object>> endpoint = new ArrayList<>();
                    Map<String, Object> map = new HashMap<>();
                    Object value = entry.getValue();
                    map.put("monitortime", key);
                    map.put("monitorvalue", value);
                    startpoints.add(map);
                    for (Map.Entry<Object, List<Map<String, Object>>> listEntry : tuzengTimes.entrySet()) {
                        if (listEntry.getKey().equals(key)) {
                            endpoint = listEntry.getValue();
                        }
                    }
                    timepointMap.put("endpoint", endpoint);
                    timepointMap.put("startpoint", startpoints);
                    timepoint.add(timepointMap);
                }
            }
        }
        resultMap.put("timepoint", timepoint);
        resultMap.put("datalist", datalist.stream().filter(distinctByKey(m -> m.get("monitortime"))).collect(Collectors.toList()));
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        ConcurrentSkipListMap<Object, Boolean> skipListMap = new ConcurrentSkipListMap<>();
        Predicate<T> predicate = t -> skipListMap.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
        return predicate;
    }


    @Override
    public List<Map<String, Object>> getStinkPointStateAndPollutantsByParam(Map<String, Object> paramMap) {
        return onlineMapper.getStinkPointStateAndPollutantsByParam(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2020/10/12 0012 上午 10:04
     * @Description:统计企业下各类型点位报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sumEntPointCountData(Map<String, Integer> mnAndType, List<Document> documents, Map<String, Integer> earlyData) {
        String tempCode;
        Integer type;
        for (Document document : documents) {
            tempCode = document.getString("DataGatherCode");
            earlyData.put("totalnum", earlyData.get("totalnum") + 1);
            type = mnAndType.get(tempCode);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                //废水
                case WasteWaterEnum:
                    earlyData.put("waternum", earlyData.get("waternum") + 1);
                    break;
                //废气
                case WasteGasEnum:
                    earlyData.put("gasnum", earlyData.get("gasnum") + 1);
                    break;
                //烟气
                case SmokeEnum:
                    earlyData.put("smokenum", earlyData.get("smokenum") + 1);
                    break;
                //雨水
                case RainEnum:
                    earlyData.put("rainnum", earlyData.get("rainnum") + 1);
                    break;
                case FactoryBoundaryStinkEnum:
                    earlyData.put("entstinknum", earlyData.get("entstinknum") + 1);
                    break;
                case FactoryBoundarySmallStationEnum:
                    earlyData.put("entsmallstationnum", earlyData.get("entsmallstationnum") + 1);
                    break;
                case SecurityLeakageMonitor:
                    earlyData.put("leakagenum", earlyData.get("leakagenum") + 1);
                    break;
                case SecurityCombustibleMonitor:
                    earlyData.put("combustiblenum", earlyData.get("combustiblenum") + 1);
                    break;
                case SecurityToxicMonitor:
                    earlyData.put("securitytoxicnum", earlyData.get("securitytoxicnum") + 1);
                    break;
                case ProductionSiteEnum:
                    earlyData.put("productionsitenum", earlyData.get("productionsitenum") + 1);
                    break;
                case StorageTankAreaEnum:
                    earlyData.put("storagetanknum", earlyData.get("storagetanknum") + 1);
                    break;
            }
        }
    }


    /**
     * @author: xsm
     * @date: 2020/12/3 0003 下午 1:26
     * @Description: 自定义查询条件查询Voc相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getVocRelationListDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
        //获取废气mn数组、废气排口名称，企业名称对照关系
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

            setGseAndVocTimeAndValue(pointTimeAndValue, mnAndTimeAndValue, paramMap);
            if (pointTimeAndValue != null && pointTimeAndValue.size() > 0) {
                pointTimeAndValue = FormatUtils.sortByKey(pointTimeAndValue, false);
            }
            Set<String> mnOutputSet = mnAndTimeAndValue.keySet();
            Double relationpercent;
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (pointTimeAndValue.size() > 0) {
                for (String mnkey : mnOutputSet) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutionname", mnAndPollutionname.get(mnkey));
                    dataMap.put("outputname", mnAndOutputname.get(mnkey));
                    dataMap.put("outputid", mnAndOutputid.get(mnkey));
                    dataMap.put("relationstarttime", paramMap.get("relationstarttime"));
                    dataMap.put("relationendtime", paramMap.get("relationendtime"));
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


    /**
     * @author: xsm
     * @date: 2020/12/07 0007 上午 10:04
     * @Description: getVocAndGasRelationChartDataByParamMap
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getVocAndGasRelationChartDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
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
            setGseAndVocTimeAndValue(pointTimeAndValue, mnAndTimeAndValue, paramMap);
            if (pointTimeAndValue != null && pointTimeAndValue.size() > 0) {
                pointTimeAndValue = FormatUtils.sortByKey(pointTimeAndValue, false);
            }
            List<Map<String, Object>> xListData = new ArrayList<>();
            List<Map<String, Object>> yListData = new ArrayList<>();
            Map<String, Object> outputTimeAndValue = mnAndTimeAndValue.get(mnKey);
            List<Double> xData = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            String collection = paramMap.get("collection").toString();
            String beforeTimeKey = "";
            Integer beforeTimeNum = Integer.parseInt(paramMap.get("beforetime").toString());
            for (String time : pointTimeAndValue.keySet()) {
                if (collection.indexOf("Hour") > -1) {//小时数据
                    beforeTimeKey = DataFormatUtil.getBeforeByHourTime(beforeTimeNum, time);
                } else if (collection.indexOf("Minute") > -1) {//分钟数据
                    beforeTimeKey = DataFormatUtil.getBeforeByMinuteTime(beforeTimeNum, time);
                }
                if (outputTimeAndValue.get(beforeTimeKey) != null) {
                    Map<String, Object> xMap = new LinkedHashMap<>();
                    Map<String, Object> yMap = new LinkedHashMap<>();
                    xMap.put("monitortime", time);
                    xMap.put("value", Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    yMap.put("monitortime", beforeTimeKey);
                    yMap.put("value", Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    yData.add(Double.parseDouble(outputTimeAndValue.get(beforeTimeKey).toString()));
                    xData.add(Double.parseDouble(pointTimeAndValue.get(time).toString()));
                    xListData.add(xMap);
                    yListData.add(yMap);
                }
            }
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
        return resultMap;
    }


    /**
     * @author: xsm
     * @date: 2020/12/07 0007 上午 10:31
     * @Description: 设置监测点的时间+值，设置mn+时间+值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    private void setGseAndVocTimeAndValue(Map<String, Object> pointTimeAndValue, Map<String, Map<String, Object>> mnAndTimeAndValue, Map<String, Object> paramMap) {
        //获取废气mn数组、废气排口名称，企业名称对照关系
        String mnKey = "";
        String collection = paramMap.get("collection").toString();
        //获取废气前n个时间的指定污染物监测数据
        Map<String, Object> gasParam = new HashMap<>();
        gasParam.put("starttime", paramMap.get("beforestarttime"));
        gasParam.put("endtime", paramMap.get("beforeendtime"));
        gasParam.put("mns", paramMap.get("mns"));
        gasParam.put("collection", collection);
        gasParam.put("sort", "asc");
        gasParam.put("pollutantcodes", Arrays.asList(paramMap.get("outputpollutant")));
        Query query = setNoGroupQuery(gasParam);
        List<Document> gasDocuments = mongoTemplate.find(query, Document.class, collection);
        if (gasDocuments.size() > 0) {
            String monitortime = "";
            String pollutantDataKey = getPollutantDataKey(collection);
            String valueKey = getValueKey(collection);
            Map<String, Object> timeAndValue;
            List<Document> pollutantData;
            for (Document gasDocument : gasDocuments) {
                mnKey = gasDocument.getString("DataGatherCode");
                monitortime = getMonitorTimeByCollection(collection, gasDocument.getDate("MonitorTime"));
                if (mnAndTimeAndValue.get(mnKey) != null) {
                    timeAndValue = mnAndTimeAndValue.get(mnKey);
                } else {
                    timeAndValue = new LinkedHashMap<>();
                }
                pollutantData = (List<Document>) gasDocument.get(pollutantDataKey);
                for (Document pollutant : pollutantData) {
                    if (pollutant.get(valueKey) != null && !"".equals(pollutant.get(valueKey))) {
                        timeAndValue.put(monitortime, pollutant.get(valueKey));
                    }
                    break;
                }
                mnAndTimeAndValue.put(mnKey, timeAndValue);
            }
            //判断Voc点位的污染物是否为配置的Voc
            boolean queryall = false;
            String voccode = DataFormatUtil.parseProperties("pollutant.voccode");
            List<String> codes = new ArrayList<>();
            if (paramMap.get("monitorpointpollutant") != null && voccode.equals(paramMap.get("monitorpointpollutant").toString())) {
                queryall = true;
                Map<String, Object> param = new HashMap<>();
                param.put("dgimn", paramMap.get("monitorpointmn"));
                param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                List<Map<String, Object>> pollutants = otherMonitorPointMapper.getAllVocPollutantByParam(param);
                if (pollutants != null && pollutants.size() > 0) {
                    for (Map<String, Object> map : pollutants) {
                        if (map.get("pollutantcode") != null) {
                            codes.add(map.get("pollutantcode").toString());
                        }
                    }
                }
            } else {
                if (paramMap.get("monitorpointpollutant") != null) {
                    codes.add(paramMap.get("monitorpointpollutant").toString());
                }
            }
            String datalistname = "";
            if ("MinuteData".equals(collection)) {
                datalistname = "MinuteDataList";
            } else if ("HourData".equals(collection)) {
                datalistname = "HourDataList";
            } else if ("DayData".equals(collection)) {
                datalistname = "DayDataList";
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            Map<String, Object> pollutantList = new HashMap<>();
            pollutantList.put("monitortime", "$MonitorTime");
            pollutantList.put("pollutantcode", "$PollutantCode");
            pollutantList.put("monitorvalue", "$AvgStrength");
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").is(paramMap.get("monitorpointmn").toString()).and("MonitorTime").gte(startDate).lte(endDate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(datalistname));
            operations.add(match(Criteria.where(datalistname + ".PollutantCode").in(codes)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(datalistname + ".AvgStrength").as("AvgStrength")
                    .and(datalistname + ".PollutantCode").as("PollutantCode"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(group("DataGatherCode").push(pollutantList).as("pollutantList"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            if (queryall == false) {
                if (mappedResults.size() > 0 && mappedResults.get(0).get("pollutantList") != null) {//判断查询数据是否为空
                    List<Document> documents = (List<Document>) mappedResults.get(0).get("pollutantList");
                    for (Document document : documents) {
                        String monitorDate = "";
                        if ("MinuteData".equals(collection)) {
                            monitorDate = DataFormatUtil.getDateYMDHM(document.getDate("monitortime"));
                        } else if ("HourData".equals(collection)) {
                            monitorDate = DataFormatUtil.getDateYMDH(document.getDate("monitortime"));
                        } else if ("DayData".equals(collection)) {
                            monitorDate = DataFormatUtil.getDateYMD(document.getDate("monitortime"));
                        }
                        pointTimeAndValue.put(monitorDate, document.get("monitorvalue"));
                    }
                }
            } else {
                if (mappedResults.size() > 0 && mappedResults.get(0).get("pollutantList") != null) {//判断查询数据是否为空
                    List<Document> documents = (List<Document>) mappedResults.get(0).get("pollutantList");
                    Map<String, List<Document>> mapDocuments = new LinkedHashMap<>();
                    if (documents.size() > 0) {
                        if ("MinuteData".equals(collection)) {
                            mapDocuments = documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("monitortime"))));
                        } else if ("HourData".equals(collection)) {
                            mapDocuments = documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDH(m.getDate("monitortime"))));
                        } else if ("DayData".equals(collection)) {
                            mapDocuments = documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMD(m.getDate("monitortime"))));
                        }

                        if (mapDocuments != null && mapDocuments.size() > 0) {
                            for (String key : mapDocuments.keySet()) {
                                List<Document> onetimedata = mapDocuments.get(key);
                                String totalvalue = "";
                                if (onetimedata != null && onetimedata.size() > 0) {
                                    for (Document document1 : onetimedata) {//计算该时间点 VOC污染物浓度值之和
                                        if (!"".equals(totalvalue)) {
                                            totalvalue = (Float.valueOf(totalvalue) + ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d)) + "";
                                        } else {
                                            totalvalue = ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d) + "";
                                        }
                                    }
                                }
                                if (!"".equals(totalvalue)) {
                                    totalvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(totalvalue)));
                                }
                                pointTimeAndValue.put(key, totalvalue);
                            }
                        }
                    }
                }
            }
        }
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
    private void setStinkAndVocMonitorPointTimeValueData(Map<String, Object> pointTimeAndValue, Map<String, Map<String, Object>> mnAndTimeAndValue, Map<String, Object> paramMap) {
        //获取废气mn数组、废气排口名称，企业名称对照关系
        String collection = paramMap.get("collection").toString();
        String datalistname = "";
        List<String> dgimnlist = (List<String>) paramMap.get("dgimnlist");
        Date startDate = DataFormatUtil.getDateYMDHM(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHM(paramMap.get("endtime").toString());
        Date beforestarttime = DataFormatUtil.getDateYMDHM(paramMap.get("beforestarttime").toString());
        Date beforeendtime = DataFormatUtil.getDateYMDHM(paramMap.get("beforeendtime").toString());
        if ("MinuteData".equals(collection)) {
            datalistname = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            datalistname = "HourDataList";
        } else if ("DayData".equals(collection)) {
            datalistname = "DayDataList";
        }
        //判断Voc点位的污染物是否为配置的Voc
        boolean queryall = false;
        String voccode = DataFormatUtil.parseProperties("pollutant.voccode");
        List<String> codes = new ArrayList<>();
        if (paramMap.get("comparepollutantcode") != null && voccode.equals(paramMap.get("comparepollutantcode").toString())) {
            queryall = true;
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimnlist.get(0));
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointMapper.getAllVocPollutantByParam(param);
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null) {
                        codes.add(map.get("pollutantcode").toString());
                    }
                }
            }
        } else {
            codes.add(paramMap.get("comparepollutantcode").toString());
        }
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("monitortime", "$MonitorTime");
        pollutantList.put("pollutantcode", "$PollutantCode");
        pollutantList.put("monitorvalue", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimnlist.get(0)).and("MonitorTime").gte(beforestarttime).lte(beforeendtime);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(datalistname));
        operations.add(match(Criteria.where(datalistname + ".PollutantCode").in(codes)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(datalistname + ".AvgStrength").as("AvgStrength")
                .and(datalistname + ".PollutantCode").as("PollutantCode"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        operations.add(group("DataGatherCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        Map<String, Object> timeAndValue = new HashMap<>();
        if (queryall == false) {
            if (mappedResults.size() > 0 && mappedResults.get(0).get("pollutantList") != null) {//判断查询数据是否为空
                List<Document> documents = (List<Document>) mappedResults.get(0).get("pollutantList");
                for (Document document : documents) {
                    String monitorDate = "";
                    if ("MinuteData".equals(collection)) {
                        monitorDate = DataFormatUtil.getDateYMDHM(document.getDate("monitortime"));
                    } else if ("HourData".equals(collection)) {
                        monitorDate = DataFormatUtil.getDateYMDH(document.getDate("monitortime"));
                    } else if ("DayData".equals(collection)) {
                        monitorDate = DataFormatUtil.getDateYMD(document.getDate("monitortime"));
                    }
                    timeAndValue.put(monitorDate, document.get("monitorvalue"));
                }
            }
        } else {
            if (mappedResults.size() > 0 && mappedResults.get(0).get("pollutantList") != null) {//判断查询数据是否为空
                List<Document> documents = (List<Document>) mappedResults.get(0).get("pollutantList");
                Map<String, List<Document>> mapDocuments = new HashMap<>();
                if (documents.size() > 0) {
                    if ("MinuteData".equals(collection)) {
                        mapDocuments = documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("monitortime"))));
                    } else if ("HourData".equals(collection)) {
                        mapDocuments = documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDH(m.getDate("monitortime"))));
                    } else if ("DayData".equals(collection)) {
                        mapDocuments = documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMD(m.getDate("monitortime"))));
                    }
                    if (mapDocuments != null && mapDocuments.size() > 0) {
                        for (String key : mapDocuments.keySet()) {
                            List<Document> onetimedata = mapDocuments.get(key);
                            String totalvalue = "";
                            if (onetimedata != null && onetimedata.size() > 0) {
                                for (Document document1 : onetimedata) {//计算该时间点 VOC污染物浓度值之和
                                    if (!"".equals(totalvalue)) {
                                        totalvalue = (Float.valueOf(totalvalue) + ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d)) + "";
                                    } else {
                                        totalvalue = ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d) + "";
                                    }
                                }
                            }
                            if (!"".equals(totalvalue)) {
                                totalvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(totalvalue)));
                            }
                            timeAndValue.put(key, totalvalue);
                        }
                    }
                }
            }
        }
        mnAndTimeAndValue.put(dgimnlist.get(0), timeAndValue);
        String monitortime = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        List<Document> pollutantData;
        //获取监测点指定时间指定污染物监测数据
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(paramMap.get("monitorpointmn")));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").is(paramMap.get("monitorpointpollutant")));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> pointDocuments = mongoTemplate.find(query, Document.class, collection);
        if (pointDocuments.size() > 0) {
            String comparepollutantcode = paramMap.get("monitorpointpollutant").toString();
            for (Document point : pointDocuments) {
                monitortime = getMonitorTimeByCollection(collection, point.getDate("MonitorTime"));
                pollutantData = (List<Document>) point.get(pollutantDataKey);
                for (Document pollutant : pollutantData) {
                    if (pollutant.get("PollutantCode").equals(comparepollutantcode)) {
                        if (pollutant.get(valueKey) != null && !"".equals(pollutant.get(valueKey))) {
                            pointTimeAndValue.put(monitortime, pollutant.get(valueKey));
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> getMonitorPointRTSPDataByParam(Map<String, Object> paramMap) {
        return onlineMapper.getMonitorPointRTSPDataByParam(paramMap);
    }


    @Override
    public List<Map<String, Object>> getWaterFlowInfoByParamsGroupbyTime(Map<String, Object> paramsMap) {
        String pollutantcode = paramsMap.get("pollutantcode") == null ? "" : paramsMap.get("pollutantcode").toString();
        String starttime = paramsMap.get("starttime") == null ? "" : paramsMap.get("starttime").toString();
        String endtime = paramsMap.get("endtime") == null ? "" : paramsMap.get("endtime").toString();
        Integer monitorPointType = paramsMap.get("monitorpointtype") == null ? -1 : Integer.valueOf(paramsMap.get("monitorpointtype").toString());
        Integer timetype = paramsMap.get("timetype") == null ? -1 : Integer.valueOf(paramsMap.get("timetype").toString());
        Integer datatype = paramsMap.get("datatype") == null ? -1 : Integer.valueOf(paramsMap.get("datatype").toString());

        if (timetype == null) {
            return new ArrayList<>();
        }
        List<String> allTimePoint = JSONObjectUtil.getAllTimePoint(starttime, endtime);
        String format = JSONObjectUtil.getFormat(starttime.length());
        List<Map<String, Object>> resultList = new ArrayList<>();
        //重点监测排放量污染物
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(monitorPointType);
        List<Map<String, Object>> keyPollutants = pollutants.stream().filter(m -> m.get("IsShowFlow") != null && Integer.parseInt(m.get("IsShowFlow").toString()) == 1).collect(Collectors.toList());
        List<String> pollutantCodes = keyPollutants.stream().map(map -> map.get("Code").toString()).collect(Collectors.toList());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitortype", monitorPointType);
        paramMap.put("pollutantcodes", pollutantCodes);
        paramMap.put("inputoroutput", null);//不包括污水处理厂出水口及进水口
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        List<Map<String, Object>> outputs = getOutPutsAndPollutants(paramMap);
        Set<String> mns = outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getStartTime(starttime))).lte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getEndTime(endtime)));
        Fields fields = fields("DataGatherCode", "MonitorTime", "FlowUnit", getFlowCollectionListstr(timetype), "_id");
        Aggregation aggregation = newAggregation(match(criteria), project(fields));
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, getFlowCollection(timetype), Document.class).getMappedResults();

        paramMap.put("inputoroutput", 1);//出水口
        List<Map<String, Object>> outinfo = getOutPutsAndPollutants(paramMap);
        Set<String> outmns = outinfo.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        Criteria outcriteria = Criteria.where("DataGatherCode").in(outmns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getStartTime(starttime))).lte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getEndTime(endtime)));
        Fields outfields = fields("DataGatherCode", "MonitorTime", "FlowUnit", getFlowCollectionListstr(timetype), "_id");
        Aggregation outaggregation = newAggregation(match(outcriteria), project(outfields));
        List<Document> outmappedResults = mongoTemplate.aggregate(outaggregation, getFlowCollection(timetype), Document.class).getMappedResults();


        paramMap.put("inputoroutput", 2);//进水口
        List<Map<String, Object>> ininfo = getOutPutsAndPollutants(paramMap);
        Set<String> inmns = ininfo.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        Criteria incriteria = Criteria.where("DataGatherCode").in(inmns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getStartTime(starttime))).lte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getEndTime(endtime)));
        Fields infields = fields("DataGatherCode", "MonitorTime", "FlowUnit", getFlowCollectionListstr(timetype), "_id");
        Aggregation inaggregation = newAggregation(match(incriteria), project(infields));
        List<Document> inmappedResults = mongoTemplate.aggregate(inaggregation, getFlowCollection(timetype), Document.class).getMappedResults();


        for (String timepoint : allTimePoint) {
            Map<String, Object> data = new HashMap<>();

            for (Map<String, Object> keyPollutant : keyPollutants) {
                String Code = keyPollutant.get("Code") == null ? "" : keyPollutant.get("Code").toString();
                //废水数据
                assembleWaterFlowData(pollutantcode, timetype, datatype, format, mappedResults, timepoint, data, Code, "water");
                //出水口数据
                assembleWaterFlowData(pollutantcode, timetype, datatype, format, outmappedResults, timepoint, data, Code, "out");
                //进水口数据
                assembleWaterFlowData(pollutantcode, timetype, datatype, format, inmappedResults, timepoint, data, Code, "in");


            }
            data.put("monitortime", timepoint);
            resultList.add(data);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getFlowInfoByParamsGroupbyTime(Map<String, Object> paramsMap) {
        String pollutantcode = paramsMap.get("pollutantcode") == null ? "" : paramsMap.get("pollutantcode").toString();
        String starttime = paramsMap.get("starttime") == null ? "" : paramsMap.get("starttime").toString();
        String endtime = paramsMap.get("endtime") == null ? "" : paramsMap.get("endtime").toString();
        String pollutionid = paramsMap.get("pollutionid") == null ? "" : paramsMap.get("pollutionid").toString();
        Integer monitorPointType = paramsMap.get("monitorpointtype") == null ? -1 : Integer.valueOf(paramsMap.get("monitorpointtype").toString());
        Integer timetype = paramsMap.get("timetype") == null ? -1 : Integer.valueOf(paramsMap.get("timetype").toString());
        Integer datatype = paramsMap.get("datatype") == null ? -1 : Integer.valueOf(paramsMap.get("datatype").toString());

        if (timetype == null) {
            return new ArrayList<>();
        }
        List<String> allTimePoint = JSONObjectUtil.getAllTimePoint(starttime, endtime);
        String format = JSONObjectUtil.getFormat(starttime.length());
        List<Map<String, Object>> resultList = new ArrayList<>();
        //重点监测排放量污染物
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(monitorPointType);
        List<Map<String, Object>> keyPollutants = pollutants.stream().filter(m -> m.get("IsShowFlow") != null && Integer.parseInt(m.get("IsShowFlow").toString()) == 1).collect(Collectors.toList());
        List<String> pollutantCodes = keyPollutants.stream().map(map -> map.get("Code").toString()).collect(Collectors.toList());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitortype", monitorPointType);
        paramMap.put("pollutantcodes", pollutantCodes);
        paramMap.put("pollutionid", pollutionid);
//        paramMap.put("inputoroutput", null);//不包括污水处理厂出水口及进水口
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        List<Map<String, Object>> outputs = getOutPutsAndPollutants(paramMap);

        Map<String, Object> mn_outputname = outputs.stream().filter(m -> m.get("OutPutName") != null && m.get("MN") != null).collect(Collectors.toMap(m -> m.get("MN").toString(), m -> m.get("OutPutName"), (a, b) -> a));

        Set<String> mns = outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getStartTime(starttime))).lte(DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getEndTime(endtime)));
        Fields fields = fields("DataGatherCode", "MonitorTime", "FlowUnit", getFlowCollectionListstr(timetype), "_id");
        Aggregation aggregation = newAggregation(match(criteria), project(fields));
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, getFlowCollection(timetype), Document.class).getMappedResults();
        Map<String, List<Document>> collect = mappedResults.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));


        for (String dgimn : collect.keySet()) {
            Map<String, Object> datamap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();

            datamap.put("dgimn", dgimn);
            datamap.put("outputname", mn_outputname.get(dgimn));
            List<Document> documentList = collect.get(dgimn);
            for (String timepoint : allTimePoint) {
                Map<String, Object> data = new HashMap<>();
                for (Map<String, Object> keyPollutant : keyPollutants) {
                    String Code = keyPollutant.get("Code") == null ? "" : keyPollutant.get("Code").toString();
                    //废水数据
                    assembleWaterFlowData(pollutantcode, timetype, datatype, format, documentList, timepoint, data, Code, "water");

                }
                Map<String, Object> stringObjectMap = (Map<String, Object>) data.get("water");
                if (stringObjectMap != null) {
                    stringObjectMap.put("monitortime", timepoint);
                    datalist.add(stringObjectMap);
                }
            }
            datamap.put("datalist", datalist);
            resultList.add(datamap);
        }
        return resultList;
    }


    private void assembleWaterFlowData(String pollutantcode, Integer timetype, Integer datatype, String format, List<Document> outmappedResults, String timepoint, Map<String, Object> data, String code, String flag) {
        for (Document mappedResult : outmappedResults) {
            String MonitorTime = mappedResult.get("MonitorTime") == null ? "" : mappedResult.get("MonitorTime").toString();
            String FlowUnit = mappedResult.get("FlowUnit") == null ? "" : mappedResult.get("FlowUnit").toString();
            String monitortimestr = FormatUtils.formatCSTString(MonitorTime, format);
            if (timepoint.equals(monitortimestr)) {
                List<Map<String, Object>> datalist = mappedResult.get(getFlowCollectionListstr(timetype)) == null ? new ArrayList<>() : (List<Map<String, Object>>) mappedResult.get(getFlowCollectionListstr(timetype));
                Map<String, Object> stringObjectMap = datalist.stream().filter(m -> m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString())).findFirst().orElse(new HashMap<>());
                if (!stringObjectMap.isEmpty() && (StringUtils.isBlank(pollutantcode) || (StringUtils.isNotBlank(pollutantcode) && code.equals(pollutantcode)))) {
                    String monitorvalue = stringObjectMap.get(getMonitorValuestr(timetype)) == null ? "" : stringObjectMap.get(getMonitorValuestr(timetype)).toString();
                    String PollutantCode = stringObjectMap.get("PollutantCode") == null ? "" : stringObjectMap.get("PollutantCode").toString();
                    if (datatype == 1) {
                        setWaterData(data, monitorvalue, FlowUnit, PollutantCode, flag);
                    } else if (datatype == 2) {
                        setListData(data, monitorvalue, FlowUnit, PollutantCode, flag);
                    }
                }
            }
        }
    }


    private String getFlowCollection(Integer timetype) {
        if (timetype == 1) {
            return "HourFlowData";
        } else if (timetype == 2) {
            return "DayFlowData";
        } else if (timetype == 3) {
            return "MonthFlowData";
        }
        return "";
    }

    private String getFlowCollectionListstr(Integer timetype) {
        if (timetype == 1) {
            return "HourFlowDataList";
        } else if (timetype == 2) {
            return "DayFlowDataList";
        } else if (timetype == 3) {
            return "MonthFlowDataList";
        }
        return "";
    }

    private String getMonitorValuestr(Integer timetype) {
        if (timetype == 1) {
            return "AvgFlow";
        } else if (timetype == 2) {
            return "AvgFlow";
        } else if (timetype == 3) {
            return "PollutantFlow";
        }
        return "";
    }


    /**
     * @author: chengzq
     * @date: 2021/1/20 0020 下午 5:05
     * @Description: 设置图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [data, monitorvalue, FlowUnit, Code, flag]
     * @throws:
     */
    private void setWaterData(Map<String, Object> data, String monitorvalue, String FlowUnit, String Code, String flag) {
        DecimalFormat format = new DecimalFormat("0.###");
        Map<String, Object> pollutantdata = data.get(flag) == null ? new HashMap<>() : (Map<String, Object>) data.get(flag);
        String pollutantcode = pollutantdata.get("pollutantcode") == null ? "" : pollutantdata.get("pollutantcode").toString();
        String value = pollutantdata.get("monitorvalue") == null ? "" : pollutantdata.get("monitorvalue").toString();
        if (pollutantcode.equals(Code)) {
            pollutantdata.put("monitorvalue", format.format(Double.valueOf(value) + Double.valueOf(monitorvalue)));
        } else {
            pollutantdata.put("monitorvalue", format.format(Double.valueOf(monitorvalue)));
        }
        pollutantdata.put("FlowUnit", FlowUnit);
        pollutantdata.put("flag", flag);
        pollutantdata.put("pollutantcode", Code);
        data.put(flag, pollutantdata);
    }

    /**
     * @author: xsm
     * @date: 2019/10/25 0025 下午 12:07
     * @Description: 自定义条件查询在线超阈值、超限、异常数据（通过mn号和日时间分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public Map<String, Object> getEarlyOverOrExceptionListDataByParams(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            List<String> exceptionlist = (List<String>) paramMap.get("exceptionlist");
            if (paramMap.get("monitortypes") != null) {
                param.put("pollutanttypes", paramMap.get("monitortypes"));
                List<Map<String, Object>> mapList = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
                if (mapList != null && mapList.size() > 0) {
                    param.clear();
                    for (Map<String, Object> map : mapList) {
                        if (map.get("code") != null) {
                            param.put(map.get("code").toString(), map.get("name"));
                        }
                    }
                }
            }
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            List<Integer> monitortypes = (List<Integer>) paramMap.get("monitortypes");
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String datatype = paramMap.get("datatype").toString();
            String starttime = paramMap.get("starttime").toString() + " 00:00:00";
            String endtime = paramMap.get("endtime").toString() + " 23:59:59";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            Integer remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            String mnField = "MN";
            if (remindtype == EarlyAlarmEnum.getCode() || remindtype == OverAlarmEnum.getCode()) {  //阈值
                timefield = "RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode() ? "FirstOverTime" : "OverTime";
                collection = "RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode()  ? overModelCollection : overData_db;
                lasttimestr = "RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode()  ? "LastOverTime" : "OverTime";
                mnField = "RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode()  ? "MN" : "DataGatherCode";
            } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                timefield = "FirstExceptionTime";
                collection = exceptionModelCollection;
                lasttimestr = "LastExceptionTime";
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {
                if (exceptionlist != null && exceptionlist.size() > 0) {
                    criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").in(exceptionlist);//.and("DataType").ne("HourData");
                } else {
                    if (monitortypes.contains(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) && remindtype == ExceptionAlarmEnum.getCode()) {//若为废水异常  则无流量异常不参与查询
                        criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode()).and("DataType").ne("HourData");
                    } else {
                        criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
                    }
                }
            } else if (remindtype == OverAlarmEnum.getCode()) {
                criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
                if ("RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode() ) {
                    criteria.and("DataType").ne("HourData");
                } else {
                    criteria.and("DataType").is("HourData");
                }
            } else if (remindtype == EarlyAlarmEnum.getCode()) {
                criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0);
                if ("RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode() ) {
                    criteria.and("DataType").ne("HourData");
                } else {
                    criteria.and("DataType").is("HourData");
                }
            }
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project(mnField, timefield)
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(
                    Aggregation.group(mnField, "MonitorTime")
            );
            long totalCount = 0;
            //获取分组总数
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Map.class);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                totalCount = resultsCount.getMappedResults().size();
                pageEntity.setTotalCount(totalCount);
                int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
                pageEntity.setPageCount(pageCount);
            }
            operations.clear();
            criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
                if (exceptionlist != null && exceptionlist.size() > 0) {
                    criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").in(exceptionlist).and("DataType").ne("HourData");
                } else {
                    if (monitortypes.contains(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) && remindtype == ExceptionAlarmEnum.getCode()) {//若为废水异常  则无流量异常不参与查询
                        criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode()).and("DataType").ne("HourData");
                    } else {
                        criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
                    }
                }
            } else if (remindtype == OverAlarmEnum.getCode()) {
                criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
                if ("RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode() ) {
                    criteria.and("DataType").ne("HourData");
                } else {
                    criteria.and("DataType").is("HourData");
                }
            } else if (remindtype == EarlyAlarmEnum.getCode()) {
                criteria.and(mnField).in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0);

                if ("RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode() ) {
                    criteria.and("DataType").ne("HourData");
                } else {
                    criteria.and("DataType").is("HourData");
                }
            }
            operations.add(Aggregation.match(criteria));
            if (remindtype == ExceptionAlarmEnum.getCode()) {    //若查异常数据 则需返回异常类型（零值异常、连续值异常、超限异常、无流量异常等）
                operations.add(Aggregation.project(mnField, "LastExceptionTime", timefield, "PollutantCode", "ExceptionType")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("pollutantcode", "$PollutantCode");
                childmap.put("starttime", "$" + timefield);
                childmap.put("endtime", "$LastExceptionTime");
                childmap.put("exceptiontype", "$ExceptionType");
                operations.add(
                        Aggregation.group(mnField, "MonitorTime")
                                .min(timefield).as("firsttime")
                                .max("LastExceptionTime").as("lasttime")
                                .push(childmap).as("pollutanttimes")
                );
            } else { //超标 超阈值
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("pollutantcode", "$PollutantCode");
                childmap.put("starttime", "$" + timefield);
                childmap.put("endtime", "$" + lasttimestr);
                if ("RealTime".equals(datatype) || remindtype == EarlyAlarmEnum.getCode() ) {
                    operations.add(Aggregation.project(mnField, "MinOverMultiple", "MaxOverMultiple", lasttimestr, timefield, "PollutantCode")
                            .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    );
                    childmap.put("minovermultiple", "$MinOverMultiple");
                    childmap.put("maxovermultiple", "$MaxOverMultiple");
                } else {
                    operations.add(Aggregation.project(mnField, "OverMultiple", timefield, "PollutantCode")
                            .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    );
                    childmap.put("minovermultiple", "$OverMultiple");
                    childmap.put("maxovermultiple", "$OverMultiple");
                }
                operations.add(
                        Aggregation.group(mnField, "MonitorTime")
                                .min(timefield).as("firsttime")
                                .max(lasttimestr).as("lasttime")
                                .push(childmap).as("pollutanttimes")
                );
            }
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", mnField));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("total", pageEntity.getTotalCount());
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("datagathercode", document.get(mnField));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                if (document.get("firsttime") != null && document.getDate("lasttime") != null) {
                    String first_time = DataFormatUtil.getDateYMDHMS(document.getDate("firsttime"));
                    String last_time = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                    if (first_time.equals(last_time)) {
                        dataMap.put("firsttime", DataFormatUtil.getDateYMD(document.getDate("firsttime")) + " 00:00:00");
                    } else {
                        dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                    }
                    dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                } else {
                    dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                    dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                }
                List<Map<String, Object>> polist = new ArrayList<>();
                List<Document> pollutanttimes = (List<Document>) document.get("pollutanttimes");
                if (pollutanttimes != null && pollutanttimes.size() > 0) {
                    Map<String, List<Document>> mapDocuments = pollutanttimes.stream().collect(Collectors.groupingBy(m -> m.get("pollutantcode").toString()));
                    for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                        Map<String, Object> onemap = new HashMap<>();
                        List<Document> onelist = entry.getValue();
                        onemap.put("pollutantcode", entry.getKey());
                        onemap.put("pollutantname", param.get(entry.getKey()));
                        Double minvalue = 0d;
                        Double maxvalue = 0d;
                        String continuityvalue = "Day".equals(datatype) ? document.getString("MonitorTime") + " 00:00:00、" : "";
                        Long continuityduration = 0l;
                        Set<String> exceptioontypes = new HashSet();
                        for (Document podo : onelist) {
                            if ("Hour".equals(datatype) && remindtype == EarlyAlarmEnum.getCode() && podo.get("starttime") != null && podo.get("endtime") != null) {
                                if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(podo.getDate("endtime"))) ||
                                        DataFormatUtil.getDateYMDH(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDH(podo.getDate("endtime")))) {
                                    String datetime = DataFormatUtil.getDateH(podo.getDate("starttime")) +":00" + "、";
                                    continuityvalue =  continuityvalue + (continuityvalue.contains(datetime) ? "" : datetime);
                                }  else  {
                                    int startDateH = Integer.parseInt(DataFormatUtil.getDateH(podo.getDate("starttime")));
                                    int endDateH = Integer.parseInt(DataFormatUtil.getDateH(podo.getDate("endtime")));
                                    for (int i = startDateH;i <= endDateH;i++){
                                        String datetime = i +":00、";
                                        continuityvalue =  continuityvalue + (continuityvalue.contains(datetime) ? "" : datetime);
                                    }
                                }
                            }else if (!"Day".equals(datatype) && podo.get("starttime") != null && podo.get("endtime") != null) {
                                if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(podo.getDate("endtime")))) {
                                    continuityvalue = continuityvalue + DataFormatUtil.getDateHM(podo.getDate("starttime")) + "、";
                                    continuityduration+=1;
                                } else {
                                    continuityvalue = continuityvalue + DataFormatUtil.getDateHM(podo.getDate("starttime")) + "-" + DataFormatUtil.getDateHM(podo.getDate("endtime")) + "、";
                                    continuityduration+=((podo.getDate("endtime").getTime() - podo.getDate("starttime").getTime())/1000/60);
                                }
                            }
                            if (podo.get("minovermultiple") != null) {
                                if (minvalue == 0d) {
                                    minvalue = Double.valueOf(podo.get("minovermultiple").toString());
                                } else {
                                    if (Double.valueOf(podo.get("minovermultiple").toString()) < minvalue) {
                                        minvalue = Double.valueOf(podo.get("minovermultiple").toString());
                                    }
                                }
                            }
                            if (podo.get("maxovermultiple") != null) {
                                if (maxvalue == 0d) {
                                    maxvalue = Double.valueOf(podo.get("maxovermultiple").toString());
                                } else {
                                    if (Double.valueOf(podo.get("maxovermultiple").toString()) > maxvalue) {
                                        maxvalue = Double.valueOf(podo.get("maxovermultiple").toString());
                                    }
                                }
                            }

                            //异常类型
                            if (podo.get("exceptiontype") != null) {
                                exceptioontypes.add(podo.getString("exceptiontype"));
                            }

                        }
                        //超标时段
                        if (!"".equals(continuityvalue)) {
                            onemap.put("continuityvalue", continuityvalue.substring(0, continuityvalue.length() - 1));
                            if(continuityduration < 60){
                                onemap.put("continuityduration", continuityduration+"分钟");
                            }else {
                                onemap.put("continuityduration", (continuityduration/60)+"小时"+(continuityduration%60)+"分钟");
                            }
                        } else {
                            onemap.put("continuityvalue", "");
                            onemap.put("continuityduration", "");
                        }
                        //超标倍数
                        //if (minvalue != 0d && maxvalue != 0d) {
                        String min = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(minvalue * 100));
                        String max = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(maxvalue * 100));
                        if (!min.equals(max)) {//最大超标倍数和最小超标倍数不相等
                            onemap.put("overmultiple", min + "%-" + max + "%");
                        } else {//相等
                            onemap.put("overmultiple", min + "%");
                        }
                       /* } else {
                            onemap.put("overmultiple", "");
                        }*/
                        String str = "";
                        if (exceptioontypes != null && exceptioontypes.size() > 0) {
                            for (String type : exceptioontypes) {
                                str += CommonTypeEnum.ExceptionTypeEnum.getNameByCode(type) + "、";
                            }
                        }
                        if (!"".equals(str)) {
                            str = str.substring(0, str.length() - 1);
                            if (onemap.get("pollutantname") != null) {
                                onemap.put("pollutantname", onemap.get("pollutantname") + "【" + str + "】");
                            }
                        }
                        polist.add(onemap);
                    }
                }
                dataMap.put("pollutantlist", polist);
                dataList.add(dataMap);
            }
            resultMap.put("datalist", dataList);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/1/20 0020 下午 5:05
     * @Description: 设置列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [data, monitorvalue, FlowUnit, Code, flag]
     * @throws:
     */
    private void setListData(Map<String, Object> data, String monitorvalue, String FlowUnit, String Code, String flag) {
        DecimalFormat format = new DecimalFormat("0.###");
        List<Map<String, Object>> objects = data.get(Code) == null ? new ArrayList<>() : (List<Map<String, Object>>) data.get(Code);
        Map<String, Object> pollutantdata = objects.stream().filter(m -> m.get("pollutantcode") != null && Code.equals(m.get("pollutantcode").toString()) && m.get("flag") != null && flag.equals(m.get("flag"))).findFirst().orElse(new HashMap<>());
        int i = objects.indexOf(pollutantdata);
        Double collect = objects.stream().filter(m -> m.get("pollutantcode") != null && Code.equals(m.get("pollutantcode").toString()) && m.get("flag") != null && flag.equals(m.get("flag"))
                && m.get("monitorvalue") != null).map(m -> m.get("monitorvalue").toString()).collect(Collectors.summingDouble(m -> Double.valueOf(m)));
        pollutantdata.put("monitorvalue", format.format(collect + Double.valueOf(monitorvalue)));
        pollutantdata.put("FlowUnit", FlowUnit);
        pollutantdata.put("flag", flag);
        pollutantdata.put("pollutantcode", Code);

        if (i >= 0) {
            objects.set(i, pollutantdata);
        } else {
            objects.add(pollutantdata);
        }

        data.put(Code, objects);
    }

    /**
     * @author: xsm
     * @date: 2021/08/02 0002 下午 16:40
     * @Description: 根据监测点类型和自定义参数获取报警详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getHierarchicalEarlyDetailDataByParams(Map<String, Object> paramMap, List<String> datatypes, Integer pagenum, Integer pagesize) {
        Integer monitorpointtype = Integer.valueOf(paramMap.get("monitorpointtype").toString());
        List<Map<String, Object>> allpoints = getAllPollutionOutputDgimnInfoByParam(paramMap);
        List<Map<String, Object>> outputlist = getAllOutputDgimnAndPollutantInfosByParam(paramMap);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        Set<String> mnlist = new HashSet<String>();
        Set<String> codelist = new HashSet<String>();
        Map<String, Object> codemap = new HashMap<>();
        Map<String, Object> codeunit = new HashMap<>();
        String timefield = "";
        String collection = "";
        Integer remindtype = OverAlarmEnum.getCode();
        if (paramMap.get("remindtype") != null) {
            remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            if (remindtype == EarlyAlarmEnum.getCode()) {  //阈值
                timefield = "EarlyWarnTime";
                collection = "EarlyWarnData";
            } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                timefield = "ExceptionTime";
                collection = "ExceptionData";
            } else if (remindtype == OverAlarmEnum.getCode()) {//超标超限
                timefield = "OverTime";
                collection = "OverData";
            }
        }
        for (Map<String, Object> obj : allpoints) {
            if (obj.get("dgimn") != null) {
                mnlist.add(obj.get("dgimn").toString());
            }
        }
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> obj : pollutants) {
                codemap.put(obj.get("code").toString(), obj.get("name"));
                codeunit.put(obj.get("code").toString(), obj.get("PollutantUnit"));
                codelist.add(obj.get("code").toString());
            }
        }
        Date startDate;
        Date endDate;
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            if (paramMap.get("starttime").toString().length() > 17) {//首页弹窗跳转 时间到秒
                startDate = DataFormatUtil.parseDate(paramMap.get("starttime").toString());
                endDate = DataFormatUtil.parseDate(paramMap.get("endtime").toString());
            } else {
                startDate = DataFormatUtil.parseDate(paramMap.get("starttime") + " 00:00:00");
                endDate = DataFormatUtil.parseDate(paramMap.get("endtime") + " 23:59:59");
            }
        } else {
            Date today = new Date();
            startDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(today) + " 00:00:00");
            endDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(today) + " 23:59:59");
        }

        PageEntity<Document> pageEntity = new PageEntity<>();
        if (pagenum != null && pagesize != null) {
            pageEntity.setPageNum(pagenum);
            pageEntity.setPageSize(pagesize);
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where("PollutantCode").in(codelist));

        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where(timefield).gte(startDate).lte(endDate)));
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mnlist)));
        operations.add(Aggregation.match(Criteria.where("PollutantCode").in(codelist)));
        List<String> datatypelist = new ArrayList<>();
        //数据类型
        if (datatypes.size() > 0) {
            for (String str : datatypes) {
                datatypelist.add(MongoDataUtils.getCollectionByDataMark(Integer.parseInt(str)));
            }
            operations.add(Aggregation.match(Criteria.where("DataType").in(datatypelist)));
            query.addCriteria(Criteria.where("DataType").in(datatypelist));

        }
        //异常类型
        if (paramMap.get("exceptiontype") != null) {
            List<String> exceptiontype = (List<String>) paramMap.get("exceptiontype");
            query.addCriteria(Criteria.where("ExceptionType").in(exceptiontype));
            operations.add(Aggregation.match(Criteria.where("ExceptionType").in(exceptiontype)));
        }
        query.addCriteria(Criteria.where(timefield).gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, timefield));
        //总条数
        long totalCount = mongoTemplate.count(query, collection);
        pageEntity.setTotalCount(totalCount);
        String orderBy = "DataGatherCode," + timefield;
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        if (remindtype == ExceptionAlarmEnum.getCode()) {//异常
            operations.add(Aggregation.project("DataGatherCode", "PollutantCode", timefield, "DataType", "ExceptionType", "MonitorValue"));
        } else if (remindtype == EarlyAlarmEnum.getCode()) {
            operations.add(Aggregation.project("DataGatherCode", "PollutantCode", timefield, "AlarmType", "AlarmLevel", "DataType", "MonitorValue"));
        } else if (remindtype == OverAlarmEnum.getCode()) {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质类型返回水质级别
                operations.add(Aggregation.project("DataGatherCode", "PollutantCode", timefield, "AlarmType", "AlarmLevel", "DataType", "MonitorValue", "IsOverStandard", "OverMultiple", "WaterLevel"));
            } else {
                operations.add(Aggregation.project("DataGatherCode", "PollutantCode", timefield, "AlarmType", "AlarmLevel", "DataType", "MonitorValue", "IsOverStandard", "OverMultiple", "WaterLevel"));
            }
        }
        //插入分页、排序条件
        if (pagenum != null && pagesize != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, collection, Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
        Map<String, Object> codeAndLevel = new HashMap<>();
        for (Map<String, Object> map : alarmLevelList) {
            codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
        }
        if (documents.size() > 0) {//判断查询数据是否为空
            Map<String, Object> waterqualitymap = new HashMap<>();
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质类型返回水质级别
                List<Map<String, Object>> qulitylevels = waterStationMapper.getAllWaterQualityLevel();
                if (qulitylevels != null && qulitylevels.size() > 0) {
                    for (Map<String, Object> qulity : qulitylevels) {
                        waterqualitymap.put(qulity.get("code").toString(), qulity.get("name"));
                    }
                }
            }
            for (Document document : documents) {
                String mn = document.getString("DataGatherCode");//MN号
                String pollutantcode = document.getString("PollutantCode");//污染物编码
                Object standardmaxvalue = null;
                Object standardminvalue = null;
                Object alarmtype = null;
                String exceptionvalue = "";
                for (Map<String, Object> objmap : outputlist) {
                    if (objmap.get("DGIMN") != null && mn.equals(objmap.get("DGIMN"))) {//当MN号相同时
                        if (remindtype == EarlyAlarmEnum.getCode()) {
                            if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                                standardmaxvalue = objmap.get("StandardMaxValue");
                                break;
                            }
                        }
                        if (remindtype == OverAlarmEnum.getCode()) {
                            if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                                alarmtype = objmap.get("AlarmType");
                                standardminvalue = objmap.get("StandardMinValue");
                                standardmaxvalue = objmap.get("StandardMaxValue");
                                break;
                            }
                        }
                        if (remindtype == OverAlarmEnum.getCode()) {
                            if (objmap.get("Code") != null && pollutantcode.equals(objmap.get("Code").toString())) {
                                standardmaxvalue = objmap.get("StandardMaxValue");
                                if (objmap.get("ExceptionMinValue") != null && !"".equals(objmap.get("ExceptionMinValue").toString())) {
                                    exceptionvalue = exceptionvalue + "<" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMinValue").toString());
                                }
                                if (objmap.get("ExceptionMaxValue") != null && !"".equals(objmap.get("ExceptionMaxValue").toString())) {
                                    if (!"".equals(exceptionvalue)) {
                                        exceptionvalue = exceptionvalue + "或>" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                                    } else {
                                        exceptionvalue = exceptionvalue + ">" + DataFormatUtil.subZeroAndDot(objmap.get("ExceptionMaxValue").toString());
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                for (Map<String, Object> objmap : allpoints) {
                    if (objmap.get("dgimn") != null && mn.equals(objmap.get("dgimn"))) {//当MN号相同时
                        Map<String, Object> result = getPointInfoByPointtype(objmap, monitorpointtype);

                        if (remindtype == EarlyAlarmEnum.getCode()) {
                            result.put("standardmaxvalue", standardmaxvalue);
                        }
                        if (remindtype == OverAlarmEnum.getCode()) {
                            result.put("multiple", document.getDouble("OverMultiple") != null ? DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(document.getDouble("OverMultiple") * 100)) + "%" : "");
                            result.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(document.getString("AlarmType")));
                            result.put("alarmlevelcode", document.get("AlarmLevel"));
                            result.put("standardmaxvalue", "");
                            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质类型返回水质级别
                                result.put("waterquality", "");
                                if (waterqualitymap != null && document.get("WaterLevel") != null) {
                                    result.put("waterquality", waterqualitymap.get(document.getString("WaterLevel")));
                                }
                            }
                            if (alarmtype != null) {
                                if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype.toString())) {//上限报警
                                    if (standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                        result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype.toString())) {//下限报警
                                    if (standardminvalue != null && !"".equals(standardminvalue.toString())) {
                                        result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()));
                                    }
                                } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(alarmtype.toString())) {//区间报警
                                    if (standardminvalue != null && !"".equals(standardminvalue.toString()) && standardmaxvalue != null && !"".equals(standardmaxvalue.toString())) {
                                        result.put("standardmaxvalue", DataFormatUtil.subZeroAndDot(standardminvalue.toString()) + "-" + DataFormatUtil.subZeroAndDot(standardmaxvalue.toString()));
                                    }
                                }
                            }
                            if (document.getBoolean("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {//判断是否超标报警
                                //判断是否即超标又超限
                                if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                    result.put("alarmlevel", (codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) + "、" : "") + "超标报警");
                                    result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                                } else {
                                    result.put("alarmlevel", "超标报警");
                                    result.put("alarmlevelvalue", "");
                                }

                            } else {
                                if (document.get("AlarmLevel") != null && Integer.parseInt(document.get("AlarmLevel").toString()) > 0) {
                                    result.put("alarmlevel", codeAndLevel.get(document.get("AlarmLevel").toString()) != null ? codeAndLevel.get(document.get("AlarmLevel").toString()) : "");
                                    result.put("alarmlevelvalue", getAlarmLevelValue(mn, pollutantcode, document.get("AlarmLevel"), outputlist));
                                }
                            }
                        }

                        //异常
                        if (remindtype == ExceptionAlarmEnum.getCode()) {//异常
                            result.put("standardmaxvalue", standardmaxvalue);
                            result.put("exceptiontype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType")));
                            if ((document.getString("ExceptionType")).equals(CommonTypeEnum.ExceptionTypeEnum.OverExceptionEnum.getCode())) {
                                result.put("exceptionvalue", exceptionvalue);
                            } else {
                                result.put("exceptionvalue", "");
                            }
                        }
                        result.put("remindtype", remindtype);
                        result.put("pollutantname", codemap.get(pollutantcode));
                        result.put("pollutantcode", pollutantcode);
                        result.put("pollutantunit", codeunit.get(pollutantcode) != null ? codeunit.get(pollutantcode) : "");
                        result.put("monitorpointtype", monitorpointtype);
                        result.put("dgimn", objmap.get("dgimn"));
                        result.put("monitorvalue", document.getString("MonitorValue"));
                        if ("RealTimeData".equals(document.getString("DataType"))) {//实时
                            result.put("datatypecode", "1");
                            result.put("datatype", RealTimeDataEnum.getName());
                            result.put("datatypename", "实时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate(timefield)));
                        } else if ("MinuteData".equals(document.getString("DataType"))) {//分钟
                            result.put("datatypecode", "2");
                            result.put("datatype", MinuteDataEnum.getName());
                            result.put("datatypename", "分钟数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate(timefield)));
                        } else if ("HourData".equals(document.getString("DataType"))) {//小时
                            result.put("datatypecode", "3");
                            result.put("datatype", HourDataEnum.getName());
                            result.put("datatypename", "小时数据");
                            result.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate(timefield)));
                        } else if ("DayData".equals(document.getString("DataType"))) {//日
                            result.put("datatypecode", "4");
                            result.put("datatype", DayDataEnum.getName());
                            result.put("datatypename", "日数据");
                            result.put("monitortime", DataFormatUtil.getDateYMD(document.getDate(timefield)));
                        }
                        resultlist.add(result);
                        break;
                    }
                }
            }
        }
        Map<String, Object> resultmap = new HashMap<>();
        resultmap.put("total", totalCount);
        resultmap.put("datalist", resultlist);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2019/10/28 0028 下午 3:09
     * @Description: 获取预警、报警、异常列表表头信息(导出)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public List<Map<String, Object>> getTableTitleForEarlyOverOrException(Integer remindtype, Integer monitorpointtype, List<Integer> monitortypes) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        if (monitortypes.contains(monitorpointtype)) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "企业名称");
            map.put("align", "center");
            tableTitleData.add(map);
            if (monitorpointtype == WasteWaterEnum.getCode() || monitorpointtype == WasteGasEnum.getCode() || monitorpointtype == RainEnum.getCode() || monitorpointtype == SmokeEnum.getCode()) {
                Map<String, Object> map2 = new HashMap<>();
                map2.put("minwidth", "180px");
                map2.put("headeralign", "center");
                map2.put("fixed", "left");
                map2.put("showhide", true);
                map2.put("prop", "monitorpointname");
                map2.put("label", "排口名称");
                map2.put("align", "center");
                tableTitleData.add(map2);
            } else {
                Map<String, Object> map2 = new HashMap<>();
                map2.put("minwidth", "180px");
                map2.put("headeralign", "center");
                map2.put("fixed", "left");
                map2.put("showhide", true);
                map2.put("prop", "monitorpointname");
                map2.put("label", "监测点名称");
                map2.put("align", "center");
                tableTitleData.add(map2);
            }
        } else if (monitorpointtype == AirEnum.getCode() ||
                monitorpointtype == EnvironmentalVocEnum.getCode() ||
                monitorpointtype == EnvironmentalStinkEnum.getCode() ||
                monitorpointtype == EnvironmentalDustEnum.getCode() ||
                monitorpointtype == WaterQualityEnum.getCode() ||
                monitorpointtype == MicroStationEnum.getCode()) {//大气
            Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "monitorpointname");
            map2.put("label", "监测点名称");
            map2.put("align", "center");
            tableTitleData.add(map2);
        } else if (monitorpointtype == StorageTankAreaEnum.getCode()) {//贮罐
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "区域名称");
            map.put("align", "center");
            tableTitleData.add(map);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "monitorpointname");
            map2.put("label", "储罐编号");
            map2.put("align", "center");
            tableTitleData.add(map2);
        } else if (monitorpointtype == SecurityLeakageMonitor.getCode()
                || monitorpointtype == SecurityCombustibleMonitor.getCode()
                || monitorpointtype == SecurityToxicMonitor.getCode()) {//安全泄露、可燃易爆气体、有毒有害气体
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "风险区域名称");
            map.put("align", "center");
            tableTitleData.add(map);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "monitorpointname");
            map2.put("label", "监测点名称");
            map2.put("align", "center");
            tableTitleData.add(map2);
        }
        Map<String, Object> map3 = new HashMap<>();
        map3.put("minwidth", "180px");
        map3.put("headeralign", "center");
        map3.put("fixed", "left");
        map3.put("showhide", true);
        map3.put("prop", "monitortime");
        map3.put("label", "日期");
        map3.put("align", "center");
        tableTitleData.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("minwidth", "180px");
        map4.put("headeralign", "center");
        map4.put("fixed", "left");
        map4.put("showhide", true);
        map4.put("prop", "pollutantname");
        map4.put("label", "污染物");
        map4.put("align", "center");
        tableTitleData.add(map4);

        Map<String, Object> map5 = new HashMap<>();
        map5.put("minwidth", "180px");
        map5.put("headeralign", "center");
        map5.put("fixed", "left");
        map5.put("showhide", true);
        map5.put("prop", "continuityvalue");
        if (remindtype == EarlyAlarmEnum.getCode()) {    //超阈值
            map5.put("label", "超阈值时间");
        } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //超限
            map5.put("label", "异常时间");
        } else if (remindtype == OverAlarmEnum.getCode()) {    //超限
            map5.put("label", "超标");
        }
        map5.put("align", "center");
        tableTitleData.add(map5);

        if (remindtype == OverAlarmEnum.getCode()) {    //超限
            Map<String, Object> map6 = new HashMap<>();
            map6.put("minwidth", "180px");
            map6.put("headeralign", "center");
            map6.put("fixed", "left");
            map6.put("showhide", true);
            map6.put("prop", "overmultiple");
            map6.put("label", "超标倍数");
            map6.put("align", "center");
            tableTitleData.add(map6);
        }

        Map<String, Object> map7 = new HashMap<>();
        map7.put("minwidth", "180px");
        map7.put("headeralign", "center");
        map7.put("fixed", "left");
        map7.put("showhide", true);
        map7.put("prop", "continuityduration");
        map7.put("label", "时长统计");
        map7.put("align", "center");
        tableTitleData.add(map7);
        return tableTitleData;
    }

    private String countHourMinuteTime(int tatalnum) {
        String str = "";
        if (tatalnum < 60) {
            str = tatalnum + "分钟";
        } else if (tatalnum == 60) {
            str = "1小时";
        } else {
            int onenum = tatalnum / 60;
            str = onenum + "小时" + ((tatalnum - onenum * 60) > 0 ? (tatalnum - onenum * 60) + "分钟" : "");
        }
        return str;
    }

    @Override
    public Map<String, Object> countGasOnlineDataGroupMmAndMonitortime(JSONObject paramMap) throws ParseException {
        Criteria criteria = new Criteria();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        Integer remindtype = null;
        Integer total = 0;
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("remindtype") != null) {
            remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString() + " 00:00:00";
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString() + " 23:59:59";
        }

        if (remindtype == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            List<Map> data = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime"), count().as("count")), hourFlowCollection, Map.class).getMappedResults();
            if (data.size() > 0) {
                String count = data.get(0).get("count").toString();
                total = Integer.valueOf(count);
            }
        } else if (remindtype == ConcentrationChangeEnum.getCode()) { //浓度
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(starttime)).lte(format.parse(endtime));
            List<Map> data = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime"), count().as("count")), hourCollection, Map.class).getMappedResults();
            if (data.size() > 0) {
                String count = data.get(0).get("count").toString();
                total = Integer.valueOf(count);
            }
        }

        Map<String, Object> excelTitleInfo = new HashMap<>();
        LinkedList<String> headers = new LinkedList<>();
        headers.add("日期");
        headers.add("突增时段");
        headers.add("突增幅度");
        LinkedList<String> headersField = new LinkedList<>();
        headersField.add("monitortime");
        headersField.add("allpoint");
        headersField.add("flowrate");
        String excelname = "";
        String partname = "";

        switch (getObjectByCode(remindtype)) {
            case ConcentrationChangeEnum: //浓度
                partname = ConcentrationChangeEnum.getName() + "预警";
                break;
            case FlowChangeEnum: //排放量
                partname = FlowChangeEnum.getName() + "预警";
                break;
        }
        excelname = "废气" + partname;
        headers.addFirst("排口名称");
        headers.addFirst("企业名称");
        headersField.addFirst("monitorname");
        headersField.addFirst("pollutionname");

        excelTitleInfo.put("headers", headers);
        excelTitleInfo.put("headersfield", headersField);
        excelTitleInfo.put("excelname", excelname);
        excelTitleInfo.put("total", total);
        return excelTitleInfo;
    }

    @Override
    public Map<String, Object> getPWOutPutLastDataList(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> inMns = (List<String>) paramMap.get("inMns");
        List<String> outMns = (List<String>) paramMap.get("outMns");
        List<String> mns = new ArrayList<>();
        mns.addAll(inMns);
        mns.addAll(outMns);
        List<Document> documents = getRealTimeDataByMNsAndDataType(mns);
        if (documents.size() > 0) {
            String mnCode;
            //获取点位及污染物标准值数据
            Map<String, Map<String, Object>> mnCodeAndStandardMap = new HashMap<>();
            paramMap.put("mnlist", mns);
            paramMap.put("monitorpointtype", WasteWaterEnum.getCode());

            List<Map<String, Object>> pollutantStandardDataList = pollutantFactorMapper.getPollutantStandarddataByParam(paramMap);
            if (pollutantStandardDataList.size() > 0) {
                for (Map<String, Object> pollutantStandard : pollutantStandardDataList) {
                    mnCode = pollutantStandard.get("DGIMN") + "," + pollutantStandard.get("Code");
                    Map<String, Object> standardMap = new HashMap<>();
                    standardMap.put("StandardMinValue", pollutantStandard.get("StandardMinValue"));
                    standardMap.put("StandardMaxValue", pollutantStandard.get("StandardMaxValue"));
                    standardMap.put("AlarmType", pollutantStandard.get("AlarmType"));
                    standardMap.put("StandardName", pollutantStandard.get("StandardName"));
                    mnCodeAndStandardMap.put(mnCode, standardMap);
                }
            }
            paramMap.put("pollutanttype", WasteWaterEnum.getCode());
            List<Map<String, Object>> pollutantData = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> codeAndUnit = new HashMap<>();
            Map<String, Object> codeAndOrder = new HashMap<>();
            for (Map<String, Object> map : pollutantData) {
                codeAndName.put(map.get("code").toString(), map.get("name"));
                codeAndUnit.put(map.get("code").toString(), map.get("unit"));
                codeAndOrder.put(map.get("code").toString(), map.get("orderindex"));
            }

            Map<String, List<Document>> mnAndPollutantList = new HashMap<>();
            Map<String, String> mnAndDate = new HashMap<>();
            String mnCommon;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                mnAndDate.put(mnCommon, DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                mnAndPollutantList.put(mnCommon, document.get("DataList", List.class));
            }
            List<Document> pollutantList;
            String pollutantcode;
            List<Map<String, Object>> inputdatalist = new ArrayList<>();
            for (String mnIndex : inMns) {
                if (mnAndPollutantList.containsKey(mnIndex)) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("monitortime", mnAndDate.get(mnIndex));
                    dataMap.put("dgimn", mnIndex);
                    dataMap.put("pointtype", "in");
                    pollutantList = mnAndPollutantList.get(mnIndex);
                    List<Map<String, Object>> pollutants = new ArrayList<>();
                    for (Document document : pollutantList) {
                        pollutantcode = document.getString("PollutantCode");
                        if (codeAndName.get(pollutantcode) != null) {
                            Map<String, Object> pollutant = new HashMap<>();
                            pollutant.put("pollutantcode", pollutantcode);
                            pollutant.put("pollutantname", codeAndName.get(pollutantcode));
                            pollutant.put("orderindex", codeAndOrder.get(pollutantcode) != null ? codeAndOrder.get(pollutantcode) : -999);
                            pollutant.put("AvgStrength", document.get("AvgStrength"));
                            pollutant.put("pollutantunit", codeAndUnit.get(pollutantcode));
                            pollutant.put("IsOver", document.get("IsOver"));
                            pollutant.put("IsException", document.get("IsException"));
                            pollutant.put("IsOverStandard", document.get("IsOverStandard"));
                            pollutant.put("IsSuddenChange", document.get("IsSuddenChange"));
                            pollutant.put("standardMap", mnCodeAndStandardMap.get(mnIndex + "," + pollutantcode));
                            pollutants.add(pollutant);
                        }
                    }
                    pollutants = pollutants.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                    dataMap.put("pollutantlist", pollutants);
                    inputdatalist.add(dataMap);
                }

            }


            List<Map<String, Object>> outputdatalist = new ArrayList<>();
            for (String mnIndex : outMns) {
                if (mnAndPollutantList.containsKey(mnIndex)) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("dgimn", mnIndex);
                    dataMap.put("monitortime", mnAndDate.get(mnIndex));
                    dataMap.put("pointtype", "out");
                    pollutantList = mnAndPollutantList.get(mnIndex);
                    List<Map<String, Object>> pollutants = new ArrayList<>();
                    for (Document document : pollutantList) {
                        pollutantcode = document.getString("PollutantCode");
                        if (codeAndName.get(pollutantcode) != null) {
                            Map<String, Object> pollutant = new HashMap<>();
                            pollutant.put("pollutantcode", pollutantcode);
                            pollutant.put("pollutantname", codeAndName.get(pollutantcode));
                            pollutant.put("orderindex", codeAndOrder.get(pollutantcode) != null ? codeAndOrder.get(pollutantcode) : -999);
                            pollutant.put("AvgStrength", document.get("AvgStrength"));
                            pollutant.put("pollutantunit", codeAndUnit.get(pollutantcode));
                            pollutant.put("IsOver", document.get("IsOver"));
                            pollutant.put("IsException", document.get("IsException"));
                            pollutant.put("IsOverStandard", document.get("IsOverStandard"));
                            pollutant.put("IsSuddenChange", document.get("IsSuddenChange"));
                            pollutant.put("standardMap", mnCodeAndStandardMap.get(mnIndex + "," + pollutantcode));
                            pollutants.add(pollutant);
                        }

                    }
                    //排序：
                    pollutants = pollutants.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                    dataMap.put("pollutantlist", pollutants);
                    outputdatalist.add(dataMap);
                }

            }


            resultMap.put("inputdatalist", inputdatalist);
            resultMap.put("outputdatalist", outputdatalist);

        }

        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2022/04/18 15:05
     * @Description: 获取其它监测点表点位信息和监测污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOtherPointInfoAndPollutantsByParam(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOtherPointInfoAndPollutantsByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 11:38
     * @Description: 获取其它类型超标数据
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getOtherPollutantEarlyAndOverAlarmsByParamMap(Integer pageNum, Integer pageSize, List<Map<String, Object>> outPuts, Map<String, Object> paramMap) {
        Map<String, Object> resultTableDataMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        //点位类型
        Integer pointType = (Integer) paramMap.get("pointtype");
        //表头数据
        List<Map<String, Object>> tabletitledata = getOtherTableTitleDataForOverAlarm(pointType);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(pointType);
        Set<Object> pollutantCodes = new HashSet<>();
        //污染物多级表头
        List<Map<String, Object>> pollutantTitle = new ArrayList<>();
        for (Map<String, Object> pollutant : pollutants) {
            //污染物Code
            String propValue = pollutant.get("Code").toString();
            pollutantCodes.add(propValue);
            String labelValue = pollutant.get("Name").toString() + "（次）";
            Map<String, Object> fatherTitle = getSingleTitle(propValue, labelValue, "");
            //子表头
            List<Map<String, Object>> childrenList = new ArrayList<>();
            childrenList.add(getSingleTitle(propValue + "_earlywarnnum", "预警", ""));
            childrenList.add(getSingleTitle(propValue + "_exceptionnum", "异常", ""));
            childrenList.add(getSingleTitle(propValue + "_overnum", "超标", ""));
            fatherTitle.put("children", childrenList);
            pollutantTitle.add(fatherTitle);
        }
        //将污染物表头数据放入表头数据中
        tabletitledata.addAll(pollutantTitle);

        //列表数据
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        if (outPuts.size() > 0) {
            //数据类型
            String dataType = paramMap.get("datatype").toString();
            //mn号
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> outPut : outPuts) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                }
            }

            List<String> countTimes = (List<String>) paramMap.get("counttime");
            Date startDate = DataFormatUtil.getDateYMDHMS(countTimes.get(0) + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(countTimes.get(1) + " 23:59:59");
            GroupOperation group = group("DataGatherCode", "PollutantCode").count().as("num");
            //异常
            Aggregation exceptionAggregation = newAggregation(
                    match(Criteria.where("DataType").is(dataType).andOperator(
                            Criteria.where("DataGatherCode").in(mns),
                            Criteria.where("ExceptionTime").gte(startDate).lte(endDate),
                            Criteria.where("PollutantCode").in(pollutantCodes)
                    )),
                    group
            );
            //超标
            Aggregation overAggregation = newAggregation(
                    match(Criteria.where("DataType").is(dataType).andOperator(
                            Criteria.where("DataGatherCode").in(mns),
                            Criteria.where("OverTime").gte(startDate).lte(endDate),
                            Criteria.where("PollutantCode").in(pollutantCodes)
                    )),
                    group
            );
            //预警
            Aggregation earlyWarnAggregation = newAggregation(
                    match(Criteria.where("DataType").is(dataType).andOperator(
                            Criteria.where("DataGatherCode").in(mns),
                            Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate),
                            Criteria.where("PollutantCode").in(pollutantCodes)
                    )),
                    group
            );
            AggregationResults<Document> exceptionData = mongoTemplate.aggregate(exceptionAggregation, exceptionData_db, Document.class);
            AggregationResults<Document> overData = mongoTemplate.aggregate(overAggregation, overData_db, Document.class);
            AggregationResults<Document> earlyWarnData = mongoTemplate.aggregate(earlyWarnAggregation, earlyWarnData_db, Document.class);
            List<Document> exceptionDocuments = exceptionData.getMappedResults();
            List<Document> overDocuments = overData.getMappedResults();
            List<Document> earlyWarnDocuments = earlyWarnData.getMappedResults();

            for (Map<String, Object> outPut : outPuts) {
                String mn = outPut.get("dgimn").toString();
                Map<String, Object> tableData = new HashMap<>();
                tableData.put("monitorpointname", outPut.get("monitorpointname"));
                tableData.put("outputid", outPut.get("pk_id"));
                tableData.put("mn", outPut.get("dgimn"));
                tableData.put("onlinestatus", outPut.get("onlinestatus"));
                boolean flag = false;
                for (Map<String, Object> pollutant : pollutants) {
                    boolean flag2 = false;
                    String pollutantCode = pollutant.get("Code").toString();
                    Map<String, Object> pollutantDataMap = new HashMap<>();
                    Object earlyWarnnum = getNumForOverOrAlarmOrException(earlyWarnDocuments, mn, pollutantCode);
                    Object exceptionnum = getNumForOverOrAlarmOrException(exceptionDocuments, mn, pollutantCode);
                    Object ovenum = getNumForOverOrAlarmOrException(overDocuments, mn, pollutantCode);
                    if (earlyWarnnum != null) {
                        flag2 = true;
                        pollutantDataMap.put(pollutantCode + "_earlywarnnum", earlyWarnnum);
                    }
                    if (exceptionnum != null) {
                        flag2 = true;
                        pollutantDataMap.put(pollutantCode + "_exceptionnum", exceptionnum);
                    }
                    if (ovenum != null) {
                        flag2 = true;
                        pollutantDataMap.put(pollutantCode + "_overnum", ovenum);
                    }
                    if (flag2) {
                        flag = true;
                        tableData.putAll(pollutantDataMap);
                    }
                }
                if (flag) {
                    tablelistdata.add(tableData);
                }
            }
        }
        //分页
        resultMap.put("total", tablelistdata.size());
        if (pageNum != null && pageSize != null) {
            List<Map<String, Object>> collect = tablelistdata.stream().skip((pageNum - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
            resultMap.put("tablelistdata", collect);
        } else {
            resultMap.put("tablelistdata", tablelistdata);
        }
        resultMap.put("tabletitledata", tabletitledata);
        resultTableDataMap.put("tabledata", resultMap);
        return resultTableDataMap;
    }

    /**
     * @author: mmt
     * @date: 2022/9/19 0007 下午 4:38
     * @Description: 自定义查询条件查询管网监测点位同污水处理厂进口超标相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getWaterPipeNetworkPointRelationTableDataByParams(HashMap<String, Object> paramMap) {
        HashMap<String, Object> result = new HashMap<>();
        try {

            String dgimn = paramMap.get("dgimn").toString();
            String comparedgimn = paramMap.get("comparedgimn").toString();
            String pollutantcode = paramMap.get("pollutantcode").toString();
            Map<String, Object> outputInfoByMn = waterOutputInfoMapper.getOutputInfoByMn(dgimn);
            Map<String, Object> compareOutputInfoByMn = waterOutputInfoMapper.getOutputInfoByMn(comparedgimn);

            paramMap.clear();
            paramMap.put("fkWaterpollutionid", outputInfoByMn.get("pollutionid"));
            paramMap.put("fkWatermonitorpointid", outputInfoByMn.get("outputid"));
            paramMap.put("fkOutfallpollutionid", compareOutputInfoByMn.get("pollutionid"));
            paramMap.put("fkOutfallmonitorpointid", compareOutputInfoByMn.get("outputid"));
            List<WaterCorrelationVO> similarityAnalysisVOS = waterCorrelationMapper.selectByParam(paramMap);
            ArrayList<Map<String, Object>> pollutants = new ArrayList<>();
            if (similarityAnalysisVOS != null && similarityAnalysisVOS.size() > 0) {
                String similarityAnalysispkId = similarityAnalysisVOS.get(0).getPkId();
                paramMap.clear();
                paramMap.put("fkWatercorrelationid", similarityAnalysispkId);
                List<WaterCorrelationPollutantSetVO> analysisPollutantSetVOS =
                        waterCorrelationPollutantSetMapper.selectByParam(paramMap);
                if (analysisPollutantSetVOS != null && analysisPollutantSetVOS.size() > 0) {
                    analysisPollutantSetVOS.forEach(item -> {
                        HashMap<String, Object> paramMaps = new HashMap<>();
                        paramMaps.put("pollutantcode", item.getFkPollutantcode());
                        List<Map<String, Object>> pollutantsByCodesAndType = pollutantFactorMapper.getPollutantsByCodesAndType(paramMaps);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("pollutantcode", item.getFkPollutantcode());
                        map.put("value", item.getValue() + "mg/L");
                        map.put("pollutantname", pollutantsByCodesAndType.get(0).get("name"));
                        pollutants.add(map);
                        if (item.getFkPollutantcode().equals(pollutantcode)) {
                            result.put("similarity", item.getR());
                        }
                    });
                }
            }
            result.put("pollutants", pollutants);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 11:39
     * @Description: 获取表头数据数据 - 预警及超标报警
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getOtherTableTitleDataForOverAlarm(Integer type) {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<String> props = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        props.add("monitorpointname");
        labels.add("监测点名称");
        for (int i = 0; i < props.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            if (props.get(i).equals("onlinestatus")) {
                map.put("type", "element");
            }
            if (props.get(i).equals("monitortime")) {
                map.put("minwidth", "200px");
            }
            if (props.get(i).equals("outputname")) {
                map.put("minwidth", "200px");
            }
            if (props.get(i).equals("monitorpointname")) {
                map.put("minwidth", "120px");
            }
            if (props.get(i).equals("shortername")) {
                map.put("minwidth", "300px");
            }
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", props.get(i));
            map.put("label", labels.get(i));
            map.put("align", "center");
            maps.add(map);
        }
        return maps;

    }
}