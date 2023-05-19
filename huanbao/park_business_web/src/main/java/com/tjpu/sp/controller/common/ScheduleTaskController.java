package com.tjpu.sp.controller.common;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.WebDriverUtil;
import com.tjpu.sp.controller.environmentalprotection.deviceproblemrecord.DeviceProblemRecordController;
import com.tjpu.sp.controller.environmentalprotection.onlinemonitor.OnlineWaterController;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutPutPollutantSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutputInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.watercorrelation.WaterCorrelationMapper;
import com.tjpu.sp.dao.environmentalprotection.watercorrelation.WaterCorrelationPollutantSetMapper;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.environmentalprotection.watercorrelation.WaterCorrelationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.watercorrelation.WaterCorrelationVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.micro.JnaServiceMicroService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WechatPushSetAlarmTypeEnum.OfflineStatusEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: lip
 * @date: 2019/7/19 0019 上午 9:14
 * @Description: rabbitmq消息队列处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */

@RestController
@RequestMapping("scheduleTask")
public class ScheduleTaskController {

    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private AuthSystemMicroService authSystemMicroService;
    @Autowired
    private FileController fileController;

    @Autowired
    private JnaServiceMicroService jnaServiceMicroService;


    @Autowired
    private OnlineWaterController onlineWaterController;

    @Autowired
    private DeviceProblemRecordController deviceProblemRecordController;

    @Autowired
    private WaterCorrelationMapper waterCorrelationMapper;

    @Autowired
    private WaterCorrelationPollutantSetMapper waterCorrelationPollutantSetMapper;

    @Autowired
    private JGUserRegisterInfoService jgUserRegisterInfoService;
    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;
    @Autowired
    private RestTemplate restTemplate;
    private static final String SERVICE_URL = "http://py-sidecar/getResult";

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;


