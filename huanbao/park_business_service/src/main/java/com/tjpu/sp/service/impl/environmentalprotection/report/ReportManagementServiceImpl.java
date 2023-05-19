package com.tjpu.sp.service.impl.environmentalprotection.report;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.report.AnalysisReportDataMapper;
import com.tjpu.sp.dao.environmentalprotection.report.ReportInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.parkinfo.controlrecords.ControlRecordsMapper;
import com.tjpu.sp.model.environmentalprotection.report.AnalysisReportDataVO;
import com.tjpu.sp.model.environmentalprotection.report.ReportInfoVO;
import com.tjpu.sp.model.environmentalprotection.controlrecords.ControlRecordsVO;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.report.ReportManagementService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportManagementServiceImpl implements ReportManagementService {

    @Autowired
    private ReportInfoMapper reportInfoMapper;

    @Autowired
    private AnalysisReportDataMapper analysisReportDataMapper;

    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;

    @Autowired
    private ControlRecordsMapper controlRecordsMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;

    @Autowired
    private PollutionTraceSourceService pollutionTraceSourceService;

    @Autowired
    private OnlineService onlineService;

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 上午 10:06
     * @Description: 保存报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void insertSelective(ReportInfoVO reportInfoVO) {
        try {
            reportInfoMapper.insert(reportInfoVO);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 上午 10:26
     * @Description: 根据自定义参数获取相关报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getReportInfosByParamMap(Map<String, Object> paramMap, List<String> reporttypes) {
        List<Map<String, Object>> result = reportInfoMapper.getReportInfosByParamMap(paramMap);
        List<Map<String, Object>> listdata = new ArrayList<>();
        Collections.sort(reporttypes);
        for (String str : reporttypes) {
            List<Map<String, Object>> reports = new ArrayList<>();
            Map<String, Object> objmap = new HashMap<String, Object>();
            if (result != null && result.size() > 0) {
                for (Map<String, Object> obj : result) {
                    if (str.equals(obj.get("ReportType").toString())) {//当类型相同时
                        reports.add(obj);
                    }
                }
            }
            objmap.put("type", str);
            objmap.put("reports", reports);
            listdata.add(objmap);
        }
        return listdata;
    }

    /**
     * @author: xsm
     * @date: 2019/7/27 0027 下午 3:38
     * @Description: 根据主键ID删除报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void deleteReportInfo(String pkid) {
        reportInfoMapper.deleteByPrimaryKey(pkid);
    }


    /**
     * @author: lip
     * @date: 2019/8/26 0026 上午 11:14
     * @Description: 自定义查询条件获取分析报告属性数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getReportAttributeDataByParam(Map<String, Object> paramMap) {
        return analysisReportDataMapper.getReportAttributeDataByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/8/26 0026 下午 2:34
     * @Description: 暂存分析报告属性数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateReportAttributeDataByParam(Map<String, Object> paramMap) {

        //删除相关属性数据
        if (paramMap.size() > 0) {
            analysisReportDataMapper.deleteReportAttributeDataByParam(paramMap);
            String username = paramMap.get("") != null ? paramMap.get("").toString() : "";
            Date updateDate = new Date();
            List<Map<String, Object>> attributeMap = (List<Map<String, Object>>) paramMap.get("attributedata");
            Short reporttype = Short.parseShort(paramMap.get("reporttype").toString());
            Date analysisreportstarttime = DataFormatUtil.getDateYMDHMS(paramMap.get("analysisreportstarttime").toString());
            Date analysisreportendtime = DataFormatUtil.getDateYMDHMS(paramMap.get("analysisreportendtime").toString());
            Date reportmakedate = DataFormatUtil.getDateYMD(paramMap.get("reportmakedate").toString());
            String reportattributecode;
            String reportattributevalue;
            List<AnalysisReportDataVO> analysisReportDataVOS = new ArrayList<>();
            for (Map<String, Object> map : attributeMap) {
                if (map.get("reportattributecode") != null && map.get("reportattributevalue") != null) {
                    reportattributecode = map.get("reportattributecode").toString();
                    reportattributevalue = map.get("reportattributevalue").toString();
                    AnalysisReportDataVO analysisReportDataVO = new AnalysisReportDataVO();
                    analysisReportDataVO.setPkId(UUID.randomUUID().toString());
                    analysisReportDataVO.setReporttype(reporttype);
                    analysisReportDataVO.setUpdatetime(updateDate);
                    analysisReportDataVO.setUpdateuser(username);
                    analysisReportDataVO.setAnalysisreportstarttime(analysisreportstarttime);
                    analysisReportDataVO.setAnalysisreportendtime(analysisreportendtime);
                    analysisReportDataVO.setReportmakedate(reportmakedate);
                    analysisReportDataVO.setReportattributecode(reportattributecode);
                    analysisReportDataVO.setReportattributevalue(reportattributevalue);
                    analysisReportDataVOS.add(analysisReportDataVO);
                }
            }
            analysisReportDataMapper.batchInsert(analysisReportDataVOS);
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/8 0008 下午 5:45
     * @Description: 插入管控建议数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addControlData(Map<String, Object> paramMap) {
        //根据厂界恶臭的MQ中的污染物（预警或超标时间往前推一个小时）分钟数据进行判断，园区主导风向，找出需要管控的企业；
        Map<String, String> mnAndPollution = new HashMap<>();
        List<String> pollutionNames = new ArrayList<>();
        Map<String, List<Double>> mnAndJW = new HashMap<>();
        double longitude;
        double latitude;
        String mnCommon;
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        List<Map<String, Object>> pointDataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("dgimns", Arrays.asList(paramMap.get("mn")));
        List<Map<String, Object>> HJDataList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(tempMap);
        if (pointDataList.size() > 0 && isHaveHJData(HJDataList)) {
            List<String> CJMNList = new ArrayList<>();
            for (Map<String, Object> pointData : pointDataList) {
                if (pointData.get("dgimn") != null) {
                    mnCommon = pointData.get("dgimn").toString();
                    if (pointData.get("pollutionname") != null && pointData.get("Longitude") != null && pointData.get("Latitude") != null) {
                        mnAndPollution.put(mnCommon, pointData.get("pollutionname").toString());
                        CJMNList.add(mnCommon);
                        longitude = Double.parseDouble(pointData.get("Longitude").toString());
                        latitude = Double.parseDouble(pointData.get("Latitude").toString());
                        mnAndJW.put(mnCommon, Arrays.asList(longitude, latitude));
                    }
                }
            }
            CJMNList = CJMNList.stream().distinct().collect(Collectors.toList());
            String time = paramMap.get("datetime").toString();
            String tempTime = DataFormatUtil.formatIntMinuteTime(time);
            Date nowDay = DataFormatUtil.getDateYMDHMS(tempTime);
            String ms = DataFormatUtil.getDateMS(nowDay);
            String endTime = DataFormatUtil.getDateYMDHMS(nowDay);
            String startTime = DataFormatUtil.getBeforeByHourTime(1, DataFormatUtil.getDateYMDH(nowDay)) + ":" + ms;
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("mns", CJMNList);
            paramMap.put("collection", "MinuteData");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<String> pollutantCodes = (List<String>) paramMap.get("pollutantcodes");
            List<String> heightMnList = getHeightValuePollution(documents, pollutantCodes);
            if (heightMnList.size() > 0) {
                //获取传输点经纬度
                Object main_wind = paramMap.get("winddirection");
                double lat_a = Double.parseDouble(HJDataList.get(0).get("Latitude").toString());
                double lng_a = Double.parseDouble(HJDataList.get(0).get("Longitude").toString());
                double azimuth;
                //获取符合的点位信息
                for (String mnIndex : heightMnList) {
                    latitude = mnAndJW.get(mnIndex).get(1);
                    longitude = mnAndJW.get(mnIndex).get(0);
                    //获取事件相当于点位的方位角
                    azimuth = DataFormatUtil.getAngle1(lat_a, lng_a, latitude, longitude);
                    //获取点位风向值
                    //判断是否属于上风向点位
                    if (isUpperWindDirection(main_wind, azimuth)) {
                        pollutionNames.add(mnAndPollution.get(mnIndex));
                    }
                }
                if (pollutionNames.size()>0){
                    pollutionNames = pollutionNames.stream().distinct().collect(Collectors.toList());
                    String HJName = paramMap.get("outputname").toString();
                    List<String> pollutantNames = (List<String>) paramMap.get("pollutantNames");

                    String desc = HJName+"在"+DataFormatUtil.FormatDateOneToOther(time,"yyyy-MM-dd HH:mm:ss","H时m分")
                            +"出现"+DataFormatUtil.FormatListToString(pollutantNames,"、")+"监测值超标现象，结合气象数据与监测数据分析，建议加大对"
                            +DataFormatUtil.FormatListToString(pollutionNames,"、")+"巡查力度。";
                    ControlRecordsVO controlRecordsVO = new ControlRecordsVO();
                    controlRecordsVO.setPkId(UUID.randomUUID().toString());
                    controlRecordsVO.setDgimn(paramMap.get("mn").toString());
                    String codes = DataFormatUtil.FormatListToString(pollutantCodes,",");
                    controlRecordsVO.setControldesc(desc);
                    controlRecordsVO.setPollutantcodes(codes);
                    controlRecordsVO.setHappentime(DataFormatUtil.getDateYMDHMS(time));
                    controlRecordsVO.setUpdatetime(new Date());
                    controlRecordsMapper.insert(controlRecordsVO);
                }


            }


        }


        //建议模板：管委会恶臭站点16.5恶臭监测值超标现象，结合气象数据与监测数据分析，建议加大对***公司、**公司巡查力度。
    }

    @Override
    public List<Map<String, Object>> countReportDataByParam(Map<String, Object> paramMap) {
        return analysisReportDataMapper.countReportDataByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/9/12 0012 下午 1:17
     * @Description: 判断是否属于上风向点位
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean isUpperWindDirection(Object main_wind, double azimuth) {
        boolean isUpper = false;
        Double mainWind = Double.parseDouble(main_wind.toString());
        Double interval = Double.parseDouble(DataFormatUtil.parseProperties("wind.direction.interval"));
        Double min;
        Double max;
        for (Double i = 0d; i <= 360d; i = i + interval) {
            min = i;
            max = interval + i;
            if (mainWind >= min && mainWind <= max) {//反向区间
                if (azimuth > min && azimuth < max) {//方位角是否在反向区间内
                    isUpper = true;
                }
                break;
            }
        }

        return isUpper;

    }

    private boolean isHaveHJData(List<Map<String, Object>> HJDataList) {
        boolean isHave = false;
        if (HJDataList.size() > 0) {
            Map<String, Object> HJData = HJDataList.get(0);
            if (HJData.get("Longitude") != null && HJData.get("Latitude") != null) {
                isHave = true;
            }
        }
        return isHave;


    }

    /**
     * @author: lip
     * @date: 2020/5/9 0009 上午 10:30
     * @Description: 获取监测值较高的企业
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getHeightValuePollution(List<Document> documents, List<String> pollutantCodes) {
        List<String> mns = new ArrayList<>();
        if (documents.size() > 0) {
            Map<String, Map<String, LinkedHashMap<String, Float>>> timeAndPollutantAndMnAndValue = new HashMap<>();
            Map<String, LinkedHashMap<String, Float>> pollutantAndMnAndValue;
            LinkedHashMap<String, Float> mnAndValue;
            String mnCommon;
            String pollutantCode;
            String ymdhm;
            float monitorValue;
            List<Document> pollutantList;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                ymdhm = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                pollutantList = document.get("MinuteDataList", List.class);
                for (Document pollutant : pollutantList) {
                    pollutantCode = pollutant.getString("PollutantCode");
                    if (pollutantCodes.contains(pollutantCode) && pollutant.get("AvgStrength") != null) {
                        monitorValue = Float.parseFloat(pollutant.get("AvgStrength").toString());
                        if (timeAndPollutantAndMnAndValue.containsKey(ymdhm)) {
                            pollutantAndMnAndValue = timeAndPollutantAndMnAndValue.get(ymdhm);
                        } else {
                            pollutantAndMnAndValue = new LinkedHashMap<>();
                        }
                        if (pollutantAndMnAndValue.containsKey(pollutantCode)) {
                            mnAndValue = pollutantAndMnAndValue.get(pollutantCode);
                        } else {
                            mnAndValue = new LinkedHashMap<>();
                        }
                        mnAndValue.put(mnCommon, monitorValue);
                        pollutantAndMnAndValue.put(pollutantCode, mnAndValue);
                        timeAndPollutantAndMnAndValue.put(ymdhm, pollutantAndMnAndValue);
                    }
                }
            }
            if (timeAndPollutantAndMnAndValue.size() > 0) {
                LinkedHashMap<String,Float> mnTimeAndValue = new LinkedHashMap<>();
                Map<String,Float> highMap;
                for (String timeIndex : timeAndPollutantAndMnAndValue.keySet()) {
                    pollutantAndMnAndValue = timeAndPollutantAndMnAndValue.get(timeIndex);
                    for (String codeIndex : pollutantAndMnAndValue.keySet()) {
                        mnAndValue = pollutantAndMnAndValue.get(codeIndex);
                        highMap = AlarmRemindUtil.findOutLiers(mnAndValue);
                        if (highMap.size() > 0) {
                            for (String mnIndex:highMap.keySet()){
                                mnTimeAndValue.put(mnIndex+"#"+timeIndex,highMap.get(mnIndex));
                            }
                        }

                    }
                }
                highMap = AlarmRemindUtil.findOutLiers(mnTimeAndValue);
                if (highMap.size()>0){
                    for (String mnTime:highMap.keySet()){
                        mns.add(mnTime.split("#")[0]);
                    }
                }
            }
        }
        return mns.stream().distinct().collect(Collectors.toList());


    }
}
