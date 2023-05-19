package com.tjpu.sp.controller.base.pollution;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.environmentalprotection.monitorpoint.CountMonitorPointController;
import com.tjpu.sp.controller.environmentalprotection.onlinemonitor.OnlineCountController;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import com.tjpu.sp.model.envhousekeepers.EntExecuteReportVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.creditevaluation.EnvCreditEvaluationService;
import com.tjpu.sp.service.environmentalprotection.licence.LicenceService;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.EffectiveTransmissionService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineWaterQualityService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.config.fileconfig.BusinessTypeConfig.businessTypeMap;

@RestController
@RequestMapping("pollution")
public class PollutionController {

    private final PollutionService pollutionService;

    private final WaterOutPutInfoService waterOutPutInfoService;
    private final GasOutPutInfoService gasOutPutInfoService;
    private final OtherMonitorPointService otherMonitorPointService;
    private final AirMonitorStationService airMonitorStationService;

    private final OutPutUnorganizedService outPutUnorganizedService;
    private final EffectiveTransmissionService effectiveTransmissionService;
    private final OnlineService onlineService;

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    private OnlineCountController onlineCountController;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private StopProductionInfoService stopProductionInfoService;
    @Autowired
    private MonitorControlService monitorControlService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private OnlineWaterQualityService onlineWaterQualityService;
    @Autowired
    private SoilPointService soilPointService;
    @Autowired
    private CountMonitorPointController countMonitorPointController;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private OnlineMonitorService onlineMonitorService;
    @Autowired
    private LicenceService licenceService;
    @Autowired
    private EnvCreditEvaluationService envCreditEvaluationService;

    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;

