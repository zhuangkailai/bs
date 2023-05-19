package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DeviceDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorcontrol.MonitorPointMonitorControlMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.online.OnlineMapper;
import com.tjpu.sp.dao.environmentalprotection.productionmaterials.ProductInfoMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineMonitorServiceImpl implements OnlineMonitorService {

    private final MongoTemplate mongoTemplate;
    private final WaterOutputInfoMapper waterOutputInfoMapper;
    private final GasOutPutInfoMapper gasOutPutInfoMapper;
    private final OtherMonitorPointMapper otherMonitorPointMapper;
    private final UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    private final WaterStationMapper waterStationMapper;
    private final AirMonitorStationMapper airMonitorStationMapper;
    private final DeviceDevOpsInfoMapper deviceDevOpsInfoMapper;
    private final PollutantFactorMapper pollutantFactorMapper;
    private final OnlineMapper onlineMapper;
    private final MonitorPointMonitorControlMapper monitorPointMonitorControlMapper;
    private final String db_LatestData = "LatestData";
    private final String db_OverData = "OverData";
    private final String db_OverModel = "OverModel";
    private final String db_suddenRiseData = "SuddenRiseData";
    private final WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;
    private final GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;
    private final ProductInfoMapper productInfoMapper;

    public OnlineMonitorServiceImpl(MongoTemplate mongoTemplate, WaterOutputInfoMapper waterOutputInfoMapper, GasOutPutInfoMapper gasOutPutInfoMapper, OtherMonitorPointMapper otherMonitorPointMapper, UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper, WaterStationMapper waterStationMapper, AirMonitorStationMapper airMonitorStationMapper, DeviceDevOpsInfoMapper deviceDevOpsInfoMapper, PollutantFactorMapper pollutantFactorMapper, OnlineMapper onlineMapper, MonitorPointMonitorControlMapper monitorPointMonitorControlMapper, WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper, GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper, ProductInfoMapper productInfoMapper) {
        this.mongoTemplate = mongoTemplate;
        this.waterOutputInfoMapper = waterOutputInfoMapper;
        this.gasOutPutInfoMapper = gasOutPutInfoMapper;
        this.otherMonitorPointMapper = otherMonitorPointMapper;
        this.unorganizedMonitorPointInfoMapper = unorganizedMonitorPointInfoMapper;
        this.waterStationMapper = waterStationMapper;
        this.airMonitorStationMapper = airMonitorStationMapper;
        this.deviceDevOpsInfoMapper = deviceDevOpsInfoMapper;
        this.pollutantFactorMapper = pollutantFactorMapper;
        this.onlineMapper = onlineMapper;
        this.monitorPointMonitorControlMapper = monitorPointMonitorControlMapper;
        this.waterOutPutPollutantSetMapper = waterOutPutPollutantSetMapper;
        this.gasOutPutPollutantSetMapper = gasOutPutPollutantSetMapper;

        this.productInfoMapper = productInfoMapper;
    }

    /**
     * @author: lip
     * @date: 2020/6/15 0015 下午 2:48
     * @Description: 获取在线监测点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineOutPutListByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> outPutOutList = new ArrayList<>();
        Integer monitorPointType = Integer.parseInt(paramMap.get("monitorPointType").toString());
        String datatypeflag = "pc";
        if (paramMap.get("datatypeflag") != null) {//根据该字段判断是 APP调用还是PC端调用  默认为Pc端调用
            datatypeflag = paramMap.get("datatypeflag").toString();
        }
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
            case WasteWaterEnum:
                //废水
                paramMap.put("outputtype", "water");
                paramMap.put("datatypeflag", datatypeflag);
                outPutOutList = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                break;
            case RainEnum:
                // 雨水
                paramMap.put("outputtype", "rain");
                paramMap.put("datatypeflag", datatypeflag);
                outPutOutList = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                break;
            case WasteGasEnum:
            case SmokeEnum:
                //废气、烟气
                paramMap.put("datatypeflag", datatypeflag);
                outPutOutList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                break;
            case MicroStationEnum:
                //微站
                outPutOutList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundaryStinkEnum:

                //厂界恶臭、厂界扬尘
                outPutOutList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalVocEnum:
            case EnvironmentalStinkEnum:
            case EnvironmentalDustEnum:
            case meteoEnum:
                //环境VOC、环境恶臭、扬尘
                if (paramMap.get("monitorpointcategory") != null && !"".equals(paramMap.get("monitorpointcategory"))) {
                    Integer monitorpointcategory = Integer.parseInt(paramMap.get("monitorpointcategory").toString());
                    paramMap.put("monitorPointCategorys", Arrays.asList(monitorpointcategory));
                }
                outPutOutList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case WaterQualityEnum:
                outPutOutList = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                break;
            case AirEnum:
                outPutOutList = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                break;
            default://其他点位信息
                outPutOutList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);

        }
        return outPutOutList;
    }

    private List<Document> getRealTimeDataByMNsAndDataType(List<String> mns) {
        List<Document> list = new ArrayList<>();
        Bson bson = Filters.and(eq("Type", "RealTimeData"), in("DataGatherCode", mns));
        FindIterable<Document> documents = mongoTemplate.getCollection(db_LatestData).find(bson);
        for (Document document : documents) {
            list.add(document);
        }
        return list;
    }

    @Override
    public Map<String, Object> getOutPutLastDataByParamMap(List<Map<String, Object>> outputs, Integer monitorPointType, Map<String, Object> paramMap) {
        List<Map<String, Object>> outPutInfos = outputs.stream().distinct().collect(Collectors.toList());
        Map<String, Object> countData = new LinkedHashMap<>();
        Set<Object> tempPollution = new HashSet<>();
        Set<Object> tempNormal = new HashSet<>();
        Set<Object> tempoffline = new HashSet<>();
        Set<Object> tempover = new HashSet<>();
        Set<Object> tempexception = new HashSet<>();
        Set<Object> stopOutPutIds = new HashSet<>();
        //mn号
        List<String> mns = new ArrayList<>();
        List<String> outPutIds = new ArrayList<>();


        for (Map<String, Object> outPut : outPutInfos) {

            outPutIds.add(outPut.get("pk_id").toString());

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

            if (outPut.get("onlinestatusname") != null && "停产".equals(outPut.get("onlinestatusname").toString())) {//停产
                stopOutPutIds.add(outPut.get("pk_id").toString());
            }

        }
        if (CommonTypeEnum.getOutPutTypeList().contains(monitorPointType) || CommonTypeEnum.getEntMonitorPointTypeList().contains(monitorPointType)) {
            countData.put("pollutioncount", tempPollution.size());
        }
        //统计各状态监测点个数
        countData.put("outputcount", outPutInfos.size());
        countData.put("pollutionids", tempPollution);
        countData.put("normalcount", tempNormal.size());
        countData.put("offlinecount", tempoffline.size());
        countData.put("overcount", tempover.size());
        countData.put("exceptioncount", tempexception.size());
        countData.put("stopnum", stopOutPutIds.size());
        paramMap.put("monitorpointtypes", Arrays.asList(monitorPointType));
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
        Map<String, Object> dbParam = new HashMap<>();
        dbParam.put("monitorpointtype", monitorPointType);
        dbParam.put("mns", mns);
        if (paramMap.get("datatype") != null) {
            dbParam.put("datatype", paramMap.get("datatype"));
        }
        List<Document> documents = getMongoDbDataByParam(dbParam);
        //获取点位及污染物标准值数据
        Map<String, List<Map<String, Object>>> mnCodeAndStandardMap = new HashMap<>();


        String mnCode;
        if (documents.size() > 0) {


            paramMap.put("mnlist", mns);
            paramMap.put("monitorpointtype", monitorPointType);
            List<Map<String, Object>> pollutantStandardDataList = pollutantFactorMapper.getPollutantStandardsByParam(paramMap);
            if (pollutantStandardDataList.size() > 0) {
                //通过mn号分组点位信息
                Map<String, List<Map<String, Object>>> listMap = pollutantStandardDataList.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                    String themn = entry.getKey();
                    List<Map<String, Object>> standlist = entry.getValue();
                    if (standlist != null && standlist.size() > 0) {
                        Map<String, List<Map<String, Object>>> pollutantMap = standlist.stream().collect(Collectors.groupingBy(m -> m.get("Code").toString()));
                        for (Map.Entry<String, List<Map<String, Object>>> entry_two : pollutantMap.entrySet()) {
                            mnCode = themn + "," + entry_two.getKey();
                            mnCodeAndStandardMap.put(mnCode, entry_two.getValue());
                        }
                    }
                }
            }
        }


        //表头数据
        List<Map<String, Object>> tabletitledata = getTableTitleDataForRealTime(monitorPointType);
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
            paramMap2.put("pollutanttype", monitorPointType);
            pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap2);
            //有对应污染物列表中就有数据
            if (pollutants.size() > 0) {
                //获取污染物表头
                List<Map<String, Object>> pollutantstabletitledata = getPollutantTableTitleData(pollutants);
                //将污染物表头数据放入表头数据中
                tabletitledata.addAll(pollutantstabletitledata);
            }
        }
        Map<String, List<String>> outputandcode = new HashMap<>();
        //铅山  雨水类型根据每个排口下监测的污染物来展示监测值
        /*if (monitorPointType == RainEnum.getCode()){
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtype", monitorPointType);
            param.put("datamark", 3);
            List<Map<String,Object>> outputpollutant = waterOutPutPollutantSetMapper.getWaterOrRainPollutantSetInfoByParamMap(param);
            Map<String, List<Map<String, Object>>> listMap = outputpollutant.stream().collect(Collectors.groupingBy(m -> m.get("outputid").toString()));
            for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                List<Map<String, Object>> onelist = entry.getValue();
                List<String> codes = new ArrayList<>();
                for (Map<String, Object> onemap: onelist){
                    if (onemap.get("pollutantcode")!=null) {
                        codes.add(onemap.get("pollutantcode").toString());
                    }
                }
                outputandcode.put(entry.getKey(),codes);
            }
        }*/
        Map<String, List<Map<String, Object>>> idAndRTSP = getRTSPData(Arrays.asList(monitorPointType));
        //排放中的雨水排口
        List<Map<String, Object>> stoplist = new ArrayList<>();
       /* if (monitorPointType == RainEnum.getCode()) {//雨水 判断排放中
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtype", RainEnum.getCode());
            stoplist = monitorPointMonitorControlMapper.getCurrentTimeMonitorControlInfoByParamMap(param);
        }*/
        //列表数据
        Integer ishasconvertdata;
        List<Map<String, Object>> tablelistdata = new ArrayList<>();
        Map<String, Object> mnAndAqi = (Map<String, Object>) dbParam.get("mnAndAqi");
        Map<String, Object> mnAndLevel = (Map<String, Object>) dbParam.get("mnAndLevel");


        //获取mongodb的flag标记
        Map<String,Object> f_map = new HashMap<>();
        f_map.put("monitorpointtypes",Arrays.asList(monitorPointType));
        List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
        Map<String, Object> flag_codeAndName = new HashMap<>();
        String flag_code;
        for (Map<String, Object> map : flagList) {
            if (map.get("code") != null) {
                flag_code = map.get("code").toString();
                flag_codeAndName.put(flag_code, map.get("name"));
            }
        }


        for (Map<String, Object> outPut : outPutInfos) {
            monitorpointid = outPut.get("monitorpointid").toString();
            String dgimn = outPut.get("dgimn").toString();
            Map<String, Object> mapdata = formatRealTimeListDataByPointType(monitorPointType, outPut);
            String monitorTime = "";
            String startCode = CommonTypeEnum.MonitorPointStatusEnum.StartEnum.getCode() + "";
            boolean pointIsStop = false;
            for (Map<String, Object> pollutant : pollutants) {
                Map<String, Object> map2 = new HashMap<>();
                ishasconvertdata = pollutant.get("ishasconvertdata") != null ? Integer.parseInt(pollutant.get("ishasconvertdata").toString()) : 0;
                String code = pollutant.get("code").toString();
                pollutantcodes = idAndCodes.get(monitorpointid);
                Map<String, Object> pollutantmap = new HashMap<>();
                getPointOnlineData(documents, dgimn, code, pollutantmap, ishasconvertdata);
                monitorTime = pollutantmap.get("MonitorTime") != null ? pollutantmap.get("MonitorTime").toString() : "";
                if (pollutantcodes != null && Arrays.asList(pollutantcodes).contains(code)) {
                    map2.put("IsStop", "1");
                    //getPointOnlineData(documents, dgimn, code, pollutantmap, ishasconvertdata);
                } else {
                    if (!CommonTypeEnum.getOutPutTypeList().contains(monitorPointType)) {//处理非排口点位 根据点位表状态判断点位是否  启用、停用
                        if (outPut.get("status") == null || outPut.get("status").toString().equals(startCode)) {
                            //处理未停用的数据 判断是否超标异常
                            //getPointOnlineData(documents, dgimn, code, pollutantmap, ishasconvertdata);
                        } else {

                        }
                    } else {
                        //判断排口点位是否停产 处理废气 烟气 废水 雨水 未停产的排口信息
                        if (outPut.get("onlinestatusname") != null && !outPut.get("onlinestatusname").toString().equals("停产")) {
                            if (monitorPointType == RainEnum.getCode()) {//雨水 判断排放中
                                //先判断雨水排口是否启用
                                if (outPut.get("status") == null || outPut.get("status").toString().equals(startCode)) {
                                    //判断排口是否正在排放中
                                    /*if (stoplist != null && stoplist.size() > 0) {
                                        for (Map<String, Object> stopmap : stoplist) {
                                            if ((outPut.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                                mapdata.put("outputname", outPut.get("outputname") + "【排放中】");
                                            }
                                        }
                                    }*/
                                    //处理未停用的数据 判断是否超标异常
                                    //getPointOnlineData(documents, dgimn, code, pollutantmap, ishasconvertdata);
                                } else {
                                    pointIsStop = true;
                                }
                            } else {
                                //判断非雨水 未停产排口是否启用 停用
                                if (outPut.get("status") == null || outPut.get("status").toString().equals(startCode)) {
                                    //处理未停用的数据 判断是否超标异常
                                    //getPointOnlineData(documents, dgimn, code, pollutantmap, ishasconvertdata);
                                } else {
                                    pointIsStop = true;
                                }
                            }
                        } else {
                            //判断已停产
                            if (outPut.get("onlinestatusname") != null && outPut.get("onlinestatusname").toString().equals("停产")) {
                                mapdata.put("outputname", outPut.get("outputname") + "【已停产】");

                                pointIsStop = true;
                            }

                        }
                    }
                    map2.put("IsStop", "0");
                }

                //铅山处理雨水类型 最新数据只展示属于排口下的因子的值
               /* if (monitorPointType == RainEnum.getCode()) {
                    List<String> pocodelist = outputandcode.get(monitorpointid);
                    map2.put("value", null);
                    for (String pocode : pocodelist) {
                        if (code.equals(pocode)) {
                            map2.put("value", value);
                            break;
                        }
                    }
                } else {
                    map2.put("value", pollutantmap.get("value"));
                }*/

                mnCode = outPut.get("dgimn") + "," + pollutant.get("code");
             /*   if (mnCodeAndStandardMap.containsKey(mnCode)) {
                    map2.putAll(mnCodeAndStandardMap.get(mnCode));
                }*/
                map2.put("standardmaxvalue", null);
                map2.put("alarmtype", null);
                map2.put("standardminvalue", null);
                map2.put("value", pollutantmap.get("value"));
                map2.put("IsOver", pollutantmap.get("IsOver"));
                map2.put("flag", flag_codeAndName.get(pollutantmap.get("flag") != null ? pollutantmap.get("flag").toString().toLowerCase() : ""));
                if (mnCodeAndStandardMap.get(mnCode) != null) {
                    List<Map<String, Object>> standlist = mnCodeAndStandardMap.get(mnCode);
                    if (standlist != null && standlist.size() > 0) {
                        Map<String, Object> obj = standlist.get(0);
                        map2.put("standardmaxvalue", obj.get("StandardMaxValue"));
                        map2.put("alarmtype", obj.get("AlarmType"));
                        map2.put("standardminvalue", obj.get("StandardMinValue"));
                        for (Map<String, Object> objmap : standlist) {
                            if (objmap.get("FK_AlarmLevelCode") != null && !"".equals(objmap.get("FK_AlarmLevelCode").toString())) {
                                Map<String, Object> codeAndMap = new HashMap<>();
                                codeAndMap.put("concenalarmmaxvalue", objmap.get("ConcenAlarmMaxValue"));
                                codeAndMap.put("concenalarmminvalue", objmap.get("ConcenAlarmMinValue"));
                                map2.put("a_" + objmap.get("FK_AlarmLevelCode").toString(), codeAndMap);
                            }
                        }
                    }
                }
                map2.put("IsException", pollutantmap.get("IsException"));
                //放入污染物code以及value
                mapdata.put(code, map2);
            }
            mapdata.put("pointisstop", pointIsStop);
            mapdata.put("alarmlevel", outPut.get("AlarmLevel"));
            mapdata.put("monitorpointtype", monitorPointType);
            mapdata.put("monitortime", monitorTime);

            if (monitorPointType == AirEnum.getCode()) {
                mapdata.put("aqi", mnAndAqi.get(dgimn));
            }
            if (monitorPointType == WaterQualityEnum.getCode()) {
                mapdata.put("waterlevel", mnAndLevel.get(dgimn));
            }
            if (monitorPointType == EnvironmentalStinkEnum.getCode()) {//恶臭区分敏感点和传输点
                mapdata.put("MonitorPointCategory", outPut.get("MonitorPointCategory"));
                mapdata.put("MonitorPointCategoryName", outPut.get("MonitorPointCategoryName"));
            }
            mapdata.put("rtsplist", idAndRTSP.get(monitorpointid));
            tablelistdata.add(mapdata);
        }
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> tabledata = new HashMap<>();
        //根据监测点类型和mn号获取各个mn号监测的污染物
        List<Map<String, String>> mnPollutant = getMonitorPollutantByParam(mns, monitorPointType);
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
        String statusKey;

        String dataSort = DataFormatUtil.parseProperties("data.sort");
        JSONObject jsonObject = StringUtils.isNotBlank(dataSort) ? JSONObject.fromObject(dataSort) : new JSONObject();

        for (Map<String, Object> map : tablelistdata) {
            String mn = map.get("mn") == null ? "" : map.get("mn").toString();
            map.put("pollutants", mncodes.get(mn) != null ? mncodes.get(mn) : new ArrayList<>());
            if (map.get("onlinestatus") == null) {
                map.replace("onlinestatus", "");
                map.put("orderindex", 10);
            } else {
                String onlineStatus = map.get("onlinestatus").toString();
                if (map.get("pointisstop") != null && Boolean.parseBoolean(map.get("pointisstop").toString())) {
                    onlineStatus = "4";
                }
                statusKey = CommonTypeEnum.StatusOrderSetEnum.getIndexByCode(onlineStatus);
                map.put("orderindex", jsonObject.get(statusKey) != null ? jsonObject.get(statusKey) : 11);
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
                tablelistdata = tablelistdata.stream().sorted(
                        Comparator.comparingInt(
                                m -> ((Map) m).get("PointOrderIndex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("PointOrderIndex").toString())
                        ).thenComparingInt(m -> ((Map) m).get("orderindex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("orderindex").toString()))

                ).collect(Collectors.toList());
            }
        }

        resultMap.put("tablelistdata", tablelistdata);
        resultMap.put("tabletitledata", tabletitledata);
        tabledata.put("tabledata", resultMap);
        tabledata.put("datacount", countData);
        return tabledata;
    }

    private List<Document> getMongoDbDataByParam(Map<String, Object> paramMap) {
        //常规空气展示AQI,水质站点展示水质类别
        String type = paramMap.get("datatype") != null ? paramMap.get("datatype").toString() : "RealTimeData";
        List<String> mns = (List<String>) paramMap.get("mns");
        List<Document> list = new ArrayList<>();
        Bson bson = Filters.and(eq("Type", type), in("DataGatherCode", mns));
        FindIterable<Document> documents = mongoTemplate.getCollection(db_LatestData).find(bson);
        List<Date> times = new ArrayList<>();
        Map<String, Date> mnAndTime = new HashMap<>();
        Date time;
        String mnCommon;
        for (Document document : documents) {
            list.add(document);
            time = document.getDate("MonitorTime");
            mnCommon = document.getString("DataGatherCode");
            mnAndTime.put(mnCommon, time);
            times.add(time);
        }
        Map<String, Object> mnAndAqi = new HashMap<>();
        Map<String, Object> mnAndLevel = new HashMap<>();
        Integer monitorpointtype = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case AirEnum:
                mnAndAqi = getMnAndAqi(mns, times, mnAndTime);
                break;
            case WaterQualityEnum:
                mnAndLevel = getMnAndLevel(mns, times, mnAndTime);
                break;
            default:
        }
        paramMap.put("mnAndAqi", mnAndAqi);
        paramMap.put("mnAndLevel", mnAndLevel);
        return list;

    }

    private Map<String, Object> getMnAndLevel(List<String> mns, List<Date> times, Map<String, Date> mnAndTime) {

        Bson bson = Filters.and(in("DataGatherCode", mns), in("EvaluateTime", times));
        FindIterable<Document> documents = mongoTemplate.getCollection("WaterStationEvaluateData").find(bson);
        Map<String, Object> mnAndLevel = new HashMap<>();
        String mnCommon;
        Date time;
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            time = document.getDate("EvaluateTime");
            if (time.equals(mnAndTime.get(mnCommon))) {
                mnAndLevel.put(mnCommon, document.get("WaterQualityClass"));
            }
        }
        return mnAndLevel;
    }

    private Map<String, Object> getMnAndAqi(List<String> mns, List<Date> times, Map<String, Date> mnAndTime) {

        Bson bson = Filters.and(in("StationCode", mns), in("MonitorTime", times));
        FindIterable<Document> documents = mongoTemplate.getCollection("StationHourAQIData").find(bson);
        Map<String, Object> mnAndAqi = new HashMap<>();
        String mnCommon;
        Date time;
        for (Document document : documents) {
            mnCommon = document.getString("StationCode");
            time = document.getDate("MonitorTime");
            if (time.equals(mnAndTime.get(mnCommon))) {
                mnAndAqi.put(mnCommon, document.get("AQI"));
            }
        }
        return mnAndAqi;
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

    private void getPointOnlineData(List<Document> documents, String dgimn, String code, Map<String, Object> pollutantmap, Integer ishasconvertdata) {
        String monitorTime = "";
        Object value = null;
        Object IsOver = null;
        Object IsException = null;
        boolean IsOverStandard = false;
        Object flag = null;
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
                            if (ishasconvertdata == 1) {
                                value = document1.get("AvgConvertStrength");
                            } else {
                                value = document1.get("AvgStrength");
                            }
                            IsOver = document1.get("IsOver");
                            flag = document1.get("Flag");
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
        pollutantmap.put("flag", flag);
        pollutantmap.put("MonitorTime", monitorTime);
        pollutantmap.put("value", value);
        pollutantmap.put("IsOver", IsOver);
        pollutantmap.put("IsException", IsException);
    }


    @Override
    public List<String> getMNListByParam(Map<String, Object> paramMap) {
        List<String> mns = new ArrayList<>();
        Integer monitorPointType = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        List<Map<String, Object>> outPuts = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
            case WasteWaterEnum:
                paramMap.put("outputtype", "water");
                outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                break;
            case WasteGasEnum:
                outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                break;
            case SmokeEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                break;
            case RainEnum:
                paramMap.put("outputtype", "rain");
                outPuts = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                break;
            case AirEnum:
                outPuts = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                break;
            case WaterQualityEnum:
                outPuts = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                break;
            case EnvironmentalStinkEnum:
            case EnvironmentalDustEnum:
                paramMap.put("monitorPointType", monitorPointType);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case meteoEnum:
                paramMap.put("monitorPointType", monitorPointType);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalVocEnum:
                paramMap.put("monitorPointType", monitorPointType);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                outPuts = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
            case MicroStationEnum:
                paramMap.put("monitorpointtype", monitorPointType);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            default:
                paramMap.put("monitorPointType", monitorPointType);
                outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        }
        Map<String, Object> pointIdAndMn = new HashMap<>();
        String key = "pk_id";
        for (Map<String, Object> outPut : outPuts) {
            if (outPut.get("dgimn") != null) {
                mns.add(outPut.get("dgimn").toString());
                pointIdAndMn.put(outPut.get(key).toString(), outPut.get("dgimn").toString());
            }
        }
        paramMap.put("pointidandmn", pointIdAndMn);
        return mns;
    }

    @Override
    public Map<String, Object> getGasMNListByParam(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        List<String> mns = new ArrayList<>();
        List<Map<String, Object>> outPuts = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
        Map<String, Object> pointIdAndMn = new HashMap<>();
        String key = "pk_id";
        String outputid;
        String name;
        Map<String, String> outPutIdAndPollution = new HashMap<>();
        for (Map<String, Object> outPut : outPuts) {
            if (outPut.get("dgimn") != null) {
                mns.add(outPut.get("dgimn").toString());
                pointIdAndMn.put(outPut.get(key).toString(), outPut.get("dgimn").toString());
            }
            name = "";
            outputid = outPut.get(key).toString();
            if (outPut.get("shortername") != null) {
                name = outPut.get("shortername") + "-";
            }
            name += outPut.get("outputname");
            outPutIdAndPollution.put(outputid, name);
        }
        resultmap.put("mns", mns);
        resultmap.put("idandname", outPutIdAndPollution);
        resultmap.put("pointidandmn", pointIdAndMn);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2022/01/25 0025 上午 10:24
     * @Description: 获取污染物code和name对照关系（废气、烟气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, String> getPollutantCodeAndNameByTypes(List<String> pollutantcodes, List<Integer> monitorpointtypes) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttypes", monitorpointtypes);
        paramMap.put("codes", pollutantcodes);
        Map<String, String> codeAndName = new HashMap<>();
        List<Map<String, Object>> pollutant = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : pollutant) {
            codeAndName.put(map.get("code").toString(), map.get("name").toString());
        }
        return codeAndName;
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
                default:
                    outputkey = "monitorpointname";
                    paramMap.put("monitorPointType", monitorPointType);
                    paramMap.put("outputids", outputids);
                    outPuts = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
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
        String type = paramMap.get("monitorpointtype") != null ? paramMap.get("monitorpointtype").toString() : "";
        if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "")) {//废气
            outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//烟气
            outputs = gasOutPutInfoMapper.getGasOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "")) {//废水
            paramMap.put("outputtype", 1);
            outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "")) {//雨水
            paramMap.put("outputtype", 3);
            outputs = waterOutputInfoMapper.getWaterOutputDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode() + "")) {//厂界小型站
            outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() + "")) {//厂界恶臭
            outputs = unorganizedMonitorPointInfoMapper.getUnorganizedDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode() + "")) {//扬尘
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() + "")) {//空气站
            outputs = airMonitorStationMapper.getAirMonitorStationDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode() + "")) {//水质
            outputs = waterStationMapper.getWaterStationDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() + "")) {//恶臭
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() + "")) {//voc
            outputs = otherMonitorPointMapper.getOtherMonitorPointDgimnAndPollutantInfosByParam(paramMap);
        } else {
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
                    if (map.get("StandardMinValue") != null && map.get("StandardMaxValue") != null) {
                        standardvalue = map.get("StandardMinValue") + "-" + map.get("StandardMaxValue");
                    } else {
                        if (map.get("StandardMinValue") != null) {
                            standardvalue = map.get("StandardMinValue").toString();
                        }
                        if (map.get("StandardMaxValue") != null) {
                            standardvalue = map.get("StandardMaxValue").toString();
                        }
                    }
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
                    if (map.get("StandardMinValue") != null && map.get("StandardMaxValue") != null) {
                        standardvalue = map.get("StandardMinValue") + "-" + map.get("StandardMaxValue");
                    } else {
                        if (map.get("StandardMinValue") != null) {
                            standardvalue = map.get("StandardMinValue").toString();
                        }
                        if (map.get("StandardMaxValue") != null) {
                            standardvalue = map.get("StandardMaxValue").toString();
                        }
                    }
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
     * @date: 2020/6/15 0015 下午 4:27
     * @Description: 获取报表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
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
                case StinkEnum:     //恶臭监测点类型（厂界恶臭+环境恶臭）
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("entmonitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    pollutants = otherMonitorPointMapper.getStinkPollutantSetDataByParam(paramMap);
                    break;
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
                    paramMap.put("pollutanttype", pollutantType);
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
                    paramMap.put("pollutantType", pollutantType);
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
                case EnvironmentalDustEnum:
                    //恶臭
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
                case FactoryBoundarySmallStationEnum:     //厂界小型站
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
                    List<Map<String, Object>> StorageTankPollutantSetsByOutputIds = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
                    for (Map<String, Object> pollutant : StorageTankPollutantSetsByOutputIds) {
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
                case SecurityLeakageMonitor:
                case SecurityCombustibleMonitor:
                case SecurityToxicMonitor:
                    if (titleMap.get("isShowFlow") != null) {
                        paramMap.put("isshowflow", titleMap.get("isShowFlow"));
                    }
                    paramMap.put("outputids", pointIDs);
                    paramMap.put("pollutanttype", pollutantType);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
                    for (Map<String, Object> pollutant : gasOutPutPollutantSetsByOutputIds) {
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
                default:
                    paramMap.put("pollutantType", pollutantType);
                    paramMap.put("outputids", pointIDs);
                    List<Map<String, Object>> pollutantList = pollutantFactorMapper.getOtherPollutantsByParamMap(paramMap);
                    for (Map<String, Object> pollutant : pollutantList) {
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
                } else if (StinkEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(201);
                }
                break;
            case 2: //数据报表
                if (CommonTypeEnum.getOutPutTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(101);
                } else if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(104);
                } else if (CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode() == pointType) {
                    int mark = 102;
                    if (titleMap.get("datamark") != null && Integer.parseInt(titleMap.get("datamark").toString()) > 2) {//小时、日
                        mark = 1021;
                    }
                    tableTitleData = getTableTitleDataForReport(mark);
                } else if (CommonTypeEnum.getMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(102);
                } else if (CommonTypeEnum.getEntMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(103);
                } else if (CommonTypeEnum.getRiskAreaMonitorPointTypeList().contains(pointType)) {
                    tableTitleData = getTableTitleDataForReport(103);
                } else if (StorageTankAreaEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(105);
                } else if (StinkEnum.getCode() == pointType) {
                    tableTitleData = getTableTitleDataForReport(101);
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
        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (documents.size() > 0) {
            //获取mongodb的flag标记
            Map<String,Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes",Arrays.asList(pollutantType));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }


        List<Map<String, Object>> standardDataList;
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndLevelStandardData;
        switch (reportType) {
            case 1:  //实时数据一览
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutantType)) {
                    case StinkEnum: //恶臭监测点（环境恶臭+厂界恶臭）
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case WasteWaterEnum: //废水
                        standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case WasteGasEnum: //废气
                    case SmokeEnum: //烟气
                        standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case RainEnum: //雨水
                        standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case AirEnum: //空气关联站点查询
                        PageEntity<Document> documentsPage = getAirLookupData(paramMap);
                        paramMap.put("total", documentsPage.getTotalCount());
                        paramMap.put("pages", documentsPage.getPageCount());
                        standardDataList = airMonitorStationMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getAirStationTableListData(documentsPage.getListItems(), collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case EnvironmentalVocEnum: //voc
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case WaterQualityEnum: //水质
                        standardDataList = waterStationMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case EnvironmentalStinkEnum: //恶臭
                    case MicroStationEnum://微站
                    case EnvironmentalDustEnum://扬尘
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case meteoEnum: //气象
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case FactoryBoundaryStinkEnum:
                        standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case StorageTankAreaEnum:
                        mnAndCodeAndLevelStandardData = new HashMap<>();
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case SecurityLeakageMonitor:
                    case SecurityCombustibleMonitor:
                    case SecurityToxicMonitor:
                        mnAndCodeAndLevelStandardData = new HashMap<>();
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    default:
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getTableListData(documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);

                }
                break;
            case 2: //数据报表
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutantType)) {

                    case StinkEnum: //恶臭监测点（环境恶臭+厂界恶臭）
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getStinkPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case WasteWaterEnum: //废水
                        standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getWaterPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case WasteGasEnum: //废气
                    case SmokeEnum: //烟气
                        standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getGasPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case RainEnum: //雨水
                        standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getWaterPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case AirEnum: //空气

                        if (paramMap.get("isReal") == null) {
                            PageEntity<Document> documentsPage = getAirLookupData(paramMap);
                            paramMap.put("total", documentsPage.getTotalCount());
                            paramMap.put("pages", documentsPage.getPageCount());
                            documents = documentsPage.getListItems();
                        }
                        standardDataList = airMonitorStationMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getAirPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case EnvironmentalVocEnum: //voc
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getVocPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case WaterQualityEnum: //水质
                        standardDataList = waterStationMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getWaterStationPollutionOutPutTableListData(outputids, documents, collection, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case EnvironmentalStinkEnum: //恶臭
                    case MicroStationEnum://微站
                    case EnvironmentalDustEnum://扬尘
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getOtherPollutionOutPutTableListData(outputids, documents, collection, pollutantType, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case meteoEnum: //气象
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getOtherPollutionOutPutTableListData(outputids, documents, collection, pollutantType, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case FactoryBoundaryStinkEnum:
                        standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getUNGasPollutionOutPutTableListData(outputids, documents, collection, pollutantType, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    case FactoryBoundarySmallStationEnum:
                        standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getUNGasPollutionOutPutTableListData(outputids, documents, collection, pollutantType, mnAndCodeAndLevelStandardData, flag_codeAndName);
                        break;
                    default:
                        standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                        mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                        tableListData = getOtherPollutionOutPutTableListData(outputids, documents, collection, pollutantType, mnAndCodeAndLevelStandardData, flag_codeAndName);
                }
                break;
        }

        return tableListData;
    }

    private Map<String, Map<String, Map<String, Object>>> setMnAndCodeAndLevelAndStandardData(List<Map<String, Object>> standardDataList) {
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndLevelAndStandardData = new HashMap<>();
        Map<String, Map<String, Object>> codeAndLevelAndStandardData;
        Map<String, Object> levelAndStandardData;
        if (standardDataList.size() > 0) {
            String mnCommon;
            String pollutantCode;
            String alarmtype;
            String standardminvalue;
            String standardmaxvalue;
            String alarmleveltype;
            String standardDataString;
            for (Map<String, Object> standardData : standardDataList) {
                if (standardData.get("dgimn") != null && standardData.get("alarmleveltype") != null) {
                    mnCommon = standardData.get("dgimn").toString();
                    if (mnAndCodeAndLevelAndStandardData.containsKey(mnCommon)) {
                        codeAndLevelAndStandardData = mnAndCodeAndLevelAndStandardData.get(mnCommon);
                    } else {
                        codeAndLevelAndStandardData = new HashMap<>();
                    }
                    pollutantCode = standardData.get("pollutantcode").toString();
                    standardminvalue = standardData.get("standardminvalue") != null ? standardData.get("standardminvalue").toString() : "-";
                    alarmleveltype = standardData.get("alarmleveltype").toString();
                    standardmaxvalue = standardData.get("standardmaxvalue") != null ? standardData.get("standardmaxvalue").toString() : "-";
                    alarmtype = standardData.get("alarmtype") != null ? standardData.get("alarmtype").toString() : null;
                    if (alarmtype != null) {
                        if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype)) {//上限报警
                            standardDataString = standardmaxvalue;
                        } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype)) {//下限报警
                            standardDataString = standardminvalue;
                        } else {
                            standardDataString = standardminvalue + "," + standardmaxvalue;
                        }
                        if (codeAndLevelAndStandardData.containsKey(pollutantCode)) {
                            levelAndStandardData = codeAndLevelAndStandardData.get(pollutantCode);
                        } else {
                            levelAndStandardData = new HashMap<>();
                        }
                        levelAndStandardData.put(alarmleveltype, standardDataString);
                        codeAndLevelAndStandardData.put(pollutantCode, levelAndStandardData);
                        mnAndCodeAndLevelAndStandardData.put(mnCommon, codeAndLevelAndStandardData);
                    }


                }
            }
        }
        return mnAndCodeAndLevelAndStandardData;
    }

    private List<Map<String, Object>> getUNGasPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Integer pollutantType, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
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
        String standardData;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : gasOutputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("shortername", map.get("shortername"));
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


                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn)
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            , pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
    }

    private List<Map<String, Object>> getOtherPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Integer pollutantType, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
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
        String standardData;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            if ("RealTimeData".equals(collection)) {
                listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            } else if ("MinuteData".equals(collection)) {
                listDataMap.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime")));
            } else if ("HourData".equals(collection)) {
                listDataMap.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
            } else if ("DayData".equals(collection)) {
                listDataMap.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
            } else if ("MonthData".equals(collection)) {
                listDataMap.put("monitortime", DataFormatUtil.getDateYM(document.getDate("MonitorTime")));
            } else {
                listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            }
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

                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            , pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;

    }

    private List<Map<String, Object>> getWaterStationPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
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
        String standardData;
        String waterLevel;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            listDataMap.put("waterlevel", document.get("WaterLevel") != null ? document.get("WaterLevel") + "类" : "");
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

                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {


                    waterLevel = pollutant.get("WaterLevel") != null ? pollutant.get("WaterLevel")+"类" : "";

                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            +"#"+waterLevel
                            , pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;

    }

    private List<Map<String, Object>> getVocPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
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
        String standardData;
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
                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            , pollutant.get(valueKey));
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
    private List<Map<String, Object>> getAirPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
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
        String standardData;
        boolean isOverStandard;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            if (document.get("StationData") != null) {
                stationDataList = (List<Document>) document.get("StationData");
                for (Document station : stationDataList) {
                    if (station.get("StationCode").equals(document.get("DataGatherCode"))) {
                        listDataMap.put("aqi", station.get("AQI"));
                        break;
                    }
                }
            }

            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : outputs) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("monitorpointname", map.get("monitorpointname"));
                    break;
                }
            }
            pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";

                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }

                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : ""),
                            pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
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
    private List<Map<String, Object>> getGasPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
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
        String standardData;
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
                    listDataMap.put("outputid", map.get("pk_id"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {

                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            , pollutant.get(valueKey));
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
     * @Description: 恶臭（环境恶臭+厂界恶臭）组装表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        paramMap.put("monitortypecode", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        paramMap.put("entmonitortypecode", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        List<Map<String, Object>> pointDataList = otherMonitorPointMapper.getStinkPointDataByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String isSuddenChange;
        String standardData;
        boolean isOverStandard;
        String pollutantCode;
        for (Document document : documents) {
            Map<String, Object> listDataMap = new HashMap<>();
            listDataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            mn = document.get("DataGatherCode").toString();
            for (Map<String, Object> map : pointDataList) {
                if (mn.equals(map.get("dgimn"))) {
                    listDataMap.put("shortername", map.get("shortername"));
                    listDataMap.put("outputname", map.get("monitorpointname"));
                    listDataMap.put("outputid", map.get("pk_id"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }

                    listDataMap.put(pollutantCode
                                    + "#" + isOver
                                    + "#" + isException
                                    + "#" + isSuddenChange
                                    + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : ""),
                            pollutant.get(valueKey));
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
    private List<Map<String, Object>> getWaterPollutionOutPutTableListData(List<String> outputids, List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", outputids);
        List<Map<String, Object>> waterOutputs = waterOutputInfoMapper.getWaterOutPutsByParamMap(paramMap);
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String mn = "";
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String standardData;
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
                    listDataMap.put("outputid", map.get("outputid"));
                    break;
                }
            }
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mn) && mnAndCodeAndStandardData.get(mn).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mn).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData

                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "")
                            , pollutant.get(valueKey));
                }
            }
            tableListData.add(listDataMap);
        }
        return tableListData;
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
    private List<Map<String, Object>> getAirStationTableListData(List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData, Map<String, Object> flag_codeAndName) {
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);

        List<Document> pollutantDataList;
        List<Document> stationDataList;
        String isOver;
        String isException;
        String isSuddenChange;
        String mnCommon;
        String standardData;
        boolean isOverStandard;
        String pollutantCode;
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
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
                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                if (pollutantCode != null) {
                    if (mnAndCodeAndStandardData.containsKey(mnCommon) && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode) != null &&
                            mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode
                                    + "#" + isOver
                                    + "#" + isException
                                    + "#" + isSuddenChange
                                    + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : ""),
                            pollutant.get(valueKey));
                }

            }
            tableListData.add(listDataMap);
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

        if (paramMap.get("leftCollection") != null) {
            String leftCollection = paramMap.get("leftCollection").toString();
            LookupOperation lookupOperation = LookupOperation.newLookup().from(leftCollection).localField("MonitorTime").foreignField("MonitorTime").as("StationData");
            operations.add(lookupOperation);
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
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;

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

    private List<Map<String, Object>> getTableListData(List<Document> documents, String collection, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndLevelAndStandardData, Map<String, Object> flag_codeAndName) {
        List<Map<String, Object>> tableListData = new ArrayList<>();
        String pollutantDataKey = getPollutantDataKey(collection);
        String valueKey = getValueKey(collection);
        String isOver;
        String isException;
        String pollutantCode;
        String isSuddenChange;

        String standardData;
        String mnCommon;
        boolean isOverStandard;
        for (Document document : documents) {

            mnCommon = document.getString("DataGatherCode");
            Map<String, Object> listDataMap = new HashMap<>();
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                isOverStandard = pollutant.getBoolean("IsOverStandard");
                if (isOverStandard) {
                    isOver = "4";
                }
                if (!"true".equals(isSuddenChange)) {
                    isSuddenChange = "false";
                }
                if (pollutantCode != null) {
                    if (mnAndCodeAndLevelAndStandardData.containsKey(mnCommon) &&
                            mnAndCodeAndLevelAndStandardData.get(mnCommon).get(pollutantCode) != null &&
                            mnAndCodeAndLevelAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) != null
                    ) {
                        standardData = mnAndCodeAndLevelAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    listDataMap.put(pollutantCode
                                    + "#" + isOver
                                    + "#" + isException
                                    + "#" + isSuddenChange
                                    + "#" + standardData
                                    + "#" + flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : ""),
                            pollutant.get(valueKey));
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

    private String getZSValueKey(String collection) {
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            valueKey = "ConvertConcentration";
        } else if ("MinuteData".equals(collection)) {
            valueKey = "AvgConvertStrength";
        } else if ("HourData".equals(collection)) {
            valueKey = "AvgConvertStrength";
        } else if ("DayData".equals(collection)) {
            valueKey = "AvgConvertStrength";
        }
        return valueKey;
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
            case 1021://水质
                props.add("monitorpointname");
                props.add("monitortime");
                props.add("waterlevel");
                labels.add("监测点名称");
                labels.add("监测时间");
                labels.add("水质类别");
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

        }
        for (int i = 0; i < props.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            if (props.get(i).equals("monitortime")) {
                map.put("minwidth", "180px");
            }
            if (props.get(i).equals("outputname")) {
                map.put("minwidth", "180px");
            }
            if (props.get(i).equals("shortername") || props.get(i).equals("monitorpointname")) {
                map.put("minwidth", "180px");
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

        if (paramMap.get("isoverstandard") != null) {
            if ("RealTimeData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("RealDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("MinuteData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("MinuteDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("HourData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("DayData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            }
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime", "DataGatherCode"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime", "DataGatherCode"));
        }
        return query;
    }


    @Override
    public List<Document> getMonitorDataByParamMap(Map<String, Object> paramMap) {
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
        if (paramMap.get("isoverstandard") != null) {
            if ("RealTimeData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("RealDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("MinuteData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("MinuteDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("HourData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("DayData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
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
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
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
            case XKLWEnum:
            case TZFEnum:
                maps = otherMonitorPointMapper.getMonitorPollutantByParam(mns);
                break;
            case WaterQualityEnum:
                maps = waterStationMapper.getMonitorPollutantByParam(mns);
                break;
        }
        return maps;
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
     * @author: zhangzc
     * @date: 2019/5/28 14:04
     * @Description: 组件污染物表头
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPollutantTableTitleData(List<Map<String, Object>> pollutants) {
        List<Map<String, Object>> maps = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        for (Map<String, Object> pollutant : pollutants) {
            if (pollutant.get("code") != null && !codes.contains(pollutant.get("code").toString())) {
                codes.add(pollutant.get("code").toString());
                Map<String, Object> map = new HashMap<>();
                map.put("headeralign", "center");
                map.put("width", "120px");
                map.put("showhide", true);
                map.put("prop", pollutant.get("code"));
                map.put("orderindex", pollutant.get("orderindex") != null ? pollutant.get("orderindex") : 9999);
                String label = pollutant.get("name").toString();
                if (pollutant.get("unit") != null && pollutant.get("unit") != "") {
                    label += "(" + pollutant.get("unit").toString() + ")";
                }
                map.put("label", label);
                map.put("type", "contaminated");
                map.put("align", "center");
                maps.add(map);
            }
        }
        //排序

        maps = maps.stream().sorted(
                Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString()))
        ).collect(Collectors.toList());
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
                resultMap.put("monitorpointtype", Integer.parseInt(outPut.get("monitorpointtype") + ""));
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
                resultMap.put("controllevelname", outPut.get("controllevelname"));
                resultMap.put("controllevelcode", outPut.get("FK_ControlLevelCode"));
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
                resultMap.put("PointOrderIndex", outPut.get("OrderIndex"));
                break;
            //恶臭监测点
            case EnvironmentalStinkEnum:
            case MicroStationEnum://微站
            case EnvironmentalDustEnum://扬尘
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                resultMap.put("PointOrderIndex", outPut.get("OrderIndex"));
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
            case SecurityLeakageMonitor:
            case SecurityCombustibleMonitor:
            case SecurityToxicMonitor:
                resultMap.put("shortername", outPut.get("shortername"));//企业名称显示企业简称字段的值
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("pollutionid", outPut.get("pk_pollutionid"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                break;
            default:
                resultMap.put("monitorpointname", outPut.get("monitorpointname"));
                resultMap.put("outputid", outPut.get("pk_id"));
                resultMap.put("mn", outPut.get("dgimn"));
                resultMap.put("onlinestatus", outPut.get("onlinestatus"));
                resultMap.put("PointOrderIndex", outPut.get("OrderIndex"));
        }
        return resultMap;

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
    private Map<String, List<Map<String, Object>>> getRTSPData(List<Integer> pointTypes) {

        Map<String, Object> paramMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> idAndList = new HashMap<>();
        List<Map<String, Object>> rtspList;
        paramMap.put("monitorpointtypes", pointTypes);
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
                props.add("onlinestatus");
                props.add("monitortime");
                props.add("aqi");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                labels.add("AQI");
                break;
            case WaterQualityEnum:
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                props.add("waterlevel");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                labels.add("水质类别");
                break;
            case EnvironmentalVocEnum:
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case EnvironmentalStinkEnum:
            case MicroStationEnum:
            case EnvironmentalDustEnum://扬尘
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            case meteoEnum:
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
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
                labels.add("状态");
                labels.add("监测时间");
                break;
            case SecurityLeakageMonitor:
            case SecurityCombustibleMonitor:
            case SecurityToxicMonitor:
                props.add("shortername");
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("企业名称");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
                break;
            default:
                props.add("monitorpointname");
                props.add("onlinestatus");
                props.add("monitortime");
                labels.add("监测点名称");
                labels.add("状态");
                labels.add("监测时间");
        }
        for (int i = 0; i < props.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            if (props.get(i).equals("onlinestatus")) {
                map.put("type", "element");
                map.put("width", "100px");
            }
            if (props.get(i).equals("monitortime")) {
                map.put("width", "165px");
            }
            if (props.get(i).equals("outputname")) {
                map.put("minwidth", "200px");
            }
            if (props.get(i).equals("shortername") || props.get(i).equals("monitorpointname")) {
                map.put("minwidth", "200px");
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
     * @author: xsm
     * @date: 2020/09/05 0005 上午 9:14
     * @Description: 根据监测类型获取该类型下所有监测点位最近小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorPointLastHourDataByParamMap(List<Map<String, Object>> outPutInfosByParamMap, List<Map<String, Object>> pollutantlist, String monitortime) {
        List<Map<String, Object>> hourlist = new ArrayList<>();
        List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
        List<AggregationOperation> aggregations = new ArrayList<>();
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "maxtime", "MonitorTime"));
        GroupOperation groupOperation = group("DataGatherCode").max("MonitorTime").as("maxtime");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        String hourtime = "";
        List<String> mns = new ArrayList<>();
        if (resultDocument.size() > 0) {
            //通过最大小时时间分组数据
            Map<String, List<Document>> mapDocuments = resultDocument.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHMS((Date) m.get("maxtime"))));
            for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                hourtime = entry.getKey();
                List<Document> docs = entry.getValue();
                for (Document doc : docs) {
                    mns.add(doc.getString("_id"));
                }
            }
        }
        hourlist.addAll(getHourDataAndAlarmStatus(dgimns, mns, hourtime, outPutInfosByParamMap, pollutantlist));
        return hourlist;
    }

    private List<Map<String, Object>> getHourDataAndAlarmStatus(List<String> dgimns, List<String> mns, String hourtime, List<Map<String, Object>> outPutInfosByParamMap, List<Map<String, Object>> pollutantlist) {
        List<Map<String, Object>> hourlist = new ArrayList<>();
        Map<String, String> codeAndName = new HashMap<>();
        Map<String, Object> codeAndunit = new HashMap<>();
        Set<String> codesets = new HashSet();
        for (Map<String, Object> map : pollutantlist) {
            if (!codesets.contains(map.get("code").toString())) {
                codeAndName.put(map.get("code").toString(), map.get("name").toString());
                codeAndunit.put(map.get("code").toString(), map.get("unit"));
                codesets.add(map.get("code").toString());
            }
        }
        Date startDate = DataFormatUtil.getDateYMDHMS(hourtime);
        Date endDate = DataFormatUtil.getDateYMDHMS(hourtime);
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, "HourData");
        List<Map> earlyWarn = new ArrayList<>();
        List<Map> exception = new ArrayList<>();
        List<Map> overdata = new ArrayList<>();
        //超阈
        Criteria criteria1 = new Criteria();
        criteria1.and("DataGatherCode").in(mns).and("EarlyWarnTime").gte(startDate).lte(endDate).and("DataType").is("HourData");
        List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria1), group("DataGatherCode").count().as("count")), "EarlyWarnData", Map.class).getMappedResults();
        earlyWarn.addAll(early);
        //异常
        Criteria criteria2 = new Criteria();
        criteria2.and("DataGatherCode").in(mns).and("ExceptionTime").gte(startDate).lte(endDate).and("DataType").is("HourData");
        List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria2), group("DataGatherCode").count().as("count")), "ExceptionData", Map.class).getMappedResults();
        exception.addAll(exceptiondata);
        //超限报警
        Criteria criteria3 = new Criteria();
        criteria3.and("DataGatherCode").in(mns).and("OverTime").gte(startDate).lte(endDate).and("DataType").is("HourData");
        List<Map> over = mongoTemplate.aggregate(newAggregation(match(criteria3), group("DataGatherCode").count().as("count")), "OverData", Map.class).getMappedResults();
        overdata.addAll(over);
        for (String mn : dgimns) {
            for (Map<String, Object> pointmap : outPutInfosByParamMap) {
                if (pointmap.get("DGIMN") != null && mn.equals(pointmap.get("DGIMN").toString())) {
                    if (pointmap.get("FK_MonitorPointTypeCode") != null && !"".equals(pointmap.get("FK_MonitorPointTypeCode").toString())) {
                        pointmap.put("FK_MonitorPointTypeCode", Integer.parseInt(pointmap.get("FK_MonitorPointTypeCode").toString()));
                    }
                    pointmap.put("MonitorTime", "");
                    pointmap.put("pollutantdata", null);
                    pointmap.put("earlywarn", false);//包含浓度突变，排放量突变，预警
                    pointmap.put("exception", false);
                    pointmap.put("overdata", false);
                    boolean havedata = false;
                    for (Document document : documents) {
                        String onemn = document.getString("DataGatherCode");
                        if (mn.equals(onemn)) {//当MN相等
                            havedata = true;
                            List<Map<String, Object>> pollutantdata = (List<Map<String, Object>>) document.get("HourDataList");
                            List<Map<String, Object>> onepollutants = new ArrayList<>();
                            for (Map<String, Object> map : pollutantdata) {
                                String code = map.get("PollutantCode").toString();
                                Object value = map.get("AvgStrength");
                                Map<String, Object> onemap = new HashMap<>();
                                if (codeAndName.get(code) != null) {
                                    onemap.put("pollutantcode", code);
                                    onemap.put("unit", codeAndunit.get(code));
                                    onemap.put("pollutantname", codeAndName.get(code));
                                    Object IsOver = map.get("IsOver");
                                    boolean IsOverStandard = map.get("IsOverStandard") != null ? Boolean.parseBoolean(map.get("IsOverStandard").toString()) : false;
                                    if (IsOverStandard) {
                                        IsOver = "4";
                                    }
                                    if (Integer.parseInt(IsOver.toString()) > -1) {
                                        onemap.put("isover", true);
                                    } else {
                                        onemap.put("isover", false);
                                    }
                                    if (map.get("IsException") != null && Integer.parseInt(map.get("IsException").toString()) > 0) {
                                        onemap.put("isexception", true);
                                    } else {
                                        onemap.put("isexception", false);
                                    }
                                    onemap.put("issuddenchange", map.get("IsSuddenChange"));
                                    onemap.put("value", value);
                                    onepollutants.add(onemap);

                                }
                            }
                            pointmap.put("MonitorTime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                            pointmap.put("pollutantdata", onepollutants);
                            break;
                        }
                    }
                    if (havedata == false) {
                        List<Map<String, Object>> onepollutants = new ArrayList<>();
                        for (String codestr : codesets) {
                            Map<String, Object> onemap = new HashMap<>();
                            onemap.put("pollutantcode", codestr);
                            onemap.put("unit", codeAndunit.get(codestr));
                            onemap.put("pollutantname", codeAndName.get(codestr));
                            onemap.put("isover", false);
                            onemap.put("isexception", false);
                            onemap.put("issuddenchange", false);
                            onemap.put("value", "-");
                            onepollutants.add(onemap);
                            pointmap.put("MonitorTime", hourtime);
                            pointmap.put("pollutantdata", onepollutants);
                        }
                    }
                    for (Map<String, Object> datum : earlyWarn) {
                        String _id = datum.get("_id") == null ? "" : datum.get("_id").toString();
                        Integer count = datum.get("count") == null ? 0 : Integer.valueOf(datum.get("count").toString());
                        if (_id.equals(mn) && count > 0) {
                            pointmap.put("earlywarn", true);
                        }
                    }
                    for (Map<String, Object> datum : exception) {
                        String _id = datum.get("_id") == null ? "" : datum.get("_id").toString();
                        Integer count = datum.get("count") == null ? 0 : Integer.valueOf(datum.get("count").toString());
                        if (_id.equals(mn) && count > 0) {
                            pointmap.put("exception", true);
                        }
                    }
                    for (Map<String, Object> datum : overdata) {
                        String _id = datum.get("_id") == null ? "" : datum.get("_id").toString();
                        Integer count = datum.get("count") == null ? 0 : Integer.valueOf(datum.get("count").toString());
                        if (_id.equals(mn) && count > 0) {
                            pointmap.put("overdata", true);
                        }
                    }
                    hourlist.add(pointmap);
                    break;
                }

            }
        }
        return hourlist;
    }

    @Override
    public List<Map> countOverDataByParams(Map<String, Object> paramMap) {
        List<String> mns = paramMap.get("mns") == null ? new ArrayList<>() : (List<String>) paramMap.get("mns");
        String startDate = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
        String endDate = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();

        //超限报警
        Criteria criteria3 = new Criteria();
        criteria3.and("DataGatherCode").in(mns).and("OverTime").gte(DataFormatUtil.getDateYMDHMS(startDate)).lte(DataFormatUtil.getDateYMDHMS(endDate));
        List<Map> over = mongoTemplate.aggregate(newAggregation(match(criteria3), group("DataGatherCode").count().as("count")), "OverData", Map.class).getMappedResults();
        return over;
    }

    @Override
    public Map<String, Object> getEarlyAndOverDataGroupMnAndByTime(List<String> mns, List<String> pollutantcodes, Integer datamark, String starttime, String endtime) {
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        Map<String, Object> onlinemap = new HashMap<>();
        Map<String, Object> earlymap = new HashMap<>();
        Map<String, Object> overmap = new HashMap<>();
        String DataType = "";
        if (datamark == 1) {
            DataType = "RealTimeData";
        } else if (datamark == 2) {
            DataType = "MinuteData";
        } else if (datamark == 3) {
            DataType = "HourData";
        } else if (datamark == 4) {
            DataType = "DayData";
        }
        if (!"".equals(DataType)) {
            //超阈
            Criteria criteria1 = new Criteria();
            criteria1.and("DataGatherCode").in(mns).and("EarlyWarnTime").gte(startDate).lte(endDate).and("PollutantCode").in(pollutantcodes).and("DataType").is(DataType);
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria1)), "EarlyWarnData", Map.class).getMappedResults();
            //超限报警
            Criteria criteria3 = new Criteria();
            criteria3.and("DataGatherCode").in(mns).and("OverTime").gte(startDate).lte(endDate).and("PollutantCode").in(pollutantcodes).and("DataType").is(DataType);
            List<Map> over = mongoTemplate.aggregate(newAggregation(match(criteria3)), "OverData", Map.class).getMappedResults();
            for (String mn : mns) {
                for (String code : pollutantcodes) {
                    if (early.size() > 0) {
                        List<String> times = new ArrayList<>();
                        for (Map map : early) {
                            String MonitorTime = "";
                            if (datamark == 1) {
                                MonitorTime = DataFormatUtil.getDateYMDHMS((Date) map.get("EarlyWarnTime"));
                            } else if (datamark == 2) {
                                MonitorTime = DataFormatUtil.getDateYMDHM((Date) map.get("EarlyWarnTime"));
                            } else if (datamark == 3) {
                                MonitorTime = DataFormatUtil.getDateYMDH((Date) map.get("EarlyWarnTime"));
                            } else if (datamark == 4) {
                                MonitorTime = DataFormatUtil.getDateYMD((Date) map.get("EarlyWarnTime"));
                            }
                            if (mn.equals(map.get("DataGatherCode").toString()) && code.equals(map.get("PollutantCode").toString())) {
                                times.add(MonitorTime);
                            }
                        }
                        earlymap.put(mn + "_" + code, times);
                    }
                    if (over.size() > 0) {
                        List<String> times = new ArrayList<>();
                        for (Map map : over) {
                            String MonitorTime = "";
                            if (datamark == 1) {
                                MonitorTime = DataFormatUtil.getDateYMDHMS((Date) map.get("OverTime"));
                            } else if (datamark == 2) {
                                MonitorTime = DataFormatUtil.getDateYMDHM((Date) map.get("OverTime"));
                            } else if (datamark == 3) {
                                MonitorTime = DataFormatUtil.getDateYMDH((Date) map.get("OverTime"));
                            } else if (datamark == 4) {
                                MonitorTime = DataFormatUtil.getDateYMD((Date) map.get("OverTime"));
                            }
                            if (mn.equals(map.get("DataGatherCode").toString()) && code.equals(map.get("PollutantCode").toString())) {
                                times.add(MonitorTime);
                            }
                        }
                        overmap.put(mn + "_" + code, times);
                    }
                }
            }
        }
        onlinemap.put("early", earlymap);
        onlinemap.put("over", overmap);
        return onlinemap;
    }

    @Override
    public void getAllMonitorPointLastRealTimeData(List<Map<String, Object>> result, Date startdate, Date enddate, String code) {
        List<String> dgimns = result.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
        Criteria criteria = new Criteria();
        List<AggregationOperation> operations = new ArrayList<>();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startdate).lte(enddate);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", "RealDataList", "MonitorTime"));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
        operations.add(group("DataGatherCode").first("RealDataList").as("RealDataList").first("MonitorTime").as("MonitorTime"));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, "RealTimeData", Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        if (documents.size() > 0) {
            for (Map<String, Object> map : result) {
                String mn = map.get("DGIMN").toString();
                int IsOver = 0;
                int IsException = 0;
                Object value = null;
                boolean IsOverStandard = false;
                map.put("monitorvalue", "-");
                for (Document document : documents) {
                    if (mn.equals(document.getString("_id"))) {
                        List<Document> documents1 = (List<Document>) document.get("RealDataList");
                        for (Document document1 : documents1) {
                            if (code.equals(document1.getString("PollutantCode"))) {
                                value = document1.get("MonitorValue");
                                IsOver = document1.getInteger("IsOver");
                                IsOverStandard = document1.get("IsOverStandard") != null ? document1.getBoolean("IsOverStandard") : false;
                                if (IsOverStandard) {
                                    IsOver = 4;
                                }
                                IsException = document1.getInteger("IsException");
                            }
                        }
                    }
                }
                if (IsException > 0) {
                    map.put("Status", 3);
                } else {
                    if (IsOver > 0) {
                        map.put("Status", 2);
                    }
                }
                map.put("monitorvalue", value);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getSmokeTableTitleByParam(Map<String, Object> titleMap) {
        String tableType = titleMap.get("tableType").toString();
        List<Map<String, Object>> titleDataList = new ArrayList<>();

        switch (tableType) {
            case "one":
                titleDataList = getTableTitleDataForReport(201);
                break;
            case "many":
                titleDataList = getTableTitleDataForReport(101);
                break;
        }
        if (titleDataList.size() > 0) {
            Map<String, Object> commonMap = new HashMap<>();
            commonMap.put("showhide", true);
            commonMap.put("align", "center");
            titleMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            String pollutantName;
            String pollutantCode;
            String pollutantUnit;
            Integer IsHasConvertData;
            List<Map<String, Object>> pollutantList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(titleMap);

            Set<String> codes = new HashSet<>();

            for (Map<String, Object> pollutant : pollutantList) {
                pollutantCode = pollutant.get("pollutantcode").toString();
                if (!codes.contains(pollutantCode)) {
                    codes.add(pollutantCode);
                    pollutantName = pollutant.get("pollutantname").toString();
                    pollutantUnit = pollutant.get("PollutantUnit").toString();
                    IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
                    Map<String, Object> parentTitle = new HashMap<>();
                    parentTitle.putAll(commonMap);

                    if (IsHasConvertData > 0) {
                        parentTitle.put("label", pollutantName);
                        parentTitle.put("prop", pollutantCode + "_par");
                        List<Map<String, Object>> children = new ArrayList<>();
                        Map<String, Object> sc = new HashMap<>();
                        sc.putAll(commonMap);
                        sc.put("prop", pollutantCode + "_sc");
                        if (StringUtils.isNotBlank(pollutantUnit)) {
                            sc.put("label", "实测值（" + pollutantUnit + "）");
                        } else {
                            sc.put("label", "实测值");
                        }
                        children.add(sc);
                        Map<String, Object> zs = new HashMap<>();
                        zs.putAll(commonMap);
                        zs.put("prop", pollutantCode + "_zs");
                        if (StringUtils.isNotBlank(pollutantUnit)) {
                            zs.put("label", "折算值（" + pollutantUnit + "）");
                        } else {
                            zs.put("label", "折算值");
                        }
                        children.add(zs);
                        parentTitle.put("children", children);
                    } else {
                        parentTitle.put("label", pollutantName + "（" + pollutantUnit + "）");
                        parentTitle.put("prop", pollutantCode + "_par");
                    }
                    titleDataList.add(parentTitle);
                }

            }
        }
        return titleDataList;
    }

    @Override
    public List<Map<String, Object>> getSmokeTableListDataByParam(Map<String, Object> paramMap) {
        String tableType = paramMap.get("tableType").toString();
        List<Map<String, Object>> tableDataList = new ArrayList<>();
        Query query = setNoGroupQuery(paramMap);
        String collection = paramMap.get("collection").toString();
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (documents.size() > 0) {
            //获取mongodb的flag标记
            Map<String,Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes",Arrays.asList(paramMap.get("monitorpointtype")));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }


        paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());


        List<Map<String, Object>> standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);

        List<Map<String, Object>> pollutantList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
        String pollutantCode;
        Integer IsHasConvertData;
        Map<String, Integer> codeAndIs = new HashMap<>();
        for (Map<String, Object> pollutant : pollutantList) {
            IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
            pollutantCode = pollutant.get("pollutantcode").toString();
            codeAndIs.put(pollutantCode, IsHasConvertData);
        }
        List<Document> pollutants;
        String pollutantDataKey;
        String scValueKey;
        String zsValueKey;
        String isOver;
        Object flag;
        String isException;
        String isSuddenChange;
        boolean isOverStandard;
        Map<String, Object> mnAndPName = new HashMap<>();
        Map<String, Object> mnAndName = new HashMap<>();
        Map<String, Object> mnAndId = new HashMap<>();
        String mnCommon;
        String standardData;
        if (tableType.equals("many")) {
            List<Map<String, Object>> outputList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
            for (Map<String, Object> output : outputList) {
                mnCommon = output.get("dgimn").toString();
                mnAndPName.put(mnCommon, output.get("shortername"));
                mnAndName.put(mnCommon, output.get("outputname"));
                mnAndId.put(mnCommon, output.get("pk_id"));
            }
        }
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            Map<String, Object> dataMap = new HashMap<>();
            if (mnAndName.containsKey(mnCommon)) {
                dataMap.put("shortername", mnAndPName.get(mnCommon));
                dataMap.put("outputname", mnAndName.get(mnCommon));
                dataMap.put("outputid", mnAndId.get(mnCommon));
            }
            dataMap.put("monitortime", getFormatTimeData(document.getDate("MonitorTime"), collection));
            pollutantDataKey = getPollutantDataKey(collection);
            scValueKey = getValueKey(collection);
            zsValueKey = getZSValueKey(collection);
            pollutants = document.get(pollutantDataKey, List.class);
            for (Document pollutant : pollutants) {
                pollutantCode = pollutant.getString("PollutantCode");
                if (codeAndIs.containsKey(pollutantCode)) {
                    isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                    isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                    pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                    isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                    isOverStandard = pollutant.getBoolean("IsOverStandard");
                    if (isOverStandard) {
                        isOver = "4";
                    }
                    if (!"true".equals(isSuddenChange)) {
                        isSuddenChange = "false";
                    }
                    if (mnAndCodeAndStandardData.containsKey(mnCommon) && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    flag = flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "");
                    if (codeAndIs.get(pollutantCode) > 0) {
                        dataMap.put(pollutantCode + "_sc" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                + "#" + flag, pollutant.get(scValueKey));
                        dataMap.put(pollutantCode + "_zs" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                + "#" + flag, pollutant.get(zsValueKey));
                    } else {
                        dataMap.put(pollutantCode + "_par" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                + "#" + flag, pollutant.get(scValueKey));
                    }
                }
            }
            tableDataList.add(dataMap);
        }
        return tableDataList;
    }

    @Override
    public List<Document> getOverDataByParam(Map<String, Object> paramMap) {

        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                query.addCriteria(Criteria.where("OverTime").gte(startDate));
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("OverTime").lte(endDate));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("PollutantCode").in(pollutantcodes));
        }
        if (paramMap.get("DataType") != null) {
            query.addCriteria(Criteria.where("DataType").is(paramMap.get("DataType")));
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "OverTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "OverTime"));
        }
        return mongoTemplate.find(query, Document.class, db_OverData);

    }

    private String getFormatTimeData(Date monitorTime, String collection) {
        String time = "";
        if ("RealTimeData".equals(collection)) {
            time = DataFormatUtil.getDateYMDHMS(monitorTime);
        } else if ("MinuteData".equals(collection)) {
            time = DataFormatUtil.getDateYMDHM(monitorTime);
        } else if ("HourData".equals(collection)) {
            time = DataFormatUtil.getDateYMDH(monitorTime);
        } else if ("DayData".equals(collection)) {
            time = DataFormatUtil.getDateYMD(monitorTime);
        }
        return time;
    }

    @Override
    public void getOutPutPointLastOnlineDataByParamForApp(List<Map<String, Object>> result, List<Map<String, Object>> onlineOutPuts, Integer type, String datatype) {
        List<Map<String, Object>> outPutInfos = onlineOutPuts.stream().distinct().collect(Collectors.toList());
        //mn号
        Map<String, Object> codeandname = new HashMap<>();
        Map<String, Object> codeandunit = new HashMap<>();
        List<String> mns = new ArrayList<>();
        Set<String> sets = new HashSet<>();
        List<String> codes = new ArrayList<>();
        List<String> smokemns = new ArrayList<>();
        for (Map<String, Object> outPut : outPutInfos) {
            if (outPut.get("dgimn") != null) {
                mns.add(outPut.get("dgimn").toString());
                if (outPut.get("monitorpointtype") != null && (outPut.get("monitorpointtype").toString()).equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {
                    smokemns.add(outPut.get("dgimn").toString());
                }
            }
        }
        //获取污染物信息
        Map<String, Object> paramMap = new HashMap<>();
        //获取各个排口监测的污染物数据
        //根据监测点类型和mn号获取各个mn号监测的污染物
        List<Map<String, String>> mnPollutant = new ArrayList<>();
        List<Map<String, Object>> keyPollutants = new ArrayList<>();
        //获取烟气类型 有设置折算值的污染物
        List<String> smokes = new ArrayList<>();
        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
            mnPollutant.addAll(getMonitorPollutantByParam(mns, CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()));
            mnPollutant.addAll(getMonitorPollutantByParam(mns, CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()));
            //获取该类型主要污染物
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
            keyPollutants.addAll(pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(paramMap));
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            List<Map<String, Object>> smokepollutants = pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(paramMap);
            keyPollutants.addAll(smokepollutants);
            if (smokepollutants != null && smokepollutants.size() > 0) {
                for (Map<String, Object> map : smokepollutants) {
                    //获取烟气折算污染物
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        smokes.add(map.get("Code").toString());
                    }
                }
            }
        } else {
            mnPollutant = getMonitorPollutantByParam(mns, type);
            //获取该类型主要污染物
            paramMap.put("monitorpointtype", type);
            keyPollutants = pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(paramMap);
        }
        Map<String, Set<String>> mncodes = new HashMap<>();
        for (Map<String, String> map : mnPollutant) {
            String mn = map.get("DGIMN");
            String code = map.get("Code");
            if (mncodes.containsKey(mn)) {
                mncodes.get(mn).add(code);
            } else {
                Set<String> allcodes = new HashSet<>();
                allcodes.add(code);
                mncodes.put(mn, allcodes);
            }
        }
        // List<Map<String, Object>> keyPollutants =  pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(paramMap);
        if (keyPollutants != null && keyPollutants.size() > 0) {
            for (Map<String, Object> keyPollutant : keyPollutants) {
                if (keyPollutant.get("Code") != null) {
                    if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                        if (!sets.contains(keyPollutant.get("Code").toString() + "_" + keyPollutant.get("FK_MonitorPointTypeCode"))) {
                            if (!codes.contains(keyPollutant.get("Code").toString())) {
                                codes.add(keyPollutant.get("Code").toString());
                            }
                            codeandname.put(keyPollutant.get("Code").toString(), keyPollutant.get("Name"));
                            sets.add(keyPollutant.get("Code").toString() + "_" + keyPollutant.get("FK_MonitorPointTypeCode"));
                            codeandunit.put(keyPollutant.get("Code").toString() + "_" + keyPollutant.get("FK_MonitorPointTypeCode"), keyPollutant.get("PollutantUnit"));

                        }
                    } else {
                        if (!sets.contains(keyPollutant.get("Code").toString())) {
                            codes.add(keyPollutant.get("Code").toString());
                            codeandname.put(keyPollutant.get("Code").toString(), keyPollutant.get("Name"));
                            sets.add(keyPollutant.get("Code").toString());
                            codeandunit.put(keyPollutant.get("Code").toString(), keyPollutant.get("PollutantUnit"));
                        }
                    }
                }
            }
        }
        //获取mongdb数据
        Map<String, Object> dbParam = new HashMap<>();
        dbParam.put("monitorpointtype", type);
        dbParam.put("mns", mns);
        if (StringUtils.isNotBlank(datatype)) {
            dbParam.put("datatype", datatype);
        }

        List<Document> documents = getMongoDbDataByParam(dbParam);
        Map<String, Object> mnAndAqi = (Map<String, Object>) dbParam.get("mnAndAqi");
        Map<String, Object> mnAndLevel = (Map<String, Object>) dbParam.get("mnAndLevel");
        String sortData = DataFormatUtil.parseProperties("data.sort");
        JSONObject sortJson = StringUtils.isNotBlank(sortData) ? JSONObject.fromObject(sortData) : new JSONObject();
        String statusKey;
        String mnCommon;
        String waterLevel;
        for (Map<String, Object> map : onlineOutPuts) {
            Map<String, Object> obj = new HashMap<>();
            if (map.get("dgimn") != null) {
                String mn = map.get("dgimn").toString();
                List<Document> dataList = new ArrayList<>();
                Set<String> outputpollutants = new HashSet<>();
                String monitortime = "";
                for (Document document : documents) {
                    if (mn.equals(document.getString("DataGatherCode"))) {
                        monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                        dataList = (List<Document>) document.get("DataList");
                        break;
                    }
                }
                if (mncodes.get(mn) != null) {
                    outputpollutants = mncodes.get(mn);
                }
                List<Map<String, Object>> pollutantdata = new ArrayList<>();
                if (codes.size() > 0) {
                    for (String code : codes) {
                        boolean ishavecode = false;
                        if (outputpollutants != null) {
                            for (String outputpocode : outputpollutants) {
                                if (code.equals(outputpocode)) {
                                    ishavecode = true;
                                    break;
                                }
                            }
                        }
                        Map<String, Object> pollutantmap = new HashMap<>();
                        pollutantmap.put("code", code);
                        pollutantmap.put("name", codeandname.get(code));
                        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                            pollutantmap.put("unit", codeandunit.get(code + "_" + map.get("monitorpointtype")));
                        } else {
                            pollutantmap.put("unit", codeandunit.get(code));
                        }
                        String value = "-";
                        if (ishavecode == true) {
                            value = "未检测";
                        }
                        Object isover = false;
                        Object isexception = false;
                        Object issuddenchange = false;
                        if (dataList != null && dataList.size() > 0) {
                            for (Document document1 : dataList) {
                                if (document1.get("PollutantCode") != null && document1.get("PollutantCode") != "" && code.equals(document1.getString("PollutantCode"))) {
                                    value = document1.get("AvgStrength") != null ? document1.getString("AvgStrength") : "";
                                    if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || type == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                                        if (smokemns.contains(mn) && smokes.contains(document1.getString("PollutantCode"))) {
                                            value = document1.get("AvgConvertStrength") != null ? document1.getString("AvgConvertStrength") : "";
                                        }
                                    }
                                    int IsOver = document1.get("IsOver") != null ? document1.getInteger("IsOver") : -1;
                                    boolean IsOverStandard = document1 != null ? document1.getBoolean("IsOverStandard") : false;
                                    if (IsOverStandard) {
                                        isover = true;
                                    }
                                    if (IsOver > -1) {
                                        isover = true;
                                    }
                                    if (document1.get("IsException") != null && document1.getInteger("IsException") > 0) {
                                        isexception = true;
                                    }
                                    if (document1.get("IsSuddenChange") != null) {
                                        issuddenchange = document1.get("IsSuddenChange");
                                    }
                                    break;
                                }
                            }
                        }
                        pollutantmap.put("isover", isover);
                        pollutantmap.put("isexception", isexception);
                        pollutantmap.put("issuddenchange", issuddenchange);
                        pollutantmap.put("value", value);
                        pollutantdata.add(pollutantmap);
                    }
                }
                obj.put("alarmlevel", map.get("AlarmLevel"));
                obj.put("onlinestatus", map.get("onlinestatus"));
                obj.put("onlinestatusname", map.get("onlinestatusname"));
                if (map.get("status") != null && "0".equals(map.get("status").toString())) {
                    obj.put("onlinestatus", -2);//自定义状态  表示设备停用
                    obj.put("onlinestatusname", "停用");
                    obj.put("orderstatus", 12);
                }
                if (map.get("onlinestatus") != null) {
                    statusKey = CommonTypeEnum.StatusOrderSetEnum.getIndexByCode(map.get("onlinestatus").toString());
                    obj.putIfAbsent("orderstatus", sortJson.get(statusKey) != null ? sortJson.get(statusKey) : 11);
                } else {
                    obj.putIfAbsent("orderstatus", 11);
                }
                obj.put("pollutionname", map.get("pollutionname") != null ? map.get("pollutionname").toString() : "");
                obj.put("shortername", map.get("shortername") != null ? map.get("shortername").toString() : "");
                obj.put("outputname", map.get("outputname") != null ? map.get("outputname").toString() : (map.get("monitorpointname") != null ? map.get("monitorpointname").toString() : ""));

                mnCommon = map.get("dgimn") + "";
                obj.put("dgimn", mnCommon);
                obj.put("aqi", mnAndAqi.get(mnCommon));
                if (mnAndLevel.get(mnCommon) != null) {
                    waterLevel = mnAndLevel.get(mnCommon).toString();
                    obj.put("waterlevel", waterLevel);
                    obj.put("waterlevelcode", waterLevel.replaceAll("类", ""));
                } else {
                    obj.put("waterlevel", "");
                    obj.put("waterlevelcode", "");
                }


                obj.put("controllevelname", map.get("controllevelname"));
                obj.put("FK_ControlLevelCode", map.get("FK_ControlLevelCode"));
                obj.put("monitorpointid", map.get("monitorpointid"));
                obj.put("pollutionid", map.get("pk_pollutionid") != null ? map.get("pk_pollutionid").toString() : "");
                obj.put("longitude", map.get("Longitude"));
                obj.put("latitude", map.get("Latitude"));
                if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode() ||
                        type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() ||
                        type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {
                    obj.put("monitorpointcategoryname", map.get("MonitorPointCategoryName"));
                }
                if (type == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {
                    obj.put("targetwaterqualityclass", map.get("waterqualityclass"));
                }
                obj.put("pollutantdata", pollutantdata);
                obj.put("monitortime", monitortime);
                obj.put("monitorpointtype", type);
                result.add(obj);
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/23 15:21
     * @Description: 统计排口点位报警类型的报警数据（超标，阈警，异常，无流量）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map> getLastOutPutPointAlarmDataByParam(Integer remind, List<String> mns, Date startDate, Date endDate, List<Integer> monitorpointtypes, Map<String, Object> mnandtype) {
        List<Map> listdata = new ArrayList<>();
        String timefield = "";
        String collection = "";
        List<Document> listItems = new ArrayList<>();
        List<Map<String, Object>> stands = new ArrayList<>();
        List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> timeAndRead = new HashMap<>();
        for (Integer monitorpointtype : monitorpointtypes) {
            param.put("pollutanttype", monitorpointtype);
            gasOutPutPollutantSetsByOutputIds.addAll(gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(param));
        }
        Map<String, String> pollutants = new HashMap<>();
        gasOutPutPollutantSetsByOutputIds.stream().filter(m -> m.get("pollutantname") != null && m.get("pollutantcode") != null).map(m -> pollutants.put(m.get("pollutantcode").toString(), m.get("pollutantname").toString())).collect(Collectors.toList());
        if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
            for (int type : monitorpointtypes) {
                param.put("monitorpointtype", type);
                stands.addAll(pollutantFactorMapper.getPollutantStandardValueDataByParam(param));
            }
        }
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remind == EarlyAlarmEnum.getCode()) {
            timefield = "EarlyWarnTime";
            collection = "EarlyWarnData";
            timeAndRead.put("time", "$" + timefield);
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "MonitorValue", "firsttime", "lasttime", timefield, "PollutantCode", "ExceptionType")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")
                            .last("MonitorValue").as("value")
                            .min(timefield).as("firsttime")
                            .max(timefield).as("lasttime")
                            .push(timeAndRead).as("timeList")
            );
        } else if (remind == OverAlarmEnum.getCode()) {
            //超限报警
            timefield = "OverTime";
            collection = "OverData";
            timeAndRead.put("time", "$" + timefield);
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "MonitorValue", "OverMultiple", timefield, "PollutantCode", "ExceptionType")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")
                            //.last("PollutantCode").as("code")
                            .last("MonitorValue").as("value")
                            .max("OverMultiple").as("maxvalue")
                            .min("OverMultiple").as("minvalue")
                            .min(timefield).as("firsttime")
                            .max(timefield).as("lasttime")
                            .push(timeAndRead).as("timeList")
            );
        } else if (remind == ExceptionAlarmEnum.getCode()) {
            //异常
            timefield = "ExceptionTime";
            collection = "ExceptionData";
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "MonitorValue", timefield, "PollutantCode", "ExceptionType")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> exceptiontypeandread = new HashMap<>();
            exceptiontypeandread.put("exceptiontype", "$ExceptionType");
            timeAndRead.put("time", "$" + timefield);
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")
                            .last("MonitorValue").as("value")
                            .min(timefield).as("firsttime")
                            .max(timefield).as("lasttime")
                            .push(exceptiontypeandread).as("ExceptionTypeList")
                            .push(timeAndRead).as("timeList")
            );
        } else if (remind == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
            //无流量异常
            timefield = "ExceptionTime";
            collection = "ExceptionData";
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "MonitorValue", timefield, "PollutantCode", "ExceptionType")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            timeAndRead.put("time", "$" + timefield);
            Map<String, Object> exceptiontypeandread = new HashMap<>();
            exceptiontypeandread.put("exceptiontype", "$ExceptionType");
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")
                            .last("MonitorValue").as("value")
                            .min(timefield).as("firsttime")
                            .max(timefield).as("lasttime")
                            .push(exceptiontypeandread).as("ExceptionTypeList")
                            .push(timeAndRead).as("timeList")
            );
        }
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "DataGatherCode"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();
        List<Document> timeList;
        List<String> dateList;
        String ymdhms;
        String continuityvalue;
        List<Map<String, Object>> exceptiontypes;
        String AlarmType;
        String minValue;
        String maxValue;
        if (listItems != null && listItems.size() > 0) {
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("remindcode", remind);
                if (remind == EarlyAlarmEnum.getCode()) {
                    dataMap.put("remindname", EarlyAlarmEnum.getName());
                } else if (remind == OverAlarmEnum.getCode()) {
                    dataMap.put("remindname", OverAlarmEnum.getName());
                } else if (remind == ExceptionAlarmEnum.getCode()) {
                    dataMap.put("remindname", ExceptionAlarmEnum.getName());
                } else if (remind == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
                    dataMap.put("remindname", CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getName());
                }
                //报警间隔时间
                int interval;
                //获取配置的各类型排口的间隔时间
                String monitortype = "";
                if (mnandtype.get(document.getString("DataGatherCode")) != null) {
                    monitortype = mnandtype.get(document.getString("DataGatherCode")).toString();
                }
                if (monitortype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || monitortype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
                } else if (monitortype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || monitortype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode())) {//废水、雨水
                    if (remind == ExceptionAlarmEnum.getCode()) {
                        interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
                    } else {
                        interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutputover.minute"));
                    }
                } else if (monitortype.equals(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode())) {//扬尘
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
                } else {//其它类型监测点
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
                }
                timeList = (List<Document>) document.get("timeList");
                dateList = new ArrayList<>();
                for (Document time : timeList) {
                    ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                    if (!dateList.contains(ymdhms)) {
                        dateList.add(ymdhms);
                    }
                }
                continuityvalue = DataFormatUtil.mergeContinueDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                dataMap.put("continuityvalue", continuityvalue);
                dataMap.put("datagathercode", document.get("DataGatherCode"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                dataMap.put("exceptiontype", document.get("ExceptionType"));
                dataMap.put("count", document.get("count"));
                dataMap.put("name", "");
                dataMap.put("value", "");
                dataMap.put("standardvalue", "");
                dataMap.put("pollutantcode", document.getString("PollutantCode"));
                //污染物
                String code = document.getString("PollutantCode");
                if (remind == OverAlarmEnum.getCode()) {
                    if (document.get("minvalue") != null && document.get("maxvalue") != null) {
                        String minvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(document.get("minvalue").toString()) * 100));
                        String maxvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(document.get("maxvalue").toString()) * 100));
                        if (!minvalue.equals(maxvalue)) {//最大超标倍数和最小超标倍数不相等
                            dataMap.put("overmultiple", minvalue + "%-" + maxvalue + "%");
                        } else {//相等
                            dataMap.put("overmultiple", minvalue + "%");
                        }
                    } else {
                        dataMap.put("overmultiple", "");
                    }
                } else {
                    dataMap.put("overmultiple", "");
                }
                Set<String> names = new HashSet<>();
                if (remind == ExceptionAlarmEnum.getCode() || remind == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
                    exceptiontypes = ((List<Map<String, Object>>) document.get("ExceptionTypeList")).stream().distinct().collect(Collectors.toList());
                    String strname = pollutants.get(code);
                    String str = "";
                    for (Map<String, Object> obj : exceptiontypes) {
                        str += CommonTypeEnum.ExceptionTypeEnum.getNameByCode(obj.get("exceptiontype").toString()) + "、";
                    }
                    if (!"".equals(str)) {
                        str = str.substring(0, str.length() - 1);
                        names.add(strname + "【" + str + "】");
                    }
                    List<String> tempList = new ArrayList<>(names);
                    dataMap.put("name", DataFormatUtil.FormatListToString(tempList, "、"));
                    dataMap.put("value", document.get("value"));
                }
                if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                    dataMap.put("name", pollutants.get(document.getString("PollutantCode")));
                    dataMap.put("value", document.get("value"));
                    if (stands != null && stands.size() > 0) {
                        for (Map<String, Object> standobj : stands) {
                            if (standobj.get("DGIMN") != null && document.getString("DataGatherCode").equals(standobj.get("DGIMN").toString())) {
                                if (standobj.get("Code") != null && document.getString("PollutantCode").equals(standobj.get("Code").toString())) {
                                    if (remind == EarlyAlarmEnum.getCode()) {
                                        dataMap.put("standardvalue", (standobj.get("ConcenAlarmMaxValue") != null && !"".equals(standobj.get("ConcenAlarmMaxValue").toString())) ? ">" + standobj.get("ConcenAlarmMaxValue").toString() : null);
                                    } else {
                                        if (standobj.get("alarmtype") != null) {
                                            AlarmType = standobj.get("alarmtype").toString();
                                            if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(AlarmType)) {//上限报警
                                                dataMap.put("standardvalue", (standobj.get("StandardMaxValue") != null && !"".equals(standobj.get("StandardMaxValue").toString())) ? "<" + standobj.get("StandardMaxValue").toString() : null);
                                            } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(AlarmType)) {//下限报警
                                                dataMap.put("standardvalue", (standobj.get("StandardMinValue") != null && !"".equals(standobj.get("StandardMinValue").toString())) ? ">" + standobj.get("StandardMinValue").toString() : null);
                                            } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(AlarmType)) {//区间报警
                                                minValue = standobj.get("StandardMinValue") != null ? standobj.get("StandardMinValue").toString() : "";
                                                maxValue = standobj.get("StandardMaxValue") != null ? standobj.get("StandardMaxValue").toString() : "";
                                                if (StringUtils.isNotBlank(minValue) && StringUtils.isNotBlank(maxValue)) {
                                                    dataMap.put("standardvalue", minValue + "-" + maxValue);
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
                listdata.add(dataMap);
            }
        }
        return listdata;
    }

    /**
     * @author: xsm
     * @date: 2020/12/17 0017 下午 5:54
     * @Description: 统计监测点在开始时间到结束时间范围内的浓度、排放量突变
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countMonitorPointChangeDataByParamMap(Integer remind, List<String> mns, Date startDate, Date endDate, List<Integer> monitorpointtypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        DecimalFormat format = new DecimalFormat("0.##");
        List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Integer monitorpointtype : monitorpointtypes) {
            param.put("pollutanttype", monitorpointtype);
            gasOutPutPollutantSetsByOutputIds.addAll(gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(param));
        }
        Map<String, String> pollutants = new HashMap<>();
        gasOutPutPollutantSetsByOutputIds.stream().filter(m -> m.get("pollutantname") != null && m.get("pollutantcode") != null).map(m -> pollutants.put(m.get("pollutantcode").toString(), m.get("pollutantname").toString())).collect(Collectors.toList());
        //浓度突变
        Criteria criteria = new Criteria();
        Criteria criteria2 = new Criteria();
        List<Document> listdata = new ArrayList<>();
        Map<String, Object> timeAndRead = new HashMap<>();
        timeAndRead.put("time", "$MonitorTime");
        if (remind == FlowChangeEnum.getCode()) {  //排放量
            criteria.and("DataGatherCode").in(mns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            criteria2.and("HourFlowDataList.IsSuddenChange").is(true);
            listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), unwind("HourFlowDataList"), match(criteria2), project("DataGatherCode", "MonitorTime", "count").and("HourFlowDataList.PollutantCode").as("code").and("HourFlowDataList.AvgFlow").as("MonitorValue").and("HourFlowDataList.ChangeMultiple").as("Multiple")
                    , group("DataGatherCode", "code").count().as("count")
                            .last("MonitorValue").as("value")
                            .min("Multiple").as("minmultiple")
                            .max("Multiple").as("maxmultiple")
                            .min("MonitorTime").as("firsttime")
                            .max("MonitorTime").as("lasttime")
                            .push(timeAndRead).as("timeList")), "HourFlowData", Document.class).getMappedResults();

        } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
            criteria.and("DataGatherCode").in(mns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            criteria2.and("HourDataList.IsSuddenChange").is(true);
            listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), unwind("HourDataList"), match(criteria2), project("DataGatherCode", "MonitorTime", "count").and("HourDataList.PollutantCode").as("code").and("HourDataList.AvgStrength").as("MonitorValue").and("HourDataList.ChangeMultiple").as("Multiple")
                    , group("DataGatherCode", "code").count().as("count")
                            .last("MonitorValue").as("value")
                            .min("Multiple").as("minmultiple")
                            .max("Multiple").as("maxmultiple")
                            .min("MonitorTime").as("firsttime")
                            .max("MonitorTime").as("lasttime")
                            .push(timeAndRead).as("timeList")), "HourData", Document.class).getMappedResults();
        }
        if (listdata.size() > 0) {
            for (Document document : listdata) {
                Map<String, Object> mappedResult = new HashMap<>();
                if (remind == FlowChangeEnum.getCode()) {  //排放量
                    mappedResult.put("remindcode", remind);
                    mappedResult.put("remindname", FlowChangeEnum.getName());
                } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
                    mappedResult.put("remindcode", remind);
                    mappedResult.put("remindname", ConcentrationChangeEnum.getName());
                }
                List<Document> timeList = (List<Document>) document.get("timeList");
                List<Integer> timelist = new ArrayList<>();
                if (timeList != null && timeList.size() > 0) {
                    for (int i = 0; i < timeList.size(); i++) {
                        Document timedo = timeList.get(i);
                        String hour;
                        String dateString = formatter.format(timedo.getDate("time"));
                        hour = dateString.substring(11, 13);
                        timelist.add(Integer.parseInt(hour));
                    }
                }
                List<List<Integer>> line = groupChangeIntegerList(timelist);
                String continuityvalue = "";
                continuityvalue = getChangeLine(line);
                if (continuityvalue.length() > 0) {
                    continuityvalue = continuityvalue.substring(0, continuityvalue.length() - 1);
                }
                mappedResult.put("continuityvalue", continuityvalue);
                Float min = document.get("minmultiple") != null ? Float.valueOf(document.get("minmultiple").toString()) : null;
                Float max = document.get("maxmultiple") != null ? Float.valueOf(document.get("maxmultiple").toString()) : null;
                if (max != min) {
                    mappedResult.put("overmultiple", format.format(min * 100) + "%" + "-" + format.format(max * 100) + "%");
                } else if (max != null) {
                    mappedResult.put("overmultiple", format.format(max * 100) + "%");
                }
                mappedResult.put("code", document.get("code"));
                mappedResult.put("count", document.get("count"));
                mappedResult.put("value", document.get("value"));
                mappedResult.put("name", pollutants.get(document.getString("code")));
                mappedResult.put("DataGatherCode", document.get("DataGatherCode"));
                mappedResult.put("starttime", DataFormatUtil.getDateYMDH(document.getDate("firsttime")));
                mappedResult.put("endtime", DataFormatUtil.getDateYMDH(document.getDate("lasttime")));
                result.add(mappedResult);
            }
        }
        return result;
    }

    @Override
    public List<PollutantSetDataVO> getStandardDataListByParam(Map<String, Object> paramMap) {
        List<PollutantSetDataVO> pollutantSetDataVOList = new ArrayList<>();
        Integer monitorPointType = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorPointType)) {
            case WasteWaterEnum:
            case RainEnum:
                //废水、雨水
                pollutantSetDataVOList = waterOutPutPollutantSetMapper.getPollutantSetDataListByParam(paramMap);
                break;
            case WasteGasEnum:
            case SmokeEnum:
                //重金属、烟气
                pollutantSetDataVOList = gasOutPutPollutantSetMapper.getPollutantSetDataListByParam(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                pollutantSetDataVOList = gasOutPutPollutantSetMapper.getUNGasPollutantSetDataListByParam(paramMap);
                break;
            case WaterQualityEnum:
                pollutantSetDataVOList = waterStationMapper.getPollutantSetDataListByParam(paramMap);
                //水质
                break;
            case AirEnum:
                pollutantSetDataVOList = airMonitorStationMapper.getPollutantSetDataListByParam(paramMap);
                //大气
                break;
            case MicroStationEnum:
            case EnvironmentalStinkEnum:
            case meteoEnum:
            case EnvironmentalDustEnum:
                pollutantSetDataVOList = otherMonitorPointMapper.getPollutantSetDataListByParam(paramMap);
                break;
            default:
                pollutantSetDataVOList = otherMonitorPointMapper.getPollutantSetDataListByParam(paramMap);
        }
        return pollutantSetDataVOList;
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
    public static List<List<Integer>> groupChangeIntegerList(List<Integer> list) {
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

    @Override
    public List<Document> getAlarmMonitorDataByParamMap(Map<String, Object> paramMap) {
        Query query = setQuery(paramMap);
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    private Query setQuery(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        }


        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                if (paramMap.get("monitortimefield") != null) {
                    query.addCriteria(Criteria.where(paramMap.get("monitortimefield").toString()).gte(startDate).lte(endDate));
                } else {
                    query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
                }
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                if (paramMap.get("monitortimefield") != null) {
                    query.addCriteria(Criteria.where(paramMap.get("monitortimefield").toString()).gte(startDate));
                } else {
                    query.addCriteria(Criteria.where("MonitorTime").gte(startDate));
                }
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("MonitorTime").lte(endDate));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            if (paramMap.get("datalistfield") != null) {
                query.addCriteria(Criteria.where(paramMap.get("datalistfield").toString() + ".PollutantCode").in(pollutantcodes));
            } else {
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
            if (paramMap.get("sortfield") != null) {
                query.with(new Sort(Sort.Direction.ASC, paramMap.get("sortfield").toString()));
            } else {
                query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            }
        } else {
            if (paramMap.get("sortfield") != null) {
                query.with(new Sort(Sort.Direction.DESC, paramMap.get("sortfield").toString()));
            } else {
                query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
            }
        }


        return query;
    }

    /**
     * @author: xsm
     * @date: 2021/06/10 0010 上午 9:11
     * @Description: 根据监测类型、污染物和时间范围获取该类型所有点位该污染物的浓度趋势分析
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllMonitorPointHourTrendDataByParamMap(String code, List<Map<String, Object>> outPutInfosByParamMap, String startTime, String endTime) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
        startTime = startTime + ":00:00";
        endTime = endTime + ":00:00";
        Date startDate = DataFormatUtil.getDateYMDHMS(startTime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endTime);
        List<String> times = new ArrayList<>();
        for (String time : DataFormatUtil.separateTimeForHour(startTime, endTime, 1)) {
            times.add(time + ":00:00");
        }
        times.add(endTime);
        //List<Date> timesDate = times.stream().map(DataFormatUtil::getDateYMDHMS).collect(Collectors.toList());
        resultMap.put("times", times.stream().map(hour -> Integer.valueOf(hour.substring(11, 13)) + "时").collect(Collectors.toList()));
        List<Map<String, Object>> monitordata = new ArrayList<>();
        if (dgimns != null && dgimns.size() > 0) {
            Aggregation aggregation = newAggregation(
                    match(Criteria.where("DataGatherCode").in(dgimns)
                            .and("MonitorTime").gte(startDate).lte(endDate)
                            .and("HourDataList.PollutantCode").is(code)),
                    unwind("HourDataList"),
                    match(Criteria.where("HourDataList.PollutantCode").is(code)),
                    project("MonitorTime", "DataGatherCode")
                            .and("HourDataList.AvgStrength").as("AvgStrength")
                            .and("HourDataList.PollutantCode").as("PollutantCode")

            );
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
            List<Document> documents = aggregationResults.getMappedResults();
            String mnCommon = "";
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
                for (Map<String, Object> monitorPoint : outPutInfosByParamMap) {
                    mnCommon = monitorPoint.get("DGIMN").toString();
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("name", monitorPoint.get("OutputName"));
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
     * @author: xsm
     * @date: 2021/06/16 0016 上午 10:43
     * @Description: 根据时间段、报警类型统计某个点位当天的报警污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<String> getAlarmPollutanByAlarmTypeAndTimes(List<String> dgimns, List<String> pollutantcodes, String starttime, String endtime, Integer remind, String overflag) {
        List<String> codes = new ArrayList<>();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        String timestr = "";
        String collection = "";
        if (remind == OverAlarmEnum.getCode()) {//超标
            timestr = "OverTime";
            collection = "OverData";
        } else if (remind == EarlyAlarmEnum.getCode()) {//预警
            timestr = "EarlyWarnTime";
            collection = "EarlyWarnData";
        } else if (remind == ExceptionAlarmEnum.getCode()) {//异常
            timestr = "ExceptionTime";
            collection = "ExceptionData";
        } else if (remind == ConcentrationChangeEnum.getCode()) {//浓度突变
            timestr = "ChangeTime";
            collection = "SuddenRiseData";
        }
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remind == OverAlarmEnum.getCode()) {
            if ("yes".equals(overflag)) {
                criteria.and("DataGatherCode").in(dgimns).
                        and(timestr).gte(startDate).lte(endDate).and("IsOverStandard").is(true);
            } else if ("no".equals(overflag)) {
                criteria.and("DataGatherCode").in(dgimns).
                        and(timestr).gte(startDate).lte(endDate).and("IsOverStandard").is(false).and("AlarmLevel").gt(0);
            }
        } else if (remind == ConcentrationChangeEnum.getCode()) {
            //突变只判断分钟突变
            criteria.and("DataGatherCode").in(dgimns).
                    and(timestr).gte(startDate).lte(endDate).and("DataType").is("MinuteData");
        } else {
            criteria.and("DataGatherCode").in(dgimns).and(timestr).gte(startDate).lte(endDate);
        }
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "PollutantCode"));
        GroupOperation groupOperation = group("PollutantCode");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        if (resultDocument.size() > 0) {
            //通过mn号分组数据
            for (Document document : resultDocument) {
                codes.add(document.getString("_id"));
            }
        }
        return codes;
    }

    /**
     * @author: xsm
     * @date: 2021/06/16 0016 下午 2:53
     * @Description: 根据单个点位单个污染物所有报警时间段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnePointAndOnePollutantAllAlarmTimes(Map<String, Object> paramMap, Map<String, String> pollutantmap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("dgimns");
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String pollutantcode = paramMap.get("pollutantcode").toString();
        //分钟浓度突变
        Map<String, Object> ndtb_map = getAlarmTimesForAlarmType(mns, startDate, endDate, ConcentrationChangeEnum.getCode(), pollutantcode);
        if (ndtb_map.size() > 0) {
            result.add(ndtb_map);
        }
        //超标
        Map<String, Object> cb_map = getAlarmTimesForAlarmType(mns, startDate, endDate, OverAlarmEnum.getCode(), pollutantcode);
        if (cb_map.size() > 0) {
            result.add(cb_map);
        }
        //预警
        Map<String, Object> yj_map = getAlarmTimesForAlarmType(mns, startDate, endDate, EarlyAlarmEnum.getCode(), pollutantcode);
        if (yj_map.size() > 0) {
            result.add(yj_map);
        }
        //异常
        Map<String, Object> yc_map = getAlarmTimesForAlarmType(mns, startDate, endDate, ExceptionAlarmEnum.getCode(), pollutantcode);
        for (Map.Entry<String, Object> entry : yc_map.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue());
            map.put("pollutantname", pollutantmap.get(pollutantcode));
            map.put("pollutantcode", pollutantcode);
            result.add(map);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getPointLastDataByParam(List<Map<String, Object>> onlineOutPuts, Map<String, Object> paramMap) {

        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Aggregation aggregation = newAggregation(
                match(Criteria.where("DataGatherCode").in(dgimns)
                        .and("Type").is("RealTimeData")
                        .and("DataList.PollutantCode").is(paramMap.get("pollutantcode"))),
                unwind("DataList"),
                match(Criteria.where("DataList.PollutantCode").is(paramMap.get("pollutantcode"))),
                project("DataGatherCode")
                        .and("DataList.AvgStrength").as("AvgStrength")
                        .and("DataList.PollutantCode").as("PollutantCode")

        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, db_LatestData, Document.class);
        List<Document> documents = results.getMappedResults();
        if (documents.size() > 0) {
            Map<String, Object> mnAndValue = new HashMap<>();
            for (Document document : documents) {
                mnAndValue.put(document.getString("DataGatherCode"), document.get("AvgStrength"));
            }
            for (Map<String, Object> point : onlineOutPuts) {
                if (mnAndValue.containsKey(point.get("dgimn"))) {
                    point.put("monitorvalue", mnAndValue.get(point.get("dgimn").toString()));
                }
            }
        }
        return onlineOutPuts;
    }

    @Override
    public Map<String, Object> getManyPonitPollutantDataByParam(Map<String, Object> param) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> mnandname = (Map<String, Object>) param.get("mnandname");
        List<String> mns = param.get("mns") != null ? (List<String>) param.get("mns") : null;
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        starttime = starttime + ":00:00";
        endtime = endtime + ":00:00";
        List<String> times = new ArrayList<>();
        for (String time : DataFormatUtil.separateTimeForHour(starttime, endtime, 1)) {
            times.add(time + ":00:00");
        }
        times.add(endtime);
        resultMap.put("times", times.stream().map(hour -> hour.substring(11, 13) + "时").collect(Collectors.toList()));
        List<String> pollutantCodes = Arrays.asList(param.get("pollutantcode").toString());
        List<Map<String, Object>> listmap = new ArrayList<>();
        List<Document> documents = new ArrayList<>();
        if (mns.size() > 0) {
            Aggregation aggregation = newAggregation(
                    match(Criteria.where("DataGatherCode").in(mns)
                            .and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime))
                            .and("HourDataList.PollutantCode").in(pollutantCodes)),
                    unwind("HourDataList"),
                    match(Criteria.where("HourDataList.PollutantCode").in(pollutantCodes)),
                    project("MonitorTime", "DataGatherCode")
                            .and("HourDataList.AvgStrength").as("AvgStrength")
                            .and("HourDataList.PollutantCode").as("PollutantCode")

            );
            AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
            documents = aggregationResults.getMappedResults();
        }
        for (String mn : mns) {
            Map<String, Object> pointmap = new HashMap<>();
            pointmap.put("mn", mn);
            pointmap.put("monitorpointname", mnandname.get(mn));
            List<String> values = new ArrayList<>();
            for (String time : times) {
                String value = "";
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        if (mn.equals(document.getString("DataGatherCode")) && time.equals(DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")))) {
                            value = document.get("AvgStrength") != null ? document.get("AvgStrength").toString() : "";
                            break;
                        }
                    }
                }
                values.add(value);
            }
            pointmap.put("valuelist", values);
            listmap.add(pointmap);
        }
        resultMap.put("monitordata", listmap);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getPollutantListByParam(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
    }

    @Override
    public List<Document> getLastDataByMns(List<String> mns) {

        List<Document> list = new ArrayList<>();
        Bson bson = Filters.and(eq("Type", "RealTimeData"), in("DataGatherCode", mns));
        FindIterable<Document> documents = mongoTemplate.getCollection(db_LatestData).find(bson);
        for (Document document : documents) {
            list.add(document);
        }
        return list;
    }

    private Map<String, Object> getAlarmTimesForAlarmType(List<String> mns, Date startDate, Date endDate, Integer remind, String pollutantcode) {
        Map<String, Object> resultmap = new HashMap<>();
        String timestr = "";
        String collection = "";
        String mapkey = "";
        if (remind == OverAlarmEnum.getCode()) {//超标
            timestr = "FirstOverTime";
            collection = "OverModel";
            mapkey = "overdata";
        } else if (remind == EarlyAlarmEnum.getCode()) {//预警
            timestr = "FirstOverTime";
            collection = "OverModel";
            mapkey = "earlydata";
        } else if (remind == ExceptionAlarmEnum.getCode()) {//异常
            timestr = "FirstExceptionTime";
            collection = "ExceptionModel";
        } else if (remind == ConcentrationChangeEnum.getCode()) {//浓度突变
            timestr = "ChangeTime";
            collection = "SuddenRiseData";
            mapkey = "suddenchange";
        }
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remind == OverAlarmEnum.getCode()) {//超标
            criteria.and("MN").in(mns).and(timestr).gte(startDate).lte(endDate).and("PollutantCode").is(pollutantcode).and("AlarmLevel").ne(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timestr, "LastOverTime"));
        } else if (remind == EarlyAlarmEnum.getCode()) {//预警
            criteria.and("MN").in(mns).and(timestr).gte(startDate).lte(endDate).and("PollutantCode").is(pollutantcode).and("AlarmLevel").is(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timestr, "LastOverTime"));
        } else if (remind == ExceptionAlarmEnum.getCode()) {//异常
            criteria.and("MN").in(mns).and(timestr).gte(startDate).lte(endDate).and("PollutantCode").is(pollutantcode);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timestr, "LastExceptionTime", "ExceptionType"));
        } else if (remind == ConcentrationChangeEnum.getCode()) {//浓度突变
            criteria.and("DataGatherCode").in(mns).and(timestr).gte(startDate).lte(endDate).and("PollutantCode").is(pollutantcode).and("DataType").is("MinuteData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timestr));
        }
        operations.add(Aggregation.sort(Sort.Direction.ASC, timestr));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        if (listItems.size() > 0) {
            if (remind == ExceptionAlarmEnum.getCode()) {
                //通过异常类型分组数据
                Map<String, List<Document>> collect = listItems.stream().filter(m -> m.get("ExceptionType") != null).collect(Collectors.groupingBy(m -> m.get("ExceptionType").toString()));
                for (Map.Entry<String, List<Document>> entry : collect.entrySet()) {
                    List<Document> onedocument = entry.getValue();
                    String str = "";
                    for (Document document : onedocument) {
                        str = str + DataFormatUtil.getDateHM(document.getDate(timestr)) + "-" + DataFormatUtil.getDateHM(document.getDate("LastExceptionTime")) + "、";
                    }
                    if (!"".equals(str)) {
                        str = str.substring(0, str.length() - 1);
                    }
                    if (entry.getKey().equals(CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum.getCode() + "")) {
                        //零值异常
                        resultmap.put("zeroexception", str);
                    } else if (entry.getKey().equals(CommonTypeEnum.ExceptionTypeEnum.ContinuousExceptionEnum.getCode() + "")) {
                        //连续值异常
                        resultmap.put("continuousexception", str);
                    }
                }
            } else {
                String str = "";
                for (Document document : listItems) {
                    if (remind == OverAlarmEnum.getCode()) {//超标
                        str = str + DataFormatUtil.getDateHM(document.getDate(timestr)) + "-" + DataFormatUtil.getDateHM(document.getDate("LastOverTime")) + "、";
                    } else if (remind == EarlyAlarmEnum.getCode()) {//预警
                        str = str + DataFormatUtil.getDateHM(document.getDate(timestr)) + "-" + DataFormatUtil.getDateHM(document.getDate("LastOverTime")) + "、";
                    } else if (remind == ConcentrationChangeEnum.getCode()) {//浓度突变
                        str = str + DataFormatUtil.getDateHM(document.getDate(timestr)) + "、";
                    }

                }
                if (!"".equals(str)) {
                    str = str.substring(0, str.length() - 1);
                }
                resultmap.put(mapkey, str);

            }
        } else {
            if (remind == OverAlarmEnum.getCode() || remind == EarlyAlarmEnum.getCode() || remind == ExceptionAlarmEnum.getCode()) {//超标
                getOverAndExceptionHourData(resultmap, mns, startDate, endDate, remind, pollutantcode);
            }
        }
        return resultmap;
    }

    private void getOverAndExceptionHourData(Map<String, Object> resultmap, List<String> mns, Date startDate, Date endDate, Integer remind, String pollutantcode) {
        String timestr = "";
        String collection = "";
        String mapkey = "";
        if (remind == OverAlarmEnum.getCode()) {//超标
            timestr = "OverTime";
            collection = "OverData";
            mapkey = "overdata";
        } else if (remind == EarlyAlarmEnum.getCode()) {//预警
            timestr = "EarlyWarnTime";
            collection = "EarlyWarnData";
            mapkey = "earlydata";
        } else if (remind == ExceptionAlarmEnum.getCode()) {//异常
            timestr = "ExceptionTime";
            collection = "ExceptionData";
        }
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and(timestr).gte(startDate).lte(endDate).and("PollutantCode").is(pollutantcode).and("DataType").is("HourData");
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", timestr, "ExceptionType"));
        aggregations.add(Aggregation.sort(Sort.Direction.ASC, timestr));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        if (resultDocument.size() > 0) {
            if (remind == ExceptionAlarmEnum.getCode()) {
                //通过异常类型分组数据
                Map<String, List<Document>> collect = resultDocument.stream().filter(m -> m.get("ExceptionType") != null).collect(Collectors.groupingBy(m -> m.get("ExceptionType").toString()));
                for (Map.Entry<String, List<Document>> entry : collect.entrySet()) {
                    List<Document> onedocument = entry.getValue();
                    String houralarmstr = "";//小时超标时段
                    String newhour = "";
                    int newhournum = 0;
                    String str1 = "";
                    for (Document document : onedocument) {
                        int hour = DataFormatUtil.getDateHourNum(document.getDate(timestr));
                        str1 = DataFormatUtil.getDateHM(document.getDate(timestr));
                        if ("".equals(newhour)) {
                            newhour = hour + "";
                            newhournum += 1;
                            houralarmstr = str1 + "、";
                        } else {
                            if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                                //连续
                                newhour = hour + "";
                                newhournum += 1;
                            } else {
                                houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                                if (newhournum > 1) {
                                    if (Integer.parseInt(newhour) > 9) {
                                        houralarmstr = houralarmstr + "-" + newhour + ":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                    } else {
                                        houralarmstr = houralarmstr + "-" + "0" + newhour + ":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                    }
                                } else {
                                    houralarmstr = houralarmstr + "、" + str1 + "、";
                                }
                                newhour = hour + "";
                                newhournum = 1;
                            }
                        }
                    }
                    if (!"".equals(houralarmstr)) {
                        houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                    }
                    if (newhournum > 1) {
                        if (Integer.parseInt(newhour) > 9) {
                            houralarmstr = houralarmstr + "-" + newhour + ":00";
                        } else {
                            houralarmstr = houralarmstr + "-" + "0" + newhour + ":00";
                        }
                    }
                    if (entry.getKey().equals(CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum.getCode() + "")) {
                        //零值异常
                        resultmap.put("zeroexception", houralarmstr);
                    } else if (entry.getKey().equals(CommonTypeEnum.ExceptionTypeEnum.ContinuousExceptionEnum.getCode() + "")) {
                        //连续值异常
                        resultmap.put("continuousexception", houralarmstr);
                    }
                }
            } else {
                String houralarmstr = "";//小时超标时段
                String newhour = "";
                int newhournum = 0;
                String str1 = "";
                for (Document document : resultDocument) {
                    int hour = DataFormatUtil.getDateHourNum(document.getDate(timestr));
                    str1 = DataFormatUtil.getDateHM(document.getDate(timestr));
                    if ("".equals(newhour)) {
                        newhour = hour + "";
                        newhournum += 1;
                        houralarmstr = str1 + "、";
                    } else {
                        if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                            //连续
                            newhour = hour + "";
                            newhournum += 1;
                        } else {
                            houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                            if (newhournum > 1) {
                                if (Integer.parseInt(newhour) > 9) {
                                    houralarmstr = houralarmstr + "-" + newhour + ":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                } else {
                                    houralarmstr = houralarmstr + "-" + "0" + newhour + ":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                }
                            } else {
                                houralarmstr = houralarmstr + "、" + str1 + "、";
                            }
                            newhour = hour + "";
                            newhournum = 1;
                        }
                    }
                }
                if (!"".equals(houralarmstr)) {
                    houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                }
                if (newhournum > 1) {
                    if (Integer.parseInt(newhour) > 9) {
                        houralarmstr = houralarmstr + "-" + newhour + ":00";
                    } else {
                        houralarmstr = houralarmstr + "-" + "0" + newhour + ":00";
                    }
                }
                resultmap.put(mapkey, houralarmstr);
            }
        }
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
    public static String getChangeLine(List<List<Integer>> list) {
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

    /**
     * 废气烟气污染物 合并多点位表头
     */
    @Override
    public List<Map<String, Object>> getGasTableTitleByParam(Map<String, Object> titleMap) {
        List<Map<String, Object>> titleDataList = new ArrayList<>();
        titleDataList = getTableTitleDataForReport(101);
        List<Integer> monitorpointtypes = (List<Integer>) titleMap.get("monitorpointtypes");
        if (titleDataList.size() > 0) {
            Map<String, Object> commonMap = new HashMap<>();
            commonMap.put("showhide", true);
            commonMap.put("align", "center");
            String pollutantName;
            String pollutantCode;
            String pollutantUnit;
            Integer IsHasConvertData;
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            for (Integer i : monitorpointtypes) {
                titleMap.put("pollutanttype", i);
                pollutantList.addAll(gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(titleMap));
            }
            Set<String> codes = new HashSet<>();

            for (Map<String, Object> pollutant : pollutantList) {
                pollutantCode = pollutant.get("pollutantcode").toString();
                if (!codes.contains(pollutantCode)) {
                    codes.add(pollutantCode);
                    pollutantName = pollutant.get("pollutantname").toString();
                    pollutantUnit = pollutant.get("PollutantUnit").toString();
                    IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
                    Map<String, Object> parentTitle = new HashMap<>();
                    parentTitle.putAll(commonMap);

                    if (IsHasConvertData > 0) {
                        parentTitle.put("label", pollutantName);
                        parentTitle.put("prop", pollutantCode + "_par");
                        List<Map<String, Object>> children = new ArrayList<>();
                        Map<String, Object> sc = new HashMap<>();
                        sc.putAll(commonMap);
                        sc.put("prop", pollutantCode + "_sc");
                        if (StringUtils.isNotBlank(pollutantUnit)) {
                            sc.put("label", "实测值（" + pollutantUnit + "）");
                        } else {
                            sc.put("label", "实测值");
                        }
                        children.add(sc);
                        Map<String, Object> zs = new HashMap<>();
                        zs.putAll(commonMap);
                        zs.put("prop", pollutantCode + "_zs");
                        if (StringUtils.isNotBlank(pollutantUnit)) {
                            zs.put("label", "折算值（" + pollutantUnit + "）");
                        } else {
                            zs.put("label", "折算值");
                        }
                        children.add(zs);
                        parentTitle.put("children", children);
                    } else {
                        parentTitle.put("label", pollutantName + "（" + pollutantUnit + "）");
                        parentTitle.put("prop", pollutantCode + "_par");
                    }
                    titleDataList.add(parentTitle);
                }

            }
        }
        return titleDataList;
    }

    @Override
    public List<Map<String, Object>> getGasTableListDataByParam(Map<String, Object> paramMap) {
        String tableType = paramMap.get("tableType").toString();
        List<Map<String, Object>> tableDataList = new ArrayList<>();
        Query query = setNoGroupQuery(paramMap);
        String collection = paramMap.get("collection").toString();
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);

        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (documents.size() > 0) {
            //获取mongodb的flag标记
            Map<String,Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes",paramMap.get("monitorpointtypes"));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }
        //paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());/
        List<Map<String, Object>> standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);

        List<Map<String, Object>> pollutantList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
        String pollutantCode;
        Integer IsHasConvertData;
        Map<String, Integer> codeAndIs = new HashMap<>();
        for (Map<String, Object> pollutant : pollutantList) {
            IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
            pollutantCode = pollutant.get("pollutantcode").toString();
            codeAndIs.put(pollutantCode, IsHasConvertData);
        }
        List<Document> pollutants;
        String pollutantDataKey;
        String scValueKey;
        String zsValueKey;
        String isOver;
        Object flag;
        String isException;
        String isSuddenChange;
        boolean isOverStandard;
        Map<String, Object> mnAndPName = new HashMap<>();
        Map<String, Object> mnAndName = new HashMap<>();
        Map<String, Object> mnAndId = new HashMap<>();
        String mnCommon;
        String standardData;
        if (tableType.equals("many")) {
            List<Map<String, Object>> outputList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
            for (Map<String, Object> output : outputList) {
                mnCommon = output.get("dgimn").toString();
                mnAndPName.put(mnCommon, output.get("shortername"));
                mnAndName.put(mnCommon, output.get("outputname"));
                mnAndId.put(mnCommon, output.get("pk_id"));
            }
        }
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            Map<String, Object> dataMap = new HashMap<>();
            if (mnAndName.containsKey(mnCommon)) {
                dataMap.put("shortername", mnAndPName.get(mnCommon));
                dataMap.put("outputname", mnAndName.get(mnCommon));
                dataMap.put("outputid", mnAndId.get(mnCommon));
            }
            dataMap.put("monitortime", getFormatTimeData(document.getDate("MonitorTime"), collection));
            pollutantDataKey = getPollutantDataKey(collection);
            scValueKey = getValueKey(collection);
            zsValueKey = getZSValueKey(collection);
            pollutants = document.get(pollutantDataKey, List.class);
            for (Document pollutant : pollutants) {
                pollutantCode = pollutant.getString("PollutantCode");
                if (codeAndIs.containsKey(pollutantCode)) {
                    isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                    flag = flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "");
                    isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                    pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                    isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                    isOverStandard = pollutant.getBoolean("IsOverStandard");
                    if (isOverStandard) {
                        isOver = "4";
                    }
                    if (!"true".equals(isSuddenChange)) {
                        isSuddenChange = "false";
                    }
                    if (mnAndCodeAndStandardData.containsKey(mnCommon) && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode) != null
                            && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) != null) {
                        standardData = mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) + "";
                    } else {
                        standardData = "-";
                    }
                    if (codeAndIs.get(pollutantCode) > 0) {
                        dataMap.put(pollutantCode + "_sc" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                +"#"+flag, pollutant.get(scValueKey));
                        dataMap.put(pollutantCode + "_zs" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                +"#"+flag, pollutant.get(zsValueKey));
                    } else {
                        dataMap.put(pollutantCode + "_par" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                +"#"+flag, pollutant.get(scValueKey));
                    }
                }
            }
            tableDataList.add(dataMap);
        }
        return tableDataList;
    }

    @Override
    public Map<String, Object> getGasOutPutLastDataByParamMap(List<Map<String, Object>> outputs, List<Integer> monitorpointtypes, Map<String, Object> paramMap) {
        List<Map<String, Object>> outPutInfos = outputs.stream().distinct().collect(Collectors.toList());
        Map<String, Object> countData = new LinkedHashMap<>();
        Set<Object> tempPollution = new HashSet<>();
        Set<Object> tempNormal = new HashSet<>();
        Set<Object> tempoffline = new HashSet<>();
        Set<Object> tempover = new HashSet<>();
        Set<Object> tempexception = new HashSet<>();

        Set<Object> stopOutPutIds = new HashSet<>();

        //mn号
        List<String> mns = new ArrayList<>();
        List<String> outPutIds = new ArrayList<>();
        for (Map<String, Object> outPut : outPutInfos) {
            outPutIds.add(outPut.get("pk_id").toString());
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
            if (outPut.get("onlinestatusname") != null && "停产".equals(outPut.get("onlinestatusname").toString())) {//停产
                stopOutPutIds.add(outPut.get("pk_id").toString());
            }
        }
        //统计各状态监测点个数
        countData.put("pollutioncount", tempPollution.size());
        countData.put("outputcount", outPutInfos.size());
        countData.put("normalcount", tempNormal.size());
        countData.put("offlinecount", tempoffline.size());
        countData.put("overcount", tempover.size());
        countData.put("exceptioncount", tempexception.size());
        countData.put("stopnum", stopOutPutIds.size());
        paramMap.put("monitorpointtypes", monitorpointtypes);
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
        Map<String, List<Map<String, Object>>> mnCodeAndStandardMap = new HashMap<>();
        String mnCode;
        if (documents.size() > 0) {
            paramMap.put("mnlist", mns);
            paramMap.put("monitortype", "gas");
            List<Map<String, Object>> pollutantStandardDataList = pollutantFactorMapper.getPollutantStandardsByParam(paramMap);
            if (pollutantStandardDataList.size() > 0) {
                //通过mn号分组点位信息
                Map<String, List<Map<String, Object>>> listMap = pollutantStandardDataList.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                    String themn = entry.getKey();
                    List<Map<String, Object>> standlist = entry.getValue();
                    if (standlist != null && standlist.size() > 0) {
                        Map<String, List<Map<String, Object>>> pollutantMap = standlist.stream().collect(Collectors.groupingBy(m -> m.get("Code").toString()));
                        for (Map.Entry<String, List<Map<String, Object>>> entry_two : pollutantMap.entrySet()) {
                            mnCode = themn + "," + entry_two.getKey();
                            mnCodeAndStandardMap.put(mnCode, entry_two.getValue());
                        }
                    }
                }
            }
        }
        //表头数据
        List<Map<String, Object>> tabletitledata = getTableTitleDataForRealTime(monitorpointtypes.get(0));
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
            paramMap2.put("pollutanttypes", monitorpointtypes);
            pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap2);
            //有对应污染物列表中就有数据
            if (pollutants.size() > 0) {
                //获取污染物表头
                List<Map<String, Object>> pollutantstabletitledata = getPollutantTableTitleData(pollutants);
                //将污染物表头数据放入表头数据中
                tabletitledata.addAll(pollutantstabletitledata);
            }
        }
        Map<String, List<Map<String, Object>>> idAndRTSP = getRTSPData(monitorpointtypes);
        //列表数据
        Integer ishasconvertdata;
        String monitorpointtype;
        List<Map<String, Object>> tablelistdata = new ArrayList<>();

        //获取mongodb的flag标记
        Map<String,Object> f_map = new HashMap<>();
        f_map.put("monitorpointtypes",monitorpointtypes);
        List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
        Map<String, Object> flag_codeAndName = new HashMap<>();
        String flag_code;
        for (Map<String, Object> map : flagList) {
            if (map.get("code") != null) {
                flag_code = map.get("code").toString();
                flag_codeAndName.put(flag_code, map.get("name"));
            }
        }


        for (Map<String, Object> outPut : outPutInfos) {
            monitorpointid = outPut.get("monitorpointid").toString();
            monitorpointtype = outPut.get("monitorpointtype") + "";
            String dgimn = outPut.get("dgimn").toString();
            Map<String, Object> mapdata = formatRealTimeListDataByPointType(monitorpointtypes.get(0), outPut);
            String monitorTime = "";
            boolean pointIsStop = false;
            for (Map<String, Object> pollutant : pollutants) {
                Map<String, Object> map2 = new HashMap<>();
                String code = pollutant.get("code").toString();
                if (monitorpointtype.equals(pollutant.get("pollutanttype") + "")) {
                    ishasconvertdata = pollutant.get("ishasconvertdata") != null ? Integer.parseInt(pollutant.get("ishasconvertdata").toString()) : 0;
                    pollutantcodes = idAndCodes.get(monitorpointid);
                    Map<String, Object> pollutantmap = new HashMap<>();
                    getPointOnlineData(documents, dgimn, code, pollutantmap, ishasconvertdata);
                    monitorTime = pollutantmap.get("MonitorTime") != null ? pollutantmap.get("MonitorTime").toString() : "";
                    if (pollutantcodes != null && Arrays.asList(pollutantcodes).contains(code)) {
                        map2.put("IsStop", "1");
                    } else {
                        if (outPut.get("onlinestatusname") != null && outPut.get("onlinestatusname").toString().equals("停产")) {
                            mapdata.put("outputname", outPut.get("outputname") + "【已停产】");
                            pointIsStop = true;
                        }
                        map2.put("IsStop", "0");
                    }
                    mnCode = outPut.get("dgimn") + "," + pollutant.get("code");
                    map2.put("standardmaxvalue", null);
                    map2.put("alarmtype", null);
                    map2.put("standardminvalue", null);
                    map2.put("value", pollutantmap.get("value"));
                    map2.put("IsOver", pollutantmap.get("IsOver"));
                    map2.put("flag", flag_codeAndName.get(pollutantmap.get("flag") != null ? pollutantmap.get("flag").toString().toLowerCase() : ""));
                    if (mnCodeAndStandardMap.get(mnCode) != null) {
                        List<Map<String, Object>> standlist = mnCodeAndStandardMap.get(mnCode);
                        if (standlist != null && standlist.size() > 0) {
                            Map<String, Object> obj = standlist.get(0);
                            map2.put("standardmaxvalue", obj.get("StandardMaxValue"));
                            map2.put("alarmtype", obj.get("AlarmType"));
                            map2.put("standardminvalue", obj.get("StandardMinValue"));
                            for (Map<String, Object> objmap : standlist) {
                                if (objmap.get("FK_AlarmLevelCode") != null && !"".equals(objmap.get("FK_AlarmLevelCode").toString())) {
                                    Map<String, Object> codeAndMap = new HashMap<>();
                                    codeAndMap.put("concenalarmmaxvalue", objmap.get("ConcenAlarmMaxValue"));
                                    codeAndMap.put("concenalarmminvalue", objmap.get("ConcenAlarmMinValue"));
                                    map2.put("a_" + objmap.get("FK_AlarmLevelCode").toString(), codeAndMap);
                                }
                            }
                        }
                    }
                    map2.put("IsException", pollutantmap.get("IsException"));
                    //放入污染物code以及value
                    mapdata.put(code, map2);
                } else {
                    map2.put("IsStop", "0");
                    map2.put("value", null);
                    map2.put("flag", null);
                    map2.put("IsOver", 1);
                    map2.put("isexception", null);
                    map2.put("standardmaxvalue", null);
                    map2.put("alarmtype", null);
                    map2.put("standardminvalue", null);
                    mapdata.putIfAbsent(code, map2);
                }
            }
            mapdata.put("pointisstop", pointIsStop);
            mapdata.put("alarmlevel", outPut.get("AlarmLevel"));
            mapdata.put("monitortime", monitorTime);
            mapdata.put("rtsplist", idAndRTSP.get(monitorpointid));
            tablelistdata.add(mapdata);
        }
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> tabledata = new HashMap<>();
        //根据监测点类型和mn号获取各个mn号监测的污染物
        List<Map<String, String>> mnPollutant = new ArrayList<>();
        for (Integer type : monitorpointtypes) {
            mnPollutant.addAll(getMonitorPollutantByParam(mns, type));
        }
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
        String statusKey;

        String dataSort = DataFormatUtil.parseProperties("data.sort");
        JSONObject jsonObject = StringUtils.isNotBlank(dataSort) ? JSONObject.fromObject(dataSort) : new JSONObject();

        for (Map<String, Object> map : tablelistdata) {
            String mn = map.get("mn") == null ? "" : map.get("mn").toString();
            map.put("pollutants", mncodes.get(mn) != null ? mncodes.get(mn) : new ArrayList<>());
            if (map.get("onlinestatus") == null) {
                map.replace("onlinestatus", "");
                map.put("orderindex", 10);
            } else {
                String onlineStatus = map.get("onlinestatus").toString();
                if (map.get("pointisstop") != null && Boolean.parseBoolean(map.get("pointisstop").toString())) {
                    onlineStatus = "4";
                }
                statusKey = CommonTypeEnum.StatusOrderSetEnum.getIndexByCode(onlineStatus);
                map.put("orderindex", jsonObject.get(statusKey) != null ? jsonObject.get(statusKey) : 11);
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
                tablelistdata = tablelistdata.stream().sorted(
                        Comparator.comparingInt(
                                m -> ((Map) m).get("PointOrderIndex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("PointOrderIndex").toString())
                        ).thenComparingInt(m -> ((Map) m).get("orderindex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("orderindex").toString()))

                ).collect(Collectors.toList());
            }
        }
        resultMap.put("tablelistdata", tablelistdata);
        resultMap.put("tabletitledata", tabletitledata);
        tabledata.put("tabledata", resultMap);
        tabledata.put("datacount", countData);
        return tabledata;
    }

    /**
     * @author: xsm
     * @date: 2022/2/11 0011 上午 11::50
     * @Description: 统计分钟浓度突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getOnlineChangeDataGroupMmAndMonitortime(JSONObject paramMap) {
        List<Map<String, Object>> resultlist = new ArrayList<>();
        Criteria criteria = new Criteria();
        List<String> dgimns = null;
        Date starttime = null;
        Date endtime = null;
        Long pagenum = null;
        Long pagesize = null;
        String collection = db_suddenRiseData;
        Integer total = 0;
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString() + " 00:00:00");
        }
        if (paramMap.get("endtime") != null) {
            endtime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString() + " 23:59:59");
        }
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pagenum = Long.valueOf(paramMap.get("pagenum").toString());
            pagesize = Long.valueOf(paramMap.get("pagesize").toString());
        }
        List<Document> mappedResults = new ArrayList<>();
        Map<String, Object> changelist = new HashMap<>();
        changelist.put("time", "$ChangeTime");
        changelist.put("code", "$PollutantCode");
        //只查分钟突变数据
        criteria.and("DataGatherCode").in(dgimns).and("ChangeTime").gte(starttime).lte(endtime).and("DataType").is("MinuteData");
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", "ChangeTime", "PollutantCode", "ChangeMultiple").and(DateOperators.DateToString.dateOf("ChangeTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("daytime"));
        operations.add(Aggregation.group("DataGatherCode", "daytime")
                .min("ChangeTime").as("mintime")
                .max("ChangeTime").as("maxtime")
                .min("ChangeMultiple").as("minmultiple")
                .max("ChangeMultiple").as("maxmultiple")
                .push(changelist).as("changelist"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "daytime"));
        //插入分页、排序条件
        if (pagenum != null && pagesize != null) {
            operations.add(Aggregation.skip((pagenum - 1) * pagesize));
            operations.add(Aggregation.limit(pagesize));
        }
        mappedResults = mongoTemplate.aggregate(newAggregation(operations), collection, Document.class).getMappedResults();
        //总条数
        List<Map> data = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode").and(DateOperators.DateToString.dateOf("ChangeTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                group("DataGatherCode", "MonitorTime"), count().as("count")), collection, Map.class).getMappedResults();
        if (data.size() > 0) {
            String count = data.get(0).get("count").toString();
            total = Integer.valueOf(count);
        }
        if (mappedResults != null && mappedResults.size() > 0) {
            //获取污染物信息
            List<Integer> pollutanttypes = (List<Integer>) paramMap.get("pollutanttypes");
            paramMap.clear();
            paramMap.put("monitorpointtypes", pollutanttypes);
            List<Map<String, Object>> polist = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codeandname = new HashMap<>();
            for (Map<String, Object> codemap : polist) {
                if (codemap.get("code") != null) {
                    codeandname.put(codemap.get("code").toString(), codemap.get("name"));
                }
            }
            List<Document> onepoint;
            List<String> timelist = new ArrayList<>();
            List<String> codes = new ArrayList<>();
            String minutetime;
            String code;
            for (Document map : mappedResults) {
                Map<String, Object> onemap = new HashMap<>();
                onepoint = (List<Document>) map.get("changelist");
                timelist.clear();
                codes.clear();
                String noread = "";
                String pollutantStr = "";
                String min = "";
                String max = "";
                if (onepoint != null && onepoint.size() > 0) {
                    //按时间大小排序
                    onepoint = onepoint.stream().sorted(Comparator.comparing((m) -> DataFormatUtil.getDateYMDHMS(m.getDate("time")))).collect(Collectors.toList());
                    for (Document twomap : onepoint) {
                        minutetime = DataFormatUtil.getDateHM(twomap.getDate("time"));
                        code = twomap.getString("code");
                        if (!timelist.contains(minutetime)) {
                            timelist.add(minutetime);
                        }
                        if (!codes.contains(code)) {
                            codes.add(code);
                        }
                    }
                    if (timelist.size() > 0) {
                        for (String str : timelist) {
                            noread = noread + str + "、";
                        }
                    }
                    if (codes.size() > 0) {
                        for (String str : codes) {
                            if (codeandname.get(str) != null) {
                                pollutantStr = pollutantStr + codeandname.get(str) + "、";
                            }
                        }
                    }
                    if (!"".equals(noread)) {
                        noread = noread.substring(0, noread.length() - 1);
                    }
                    if (!"".equals(pollutantStr)) {
                        pollutantStr = pollutantStr.substring(0, pollutantStr.length() - 1);
                    }
                }
                //突变幅度
                min = map.get("minmultiple") != null ? DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(map.get("minmultiple").toString()) * 100)) : "0";
                max = map.get("maxmultiple") != null ? DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(map.get("maxmultiple").toString()) * 100)) : "0";
                if (!"".equals(min) && !"".equals(max)) {
                    if (!min.equals(max)) {//最大超标倍数和最小超标倍数不相等
                        onemap.put("flowrate", min + "%-" + max + "%");
                    } else {//相等
                        onemap.put("flowrate", min + "%");
                    }
                } else {
                    onemap.put("flowrate", "-");
                }
                onemap.put("DataGatherCode", map.get("DataGatherCode"));
                onemap.put("monitortime", map.get("daytime"));
                onemap.put("noread", noread);
                onemap.put("pollutantStr", pollutantStr);
                onemap.put("starttime", map.get("mintime") != null ? DataFormatUtil.getDateYMDHM(map.getDate("mintime")) : "");
                onemap.put("endtime", map.get("maxtime") != null ? DataFormatUtil.getDateYMDHM(map.getDate("maxtime")) : "");
                resultlist.add(onemap);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", resultlist);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/06/06 0006 上午 11::25
     * @Description: 根据监测类型和数据类型获取污染物监测值（APP）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorDataByParamMapForApp(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        String pollutantcode = paramMap.get("pollutantcode").toString();
        String monitorpointtype = paramMap.get("monitorpointtype").toString();
        //排口流量因子代码集合（废水，废气因子代码不同）
        List<String> flowCode = Arrays.asList("b01", "b02");
        Map<String, Object> pollutantmap = (Map<String, Object>) paramMap.get("pollutantmap");
        Map<String, Object> parm = new HashMap<>();
        parm.put("pollutanttype", monitorpointtype);
        parm.put("codes", flowCode);
        List<Map<String, Object>> ll_infos = pollutantFactorMapper.getPollutantsByCodesAndType(parm);
        String ll_unit = "";
        if (ll_infos != null && ll_infos.size() > 0 && ll_infos.get(0) != null) {
            ll_unit = ll_infos.get(0).get("unit") != null ? ll_infos.get(0).get("unit").toString() : "";
        }
        //判断污染物是否需要展示折算值  和排放量
        boolean iszs = false;
        if (pollutantmap.get("ishasconvertdata") != null && "1".equals(pollutantmap.get("ishasconvertdata").toString())) {
            iszs = true;
        }
        boolean ispfl = false;
        if (pollutantmap.get("isshowflow") != null && "1".equals(pollutantmap.get("isshowflow").toString())) {
            ispfl = true;
        }
        String Liststr = "";
        String timestr = "";
        String valuestr = "";
        String zsvaluestr = "";
        String pfl_collection = "";
        String pfl_valuestr = "";
        String pfl_Liststr = "";
        String pflunit = "";
        if ("RealTimeData".equals(paramMap.get("collection").toString())) {
            Liststr = "RealDataList";
            timestr = "%Y-%m-%d %H:%M:%S";
            valuestr = "MonitorValue";
            zsvaluestr = "ConvertConcentration";
        } else if ("MinuteData".equals(paramMap.get("collection").toString())) {
            Liststr = "MinuteDataList";
            timestr = "%Y-%m-%d %H:%M";
            valuestr = "AvgStrength";
            zsvaluestr = "AvgConvertStrength";
        } else if ("HourData".equals(paramMap.get("collection").toString())) {
            Liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
            valuestr = "AvgStrength";
            zsvaluestr = "AvgConvertStrength";
            pfl_collection = "HourFlowData";
            pfl_valuestr = "AvgFlow";
            pfl_Liststr = "HourFlowDataList";
            pflunit = "千克/时";
        } else if ("DayData".equals(paramMap.get("collection").toString())) {
            Liststr = "DayDataList";
            timestr = "%Y-%m-%d";
            valuestr = "AvgStrength";
            zsvaluestr = "AvgConvertStrength";
            pfl_collection = "DayFlowData";
            pfl_valuestr = "AvgFlow";
            pfl_Liststr = "DayFlowDataList";
            pflunit = "千克/日";
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> count_operations = new ArrayList<>();
        count_operations.add(Aggregation.match(criteria));
        count_operations.add(Aggregation.project("DataGatherCode", "MonitorTime", Liststr));
        count_operations.add(Aggregation.group("DataGatherCode").count().as("num"));
        Aggregation count_aggregationList = Aggregation.newAggregation(count_operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> count_pageResults = mongoTemplate.aggregate(count_aggregationList, paramMap.get("collection").toString(), Document.class);
        List<Document> count_listItems = count_pageResults.getMappedResults();
        int total = 0;
        if (count_listItems != null && count_listItems.size() > 0) {
            total = count_listItems.get(0).getInteger("num");
            if (total > 0) {
                //浓度
                List<AggregationOperation> operations = new ArrayList<>();
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", Liststr)
                        .and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
                if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                    operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                    operations.add(Aggregation.limit(pageEntity.getPageSize()));
                }
                Aggregation aggregationList = Aggregation.newAggregation(operations)
                        .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
                AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, paramMap.get("collection").toString(), Document.class);
                List<Document> listItems = pageResults.getMappedResults();
                //排放量
                List<Document> pfl_result = getPollutantFlowData(paramMap, pfl_Liststr, pfl_collection, timestr);
                //遍历数据
                String thetime;
                List<Document> pollutantlist;
                List<Document> pfl_pollutantlist;
                for (Document document : listItems) {
                    Map<String, Object> onemap = new HashMap<>();
                    thetime = document.getString("MonitorTime");
                    pollutantlist = (List<Document>) document.get(Liststr);
                    if (pollutantlist != null) {
                        for (Document doc : pollutantlist) {
                            if (pollutantcode.equals(doc.getString("PollutantCode"))) {
                                onemap.put("monitortime", thetime);
                                onemap.put("monitorvalue", doc.get(valuestr));
                                onemap.put("isover", doc.get("IsOver"));
                                onemap.put("isoverstandard", doc.get("IsOverStandard"));
                                onemap.put("issuddenchange", doc.get("IsSuddenChange"));
                                onemap.put("isexception", doc.get("IsException"));
                                if (iszs) {
                                    onemap.put("zs_value", doc.get(zsvaluestr));
                                }
                            }
                            if (!flowCode.contains(pollutantcode)) {
                                if (flowCode.contains(doc.getString("PollutantCode"))) {
                                    onemap.put("ll_value", doc.get(valuestr));
                                    onemap.put("ll_unit", ll_unit);
                                }
                            }
                        }
                    }
                    if (ispfl && onemap.get("monitorvalue") != null) {//是排放量污染物
                        for (Document pfl_document : pfl_result) {
                            if (thetime.equals(pfl_document.getString("MonitorTime"))) {
                                pfl_pollutantlist = (List<Document>) pfl_document.get(pfl_Liststr);
                                if (pfl_pollutantlist != null) {
                                    for (Document pfl_doc : pfl_pollutantlist) {
                                        onemap.put("pfl_value", (pfl_doc.get(pfl_valuestr) != null && !"".equals(pfl_doc.get(pfl_valuestr).toString())) ? DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(pfl_doc.get(pfl_valuestr).toString())) : "");
                                        onemap.put("pfl_unit", pflunit);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (onemap.get("monitorvalue") != null) {
                        result.add(onemap);
                    }
                }
            }
        }
        resultmap.put("total", total);
        resultmap.put("datalist", result);
        return resultmap;
    }

    @Override
    public Map<String, Map<String, Map<String, Object>>> getMnAndCodeAndLevelStandardData(Map<String, Object> paramMap) {


        int monitorpointtype = Integer.parseInt(paramMap.get("outputtype") + "");
        List<Map<String, Object>> standardDataList;

        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndLevelStandardData;
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case StinkEnum: //恶臭监测点（环境恶臭+厂界恶臭）
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case WasteWaterEnum: //废水
                standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case WasteGasEnum: //废气
            case SmokeEnum: //烟气
                standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case RainEnum: //雨水
                standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case AirEnum: //空气关联站点查询
                standardDataList = airMonitorStationMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case EnvironmentalVocEnum: //voc
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case WaterQualityEnum: //水质
                standardDataList = waterStationMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case EnvironmentalStinkEnum: //恶臭
            case MicroStationEnum://微站
            case EnvironmentalDustEnum://扬尘
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case meteoEnum: //气象
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case FactoryBoundaryStinkEnum:
                standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            case FactoryBoundarySmallStationEnum:
                standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);
                break;
            default:
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndLevelStandardData = setMnAndCodeAndLevelAndStandardData(standardDataList);


        }
        return mnAndCodeAndLevelStandardData;
    }

    @Override
    public long getMongodbCountByParam(Map<String, Object> paramMap) {
        String dataGatherCodeKey = "DataGatherCode";
        String monitorTimeKey = "MonitorTime";
        String collection = paramMap.get("collection").toString();
        switch (collection){
            case db_OverModel :
                monitorTimeKey = "FirstOverTime";
                dataGatherCodeKey = "MN";break;
            case db_OverData :
                monitorTimeKey = "OverTime";
                break;
        }
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where(dataGatherCodeKey).in(mns));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where(monitorTimeKey).gte(startDate).lte(endDate));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                query.addCriteria(Criteria.where(monitorTimeKey).gte(startDate));
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where(monitorTimeKey).lte(endDate));
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
        if (paramMap.get("isoverstandard") != null) {
            if ("RealTimeData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("RealDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("MinuteData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("MinuteDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("HourData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("HourDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            } else if ("DayData".equals(paramMap.get("collection"))) {
                query.addCriteria(Criteria.where("DayDataList.IsOverStandard").is(paramMap.get("isoverstandard")));
            }
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, monitorTimeKey));
        } else {
            query.with(new Sort(Sort.Direction.DESC, monitorTimeKey));
        }
        return mongoTemplate.count(query, Document.class, collection);
    }

    /**
     * 获取污染物排放量数据
     */
    private List<Document> getPollutantFlowData(Map<String, Object> paramMap, String pfl_Liststr, String pfl_collection, String timestr) {
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        List<Document> result = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        if (paramMap.get("monitorpointtype") != null && !"".equals(pfl_collection)) {
            int monitorpointtype = Integer.valueOf(paramMap.get("monitorpointtype").toString());
            //判断是否为 废水、废气、雨水类型
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                PageEntity<Document> pageEntity = new PageEntity<>();
                if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                    pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
                    pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
                }
                Criteria criteria = new Criteria();
                criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
                List<AggregationOperation> operations = new ArrayList<>();
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", "FlowUnit", pfl_Liststr)
                        .and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
                if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                    operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                    operations.add(Aggregation.limit(pageEntity.getPageSize()));
                }
                Aggregation aggregationList = Aggregation.newAggregation(operations)
                        .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
                AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, pfl_collection, Document.class);
                result = pageResults.getMappedResults();
            }
        }
        return result;
    }
}
