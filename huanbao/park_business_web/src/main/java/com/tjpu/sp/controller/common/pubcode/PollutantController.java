package com.tjpu.sp.controller.common.pubcode;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("pollutant")
public class PollutantController {


    @Autowired
    private WaterStationPollutantSetService waterStationPollutantSetService;


    private final PollutantService pollutantService;
    private final WaterOutPutPollutantSetService waterOutPutPollutantSetService;
    private final GasOutPutPollutantSetService gasOutPutPollutantSetService;
    private final AirStationPollutantSetService airStationPollutantSetService;
    private final OtherMonitorPointPollutantSetService otherMonitorPointPollutantSetService;
    private final SoilPointService soilPointService;

    public PollutantController(PollutantService pollutantService,
                               WaterOutPutPollutantSetService waterOutPutPollutantSetService,
                               GasOutPutPollutantSetService gasOutPutPollutantSetService,
                               AirStationPollutantSetService airStationPollutantSetService,
                               OtherMonitorPointPollutantSetService otherMonitorPointPollutantSetService, SoilPointService soilPointService
    ) {
        this.pollutantService = pollutantService;
        this.waterOutPutPollutantSetService = waterOutPutPollutantSetService;
        this.gasOutPutPollutantSetService = gasOutPutPollutantSetService;
        this.airStationPollutantSetService = airStationPollutantSetService;
        this.otherMonitorPointPollutantSetService = otherMonitorPointPollutantSetService;
        this.soilPointService = soilPointService;
    }

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过排口id和污染物类型获取排口关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutantsByOutputIdAndPollutantType", method = RequestMethod.POST)
    public Object getPollutantsByOutputIdAndPollutantType(
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutanttype") Integer pollutanttype) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutanttype)) {
                case WasteWaterEnum:
                    paramMap.put("pollutanttype", pollutanttype);
                    dataList = waterOutPutPollutantSetService.getWaterOrRainPollutantsByParamMap(paramMap);
                    break;
                case WasteGasEnum:
                case SmokeEnum:
                    paramMap.put("unorgflag", false);
                    paramMap.put("pollutanttype", pollutanttype);
                    dataList = gasOutPutPollutantSetService.getGasPollutantsByOutputId(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("pollutanttype", pollutanttype);
                    dataList = waterOutPutPollutantSetService.getWaterOrRainPollutantsByParamMap(paramMap);
                    break;
                case AirEnum:
                    dataList = airStationPollutantSetService.getAirPollutantByParamMap(paramMap);
                    Map<String, Object> aqi = new HashMap<>();
                    aqi.put("pollutantcode", "aqi");
                    aqi.put("pollutantname", "AQI");
                    dataList.add(0, aqi);
                    break;
                case WaterQualityEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode() + "");
                    dataList = waterStationPollutantSetService.getWaterStationPollutantByParamMap(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() + "");
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
                    break;
                case EnvironmentalDustEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode() + "");
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
                    break;
                case meteoEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode() + "");
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
                    break;
                case EnvironmentalVocEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() + "");
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
                    break;
                case MicroStationEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode() + "");
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    dataList = gasOutPutPollutantSetService.getGasPollutantsByOutputId(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("unorgflag", true);
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    dataList = gasOutPutPollutantSetService.getGasPollutantsByOutputId(paramMap);
                    break;
                case soilEnum:
                    paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.soilEnum.getCode());
                    dataList = soilPointService.getSoilPollutantsByParam(paramMap);
                    break;
                default:
                    paramMap.put("pollutanttype", pollutanttype+"");
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:31
     * @Description: 根据监测点类型获取重点监测污染物信息
     * @param: pollutantType  9 是恶臭 、10 voc
     * @return:
     */
    @RequestMapping(value = "getKeyPollutantsByMonitorPointType", method = RequestMethod.POST)
    public Object getKeyPollutantsByMonitorPointType(@RequestJson(value = "pollutanttype") Integer pollutantType) {
        try {
            return AuthUtil.parseJsonKeyToLower("success", pollutantService.getKeyPollutantsByMonitorPointType(pollutantType));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取监测点报警等级数据（废水、废气、烟气、环境恶臭、厂界恶臭、微站、扬尘、VOC）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/1 14:56
     */
    @RequestMapping(value = "getPointAlarmSetByParam", method = RequestMethod.POST)
    public Object getPointAlarmSetByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("monitorpointtype", monitorpointtype);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum: //废水
                    dataList = waterOutPutPollutantSetService.getPollutantSetByParam(paramMap);
                    break;
                case FactoryBoundaryStinkEnum://厂界恶臭
                case WasteGasEnum: //废气
                case SmokeEnum: //烟气
                    dataList = gasOutPutPollutantSetService.getPollutantSetByParam(paramMap);
                    break;
                case EnvironmentalVocEnum: //voc
                case EnvironmentalStinkEnum: //恶臭
                case MicroStationEnum://微站
                case EnvironmentalDustEnum://扬尘
                    dataList = otherMonitorPointPollutantSetService.getOtherPollutantSetByParam(paramMap);
                    break;
            }
            if (dataList.size() > 0) {
                Integer levelcode;
                List<Map<String, Object>> alarmList = new ArrayList<>();
                for (Map<String, Object> dataMap : dataList) {
                    resultMap.put("alarmtype", dataMap.get("alarmtype"));
                    resultMap.put("standardmaxvalue", dataMap.get("standardmaxvalue"));
                    resultMap.put("standardminvalue", dataMap.get("standardminvalue"));
                    if (dataMap.get("fk_alarmlevelcode") != null && StringUtils.isNotBlank(dataMap.get("fk_alarmlevelcode").toString())) {
                        levelcode = Integer.parseInt(dataMap.get("fk_alarmlevelcode").toString());
                        if (levelcode > 0) {//报警set
                            Map<String, Object> alarmMap = new HashMap<>();
                            alarmMap.put("alarmlevelcode", levelcode);
                            alarmMap.put("concenalarmmaxvalue", dataMap.get("concenalarmmaxvalue"));
                            alarmMap.put("concenalarmminvalue", dataMap.get("concenalarmminvalue"));
                            alarmList.add(alarmMap);
                        }
                    }
                }
                if (alarmList.size() > 0) {
                    alarmList = alarmList.stream().sorted(Comparator.comparing(m -> (m).get("alarmlevelcode").toString())).collect(Collectors.toList());
                }
                resultMap.put("alarmlist", alarmList);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/6 0006 下午 2:51
     * @Description: 根据监测点类型获取重点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getKeyPollutantsByMonitorPointTypes", method = RequestMethod.POST)
    public Object getKeyPollutantsByMonitorPointType(@RequestJson(value = "monitorpointtypes") List<Integer> monitorPointTypes) {
        try {
            Set<Map<String, Object>> resultSet = new LinkedHashSet<>();
            Set<String> pollutantcodes = new HashSet<>();
            if (monitorPointTypes.size() > 0) {
                List<Map<String, Object>> dataList = new ArrayList<>();
                for (Integer type : monitorPointTypes) {
                    dataList.addAll(pollutantService.getKeyPollutantsByMonitorPointType(type));
                }
                if (dataList.size() > 0) {
                    for (Map<String, Object> map : dataList) {
                        if (map.get("Code") != null && !pollutantcodes.contains(map.get("Code").toString())) {
                            resultSet.add(map);
                            pollutantcodes.add(map.get("Code").toString());
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultSet);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/6 0006 下午 2:51
     * @Description: 获取全部监测点类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllMonitorPointTypeList", method = RequestMethod.POST)
    public Object getAllMonitorPointTypeList() {
        try {
            List<Map<String, Object>> dataList = pollutantService.getAllMonitorPointTypeList();
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 7:03
     * @Description: 通过排口id集合查询废气，或废气无组织污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputids, pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "getGasPollutantsByOutputIdsAndPollutantType", method = RequestMethod.POST)
    public Object getGasPollutantsByOutputIdsAndPollutantType(
            @RequestJson(value = "outputids", required = false) List<String> outputids,
            @RequestJson(value = "idandtype", required = false) Object idAndType,
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "pollutanttype", required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes", required = false) List<Integer> pollutanttypes) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            if (pollutanttypes == null || pollutanttypes.size() == 0) {
                pollutanttypes = new ArrayList<>();
                pollutanttypes.add(pollutanttype);
            }

            if (idAndType!=null){
                Map<String,Object> idAndTypeMap = (Map<String, Object>) idAndType;
                for (String id:idAndTypeMap.keySet()){
                    paramMap.put("outputids", Arrays.asList(id));
                    paramMap.put("pollutanttype", idAndTypeMap.get(id));
                    dataList.addAll(gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap));
                }
            }else {
                for (Integer i : pollutanttypes) {
                    paramMap.put("outputids", outputids);
                    paramMap.put("pollutanttype",i);
                    paramMap.put("pollutionid",pollutionid);
                    dataList.addAll(gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap));
                }
            }
            if (dataList.size() > 0) {
                List<String> codes = new ArrayList<>();
                for (Map<String, Object> m : dataList) {
                    m.remove("outputid");
                    m.remove("pollutanttype");
                    m.remove("StandardMaxValue");
                    m.remove("StandardMinValue");
                    if (m.get("pollutantcode") != null && !codes.contains(m.get("pollutantcode").toString())) {
                        result.add(m);
                        codes.add(m.get("pollutantcode").toString());
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/2/26 0026 下午 4:36
     * @Description: 为app实时数据功能单独提供，（慎用）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputids, pollutionid, pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByOutputIdsAndPollutantType", method = RequestMethod.POST)
    public Object getPollutantsByOutputIdsAndPollutantType(
            @RequestJson(value = "outputids", required = false) List<String> outputids,
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "pollutanttype") Integer pollutanttype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", outputids);
            paramMap.put("pollutanttype", pollutanttype);
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);

            List<Integer> AQType = Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode());
            if (dataList.size() > 0 && AQType.contains(pollutanttype)) {
                String pollutantCode;
                //获取报警分级标准（安全）
                List<Map<String, Object>> alarmLevelDataList = gasOutPutPollutantSetService.getAlarmLevelDataByParam(paramMap);
                Map<String, List<Map<String, Object>>> codeAndLevelData = new HashMap<>();
                if (alarmLevelDataList.size() > 0) {
                    String alarmType;

                    List<Map<String, Object>> levelData;
                    for (Map<String, Object> alarmLevel : alarmLevelDataList) {
                        alarmType = alarmLevel.get("AlarmType") != null ? alarmLevel.get("AlarmType").toString() : null;
                        if (alarmType != null) {
                            pollutantCode = alarmLevel.get("pollutantcode").toString();
                            if (codeAndLevelData.containsKey(pollutantCode)) {
                                levelData = codeAndLevelData.get(pollutantCode);
                            } else {
                                levelData = new ArrayList<>();
                            }
                            Map<String, Object> levelMap = new HashMap<>();
                            levelMap.put("levelcode", alarmLevel.get("AlarmLevelCode"));
                            if (alarmType.equals(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {//上限报警
                                levelMap.put("AlarmMaxValue", alarmLevel.get("AlarmMaxValue"));
                            } else if (alarmType.equals(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())) {
                                levelMap.put("AlarmMinValue", alarmLevel.get("AlarmMinValue"));
                            } else if (alarmType.equals(CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode())) {
                                levelMap.put("AlarmMaxValue", alarmLevel.get("AlarmMaxValue"));
                                levelMap.put("AlarmMinValue", alarmLevel.get("AlarmMinValue"));
                            }
                            levelData.add(levelMap);
                            codeAndLevelData.put(pollutantCode, levelData);
                        }

                    }
                }
                for (Map<String, Object> dataMap : dataList) {
                    pollutantCode = dataMap.get("pollutantcode").toString();
                    if (codeAndLevelData.containsKey(pollutantCode)) {
                        dataMap.put("leveldata", codeAndLevelData.get(pollutantCode));
                    }
                }
            }

            if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                Map<String, Object> aqi = new HashMap<>();
                aqi.put("pollutantcode", "aqi");
                aqi.put("pollutantname", "AQI");
                dataList.add(0, aqi);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/12/2 0002 下午 4:12
     * @Description: 获取恶臭（环境恶臭，厂界恶臭）关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkPollutantsByParam", method = RequestMethod.POST)
    public Object getStinkPollutantsByParam(
            @RequestJson(value = "outputids", required = false) List<String> outputids) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", outputids);
            List<Integer> monitorpointtypes = Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Integer type : monitorpointtypes) {
                paramMap.put("pollutanttype", type);
                dataList.addAll(gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap));
            }

            Set<Object> codes = new HashSet<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Map<String, Object> pollutant : dataList) {
                if (!codes.contains(pollutant.get("pollutantcode"))) {
                    codes.add(pollutant.get("pollutantcode"));
                    resultList.add(pollutant);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 7:03
     * @Description: 通过排口id集合查询废气，或废气无组织污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputids, pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByParamJson", method = RequestMethod.POST)
    public Object getPollutantsByParamJson(
            @RequestJson(value = "paramjson") Object paramjson
    ) {
        try {
            List<Map<String, Object>> codes = new ArrayList<>();
            List<String> temp = new ArrayList<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (paramjson != null) {
                List<Map<String, Object>> outputList = (List<Map<String, Object>>) paramjson;
                for (Map<String, Object> map : outputList) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("outputids", map.get("outputids"));
                    paramMap.put("pollutanttype", map.get("pollutanttype"));
                    paramMap.put("pollutionid", map.get("pollutionid"));
                    dataList.addAll(gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap).stream().peek(m -> {
                        m.remove("outputid");
                        m.remove("pollutanttype");
                    }).distinct().collect(Collectors.toList()));
                }
            }

            if (dataList.size() > 0) {//去重复
                for (Map<String, Object> map : dataList) {
                    String pollutantname = map.get("pollutantname") == null ? "" : map.get("pollutantname").toString();
                    String pollutantcode = map.get("pollutantcode") == null ? "" : map.get("pollutantcode").toString();
                    map.put("OrderIndex",map.get("map")!=null?map.get("map"):9999);
                    if (!temp.contains(pollutantcode + pollutantname)) {
                        temp.add(pollutantcode + pollutantname);
                        codes.add(map);
                    }
                }
            }
            //排序
            codes = codes.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("OrderIndex").toString()))).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", codes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 3:46
     * @Description: 通过污染物类型获取对应类型的污染物下拉数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByPollutantType", method = RequestMethod.POST)
    public Object getPollutantsByPollutantType(@RequestJson(value = "pollutanttype", required = false) String pollutanttype,
                                               @RequestJson(value = "isshowflow", required = false) String isshowflow,
                                               @RequestJson(value = "pollutanttypes", required = false) List<String> pollutanttypes) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            if (pollutanttype != null) {
                paramMap.put("pollutanttype", pollutanttype);
            } else {
                paramMap.put("monitorpointtypes", pollutanttypes);
            }
            //获取排放量污染物
            paramMap.put("isshowflow", isshowflow);
            List<Map<String, Object>> listdata = pollutantService.getPollutantsByPollutantType(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> codes = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    if (map.get("code") != null && !codes.contains(map.get("code").toString())) {
                        Map<String, Object> objmap = new HashMap<String, Object>();
                        objmap.put("labelname", map.get("name"));
                        objmap.put("value", map.get("code"));
                        result.add(objmap);
                        codes.add(map.get("code").toString());
                    }
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
     * @date: 2019/7/11 0011 下午 4:24
     * @Description: 通过监测点ID、监测点类型和污染物编码获取污染物的标准值和预警值(浓度阈值预警)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEarlyAndStandardValueByParams", method = RequestMethod.POST)
    public Object getEarlyAndStandardValueByParams(@RequestJson(value = "monitorpointid", required = true) String monitorpointid,
                                                   @RequestJson(value = "alarmlevel", required = false) String alarmlevel,
                                                   @RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype,
                                                   @RequestJson(value = "pollutantcodes", required = true) List<String> pollutants) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("alarmlevel", alarmlevel);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pollutants", pollutants);
            List<Map<String, Object>> listdata = pollutantService.getEarlyAndStandardValueByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 10:02
     * @Description: 通过监测点ID、监测点类型获取该点位监测的污染物信息（安全点位）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getSecurityPointPollutantDataByParams", method = RequestMethod.POST)
    public Object getSecurityPointPollutantDataByParams(@RequestJson(value = "monitorpointid", required = true) String monitorpointid,
                                                        @RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listdata = pollutantService.getSecurityPointPollutantDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/5/17 0017 上午 11:18
     * @Description: 通过监测点类型获取污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByMonitorpointTypes", method = RequestMethod.POST)
    public Object getPollutantsByMonitorpointTypes(@RequestJson(value = "monitorpointtypes", required = true) Object monitorpointtypes) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttypes", monitorpointtypes);
            List<Map<String, Object>> listdata = pollutantService.getAllPollutionPollutants(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