    //图片类型数据集合
    public static List<String> imgList = new ArrayList<>();
    private String sysmodel = "pollutionInfo";
    private String pk_id = "pk_pollutionid";
    private final String DB_YearFlowData = "YearFlowData";
    /**
     * 数据中心数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;



    public PollutionController(PollutionService pollutionService, WaterOutPutInfoService waterOutPutInfoService, GasOutPutInfoService gasOutPutInfoService, OtherMonitorPointService otherMonitorPointService, AirMonitorStationService airMonitorStationService, OutPutUnorganizedService outPutUnorganizedService, EffectiveTransmissionService effectiveTransmissionService, OnlineService onlineService) {
        this.pollutionService = pollutionService;
        this.waterOutPutInfoService = waterOutPutInfoService;
        this.gasOutPutInfoService = gasOutPutInfoService;
        this.otherMonitorPointService = otherMonitorPointService;
        this.airMonitorStationService = airMonitorStationService;
        this.outPutUnorganizedService = outPutUnorganizedService;

        this.effectiveTransmissionService = effectiveTransmissionService;
        this.onlineService = onlineService;
    }

    static {
        imgList.add("BMP");
        imgList.add("JPG");
        imgList.add("JPEG");
        imgList.add("PNG");
        imgList.add("GIF");
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 16:01
     * @Description: 按行业类型统计企业分布情况
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEnterpriseForIndustry", method = RequestMethod.GET)
    public Object countEnterpriseForIndustry() {
        try {
            List<Map<String, Object>> listMap = pollutionService.getEnterpriseForIndustry();
            int total = 0;
            for (Map<String, Object> map : listMap) {
                total += Integer.parseInt(map.get("num").toString());
            }
            Map<String, Object> map = new HashMap<>();
            map.put("total", total);
            map.put("list", listMap);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/12 0012 下午 2:11
     * @Description: 按污染标签统计风险源企业数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countRiskSourcesForPollutionLabel", method = RequestMethod.GET)
    public Object countRiskSourcesForPollutionLabel() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("labletype", "风险分类标签");
            List<Map<String, Object>> listMap = pollutionService.countPollutionForPollutionLabelByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过监测点类型（1-废水，2-废气，37-雨水,5-空气,6-水质,9-恶臭，10-voc）获取企业及排放口/监测点及状态信息
     * @updateUser:xsm
     * @updateDate:2019/12/26 0026 下午 1:58
     * @updateDescription:排口名称附加排口停产、是否监控状态
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionOuputsAndStatusByMonitorPointType", method = RequestMethod.POST)
    public Object getPollutionOuputsAndStatusByMonitorPointType(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                                @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                @RequestJson(value = "orderstatus", required = false) Boolean orderstatus) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (pollutionid != null && !"".equals(pollutionid)) {
                paramMap.put("pollutionids", Arrays.asList(pollutionid));
            }
            if (orderstatus != null && orderstatus == true) {
                paramMap.put("orderfield", "status");
            }
            if (dgimns != null && dgimns.size() > 0) {
                paramMap.put("dgimns", dgimns);
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                    case WasteWaterEnum:
                        paramMap.put("outputtype", "water");
                        dataList = waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap);
                        break;
                    case WasteGasEnum:
                        paramMap.put("monitorpointtype", monitorpointtype);
                        dataList = gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap);
                        break;
                    case SmokeEnum:
                        paramMap.put("monitorpointtype", monitorpointtype);
                        dataList = gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap);
                        break;
                    case RainEnum:
                        paramMap.put("outputtype", "rain");
                        dataList = waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap);
                        break;
                    case AirEnum:
                        dataList = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                        break;
                    case WaterQualityEnum:
                        dataList = waterStationService.getOnlineWaterStationInfoByParamMap(paramMap);
                        break;
                    case EnvironmentalStinkEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                        dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                        break;
                    case EnvironmentalDustEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode());
                        dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                        break;
                    case meteoEnum:
                        paramMap.remove("dgimns");
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                        dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                        break;
                    case EnvironmentalVocEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                        dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                        break;
                    case soilEnum:
                        dataList = soilPointService.getSoilPointInfoByParamMap(paramMap);
                        break;
                    case MicroStationEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                        dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                        break;
                    case FactoryBoundaryStinkEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                        dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                        dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;
                    default://其他点位信息
                        paramMap.put("monitorPointType", monitorpointtype);
                        dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                  /*  case FactoryBoundaryDustEnum://扬尘
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryDustEnum.getCode());
                        dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;*/
                }
                if (dataList != null && dataList.size() > 0) {
                    if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//为废气、废水排口时
                        paramMap.put("monitorpointtype", monitorpointtype);
                        List<Map<String, Object>> stoplist = stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(paramMap);
                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> obj : dataList) {
                                List<Map<String, Object>> outputlist = (List<Map<String, Object>>) obj.get("outputdata");
                                for (Map<String, Object> map : outputlist) {
                                    for (Map<String, Object> stopmap : stoplist) {
                                        if ((map.get("pk_id").toString()).equals(stopmap.get("FK_Outputid").toString())) {
                                            map.put("outputname", map.get("outputname") + "【停产】");
                                        }
                                    }
                                }
                            }
                        }
                    }/* else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                        paramMap.put("monitorpointtype", monitorpointtype);
                        List<Map<String, Object>> stoplist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> obj : dataList) {
                                List<Map<String, Object>> outputlist = (List<Map<String, Object>>) obj.get("outputdata");
                                for (Map<String, Object> map : outputlist) {
                                    for (Map<String, Object> stopmap : stoplist) {
                                        if ((map.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                            map.put("outputname", map.get("outputname") + "【排放中】");
                                        }
                                    }
                                }
                            }
                        }
                    }*/
                }
                if (orderstatus != null && orderstatus == true) {
                    if (dataList != null && dataList.size() > 0) {
                        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() ||
                                monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() ||
                                monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode() ||
                                monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {
                            for (Map<String, Object> objmap : dataList) {
                                String namebycode = "";
                                String thestatus = objmap.get("onlinestatus") != null ? objmap.get("onlinestatus").toString() : "";
                                if (!"".equals(thestatus)) {
                                    namebycode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(thestatus);
                                }
                                if (!"".equals(namebycode)) {
                                    objmap.put("monitorpointname", namebycode + "_" + objmap.get("monitorpointname"));
                                }
                            }
                        }
                    }
                }
            }
            if (dataList.size() > 0) {
                dataList = dataList.stream().sorted(
                        Comparator.comparingInt(
                                m -> ((Map) m).get("OrderIndex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("OrderIndex").toString())
                        )
                ).collect(Collectors.toList());
            }

            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/1/21 0021 下午 4:56
     * @Description: 通过多参数获取所有监测点树信息 不包含权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid, monitorpointtypes, orderstatus]
     * @throws:
     */
    @RequestMapping(value = "getPollutionOuputsAndStatusByParamsNoAuth", method = RequestMethod.POST)
    public Object getPollutionOuputsAndStatusByParamsNoAuth(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                            @RequestJson(value = "orderstatus", required = false) Boolean orderstatus) throws Exception {
        try {
            List<Map<String, Object>> dataList = new LinkedList<>();
            Map<String, Object> paramMap = new HashMap<>();
            Object allMonitorPointInfosByMonitorPointTypes = countMonitorPointController.getAllMonitorPointInfosByMonitorPointTypes(monitorpointtypes);
            if (pollutionid != null && !"".equals(pollutionid)) {
                paramMap.put("pollutionids", Arrays.asList(pollutionid));
            }
            if (orderstatus != null && orderstatus == true) {
                paramMap.put("orderfield", "status");
            }
            JSONObject jsonObject = JSONObject.fromObject(allMonitorPointInfosByMonitorPointTypes);
            Collection<Object> values = (jsonObject.get("data") == null ? new HashMap<>() : (Map<String, Object>) jsonObject.get("data")).values();
            List<String> dgimns = values.stream().flatMap(m -> ((List<Map<String, Object>>) m).stream()).filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            for (Integer monitorpointtype : monitorpointtypes) {
                List<Map<String, Object>> maps = setData(pollutionid, orderstatus, monitorpointtype, paramMap, dgimns);
                dataList.addAll(maps);
            }
            Map<String, List<Map<String, Object>>> collect = dataList.stream().collect(Collectors.groupingBy(m -> m.get("pk_pollutionid") == null && m.get("shortername") == null ? "" : m.get("pk_pollutionid").toString() + "_" + m.get("shortername").toString()));
            //按类型排序
            List<String> collect1 = collect.keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
            dataList.clear();
            for (String pk_pollutionidAndName : collect1) {
                List<Map<String, Object>> maps = collect.get(pk_pollutionidAndName);
                String[] split = pk_pollutionidAndName.split("_");
                if (split.length > 1) {
                    Map<String, Object> data = new HashMap<>();
                    String pk_pollutionid = split[0];
                    String shortername = split[1];
                    List<Map<String, Object>> outputdata = maps.stream().filter(m -> m.get("outputdata") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("outputdata")).stream()).peek(m -> {
                        m.put("pk_pollutionid", pk_pollutionid);
                        m.put("shortername", shortername);
                    }).collect(Collectors.toList());
                    data.put("pk_pollutionid", pk_pollutionid);
                    data.put("shortername", shortername);
                    data.put("outputdata", outputdata);
                    dataList.add(data);
                } else {
                    dataList.addAll(maps);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> setData(String pollutionid, Boolean orderstatus, Integer monitorpointtype, Map<String, Object> paramMap, List<String> dgimns) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (dgimns != null && dgimns.size() > 0) {
            paramMap.put("dgimns", dgimns);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    dataList = waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    paramMap.put("monitorpointtype", monitorpointtype);
                    dataList = gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap);
                    break;
                case SmokeEnum:
                    paramMap.put("monitorpointtype", monitorpointtype);
                    dataList = gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputtype", "rain");
                    dataList = waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case AirEnum:
                    dataList = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case WaterQualityEnum:
                    dataList = waterStationService.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                    paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case EnvironmentalDustEnum:
                    paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode());
                    dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case meteoEnum:
                    paramMap.remove("dgimns");
                    paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                    dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case EnvironmentalVocEnum:
                    paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                    dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case soilEnum:
                    dataList = soilPointService.getSoilPointInfoByParamMap(paramMap);
                    break;
                case MicroStationEnum:
                    paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                    dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                  /*  case FactoryBoundaryDustEnum://扬尘
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryDustEnum.getCode());
                        dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                        break;*/
            }
            if (dataList != null && dataList.size() > 0) {
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//为废气、废水排口时
                    paramMap.put("monitorpointtype", monitorpointtype);
                    List<Map<String, Object>> stoplist = stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(paramMap);
                    if (stoplist != null && stoplist.size() > 0) {
                        for (Map<String, Object> obj : dataList) {
                            List<Map<String, Object>> outputlist = (List<Map<String, Object>>) obj.get("outputdata");
                            for (Map<String, Object> map : outputlist) {
                                for (Map<String, Object> stopmap : stoplist) {
                                    if ((map.get("pk_id").toString()).equals(stopmap.get("FK_Outputid").toString())) {
                                        map.put("outputname", map.get("outputname") + "【停产】");
                                    }
                                }
                            }
                        }
                    }
                } /*else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                    paramMap.put("monitorpointtype", monitorpointtype);
                    List<Map<String, Object>> stoplist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
                    if (stoplist != null && stoplist.size() > 0) {
                        for (Map<String, Object> obj : dataList) {
                            List<Map<String, Object>> outputlist = (List<Map<String, Object>>) obj.get("outputdata");
                            for (Map<String, Object> map : outputlist) {
                                for (Map<String, Object> stopmap : stoplist) {
                                    if ((map.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                        map.put("outputname", map.get("outputname") + "【排放中】");
                                    }
                                }
                            }
                        }
                    }
                }*/
            }
            if (orderstatus != null && orderstatus == true) {
                if (dataList != null && dataList.size() > 0) {
                    if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() ||
                            monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() ||
                            monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode() ||
                            monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {
                        for (Map<String, Object> objmap : dataList) {
                            String namebycode = "";
                            String thestatus = objmap.get("onlinestatus") != null ? objmap.get("onlinestatus").toString() : "";
                            if (!"".equals(thestatus)) {
                                namebycode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(thestatus);
                            }
                            if (!"".equals(namebycode)) {
                                objmap.put("monitorpointname", namebycode + "_" + objmap.get("monitorpointname"));
                            }
                        }
                    }
                }
            }
        }
        return dataList;
    }


    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 2:52
     * @Description: 通过监测点类型数组及监测点类别（1-废水，2-废气，37-雨水,5-空气,6-水质,9-恶臭，10-voc）获取企业及排放口/监测点及状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, monitorpointcategory]
     * @throws:
     */
    @RequestMapping(value = "getPollutionOuputsAndStatusByMonitorPointTypes", method = RequestMethod.POST)
    public Object getPollutionOuputsAndStatusByMonitorPointTypes(@RequestJson(value = "monitorpointtypes") Object monitorpointtypes,
                                                                 @RequestJson(value = "treeflag", required = false) Boolean treeflag,
                                                                 @RequestJson(value = "pollutionids", required = false) Object pollutionids,
                                                                 @RequestJson(value = "monitorpointcategorys", required = false) Object monitorpointcategorys) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionids", pollutionids);
            paramMap.put("userid", userId);
            for (Integer monitorpointtype : (List<Integer>) monitorpointtypes) {
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                    case WasteWaterEnum:
                        paramMap.put("outputtype", "water");
                        dataList.addAll(waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap));
                        break;
                    case WasteGasEnum:
                        paramMap.put("monitorpointtype", monitorpointtype);
                        dataList.addAll(gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap));
                        break;
                    case SmokeEnum:
                        paramMap.put("monitorpointtype", monitorpointtype);
                        dataList.addAll(gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap));
                        break;
                    case RainEnum:
                        paramMap.put("outputtype", "rain");
                        dataList.addAll(waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap));
                        break;
                    case AirEnum:
                        dataList.addAll(airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap));
                        break;
                    case WaterQualityEnum:
                        dataList.addAll(waterStationService.getOnlineWaterStationInfoByParamMap(paramMap));
                        break;
                    case EnvironmentalStinkEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                        paramMap.put("monitorPointCategorys", monitorpointcategorys);
                        dataList.addAll(otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap));
                        break;
                    case EnvironmentalDustEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode());
                        dataList.addAll(otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap));
                        break;
                    case MicroStationEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                        dataList.addAll(otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap));
                        break;
                    case EnvironmentalVocEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                        paramMap.put("monitorpointcategory", monitorpointcategorys);
                        dataList.addAll(otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap));
                        break;
                    case FactoryBoundaryStinkEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                        dataList.addAll(outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap));
                        break;
                    case FactoryBoundarySmallStationEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                        dataList.addAll(outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap));
                        break;
                    case unOrganizationWasteGasEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.unOrganizationWasteGasEnum.getCode());
                        dataList.addAll(outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap));
                        break;
                }
            }
            if (treeflag != null && treeflag) {
                Map<String, List<Map<String, Object>>> collect = dataList.stream().filter(m -> m.get("pk_pollutionid") != null && m.get("shortername") != null).collect(Collectors.groupingBy(m -> m.get("pk_pollutionid").toString() + "_" + m.get("shortername").toString()));
                dataList.clear();
                for (String key : collect.keySet()) {
                    Map<String, Object> data = new HashMap<>();
                    String[] split = key.split("_");
                    String pk_pollutionid = split[0];
                    String shortername = split[1];
                    List<Map<String, Object>> maps = collect.get(key);
                    List<Map<String, Object>> alloutput = maps.stream().filter(m -> m.get("outputdata") != null).flatMap(m -> ((List<Map<String, Object>>) (m.get("outputdata"))).stream()).collect(Collectors.toList());
                    data.put("pk_pollutionid", pk_pollutionid);
                    data.put("shortername", shortername);
                    data.put("outputdata", alloutput);
                    dataList.add(data);
                }
            }
            if (dataList.size() > 0) {
                String dataSort = DataFormatUtil.parseProperties("data.sort");
                JSONObject jsonObject = StringUtils.isNotBlank(dataSort) ? JSONObject.fromObject(dataSort) : new JSONObject();
                String onlineStatus;
                String statusKey;
                for (Map<String, Object> dataMap : dataList) {
                    onlineStatus = dataMap.get("onlinestatus") + "";
                    statusKey = CommonTypeEnum.StatusOrderSetEnum.getIndexByCode(onlineStatus);
                    dataMap.put("orderindex", jsonObject.get(statusKey) != null ? jsonObject.get(statusKey) : 11);
                }
                dataList = dataList.stream().sorted(
                        Comparator.comparingInt(
                                m -> ((Map) m).get("orderindex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("orderindex").toString())
                        )
                ).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过监测点类型获取排放口/监测点及状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointAndStatusByTypeAndId", method = RequestMethod.POST)
    public Object getMonitorPointAndStatusByMonitorPointType(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutionid", required = false) String pollutionid
    ) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (monitorpointtypes.size() > 0) {
                for (Integer type : monitorpointtypes) {
                    List<Map<String, Object>> dataListSub = getMonitorPointData(type, pollutionid);
                    dataList.addAll(dataListSub);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/29 0029 上午 9:26
     * @Description: 根据污染源id和监测类型获取排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointData(Integer type, String pollutionid) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("userid", userid);
        if (StringUtils.isNotBlank(pollutionid)) {
            paramMap.put("pollutionids", Arrays.asList(pollutionid));
        }
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
            case WasteWaterEnum:
                paramMap.put("outputtype", "water");
                dataList = waterOutPutInfoService.getWaterOuPutAndStatusByParamMap(paramMap);
                break;
            case SmokeEnum:
            case WasteGasEnum:
                paramMap.put("monitorpointtype", type);
                dataList = gasOutPutInfoService.getGasOutPutAndStatusByParamMap(paramMap);
                break;
            case RainEnum:
                paramMap.put("outputtype", "rain");
                dataList = waterOutPutInfoService.getWaterOuPutAndStatusByParamMap(paramMap);
                break;
            case EnvironmentalStinkEnum:
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalDustEnum:
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode());
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case MicroStationEnum:
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalVocEnum:
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                dataList = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                dataList = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
            case WaterQualityEnum:
                dataList = waterStationService.getOnlineWaterStationInfoByParamMap(paramMap);
                break;
        }
        if (dataList.size() > 0) {

            Map<String, Map<String, Object>> data = new HashMap<>();

            Set<String> mns = new HashSet<>();
            String mn;
            for (Map<String, Object> map : dataList) {
                mn = map.get("dgimn").toString();
                mns.add(mn);
            }
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            String ymd = DataFormatUtil.getDateYMD(new Date());
            queryVO.setMonitorPointType(type);
            queryVO.setStartTime(DataFormatUtil.parseDate(ymd + " 00:00:00"));
            queryVO.setEndTime(DataFormatUtil.parseDate(ymd + " 23:59:59"));
            queryVO.setMns(mns);
            List<Map<String, Object>> resultList = new ArrayList<>();
            int[] reminds = CommonTypeEnum.getRemindsByMonitorPointType(type);

            onlineCountController.countAlarmRemindNum(reminds, queryVO, data, 1);
            Map<String, Integer> mnAndAlarmNum = new HashMap<>();
            if (data.size() > 0) {
                Map<String, Object> remindData;
                int num;
                for (String mnKey : data.keySet()) {
                    num = 0;
                    remindData = data.get(mnKey);
                    for (String remindType : remindData.keySet()) {
                        num += remindData.get(remindType) != null ? Integer.parseInt(remindData.get(remindType).toString()) : 0;
                    }
                    mnAndAlarmNum.put(mnKey, num);
                }
            }
            for (Map<String, Object> map : dataList) {
                Map<String, Object> resultMap = new HashMap<>();
                if (CommonTypeEnum.getOutPutTypeList().contains(type)) {
                    resultMap.put("monitorpointname", map.get("outputname"));
                } else {
                    resultMap.put("monitorpointname", map.get("monitorpointname"));
                }
                mn = map.get("dgimn").toString();
                resultMap.put("monitorpointid", map.get("pk_id"));
                resultMap.put("pollutionid", map.get("pk_pollutionid"));
                resultMap.put("pollutionname", map.get("pollutionname"));
                resultMap.put("Longitude", map.get("Longitude"));
                resultMap.put("Latitude", map.get("Latitude"));
                resultMap.put("dgimn", mn);
                resultMap.put("alarmnum", mnAndAlarmNum.get(mn) != null ? mnAndAlarmNum.get(mn) : 0);
                resultMap.put("onlinestatus", map.get("onlinestatus"));
                resultMap.put("monitorpointtype", type);
                resultList.add(resultMap);
            }
            return resultList;
        } else {
            return dataList;
        }


    }


    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 获取污染源初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "/getPollutionListPage", method = RequestMethod.POST)
    public Object getPollutionListPage(HttpServletRequest request) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 通过自定义参数获取污染源列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "/getPollutionsInfoByParamMap", method = RequestMethod.POST)
    public Object getPollutionsInfoByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(param);
            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 通过id获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "getPollutionInfoByid", method = RequestMethod.POST)
    public Object getPollutionInfoByid(@RequestJson(value = "id") String id) throws Exception {
        try {
            PollutionVO pollutionAndLabelsByPollutionid = pollutionService.getPollutionAndLabelsByPollutionid(id);
            PollutionVO jsonObject = formatDate(pollutionAndLabelsByPollutionid, "yyyy-MM-dd");
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("flag", "success");
            map.put("data", jsonObject);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/12 11:28
     * @Description: 监测点状态及污染物统计
     * @updateUser:xsm
     * @updateDate:2022/04/18 15:09
     * @updateDescription:修改其它监测点表为通用类型表（支持添加类型，不用修改接口）
     * @param:
     * @return:
     */
    @RequestMapping(value = "countMonitorPointStateAndPollutants", method = RequestMethod.POST)
    public Object countMonitorPointStateAndPollutants(
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorPointType,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorPointTypes
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            //添加数据权限
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("datauserid", userid);
            List<Map<String, Object>> outputs = new ArrayList<>();
            if (monitorPointTypes == null || monitorPointTypes.size() == 0) {
                monitorPointTypes = new ArrayList<>();
                monitorPointTypes.add(monitorPointType);
            }
            String time = LocalDate.now().toString();
            paramMap.put("time", time);
            int all = 0;
            List<Map<String, Object>> outPuts = new ArrayList<>();
            Map<String, Object> monitorpointstatus = new LinkedHashMap<>();
            Set<String> stopOutPutIds = new HashSet<>();
            //获取所有非其它监测类型表的监测点类型
            List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
            for (Integer monitortype : monitorPointTypes) {
                if (notothertypes.contains(monitortype)) {//非其它监测点类型
                    paramMap.put("monitorpointtype", monitortype);
                    outputs.addAll(onlineService.getOutPutsAndPollutantsByParam(paramMap));
                } else {//其它监测点类型表
                    paramMap.put("monitorpointtype", monitortype);
                    outputs.addAll(onlineService.getOtherPointInfoAndPollutantsByParam(paramMap));
                }
                CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = getCodeByInt(monitortype);
                if (monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum
                        || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum
                        || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum
                        || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.RainEnum) {
                    //获取停产点位
                    outPuts.addAll(getOutPutInfoByType(monitortype));
                }
            }
            //获取污染物
            Map<String, Integer> nameAndOrder = new HashMap<>();
            for (Map<String, Object> output : outputs) {
                if (output.get("Name") != null) {
                    nameAndOrder.put(output.get("Name").toString(), output.get("OrderIndex") != null ? Integer.parseInt(output.get("OrderIndex").toString()) : -99);
                }
            }
            nameAndOrder = DataFormatUtil.sortMapByValue(nameAndOrder, false);

            Set<String> name = outputs.stream().filter(m -> m.get("Name") != null).map(output -> output.get("Name").toString()).collect(Collectors.toSet());
            //根据排口ID去重
            Map<String, Object> outPutMap = outputs.stream().filter(m -> m.get("PK_ID") != null && m.get("Status") != null).collect(Collectors.toMap(p -> p.get("PK_ID").toString(), p -> p.get("Status"), (k1, k2) -> k1));
            for (Map<String, Object> output : outPuts) {
                if (output.get("onlinestatusname") != null && "停产".equals(output.get("onlinestatusname").toString())) {
                    stopOutPutIds.add(output.get("pk_id").toString());
                }
            }
            int stopnum = stopOutPutIds.size();
            monitorpointstatus.put("tc", stopnum);
            //移除停产排口
            all += stopOutPutIds.size();
            stopOutPutIds.stream().filter(outPutMap::containsKey).forEach(outPutMap::remove);
            int onlinenum = 0;
            int offlinenum = 0;
            int overNum = 0;
            int exceptionNum = 0;
            String codeTemp;
            String online = CommonTypeEnum.OnlineStatusEnum.NormalStatusEnum.getCode();
            String offline = CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode();
            String over = CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode();
            String exception = CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum.getCode();
            for (Map.Entry<String, Object> entry : outPutMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    codeTemp = value.toString();
                    if (codeTemp.equals(online)) {
                        all++;
                        onlinenum++;
                    }
                    if (codeTemp.equals(offline)) {
                        all++;
                        offlinenum++;
                    }
                    if (codeTemp.equals(over)) {
                        all++;
                        overNum++;
                    }
                    if (codeTemp.equals(exception)) {
                        all++;
                        exceptionNum++;
                    }
                }
            }
            monitorpointstatus.put("all", all);
            monitorpointstatus.put("lx", offlinenum);
            monitorpointstatus.put("zc", onlinenum);
            monitorpointstatus.put("cb", overNum);
            monitorpointstatus.put("yc", exceptionNum);
            resultMap.put("state", monitorpointstatus);
            resultMap.put("pllutants", nameAndOrder.keySet());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/11/30 0030 下午 1:12
     * @Description: 统计恶臭点位状态及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countStinkPointStateAndPollutant", method = RequestMethod.POST)
    public Object countStinkPointStateAndPollutant() {
        try {
            Map<String, Object> resultMap = new HashMap<>();


            Map<String, Object> paramMap = new HashMap<>();
            //根据监测点类型获取污染物
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            paramMap.put("entmonitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
            List<Map<String, Object>> outputs = onlineService.getStinkPointStateAndPollutantsByParam(paramMap);
            //获取污染物
            Set<String> name = outputs.stream().filter(m -> m.get("Name") != null).map(output -> output.get("Name").toString()).collect(Collectors.toSet());
            //根据排口ID去重
            Map<String, Object> outPutMap = outputs.stream().filter(m -> m.get("PK_ID") != null && m.get("Status") != null).collect(Collectors.toMap(p -> p.get("PK_ID").toString(), p -> p.get("Status"), (k1, k2) -> k1));
            Map<String, Object> monitorpointstatus = new LinkedHashMap<>();
            int all = 0;
            int onlinenum = 0;
            int offlinenum = 0;
            int overNum = 0;
            int exceptionNum = 0;
            String codeTemp;
            String online = CommonTypeEnum.OnlineStatusEnum.NormalStatusEnum.getCode();
            String offline = CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode();
            String over = CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode();
            String exception = CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum.getCode();
            for (Map.Entry<String, Object> entry : outPutMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    codeTemp = value.toString();
                    if (codeTemp.equals(online)) {
                        all++;
                        onlinenum++;
                    }
                    if (codeTemp.equals(offline)) {
                        all++;
                        offlinenum++;
                    }
                    if (codeTemp.equals(over)) {
                        all++;
                        overNum++;
                    }
                    if (codeTemp.equals(exception)) {
                        all++;
                        exceptionNum++;
                    }
                }
            }
            monitorpointstatus.put("all", all);
            monitorpointstatus.put("lx", offlinenum);
            monitorpointstatus.put("zc", onlinenum);
            monitorpointstatus.put("cb", overNum);
            monitorpointstatus.put("yc", exceptionNum);
            resultMap.put("state", monitorpointstatus);
            resultMap.put("pllutants", name);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/12/24 0024 下午 1:46
     * @Description: 根据监测点类型获取点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getOutPutInfoByType(Integer monitorPointType) {
        List<Map<String, Object>> outPuts = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid",RedisTemplateUtil.getRedisCacheDataByToken("userid",String.class));
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
            case WasteWaterEnum:
                paramMap.put("outputtype", "water");
                outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                break;
            case SmokeEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                break;
            case WasteGasEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                break;
            case RainEnum:
                paramMap.put("outputtype", "rain");
                outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                break;
        }
        return outPuts;
    }


    /**
     * @author: lip
     * @date: 2019/7/31 0031 上午 9:15
     * @Description: 根据监测点类型获取企业总览信息：监测点状态信息（在线数、离线数、停产数）、传输信息（最新）、
     * 企业信息（总数、安装在线监测数、未安装在线监测数、排污许可证数）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionOverviewDataByMonitorPointType", method = RequestMethod.POST)
    public Object getPollutionOverviewDataByMonitorPointType(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) throws Exception {
        try {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (monitorpointtypes.size() > 0) {
                //企业信息
                Map<String, Object> paramMap = new HashMap<>();
                long totalpollution = pollutionService.countTotalByParam(paramMap);
                resultMap.put("totalpollution", totalpollution);
                boolean israin = false;
                //安装在线设备企业（废水、废气、雨水）
                List<Map<String, Object>> outputList = new ArrayList<>();
                paramMap.put("userid", userId);
                for (Integer type : monitorpointtypes) {
                    switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                        case WasteWaterEnum:
                            paramMap.put("outputtype", "water");
                            outputList.addAll(waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap));
                            break;
                        case WasteGasEnum:
                        case SmokeEnum:
                            outputList.addAll(gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap));
                            break;
                        case RainEnum:
                            paramMap.put("outputtype", "rain");
                            outputList.addAll(waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap));
                            israin = true;
                            break;
                    }
                }


                Set<Object> onlinepollution = new HashSet<>();
                for (Map<String, Object> map : outputList) {
                    if (map.get("pk_pollutionid") != null) {
                        onlinepollution.add(map.get("pk_pollutionid"));
                    }
                }
                int offlinepollution = (int) (totalpollution - onlinepollution.size());
                resultMap.put("onlinepollution", onlinepollution.size());
                resultMap.put("offlinepollution", offlinepollution);
                List<Map<String, Object>> pwxkpollution = pollutionService.getPWXKPollutionDataByParamMap(paramMap);
                resultMap.put("pwxkpollution", pwxkpollution.size());
                //监测点状态信息
                //传输信息
                int onlinenum = 0;
                int offlinenum = 0;
                int stopnum = 0;//停产类型：厂界是否有
                Set<String> stopOutPutIds = new HashSet<>();
                // String startStatus = CommonTypeEnum.MonitorPointStatusEnum.StartEnum.getCode() + "";
                List<Map<String, Object>> rainstoplist = new ArrayList<>();
                if (israin == true) {
                    Map<String, Object> rainparam = new HashMap<>();
                    rainparam.put("monitorpointtype", RainEnum.getCode());
                    rainstoplist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);

                }

                for (Map<String, Object> output : outputList) {
                    if (output.get("monitorpointtype") != null && (output.get("monitorpointtype").toString()).equals(RainEnum.getCode() + "")) {
                        if (rainstoplist != null && rainstoplist.size() > 0) {
                            for (Map<String, Object> stopmap : rainstoplist) {
                                if ((output.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                    stopOutPutIds.add(output.get("pk_id").toString());
                                }
                            }
                        }
                    } else {
                        if (output.get("onlinestatusname") != null && (output.get("onlinestatusname").toString().equals("停产")
                                || output.get("onlinestatusname").toString().equals("排放中"))) {
                            //if (!(output.get("status") != null && startStatus.equals(output.get("status").toString()))) {
                            stopOutPutIds.add(output.get("pk_id").toString());
                        }
                    }
                }
                stopnum = stopOutPutIds.size();
                Map<String, Object> monitorpointstatus = new LinkedHashMap<>();
                List<Map<String, Object>> monitorPointDataList = new ArrayList<>();
                Map<String, Object> effectivetransmissiondata = new LinkedHashMap<>();
                String updateDate = null;
                Set<String> updateDates = new HashSet<>();
                for (Integer type : monitorpointtypes) {
                    paramMap.put("monitorpointtype", type);
                    updateDate = effectiveTransmissionService.getLastDateByParamMap(paramMap);
                    if (updateDate != null) {
                        updateDates.add(updateDate);
                    }
                }
                if (updateDates.size() > 0) {
                    Set<String> sortSet = new TreeSet<>(Comparator.reverseOrder());
                    sortSet.addAll(updateDates);
                    updateDate = sortSet.iterator().next();
                    paramMap.clear();
                    paramMap.put("countdate", updateDate);
                    //有效传输数据集合
                    List<Map<String, Object>> estData = new ArrayList<>();
                    for (Integer type : monitorpointtypes) {
                        paramMap.put("monitorpointtype", type);
                        List<Map<String, Object>> estDataSub = effectiveTransmissionService.getEffectiveTransmissionByParamMap(paramMap);
                        estData.addAll(estDataSub);
                    }
                    if (estData.size() > 0) {
                        //实传输数量
                        Double transmissionnumber = estData.stream().peek(m -> m.put("transmissionnumber", decimalFormat.format(Integer.valueOf(m.get("transmissionnumber") == null ? "0" : m.get("transmissionnumber")
                                .toString())))).map(m -> Double.valueOf(m.get("transmissionnumber") == null ? "0d" : m.get("transmissionnumber").toString())).collect(Collectors.summingDouble(m -> m));
                        //实有效数量
                        Double effectivenumber = estData.stream().peek(m -> m.put("effectivenumber", decimalFormat.format(Integer.valueOf(m.get("effectivenumber") == null ? "0" : m.get("effectivenumber")
                                .toString())))).map(m -> Double.valueOf(m.get("effectivenumber") == null ? "0d" : m.get("effectivenumber").toString())).collect(Collectors.summingDouble(m -> m));
                        //应传输数量
                        Double shouldnumber = estData.stream().peek(m -> m.put("shouldnumber", decimalFormat.format(Integer.valueOf(m.get("shouldnumber") == null ? "0" : m.get("shouldnumber")
                                .toString())))).map(m -> Double.valueOf(m.get("shouldnumber") == null ? "0d" : m.get("shouldnumber").toString())).collect(Collectors.summingDouble(m -> m));
                        //应有效数量
                        Double shouldeffectivenumber = estData.stream().peek(m -> m.put("shouldeffectivenumber", decimalFormat.format(Integer.valueOf(m.get("shouldeffectivenumber") == null ? "0" : m.get("shouldeffectivenumber")
                                .toString())))).map(m -> Double.valueOf(m.get("shouldeffectivenumber") == null ? "0d" : m.get("shouldeffectivenumber").toString())).collect(Collectors.summingDouble(m -> m));
                        //传输率
                        double transmissionrate = shouldnumber == 0d ? 0d : transmissionnumber / shouldnumber;
                        //有效率
                        double effectiverate = shouldeffectivenumber == 0d ? 0d : effectivenumber / shouldeffectivenumber;
                        //传输有效率
                        double transmissioneffectiverate = transmissionrate * effectiverate;
                        effectivetransmissiondata.put("transmissionrate", decimalFormat.format(transmissionrate * 100) + "%");
                        effectivetransmissiondata.put("effectiverate", decimalFormat.format(effectiverate * 100) + "%");
                        effectivetransmissiondata.put("transmissioneffectiverate", decimalFormat.format(transmissioneffectiverate * 100) + "%");
                        effectivetransmissiondata.put("updatedate", updateDate);
                    }
                }
                resultMap.put("effectivetransmissiondata", effectivetransmissiondata);
                for (Integer type : monitorpointtypes) {
                    List<Map<String, Object>> monitorPointDataListSub = getMonitorPointData(type, "");
                    monitorPointDataList.addAll(monitorPointDataListSub);
                }
                String onlineCode = CommonTypeEnum.OnlineStatusEnum.NormalStatusEnum.getCode();
                String overCode = CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode();
                String exceptionCode = CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode();
                String offlineCode = CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode();
                String codeTemp;

                for (Map<String, Object> map : monitorPointDataList) {
                    if (map.get("onlinestatus") != null) {
                        codeTemp = map.get("onlinestatus").toString();
                        if (codeTemp.equals(onlineCode) || codeTemp.equals(overCode) || codeTemp.equals(exceptionCode)) {
                            onlinenum++;
                        }
                        if (codeTemp.equals(offlineCode)) {
                            offlinenum++;
                        }
                    }
                }
                monitorpointstatus.put("onlinenum", onlinenum);
                monitorpointstatus.put("offlinenum", offlinenum);
                monitorpointstatus.put("stopnum", stopnum);
                Date nowDay = new Date();
                Date startDate = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(nowDay) + " 00:00:00");
                Date endDate = nowDay;
                Map<String, Object> alarmData = onlineCountController.getAlarmEntNumByParam(monitorpointtypes, startDate, endDate);
                alarmData.put("monitorpointtype", monitorpointtypes.get(0));
                resultMap.put("alarmData", alarmData);
                resultMap.put("monitorpointstatus", monitorpointstatus);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/12/2 0002 下午 1:39
     * @Description: 获取监测点传输率、有效率、传输有效率列表信息
     * @updateUser: xsm
     * @updateDate: 2020/06/18 0018 上午 10:01
     * @updateDescription: 新增时间范围查询，新增企业或点位名称查询
     * @param: monitorpointcategory:1：敏感点，2：传输点
     * datamark:effectiverate(有效率)、transmissionrate（传输率）、transmissioneffectiverate（传输有效率）
     * starttime:开始时间 endtime:结束时间
     * customname:自定义名称（企业名称，点位名称）
     * @return:
     */
    @RequestMapping(value = "getMonitorPointTransmissionEffectiveRateList", method = RequestMethod.POST)
    public Object getMonitorPointTransmissionEffectiveRateList(
            @RequestJson(value = "pagenum") Integer pagenum,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "datamark") String datamark,
            @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "customname", required = false) String customname

    ) {
        try {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            if (StringUtils.isNotBlank(monitorpointcategory)) {
                paramMap.put("monitorpointcategory", monitorpointcategory);
            }
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("datamark", datamark);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("customname", customname);


            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            PageInfo<Map<String, Object>> pageInfo = pollutionService.getMonitorPointTransmissionEffectiveRateList(paramMap);
            resultMap.put("datalist", pageInfo.getList());
            resultMap.put("total", pageInfo.getTotal());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/24 0024 下午 1:29
     * @Description: 根据时间范围和多个监测类型获取监测点传输率、有效率、传输有效率列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitorpointcategory:1：敏感点，2：传输点
     * datamark:effectiverate(有效率)、transmissionrate（传输率）、transmissioneffectiverate（传输有效率）
     * starttime:开始时间 endtime:结束时间
     * customname:自定义名称（企业名称，点位名称）
     * @return:
     */
    @RequestMapping(value = "getManyMonitorPointTransmissionEffectiveRateList", method = RequestMethod.POST)
    public Object getManyMonitorPointTransmissionEffectiveRateList(
            @RequestJson(value = "pagenum") Integer pagenum,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "datamark") String datamark,
            @RequestJson(value = "ratioflag", required = false) String ratioflag,
            @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "customname", required = false) String customname

    ) {
        try {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            if (StringUtils.isNotBlank(monitorpointcategory)) {
                paramMap.put("monitorpointcategory", monitorpointcategory);
            }
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("datamark", datamark);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("ratioflag", ratioflag);
            paramMap.put("customname", customname);

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            PageInfo<Map<String, Object>> pageInfo = pollutionService.getManyMonitorPointTransmissionEffectiveRateList(paramMap);
            resultMap.put("datalist", pageInfo.getList());
            resultMap.put("total", pageInfo.getTotal());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 通过id和监测类型获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "getPollutionDataByIdAndType", method = RequestMethod.POST)
    public Object getPollutionDataByIdAndType(@RequestJson(value = "pollutionid") String pollutionid,
                                              @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype) throws Exception {
        try {
            List<Integer> monitortypes = new ArrayList<>();
            if (monitorpointtype != null) {
                monitortypes.add(monitorpointtype);
            } else {
                monitortypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            }
            Map<String, Object> paramMap = new HashMap<>();
            Set<Object> pollutantnames = new HashSet<>();
            for (int type : monitortypes) {
                paramMap.put("pollutionid", pollutionid);
                paramMap.put("monitorpointtype", type);
                List<Map<String, Object>> pollutionData = pollutionService.getPollutionDataByIdAndType(paramMap);
                paramMap.clear();
                if (pollutionData != null && pollutionData.size() > 0) {
                    for (Map<String, Object> map : pollutionData) {
                        paramMap.putIfAbsent("pollutionname", map.get("pollutionname"));
                        paramMap.put("devopspeople", map.get("devopspeople"));
                        paramMap.put("devopsunit", map.get("devopsunit"));
                        paramMap.put("telephone", map.get("telephone"));
                        paramMap.putIfAbsent("address", map.get("address"));
                        paramMap.putIfAbsent("environmentalmanager", map.get("environmentalmanager"));
                        paramMap.putIfAbsent("linkmanphone", map.get("linkmanphone"));
                        if (map.get("pollutantname") != null && !"".equals(map.get("pollutantname"))) {
                            pollutantnames.add(map.get("pollutantname"));
                        }
                    }
                }
            }
            paramMap.put("pollutantnames", pollutantnames);
            return AuthUtil.parseJsonKeyToLower("success", paramMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 通过自定义参数新增污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "addPollutionByParamMap", method = RequestMethod.POST)
    public Object addPollutionByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            PollutionVO pollutionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PollutionVO());
            pollutionVO.setPkpollutionid(UUID.randomUUID().toString());
            JSONArray pollutionlabels = new JSONArray();
            if (jsonObject.get("labelcode") != null) {
                Object labels = jsonObject.get("labelcode");
                Object collect = JSONArray.fromObject(labels).stream().filter(m -> !"null".equals(m.toString())).collect(Collectors.toList());
                pollutionlabels = JSONArray.fromObject(collect);
            }

            pollutionService.insertSelective(pollutionVO, pollutionlabels);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 通过自定义参数修改污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "updatePollutionByParamMap", method = RequestMethod.POST)
    public Object updatePollutionByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            PollutionVO pollutionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PollutionVO());
            JSONArray pollutionlabels = new JSONArray();
            if (jsonObject.get("labelcode") != null) {
                Object labels = jsonObject.get("labelcode");
                Object collect = JSONArray.fromObject(labels).stream().filter(m -> !"null".equals(m.toString())).collect(Collectors.toList());
                pollutionlabels = JSONArray.fromObject(collect);
            }

            pollutionService.updateByPrimaryKeySelective(pollutionVO, pollutionlabels);
            pollutionService.updateOutPutCode(pollutionVO.getPkpollutionid());
            return AuthUtil.parseJsonKeyToLower("success", null);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 2:57
     * @Description: 通过污染源id删除污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "deletePollutionById", method = RequestMethod.POST)
    public Object deletePollutionById(@RequestJson(value = "id") String id) throws Exception {
        try {
            if (StringUtils.isNotBlank(id)) {
                PollutionVO pollutionVO = pollutionService.selectByPrimaryKey(id);
                //获取所有fileflag
                List<String> fileflags = pollutionService.getImgIdByPollutionid(id);
                //删除污染源
                pollutionService.deleteByPrimaryKey(id);

                fileflags.add(pollutionVO.getFileid());


                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("fileflags", fileflags);
                List<FileInfoVO> filesInfosByParam = fileInfoService.getFilesInfosByParam(paramMap);
                //删除文件表数据
                fileInfoService.deleteByParam(paramMap);
                List<String> filePaths = filesInfosByParam.stream().map(m -> m.getFilepath()).collect(Collectors.toList());
                //删除mongos数据
                if (filePaths != null && filePaths.size() > 0) {
                    MongoDatabase useDatabase = mongoTemplate.getDb();
                    String collectionType = businessTypeMap.get("1");
                    GridFSBucket gridFSBucket = GridFSBuckets.create(useDatabase, collectionType);
                    fileInfoService.deleteFilesByParams(filePaths, gridFSBucket);
                }


            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:47
     * @Description: 通过id获取污染源详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "getPollutionDetailByid", method = RequestMethod.POST)
    public Object getPollutionDetailByid(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", id);
            PollutionVO detailById = pollutionService.getDetailById(paramMap);
            if (detailById == null) {
                return AuthUtil.parseJsonKeyToLower("success", detailById);
            } else {
                if (detailById.getEstablishmentdate() != null && detailById.getEstablishmentdate().length() > 19) {
                    detailById.setEstablishmentdate(detailById.getEstablishmentdate().substring(0, 19));
                }
                if (detailById.getRevokedate() != null && detailById.getRevokedate().length() > 19) {
                    detailById.setRevokedate(detailById.getRevokedate().substring(0, 19));
                }
            }
            PollutionVO jsonObject = formatDate(detailById, "yyyy-MM-dd");
            return AuthUtil.parseJsonKeyToLower("success", jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/27 14:01
     * @Description: 获取所有污染源名称信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutions", method = RequestMethod.GET)
    public Object getPollutions() {
        try {
            List<String> pollutions = pollutionService.getPollutionNames();
            return AuthUtil.returnLogJson("success", pollutions);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/29 0029 下午 7:55
     * @Description: 将实体内为日期类型的实例变量格式化日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t]
     * @throws:
     */
    public <T> T formatDate(T t, String pattern) throws Exception {
        Class<?> aClass = t.getClass();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Field[] declaredFields = aClass.getDeclaredFields();
        JsonConfig jsonConfig = JSONObjectUtil.getRegisterDefaultJsonConfig();
        JSONObject jsonObject = JSONObject.fromObject(t, jsonConfig);
        for (Field declaredField : declaredFields) {
            String name = declaredField.getName();
            declaredField.setAccessible(true);
            if ("java.util.Date".equals(declaredField.getGenericType().getTypeName())) {
                Date o = (Date) declaredField.get(t);
                if (o != null) {
                    String format1 = format.format(o);
                    jsonObject.put(name, format1);
                }
            }
        }
        return JSONObjectUtil.JsonObjectToEntity(jsonObject, t);
    }

    /**
     * @author: xsm
     * @date: 2019/6/13 0013 下午 1:34
     * @Description: 获取按标签类型分组的污染源标签信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionLabelsGroupByLabelType", method = RequestMethod.POST)
    public Object getPollutionLabelsGroupByLabelType() throws Exception {
        try {
            //获取污染源标签信息
            List<Map<String, Object>> resultlist = pollutionService.getPollutionLabelsGroupByLabelType();
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 上午 10:23
     * @Description: 自定义查询条件获取所有污染源下排口（废水直接排口、废水间接排口、雨水排口、废气有组织、废气无组织）信息以及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllPollutionAndOutPutAndPollutantInfoByParamMap", method = RequestMethod.POST)
    public Object getAllPollutionAndOutPutAndPollutantInfoByParamMap(
            @RequestJson(value = "paramsjson", required = false) Object paramsjson) {

        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (paramsjson != null) {
                paramMap = JSONObject.fromObject(paramsjson);
            }
            List<Map<String, Object>> dataList = pollutionService.getAllPollutionAndOutPutAndPollutantInfoByParamMap(paramMap);
            dataMap.put("listdata", dataList);
            dataMap.put("total", paramMap.get("total"));
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 上午 10:23
     * @Description: 自定义查询条件导出所有污染源下排口（废水直接排口、废水间接排口、雨水排口、废气有组织、废气无组织）信息以及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportAllPollutionAndOutPutAndPollutantInfoByParamMap", method = RequestMethod.POST)
    public void exportAllPollutionAndOutPutAndPollutantInfoByParamMap(
            @RequestJson(value = "paramsjson", required = false) Object paramsjson, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {

            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (paramsjson != null) {
                paramMap = JSONObject.fromObject(paramsjson);
            }
            List<Map<String, Object>> dataList = pollutionService.getAllPollutionAndOutPutAndPollutantInfoByParamMap(paramMap);
            //设置导出文件数据格式
            List<String> headers = setPollutionAndOutPutAndPollutantHeader();
            List<String> headersField = setPollutionAndOutPutAndPollutantHeaderField();
            //设置文件名称
            String fileName = "企业排口信息导出文件";
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 6:19
     * @Description: 通过监测点类型获取监测点及状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getUnorganizedMonitorAndStatusByMonitortype", method = RequestMethod.POST)
    public Object getUnorganizedMonitorAndStatusByMonitortype(@RequestJson(value = "monitorpointtype") Integer monitorpointtype) throws IOException {
        try {

            if (CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() == monitorpointtype) {
                List<Map<String, Object>> allMonitorUnstenchAndStatusInfo = outPutUnorganizedService.getAllMonitorUnstenchAndStatusInfo();//厂界恶臭
                return AuthUtil.parseJsonKeyToLower("success", allMonitorUnstenchAndStatusInfo);
            } else if (CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() == monitorpointtype) {
                List<Map<String, Object>> allMonitorUnMINIAndStatusInfo = outPutUnorganizedService.getAllMonitorUnMINIAndStatusInfo();//厂界小型站
                return AuthUtil.parseJsonKeyToLower("success", allMonitorUnMINIAndStatusInfo);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 3:43
     * @Description: 获取所有污染源名称和id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getPollutionNameAndPkid", method = RequestMethod.POST)
    public Object getPollutionNameAndPkid(@RequestJson(value = "pollutionname", required = false) String pollutionname) throws IOException {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionname", pollutionname);
            List<Map<String, Object>> pollutionNameAndPkid = pollutionService.getPollutionNameAndPkid(param);
            return AuthUtil.parseJsonKeyToLower("success", pollutionNameAndPkid);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 3:46
     * @Description: 设置污染源、排口、污染物表头信息字段值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> setPollutionAndOutPutAndPollutantHeaderField() {
        List<String> headerField = new ArrayList<>();
        headerField.add("pollutionname");
        headerField.add("outputname");
        headerField.add("monitorpointtypename");
        headerField.add("monitorpollutants");
        headerField.add("particularpollutants");
        headerField.add("dgimn");
        headerField.add("longitude");
        headerField.add("latitude");
        return headerField;
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 3:46
     * @Description: 设置污染源、排口、污染物表头信息字段描述
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> setPollutionAndOutPutAndPollutantHeader() {
        List<String> headers = new ArrayList<>();
        headers.add("污染源名称");
        headers.add("排口名称");
        headers.add("监测点类型");
        headers.add("监测污染物");
        headers.add("特征污染物");
        headers.add("数采仪MN号");
        headers.add("中心经度");
        headers.add("中心纬度");
        return headers;
    }

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 获取用户报警关联数据信息（树形数据+选中数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserEntAlarmRelationData", method = RequestMethod.POST)
    public Object getUserEntAlarmRelationData(@RequestJson(value = "userid", required = false) String userid) {
        try {
            //响应结果集合
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> typeDataList = pollutionService.getIsUseMonitorPointTypeData();
            if (typeDataList.size() > 0) {
                Integer monitorPointType;
                Map<String, Object> dataMap;
                for (Map<String, Object> typeData : typeDataList) {
                    monitorPointType = Integer.parseInt(typeData.get("code").toString());
                    if (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType) != null) {
                        dataMap = getDataMap(monitorPointType, userid);
                        dataList.add(dataMap);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private Map<String, Object> getDataMap(Integer monitorPointType, String userid) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> treeDataList = new ArrayList<>();
        List<Map<String, Object>> dataList;
        String titleName = "";
        Map<String, Object> paramMap = new HashMap<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
            case WasteWaterEnum:
                paramMap.put("outputtype", "water");
                dataList = waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap);
                treeDataList = setOutPutTreeData(dataList);
                titleName = "废水排口信息";
                break;
            case WasteGasEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                dataList = gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap);
                treeDataList = setOutPutTreeData(dataList);
                titleName = DataFormatUtil.parseProperties("gas.name") + "排口信息";
                break;
            case SmokeEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                dataList = gasOutPutInfoService.getPollutionGasOuputsAndStatus(paramMap);
                treeDataList = setOutPutTreeData(dataList);
                titleName = "烟气排口信息";
                break;
            case RainEnum:
                paramMap.put("outputtype", "rain");
                dataList = waterOutPutInfoService.getPollutionWaterOuputsAndStatus(paramMap);
                treeDataList = setOutPutTreeData(dataList);
                titleName = "雨水排口信息";
                break;
            case AirEnum:
                dataList = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "空气监测点信息";
                break;
            case EnvironmentalStinkEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "恶臭监测点信息";
                break;
            case EnvironmentalVocEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "VOC监测点信息";
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                titleName = "厂界恶臭监测点信息";
                break;
            case WaterQualityEnum:
                monitorPointType = WaterQualityEnum.getCode();
                paramMap.put("monitorPointType", monitorPointType);
                dataList = waterStationService.getOnlineWaterStationInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "水质监测点信息";
                break;
            case FactoryBoundarySmallStationEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                dataList = outPutUnorganizedService.getPollutionUnorganizedMonitorPointInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                titleName = "厂界小型站监测点信息";
                break;
            case EnvironmentalDustEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "扬尘监测点信息";
                break;
            case MicroStationEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "微型站监测点信息";
                break;

            case XKLWEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "细颗粒物监测点信息";
                break;
            case TZFEnum:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = "碳组分监测点信息";
                break;
            default:
                paramMap.put("monitorPointType", monitorPointType);
                dataList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                treeDataList = setMonitorPointTreeData(dataList);
                titleName = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType).getName();
                break;
            /*case StorageTankAreaEnum:
                dataList = StorageTankInfoService.getPollutionStorageTankMonitorPointInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                titleName = "企业贮罐监测点信息";
                break;
            case SecurityLeakageMonitor:
                paramMap.put("monitortype", monitorPointType);
                dataList = riskAreaMonitorPointService.getPollutionRiskAreaPointInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                titleName = "安全泄露监测点信息";
                break;
            case SecurityCombustibleMonitor: //可燃易爆气体监测点类型
                paramMap.put("monitortype", monitorPointType);
                titleName = "可燃易爆气体监测点信息";
                dataList = riskAreaMonitorPointService.getPollutionRiskAreaPointInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                break;
            case SecurityToxicMonitor: //有毒有害气体监测点类型
                paramMap.put("monitortype", monitorPointType);
                dataList = riskAreaMonitorPointService.getPollutionRiskAreaPointInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                titleName = "有毒有害气体监测点信息";
                break;
            case ProductionSiteEnum: //生产场所装置
                dataList = hazardSourceProductDeviceService.getHazardSourceProductDeviceInfoByParamMap(paramMap);
                treeDataList = setEntMonitorPointTreeData(dataList);
                titleName = "生产场所装置信息";
                break;*/
        }
        if (StringUtils.isBlank(titleName)) {
            titleName = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(monitorPointType).replaceAll("类型", "信息");
        }
        dataMap.put("titlename", titleName);
        dataMap.put("monitorpointtype", monitorPointType);
        dataMap.put("treedata", treeDataList);
        dataMap.put("selected", getSelectedTreeByMonitorPointType(monitorPointType, userid));
        return dataMap;

    }


    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 设置用户报警关联数据信息（选中数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "setUserEntAlarmRelationData", method = RequestMethod.POST)
    public Object setUserEntAlarmRelationData(@RequestJson(value = "userid") String userid,
                                              @RequestJson(value = "formdata", required = false) Object formdata
    ) {
        try {
            if (formdata != null && !"".equals(formdata)) {
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                pollutionService.setUserEntAlarmRelationData(userid, formdata, username);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 3:40
     * @Description: 根据监测点类型获取选中节点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Set<String> getSelectedTreeByMonitorPointType(int monitorPointType, String userid) {
        Set<String> selecteds = new HashSet<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("monitorpointtype", monitorPointType);
        List<Map<String, Object>> dataList = pollutionService.getUserEntAlarmRelationListByParamMap(paramMap);
        String type = "_output_";
        if (CommonTypeEnum.getMonitorPointTypeList().contains(monitorPointType)) {
            type = "_monitorpointname";
        }
        for (Map<String, Object> map : dataList) {
            String useriddata = map.get("userid") == null ? "" : map.get("userid").toString();
            if (map.get("monitorpointid") != null) {
                if (type.equals("_output_") && useriddata.equals(userid)) {
                    selecteds.add(map.get("monitorpointid").toString() + type + map.get("pollutionid").toString() + "," + map.get("dgimn"));   //排口
                } else if (useriddata.equals(userid)) {
                    selecteds.add(map.get("monitorpointid").toString() + type + "," + map.get("dgimn"));//监测点
                }
            }
        }
        return selecteds;
    }

    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 3:42
     * @Description: 设置企业排口树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> setOutPutTreeData(List<Map<String, Object>> pollutionOutputList) {
        List<Map<String, Object>> treedata = new ArrayList<>();
        List<Map<String, Object>> outputData;
        for (Map<String, Object> map : pollutionOutputList) {
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("code", map.get("pk_pollutionid"));
            mapTemp.put("label", map.get("shortername"));
            outputData = (List<Map<String, Object>>) map.get("outputdata");
            List<Map<String, Object>> children = new ArrayList<>();
            for (Map<String, Object> output : outputData) {
                Map<String, Object> mapOutPutTemp = new HashMap<>();
                mapOutPutTemp.put("code", output.get("pk_id") + "_output_" + map.get("pk_pollutionid") + "," + output.get("dgimn"));
                mapOutPutTemp.put("label", output.get("outputname"));
                children.add(mapOutPutTemp);
            }
            mapTemp.put("children", children);
            treedata.add(mapTemp);

        }
        return treedata;
    }


    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 3:42
     * @Description: 设置企业监测点树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> setEntMonitorPointTreeData(List<Map<String, Object>> pollutionOutputList) {
        List<Map<String, Object>> treedata = new ArrayList<>();
        List<Map<String, Object>> outputData;
        for (Map<String, Object> map : pollutionOutputList) {
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("code", map.get("pk_pollutionid"));
            mapTemp.put("label", map.get("shortername"));
            outputData = (List<Map<String, Object>>) map.get("outputdata");
            List<Map<String, Object>> children = new ArrayList<>();
            for (Map<String, Object> output : outputData) {
                Map<String, Object> mapOutPutTemp = new HashMap<>();
                mapOutPutTemp.put("code", output.get("pk_id") + "_output_" + map.get("pk_pollutionid") + "," + output.get("dgimn"));
                mapOutPutTemp.put("label", output.get("monitorpointname"));
                children.add(mapOutPutTemp);
            }
            mapTemp.put("children", children);
            treedata.add(mapTemp);

        }
        return treedata;
    }

    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 3:42
     * @Description: 设置监测点树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> setMonitorPointTreeData(List<Map<String, Object>> pollutionOutputList) {
        List<Map<String, Object>> treedata = new ArrayList<>();
        for (Map<String, Object> map : pollutionOutputList) {
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("code", map.get("pk_id") + "_monitorpointname" + "," + map.get("dgimn"));
            mapTemp.put("label", map.get("monitorpointname"));
            treedata.add(mapTemp);

        }
        return treedata;
    }

    /**
     * @author: xsm
     * @date: 2019/8/22 0022 上午 9:51
     * @Description: 根据污染源id获取关联该污染源的所有监测点信息
     * @updateUser:xsm
     * @updateDate:2020/12/15 0015 下午 13:32
     * @updateDescription:添加监测类型条件
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllMonitorPointInfoByPollutionId", method = RequestMethod.POST)
    public Object getAllMonitorPointInfoByPollutionId(@RequestJson(value = "id") String pollutionid,
                                                      @RequestJson(value = "monitortypes", required = false) List<Integer> monitortypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtils.isNotBlank(pollutionid)) {
            paramMap.put("pollutionids", Arrays.asList(pollutionid));
        }
        if (monitortypes == null || monitortypes.size() == 0) {
            monitortypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
        }
        for (Integer type : monitortypes) {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    dataList = waterOutPutInfoService.getWaterOuPutAndStatusByParamMap(paramMap);
                    break;
                case WasteGasEnum:
                case SmokeEnum:
                    paramMap.put("monitorpointtype", type);
                    dataList = gasOutPutInfoService.getGasOutPutAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputtype", "rain");
                    dataList = waterOutPutInfoService.getWaterOuPutAndStatusByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    dataList = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    dataList = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
               /* case FactoryBoundaryDustEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryDustEnum.getCode());
                    dataList = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;*/
            }
            if (dataList.size() > 0) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (Map<String, Object> map : dataList) {
                    Map<String, Object> resultMap = new HashMap<>();
                    if (CommonTypeEnum.getOutPutTypeList().contains(type)) {
                        resultMap.put("monitorpointname", map.get("outputname"));
                    } else {
                        resultMap.put("monitorpointname", map.get("monitorpointname"));
                    }
                    resultMap.put("monitorpointid", map.get("pk_id"));
                    resultMap.put("pollutionid", map.get("pk_pollutionid"));
                    resultMap.put("pollutionname", map.get("pollutionname"));
                    resultMap.put("dgimn", map.get("dgimn"));
                    resultMap.put("onlinestatus", map.get("onlinestatus"));
                    resultMap.put("monitorpointtype", type);
                    resultMap.put("Longitude", map.get("Longitude"));
                    resultMap.put("Latitude", map.get("Latitude"));
                    resultList.add(resultMap);
                }
                resultmap.put("monitorpointtype", type);
                resultmap.put("monitorpointdata", resultList);
                result.add(resultmap);
            }
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/23 0023 上午 9:44
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getPollutionByParamsMap", method = RequestMethod.POST)
    public Object getPollutionByParamsMap(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }

            List<Map<String, Object>> pollutionByParamsMap = pollutionService.getPollutionByParamsMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pollutionByParamsMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", pollutionByParamsMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: liyc
     * @date: 2019/11/11 0011 18:41
     * @Description: 档案首页  通过污染源的id获取污染源的基本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     **/
    @RequestMapping(value = "getPollutionBasicInfoByPollutionId", method = RequestMethod.POST)
    public Object getPollutionBasicInfoByPollutionId(@RequestJson(value = "pollutionid", required = true) String pollutionid) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> dataList = pollutionService.getPollutionBasicInfoByPollutionId(pollutionid);
            requestMap.put("tablelist", dataList);
            return AuthUtil.parseJsonKeyToLower("success", requestMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 4:41
     * @Description: 通过自定义参数获取所有企业下的废水，废气，雨水，厂界恶臭，厂界小型站因子
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "getAllPollutionPollutantInfoByParamMap", method = RequestMethod.POST)
    public Object getAllPollutionPollutantInfoByParamMap(@RequestJson(value = "pollutanttype", required = true) Integer pollutanttype) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("pollutanttype", pollutanttype);
            List<Map<String, Object>> allPollutionPollutantInfoByParamMap = pollutionService.getAllPollutionPollutantInfoByParamMap(requestMap);
            return AuthUtil.parseJsonKeyToLower("success", allPollutionPollutantInfoByParamMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 5:06
     * @Description: 通过污染因子获取企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutantcodes]
     * @throws:
     */
    @RequestMapping(value = "getPollutionInfoByPollutantcodes", method = RequestMethod.POST)
    public Object getPollutionInfoByPollutantcodes(@RequestJson(value = "fkpollutantcodes", required = false) Object fkpollutantcodes) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("fkpollutantcodes", fkpollutantcodes);
            List<Map<String, Object>> allPollutionPollutantInfoByParamMap = pollutionService.getPollutionInfoByPollutantcodes(requestMap);
            return AuthUtil.parseJsonKeyToLower("success", allPollutionPollutantInfoByParamMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/1/7 0007 上午 10:28
     * @Description: 通过自定义参数获取企业最高风险等级
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getMaxRiskLevelInfoByParamMap", method = RequestMethod.POST)
    public Object getMaxRiskLevelInfoByParamMap(@RequestJson(value = "fkpollutionid") String fkpollutionid) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("fkpollutionid", fkpollutionid);
            Map<String, Object> maxRiskLevelInfoByParamMap = pollutionService.getMaxRiskLevelInfoByParamMap(requestMap);
            return AuthUtil.parseJsonKeyToLower("success", maxRiskLevelInfoByParamMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/2/26 0026 下午 3:39
     * @Description: 通过监测点类型获取企业及点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getAllOutputInfoByParamMap", method = RequestMethod.POST)
    public Object getAllOutputInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
            Map<String, Object> paramMap = (Map) paramsjson;
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("onlydataauthor", "1");
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            Map<String, Long> collect = outPutInfosByParamMap.stream().filter(m -> m.get("Status") != null).map(m -> m.get("Status").toString()).collect(Collectors.groupingBy(m -> m, Collectors.counting()));


            Map<String, List<Map<String, Object>>> groupdata = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));

            for (String monitortype : groupdata.keySet()) {
                List<Map<String, Object>> list = groupdata.get(monitortype);

                if (monitortype.equals(AirEnum.getCode() + "")) {
                    airMonitorStationService.setAirStationAqi(list);
                }
                if (monitortype.equals(WaterQualityEnum.getCode() + "")) {
                    onlineWaterQualityService.setWaterQaulity(list);
                }

                List<String> outputid = list.stream().filter(m -> m.get("outputid") != null).map(m -> m.get("outputid").toString()).collect(Collectors.toList());
                paramMap.put("monitorpointids", outputid);
                paramMap.put("monitorpointtype", monitortype);
                List<Map<String, Object>> videoCameraInfoByParamMap = videoCameraService.getVideoCameraInfoByParamMap(paramMap);
                for (Map<String, Object> map : list) {
                    List<Map<String, Object>> cameras = new ArrayList<>();
                    for (Map<String, Object> stringObjectMap : videoCameraInfoByParamMap) {
                        if (map.get("outputid") != null && stringObjectMap.get("monitorpointid") != null && map.get("outputid").toString().equals(stringObjectMap.get("monitorpointid").toString())) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("RTSP", stringObjectMap.get("rtsp"));
                            data.put("PK_VedioCameraID", stringObjectMap.get("PK_VedioCameraID"));
                            cameras.add(data);
                        }
                    }
                    map.put("cameras", cameras);
                }
            }


            CommonTypeEnum.OnlineStatusEnum[] values = CommonTypeEnum.OnlineStatusEnum.values();

            List<Map<String, Object>> countlist = new ArrayList<>();
            for (CommonTypeEnum.OnlineStatusEnum value : values) {
                Map<String, Object> data = new HashMap<>();
                String code = value.getCode();
                String name = value.getName();
                if (collect.containsKey(code)) {
                    data.put("code", code);
                    data.put("name", name);
                    data.put("count", collect.get(code));
                } else if (!collect.containsKey(code)) {
                    data.put("code", code);
                    data.put("name", name);
                    data.put("count", 0);
                }
                countlist.add(data);
            }
            resultMap.put("outputinfo", outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).sorted(Comparator.comparing(m -> m.get("FK_MonitorPointTypeCode").toString())).collect(Collectors.toList()));
            resultMap.put("countlist", countlist);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/01/16 9:13
     * @Description: 根据监测点类型和标记字段获取相关列表数据(废水 、 废气 、 烟气 、 雨水 、 水质)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointListDataByMonitorPointTypeAndFlag", method = RequestMethod.POST)
    public Object getMonitorPointListDataByMonitorPointTypeAndFlag(@RequestJson(value = "monitorpointtype") Integer monitorPointType,
                                                                   @RequestJson(value = "devicestatus", required = false) String devicestatus,
                                                                   @RequestJson(value = "flag") String flag,
                                                                   @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = getCodeByInt(monitorPointType);
            if (monitorPointTypeEnum == null) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> outputs = new ArrayList<>();
            paramMap.put("monitorpointtype", monitorPointType);
            //添加数据权限
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            if ("outputstop".equals(flag)) {//排口停产
                if (monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum
                        || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum
                        || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum
                        || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.RainEnum) {
                    paramMap.put("outputstatus", "stop");
                }
            } else if ("outputstatus".equals(flag)) {//排口状态  全部、在线、离线、超标、异常
                if (!"".equals(devicestatus)) {
                    if (monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum
                            || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum
                            || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum
                            || monitorPointTypeEnum == CommonTypeEnum.MonitorPointTypeEnum.RainEnum) {
                        paramMap.put("outputstatus", "start");
                    }
                }
                paramMap.put("devicestatus", devicestatus);
            }
            outputs = onlineService.getAllPollutionOutputDgimnInfoByParam(paramMap);
            if (outputs.size() > 0) {
                if (pagesize != null && pagenum != null) {
                    List<Map<String, Object>> dataList = getPageData(outputs, pagenum, pagesize);
                    resultMap.put("total", outputs.size());
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", outputs);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/11/30 0030 下午 2:07
     * @Description: 获取恶臭
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkMonitorPointListData", method = RequestMethod.POST)
    public Object getStinkMonitorPointListData(
            @RequestJson(value = "devicestatus", required = false) String devicestatus,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.StinkEnum.getCode());
            paramMap.put("devicestatus", devicestatus);
            List<Map<String, Object>> outputs = onlineService.getAllPollutionOutputDgimnInfoByParam(paramMap);
            if (outputs.size() > 0) {
                if (pagesize != null && pagenum != null) {
                    List<Map<String, Object>> dataList = getPageData(outputs, pagenum, pagesize);
                    resultMap.put("total", outputs.size());
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", outputs);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/01/16 0016 上午 10:38
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
     * @date: 2020/3/3 0003 上午 9:44
     * @Description: 根据企业ID获取企业点位信息（点位状态，传输有效率，监测污染物）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointDataByPollutionId", method = RequestMethod.POST)
    public Object getMonitorPointDataByPollutionId(
            @RequestJson(value = "pollutionid") String pollutionId) {
        try {
            Map<String, Object> resultMap = pollutionService.getMonitorPointDataByPollutionId(pollutionId);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/24 0024 上午 10:36
     * @Description: 通过用户id获取污染源详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPollutionDetailByUserId", method = RequestMethod.POST)
    public Object getPollutionDetailByUserId(@RequestJson(value = "userid", required = false) String userid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (userid == null) {

                userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            }
            paramMap.put("userid", userid);
            PollutionVO detailById = pollutionService.getPollutionDetailByUserId(paramMap);
            if (detailById != null) {
                PollutionVO jsonObject = formatDate(detailById, "yyyy-MM-dd");
                return AuthUtil.parseJsonKeyToLower("success", jsonObject);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", detailById);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/24 0024 下午 1:11
     * @Description: 获取企业信息提醒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPollutionInfoRemindData", method = RequestMethod.POST)
    public Object getPollutionInfoRemindData() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();

            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            Map<String, Object> result = pollutionService.getPollutionInfoRemindData(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/24 0024 下午 2:41
     * @Description: 统计企业各子级菜单数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countPollutionChildMenuDataNum", method = RequestMethod.POST)
    public Object countPollutionChildMenuDataNum() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();

            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            Map<String, Object> result = pollutionService.countPollutionChildMenuDataNum(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/18 0018 上午 10:13
     * @Description: 统计某个时间范围内监测点传输率、有效率、传输有效率（100%,75%-100%,<75%）占比个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitorpointcategory:1：敏感点，2：传输点
     * datamark:effectiverate(有效率)、transmissionrate（传输率）、transmissioneffectiverate（传输有效率）
     * starttime:开始时间 endtime:结束时间
     * customname:自定义名称（企业名称，点位名称）
     * @return:
     */
    @RequestMapping(value = "countMonitorPointTransmissionEffectiveRateNum", method = RequestMethod.POST)
    public Object countMonitorPointTransmissionEffectiveRateNum(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "datamark") String datamark,
            @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "customname", required = false) String customname

    ) {
        try {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(monitorpointcategory)) {
                paramMap.put("monitorpointcategory", monitorpointcategory);
            }
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("datamark", datamark);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("customname", customname);

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            resultMap = pollutionService.countMonitorPointTransmissionEffectiveRateNum(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/19 0019 下午 4:31
     * @Description: 修改污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark]
     * @throws:
     */
    @RequestMapping(value = "updatePollutantPartInfo", method = RequestMethod.POST)
    public Object updatePollutantPartInfo(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            PollutionVO pollutionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PollutionVO());
            JSONArray pollutionlabels = new JSONArray();
            if (jsonObject.get("labelcode") != null) {
                Object labels = jsonObject.get("labelcode");
                Object collect = JSONArray.fromObject(labels).stream().filter(m -> !"null".equals(m.toString())).collect(Collectors.toList());
                pollutionlabels = JSONArray.fromObject(collect);
            }
            pollutionService.updatePollutantPartInfo(pollutionVO, pollutionlabels);
            pollutionService.updateOutPutCode(pollutionVO.getPkpollutionid());
            return AuthUtil.parseJsonKeyToLower("success", null);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getSafePollutionList", method = RequestMethod.POST)
    public Object getSafePollutionList(
            @RequestJson(value = "pagenum") Integer pagenum,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pollutionname", required = false) String pollutionname,
            @RequestJson(value = "ishavemajorhazards", required = false) Integer ishavemajorhazards,
            @RequestJson(value = "pollutionclasslist", required = false) List<String> pollutionclasslist,
            @RequestJson(value = "entscalelist", required = false) List<String> entscalelist

    ) throws Exception {
        try {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            if (StringUtils.isNotBlank(pollutionname)) {
                paramMap.put("pollutionname", pollutionname);
            }
            if (ishavemajorhazards != null) {
                paramMap.put("ishavemajorhazards", ishavemajorhazards);
            }
            if (pollutionclasslist != null && pollutionclasslist.size() > 0) {
                paramMap.put("pollutionclasslist", pollutionclasslist);
            }
            if (entscalelist != null && entscalelist.size() > 0) {
                paramMap.put("entscalelist", entscalelist);
            }

            PageInfo<Map<String, Object>> pageInfo = pollutionService.getSafePollutionListByParam(paramMap);
            List<Map<String, Object>> dataList = pageInfo.getList();
           /* //获取厂区图片数据
            Map<String, String> idAndFileId = new HashMap<>();
            String pollutionId;
            String fileId;
            List<String> fileIds = new ArrayList<>();
            for (Map<String, Object> dataMap : dataList) {
                if (dataMap.get("fileid") != null) {
                    pollutionId = dataMap.get("pollutionid").toString();
                    fileId = dataMap.get("fileid").toString();
                    fileIds.add(fileId);
                    idAndFileId.put(pollutionId, fileId);
                }
            }
            Map<String, List<Object>> idAndData = new HashMap<>();
            if (fileIds.size() > 0) {
                paramMap.clear();
                String businessType = "1";
                paramMap.put("fileflags", fileIds);
                paramMap.put("businesstype", "1");
                paramMap.put("businessfiletype", "201");
                List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfosByParam(paramMap);
                if (fileInfoVOS.size() > 0) {
                    Map<String, List<String>> fileIdAndPaths = new HashMap<>();
                    List<String> paths;
                    List<ObjectId> pathList = new ArrayList<>();
                    for (FileInfoVO fileInfoVO : fileInfoVOS) {
                        if (fileInfoVO.getFilepath() != null && imgList.contains(fileInfoVO.getFileextname().toUpperCase())) {
                            fileId = fileInfoVO.getFileflag();
                            if (fileIdAndPaths.containsKey(fileId)) {
                                paths = fileIdAndPaths.get(fileId);
                            } else {
                                paths = new ArrayList<>();
                            }
                            paths.add(fileInfoVO.getFilepath());
                            fileIdAndPaths.put(fileId, paths);
                            pathList.add(new ObjectId(fileInfoVO.getFilepath()));
                        }
                    }
                    if (pathList.size() > 0) {
                        Map<String, Object> pathAndData = fileController.getImgIdAndData(pathList, businessType);
                        if (pathAndData.size() > 0) {
                            List<Object> datas;
                            for (String id : idAndFileId.keySet()) {
                                paths = fileIdAndPaths.get(idAndFileId.get(id));
                                if (paths != null) {
                                    for (String path : paths) {
                                        if (pathAndData.get(path) != null) {
                                            if (idAndData.containsKey(id)) {
                                                datas = idAndData.get(id);
                                            } else {
                                                datas = new ArrayList<>();
                                            }
                                            datas.add(pathAndData.get(path));
                                            idAndData.put(id, datas);
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
            for (Map<String, Object> dataMap : dataList) {
                pollutionId = dataMap.get("pollutionid").toString();
                if (idAndData.containsKey(pollutionId)) {
                    dataMap.put("imglist", idAndData.get(pollutionId));
                } else {
                    dataMap.put("imglist", Arrays.asList());
                }
            }*/
            resultMap.put("datalist", dataList);
            resultMap.put("total", pageInfo.getTotal());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/02/01 09:46
     * @Description: 根据自定义参数统计某类型所有点位某污染的小时浓度对比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-MM-dd HH
     * @return:
     */
    @RequestMapping(value = "getMonitorPointHourConcentrationDataByParam", method = RequestMethod.POST)
    public Object getMonitorPointHourConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                              @RequestJson(value = "pollutantcode") String pollutantcode,
                                                              @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes

    ) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mn_name = new HashMap<>();
            List<Map<String, Object>> allpoints = new ArrayList<>();
            allpoints = getAllPointDataByTypes(monitorpointtypes);
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (allpoints.size() > 0) {
                for (Map<String, Object> map : allpoints) {
                    String mn = map.get("DGIMN") != null ? map.get("DGIMN").toString() : (map.get("dgimn") != null ? map.get("dgimn").toString() : "");
                    if (!"".equals(mn) && dgimns.contains(mn)) {
                        mns.add(mn);
                        String outputname = map.get("OutputName") != null ? map.get("OutputName").toString() : (map.get("outputname") != null ? map.get("outputname").toString() : "");
                        if (!"".equals(outputname)) {
                            mn_name.put(mn, outputname);
                        }
                    }
                }
                List<Map<String, Object>> listdata = new ArrayList<>();
                Map<String, Object> paramMap = new HashMap<>();
                Map<String, Double> mnandstand = new HashMap<>();
                for (Integer type : monitorpointtypes) {
                    paramMap.put("monitorpointtype", type);
                    paramMap.put("dgimns", mns);
                    paramMap.put("pollutantcode", pollutantcode);
                    listdata.addAll(pollutantService.getPollutantStandardValueDataByParam(paramMap));
                }
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("DGIMN") != null && map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                            mnandstand.put(map.get("DGIMN").toString(), Double.valueOf(map.get("StandardMaxValue").toString()));
                        }
                    }
                }
                result = pollutionService.getMonitorPointHourConcentrationDataByParam(mns, mn_name, monitortime, pollutantcode, mnandstand);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/02/01 09:46
     * @Description: 导出——根据自定义参数统计某类型所有点位某污染的小时浓度对比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-MM-dd HH
     * @return:
     */
    @RequestMapping(value = "exportMonitorPointHourConcentrationDataByParam", method = RequestMethod.POST)
    public void exportMonitorPointHourConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                               @RequestJson(value = "pollutantcode") String pollutantcode,
                                                               @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> result = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mn_name = new HashMap<>();
            List<Map<String, Object>> allpoints = new ArrayList<>();
            allpoints = getAllPointDataByTypes(monitorpointtypes);
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (allpoints.size() > 0) {
                for (Map<String, Object> map : allpoints) {
                    String mn = map.get("DGIMN") != null ? map.get("DGIMN").toString() : (map.get("dgimn") != null ? map.get("dgimn").toString() : "");
                    if (!"".equals(mn) && dgimns.contains(mn)) {
                        mns.add(mn);
                        String outputname = map.get("OutputName") != null ? map.get("OutputName").toString() : (map.get("outputname") != null ? map.get("outputname").toString() : "");
                        if (!"".equals(outputname)) {
                            mn_name.put(mn, outputname);
                        }
                    }
                }
                /*List<Map<String, Object>> listdata = new ArrayList<>();
                Map<String, Object> paramMap = new HashMap<>();
                Map<String, Double> mnandstand = new HashMap<>();
                for (Integer type:monitorpointtypes){
                    paramMap.put("monitorpointtype",type);
                    paramMap.put("dgimns", mns);
                    paramMap.put("pollutantcode", pollutantcode);
                    listdata.addAll(pollutantService.getPollutantStandardValueDataByParam(paramMap));
                }
                if (listdata!=null&&listdata.size()>0){
                    for (Map<String, Object>  map:listdata){
                        if (map.get("DGIMN")!=null&&map.get("StandardMaxValue")!=null&&!"".equals(map.get("StandardMaxValue").toString())){
                            mnandstand.put(map.get("DGIMN").toString(),Double.valueOf(map.get("StandardMaxValue").toString()));
                        }
                    }
                }*/
                result = pollutionService.getMonitorPointHourConcentrationDataByParam(mns, mn_name, monitortime, pollutantcode, new HashMap<>());
            }
            //获取表头数据
            List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) result.get("tabletitledata");
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) result.get("datalist");
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    if (map.get("hbfd_value") != null && !"".equals(map.get("hbfd_value").toString())) {
                        map.put("hbfd_value", map.get("hbfd_value") + "%");
                    }
                }
            }
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "监测点统计分析导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, listdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/2/26 0026 下午 4:20
     * @Description: 通过企业id获取企业资料完善程度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "countComplateData", method = RequestMethod.POST)
    public Object countComplateData(@RequestJson(value = "fkpollutionid") String fkpollutionid) throws Exception {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            dataMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> maps = pollutionService.countComplateData(dataMap);
            long count = maps.stream().filter(m -> m.get("count") != null && m.get("iscalculat") != null && "yes".equals(m.get("iscalculat").toString()) && Integer.valueOf(m.get("count").toString()) > 0).count();

            //计算资料完善程度
            String format = decimalFormat.format(Double.valueOf(count) / Double.valueOf(maps.size()) * 100);

            dataMap.clear();
            dataMap.put("complatedatarate", format);
            dataMap.put("datalist", maps);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/29 0029 上午 11:34
     * @Description: 通过自定义条件获取企业信息(企业区域信息)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getPollutionInfoByParams", method = RequestMethod.POST)
    public Object getPollutionInfoByParams(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                           @RequestJson(value = "enttype", required = false) String enttype
    ) throws Exception {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            List<Map<String, Object>> maps;
            if (StringUtils.isNotBlank(enttype)) {
                List<String> pollutionIds = getPollutionIds(enttype);
                if (pollutionIds.size() == 0) {
                    pollutionIds = Arrays.asList("NO");
                }
                dataMap.put("pollutionids", pollutionIds);
                maps = pollutionService.getPollutionInfoByParamMaps(dataMap);
            } else {
                dataMap.put("fkpollutionid", fkpollutionid);
                maps = pollutionService.getPollutionInfoByParamMaps(dataMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> getPollutionIds(String enttype) {
        List<String> ids = new ArrayList<>();
        String labelcode;
        String nowDay;
        Map<String, Object> paramMap;
        switch (enttype) {
            case "totalent":
                ids = pollutionService.getTotalIdsByParam();
                break;
            case "stopnum":
                nowDay = DataFormatUtil.getDateYMDHMS(new Date());
                ids = pollutionService.getStopIdsTotal(nowDay);
                break;
            case "nostopnum":
                nowDay = DataFormatUtil.getDateYMDHMS(new Date());
                ids = pollutionService.getStopIdsTotal(nowDay);
                List<String> Aids = pollutionService.getTotalIdsByParam();
                Aids.removeAll(ids);
                ids = Aids;
                break;
            case "waterent":

                labelcode = DataFormatUtil.parseProperties("water.class");
                paramMap = new HashMap<>();
                paramMap.put("class", labelcode);
                ids = pollutionService.getTotalIdsByClass(paramMap);


                break;
            case "gasent":
                labelcode = DataFormatUtil.parseProperties("gas.class");
                paramMap = new HashMap<>();
                paramMap.put("class", labelcode);
                ids = pollutionService.getTotalIdsByClass(paramMap);


                break;
            case "wfent":
                labelcode = DataFormatUtil.parseProperties("wf.label");
                ids = pollutionService.getTotalIdsByLabel(labelcode);


                break;
            case "zmqd"://正面清单

                labelcode = DataFormatUtil.parseProperties("zmqd.label");
                ids = pollutionService.getTotalIdsByLabel(labelcode);
                break;
            case "zxjk"://在线监控
                labelcode = DataFormatUtil.parseProperties("zxjk.label");
                ids = pollutionService.getTotalIdsByLabel(labelcode);
                break;
            case "pwent"://排污许可
                ids = pollutionService.getAllPWIds();
                break;
        }
        return ids;
    }


    /**
     * @author: chengzq
     * @date: 2021/3/29 0029 上午 11:35
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getUserEntInfoByParamMap", method = RequestMethod.POST)
    public Object getUserEntInfoByParamMap(@RequestJson(value = "pagenum", required = false) Integer pagenum,
                                           @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                           @RequestJson(value = "pollutionname", required = false) String pollutionname) throws Exception {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            dataMap.put("pollutionname", pollutionname);
            List<Map<String, Object>> maps = pollutionService.getUserEntInfoByParamMap(dataMap);

            int total = maps.size();
            for (Map<String, Object> map : maps) {
                List<Map<String, Object>> entusers = map.get("entusers") == null ? new ArrayList<>() : (List<Map<String, Object>>) map.get("entusers");

                List<Map<String, Object>> minlist = entusers.stream().filter(m -> m.get("monitorpointtypes") != null).map(m -> (List<Map<String, Object>>) m.get("monitorpointtypes")).map(m -> m.stream().filter(n -> n.get("selected") != null)
                        .flatMap(n -> ((List<Map<String, Object>>) n.get("selected")).stream()).collect(Collectors.toList())).min(Comparator.comparing(m -> m.size())).orElse(new ArrayList<>());
//                List<Map<String, Object>> minlist = entusers.stream().filter(m -> m.get("monitorpointtypes") != null).map(m -> ((List<Map<String, Object>>) m.get("monitorpointtypes")).stream()).min(Comparator.comparing(m -> m.size())).orElse(new ArrayList<>());
                Set<String> userids = entusers.stream().filter(m -> m.get("fkuserid") != null).map(m -> m.get("fkuserid").toString()).collect(Collectors.toSet());
                Set<Map<String, Object>> data = new HashSet<>();
                for (Map<String, Object> stringObjectMap : entusers) {
                    List<Map<String, Object>> monitorpointtypes = stringObjectMap.get("monitorpointtypes") == null ? new ArrayList<>() : (List<Map<String, Object>>) stringObjectMap.get("monitorpointtypes");
                    List<Map<String, Object>> outputs = monitorpointtypes.stream().filter(m -> m.get("selected") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("selected")).stream()).collect(Collectors.toList());
                    minlist.retainAll(outputs);
                    for (Map<String, Object> output : monitorpointtypes) {
                        Map<String, Object> datamap = new HashMap<>();
                        String monitorpointtype = output.get("monitorpointtype") == null ? "" : output.get("monitorpointtype").toString();
                        List<Map<String, Object>> selected = output.get("selected") == null ? new ArrayList<>() : (List<Map<String, Object>>) output.get("selected");
                        List<String> outputids = selected.stream().filter(m -> m.get("selectid") != null).map(m -> m.get("selectid").toString()).collect(Collectors.toList());
                        datamap.put("monitorpointtype", monitorpointtype);
                        datamap.put("selected", outputids);
                        data.add(datamap);
                    }
                }
                if (minlist.size() == 0) {
                    map.put("outputnames", "");
                } else {
                    String outputnames = minlist.stream().filter(m -> m.get("outputname") != null).map(m -> m.get("outputname").toString()).collect(Collectors.joining("、"));
                    map.put("outputnames", outputnames);
                }
                map.remove("entusers");
                map.put("userids", userids);
                map.put("monitiorpints", data);
            }

            if (pagenum != null && pagesize != null) {
                maps = maps.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total", total);
            resultMap.put("datalist", maps);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private List<Map<String, Object>> getAllPointDataByTypes(List<Integer> monitorpointtypes) {
        List<Map<String, Object>> allpoints = new ArrayList<>();
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        Map<String, Object> param = new HashMap<>();
        if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
            for (Integer i : monitorpointtypes) {
                if (notothertypes.contains(i)) {
                    switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(i)) {
                        case WasteWaterEnum: //废水
                            allpoints.addAll(waterOutPutInfoService.getAllMonitorWaterOutPutAndStatusInfo());//废水
                            break;
                        case WasteGasEnum: //废气
                            allpoints.addAll(gasOutPutInfoService.getAllMonitorGasOutPutAndStatusInfo());//废气
                            break;
                        case SmokeEnum: //烟气
                            allpoints.addAll(gasOutPutInfoService.getAllMonitorSmokeOutPutAndStatusInfo());//废气
                            break;
                        case RainEnum: //雨水
                            allpoints.addAll(waterOutPutInfoService.getAllMonitorRainOutPutAndStatusInfo());//雨水
                            break;
                        case AirEnum: //空气
                            allpoints.addAll(airMonitorStationService.getAllMonitorAirStationAndStatusInfo());//空气
                            break;
                        case FactoryBoundaryStinkEnum: //厂界恶臭
                            allpoints.addAll(outPutUnorganizedService.getAllMonitorUnstenchAndStatusInfo());//厂界恶臭
                            break;
                        case FactoryBoundarySmallStationEnum: //厂界小型站
                            allpoints.addAll(outPutUnorganizedService.getAllMonitorUnMINIAndStatusInfo());//厂界小型站
                            break;
                        case WaterQualityEnum: //水质
                            allpoints.addAll(waterStationService.getAllWaterStationAndStatusInfo());//水质
                            break;
                        case meteoEnum: //气象站
                            allpoints.addAll(otherMonitorPointService.getAllMonitorEnvironmentalMeteoAndStatusInfo());//气象
                            break;
                    }
                } else {//其它监测点类型表 类型
                    param.put("monitorpointtype", i);
                    allpoints.addAll(otherMonitorPointService.getAllMonitorPointAndStatusInfo(param));
                }
            }

        }
        return allpoints;
    }

    /**
     * @author: xsm
     * @date: 2021/6/07 0007 上午 10:35
     * @Description: 获取企业监测类型下拉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionPointTypeSelectData", method = RequestMethod.POST)
    public Object getPollutionPointTypeSelectData() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Integer> hb_types = CommonTypeEnum.getPollutionMonitorPointTypeList();
            List<Map<String, Object>> alltypes = pollutionService.getAllIsUsedMonitorPointTypes();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            if (alltypes != null && alltypes.size() > 0) {
                String type = "";
                for (Map<String, Object> map : alltypes) {
                    type = map.get("value") != null ? map.get("value").toString() : "";
                    if (categorys != null && categorys.size() > 0) {
                        if (categorys.contains("1")) {//环保
                            for (Integer hbtype : hb_types) {
                                if (!"".equals(type) && type.equals(hbtype + "")) {
                                    resultList.add(map);
                                }
                            }
                        }
                    }
                }
            }
            Map<String, Object> obj = new HashMap<>();
            obj.put("labelname", "其它");
            obj.put("value", "other");
            resultList.add(obj);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 下午 15:18
     * @Description: 按行业类型分组统计企业个数及占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countEntRateDataGroupByIndustryType", method = RequestMethod.POST)
    public Object countEntRateDataGroupByIndustryType() throws Exception {
        try {
            List<Map<String, Object>> maplist = pollutionService.countEntRateDataGroupByIndustryType();
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID获取该企业所监测的监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEntMonitorPointTypeByEntID", method = RequestMethod.POST)
    public Object getEntMonitorPointTypeByEntID(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            //获取环保 企业关联类型
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> param = new HashMap<>();
            param.put("pollutiontypes", pollutiontypes);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> maps = pollutionService.getEntMonitorPointTypeByEntID(param);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID和监测类型获取该类型企业点位监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid、monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getEntMonitorPointPollutantDataByParam", method = RequestMethod.POST)
    public Object getEntMonitorPointPollutantDataByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                         @RequestJson(value = "monitorpointtype") String monitorpointtype) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtype", monitorpointtype);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> maps = pollutionService.getEntMonitorPointPollutantDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID和监测类型获取该类型企业点位监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid、monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getPollutant24HourDataByParam", method = RequestMethod.POST)
    public Object getPollutant24HourDataByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                @RequestJson(value = "monitorpointtype") String monitorpointtype,
                                                @RequestJson(value = "starttime") String starttime,
                                                @RequestJson(value = "endtime") String endtime,
                                                @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtype", monitorpointtype);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> pointlist = pollutionService.getEntPointMNDataByParam(param);
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            if (pointlist != null && pointlist.size() > 0) {
                for (Map<String, Object> map : pointlist) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                        mnandname.put(map.get("DGIMN").toString(), map.get("monitorpointname"));
                    }
                }
            }
            param.put("mnandname", mnandname);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("mns", mns);
            param.put("pollutantcode", pollutantcode);
            Map<String, Object> resultmap = onlineMonitorService.getManyPonitPollutantDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/09/13 0013 上午 08:57
     * @Description: 根据企业ID获取企业下排放口/监测点及状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionPointInfoTreeByID", method = RequestMethod.POST)
    public Object getPollutionPointInfoTreeByID(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            //获取环保 企业关联类型
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> param = new HashMap<>();
            param.put("pollutiontypes", pollutiontypes);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            //根据用户数据权限 获取企业下的监测类型
            List<Map<String, Object>> maps = pollutionService.getEntMonitorPointTypeByEntID(param);
            if (maps != null && maps.size() > 0) {
                List<String> types = maps.stream().filter(m -> m.get("FK_MonitorPointType") != null).map(m -> m.get("FK_MonitorPointType").toString()).collect(Collectors.toList());
                param.put("monitorpointtypes", types);
                //获取停产排口
                //List<Map<String, Object>> stoplist = stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(param);
                List<Map<String, Object>> dataList = pollutionService.getEntPointInfoByEntIDAndTypes(param);
                if (dataList != null && dataList.size() > 0) {
                    Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
                    listmap = dataList.stream().collect(Collectors.groupingBy(m -> m.get("type").toString()));
                    for (Map<String, Object> typemap : maps) {
                        if (listmap.get(typemap.get("FK_MonitorPointType").toString()) != null) {
                            typemap.put("child", listmap.get(typemap.get("FK_MonitorPointType").toString()));
                        } else {
                            typemap.put("child", new ArrayList<>());
                        }
                    }
                }
            }

               /* if (dataList != null && dataList.size() > 0) {
                    if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//为废气、废水排口时

                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> obj : dataList) {
                                List<Map<String, Object>> outputlist = (List<Map<String, Object>>) obj.get("outputdata");
                                for (Map<String, Object> map : outputlist) {
                                    for (Map<String, Object> stopmap : stoplist) {
                                        if ((map.get("pk_id").toString()).equals(stopmap.get("FK_Outputid").toString())) {
                                            map.put("outputname", map.get("outputname") + "【停产】");
                                        }
                                    }
                                }
                            }
                        }
                    } else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                        paramMap.put("monitorpointtype", monitorpointtype);
                        List<Map<String, Object>> stoplist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> obj : dataList) {
                                List<Map<String, Object>> outputlist = (List<Map<String, Object>>) obj.get("outputdata");
                                for (Map<String, Object> map : outputlist) {
                                    for (Map<String, Object> stopmap : stoplist) {
                                        if ((map.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                            map.put("outputname", map.get("outputname") + "【排放中】");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }*/
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 总企业数、重点源、涉水、涉气、涉危废、行业类型
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/1/19 14:26
     */
    @RequestMapping(value = "countPollutionData", method = RequestMethod.POST)
    public Object countPollutionData() throws Exception {
        try {
            //总企业数
            long totalEnt = pollutionService.countTotalByParam(new HashMap<>());
            //停产企业数

            String nowDay = DataFormatUtil.getDateYMDHMS(new Date());
           /* String labelcode = DataFormatUtil.parseProperties("zd.label");
            long zdEnt = pollutionService.countTotalByLabel(labelcode);*/

            long stopNum = pollutionService.countStopTotal(nowDay);

            long noStopNum = totalEnt - stopNum;
            //涉水
            String labelcode = DataFormatUtil.parseProperties("water.class");
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("class", labelcode);
            long waterEnt = pollutionService.countTotalByClass(paramMap);
            //涉气
            labelcode = DataFormatUtil.parseProperties("gas.class");
            paramMap.put("class", labelcode);
            long gasEnt = pollutionService.countTotalByClass(paramMap);
            //涉危废
            labelcode = DataFormatUtil.parseProperties("wf.label");
            long wfEnt = pollutionService.countTotalByLabel(labelcode);

            //许可排放数
            long pwEnt = pollutionService.countPWTotalByParam(new HashMap<>());
            //在线监控数
            labelcode = DataFormatUtil.parseProperties("zxjc.label");
            long zxEnt = pollutionService.countTotalByLabel(labelcode);
            //正面清单数
            labelcode = DataFormatUtil.parseProperties("zmqd.label");
            long zmEnt = pollutionService.countTotalByLabel(labelcode);
            //行业类型
            List<Map<String, Object>> industryData = pollutionService.countTotalByIndustry();

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("zmqd", zmEnt);
            resultMap.put("zxjk", zxEnt);
            resultMap.put("pwEnt", pwEnt);
            resultMap.put("totalEnt", totalEnt);
            resultMap.put("stopNum", stopNum);
            resultMap.put("noStopNum", noStopNum);
            resultMap.put("waterEnt", waterEnt);
            resultMap.put("gasEnt", gasEnt);
            resultMap.put("wfEnt", wfEnt);
            resultMap.put("industryData", industryData);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 汽油桶关注类型统计
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/1/19 14:26
     */
    @RequestMapping(value = "countEntControlData", method = RequestMethod.POST)
    public Object countEntControlData() throws Exception {
        try {

            //行业类型
            List<Map<String, Object>> industryData = pollutionService.countEntControlData();


            return AuthUtil.parseJsonKeyToLower("success", industryData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 企业行政区划统计
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/1/19 14:26
     */
    @RequestMapping(value = "countEntRegionData", method = RequestMethod.POST)
    public Object countEntRegionData() throws Exception {
        try {


            List<Map<String, Object>> industryData = pollutionService.countEntRegionData();


            return AuthUtil.parseJsonKeyToLower("success", industryData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 统计排污许可类型
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/1/19 14:26
     */
    @RequestMapping(value = "countPWPollutionData", method = RequestMethod.POST)
    public Object countPWPollutionData() throws Exception {
        try {
            List<Map<String, Object>> pwTypeData = pollutionService.countPWPollutionData();
            return AuthUtil.parseJsonKeyToLower("success", pwTypeData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计排污许可类型（发证企业数，排污管理类型）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/1/19 14:26
     */
    @RequestMapping(value = "countPWData", method = RequestMethod.POST)
    public Object countPWData() throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            long entnum = pollutionService.countPWTotalByParam(new HashMap<>());
            resultMap.put("entnum", entnum);
            List<Map<String, Object>> pwTypeData = pollutionService.countPWPollutionData();
            resultMap.put("pwTypeData", pwTypeData);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/25 0025 下午 14:34
     * @Description: 根据企业ID获取该企业所监测的监测类型(2.0, 支持废气烟气合并)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEntMonitorPointTypeByParam", method = RequestMethod.POST)
    public Object getEntMonitorPointTypeByParam(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            //获取环保 企业关联类型
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> param = new HashMap<>();
            param.put("pollutiontypes", pollutiontypes);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> maps = pollutionService.getEntMonitorPointTypeByParam(param);
            //按类型名称分组
            Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
            //MN_污染物 分组
            if (maps != null && maps.size() > 0) {
                listmap = maps.stream().collect(Collectors.groupingBy(m -> m.get("monitorpointtypename").toString()));
                List<Map<String, Object>> codelist;
                for (Map.Entry<String, List<Map<String, Object>>> entry : listmap.entrySet()) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("monitorpointtypename", entry.getKey());
                    List<Integer> codes = new ArrayList<>();
                    codelist = entry.getValue();
                    for (Map<String, Object> codemap : codelist) {
                        if (codemap.get("monitorpointtypecode") != null) {
                            codes.add(Integer.valueOf(codemap.get("monitorpointtypecode").toString()));
                        }
                        if (onemap.get("orderindex") == null) {
                            onemap.put("orderindex", codemap.get("orderindex") != null ? codemap.get("orderindex") : codemap.get("monitorpointtypecode"));
                        }
                    }
                    onemap.put("monitorpointtypecode", codes);
                    result.add(onemap);
                }
            }
            if (result.size() > 0) {
                //排序
                result = result.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/25 0025 下午 14:52
     * @Description: 根据企业ID获取该企业下所有监测点信息（包括视频信息）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEntMonitorPointInfoDataByParam", method = RequestMethod.POST)
    public Object getEntMonitorPointInfoDataByParam(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtypes", pollutiontypes);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> dataList = new ArrayList<>();
            //企业下监测点信息
            dataList = pollutionService.getEntPointInfoByEntIDAndTypes(param);
            //企业下摄像头信息
            dataList.addAll(videoCameraService.getVideoCameraInfosByMonitorPointIDAndType(param));
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 2:13
     * @Description: 根据企业ID获取该企业的最新台账记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "countEntStandingBookDataByPollutionID", method = RequestMethod.POST)
    public Object countEntStandingBookDataByPollutionID(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> maps = pollutionService.countEntStandingBookDataByPollutionID(param);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取企业厂内外二维码数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/9 15:46
     */
    @RequestMapping(value = "getEntQRListDataByParamMap", method = RequestMethod.POST)
    public Object getEntQRListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = pollutionService.getEntQRListDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            //二维码参数
            if (datalist.size() > 0) {
                for (Map<String, Object> dataMap : datalist) {
                    dataMap.put("inparam", dataMap.get("pkpollutionid") + "_1");
                    dataMap.put("outparam", dataMap.get("pkpollutionid") + "_2");
                }
            }
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 更新企业厂内外二维码信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "updateQRData", method = RequestMethod.POST)
    public Object updateQRData(
            @RequestJson(value = "codetype") Integer codetype,
            @RequestJson(value = "codedata") String codedata,
            @RequestJson(value = "pollutionid") String pollutionid

    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", pollutionid);
            param.put("codedata", codedata);
            param.put("updatetime", new Date());
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("updateuser", userId);
            if (codetype == 1) {//厂内码
                param.put("set", "incode");

            } else {//厂外码
                param.put("set", "outcode");
            }
            int num = pollutionService.updateQRDataByParam(param);
            if (num == 0) {
                param.put("pk_id", UUID.randomUUID().toString());
                pollutionService.addQRDataByParam(param);
            }

            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取企业画像信息数据
     * <p>
     * <p>
     * 企业基本特征（企业状态、在线监测企业、大型企业、上市企业、排污重点管理、污水处理厂、清洁生产企业）
     * 产治排特征（排口规范管理、危固废规范化、排污大户企业）
     * 监管特征（环保手续齐全、证后规范管理、许可证状态、环境（违法、守法）企业、企业信用（绿牌））
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/4 11:52
     */
    @RequestMapping(value = "getEntPortraitsDataList", method = RequestMethod.POST)
    public Object getEntPortraitsDataList(
            @RequestJson(value = "pollutionid") String pollutionid

    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            List<Map<String, Object>> dataList = pollutionService.getEntLabelDataListById(pollutionid);

            //1、企业基本特征（企业状态、在线监测企业、大型企业、上市企业、排污重点管理、污水处理厂、清洁生产企业）
            Map<String, Object> baseMap = new HashMap<>();
            baseMap.put("featurename", "企业基本特征");
            List<Map<String, Object>> itemlist = new ArrayList<>();

            Map<String, Object> entstate = new HashMap<>();
            String state = dataList.size() > 0 ? dataList.get(0).get("entstate") != null ? dataList.get(0).get("entstate").toString()
                    : "正常" : "正常";
            entstate.put("itemname", "企业状态（" + state + "）");
            entstate.put("itemcode", "entstate");
            entstate.put("itemtype", "1");
            itemlist.add(entstate);

            Map<String, Object> online = new HashMap<>();
            String labelcode = DataFormatUtil.parseProperties("online.code");
            String isHave = isHaveLabel(dataList, labelcode);
            online.put("itemcode", "online");
            online.put("itemname", "在线监测企业");
            online.put("itemtype", isHave);
            itemlist.add(online);

            Map<String, Object> shangs = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("shangs.code");
            isHave = isHaveLabel(dataList, labelcode);
            shangs.put("itemcode", "shangs");
            shangs.put("itemname", "上市企业");
            shangs.put("itemtype", isHave);
            itemlist.add(shangs);

            Map<String, Object> entscale = new HashMap<>();
            String scale = dataList.size() > 0 ? dataList.get(0).get("entscale") != null ? dataList.get(0).get("entscale").toString() : "其他" : "其他";
            entscale.put("itemcode", "entscale");
            entscale.put("itemname", "企业规模（" + scale + "）");
            entscale.put("itemtype", "1");
            itemlist.add(entscale);


            Map<String, Object> zdpw = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("zdpw.code");
            isHave = isHaveLabel(dataList, labelcode);
            zdpw.put("itemcode", "zdpw");
            zdpw.put("itemname", "排污重点管理");
            zdpw.put("itemtype", isHave);
            itemlist.add(zdpw);


            Map<String, Object> wsclc = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("wsclc.code");
            isHave = isHaveLabel(dataList, labelcode);
            wsclc.put("itemcode", "wsclc");
            wsclc.put("itemname", "污水处理厂");
            wsclc.put("itemtype", isHave);
            itemlist.add(wsclc);

            Map<String, Object> qjsc = new HashMap<>();
            long qjscNum = pollutionService.getQJSCNumByPid(pollutionid);
            qjsc.put("itemcode", "qjsc");
            qjsc.put("itemname", "清洁生产企业");
            qjsc.put("itemtype", qjscNum > 0 ? "1" : "0");
            itemlist.add(qjsc);
            baseMap.put("itemlist", itemlist);
            resultList.add(baseMap);
            //2、 产治排特征（排口规范管理、危固废规范化、排污大户企业）
            Map<String, Object> CZPMap = new HashMap<>();
            CZPMap.put("featurename", "产治排特征");
            List<Map<String, Object>> itemlist_czp = new ArrayList<>();

            Map<String, Object> pkgf = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("pkgf.code");
            isHave = isHaveLabel(dataList, labelcode);
            pkgf.put("itemcode", "pkgf");
            pkgf.put("itemname", "排口规范管理");
            pkgf.put("itemtype", isHave);
            itemlist_czp.add(pkgf);


            Map<String, Object> wgfgf = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("wgfgf.code");
            isHave = isHaveLabel(dataList, labelcode);
            wgfgf.put("itemcode", "wgfgf");
            wgfgf.put("itemname", "危固废规范化");
            wgfgf.put("itemtype", isHave);
            itemlist_czp.add(wgfgf);

            Map<String, Object> pwdh_water = new HashMap<>();
            String maxWaterFlow = DataFormatUtil.parseProperties("maxWaterFlow");

            isHave = StringUtils.isBlank(maxWaterFlow) ? "0" : isOverFlow(maxWaterFlow, pollutionid, WasteWaterEnum.getCode());
            pwdh_water.put("itemcode", "waterflow");
            pwdh_water.put("itemname", "水排污大户企业");
            pwdh_water.put("itemtype", isHave);
            itemlist_czp.add(pwdh_water);

            Map<String, Object> pwdh_gas = new HashMap<>();
            String maxGasFlow = DataFormatUtil.parseProperties("maxGasFlow");
            isHave = StringUtils.isBlank(maxWaterFlow) ? "0" : isOverFlow(maxGasFlow, pollutionid, WasteGasEnum.getCode());
            pwdh_gas.put("itemcode", "gasflow");
            pwdh_gas.put("itemname", "气排污大户企业");
            pwdh_gas.put("itemtype", isHave);
            itemlist_czp.add(pwdh_gas);

            CZPMap.put("itemlist", itemlist_czp);
            resultList.add(CZPMap);
            //监管特征（环保手续齐全、证后规范管理、许可证状态、环境（违法、守法）企业、企业信用（绿牌））

            Map<String, Object> JGMap = new HashMap<>();
            JGMap.put("featurename", "监管特征");
            List<Map<String, Object>> itemlist_jg = new ArrayList<>();

            Map<String, Object> hbsx = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("hbsx.code");
            isHave = isHaveLabel(dataList, labelcode);
            hbsx.put("itemcode", "hbsx");
            hbsx.put("itemname", "环保手续齐全");
            hbsx.put("itemtype", isHave);
            itemlist_jg.add(hbsx);


            //证后规范管理
            Map<String, Object> zhgl = new HashMap<>();
            isHave = getZHGL(pollutionid);
            zhgl.put("itemcode", "zhgl");
            zhgl.put("itemname", "证后规范管理");
            zhgl.put("itemtype", isHave);
            itemlist_jg.add(zhgl);

            //许可证状态
            Map<String, Object> xkz = new HashMap<>();
            isHave = getXKZ(pollutionid);
            xkz.put("itemcode", "xkz");
            xkz.put("itemname", "许可证（" + isHave + "）");
            xkz.put("itemtype", "1");
            itemlist_jg.add(xkz);


            //许可证状态、环境（违法、守法）企业
            Map<String, Object> hjwf = new HashMap<>();
            labelcode = DataFormatUtil.parseProperties("hjwf.code");
            isHave = isHaveLabel(dataList, labelcode);
            if (isHave.equals("1")) {
                hjwf.put("itemname", "环境违法企业");
            } else {
                hjwf.put("itemname", "环境守法企业");
            }
            hjwf.put("itemcode", "hjwf");
            hjwf.put("itemtype", "1");
            itemlist_jg.add(hjwf);

            //企业信用

            Map<String, Object> xypj = new HashMap<>();
            isHave = getXYPJ(pollutionid);
            xypj.put("itemname", "企业信用（" + isHave + "）");
            xypj.put("itemcode", "xypj");
            xypj.put("itemtype", "1");
            itemlist_jg.add(xypj);
            JGMap.put("itemlist", itemlist_jg);
            resultList.add(JGMap);

            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getXYPJ(String pollutionid) {
        String entenvcredit = envCreditEvaluationService.getLastEntEnvCreditByPid(pollutionid);
        return StringUtils.isNotBlank(entenvcredit) ? entenvcredit : "未评价";
    }

    private String getXKZ(String pollutionid) {
        Map<String, Object> paramMap = new HashMap<>();
        String isHave = "";
        paramMap.put("pollutionid", pollutionid);
        List<Map<String, Object>> dataList = licenceService.getPWLicenceListDataByParamMap(paramMap);
        if (dataList.size() > 0) {
            Map<String, Object> dataMap = dataList.get(0);
            String LicenceConditionname = dataMap.get("LicenceConditionname") != null ? dataMap.get("LicenceConditionname").toString() : "正常";
            isHave = LicenceConditionname;
            if ("正常".equals(LicenceConditionname)) {//判断是否过期
                if (dataMap.get("licenceenddate") != null) {
                    Date date = DataFormatUtil.getDateYMD(dataMap.get("licenceenddate").toString());
                    if (date.before(new Date())) {
                        isHave = "过期";
                    }
                }
            }
        }
        return isHave;
    }

    private String getZHGL(String pollutionid) {

        String isHave = "0";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pollutionid", pollutionid);
        String year = DataFormatUtil.getBeforeYear(1, DataFormatUtil.getDateY(new Date()));
        jsonObject.put("year", year);
        PageInfo<Map<String, Object>> pageInfos = licenceService.getEntStandingInfoByParam(jsonObject);
        List<Map<String, Object>> datas = pageInfos.getList();
        if (datas.size() > 0) {
            Map<String, Object> dataMap = datas.get(0);
            if (dataMap.get("tznum") != null && dataMap.get("zxnum") != null) {
                int tznum = Integer.parseInt(dataMap.get("tznum").toString());
                int zxnum = Integer.parseInt(dataMap.get("zxnum").toString());
                if (tznum > 0 && zxnum > 0) {
                    isHave = "1";
                }
            }

        }
        return isHave;

    }

    private String isOverFlow(String maxWaterFlow, String pollutionid, int type) {
        String isHave = "0";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutionid", pollutionid);
        List<Map<String, Object>> outPutList;
        if (type == WasteWaterEnum.getCode()) {
            outPutList = onlineService.getWaterMNSByParam(paramMap);
        } else {
            outPutList = onlineService.getGasMNSByParam(paramMap);
        }
        List<String> mns = outPutList.stream().map(m -> m.get("DGIMN") + "").collect(Collectors.toList());
        paramMap.clear();
        String year = DataFormatUtil.getDateY(new Date());
        String starttime = DataFormatUtil.getYearFirst(year);
        String endtime = DataFormatUtil.getYearLast(year);

        paramMap.put("starttime", starttime + " 00:00:00");
        paramMap.put("endtime", endtime + " 23:59:59");
        paramMap.put("mns", mns);
        paramMap.put("collection", DB_YearFlowData);
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        Double totalFlow = 0d;
        for (Document document : documents) {
            totalFlow += document.get("TotalFlow") != null ? Double.parseDouble(document.getString("TotalFlow")) : 0;
        }
        if (totalFlow >= Double.parseDouble(maxWaterFlow)) {
            isHave = "1";
        }
        return isHave;


    }

    private String isHaveLabel(List<Map<String, Object>> dataList, String labelcode) {
        String isHave = "0";
        for (Map<String, Object> dataMap : dataList) {
            if (labelcode.equals(dataMap.get("labelcode"))) {
                isHave = "1";
            }
        }
        return isHave;
    }


}