    public void waterSimilarityAnalysisSendTask() {
        try {
            Date startDate = DataFormatUtil.getPreYearDate(DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateY(new Date()) + "-01-01 00:00:00"), 1);
            Date endDate = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(DataFormatUtil.getPreDate(new Date(), 1)) + " 23:59:59");
            //获取工业污水厂进口水mn
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 2);
            List<Map<String, Object>> inMns = waterOutputInfoMapper.getInOrOutPutMnListByParams(paramMap);
            if (inMns != null && inMns.size() > 0) {
                Map<String, Object> map = inMns.get(0);
                String inMn = map.get("DGIMN").toString();
                String inPollutionid = map.get("FK_Pollutionid").toString();
                String inPkid = map.get("PK_ID").toString();
                //污水管网监测点mn
                paramMap.clear();
                paramMap.put("outputproperty", 2);
                List<Map<String, Object>> wgMns = waterOutputInfoMapper.getInOrOutPutMnListByParams(paramMap);
                HashMap<String, Double> timeandvalue = new HashMap<>();
                HashMap<String, Object> timeandcode = new HashMap<>();

                if (wgMns != null && wgMns.size() > 0) {
                    Query query = new Query();
                    query.addCriteria(Criteria.where("DataGatherCode").is(inMn))
                            .addCriteria(Criteria.where("DataType").is("HourData"))
                            .addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
                    List<Document> inDocuments = mongoTemplate.find(query, Document.class, "OverData");
                    for (Document document : inDocuments) {
                        String overTime = FormatUtils.formatCSTString(document.get("OverTime").toString(), "yyyy-MM-dd HH");
                        Double monitorValue = document.getString("MonitorValue") == null ? null : Double.parseDouble(document.getString("MonitorValue"));
                        Object pollutantCode = document.get("PollutantCode");
                        timeandcode.put(overTime, pollutantCode);
                        timeandvalue.put(overTime, monitorValue);
                    }
                    Set<Object> pollutantCodeSets = timeandcode.values().stream().collect(Collectors.toSet());
                    Set<Object> commonPollutantCodes = new HashSet<>();

                    paramMap.clear();
                    paramMap.put("outputids", Arrays.asList(inPkid));
                    List<Map<String, Object>> pollutantStandardDataListByParam = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                    for (Map<String, Object> map1 : wgMns) {
                        String wgPollutionid = map1.get("FK_Pollutionid").toString();
                        String pkId = UUID.randomUUID().toString();
                        String wgPkid = map1.get("PK_ID").toString();
                        for (Object pollutantCode : pollutantCodeSets) {
                            List<Double> xData = new ArrayList<>();
                            List<Double> yData = new ArrayList<>();
                            String wgMn = map1.get("DGIMN").toString();
                            query = new Query();
                            query.addCriteria(Criteria.where("DataGatherCode").is(wgMn))
                                    .addCriteria(Criteria.where("DataType").is("HourData"))
                                    .addCriteria(Criteria.where("PollutantCode").is(pollutantCode))
                                    .addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
                            List<Document> wgDocuments = mongoTemplate.find(query, Document.class, "OverData");
                            for (Document document : wgDocuments) {
                                String overTime = FormatUtils.formatCSTString(document.get("OverTime").toString(), "yyyy-MM-dd HH");
                                Double monitorValue = document.getString("MonitorValue") == null ? null : Double.parseDouble(document.getString("MonitorValue"));
                                if (timeandvalue.containsKey(overTime) && timeandcode.get(overTime).equals(pollutantCode)) {
                                    xData.add(monitorValue);
                                    yData.add(timeandvalue.get(overTime));
                                    commonPollutantCodes.add(pollutantCode);
                                }
                            }

                            Double slope = null;
                            Double constant = null;
                            Double value = null;
                            String similarity = null;
                            if (!(xData.size() == 0 || yData.size() == 0)) {
                                slope = DataFormatUtil.getRelationSlope(xData, yData);
                                constant = DataFormatUtil.getRelationConstant(xData, yData, slope);

                                value = DataFormatUtil.getRelationPercent(xData, yData);
                                if (value > -0.000001 && value < +0.000001) {//Double类型最小负数和最小正数，判断为0
                                    value = 0.0;
                                }
                                similarity = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(value));
                            }
                            System.out.println(xData);
                            System.out.println(yData);

                            paramMap.clear();
                            paramMap.put("fkWatermonitorpointid", inPkid);
                            paramMap.put("fkOutfallmonitorpointid", wgPkid);
                            paramMap.put("StartTime", DataFormatUtil.getDateYMDHMS(startDate));
                            paramMap.put("EndTime", DataFormatUtil.getDateYMDHMS(endDate));
                            List<WaterCorrelationVO> waterCorrelationVOS = waterCorrelationMapper.selectByParam(paramMap);
                            List<Map<String, Object>> pollutantStandardDataList = pollutantStandardDataListByParam.stream().filter(item -> item.get("pollutantcode").toString().equals(pollutantCode)).collect(Collectors.toList());
                            List<Map<String, Object>> notExistsPollutantStandardDataList = pollutantStandardDataListByParam.stream().filter(item -> !commonPollutantCodes.contains(item.get("pollutantcode").toString())).collect(Collectors.toList());
                            Double r = similarity != null ? Double.parseDouble(similarity) < 0d ? 0d : Double.parseDouble(similarity) : null;

                            if (waterCorrelationVOS != null && waterCorrelationVOS.size() > 0) {
                                for (WaterCorrelationVO waterCorrelationVO : waterCorrelationVOS) {
                                    waterCorrelationVO.setFkOutfallpollutionid(wgPollutionid);
                                    waterCorrelationVO.setFkWaterpollutionid(inPollutionid);
                                    waterCorrelationVO.setUpdatetime(new Date());
                                    waterCorrelationMapper.updateByPrimaryKeySelective(waterCorrelationVO);
                                    updateWaterCorrelationPolluantSets(r, slope, constant, waterCorrelationVO, pollutantStandardDataList);
                                    updateWaterCorrelationPolluantSets(null, null, null, waterCorrelationVO, notExistsPollutantStandardDataList);
                                }
                            } else {
                                WaterCorrelationVO waterCorrelationVO = new WaterCorrelationVO();
                                waterCorrelationVO.setFkOutfallmonitorpointid(wgPkid);
                                waterCorrelationVO.setFkOutfallpollutionid(wgPollutionid);
                                waterCorrelationVO.setFkWatermonitorpointid(inPkid);
                                waterCorrelationVO.setFkWaterpollutionid(inPollutionid);
                                waterCorrelationVO.setStarttime(startDate);
                                waterCorrelationVO.setEndtime(endDate);
                                waterCorrelationVO.setUpdatetime(new Date());
                                waterCorrelationVO.setPkId(pkId);
                                waterCorrelationMapper.insert(waterCorrelationVO);
                                updateWaterCorrelationPolluantSets(r, slope, constant, waterCorrelationVO, pollutantStandardDataList);
                                updateWaterCorrelationPolluantSets(null, null, null, waterCorrelationVO, notExistsPollutantStandardDataList);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateWaterCorrelationPolluantSets(Double r, Double slope, Double constant, WaterCorrelationVO waterCorrelationVO, List<Map<String, Object>> pollutantStandardDataListByParam) {
        for (Map<String, Object> standardDataMap : pollutantStandardDataListByParam) {
            Double maxValue = null;
            Double minValue = null;
            Object pollutantcode = standardDataMap.get("pollutantcode");
            Double standardmaxvalue = standardDataMap.get("standardmaxvalue") == null ? null : (Double) standardDataMap.get("standardmaxvalue");
            Double standardminvalue = standardDataMap.get("standardminvalue") == null ? null : (Double) standardDataMap.get("standardminvalue");
            BigDecimal accuracy = standardDataMap.get("accuracy") == null ? null : (BigDecimal) standardDataMap.get("accuracy");
            if ((slope != null && slope == 0d) || constant == null || slope == null) {
                maxValue = null;
                minValue = null;
            } else {
                if (standardmaxvalue != null) {
                    maxValue = (standardmaxvalue - constant) / slope;
                    maxValue = formatDoubleSaveDouble(maxValue, accuracy);
                } else if (standardminvalue != null) {
                    minValue = (standardminvalue - constant) / slope;
                    minValue = formatDoubleSaveDouble(minValue, accuracy);
                }
            }

            String collect = null;
            if (minValue != null && maxValue != null) {
                collect = minValue + "," + maxValue;
            } else if (minValue != null) {
                collect = minValue + "";
            } else if (maxValue != null) {
                collect = maxValue + "";
            } else {
                collect = null;
            }

            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkWatercorrelationid", waterCorrelationVO.getPkId());
            paramMap.put("fkPollutantcode", pollutantcode.toString());
            List<WaterCorrelationPollutantSetVO> analysisPollutantSetVOS = waterCorrelationPollutantSetMapper.selectByParam(paramMap);
            if (analysisPollutantSetVOS != null && analysisPollutantSetVOS.size() > 0) {
                for (WaterCorrelationPollutantSetVO analysisPollutantSetVO : analysisPollutantSetVOS) {
                    analysisPollutantSetVO.setValue(collect);
                    analysisPollutantSetVO.setA(slope);
                    analysisPollutantSetVO.setB(constant);
                    analysisPollutantSetVO.setR(r);
                    waterCorrelationPollutantSetMapper.updateByPrimaryKey(analysisPollutantSetVO);
                }
            } else {
                WaterCorrelationPollutantSetVO analysisPollutantSetVO = new WaterCorrelationPollutantSetVO();
                analysisPollutantSetVO.setFkPollutantcode(pollutantcode.toString());
                analysisPollutantSetVO.setFkWatercorrelationid(waterCorrelationVO.getPkId());
                analysisPollutantSetVO.setPkId(UUID.randomUUID().toString());
                analysisPollutantSetVO.setValue(collect);
                analysisPollutantSetVO.setA(slope);
                analysisPollutantSetVO.setB(constant);
                analysisPollutantSetVO.setR(r);
                waterCorrelationPollutantSetMapper.insert(analysisPollutantSetVO);
            }
        }
    }

    private Double formatDoubleSaveDouble(Double aDouble, BigDecimal accuracy) {

        if (accuracy != null && aDouble != null) {
            int accuracyInt = accuracy.intValue();
            String format = "######0";
            if (accuracyInt >= 0) {
                format += ".";
                for (Integer i = 0; i < accuracyInt; i++) {
                    format += "0";
                }
            }
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            aDouble = Double.parseDouble(df.format(aDouble));
        }
        return aDouble;
    }

    @GetMapping("/startScheduleTask")
    public void startScheduleTask() {
        try {
            // 1，获取接口需要参数；
            Map<String, Object> paramMap = new HashMap<>();
            List<String> pollutantCodes = Arrays.asList(
                    CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode(),
                    CommonTypeEnum.StinkPollutionEnum.H2SEnum.getCode(),
                    CommonTypeEnum.StinkPollutionEnum.NHEnum.getCode(),
                    CommonTypeEnum.StinkPollutionEnum.VOCEnum.getCode());
            paramMap.put("pollutantcodes", pollutantCodes);
            Date nowDay = new Date();
            String startTime = DataFormatUtil.getDateYMDH(nowDay) + ":00:00";
            String endTime = DataFormatUtil.getDateYMDH(nowDay) + ":59:59";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("collection", "HourData");
            List<String> QXCodes = Arrays.asList(
                    CommonTypeEnum.WeatherPollutionEnum.TemperatureEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.HumidityEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.PressureEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode());
            paramMap.put("pollutantcodes", pollutantCodes);
            Map<String, String> stinkMNAndPoints = onlineService.getMNAndMonitorPoint(new ArrayList<>(),
                    CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            List<Document> NDDocuments;
            List<Document> QXDocuments;
            String airMn;
            List<String> airMns;

            List<Map<String, Object>> pollutantList;
            boolean isSendMessage = false;
            for (String mnKey : stinkMNAndPoints.keySet()) {
                paramMap.put("mns", Arrays.asList(mnKey));
                NDDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                airMns = otherMonitorPointService.getAirDgimnByMonitorDgimn(mnKey);
                if (airMns.size() > 0) {
                    airMn = airMns.get(0);
                } else {
                    airMn = mnKey;
                }

                paramMap.put("mns", Arrays.asList(airMn));
                paramMap.put("pollutantcodes", QXCodes);
                QXDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                if (NDDocuments.size() > 0 && QXDocuments.size() > 0) {
                    MultiValueMap<String, String> resultMap = new LinkedMultiValueMap<>();
                    Document NDDocument = NDDocuments.get(0);
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    pollutantList = (List<Map<String, Object>>) NDDocument.get("HourDataList");
                    for (String pollutantCode : pollutantCodes) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        pollutantMap.put("pollutantname", CommonTypeEnum.StinkPollutionEnum.getNameByCode(pollutantCode));
                        pollutantMap.put("pollutantcode", pollutantCode);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                                if (pollutant.get("AvgStrength") != null && !"".equals(pollutant.get("AvgStrength"))) {
                                    pollutantMap.put("value", pollutant.get("AvgStrength"));
                                    dataList.add(pollutantMap);
                                }
                                break;
                            }
                        }
                    }
                    Document XQDocument = QXDocuments.get(0);
                    pollutantList = (List<Map<String, Object>>) XQDocument.get("HourDataList");
                    for (String pollutantCode : QXCodes) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        pollutantMap.put("pollutantname", CommonTypeEnum.WeatherPollutionEnum.getNameByCode(pollutantCode));
                        pollutantMap.put("pollutantcode", pollutantCode);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                                if (pollutant.get("AvgStrength") != null && !"".equals(pollutant.get("AvgStrength"))) {
                                    if (pollutantCode.equals("a01008")) {//风向转换
                                        String windDirection = pollutant.get("AvgStrength") != null ? DataFormatUtil.windDirectionSwitch(
                                                Double.parseDouble(pollutant.get("AvgStrength").toString()), "code") : "";
                                        pollutantMap.put("value", windDirection);
                                    } else {
                                        pollutantMap.put("value", pollutant.get("AvgStrength"));
                                    }
                                    dataList.add(pollutantMap);
                                    break;
                                }
                            }
                        }
                    }
                    if (dataList.size() > 0) {
                        resultMap.add("mn", mnKey);
                        resultMap.add("datalist", dataList.toString());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(resultMap, headers);
                        ResponseEntity<String> response = restTemplate.postForEntity(SERVICE_URL, request, String.class);
                        JSONObject jsonObject = JSONObject.fromObject(response.getBody());
                        if (jsonObject.get("result") != null && jsonObject.get("result").equals("1")) {
                            isSendMessage = true;
                            break;
                        }
                    }
                }
            }
            if (isSendMessage) {
                // 4，推送服务（所有pc客户端）；
                String msg = "根据园区企业当前排放情况，当前气象扩散条件容易造成废气进城的影响，请加强企业巡查监管。";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("messagedata", msg);
                jsonObject.put("messagemethod", CommonTypeEnum.SocketTypeEnum.ForecastDataEnum.getSocketMethod());
                authSystemMicroService.sendAllClientMessage(jsonObject);
                /*// 5，推送服务（所有app端）
                paramMap.clear();
                List<Map<String, Object>> userRegisterInfo = jgUserRegisterInfoService.getUserRegisterInfoListByParam(paramMap);
                List<Map<String, Object>> remindData = new ArrayList<>();
                for (Map<String, Object> client : userRegisterInfo) {
                    if (client.get("usercode") != null && client.get("fk_userid") != null) {
                        Map<String, Object> remindMap = new HashMap<>();
                        remindMap.put("userid", client.get("fk_userid").toString());
                        remindMap.put("regid", client.get("regid"));
                        remindMap.put("reminddata", msg);
                        remindData.add(remindMap);
                    }
                }
                if (remindData.size() > 0) {
                    JSONObject JGJson = new JSONObject();
                    String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.ForecastMessage.getCode();
                    JGJson.put("messagetype", messageType);
                    JGJson.put("messagetypename", CommonTypeEnum.RabbitMQMessageTypeEnum.getNameByCode(messageType));
                    JGJson.put("messageanduserdata", remindData);
                    authSystemMicroService.sendMessageToAppClient(JGJson);
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/9/17 0017 上午 8:50
     * @Description: 定时解析气象数据入库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @GetMapping("/fixedTimeParseMeteData")
    public void fixedTimeParseMeteData() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/9/17 0017 上午 8:50
     * @Description: 间隔时间解析气象数据入库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @GetMapping("/intervalTimeParseMeteData")
    public void intervalTimeParseMeteData() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/3/13 0013 上午 9:20
     * @Description: 发送文件到指定微信群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @GetMapping("/sendFileDataToWeChart")
    public void sendFileDataToWeChart() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("AlarmTypes", Arrays.asList("problemreport"));
            List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
            if (weChartGroups.size() > 0) {
                //获取前一天问题报告文件
                String businessType = "30";
                String ymd = DataFormatUtil.getDateYMD(new Date());
                paramMap.clear();
                paramMap.put("uploadtime", ymd);
                paramMap.put("businesstype", businessType);
                List<FileInfoVO> fileList = fileInfoService.getFilesByParamMap(paramMap);
                InputStream inputStream = null;
                if (fileList.size() > 0) {
                    FileInfoVO fileInfoVO = fileList.get(0);
                    String fileId = fileInfoVO.getFilepath();
                    if (StringUtils.isNotBlank(fileId)) {
                        inputStream = fileController.getFileInputStream(fileId, businessType);
                    }
                    Set<String> weChartNames = new HashSet<>();
                    for (Map<String, Object> weChartGroup : weChartGroups) {
                        weChartNames.add(weChartGroup.get("WechatName").toString());
                    }
                    String originalFileName = fileInfoVO.getOriginalfilename();
                    MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
                    for (String weChartName : weChartNames) {
                        jnaServiceMicroService.sendGroupFileData(multipartFile, weChartName);
                        Thread.sleep(1500);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/3/13 0013 上午 9:20
     * @Description: 发送小时数据截图到指定微信群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */


    @GetMapping("/sendHourDataImgToWeChart")
    public void sendHourDataImgToWeChart() {
        try {


            String monitorPollutant = DataFormatUtil.parseProperties("monitorPollutantList");
            String url = DataFormatUtil.parseProperties("hour.screen.shot.url");
            if (StringUtils.isNotBlank(monitorPollutant) && StringUtils.isNotBlank(url)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("AlarmTypes", Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getCode()));
                List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
                String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
                if (weChartGroups.size() > 0) {
                    for (Map<String, Object> weChartGroup : weChartGroups) {
                        weChartGroupName = weChartGroup.get("WechatName").toString();
                    }
                }
                String[] types = monitorPollutant.split(";");
                String typeAndPollutant;
                String type;
                String[] pollutants;
                String monitorTime = DataFormatUtil.getDateYMDH(new Date());
                String paramString;
                List<Map<String, Object>> pointList;
                int times = Integer.parseInt(DataFormatUtil.FormatDateOneToOther(monitorTime, "yyyy-MM-dd HH", "H")) + 1;
                paramMap.clear();
                String address;
                String pollutantCodeAndName;
                String[] monitorpointtypes;
                for (int i = 0; i < types.length; i++) {
                    typeAndPollutant = types[i];
                    type = typeAndPollutant.split(":")[0];
                    paramMap.put("outputids", Arrays.asList());

                    pointList = new ArrayList<>();
                    monitorpointtypes = type.split("_");
                    for (int j = 0; j < monitorpointtypes.length; j++) {
                        paramMap.put("monitorpointtype", monitorpointtypes[j]);
                        //获取监测点个数
                        pointList.addAll(onlineService.getMonitorPointDataByParam(paramMap));
                    }
                    //获取时间点数
                    pollutants = typeAndPollutant.split(":")[1].split(",");
                    for (int j = 0; j < pollutants.length; j++) {
                        pollutantCodeAndName = pollutants[j];
                        paramString = "monitorpointtype=" + type
                                + "&monitortime=" + monitorTime
                                + "&pollutantcode=" + pollutantCodeAndName.split("-")[0]
                                + "&pollutantname=" + pollutantCodeAndName.split("-")[1];
                        address = url + paramString;

                        System.out.println(url + "===============" + address);

                        int windowX = 160 + 60 * times + 150;
                        int windowY = 120 + 40 + 40 * pointList.size();
                        String filePath = WebDriverUtil.getDocument(address, windowX + "", windowY + "");
                        //发送图片
                        File file = new File(filePath);
                        String originalFileName = file.getName();
                        InputStream inputStream = new FileInputStream(file);
                        MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
                        String sendType = DataFormatUtil.parseProperties("send.type");
                        if ("qq".equals(sendType)) {
                            System.out.println(jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName));
                        } else {
                            System.out.println(jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/3/13 0013 上午 9:20
     * @Description: 发送文件到指定微信群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @GetMapping("/sendFileDataToWeChartTest")
    public void sendFileDataToWeChartTest() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("AlarmTypes", Arrays.asList("test_type"));
            List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
            if (weChartGroups.size() > 0) {
                //获取前一天问题报告文件
                String businessType = "1";
                String ymd = DataFormatUtil.getDateYMD(new Date());
                paramMap.clear();
                paramMap.put("uploadtime", ymd);
                paramMap.put("businesstype", businessType);
                List<FileInfoVO> fileList = fileInfoService.getFilesByParamMap(paramMap);
                InputStream inputStream = null;
                if (fileList.size() > 0) {
                    FileInfoVO fileInfoVO = fileList.get(0);
                    String fileId = fileInfoVO.getFilepath();
                    if (StringUtils.isNotBlank(fileId)) {
                        inputStream = fileController.getFileInputStream(fileId, businessType);
                    }
                    Set<String> weChartNames = new HashSet<>();
                    for (Map<String, Object> weChartGroup : weChartGroups) {
                        weChartNames.add(weChartGroup.get("WechatName").toString());
                    }
                    String originalFileName = fileInfoVO.getOriginalfilename();
                    MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
                    for (String weChartName : weChartNames) {
                        System.out.println(jnaServiceMicroService.sendGroupFileData(multipartFile, weChartName));
                        /*JSONObject jsonObject = new JSONObject();
                        jsonObject.put("username","李培");
                        jsonObject.put("message","测试");
                        jnaServiceMicroService.sendUserMessage(jsonObject);*/

                        //Thread.sleep(1500);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @GetMapping("/startScheduleTaskTest")
    public void startScheduleTaskTest() {
        try {
            // 1，获取接口需要参数；
            Map<String, Object> paramMap = new HashMap<>();
            List<String> pollutantCodes = Arrays.asList(
                    CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode(),
                    CommonTypeEnum.StinkPollutionEnum.H2SEnum.getCode(),
                    CommonTypeEnum.StinkPollutionEnum.NHEnum.getCode(),
                    CommonTypeEnum.StinkPollutionEnum.VOCEnum.getCode());
            paramMap.put("pollutantcodes", pollutantCodes);
            Date nowDay = new Date();
            String startTime = DataFormatUtil.getDateYMDH(nowDay) + ":00:00";
            String endTime = DataFormatUtil.getDateYMDH(nowDay) + ":59:59";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("collection", "HourData");
            List<String> QXCodes = Arrays.asList(
                    CommonTypeEnum.WeatherPollutionEnum.TemperatureEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.HumidityEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.PressureEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode());
            paramMap.put("pollutantcodes", pollutantCodes);
            Map<String, String> stinkMNAndPoints = onlineService.getMNAndMonitorPoint(new ArrayList<>(),
                    CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            List<Document> NDDocuments;
            List<Document> QXDocuments;
            String airMn;
            List<String> airMns;

            List<Map<String, Object>> pollutantList;
            boolean isSendMessage = false;
            for (String mnKey : stinkMNAndPoints.keySet()) {
                paramMap.put("mns", Arrays.asList(mnKey));
                NDDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                airMns = otherMonitorPointService.getAirDgimnByMonitorDgimn(mnKey);
                if (airMns.size() > 0) {
                    airMn = airMns.get(0);
                } else {
                    airMn = mnKey;
                }

                paramMap.put("mns", Arrays.asList(airMn));
                paramMap.put("pollutantcodes", QXCodes);
                QXDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                if (NDDocuments.size() > 0 && QXDocuments.size() > 0) {
                    MultiValueMap<String, String> resultMap = new LinkedMultiValueMap<>();
                    Document NDDocument = NDDocuments.get(0);
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    pollutantList = (List<Map<String, Object>>) NDDocument.get("HourDataList");
                    for (String pollutantCode : pollutantCodes) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        pollutantMap.put("pollutantname", CommonTypeEnum.StinkPollutionEnum.getNameByCode(pollutantCode));
                        pollutantMap.put("pollutantcode", pollutantCode);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                                if (pollutant.get("AvgStrength") != null && !"".equals(pollutant.get("AvgStrength"))) {
                                    pollutantMap.put("value", pollutant.get("AvgStrength"));
                                    dataList.add(pollutantMap);
                                }
                                break;
                            }
                        }
                    }
                    Document XQDocument = QXDocuments.get(0);
                    pollutantList = (List<Map<String, Object>>) XQDocument.get("HourDataList");
                    for (String pollutantCode : QXCodes) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        pollutantMap.put("pollutantname", CommonTypeEnum.WeatherPollutionEnum.getNameByCode(pollutantCode));
                        pollutantMap.put("pollutantcode", pollutantCode);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                                if (pollutant.get("AvgStrength") != null && !"".equals(pollutant.get("AvgStrength"))) {
                                    if (pollutantCode.equals("a01008")) {//风向转换
                                        String windDirection = pollutant.get("AvgStrength") != null ? DataFormatUtil.windDirectionSwitch(
                                                Double.parseDouble(pollutant.get("AvgStrength").toString()), "code") : "";
                                        pollutantMap.put("value", windDirection);
                                    } else {
                                        pollutantMap.put("value", pollutant.get("AvgStrength"));
                                    }
                                    dataList.add(pollutantMap);
                                    break;
                                }
                            }
                        }
                    }
                    mnKey = "108";
                    updateTempData(mnKey);
                    setTempDataList(dataList);
                    if (dataList.size() > 0) {
                        resultMap.add("mn", mnKey);
                        resultMap.add("datalist", dataList.toString());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(resultMap, headers);
                        ResponseEntity<String> response = restTemplate.postForEntity(SERVICE_URL, request, String.class);
                        JSONObject jsonObject = JSONObject.fromObject(response.getBody());
                        if (jsonObject.get("result") != null && jsonObject.get("result").equals("1")) {
                            isSendMessage = true;
                            break;
                        }
                    }
                }
            }
            if (isSendMessage) {
                // 4，推送服务（所有pc客户端）；
                String msg = "根据园区企业当前排放情况，当前气象扩散条件容易造成废气进城的影响，请加强企业巡查监管。";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("messagedata", msg);
                jsonObject.put("messagemethod", CommonTypeEnum.SocketTypeEnum.ForecastDataEnum.getSocketMethod());
                authSystemMicroService.sendAllClientMessage(jsonObject);
               /* // 5，推送服务（所有app端）
                paramMap.clear();
                List<Map<String, Object>> userRegisterInfo = jgUserRegisterInfoService.getUserRegisterInfoListByParam(paramMap);
                List<Map<String, Object>> remindData = new ArrayList<>();
                for (Map<String, Object> client : userRegisterInfo) {
                    if (client.get("usercode") != null && client.get("fk_userid") != null) {
                        Map<String, Object> remindMap = new HashMap<>();
                        remindMap.put("userid", client.get("fk_userid").toString());
                        remindMap.put("regid", client.get("regid"));
                        remindMap.put("reminddata", msg);
                        remindData.add(remindMap);
                    }
                }
                if (remindData.size() > 0) {
                    JSONObject JGJson = new JSONObject();
                    String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.ForecastMessage.getCode();
                    JGJson.put("messagetype", messageType);
                    JGJson.put("messagetypename", CommonTypeEnum.RabbitMQMessageTypeEnum.getNameByCode(messageType));
                    JGJson.put("messageanduserdata", remindData);
                    authSystemMicroService.sendMessageToAppClient(JGJson);
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTempData(String mnKey) {

        //
        String maxId = null;
        boolean IsSuddenChange = false;
        String changeMultiple = "0.0";

        List<AggregationOperation> aggregations = new ArrayList<>();

        Criteria criteria = Criteria.where("DataGatherCode").is(mnKey)
                .and("HourDataList.PollutantCode").is("06");
        aggregations.add(match(criteria));
        Fields fields = fields("_id", "MonitorTime", "HourDataList");
        aggregations.add(project(fields));
        aggregations.add(Aggregation.limit(2));
        aggregations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        Double lastValue = 12d;
        if (resultDocument.size() == 2) {
            Document lastData = resultDocument.get(0);
            Date lastTime = lastData.getDate("MonitorTime");
            Document preData = resultDocument.get(1);
            String lastString = DataFormatUtil.getDateYMDH(lastTime);
            List<Document> pollutants;
            String pollutantCode;
            Double preValue = null;
            String preString = DataFormatUtil.getBeforeByHourTime(1, lastString);
            String tempPre = DataFormatUtil.getDateYMDH(preData.getDate("MonitorTime"));
            if (preString.equals(tempPre)) {
                pollutants = preData.get("HourDataList", List.class);
                for (Document document : pollutants) {
                    pollutantCode = document.getString("PollutantCode");
                    if (pollutantCode.equals("06")) {
                        if (document.get("AvgStrength") != null) {
                            preValue = Double.parseDouble(document.get("AvgStrength").toString());
                        }
                        break;
                    }
                }
                if (lastValue != null && preValue != null) {
                    if (lastValue > preValue) {
                        Double rate = (lastValue - preValue) / preValue;
                        if (rate > 0.5) {
                            IsSuddenChange = true;
                        }
                        changeMultiple = DataFormatUtil.formatDouble("###0.000", rate);
                    }
                }
                maxId = lastData.get("_id").toString();
            }
        }
        //更新
        if (maxId != null) {
            Query query = Query.query(Criteria.where("_id").is(maxId).and("HourDataList.PollutantCode").is("06"));
            Update update = new Update();
            update.set("HourDataList.$.AvgStrength", lastValue.toString())
                    .set("HourDataList.$.IsSuddenChange", IsSuddenChange)
                    .set("HourDataList.$.ChangeMultiple", Double.parseDouble(changeMultiple));
            mongoTemplate.updateFirst(query, update, "HourData");
        }
    }

    private void setTempDataList(List<Map<String, Object>> dataList) {
        dataList.clear();
        Map<String, Object> ou = new HashMap<>();
        ou.put("pollutantname", "OU");
        ou.put("pollutantcode", "06");
        ou.put("value", "8.58");
        dataList.add(ou);

        Map<String, Object> H2S = new HashMap<>();
        H2S.put("pollutantname", "硫化氢");
        H2S.put("pollutantcode", "05");
        H2S.put("value", "4.38");
        dataList.add(H2S);


        Map<String, Object> voc = new HashMap<>();
        voc.put("pollutantname", "voc");
        voc.put("pollutantcode", "m005");
        voc.put("value", "6");
        dataList.add(voc);


        Map<String, Object> QW = new HashMap<>();
        QW.put("pollutantname", "气温");
        QW.put("pollutantcode", "a01012");
        QW.put("value", "10");
        dataList.add(QW);

        Map<String, Object> SD = new HashMap<>();
        SD.put("pollutantname", "湿度");
        SD.put("pollutantcode", "a01002");
        SD.put("value", "30");
        dataList.add(SD);

        Map<String, Object> DQY = new HashMap<>();
        DQY.put("pollutantname", "大气压");
        DQY.put("pollutantcode", "a01006");
        DQY.put("value", "1000");
        dataList.add(DQY);

        Map<String, Object> FS = new HashMap<>();
        FS.put("pollutantname", "风速");
        FS.put("pollutantcode", "a01007");
        FS.put("value", "1.3");
        dataList.add(FS);

        Map<String, Object> FX = new HashMap<>();
        FX.put("pollutantname", "风向");
        FX.put("pollutantcode", "a01008");
        FX.put("value", "NE");
        dataList.add(FX);

    }


    public void sendMessageToWeChartUser(JSONObject jsonObject) throws InterruptedException {
        String messageContent = "【" + jsonObject.getString("DateTime") + "】";
        if (StringUtils.isNotBlank(jsonObject.getString("PollutionName"))) {
            messageContent += jsonObject.getString("PollutionName") + "-";
        }
        messageContent += jsonObject.getString("OutPutName") + "已离线，请及时处理。";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("AlarmTypes", Arrays.asList(OfflineStatusEnum.getCode()));
        List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
        String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
        if (weChartGroups.size() > 0) {
            for (Map<String, Object> weChartGroup : weChartGroups) {
                weChartGroupName = weChartGroup.get("WechatName").toString();
            }
        }
        JSONObject sendObject = new JSONObject();
        sendObject.put("groupname", weChartGroupName);
        sendObject.put("message", messageContent);
        //推送消息到微信好友
        jnaServiceMicroService.sendGroupMessage(sendObject);
        Thread.sleep(2000);
    }

    /**
     * @author: lip
     * @date: 2021/1/28 0028 下午 4:16
     * @Description: 发送报警信息到微信群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendOverToWeChartGroup(JSONObject jsonObject) throws IOException, InterruptedException {


        String DataType = jsonObject.getString("DataType");
        if (DataType.equals("HourData")) {
            sendHourTextToGroup(jsonObject);
        } else {
            sendOtherTextToGroup(jsonObject);
        }


    }

    private void sendOtherTextToGroup(JSONObject jsonObject) throws IOException, InterruptedException {
        if ("MinuteData".equals(jsonObject.getString("DataType"))) {
            return;
        }
        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
        String AlarmType;
        List<String> alarmType = Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverStandardEnum.getCode(),
                CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverLimitEnum.getCode());
        boolean isSend = false;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            AlarmType = object.getString("AlarmType");
            if (alarmType.contains(AlarmType)) {
                isSend = true;
                break;
            }
        }
        if (!isSend) {
            return;
        }
        //主导风向
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", Arrays.asList());
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
        List<Map<String, Object>> qxPoints = onlineService.getMonitorPointDataByParam(paramMap);
        List<String> mns = new ArrayList<>();
        String mnCommon;
        if (qxPoints.size() > 0) {
            for (Map<String, Object> point : qxPoints) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
            }
        }
        paramMap.clear();
        String endTime = jsonObject.getString("DateTime");
        String pointName = jsonObject.getString("OutPutName");
        if (StringUtils.isNotBlank(jsonObject.getString("PollutionName"))) {
            pointName = jsonObject.getString("PollutionName") + "-" + pointName;
        }

        String startTime;
        String continueTime;
        Double hour;
        String overTime = "";
        int second;
        String pollutantCode;
        String MN = jsonObject.getString("MN");
        List<Document> docs;
        List<Document> pollutants;
        List<Double> overMultiples = new ArrayList<>();
        Double overMultiple;
        String sendText;
        for (int i = 0; i < jsonArray.size(); i++) {
            overMultiples.clear();
            JSONObject object = (JSONObject) jsonArray.get(i);
            AlarmType = object.getString("AlarmType");
            if (alarmType.contains(AlarmType)) {
                continueTime = object.getString("ContinueTime");
                if (StringUtils.isNotBlank(continueTime)) {
                    hour = Double.parseDouble(continueTime);
                    second = DataFormatUtil.HourToSecond(hour);
                    overTime = DataFormatUtil.secondToText(second);
                    startTime = DataFormatUtil.getBeforeBySecondTime(second, endTime);
                    pollutantCode = object.getString("PollutantCode");
                    //获取超标倍数范围
                    paramMap.put("starttime", startTime);
                    paramMap.put("endtime", endTime);
                    paramMap.put("mns", Arrays.asList(MN));
                    paramMap.put("collection", "MinuteData");
                    paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
                    docs = onlineService.getMonitorDataByParamMap(paramMap);
                    for (Document doc : docs) {
                        pollutants = doc.get("MinuteDataList", List.class);
                        for (Document pollutant : pollutants) {
                            overMultiple = pollutant.getDouble("OverMultiple");
                            if (pollutantCode.equals(pollutant.getString("PollutantCode"))
                                    && overMultiple > 0) {
                                overMultiples.add(overMultiple);
                                break;
                            }
                        }
                    }
                    String min = "";
                    String max = "";
                    if (overMultiples.size() > 0) {
                        Collections.sort(overMultiples);
                        min = DataFormatUtil.SaveOneAndSubZero(100 * overMultiples.get(0));
                        max = DataFormatUtil.SaveOneAndSubZero(100 * overMultiples.get(overMultiples.size() - 1));
                    }
                    String range = "";
                    if (StringUtils.isNotBlank(min) && StringUtils.isNotBlank(max)) {
                        if (!min.equals(max)) {
                            range = min + "%-" + max + "%";
                        } else {
                            range = min + "%";
                        }
                    }
                    paramMap.put("starttime", startTime);
                    paramMap.put("endtime", endTime);
                    paramMap.put("mns", mns);
                    paramMap.put("collection", "MinuteData");
                    String windDirectionCode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
                    String WindSpeedCode = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
                    paramMap.put("pollutantcodes", Arrays.asList(windDirectionCode, WindSpeedCode));
                    List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                    Map<String, Integer> speedAndNum = new HashMap<>();
                    Map<String, List<Double>> speedAndValueList = new HashMap<>();
                    List<Double> valueList;
                    String speedName = "XXX";
                    Double valueD;
                    //主导风向、风速
                    if (documents.size() > 0) {
                        List<Document> pollutantList;
                        String value;
                        for (Document document : documents) {
                            pollutantList = document.get("MinuteDataList", List.class);
                            speedName = "";
                            //获取风向
                            for (Document pollutant : pollutantList) {
                                if (windDirectionCode.equals(pollutant.get("PollutantCode"))) {
                                    value = pollutant.getString("AvgStrength");
                                    if (StringUtils.isNotBlank(value)) {
                                        speedName = DataFormatUtil.windDirectionSwitch(Double.parseDouble(value), "name");
                                        if (speedAndNum.containsKey(speedName)) {
                                            speedAndNum.put(speedName, speedAndNum.get(speedName) + 1);
                                        } else {
                                            speedAndNum.put(speedName, 1);
                                        }
                                    }
                                    break;
                                }
                            }
                            //获取风速
                            for (Document pollutant : pollutantList) {
                                if (WindSpeedCode.equals(pollutant.get("PollutantCode"))) {
                                    value = pollutant.getString("AvgStrength");
                                    if (StringUtils.isNotBlank(value)) {
                                        if (speedAndValueList.containsKey(speedName)) {
                                            valueList = speedAndValueList.get(speedName);
                                        } else {
                                            valueList = new ArrayList<>();
                                        }
                                        valueD = Double.parseDouble(value);
                                        valueList.add(valueD);
                                        speedAndValueList.put(speedName, valueList);
                                    }
                                }
                            }
                        }
                    }
                    speedAndNum = DataFormatUtil.sortMapByValue(speedAndNum, true);
                    for (String index : speedAndNum.keySet()) {
                        speedName = index;
                        break;
                    }
                    String speedValue = "XXX";
                    if (StringUtils.isNotBlank(speedName)) {
                        speedValue = DataFormatUtil.getListAvgValue(speedAndValueList.get(speedName));
                    }
                    Integer AlarmLevel = Integer.parseInt(object.getString("AlarmLevel"));
                    String alarmLevel = CommonTypeEnum.alarmLevelEnum.getNameByCode(AlarmLevel);
                    alarmLevel = "【" + alarmLevel + "超标提醒】：";
                    sendText = alarmLevel + startTime + " 至 " + endTime + pointName + "已超标" + overTime + "，"
                            + "超标因子为" + object.getString("PollutantName") + "，"
                            + "超标倍数范围为" + range + "，园区主导风向为" + speedName + "风、风速为" + speedValue + "m/s。";
                    paramMap.put("AlarmTypes", Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getCode()));
                    List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
                    String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
                    if (weChartGroups.size() > 0) {
                        for (Map<String, Object> weChartGroup : weChartGroups) {
                            weChartGroupName = weChartGroup.get("WechatName").toString();
                        }
                    }
                    //weChartGroupName = "微信测试群_异常数据";
                    JSONObject sendMessage = new JSONObject();
                    sendMessage.put("groupname", weChartGroupName);
                    sendMessage.put("message", sendText);
                    System.out.println("发送消息：" + sendText);
                    String sendType = DataFormatUtil.parseProperties("send.type");
                    if ("qq".equals(sendType)) {
                        jnaServiceMicroService.sendTXGroupMessage(sendMessage);
                    } else {
                        jnaServiceMicroService.sendGroupMessage(sendMessage);
                    }
                    if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("chrome.driver.path"))) {
                        sendFileToGroup(startTime, endTime, MN, jsonObject, object, pollutantCode, paramMap, sendType, weChartGroupName);
                    } else {
                        Thread.sleep(1000);
                    }
                }
            }
        }

    }

    private void sendHourTextToGroup(JSONObject jsonObject) {


        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
        String AlarmType;
        List<String> alarmType = Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverStandardEnum.getCode(),
                CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverLimitEnum.getCode());
        boolean isSend = false;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            AlarmType = object.getString("AlarmType");
            if (alarmType.contains(AlarmType)) {
                isSend = true;
                break;
            }
        }
        if (!isSend) {
            return;
        }
        //主导风向
        Map<String, Object> paramMap = new HashMap<>();

        String endTime = jsonObject.getString("DateTime");
        String pointName = jsonObject.getString("OutPutName");
        if (StringUtils.isNotBlank(jsonObject.getString("PollutionName"))) {
            pointName = jsonObject.getString("PollutionName") + "-" + pointName;
        }
        // ***企业-***排口 2022年8月31日13时 氨氮超标，监测值15mg/L（标准值：10）；化学需氧量超标，监测值15mg/L（标准值：10）
        String pollutantText = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            AlarmType = object.getString("AlarmType");
            if (alarmType.contains(AlarmType)) {
                pollutantText += object.getString("PollutantName");
                Integer AlarmLevel = Integer.parseInt(object.getString("AlarmLevel"));
                String alarmLevel = CommonTypeEnum.alarmLevelEnum.getNameByCode(AlarmLevel);
                pollutantText += alarmLevel + "超标，监测值" + object.get("MonitorValue") + object.get("PollutantUnit")
                        + "（标准值：" + object.get("StandValue") + "）；";
            }
        }
        endTime = DataFormatUtil.FormatDateOneToOther(endTime, "yyyy-MM-dd HH:mm:ss", "yyyy年MM月dd日 H时");
        pollutantText = "【超标提醒】：" + pointName + " " + endTime + " " + pollutantText;

        paramMap.put("AlarmTypes", Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getCode()));
        List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
        String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
        if (weChartGroups.size() > 0) {
            for (Map<String, Object> weChartGroup : weChartGroups) {
                weChartGroupName = weChartGroup.get("WechatName").toString();
            }
        }
        //weChartGroupName = "微信测试群_异常数据";
        JSONObject sendMessage = new JSONObject();
        sendMessage.put("groupname", weChartGroupName);
        sendMessage.put("message", pollutantText);
        System.out.println("发送消息：" + pollutantText);
        String sendType = DataFormatUtil.parseProperties("send.type");
        if ("qq".equals(sendType)) {
            jnaServiceMicroService.sendTXGroupMessage(sendMessage);
        } else {
            jnaServiceMicroService.sendGroupMessage(sendMessage);
        }
    }


    public void sendExceptionToWeChartGroup(JSONObject jsonObject) {


        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
        String ExceptionType;

        //主导风向
        Map<String, Object> paramMap = new HashMap<>();

        String endTime = jsonObject.getString("DateTime");
        String pointName = jsonObject.getString("OutPutName");
        if (StringUtils.isNotBlank(jsonObject.getString("PollutionName"))) {
            pointName = jsonObject.getString("PollutionName") + "-" + pointName;
        }
        // ***企业-***排口 2022年8月31日13时 氨氮超标，监测值15mg/L（标准值：10）；化学需氧量超标，监测值15mg/L（标准值：10）
        String pollutantText = "";

        String FirstAlarmTime;
        String ContinueTime;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            FirstAlarmTime = object.getString("FirstAlarmTime");
            ContinueTime = getContinueTime(FirstAlarmTime, endTime);
            ExceptionType = CommonTypeEnum.ExceptionTypeEnum.getNameByCode(object.getString("ExceptionType"));
            pollutantText += object.getString("PollutantName");
            pollutantText += "异常，监测值" + object.get("MonitorValue") + object.get("PollutantUnit")
                    + "（" + ExceptionType + "，持续：" + ContinueTime + "）；";

        }
        endTime = DataFormatUtil.FormatDateOneToOther(endTime, "yyyy-MM-dd HH:mm:ss", "yyyy年MM月dd日 H时m分");
        pollutantText = "【异常提醒】：" + pointName + " " + endTime + " " + pollutantText;

        paramMap.put("AlarmTypes", Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getCode()));
        List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
        String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
        if (weChartGroups.size() > 0) {
            for (Map<String, Object> weChartGroup : weChartGroups) {
                weChartGroupName = weChartGroup.get("WechatName").toString();
            }
        }
        //weChartGroupName = "微信测试群_异常数据";
        JSONObject sendMessage = new JSONObject();
        sendMessage.put("groupname", weChartGroupName);
        sendMessage.put("message", pollutantText);
        System.out.println("发送消息：" + pollutantText);
        String sendType = DataFormatUtil.parseProperties("send.type");
        if ("qq".equals(sendType)) {
            jnaServiceMicroService.sendTXGroupMessage(sendMessage);
        } else {
            jnaServiceMicroService.sendGroupMessage(sendMessage);
        }
    }

    private String getContinueTime(String firstAlarmTime, String endTime) {
        Long minute = DataFormatUtil.getDateMinutes(firstAlarmTime, endTime);
        return DataFormatUtil.countHourMinuteTime(minute.intValue());
    }

    private void sendFileToGroup(String startTime, String endTime, String MN,
                                 JSONObject jsonObject, JSONObject object, String pollutantCode, Map<String, Object> paramMap
            , String sendType, String weChartGroupName) throws IOException, InterruptedException {

        //获取主导风向、风速
        //获取分钟超标倍数范围
        //报警文字：2020-1-27 15:39:30 至 2020-1-27 16:39:30监测点1已超标1小时10分钟，
        //超标因子为TVOC，超标倍数范围为10%-54%，园区主导风向为东南风、风速2.0m/s。
        String minute = DataFormatUtil.FormatDateOneToOther(startTime, "yyyy-MM-dd HH:mm:ss", ":mm");
        startTime = DataFormatUtil.FormatDateOneToOther(startTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH");
        String startime = DataFormatUtil.getBeforeByHourTime(1, startTime) + minute + ":00";
        String paramString = "mn=" + MN
                + "&startime=" + startime
                + "&endtime=" + endTime
                + "&monitorpointtype=" + jsonObject.getString("MonitorPointTypeCode")
                + "&pollutantname=" + object.getString("PollutantName")
                + "&pollutantunit=" + object.getString("PollutantUnit")
                + "&monitorpointname=" + jsonObject.getString("OutPutName")
                + "&pollutantcode=" + pollutantCode;
        String url = DataFormatUtil.parseProperties("screen.shot.url");
        url = url + paramString;
        System.out.println(url);
        //计算高度
        //获取超标倍数范围
        paramMap.put("starttime", startime);
        paramMap.put("endtime", endTime);
        paramMap.put("mns", Arrays.asList(MN));
        paramMap.put("collection", "MinuteData");
        paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
        List<Document> docs = onlineService.getMonitorDataByParamMap(paramMap);
        int size = docs.size();
        int windowY = 529 + 39 + 70 + 40 + 24 * size;
        String filePath = WebDriverUtil.getDocument(url, "1050", windowY + "");
        //发送图片
        File file = new File(filePath);
        String originalFileName = file.getName();
        InputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
        if ("qq".equals(sendType)) {
            jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName);
        } else {
            jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName);
        }
        Thread.sleep(2000);

    }

    @PostMapping("/test")
    public void test() {
        try {
            String s = "{\n" +
                    "    \"PollutionName\": \"城市污水处理厂\",\n" +
                    "    \"DataType\": \"RealTimeData\",\n" +
                    "    \"OnlineStatus\": \"\",\n" +
                    "    \"MQMessage\": [\n" +
                    "        {\n" +
                    "            \"AlarmType\": \"Online_Exception\",\n" +
                    "            \"PollutantName\": \"化学需氧量\",\n" +
                    "            \"MonitorValue\": 11.7,\n" +
                    "            \"StandValue\": \"连续值值为：14.700\",\n" +
                    "            \"PollutantCode\": \"011\",\n" +
                    "            \"PollutantUnit\": \"mg/L\",\n" +
                    "            \"Multiple\": 0,\n" +
                    "            \"ContinueTime\": 0,\n" +
                    "            \"ExceptionType\": \"2\",\n" +
                    "            \"AlarmLevel\": null,\n" +
                    "            \"FirstAlarmTime\": \"2022-08-31 13:00:00\",\n" +
                    "            \"LastAlarmTime\": null\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"TaskID\": null,\n" +
                    "    \"PollutionID\": \"7eed74ec-28ae-4462-a0bb-a9d3937a4887\",\n" +
                    "    \"MessageType\": \"2\",\n" +
                    "    \"DateTime\": \"2022-08-31 13:09:00\",\n" +
                    "    \"MonitorPointId\": \"5f2ce453-89b9-4190-91b9-16e056f0aa4b\",\n" +
                    "    \"OutPutName\": \"废水排口\",\n" +
                    "    \"MonitorPointTypeCode\": \"1\",\n" +
                    "    \"MN\": \"29360510210013\",\n" +
                    "    \"MonitorPointCategory\": \"\",\n" +
                    "    \"IsContinueOver\": null,\n" +
                    "    \"IsContinueException\": true,\n" +
                    "    \"IsContinueChange\": null,\n" +
                    "    \"RecoveryTime\": \"2022-08-31 15:57:00\"\n" +
                    "}";
            JSONObject jsonObject = JSONObject.fromObject(s);

            sendExceptionToWeChartGroup(jsonObject);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2021/1/28 0028 下午 4:16
     * @Description: 发送报警信息到微信群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendChangeToWeChartGroup(JSONObject jsonObject) throws IOException, InterruptedException {
        if (!CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getName().equals(jsonObject.getString("DataType"))) {
            return;
        }
        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
        String AlarmType;
        List<String> alarmType = Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.ConcentrationChangeEnum.getCode());
        boolean isSend = false;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            AlarmType = object.getString("AlarmType");
            if (alarmType.contains(AlarmType)) {
                isSend = true;
                break;
            }
        }
        if (!isSend) {
            return;
        }
        //主导风向
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputids", Arrays.asList());
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
        List<Map<String, Object>> qxPoints = onlineService.getMonitorPointDataByParam(paramMap);
        List<String> mns = new ArrayList<>();
        String mnCommon;
        if (qxPoints.size() > 0) {
            for (Map<String, Object> point : qxPoints) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
            }
        }
        paramMap.clear();
        String endTime = jsonObject.getString("DateTime");
        String pointName = jsonObject.getString("OutPutName");
        if (StringUtils.isNotBlank(jsonObject.getString("PollutionName"))) {
            pointName = jsonObject.getString("PollutionName") + "-" + pointName;
        }
        String startTime;
        String continueTime;
        Double hour;
        int second;
        String pollutantCode;
        String MN = jsonObject.getString("MN");
        String sendText;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            AlarmType = object.getString("AlarmType");
            if (alarmType.contains(AlarmType)) {
                continueTime = object.getString("ContinueTime");
                if (StringUtils.isNotBlank(continueTime)) {
                    hour = Double.parseDouble(continueTime);
                    second = DataFormatUtil.HourToSecond(hour);
                    startTime = DataFormatUtil.getBeforeBySecondTime(second, endTime);
                    pollutantCode = object.getString("PollutantCode");
                    paramMap.put("starttime", startTime);
                    paramMap.put("endtime", endTime);
                    paramMap.put("mns", mns);
                    paramMap.put("collection", "MinuteData");
                    String windDirectionCode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
                    String WindSpeedCode = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
                    paramMap.put("pollutantcodes", Arrays.asList(windDirectionCode, WindSpeedCode));
                    List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                    Map<String, Integer> speedAndNum = new HashMap<>();
                    Map<String, List<Double>> speedAndValueList = new HashMap<>();
                    List<Double> valueList;
                    String speedName = "XXX";
                    Double valueD;
                    //主导风向、风速
                    if (documents.size() > 0) {
                        List<Document> pollutantList;
                        String value;
                        for (Document document : documents) {
                            pollutantList = document.get("MinuteDataList", List.class);
                            speedName = "";
                            //获取风向
                            for (Document pollutant : pollutantList) {
                                if (windDirectionCode.equals(pollutant.get("PollutantCode"))) {
                                    value = pollutant.getString("AvgStrength");
                                    if (StringUtils.isNotBlank(value)) {
                                        speedName = DataFormatUtil.windDirectionSwitch(Double.parseDouble(value), "name");
                                        if (speedAndNum.containsKey(speedName)) {
                                            speedAndNum.put(speedName, speedAndNum.get(speedName) + 1);
                                        } else {
                                            speedAndNum.put(speedName, 1);
                                        }
                                    }
                                    break;
                                }
                            }
                            //获取风速
                            for (Document pollutant : pollutantList) {
                                if (WindSpeedCode.equals(pollutant.get("PollutantCode"))) {
                                    value = pollutant.getString("AvgStrength");
                                    if (StringUtils.isNotBlank(value)) {
                                        if (speedAndValueList.containsKey(speedName)) {
                                            valueList = speedAndValueList.get(speedName);
                                        } else {
                                            valueList = new ArrayList<>();
                                        }
                                        valueD = Double.parseDouble(value);
                                        valueList.add(valueD);
                                        speedAndValueList.put(speedName, valueList);
                                    }
                                }
                            }
                        }
                    }
                    speedAndNum = DataFormatUtil.sortMapByValue(speedAndNum, true);
                    for (String index : speedAndNum.keySet()) {
                        speedName = index;
                        break;
                    }
                    String speedValue = "XXX";
                    if (StringUtils.isNotBlank(speedName)) {
                        speedValue = DataFormatUtil.getListAvgValue(speedAndValueList.get(speedName));
                    }
                    sendText = "【突高提醒】：" + endTime + pointName + object.getString("PollutantName") + "监测值突然升高，" + "，园区主导风向为" + speedName + "风、风速为" + speedValue + "m/s。";
                    paramMap.put("AlarmTypes", Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getCode()));
                    List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
                    String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
                    if (weChartGroups.size() > 0) {
                        for (Map<String, Object> weChartGroup : weChartGroups) {
                            weChartGroupName = weChartGroup.get("WechatName").toString();
                        }
                    }
                    JSONObject sendMessage = new JSONObject();
                    sendMessage.put("groupname", weChartGroupName);
                    sendMessage.put("message", sendText);
                    String sendType = DataFormatUtil.parseProperties("send.type");
                    if ("qq".equals(sendType)) {
                        jnaServiceMicroService.sendTXGroupMessage(sendMessage);
                    } else {
                        jnaServiceMicroService.sendGroupMessage(sendMessage);
                    }
                    startTime = DataFormatUtil.FormatDateOneToOther(startTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
                    String paramString = "dgimn=" + MN
                            + "&startTime=" + startTime
                            + "&endTime=" + startTime
                            + "&isHasConvertData=0"
                            + "&monitorType=" + jsonObject.getString("MonitorPointTypeCode")
                            + "&pollutantName=" + object.getString("PollutantName")
                            + "&pollutantUnit=" + object.getString("PollutantUnit")
                            + "&titleName=" + pointName + endTime + object.getString("PollutantName") + "浓度突高趋势图"
                            + "&pollutantCode=" + pollutantCode;

                    String url = DataFormatUtil.parseProperties("change.screen.shot.url");
                    url = url + paramString;
                    //计算高度
                    String filePath = WebDriverUtil.getDocument(url, "1050", "500");
                    //发送图片
                    File file = new File(filePath);
                    String originalFileName = file.getName();
                    InputStream inputStream = new FileInputStream(file);
                    MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
                    if ("qq".equals(sendType)) {
                        jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName);
                    } else {
                        jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName);
                    }
                    Thread.sleep(2000);
                }
            }
        }
    }


    /**
     * @Description: 废水、雨水、污水处理厂每日数据推送
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/7 9:45
     */
    public void waterDayDataSendTask() {
        try {
            //污水处理厂数据推送
            String monitortime = DataFormatUtil.getDateYMD(new Date());

            monitortime = DataFormatUtil.getBeforeByDayTime(1, monitortime);
            Object resultObject = onlineWaterController.getTreatmentPlantDayConcentrationDataByParam(monitortime);
            if (resultObject != null) {
                Map<String, Object> resultMap = (Map<String, Object>) resultObject;
                Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
                List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) dataMap.get("tabletitledata");
                int width = 220;
                List<Map<String, Object>> children;
                for (Map<String, Object> titleMap : tabletitledata) {
                    if (titleMap.get("children") != null) {
                        children = (List<Map<String, Object>>) titleMap.get("children");
                        width += children.size() * 150;
                    }
                }
                List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) dataMap.get("tablelistdata");
                int height = (tablelistdata.size() + 4) * 48;
                String paramString = "monitortime=" + monitortime;
                String url = DataFormatUtil.parseProperties("sewagewater.screen.shot.url");
                url = url + paramString;
                System.out.println(url);
                //计算高度
                String filePath = WebDriverUtil.getDocument(url, width + "", height + "");
                //发送图片
                File file = new File(filePath);
                String originalFileName = file.getName();
                String sendType = DataFormatUtil.parseProperties("send.type");
                InputStream inputStream = new FileInputStream(file);
                MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("AlarmTypes", Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getCode()));
                List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
                String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
                if (weChartGroups.size() > 0) {
                    for (Map<String, Object> weChartGroup : weChartGroups) {
                        weChartGroupName = weChartGroup.get("WechatName").toString();
                    }
                }
                if ("qq".equals(sendType)) {
                    jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName);
                } else {
                    jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName);
                }
                Thread.sleep(2000);
                //废水统计图
                String pollutantCodeAndUnits = DataFormatUtil.parseProperties("waterPollutantCode");
                if (StringUtils.isNotBlank(pollutantCodeAndUnits)) {
                    String[] itemList = pollutantCodeAndUnits.split(",");
                    String pollutantCode;
                    String pollutantUnit;
                    String pollutantName;
                    url = DataFormatUtil.parseProperties("wastewater.screen.shot.url");

                    for (int i = 0; i < itemList.length; i++) {
                        pollutantCode = itemList[i].split("_")[0];
                        pollutantUnit = itemList[i].split("_")[1];
                        pollutantName = itemList[i].split("_")[2];
                        resultObject = onlineWaterController.getWaterPollutantDayConcentrationDataByParam(monitortime, CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode(), pollutantCode);
                        if (resultObject != null) {
                            dataMap = (Map<String, Object>) resultObject;
                            List<Object> dataList = (List<Object>) dataMap.get("data");
                            height = (dataList.size() + 4) * 48;
                            //计算高度
                            //发送图片
                            paramString = "monitortime=" + monitortime
                                    + "&pollutantcode=" + pollutantCode
                                    + "&pollutantunit=" + pollutantUnit
                                    + "&pollutantname=" + pollutantName;
                            String urlThis = url + paramString;
                            System.out.println(urlThis);
                            filePath = WebDriverUtil.getDocument(urlThis, "1330", height + "");
                            file = new File(filePath);
                            originalFileName = file.getName();
                            inputStream = new FileInputStream(file);
                            multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);

                            if ("qq".equals(sendType)) {
                                jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName);
                            } else {
                                jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName);
                            }
                            Thread.sleep(2000);
                        }
                    }
                }
                //雨水统计分析
                pollutantCodeAndUnits = DataFormatUtil.parseProperties("rainPollutantCode");
                if (StringUtils.isNotBlank(pollutantCodeAndUnits)) {
                    String[] itemList = pollutantCodeAndUnits.split(",");
                    String pollutantCode;
                    String pollutantUnit;
                    String pollutantName;
                    url = DataFormatUtil.parseProperties("rain.screen.shot.url");
                    for (int i = 0; i < itemList.length; i++) {
                        pollutantCode = itemList[i].split("_")[0];
                        pollutantUnit = itemList[i].split("_")[1];
                        pollutantName = itemList[i].split("_")[2];
                        resultObject = onlineWaterController.getWaterPollutantDayConcentrationDataByParam(monitortime, CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode(), pollutantCode);
                        if (resultObject != null) {
                            dataMap = (Map<String, Object>) resultObject;
                            List<Object> dataList = (List<Object>) dataMap.get("data");
                            height = (dataList.size() + 4) * 48;
                            //计算高度
                            //发送图片
                            paramString = "monitortime=" + monitortime
                                    + "&pollutantcode=" + pollutantCode
                                    + "&pollutantunit=" + pollutantUnit
                                    + "&pollutantname=" + pollutantName;
                            String urlThis = url + paramString;
                            System.out.println(urlThis);
                            filePath = WebDriverUtil.getDocument(urlThis, "1330", height + "");
                            file = new File(filePath);
                            originalFileName = file.getName();
                            inputStream = new FileInputStream(file);
                            multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);

                            if ("qq".equals(sendType)) {
                                jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName);
                            } else {
                                jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName);
                            }
                            Thread.sleep(2000);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: 设备问题记录推送
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/5/27 10:42
     */
    public void deviceProblemRecord() {

        try {
            String monitortime = DataFormatUtil.getDateYMD(new Date());
            monitortime = DataFormatUtil.getBeforeByDayTime(1, monitortime);
            JSONObject jsonObject = new JSONObject();
            Map<String, Object> paramMap = new HashMap<>();
            String starttime = monitortime + " 00";
            String endtime = monitortime + " 23";
            paramMap.put("startdate", starttime);
            paramMap.put("enddate", endtime);
            jsonObject.put("paramsjson", paramMap);
            Object resultObject = deviceProblemRecordController.getDeviceProblemRecordsByParamMap(jsonObject);
            if (resultObject != null) {
                Map<String, Object> resultMap = (Map<String, Object>) resultObject;
                Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
                int total = Integer.parseInt(dataMap.get("total").toString());
                if (total > 0) {
                    int height = (total + 4) * 48;
                    String paramString = "starttime=" + starttime + "&endtime=" + endtime;
                    String url = DataFormatUtil.parseProperties("device.problem.shot.url");
                    url = url + paramString;
                    //计算高度
                    String filePath = WebDriverUtil.getDocument(url, "1330", height + "");
                    //发送图片
                    File file = new File(filePath);
                    String originalFileName = file.getName();
                    String sendType = DataFormatUtil.parseProperties("send.type");
                    InputStream inputStream = new FileInputStream(file);
                    MultipartFile multipartFile = new MockMultipartFile("attachment", originalFileName, "", inputStream);
                    paramMap.put("AlarmTypes", Arrays.asList(OfflineStatusEnum.getCode()));
                    List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
                    String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
                    if (weChartGroups.size() > 0) {
                        for (Map<String, Object> weChartGroup : weChartGroups) {
                            weChartGroupName = weChartGroup.get("WechatName").toString();
                        }
                    }
                    if ("qq".equals(sendType)) {
                        jnaServiceMicroService.sendTXGroupFileData(multipartFile, weChartGroupName);
                    } else {
                        jnaServiceMicroService.sendGroupFileData(multipartFile, weChartGroupName);
                    }
                    Thread.sleep(2000);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void isAllOffLineStatus() throws InterruptedException {
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> dataList = deviceStatusService.getDeviceStatusDataByParam(paramMap);
        List<String> offLines = deviceStatusService.getOnLinePoints(CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode());
        if (dataList.size() > 0 && offLines.size() == 0) {
            String messageContent = "【网络或程序故障提醒】所有设备已离线，请检查采集服务器网络或程序是否正常。";
            paramMap.put("AlarmTypes", Arrays.asList(OfflineStatusEnum.getCode()));
            List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
            String weChartGroupName = CommonTypeEnum.WechatPushSetAlarmTypeEnum.QHY_AlarmPushEnum.getName();
            if (weChartGroups.size() > 0) {
                for (Map<String, Object> weChartGroup : weChartGroups) {
                    weChartGroupName = weChartGroup.get("WechatName").toString();
                }
            }
            JSONObject sendObject = new JSONObject();
            sendObject.put("groupname", weChartGroupName);
            sendObject.put("message", messageContent);
            //推送消息到微信好友
            jnaServiceMicroService.sendGroupMessage(sendObject);
            Thread.sleep(2000);
        }
    }

    public void sendTestToWeChartGroup(String test) {
        JSONObject sendObject = new JSONObject();
        sendObject.put("groupname", "测试群");
        sendObject.put("message", test);
        //推送消息到微信好友
        jnaServiceMicroService.sendGroupMessage(sendObject);

    }
}
