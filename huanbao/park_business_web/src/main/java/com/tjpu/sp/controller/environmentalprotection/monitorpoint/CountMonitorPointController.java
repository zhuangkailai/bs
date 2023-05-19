package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineWaterQualityService;
import com.tjpu.sp.service.environmentalprotection.petition.PetitionInfoService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;

/**
 * @author: chengzq
 * @date: 2019/6/21 0021 下午 3:49
 * @Description: 监测点公共控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("monitorPoint")
public class CountMonitorPointController {

    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private OnlineService onlineService;


    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private MonitorPointService monitorPointService;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private OnlineWaterQualityService onlineWaterQualityService;
    @Autowired
    private OnlineMonitorService onlineMonitorService;
    @Autowired
    private PollutantService pollutantService;


    @Autowired
    private RiverSectionService riverSectionService;

    @Autowired
    private PetitionInfoService petitionInfoService;
    private String DB_OverModel = "OverModel";


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:50
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getAllMonitorPointInfosByMonitorPointTypes", method = RequestMethod.POST)
    public Object getAllMonitorPointInfosByMonitorPointTypes(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            //获取所有视频信息
            Map<String, Object> pointidAndrtsp = videoCameraService.getAllVideoInfoGroupByPointID();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Integer i : monitorpointtypes) {
                    switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(i)) {
                        case WasteWaterEnum: //废水
                            List<Map<String, Object>> allMonitorWaterOutPutAndStatusInfo = waterOutPutInfoService.getAllMonitorWaterOutPutAndStatusInfo();//废水
                            getMonitorPointVideoInfo(allMonitorWaterOutPutAndStatusInfo, pointidAndrtsp);
                            resultMap.put("wastewater", allMonitorWaterOutPutAndStatusInfo);
                            break;
                        case WasteGasEnum: //废气
                            List<Map<String, Object>> allMonitorGasOutPutAndStatusInfo = gasOutPutInfoService.getAllMonitorGasOutPutAndStatusInfo();//废气
                            getMonitorPointVideoInfo(allMonitorGasOutPutAndStatusInfo, pointidAndrtsp);
                            resultMap.put("wastegas", allMonitorGasOutPutAndStatusInfo);
                            break;
                        case SmokeEnum: //烟气
                            List<Map<String, Object>> allMonitorSmokeOutPutAndStatusInfo = gasOutPutInfoService.getAllMonitorSmokeOutPutAndStatusInfo();//废气
                            getMonitorPointVideoInfo(allMonitorSmokeOutPutAndStatusInfo, pointidAndrtsp);
                            resultMap.put("smoke", allMonitorSmokeOutPutAndStatusInfo);
                            break;

                        case RainEnum: //雨水
                            List<Map<String, Object>> allMonitorRainOutPutAndStatusInfo = waterOutPutInfoService.getAllMonitorRainOutPutAndStatusInfo();//雨水
                            getMonitorPointVideoInfo(allMonitorRainOutPutAndStatusInfo, pointidAndrtsp);
                            resultMap.put("rain", allMonitorRainOutPutAndStatusInfo);
                            break;
                        case AirEnum: //空气
                            List<Map<String, Object>> allMonitorAirStationAndStatusInfo = airMonitorStationService.getAllMonitorAirStationAndStatusInfo();//空气
                            getMonitorPointVideoInfo(allMonitorAirStationAndStatusInfo, pointidAndrtsp);
                            airMonitorStationService.setAirStationAqi(allMonitorAirStationAndStatusInfo);
                            resultMap.put("airstation", allMonitorAirStationAndStatusInfo);
                            break;
                        case EnvironmentalVocEnum: //voc
                            List<Map<String, Object>> allMonitorEnvironmentalVocAndStatusInfo = otherMonitorPointService.getAllMonitorEnvironmentalVocAndStatusInfo();//VOC
                            getMonitorPointVideoInfo(allMonitorEnvironmentalVocAndStatusInfo, pointidAndrtsp);
                            resultMap.put("voc", allMonitorEnvironmentalVocAndStatusInfo);
                            break;
                        case EnvironmentalStinkEnum: //恶臭
                            List<Map<String, Object>> allMonitorEnvironmentalStinkAndStatusInfo = otherMonitorPointService.getAllMonitorEnvironmentalStinkAndStatusInfo();//恶臭
                            getMonitorPointVideoInfo(allMonitorEnvironmentalStinkAndStatusInfo, pointidAndrtsp);
                            resultMap.put("stink", allMonitorEnvironmentalStinkAndStatusInfo);
                            break;
                        case EnvironmentalDustEnum: //扬尘
                            List<Map<String, Object>> allMonitorEnvironmentalDustAndStatusInfo = otherMonitorPointService.getAllMonitorEnvironmentalStinkAndStatusInfo();//恶臭
                            getMonitorPointVideoInfo(allMonitorEnvironmentalDustAndStatusInfo, pointidAndrtsp);
                            resultMap.put("entboundarydust", allMonitorEnvironmentalDustAndStatusInfo);
                            break;
                        case MicroStationEnum: //微站
                            List<Map<String, Object>> allMonitorMicroStationAndStatusInfo = otherMonitorPointService.getAllMonitorMicroStationAndStatusInfo();//微站
                            getMonitorPointVideoInfo(allMonitorMicroStationAndStatusInfo, pointidAndrtsp);
                            resultMap.put("stink", allMonitorMicroStationAndStatusInfo);
                            break;
                        case FactoryBoundaryStinkEnum: //厂界恶臭
                            List<Map<String, Object>> allMonitorUnstenchAndStatusInfo = outPutUnorganizedService.getAllMonitorUnstenchAndStatusInfo();//厂界恶臭
                            getMonitorPointVideoInfo(allMonitorUnstenchAndStatusInfo, pointidAndrtsp);
                            resultMap.put("entboundarystink", allMonitorUnstenchAndStatusInfo);
                            break;
                        case FactoryBoundarySmallStationEnum: //厂界小型站
                            List<Map<String, Object>> allMonitorUnMINIAndStatusInfo = outPutUnorganizedService.getAllMonitorUnMINIAndStatusInfo();//厂界小型站
                            getMonitorPointVideoInfo(allMonitorUnMINIAndStatusInfo, pointidAndrtsp);
                            resultMap.put("entboundarysmallstation", allMonitorUnMINIAndStatusInfo);

                        case WaterQualityEnum: //水质
                            List<Map<String, Object>> allWaterStationAndStatusInfo = waterStationService.getAllWaterStationAndStatusInfo();//水质
                            getMonitorPointVideoInfo(allWaterStationAndStatusInfo, pointidAndrtsp);
                            onlineWaterQualityService.setWaterQaulity(allWaterStationAndStatusInfo);
                            if (allWaterStationAndStatusInfo != null && allWaterStationAndStatusInfo.size() > 0) {
                                for (Map<String, Object> listmap : allWaterStationAndStatusInfo) {
                                    if (listmap.get("WaterLevelName") != null) {
                                        listmap.put("monitorpointname", listmap.get("monitorpointname") + "【" + listmap.get("WaterLevelName") + "】");
                                    }
                                }
                            }
                            resultMap.put("waterstation", allWaterStationAndStatusInfo);
                            break;
                        case meteoEnum: //气象站
                            List<Map<String, Object>> allMeteoStationAndStatusInfo = otherMonitorPointService.getAllMonitorEnvironmentalMeteoAndStatusInfo();//气象
                            getMonitorPointVideoInfo(allMeteoStationAndStatusInfo, pointidAndrtsp);
                            resultMap.put("meteostation", allMeteoStationAndStatusInfo);
                            break;
                        case videoEnum: //视频
                            List<Map<String, Object>> allMonitorVideoAndStatusInfo = videoCameraService.getAllMonitorVideoInfos();//视频
                            resultMap.put("vedio", allMonitorVideoAndStatusInfo);
                            break;
                    }
                }
            } else {//当所传类型为空时，查询所有类型的点位信息
                List<Map<String, Object>> allMonitorRainOutPutAndStatusInfo = waterOutPutInfoService.getAllMonitorRainOutPutAndStatusInfo();//雨水
                getMonitorPointVideoInfo(allMonitorRainOutPutAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorWaterOutPutAndStatusInfo = waterOutPutInfoService.getAllMonitorWaterOutPutAndStatusInfo();//废水
                getMonitorPointVideoInfo(allMonitorWaterOutPutAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorGasOutPutAndStatusInfo = gasOutPutInfoService.getAllMonitorGasOutPutAndStatusInfo();//废气
                getMonitorPointVideoInfo(allMonitorGasOutPutAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorSmokeOutPutAndStatusInfo = gasOutPutInfoService.getAllMonitorSmokeOutPutAndStatusInfo();//烟气
                getMonitorPointVideoInfo(allMonitorSmokeOutPutAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorUnMINIAndStatusInfo = outPutUnorganizedService.getAllMonitorUnMINIAndStatusInfo();//厂界小型站
                getMonitorPointVideoInfo(allMonitorUnMINIAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorUnstenchAndStatusInfo = outPutUnorganizedService.getAllMonitorUnstenchAndStatusInfo();//厂界恶臭
                getMonitorPointVideoInfo(allMonitorUnstenchAndStatusInfo, pointidAndrtsp);
                //List<Map<String, Object>> allMonitorUnDustAndStatusInfo = outPutUnorganizedService.getAllMonitorUnDustAndStatusInfo();//厂界扬尘
                List<Map<String, Object>> allMonitorAirStationAndStatusInfo = airMonitorStationService.getAllMonitorAirStationAndStatusInfo();//空气
                getMonitorPointVideoInfo(allMonitorAirStationAndStatusInfo, pointidAndrtsp);
                airMonitorStationService.setAirStationAqi(allMonitorAirStationAndStatusInfo);
                List<Map<String, Object>> allMonitorEnvironmentalVocAndStatusInfo = otherMonitorPointService.getAllMonitorEnvironmentalVocAndStatusInfo();//VOC
                getMonitorPointVideoInfo(allMonitorEnvironmentalVocAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorEnvironmentalStinkAndStatusInfo = otherMonitorPointService.getAllMonitorEnvironmentalStinkAndStatusInfo();//恶臭
                getMonitorPointVideoInfo(allMonitorEnvironmentalStinkAndStatusInfo, pointidAndrtsp);
                List<Map<String, Object>> allMonitorVideoAndStatusInfo = videoCameraService.getAllMonitorVideoInfos();//视频
                List<Map<String, Object>> allWaterStationAndStatusInfo = waterStationService.getAllWaterStationAndStatusInfo();//水质
                getMonitorPointVideoInfo(allWaterStationAndStatusInfo, pointidAndrtsp);
                onlineWaterQualityService.setWaterQaulity(allWaterStationAndStatusInfo);
                resultMap.put("vedio", allMonitorVideoAndStatusInfo);//视频
                resultMap.put("stink", allMonitorEnvironmentalStinkAndStatusInfo);//恶臭
                resultMap.put("voc", allMonitorEnvironmentalVocAndStatusInfo);//voc
                resultMap.put("airstation", allMonitorAirStationAndStatusInfo);//空气站
                resultMap.put("wastewater", allMonitorWaterOutPutAndStatusInfo);//废水
                resultMap.put("rain", allMonitorRainOutPutAndStatusInfo);//雨水
                resultMap.put("wastegas", allMonitorGasOutPutAndStatusInfo);//废气
                resultMap.put("smoke", allMonitorSmokeOutPutAndStatusInfo);//烟气
                resultMap.put("entboundarysmallstation", allMonitorUnMINIAndStatusInfo);//厂界小型站
                resultMap.put("entboundarystink", allMonitorUnstenchAndStatusInfo);//厂界恶臭
                // resultMap.put("entboundarydust", allMonitorUnDustAndStatusInfo);//厂界扬尘
                resultMap.put("waterstation", allWaterStationAndStatusInfo);//水质
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/6/23 0023 下午 2:08
     * @Description: 添加点位视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointVideoInfo", method = RequestMethod.POST)
    private void getMonitorPointVideoInfo(List<Map<String, Object>> pointlist, Map<String, Object> pointidAndrtsp) {
        if (pointlist != null && pointlist.size() > 0) {
            for (Map<String, Object> map : pointlist) {
                String pointid = (map.get("pkid") != null || map.get("PKID") != null) ? (map.get("pkid") != null ? map.get("pkid").toString() : map.get("PKID").toString()) : "";
                if (!"".equals(pointid)) {
                    map.put("rtsplist", pointidAndrtsp.get(pointid));
                }
            }
        }
    }


    /**
     * @Description: 统计雨水点位、一企一管企业数数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/24 14:32
     */
    @RequestMapping(value = "countWGPointData", method = RequestMethod.POST)
    public Object countWGPointData() {

        List<Map<String, Object>> dataList = waterOutPutInfoService.countWGPointData();

        return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, dataList);
    }


    /**
     * @author: xsm
     * @date: 2019/6/27 0027 下午 1:55
     * @Description: 根据监测点类型获取该类型下的所有监测点（恶臭、voc、厂界恶臭、厂界小型站）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointSelectDatasByMonitorPointType", method = RequestMethod.POST)
    public Object getMonitorPointSelectDatasByMonitorPointType(@RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> listmap = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case EnvironmentalVocEnum: //voc
                    listmap = otherMonitorPointService.getAllMonitorEnvironmentalVocAndStatusInfo();//VOC
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    listmap = otherMonitorPointService.getAllMonitorEnvironmentalStinkAndStatusInfo();//恶臭
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    listmap = outPutUnorganizedService.getAllMonitorUnstenchAndStatusInfo();//厂界恶臭
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    listmap = outPutUnorganizedService.getAllMonitorUnMINIAndStatusInfo();//厂界小型站
                    break;
            }
            if (listmap != null && listmap.size() > 0) {
                for (Map<String, Object> map : listmap) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("MonitorPointName"));
                    objmap.put("value", map.get("pkid"));
                    result.add(objmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取监测点数据传输时间列表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/5/31 11:47
     */
    @RequestMapping(value = "getMonitorPointDataTimeSetListByParam", method = RequestMethod.POST)
    public Object getMonitorPointDataTimeSetListByParam(
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
            @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
            @RequestJson(value = "pollutantname", required = false) String pollutantname,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutantname", pollutantname);
            if (pagesize != null && pagenum != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> dataList = monitorPointService.getMonitorPointDataTimeSetListByParam(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo(dataList);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", pageInfo.getList());
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 设置监测点数据传输时间
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/5/31 11:47
     */
    @RequestMapping(value = "setMonitorPointDataTimeSet", method = RequestMethod.POST)
    public Object setMonitorPointDataTimeSet(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "alarmtime", required = false) Integer alarmtime,
            @RequestJson(value = "zerotime", required = false) Integer zerotime,
            @RequestJson(value = "continuitytime", required = false) Integer continuitytime
    ) {
        try {
            //1,设置报警次数、零值次数、连续值次数

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> setDataList = monitorPointService.getTimeDataSetByParam(paramMap);
            if (setDataList.size() > 0) {
                List<Map<String, Object>> updateDataList = new ArrayList<>();
                Map<String, Integer> mnAndRealTime = new HashMap<>();
                Map<String, Integer> mnAndMinuteTime = new HashMap<>();
                String mnCommon;
                Set<String> mns = new HashSet<>();
                for (Map<String, Object> setMap : setDataList) {
                    if (setMap.get("dgimn") != null) {
                        mnCommon = setMap.get("dgimn").toString();
                        mns.add(mnCommon);
                        if (setMap.get("realtimetime") != null) {
                            mnAndRealTime.put(mnCommon, Integer.parseInt(setMap.get("realtimetime").toString()));
                        }
                        if (setMap.get("minutetime") != null) {
                            mnAndMinuteTime.put(mnCommon, Integer.parseInt(setMap.get("minutetime").toString()));
                        }
                    }
                }
                int num;
                for (String mnIndex : mns) {
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("dgimn", mnIndex);
                    updateMap.put("pollutantcode", pollutantcode);
                    if (alarmtime != null) {
                        if (datatype.equals("realtimedata") && mnAndRealTime.containsKey(mnIndex)) {
                            num = alarmtime / mnAndRealTime.get(mnIndex);
                            updateMap.put("alarmcontroltimes", num);
                        } else if (datatype.equals("minutedata") && mnAndMinuteTime.containsKey(mnIndex)) {
                            num = alarmtime / mnAndMinuteTime.get(mnIndex);
                            updateMap.put("alarmcontroltimes", num);
                        }
                    } else {
                        updateMap.put("alarmcontroltimes", null);
                    }
                    if (zerotime != null && mnAndRealTime.containsKey(mnIndex)) {
                        num = zerotime / mnAndRealTime.get(mnIndex);
                        updateMap.put("zerovaluetimes", num);
                    } else {
                        updateMap.put("zerovaluetimes", null);
                    }
                    if (continuitytime != null && mnAndRealTime.containsKey(mnIndex)) {
                        num = continuitytime / mnAndRealTime.get(mnIndex);
                        updateMap.put("continuityvaluetimes", num);
                    } else {
                        updateMap.put("continuityvaluetimes", null);
                    }
                    updateDataList.add(updateMap);
                }
                //批量更新污染物设置表
                monitorPointService.updateListData(updateDataList, monitorpointtype);
                //2,发送mq消息
                sendMqMessage();
            }

            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void sendMqMessage() {
        Map<String, Object> mqMap = new HashMap<>();
        mqMap.put("monitorpointtype", "");
        mqMap.put("dgimn", "");
        mqMap.put("monitorpointid", "");
        mqMap.put("fkpollutionid", "");
        rabbitmqController.sendAQPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }

    /**
     * @author: xsm
     * @date: 2019/6/27 0027 下午 2:37
     * @Description: 根据监测点类型和点位ID获取该点位的所有监测因子（恶臭、voc、厂界恶臭、厂界小型站）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype，monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPollutantsByMonitorPointTypeAndID", method = RequestMethod.POST)
    public Object getMonitorPollutantsByMonitorPointTypeAndID(@RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype,
                                                              @RequestJson(value = "monitorpointid", required = true) String monitorpointid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listmap = new ArrayList<>();
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case EnvironmentalVocEnum: //voc
                    paramMap.put("pkidlist", Arrays.asList(monitorpointid));
                    listmap = otherMonitorPointService.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);//VOC
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    paramMap.put("pkidlist", Arrays.asList(monitorpointid));
                    listmap = otherMonitorPointService.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);//恶臭
                    break;
                case AirEnum: //空气站
                    paramMap.put("pkidlist", Arrays.asList(monitorpointid));
                    List<Map<String, Object>> listMapTemp = airMonitorStationService.getAirStationAllPollutantsByIDAndType(paramMap);//空气站
                    if (listMapTemp.size() > 0) {
                        List<String> sixIndexList = CommonTypeEnum.getSixIndexList();
                        for (Map<String, Object> mapTemp : listMapTemp) {
                            if (sixIndexList.contains(mapTemp.get("code"))) {
                                listmap.add(mapTemp);
                            }
                        }
                    }
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    paramMap.put("pkidlist", Arrays.asList(monitorpointid));
                    listmap = outPutUnorganizedService.getEntBoundaryAllPollutantsByIDAndType(paramMap);//厂界恶臭
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    paramMap.put("pkidlist", Arrays.asList(monitorpointid));
                    listmap = outPutUnorganizedService.getEntBoundaryAllPollutantsByIDAndType(paramMap);//厂界小型站
                    break;
            }
            return AuthUtil.parseJsonKeyToLower("success", listmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 9:57
     * @Description:gis-根据数据标记类型获取单个或多个点位的基本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getAllPollutionAndMonitorPointInfoByTypes", method = RequestMethod.POST)
    public Object getAllPollutionAndMonitorPointInfoByTypes(@RequestJson(value = "monitorpointtypes", required = false) List<String> monitorpointtypes) throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> pollutionMap = new HashMap<>();
            pollutionMap = pollutionService.getAllPollutionInfoAndStatus();//污染源
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (String str : monitorpointtypes) {
                    Map<String, Object> resultMap = new HashMap<>();
                    if (CommonTypeEnum.GisMonitorPointTypeEnum.getCodeByString(str) != null) {
                        switch (CommonTypeEnum.GisMonitorPointTypeEnum.getCodeByString(str)) {
                            case GisPollutionEnum: //污染源
                                result.put("pollution", pollutionMap);//污染源
                                break;
                            case GisWasteWaterEnum: //废水
                                resultMap = waterOutPutInfoService.getAllMonitorWaterOutPutInfo(pollutionMap);//废水
                                result.put("wastewater", resultMap);//废水
                                break;
                            case GisWasteGasEnum: //废气
                                resultMap = gasOutPutInfoService.getAllMonitorGasOutPutInfo(pollutionMap);//废气
                                result.put("wastegas", resultMap);//废气
                                break;
                            case GisRainEnum: //雨水
                                resultMap = waterOutPutInfoService.getAllMonitorRainOutPutInfo(pollutionMap);//雨水
                                result.put("rain", resultMap);//雨水
                                break;
                            case GisAirEnum: //空气
                                resultMap = airMonitorStationService.getAllAirMonitorStationInfo();//空气
                                result.put("airstation", resultMap);//空气站
                                break;
                            case GisEnvironmentalVocEnum: //voc
                                paramMap.clear();
                                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                                paramMap.put("orderfield", "status");
                                resultMap = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//VOC
                                result.put("voc", resultMap);//voc
                                break;
                            case GisMicroStationEnum: //微站
                                paramMap.clear();
                                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                                paramMap.put("orderfield", "status");
                                resultMap = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//恶臭
                                result.put("tvoc", resultMap);//微站
                                break;
                            case GisEnvironmentalStinkEnum: //恶臭
                                paramMap.clear();
                                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                                paramMap.put("orderfield", "status");
                                resultMap = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//恶臭
                                result.put("stink", resultMap);//恶臭
                                break;
                            case GisWaterQualityEnum: //水质
                                paramMap.clear();
                                resultMap = waterStationService.getAllWaterStationByType(paramMap);
                                result.put("WaterQuality", resultMap);//水质
                                break;
                            case GisvideoEnum: //视频
                                resultMap = videoCameraService.getAllMonitorVideoInfo();//视频
                                result.put("vedio", resultMap);//视频
                                break;
                            case GisFactoryBoundaryStinkEnum: //厂界恶臭
                                paramMap.clear();
                                paramMap.put("orderfield", "status");
                                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                                resultMap = outPutUnorganizedService.getAllUnorganizedInfoByType(paramMap, pollutionMap);//厂界恶臭
                                result.put("entboundarystink", resultMap);//厂界恶臭
                                break;
                            case GisFactoryBoundarySmallStationEnum: //厂界小型站
                                paramMap.clear();
                                paramMap.put("orderfield", "status");
                                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                                resultMap = outPutUnorganizedService.getAllUnorganizedInfoByType(paramMap, pollutionMap);//厂界小型站
                                result.put("entboundarysmallstation", resultMap);//厂界小型站
                                break;
                            case GisMeteoEnum: //气象站
                                paramMap.clear();
                                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                                resultMap = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//气象站
                                result.put("meteostation", resultMap);
                                break;
                        }
                    }

                }
            } else {//当所传类型为空时，查询所有类型的点位信息
                result.put("pollution", pollutionMap);//污染源
                Map<String, Object> resultMap = new HashMap<>();

                Map<String, Object> wastewater = waterOutPutInfoService.getAllMonitorWaterOutPutInfo(pollutionMap);//废水
                result.put("wastewater", wastewater);//废水

                Map<String, Object> wastegas = gasOutPutInfoService.getAllMonitorGasOutPutInfo(pollutionMap);//废气
                result.put("wastegas", wastegas);//废气

                Map<String, Object> rain = waterOutPutInfoService.getAllMonitorRainOutPutInfo(pollutionMap);//雨水
                result.put("rain", rain);//雨水

                Map<String, Object> airstation = airMonitorStationService.getAllAirMonitorStationInfo();//空气
                result.put("airstation", airstation);//空气站

                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                Map<String, Object> allVOCPoint = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//VOC
                result.put("voc", allVOCPoint);//voc

                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                Map<String, Object> stink = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//恶臭
                result.put("stink", stink);//恶臭

                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                Map<String, Object> meteo = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//气象
                result.put("meteo", meteo);//气象

                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                Map<String, Object> tvoc = otherMonitorPointService.getAllOtherMonitorPointInfoByType(paramMap);//微站
                result.put("tvoc", tvoc);//微站

                Map<String, Object> vedio = videoCameraService.getAllMonitorVideoInfo();//视频
                result.put("vedio", vedio);//视频

                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                Map<String, Object> entboundarystink = outPutUnorganizedService.getAllUnorganizedInfoByType(paramMap, pollutionMap);//厂界恶臭
                result.put("entboundarystink", entboundarystink);//厂界恶臭

                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                Map<String, Object> entboundarysmallstation = outPutUnorganizedService.getAllUnorganizedInfoByType(paramMap, pollutionMap);//厂界小型站
                result.put("entboundarysmallstation", entboundarysmallstation);//厂界小型站
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 9:57
     * @Description:根据监测点类型返回按点位状态分组的点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getGroupByStatusMonitorPointInfoByType", method = RequestMethod.POST)
    public Object getGroupByStatusMonitorPointInfoByType(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype) throws Exception {
        try {
            List<Integer> monitortypes = new ArrayList<>();
            if (monitorpointtype != null) {
                monitortypes.add(monitorpointtype);
            } else {
                monitortypes = CommonTypeEnum.getAllMonitorPointTypeList();
            }
            List<Map<String, Object>> result = monitorPointService.getGroupByStatusMonitorPointInfoByTypes(monitortypes);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/2/17 0017 上午 10:18
     * @Description:根据监测类型统计各类型监测点的点位状态情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "countMonitorPointStatusNumByMonitorPointTypes", method = RequestMethod.POST)
    public Object countMonitorPointStatusNumByMonitorPointTypes(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                                @RequestJson(value = "userauth", required = false) Boolean userauth) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> listdata = deviceStatusService.countMonitorPointStatusNumByMonitorPointTypes(paramMap, userauth);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 9:45
     * @Description:统计按点位状态分组的各类型点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "countAllMonitorPointNumGroupByStatusByTypes", method = RequestMethod.POST)
    public Object countAllMonitorPointNumGroupByStatusByTypes(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                              @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("hassecurity", 0);
            List<Map<String, Object>> listdata = deviceStatusService.countMonitorPointNumGroupByStatusByTypes(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 下午 14:36
     * @Description:根据监测类型和点位状态获取多个点位信息(包含经纬度,无数据权限)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getAllMonitorPointInfoByMonitorTypeAndPointStatus", method = RequestMethod.POST)
    public Object getAllMonitorPointInfoByMonitorTypeAndPointStatus(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                                    @RequestJson(value = "statuslist") List<String> statuslist,
                                                                    @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("statuslist", statuslist);
            paramMap.put("hassecurity", 1);
            paramMap.put("hasmeteo", 1);
            List<Map<String, Object>> listdata = deviceStatusService.getMonitorPointInfoByMonitorTypeAndPointStatus(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 9:45
     * @Description:统计按点位状态分组的各类型点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "countMonitorPointNumGroupByStatusByTypes", method = RequestMethod.POST)
    public Object countMonitorPointNumGroupByStatusByTypes(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                           @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                           @RequestJson(value = "categorys", required = false) List<String> categorys, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("userid", userid);
            List<Map<String, Object>> listdata = deviceStatusService.countMonitorPointNumGroupByStatusByTypes(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 下午 14:36
     * @Description:根据监测类型和点位状态获取多个点位信息(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfoByMonitorTypeAndPointStatus", method = RequestMethod.POST)
    public Object getMonitorPointInfoByMonitorTypeAndPointStatus(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                                 @RequestJson(value = "statuslist") List<String> statuslist,
                                                                 @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                                 @RequestJson(value = "iscategory", required = false) String iscategory,
                                                                 @RequestJson(value = "categorys", required = false) List<String> categorys,
                                                                 @RequestJson(value = "ishavepropertys", required = false) String ishavepropertys,
                                                                 HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("userid", userId);
            paramMap.put("statuslist", statuslist);
            if (monitorpointtypes != null) {
                List<Integer> typelist = (List<Integer>) paramMap.get("monitorpointtypes");
                for (Integer type : typelist) {
                    if (type == meteoEnum.getCode()) {
                        paramMap.put("hasmeteo", 1);
                    }
                }
            } else {
                paramMap.put("hasmeteo", 1);
            }
            //判断有无安全类型
            paramMap.put("hassecurity", 0);
            categorys = Arrays.asList("1");
            List<Map<String, Object>> listdata = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            //是否区分一企一管废水排口  yes 区分
            if (ishavepropertys != null) {
                List<String> types = new ArrayList<>();
                if (monitorpointtypes != null) {
                    for (Integer i : monitorpointtypes) {
                        types.add(i + "");
                    }
                }
                if ("yes".equals(ishavepropertys)) {
                    types.add("yqygoutput");
                    paramMap.put("ishavepropertys", ishavepropertys);
                }
                paramMap.put("monitorpointtypes", types);

            }
            if (iscategory != null) {//是否按敏感点传输点分组
                paramMap.put("iscategory", iscategory);
                if (monitorpointtypes == null || monitorpointtypes.size() == 0 && !"".equals(iscategory)) {
                    paramMap.put("isshow", "0");
                } else {
                    paramMap.put("isshow", "1");
                }
                listdata = deviceStatusService.getMonitorPointInfoForEnvSupervisionHomeMap(paramMap);
            } else {

                listdata = deviceStatusService.getMonitorPointInfoByMonitorTypeAndPointStatus(paramMap);
            }
            if (listdata != null && listdata.size() > 0) {
                Map<String, List<Map<String, Object>>> listMap = listdata.stream().collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    list = entry.getValue();
                    if ((AirEnum.getCode() + "").equals(entry.getKey())) {
                        airMonitorStationService.setAirStationAqi(list);
                        result.addAll(list);
                    } else if ((WaterQualityEnum.getCode() + "").equals(entry.getKey())) {
                        onlineWaterQualityService.setWaterQaulity(list);
                        if (list != null && list.size() > 0) {
                            for (Map<String, Object> listmap : list) {
                                if (listmap.get("WaterLevelName") != null) {
                                    listmap.put("monitorpointname", listmap.get("monitorpointname") + "【" + listmap.get("WaterLevelName") + "】");
                                }
                            }
                        }
                        result.addAll(list);
                    } else {
                        result.addAll(entry.getValue());
                    }

                }
            }
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/04 0004 上午 10:25
     * @Description:根据监测类型和点位iD获取单个点位信息(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfoByDgimn", method = RequestMethod.POST)
    public Object getMonitorPointInfoByDgimn(@RequestJson(value = "dgimn") String dgimn
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("hassecurity", 1);
            paramMap.put("hasmeteo", 1);
            Map<String, Object> result = deviceStatusService.getMonitorPointInfoByDgimn(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/31 0031 上午 10:19
     * @Description:获取首页地图所有监测类型信息（包含储罐、风险点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllMonitorPointTypeDataForHomeMap", method = RequestMethod.POST)
    public Object getAllMonitorPointTypeDataForHomeMap() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Map<String, Object>> result = deviceStatusService.getALLMonitorPointTypeInfoByTypes(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/4/20 0020 上午 10:19
     * @Description:根据用户数据权限获取在线监控首页的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllMonitorPointTypeDataForOnlineMonitorHomeMap", method = RequestMethod.POST)
    public Object getAllMonitorPointTypeDataForOnlineMonitorHomeMap(HttpSession session) throws Exception {
        try {
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("category", 1);
            List<Map<String, Object>> result = deviceStatusService.getAllMonitorPointTypeDataForOnlineMonitorHomeMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 3:12
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型
     * @updateUser:xsm
     * @updateDate:2022/06/09
     * @updateDescription:新增主题类型参数 themetype
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllMonitorPointTypeDataForEnvSupervisionHomeMap", method = RequestMethod.POST)
    public Object getAllMonitorPointTypeDataForEnvSupervisionHomeMap(@RequestJson(value = "ishavecategory", required = false) String ishavecategory,
                                                                     @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                                     @RequestJson(value = "categorys", required = false) List<String> categorys,
                                                                     @RequestJson(value = "ishavepropertys", required = false) String ishavepropertys,
                                                                     @RequestJson(value = "themetype", required = false) Integer themetype) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            //ishavecategory：yes或空值  恶臭、VOC按传输、敏感点分类 ；no  按监测类型分; stink  合并环境恶臭、厂界恶臭
            if (ishavecategory != null) {
                paramMap.put("ishavecategory", ishavecategory);
            }
            //监测类型
            if (monitorpointtypes != null) {
                paramMap.put("monitorpointtypes", monitorpointtypes);
            }
            //环保点位还是安全点位
            if (categorys != null) {//环保还是安全 不传根据配置文件配置  展示
                paramMap.put("categorys", categorys);
            }
            //是否区分废水一企一管
            if (ishavepropertys != null) {
                paramMap.put("ishavepropertys", ishavepropertys);
            }
            //主题类型
            if (themetype != null){
                paramMap.put("themetype", themetype);
            }
            result = deviceStatusService.getAllMonitorPointTypeDataForEnvSupervisionHomeMap(paramMap);
            //判断 是否为2.0调用
            //合并 mainname相同的类型  如 废气、烟气
            if (themetype != null && result.size()>0){
                List<Map<String, Object>> result_two = new ArrayList<>();
                //按 mainname 分组
                Map<String, List<Map<String, Object>>> collect = result.stream().filter(m -> m.get("mainname") != null).collect(Collectors.groupingBy(m -> m.get("mainname").toString()));
                for (String mainname : collect.keySet()) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("name", mainname);
                    List<Integer> typecodes = new ArrayList<>();
                    int onlinenum = 0;
                    int totalnum = 0;
                    int alarmflag = 0;
                    int ordertype = 0;
                    List<Map<String, Object>> alllist = collect.get(mainname);
                    for (Map<String, Object> twomap : alllist) {
                        if(twomap.get("code")!=null) {
                            typecodes.add(Integer.valueOf(twomap.get("code").toString()));
                            onlinenum += twomap.get("onlinenum") != null ? Integer.valueOf(twomap.get("onlinenum").toString()) : 0;
                            totalnum += twomap.get("totalnum") != null ? Integer.valueOf(twomap.get("totalnum").toString()) : 0;
                            if (twomap.get("alarmflag") != null && Integer.valueOf(twomap.get("alarmflag").toString()) > alarmflag) {
                                alarmflag = Integer.valueOf(twomap.get("alarmflag").toString());
                            }
                            if (twomap.get("OrderIndex")!=null) {
                                if (ordertype == 0) {
                                    ordertype = Integer.valueOf(twomap.get("OrderIndex").toString());
                                } else {
                                    if (Integer.valueOf(twomap.get("totalnum").toString()) < ordertype) {
                                        ordertype = Integer.valueOf(twomap.get("OrderIndex").toString());
                                    }
                                }
                            }else{
                                if (ordertype == 0) {
                                    ordertype = 999;
                                }
                            }
                        }
                    }
                    onemap.put("ordertype", ordertype);
                    onemap.put("onlinenum", onlinenum);
                    onemap.put("totalnum", totalnum);
                    onemap.put("alarmflag", alarmflag);
                    onemap.put("types", typecodes);
                    result_two.add(onemap);
                }
                if (result_two.size()>0){
                    result_two = result_two.stream().sorted(
                            Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("ordertype").toString()))
                    ).collect(Collectors.toList());
                }
                return AuthUtil.parseJsonKeyToLower("success", result_two);
            }else{
                return AuthUtil.parseJsonKeyToLower("success", result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/5/11 0011 下午 2:54
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型点位的状态（在线，离线，超标，异常，停产）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countMonitorPointStatusNumForEnvSupervisionHomeMap", method = RequestMethod.POST)
    public Object countMonitorPointStatusNumForEnvSupervisionHomeMap(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                                     @RequestJson(value = "iscategory", required = false) String iscategory,
                                                                     @RequestJson(value = "categorys", required = false) List<String> categorys,
                                                                     @RequestJson(value = "ishavepropertys", required = false) String ishavepropertys
    ) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            if (!(monitorpointtypes != null && monitorpointtypes.size() > 0)) {
                return AuthUtil.parseJsonKeyToLower("success",new ArrayList<>());
            }
            if (monitorpointtypes != null) {
                paramMap.put("monitorpointtypes", monitorpointtypes);
            }
            if (iscategory != null) {//0 查传输点 敏感点   1只查敏感点 2只查传输点  “” 都不查
                paramMap.put("iscategory", iscategory);
            } else {
                paramMap.put("iscategory", "");
            }
            categorys = Arrays.asList("1");
            //是否区分一企一管废水排口  yes 区分
            if (ishavepropertys != null) {
                List<String> types = new ArrayList<>();
                if (monitorpointtypes != null) {
                    for (Integer i : monitorpointtypes) {
                        types.add(i + "");
                    }
                }
                if ("yes".equals(ishavepropertys)) {//有勾选 一企一管
                    types.add("yqygoutput");
                    paramMap.put("ishavepropertys", ishavepropertys);
                }
                paramMap.put("monitorpointtypes", types);

            }
            List<Map<String, Object>> result = deviceStatusService.countMonitorPointStatusNumForEnvSupervisionHomeMap(paramMap);

            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/6/17 0017 下午 1:25
     * @Description:根据企业或监测点名称、监测类型、点位状态获取点位
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointListDataByParamMapForApp", method = RequestMethod.POST)
    public Object getMonitorPointListDataByParamMapForApp(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                          @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                          @RequestJson(value = "customname", required = false) String customname) throws Exception {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            paramMap.put("userid", userid);
            paramMap.put("onlineoutputstatus", statuslist);
            for (Integer monitorpointtype : monitorpointtypes) {
                paramMap.put("monitorpointtype", monitorpointtype);
                if (StringUtils.isNotBlank(customname)) {
                    if (pollutiontypes.contains(monitorpointtype)) {//若为关联企业的监测点类型  取企业名称
                        paramMap.put("pollutionname", customname);
                    } else {
                        paramMap.put("monitorpointname", customname);
                    }
                }
                switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                    case WasteWaterEnum:
                    case WasteGasEnum:
                    case SmokeEnum:
                        dataList.addAll(waterOutPutInfoService.getGasAndWaterOutPutAndStatusByParamMap(paramMap));
                        break;
                    case RainEnum:
                        dataList.addAll(waterOutPutInfoService.getRainOutPutAndStatusByParamMap(paramMap));
                        break;
                    case EnvironmentalStinkEnum:
                        paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
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
                        dataList.addAll(otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap));
                        break;
                    case FactoryBoundaryStinkEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                        dataList.addAll(outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap));
                        break;
                    case FactoryBoundarySmallStationEnum:
                        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                        dataList.addAll(outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap));
                        break;
                    case WaterQualityEnum:
                        dataList.addAll(waterStationService.getOnlineWaterStationInfoByParamMap(paramMap));
                        break;
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/19 0019 下午 4:51
     * @Description: 通过监测类型和数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）及自定义参数获取某个类型多排口多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutManyPollutantMonitorDataByParamsForApp", method = RequestMethod.POST)
    public Object getManyOutPutManyPollutantMonitorDataByParamsForApp(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorpointtype, outPutIdAndMn);
            Map<String, String> idAndName = onlineService.getOutPutIdAndPollution(outputids, monitorpointtype);
            Map<String, String> codeAndName = onlineService.getPollutantCodeAndName(pollutantcodes, monitorpointtype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            if (datamark == 3) {
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
            } else if (datamark == 4) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
            } else if (datamark == 5) {
                String yearMothFirst = DataFormatUtil.getYearMothFirst(starttime);
                String yearMothEnd = DataFormatUtil.getYearMothLast(endtime);
                starttime = yearMothFirst + " 00:00:00";
                endtime = yearMothEnd + " 23:59:59";
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(documents, pollutantcodes,
                    collection, outPutIdAndMn, outputids, idAndName, codeAndName);
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/08 0008 下午 13:59
     * @Description:根据自定义参数获取多个点位信息及点位状态和污染物一小时内最新监测值(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfoAndPollutantLastDataByParam", method = RequestMethod.POST)
    public Object getMonitorPointInfoAndPollutantLastDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                                 @RequestJson(value = "statuslist") List<String> statuslist,
                                                                 @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                                 @RequestJson(value = "iscategory") String iscategory) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("userid", userId);
            paramMap.put("statuslist", statuslist);
            List<Map<String, Object>> listdata = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("iscategory", iscategory);
            if (monitorpointtypes == null || monitorpointtypes.size() == 0 && !"".equals(iscategory)) {
                paramMap.put("isshow", "0");
            } else {
                paramMap.put("isshow", "1");
            }
            //环保首页  无安全点位 区分敏感点  传输点
            listdata = deviceStatusService.getMonitorPointInfoForEnvSupervisionHomeMap(paramMap);
            if (listdata != null && listdata.size() > 0) {
                Map<String, List<Map<String, Object>>> listMap = listdata.stream().collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    list = entry.getValue();
                    if ((AirEnum.getCode() + "").equals(entry.getKey())) {
                        airMonitorStationService.setAirStationAqi(list);
                        result.addAll(list);
                    } else if ((WaterQualityEnum.getCode() + "").equals(entry.getKey())) {
                        onlineWaterQualityService.setWaterQaulity(list);

                        result.addAll(list);
                    } else {
                        result.addAll(entry.getValue());
                    }

                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
            Date enddate = new Date();//当前时间为结束时间
            Date startdate = calendar.getTime();//一个小时前为开始时间
            //根据污染物和查出来的点位信息获取最近一小时该污染物的最近一条数据
            onlineMonitorService.getAllMonitorPointLastRealTimeData(result, startdate, enddate, pollutantcode);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/05 0005 上午 9:45
     * @Description:统计按点位状态分组的各类型点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "countSecurityMonitorPointAllStatusNumByTypes", method = RequestMethod.POST)
    public Object countSecurityMonitorPointAllStatusNumByTypes(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Map<String, Object>> listdata = deviceStatusService.countSecurityMonitorPointAllStatusNumByTypes(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/06 0006 上午 9:16
     * @Description:根据监测类型和点位状态获取多个点位信息(包含经纬度,有数据权限)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @RequestMapping(value = "getAllSecurityMonitorPointInfoByParamForHomePage", method = RequestMethod.POST)
    public Object getAllSecurityMonitorPointInfoByParamForHomePage(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                                   @RequestJson(value = "statuslist") List<String> statuslist,
                                                                   @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("statuslist", statuslist);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Map<String, Object>> listdata = deviceStatusService.getAllSecurityMonitorPointInfoByParamForHomePage(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/11 0011 下午 13:21
     * @Description:统计各类型点位在线离线数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllPointStatusNumForMonitorType", method = RequestMethod.POST)
    public Object countAllPointStatusNumForMonitorType(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                       @RequestJson(value = "istreedata", required = false) Boolean istreedata) {
        try {
            //统计各类型点位在线离线数量
            Map<String, Object> paramMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> listdata = deviceStatusService.countAllPointStatusNumForMonitorType(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> lastmap = new HashMap<>();
            if (listdata != null && listdata.size() > 0) {
                if (istreedata == null || istreedata == true) {//默认按 monitoringclass 分组
                    Map<String, List<Map<String, Object>>> listMap = listdata.stream().filter(m -> m.get("monitoringclass") != null).collect(Collectors.groupingBy(m -> m.get("monitoringclass").toString()));
                    for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                        if (Integer.valueOf(entry.getKey()) != 0) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("monitoringclass", Integer.valueOf(entry.getKey()));
                            map.put("monitoringclassname", CommonTypeEnum.MonitoringClassEnum.getNameByCode(Integer.valueOf(entry.getKey())));
                            map.put("numdata", entry.getValue());
                            result.add(map);
                        } else {
                            lastmap.put("monitoringclass", 0);
                            lastmap.put("monitoringclassname", "其它");
                            lastmap.put("numdata", entry.getValue());
                        }
                    }
                    if (lastmap.size() > 0) {
                        result.add(lastmap);
                    }
                } else {
                    result = listdata;
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 根据主题类型统计点位个数
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/7 10:34
     */
    @RequestMapping(value = "countAllPointNumForThemeType", method = RequestMethod.POST)
    public Object countAllPointNumForThemeType(@RequestJson(value = "themetypes") List<Integer> themetypes) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("themetypes", themetypes);
            List<Map<String, Object>> monitorTypes = deviceStatusService.getMonitorTypeListByParam(paramMap);
            //合并同类

            Map<String, List<Integer>> nameAndTypeList = new HashMap<>();
            Map<String, Integer> nameAndIndex = new HashMap<>();
            List<Integer> typeList;
            Integer type;
            Integer orderindex;
            String mainName;
            for (Map<String, Object> monitorType : monitorTypes) {
                type = Integer.parseInt(monitorType.get("code").toString());
                orderindex = monitorType.get("orderindex") != null ? Integer.parseInt(monitorType.get("orderindex").toString()) : 999;
                mainName = monitorType.get("mainname") != null ? monitorType.get("mainname").toString() : monitorType.get("name").toString();
                if (nameAndTypeList.containsKey(mainName)) {
                    typeList = nameAndTypeList.get(mainName);
                } else {
                    typeList = new ArrayList<>();
                }
                typeList.add(type);
                nameAndTypeList.put(mainName, typeList);
                if (nameAndIndex.get(mainName) != null && orderindex > nameAndIndex.get(mainName)) {
                    orderindex = nameAndIndex.get(mainName);
                }
                nameAndIndex.put(mainName, orderindex);

            }
            Map<String, Object> resultMap;
            for (String nameIndex : nameAndTypeList.keySet()) {
                resultMap = setResultMap(nameAndTypeList.get(nameIndex), nameIndex, nameAndIndex, userId);
                resultList.add(resultMap);
            }
            //排序
            resultList = resultList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> setResultMap(List<Integer> types, String name, Map<String, Integer> nameAndIndex, String userId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put("orderindex", nameAndIndex.get(name));
        resultMap.put("types", types);
        long pointNum = 0;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userId);
        for (Integer typeIndex : types) {
            paramMap.put("monitorpointtype", typeIndex);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(typeIndex)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    pointNum += waterOutPutInfoService.countTotalByParam(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputtype", "rain");
                    pointNum += waterOutPutInfoService.countTotalByParam(paramMap);
                    break;
                case WasteGasEnum:
                case SmokeEnum:
                    pointNum += gasOutPutInfoService.countTotalByParam(paramMap);
                    break;
                case TZFEnum:
                case XKLWEnum:
                case EnvironmentalVocEnum:
                case EnvironmentalStinkEnum:
                    pointNum += otherMonitorPointService.countTotalByParam(paramMap);
                    break;
                case AirEnum:
                    pointNum += airMonitorStationService.countTotalByParam(paramMap);
                    break;
                case WaterQualityEnum:
                    pointNum += waterStationService.countTotalByParam(paramMap);
                    break;
                case DBWaterEnum:
                    pointNum += riverSectionService.countTotalByParam(paramMap);
                    break;

            }
        }
        resultMap.put("pointNum", pointNum);
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2022/01/11 0011 下午 13:21
     * @Description:统计各类型点位的浓度及排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllPointFlowAndConcentrationData", method = RequestMethod.POST)
    public Object countAllPointFlowAndConcentrationData(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                        @RequestJson(value = "pollutantcode") String pollutantcode,
                                                        @RequestJson(value = "datetype") String datetype,
                                                        @RequestJson(value = "monitortime",required = false) String monitortime,
                                                        @RequestJson(value = "starttime",required = false) String starttime,
                                                        @RequestJson(value = "endtime",required = false) String endtime

                                                        ) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtypes", monitorpointtypes);
            param.put("datetype", datetype);
            param.put("monitortime", monitortime);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("pollutantcode", pollutantcode);
            Map<String, Map<String, Object>> result = monitorPointService.countAllPointFlowAndConcentrationData(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2022/01/11 0011 下午 13:21
     * @Description: 统计各类型点位的月排放量及同比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAllPointFlowData", method = RequestMethod.POST)
    public Object countAllPointFlowData(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                        @RequestJson(value = "pollutantcode") String pollutantcode,
                                        @RequestJson(value = "datetype") String datetype,
                                        @RequestJson(value = "monitortime") String monitortime) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtypes", monitorpointtypes);
            param.put("datetype", datetype);
            param.put("monitortime", monitortime);
            param.put("pollutantcode", pollutantcode);
            //当前月
            Map<String, Map<String, Object>> thisData = monitorPointService.countAllPointFlowAndConcentrationData(param);
            //同比月
            String thatTime = DataFormatUtil.getBeforeYear(1, monitortime);
            param.put("monitortime", thatTime);
            Map<String, Map<String, Object>> thatData = monitorPointService.countAllPointFlowAndConcentrationData(param);
            String thisMonth;
            String thatMonth;
            List<Map<String,Object>> resultList = new ArrayList<>();
            for (int i = 1; i < 13; i++) {
                Map<String, Object> resultMap = new HashMap<>();
                thisMonth = DataFormatUtil.FormatDateOneToOther(monitortime+ "-"+i,"yyyy-M","yyyy-MM") ;
                thatMonth = DataFormatUtil.FormatDateOneToOther(thatTime+ "-"+i,"yyyy-M","yyyy-MM") ;
                resultMap.put("month",thisMonth);
                if (thisData.get(thisMonth)!=null){
                    resultMap.put("thisvalue",thisData.get(thisMonth).get("pfl_value"));
                }else {
                    resultMap.put("thisvalue",0);
                }
                if (thatData.get(thatMonth)!=null){
                    resultMap.put("thatvalue",thatData.get(thatMonth).get("pfl_value"));
                }else {
                    resultMap.put("thatvalue",0);
                }
                resultList.add(resultMap);

            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/12 0012 上午 11:08
     * @Description:统计各类型点位近七天总超标时长排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEntLastSevenDaysOverTimeRankData", method = RequestMethod.POST)
    public Object getEntLastSevenDaysOverTimeRankData(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                      @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorPointTypes,
                                                      @RequestJson(value = "starttime", required = false) String starttime,
                                                      @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndPollutionID = new HashMap<>();
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> idAndName = new HashMap<>();
            String mnCommon;
            List<Map<String, Object>> pointDataList = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (monitorPointTypes == null || monitorPointTypes.size() == 0) {
                monitorPointTypes = new ArrayList<>();
                monitorPointTypes.add(monitorpointtype);
            }
            for (Integer i : monitorPointTypes) {
                paramMap.put("monitorpointtypecode", i);
                paramMap.put("userid", userid);
                //根据类型获取点位信息
                pointDataList.addAll(onlineService.getMNAndMonitorPointByParam(paramMap));
            }
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("DGIMN") != null || pointData.get("dgimn") != null) {
                        mnCommon = pointData.get("DGIMN") != null ? pointData.get("DGIMN").toString() : (pointData.get("dgimn") != null ? pointData.get("dgimn").toString() : "");
                        mns.add(mnCommon);
                        mnAndPollutionName.put(mnCommon, pointData.get("pollutionname") != null ? pointData.get("pollutionname") : "");
                        mnAndPollutionID.put(mnCommon, pointData.get("pk_pollutionid") != null ? pointData.get("pk_pollutionid") : "");
                        if (pointData.get("pk_pollutionid") != null) {
                            idAndName.put(pointData.get("pk_pollutionid").toString(), pointData.get("pollutionname"));
                        }
                    }
                }
            }
            //获取该类型污染物
            paramMap.put("pollutanttypes", monitorPointTypes);
            List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantData.size() > 0) {
                String key;
                for (Map<String, Object> pollutant : pollutantData) {
                    key = pollutant.get("code").toString();
                    if (pollutant.get("name") != null) {
                        codeAndName.put(key, pollutant.get("name"));
                    }
                }
            }
            paramMap.clear();
            paramMap.put("mns", mns);
            paramMap.put("collection",DB_OverModel);
            if (starttime != null && endtime != null) {
                paramMap.put("starttime", starttime + " 00:00:00");
                paramMap.put("endtime", endtime + " 23:59:59");
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);

                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
            }
            if (starttime != null && endtime != null) {
                paramMap.put("starttime", DataFormatUtil.getDateYMD(starttime));
                paramMap.put("endtime", DataFormatUtil.getDateYMD(endtime));
            } else {
                //获取近七天时间
                Date thedate = DataFormatUtil.parseDateYMD(DataFormatUtil.getDate());
                Date postponeDate = DataFormatUtil.parseDateYMD(DataFormatUtil.getPostponeDate(thedate, -6));
                paramMap.put("starttime", postponeDate);
                paramMap.put("endtime", thedate);
            }

            paramMap.put("codeandname", codeAndName);
            paramMap.put("mnandpollutionid", mnAndPollutionID);
            paramMap.put("mnandpollutionname", mnAndPollutionName);
            paramMap.put("idandname", idAndName);
            List<Map<String, Object>> result = monitorPointService.getEntLastSevenDaysOverTimeRankData(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/13 0013 上午 08:41
     * @Description:获取当月报警任务处置情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastMonthAlarmTaskDisposalByParam", method = RequestMethod.POST)
    public Object getLastMonthAlarmTaskDisposalByParam(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                       @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                       @RequestJson(value = "monthdate") String monthdate,
                                                       @RequestJson(value = "tasktype") Integer tasktype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (monitorpointtypes == null || monitorpointtypes.size() == 0) {
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype != null) {
                    monitorpointtypes.add(monitorpointtype);
                }
            }
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("monthdate", monthdate);
            paramMap.put("tasktype", tasktype);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            Map<String, Object> result = monitorPointService.getLastMonthAlarmTaskDisposalByParam(paramMap);
            if (result != null) {
                Double total = result.get("totalnum") != null ? Double.valueOf(result.get("totalnum").toString()) : 0d;
                Double ywcnum = result.get("ywcnum") != null ? Double.valueOf(result.get("ywcnum").toString()) : 0d;
                if (total > 0) {
                    String proportion = DataFormatUtil.SaveOneAndSubZero(ywcnum * 100 / total) + "%";
                    result.put("proportion", proportion);
                } else {
                    result.put("proportion", "0%");
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
     * @date: 2022/01/13 0013 上午 08:41
     * @Description:获取报警统计清单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime,endtime [yyyy-mm-dd]
     * @return:
     */
    @RequestMapping(value = "getAlarmStatisticsInventoryByParam", method = RequestMethod.POST)
    public Object getAlarmStatisticsInventoryByParam(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                     @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                     @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                     @RequestJson(value = "outputname", required = false) String outputname,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndShorterName = new HashMap<>();
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> mnandpointname = new HashMap<>();
            Map<String, Object> mnandpointid = new HashMap<>();
            Map<String, Object> mnandtype = new HashMap<>();
            String mnCommon;
            List<Map<String, Object>> pointDataList = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            paramMap.put("userid", userid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            //根据类型获取点位信息
            if (monitorpointtypes != null) {
                for (Integer i : monitorpointtypes) {
                    paramMap.put("monitorpointtypecode", i);
                    pointDataList.addAll(onlineService.getMNAndMonitorPointByParam(paramMap));
                }
            } else {
                pointDataList = onlineService.getMNAndMonitorPointByParam(paramMap);
            }
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("DGIMN") != null || pointData.get("dgimn") != null) {
                        mnCommon = pointData.get("DGIMN") != null ? pointData.get("DGIMN").toString() : (pointData.get("dgimn") != null ? pointData.get("dgimn").toString() : "");
                        mns.add(mnCommon);
                        mnAndPollutionName.put(mnCommon, pointData.get("pollutionname") != null ? pointData.get("pollutionname") : "");
                        mnAndShorterName.put(mnCommon, pointData.get("shortername") != null ? pointData.get("shortername") : "");
                        mnandpointname.put(mnCommon, pointData.get("monitorpointname"));
                        mnandpointid.put(mnCommon, pointData.get("monitorpointid"));
                        mnandtype.put(mnCommon, pointData.get("monitorpointtype"));
                    }
                }
            }
            //获取该类型污染物
            if (monitorpointtypes != null) {
                paramMap.put("pollutanttypes", monitorpointtypes);
            } else {
                paramMap.put("pollutanttype", monitorpointtype);
            }
            List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantData.size() > 0) {
                String key;
                for (Map<String, Object> pollutant : pollutantData) {
                    key = pollutant.get("code").toString();
                    if (pollutant.get("name") != null) {
                        codeAndName.put(key, pollutant.get("name"));
                    }
                }
            }
            paramMap.clear();
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("codeandname", codeAndName);
            paramMap.put("mnandpollutionname", mnAndPollutionName);
            paramMap.put("mnandshortername", mnAndShorterName);
            paramMap.put("mnandpointname", mnandpointname);
            paramMap.put("mnandpointid", mnandpointid);
            paramMap.put("mnandtype", mnandtype);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> result = monitorPointService.getAlarmStatisticsInventoryByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/13 0013 上午 08:41
     * @Description:导出报警统计清单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime,endtime [yyyy-mm-dd]
     * @return:
     */
    @RequestMapping(value = "exportAlarmStatisticsInventoryByParam", method = RequestMethod.POST)
    public void exportAlarmStatisticsInventoryByParam(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                      @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                      @RequestJson(value = "outputname", required = false) String outputname,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      HttpServletResponse response, HttpServletRequest request) throws IOException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> mnandpointname = new HashMap<>();
            String mnCommon;
            List<Map<String, Object>> pointDataList;
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("monitorpointtypecode", monitorpointtype);
            paramMap.put("userid", userid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            //根据类型获取点位信息
            pointDataList = onlineService.getMNAndMonitorPointByParam(paramMap);
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("DGIMN") != null || pointData.get("dgimn") != null) {
                        mnCommon = pointData.get("DGIMN") != null ? pointData.get("DGIMN").toString() : (pointData.get("dgimn") != null ? pointData.get("dgimn").toString() : "");
                        mns.add(mnCommon);
                        mnAndPollutionName.put(mnCommon, pointData.get("pollutionname") != null ? pointData.get("pollutionname") : "");
                        mnandpointname.put(mnCommon, pointData.get("monitorpointname"));
                    }
                }
            }
            //获取该类型污染物
            paramMap.put("pollutanttype", monitorpointtype);
            List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantData.size() > 0) {
                String key;
                for (Map<String, Object> pollutant : pollutantData) {
                    key = pollutant.get("code").toString();
                    if (pollutant.get("name") != null) {
                        codeAndName.put(key, pollutant.get("name"));
                    }
                }
            }
            paramMap.clear();
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("codeandname", codeAndName);
            paramMap.put("mnandpollutionname", mnAndPollutionName);
            paramMap.put("mnandpointname", mnandpointname);
            Map<String, Object> result = monitorPointService.getAlarmStatisticsInventoryByParam(paramMap);
            List<Map<String, Object>> datalist = (List<Map<String, Object>>) result.get("datalist");
            List<Map<String, Object>> tabletitledata = monitorPointService.getTableTitleForAlarmStatisticsInventory();
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            ExcelUtil.exportExcelFile("报警清单", response, request, "", headers, headersField, datalist, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 周边因素
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/22 10:51
     */
    @RequestMapping(value = "getPeripheryType", method = RequestMethod.POST)
    public Object getPeripheryType() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            CommonTypeEnum.PeripheryTypeEnum[] typeEnums = CommonTypeEnum.PeripheryTypeEnum.values();
            for (int i = 0; i < typeEnums.length; i++) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("typecode", typeEnums[i].getCode());
                resultMap.put("typename", typeEnums[i].getName());
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 周边分析（点位信息、距离信息）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/22 10:51
     */
    @RequestMapping(value = "getPeripheryPointByParam", method = RequestMethod.POST)
    public Object getPeripheryPointByParam(
            @RequestJson(value = "pointtypes") List<Integer> pointtypes,
            @RequestJson(value = "longitude") String longitude,
            @RequestJson(value = "latitude") String latitude,
            @RequestJson(value = "distance") String distance,
            @RequestJson(value = "pointid") String pointid
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            Double lonS = Double.parseDouble(longitude);
            Double latS = Double.parseDouble(latitude);
            Double disS = Double.parseDouble(distance);

            for (Integer type : pointtypes) {
                resultList.addAll(getPointDataList(type, lonS, latS, disS, pointid));
            }
            //排序
            if (resultList.size() > 0) {
                resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("distance").toString()))).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getPointDataList(Integer type, Double longitude, Double latitude, Double distance, String pointid) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> pointList;

        Map<String, Object> paramMap = new HashMap<>();

        Double TLongitude;
        Double TLatitude;
        Double TDistance;

        switch (CommonTypeEnum.PeripheryTypeEnum.getObjectByCode(type)) {
            case FSEnum://废水
                paramMap.put("outputtype", "water");
                pointList = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("monitorpointid")) && point.get("Longitude") != null && point.get("Latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("Longitude").toString());
                        TLatitude = Double.parseDouble(point.get("Latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("monitorpointid"));
                            dataMap.put("monitorpointname", point.get("shortername") + "-" + point.get("outputname"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }

                break;
            case YSEnum://雨水
                paramMap.put("outputtype", "rain");
                pointList = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("monitorpointid")) && point.get("Longitude") != null && point.get("Latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("Longitude").toString());
                        TLatitude = Double.parseDouble(point.get("Latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("monitorpointid"));
                            dataMap.put("monitorpointname", point.get("shortername") + "-" + point.get("outputname"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }
                break;
            case FQEnum://废气
                pointList = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("monitorpointid")) && point.get("Longitude") != null && point.get("Latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("Longitude").toString());
                        TLatitude = Double.parseDouble(point.get("Latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("monitorpointid"));
                            dataMap.put("monitorpointname", point.get("shortername") + "-" + point.get("outputname"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }
                break;
            case ECEnum://恶臭、VOC、微站、气象
            case VOCEnum:
            case WZEnum:
            case QXEnum:
                paramMap.put("monitorPointType", type);
                pointList = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("monitorpointid")) && point.get("Longitude") != null && point.get("Latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("Longitude").toString());
                        TLatitude = Double.parseDouble(point.get("Latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("monitorpointid"));
                            dataMap.put("monitorpointname", point.get("monitorpointname"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }

                break;
            case AIREnum://空气站
                pointList = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("monitorpointid")) && point.get("Longitude") != null && point.get("Latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("Longitude").toString());
                        TLatitude = Double.parseDouble(point.get("Latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("monitorpointid"));
                            dataMap.put("monitorpointname", point.get("monitorpointname"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }
                break;
            case TSDEnum://投诉点
                pointList = petitionInfoService.getPetitionDataByParam(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("pkid")) && point.get("longitude") != null && point.get("latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("longitude").toString());
                        TLatitude = Double.parseDouble(point.get("latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("pkid"));
                            dataMap.put("monitorpointname", point.get("petitiontitle"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }
                break;
            case SZEnum://水质
                pointList = waterStationService.getOnlineWaterStationInfoByParamMap(paramMap);
                for (Map<String, Object> point : pointList) {
                    if (!pointid.equals(point.get("monitorpointid")) && point.get("Longitude") != null && point.get("Latitude") != null) {
                        TLongitude = Double.parseDouble(point.get("Longitude").toString());
                        TLatitude = Double.parseDouble(point.get("Latitude").toString());
                        TDistance = DataFormatUtil.getMeterByDegree(latitude, longitude, TLatitude, TLongitude);
                        if (TDistance <= distance) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitorpointid", point.get("monitorpointid"));
                            dataMap.put("monitorpointname", point.get("monitorpointname"));
                            dataMap.put("pointtype", type);
                            dataMap.put("longitude", TLongitude);
                            dataMap.put("latitude", TLatitude);
                            dataMap.put("distance", DataFormatUtil.formatDoubleSaveNo(TDistance));
                            dataList.add(dataMap);
                        }
                    }
                }
                break;
        }
        return dataList;
    }

}
