package com.tjpu.sp.controller.environmentalprotection.effectivetransmission;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.HourQueryDataVO;
import com.tjpu.sp.model.environmentalprotection.online.EffectiveTransmissionVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.MonitorPointCommonService;
import com.tjpu.sp.service.environmentalprotection.online.EffectiveTransmissionService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt;

@RestController
@RequestMapping("effectiveTransmission")
public class EffectiveTransmissionController {
    private final EffectiveTransmissionService effectiveTransmissionService;
    @Autowired
    private MonitorPointCommonService monitorPointCommonService;

    private final String hourcollection = "HourData";
    @Autowired
    private MongoBaseService mongoBaseService;

    public EffectiveTransmissionController(EffectiveTransmissionService effectiveTransmissionService) {
        this.effectiveTransmissionService = effectiveTransmissionService;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/12 10:47
     * @Description: 根据监测点类型获取传输有效率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEffectiveTransmissionByParamMap", method = RequestMethod.POST)
    public Object getEffectiveTransmissionByParamMap(@RequestJson(value = "monitorpointtype") Integer monitorPointType,
                                                     @RequestJson(value = "starttime", required = false) String starttime,
                                                     @RequestJson(value = "dgimn", required = false) String dgimn,
                                                     @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            CommonTypeEnum.MonitorPointTypeEnum monitorPointTypeEnum = getCodeByInt(monitorPointType);
            if (monitorPointTypeEnum == null) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            Map<String, Object> paramMap = new HashMap<>();
            //添加数据权限
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("datauserid", userid);
            paramMap.put("monitorpointtype", monitorPointType);
            paramMap.put("dgimn", dgimn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> estData = effectiveTransmissionService.getEffectiveTransmissionByParamMap(paramMap);
            setEffectiveTransmissionMap(estData, resultMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/12 10:47
     * @Description: 根据监测点类型获取传输有效率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkEffectiveTransmissionByParamMap", method = RequestMethod.POST)
    public Object getStinkEffectiveTransmissionByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                          @RequestJson(value = "dgimn", required = false) String dgimn,
                                                          @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            paramMap.put("entmonitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
            paramMap.put("dgimn", dgimn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> estData = effectiveTransmissionService.getStinkEffectiveTransmissionByParamMap(paramMap);
            setEffectiveTransmissionMap(estData, resultMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void setEffectiveTransmissionMap(List<Map<String, Object>> estData, Map<String, Object> resultMap) {

        if (estData.size() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
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
            resultMap.put("transmissionrate", decimalFormat.format(transmissionrate * 100) + "%");
            resultMap.put("effectiverate", decimalFormat.format(effectiverate * 100) + "%");
            resultMap.put("transmissioneffectiverate", decimalFormat.format(transmissioneffectiverate * 100) + "%");

        } else {
            resultMap.put("effectiverate", "0%");
            resultMap.put("transmissionrate", "0%");
            resultMap.put("transmissioneffectiverate", "0%");
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 下午 2:58
     * @Description: 获取企业及排口传输有效率信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEffectiveTransmissionInfoByParamMap", method = RequestMethod.POST)
    public Object getEffectiveTransmissionInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
            Map<String, Object> paramMap = (Map) paramsjson;
            List<Map<String, Object>> effectiveTransmissionInfoByParamMap = effectiveTransmissionService.getPollutionEffectiveTransmissionInfoByParamMap(paramMap);

            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            Map<String, Object> resultMap = new HashMap<>();
            int total = effectiveTransmissionInfoByParamMap.size();
            if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
                Integer pagenum = Integer.valueOf(paramMap.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(paramMap.get("pagesize").toString());
                effectiveTransmissionInfoByParamMap = effectiveTransmissionInfoByParamMap.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            for (Map<String, Object> map : effectiveTransmissionInfoByParamMap) {
                Set<Map<String, Object>> set = (Set) map.get("monitorpointlist");
                map.remove("pkid");
                //实传输数量
                Double transmissionnumber = set.stream().peek(m -> m.put("transmissionnumber", decimalFormat.format(Integer.valueOf(m.get("transmissionnumber") == null ? "0" : m.get("transmissionnumber")
                        .toString())))).map(m -> Double.valueOf(m.get("transmissionnumber") == null|| !(m.get("isEffectiveTransmission") != null && Integer.valueOf(m.get("isEffectiveTransmission").toString()) == 1)
                        ? "0d" : m.get("transmissionnumber").toString())).collect(Collectors.summingDouble(m -> m));
                //实有效数量
                Double effectivenumber = set.stream().peek(m -> m.put("effectivenumber", decimalFormat.format(Integer.valueOf(m.get("effectivenumber") == null ? "0" : m.get("effectivenumber")
                        .toString())))).map(m -> Double.valueOf(m.get("effectivenumber") == null || !(m.get("isEffectiveTransmission") != null && Integer.valueOf(m.get("isEffectiveTransmission").toString()) == 1)
                        ? "0d" : m.get("effectivenumber").toString())).collect(Collectors.summingDouble(m -> m));
                //应传输数量
                Double shouldnumber = set.stream().peek(m -> m.put("shouldnumber", decimalFormat.format(Integer.valueOf(m.get("shouldnumber") == null ? "0" : m.get("shouldnumber")
                        .toString())))).map(m -> Double.valueOf(m.get("shouldnumber") == null || !(m.get("isEffectiveTransmission") != null && Integer.valueOf(m.get("isEffectiveTransmission").toString()) == 1)
                        ? "0d" : m.get("shouldnumber").toString())).collect(Collectors.summingDouble(m -> m));
                //应有效数量
                Double shouldeffectivenumber = set.stream().peek(m -> m.put("shouldeffectivenumber", decimalFormat.format(Integer.valueOf(m.get("shouldeffectivenumber") == null ? "0" : m.get("shouldeffectivenumber")
                        .toString())))).map(m -> Double.valueOf(m.get("shouldeffectivenumber") == null || !(m.get("isEffectiveTransmission") != null && Integer.valueOf(m.get("isEffectiveTransmission").toString()) == 1)
                        ? "0d" : m.get("shouldeffectivenumber").toString())).collect(Collectors.summingDouble(m -> m));
                //传输率
                double transmissionrate = shouldnumber == 0d ? 0d : transmissionnumber / shouldnumber;
                //有效率
                double effectiverate = shouldeffectivenumber == 0d ? 0d : effectivenumber / shouldeffectivenumber;
                //传输有效率
                double transmissioneffectiverate = transmissionrate * effectiverate;
                map.put("transmissionnumber", transmissionnumber);
                map.put("effectivenumber", effectivenumber);
                map.put("shouldnumber", shouldnumber);
                map.put("shouldeffectivenumber", shouldeffectivenumber);
                map.put("transmissionrate", decimalFormat.format(transmissionrate * 100) + "%");
                map.put("effectiverate", decimalFormat.format(effectiverate * 100) + "%");
                map.put("transmissioneffectiverate", decimalFormat.format(transmissioneffectiverate * 100) + "%");
            }
            resultMap.put("total", total);
            resultMap.put("datalist", effectiveTransmissionInfoByParamMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 下午 5:24
     * @Description: 获取企业下排口传输有效率详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEffectiveTransmissionDetailByParamMap", method = RequestMethod.POST)
    public Object getEffectiveTransmissionDetailByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
            Map<String, Object> paramMap = (Map) paramsjson;
            String isexport = paramMap.get("isexport") != null ? paramMap.get("isexport").toString() : "";
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
                Integer pagenum = Integer.valueOf(paramMap.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(paramMap.get("pagesize").toString());
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("userid",RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class));
            List<Map<String, Object>> effectiveTransmissionInfoByParamMap = effectiveTransmissionService.getMonitorEffectiveTransmissionInfoByParamMap(paramMap);

            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(effectiveTransmissionInfoByParamMap);
            long total = pageInfo.getTotal();

            effectiveTransmissionInfoByParamMap.stream().forEach(m -> {
                Double shouldnumber = m.get("shouldnumber") == null ? 0d : Double.valueOf(m.get("shouldnumber").toString());
                Double shouldeffectivenumber = m.get("shouldeffectivenumber") == null ? 0d : Double.valueOf(m.get("shouldeffectivenumber").toString());
                Double transmissionnumber = m.get("transmissionnumber") == null ? 0d : Double.valueOf(m.get("transmissionnumber").toString());
                Double effectivenumber = m.get("effectivenumber") == null ? 0d : Double.valueOf(m.get("effectivenumber").toString());

                //传输率
                double transmissionrate = shouldnumber == 0d ? 0d : transmissionnumber / shouldnumber;
                //有效率
                double effectiverate = shouldeffectivenumber == 0d ? 0d : effectivenumber / shouldeffectivenumber;
                //传输有效率
                double transmissioneffectiverate = transmissionrate * effectiverate;

                if ("".equals(isexport)) {
                    m.put("TransmissionRate", decimalFormat.format(transmissionrate * 100) + "%");
                    m.put("EffectiveRate", decimalFormat.format(effectiverate * 100) + "%");
                    m.put("TransmissionEffectiveRate", decimalFormat.format(transmissioneffectiverate * 100) + "%");
                } else {
                    m.put("TransmissionRate", decimalFormat.format(transmissionrate * 100));
                    m.put("EffectiveRate", decimalFormat.format(effectiverate * 100));
                    m.put("TransmissionEffectiveRate", decimalFormat.format(transmissioneffectiverate * 100));
                }
            });
            resultMap.put("total", total);
            resultMap.put("datalist", effectiveTransmissionInfoByParamMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/13 0013 下午 3:33
     * @Description: 查询排口传输有效率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkmonitorpointtypecodes, fkpollutionid, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getOutPutEffectiveTransmissionByParams", method = RequestMethod.POST)
    public Object getOutPutEffectiveTransmissionByParams(@RequestJson(value = "fkmonitorpointtypecodes", required = false) Object fkmonitorpointtypecodes,
                                                         @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                         @RequestJson(value = "starttime", required = false) String starttime,
                                                         @RequestJson(value = "endtime", required = false) String endtime,
                                                         @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                         @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List list = (List) fkmonitorpointtypecodes;
            if (list.size() > 0) {
                paramMap.put("fkmonitorpointtypecode", list.get(0));
            }
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            //添加数据权限
            if (paramMap.get("fkmonitorpointtypecode") != null && Integer.valueOf(paramMap.get("fkmonitorpointtypecode").toString()) != CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode()) {
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                paramMap.put("datauserid", userid);
            }
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> effectiveTransmissionInfoByParamMap = effectiveTransmissionService.getOutPutEffectiveTransmissionInfoByParamMap(paramMap);

            Map<String, List<Map<String, Object>>> collect = effectiveTransmissionInfoByParamMap.stream().filter(m -> m.get("FK_MonitorPointID") != null && m.get("CountDate") != null).collect(Collectors.groupingBy
                    (m -> m.get("FK_MonitorPointID").toString() + "$" + m.get("CountDate").toString()));

            for (String FK_MonitorPointID : collect.keySet()) {
                List<Map<String, Object>> set = collect.get(FK_MonitorPointID);
                Optional<Map<String, Object>> first = set.stream().findFirst();
                if (first.isPresent()) {
                    //实传输数量
                    Double transmissionnumber = set.stream().map(m -> Double.valueOf(m.get("TransmissionNumber") == null ? "0d" : m.get("TransmissionNumber").toString())).collect(Collectors.summingDouble(m -> m));
                    //实有效数量
                    Double effectivenumber = set.stream().map(m -> Double.valueOf(m.get("EffectiveNumber") == null ? "0d" : m.get("EffectiveNumber").toString())).collect(Collectors.summingDouble(m -> m));
                    //应传输数量
                    Double shouldnumber = set.stream().map(m -> Double.valueOf(m.get("ShouldNumber") == null ? "0d" : m.get("ShouldNumber").toString())).collect(Collectors.summingDouble(m -> m));
                    //应有效数量
                    Double shouldeffectivenumber = set.stream().map(m -> Double.valueOf(m.get("ShouldEffectiveNumber") == null ? "0d" : m.get("ShouldEffectiveNumber").toString())).collect(Collectors.summingDouble(m -> m));
                    //传输率
                    double transmissionrate = shouldnumber == 0d ? 0d : transmissionnumber / shouldnumber;
                    //有效率
                    double effectiverate = shouldeffectivenumber == 0d ? 0d : effectivenumber / shouldeffectivenumber;
                    //传输有效率
                    double transmissioneffectiverate = transmissionrate * effectiverate;

                    Map<String, Object> map = first.get();
                    map.put("TransmissionNumber", transmissionnumber);
                    map.remove("pollutantname");
                    map.remove("FK_PollutantCode");
                    map.put("pollutionname", map.get("pollutionname") == null ? "" : map.get("pollutionname").toString());
                    map.put("EffectiveNumber", effectivenumber);
                    map.put("ShouldNumber", shouldnumber);
                    map.put("ShouldEffectiveNumber", shouldeffectivenumber);
                    map.put("TransmissionRate", decimalFormat.format(transmissionrate * 100) + "%");
                    map.put("EffectiveRate", decimalFormat.format(effectiverate * 100) + "%");
                    map.put("TransmissionEffectiveRate", decimalFormat.format(transmissioneffectiverate * 100) + "%");
                    resultList.add(map);
                }
            }
            resultList = resultList.stream().filter(m -> m.get("pollutionname") != null && m.get("outputname") != null && m.get("TransmissionEffectiveRate") != null && m.get("CountDate") != null)
                    .sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("TransmissionEffectiveRate").toString().substring(0, ((Map) m).get("TransmissionEffectiveRate").toString().length() - 1))).thenComparing(m -> ((Map) m).get("pollutionname").toString())
                            .thenComparing(m -> ((Map) m).get("outputname").toString()).thenComparing(m -> ((Map) m).get("CountDate").toString()).reversed()).collect(Collectors.toList());
            int total = resultList.size();
            if (pagenum != null && pagesize != null) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", total);
            resultMap.put("datalist", resultList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/30 0030 下午 5:10
     * @Description: 通过多参数重新计算传输有效率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkmonitorpointtypecodes, fkpollutionid, monitorpointid, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "supplyOutPutEffectiveTransmissionByParams", method = RequestMethod.POST)
    public Object supplyOutPutEffectiveTransmissionByParams(@RequestJson(value = "fkmonitorpointtypecodes") Object fkmonitorpointtypecodes,
                                                            @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.###");
            paramMap.put("fkmonitorpointtypecodes", fkmonitorpointtypecodes);
            paramMap.put("fk_pollutionid", fkpollutionid);
            paramMap.put("monitorpointid", monitorpointid);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);

            List<Map<String, Object>> outPutInfosByParamMap = monitorPointCommonService.getOutPutInfosByParamMap(paramMap);

            //由于有气象站点和其他类型的mn号会有重复，所以要通过类型和mn号确定为一个站点
            Map<String, List<Map<String, Object>>> collect1 = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null && m.get("DGIMN") != null).collect(Collectors.groupingBy(m -> m.get("DGIMN").toString() + "_" + m.get("FK_MonitorPointTypeCode").toString()));


            List<Integer> allMonitorPointType = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).map(m -> Integer.valueOf(m.get("FK_MonitorPointTypeCode").toString())).distinct().collect(Collectors.toList());


            List<String> mns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            String dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.joining(","));
            Map<String, String> dgimnAndPollutionid = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("FK_MonitorPointTypeCode") != null && m.get("PK_PollutionID") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString() + "_" + m.get("FK_MonitorPointTypeCode").toString(),
                    m -> m.get("PK_PollutionID").toString(), (a, b) -> a));
            Map<String, String> dgimnAndOutputid = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("FK_MonitorPointTypeCode") != null && m.get("outputid") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString() + "_" + m.get("FK_MonitorPointTypeCode").toString(),
                    m -> m.get("outputid").toString(), (a, b) -> a));

            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            paramMap.put("mns", mns);
            HourQueryDataVO hourDataVO = new HourQueryDataVO();
            hourDataVO.setDataGatherCode(dgimns);
            hourDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            List<HourQueryDataVO> listByParam = mongoBaseService.getListByParam(hourDataVO, hourcollection, "yyyy-MM-dd HH:mm:ss");


            Map<String, Object> pollutantMap = new HashMap<>();
            List<Map<String, Object>> monitorPollutantSetData = new ArrayList<>();
            for (Integer fkmonitorpointtypecode : allMonitorPointType) {
                pollutantMap.put("monitorpointtype", fkmonitorpointtypecode);
                monitorPollutantSetData.addAll(effectiveTransmissionService.getMonitorPollutantSetDataByParam(pollutantMap));
            }
            Map<String, Set<String>> dgimnAndPollutantMap = monitorPollutantSetData.stream().filter(m -> m.get("dgimn") != null && m.get("pollutants") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString()
                    , m -> (Set<String>) m.get("pollutants"), (a, b) -> a));
            Map<String, List<HourQueryDataVO>> collect = listByParam.stream().collect(Collectors.groupingBy(m -> m.getDataGatherCode() + "_" + FormatUtils.formatCSTString(m.getMonitorTime(), "yyyy-MM-dd")));

            List<EffectiveTransmissionVO> datalist = new ArrayList<>();


            List<String> allDays = JSONObjectUtil.getAllDays(starttime, endtime);

            for (String dgimnandtype : collect1.keySet()) {
                String[] split1 = dgimnandtype.split("_");
                if (split1.length > 1) {
                    String DGIMN = split1[0];
                    String type = split1[1];
                    Set<String> pollutants = dgimnAndPollutantMap.get(DGIMN) == null ? new HashSet<>() : dgimnAndPollutantMap.get(DGIMN);

                    for (String date : allDays) {
                        List<HourQueryDataVO> hourQueryDataVOS1 = collect.get(DGIMN + "_" + date);
                        //应传个数
                        long shouldNum = calculateShouldNum(date);
                        //如果当天有数据
                        if (hourQueryDataVOS1 != null && hourQueryDataVOS1.size() > 0) {
                            setEffectiveTransData(decimalFormat, username, dgimnAndPollutionid, dgimnAndOutputid, datalist, DGIMN, type, pollutants, shouldNum, hourQueryDataVOS1, date);
                        } else {
                            //如果当天没有数据则设置空数据
                            setEffectiveTransData(decimalFormat, username, dgimnAndPollutionid, dgimnAndOutputid, datalist, DGIMN, type, pollutants, shouldNum, new ArrayList<>(), date);
                        }
                    }
                }
            }
            effectiveTransmissionService.supplyOutPutEffectiveTransmissionByParams(paramMap, datalist);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void setEffectiveTransData(DecimalFormat decimalFormat, String username, Map<String, String> dgimnAndPollutionid, Map<String, String> dgimnAndOutputid, List<EffectiveTransmissionVO> datalist, String DGIMN, String type, Set<String> pollutants, long shouldNum, List<HourQueryDataVO> hourQueryDataVOS, String time) {
        for (String pollutant : pollutants) {
            EffectiveTransmissionVO effectiveTransmissionVO = new EffectiveTransmissionVO();
            //实传输数量
            long count = hourQueryDataVOS.stream().flatMap(m -> m.getHourDataList().stream().distinct()).filter(m -> m.get("PollutantCode") != null && pollutant.equals(m.get("PollutantCode").toString())).count();
            //实传数量不能大于应传数量
            if (count > shouldNum) {
                count = shouldNum;
            }
            //实有效数量
            long exceptioncount = hourQueryDataVOS.stream().flatMap(m -> m.getHourDataList().stream().distinct()).filter(m -> m.get("PollutantCode") != null && m.get("IsException") != null &&
                    pollutant.equals(m.get("PollutantCode").toString()) && Integer.valueOf(m.get("IsException").toString()) <= 0).count();
            if (exceptioncount > shouldNum) {
                exceptioncount = shouldNum;
            }
            effectiveTransmissionVO.setPkId(UUID.randomUUID().toString());
            effectiveTransmissionVO.setCountdate(DataFormatUtil.getDateYMD(time));
            effectiveTransmissionVO.setDgimn(DGIMN);
            effectiveTransmissionVO.setFkMonitorpointid(dgimnAndOutputid.get(DGIMN + "_" + type));
            effectiveTransmissionVO.setFkPollutionid(dgimnAndPollutionid.get(DGIMN + "_" + type));
            effectiveTransmissionVO.setFkMonitorpointtypecode(type);
            effectiveTransmissionVO.setUpdatetime(new Date());
            effectiveTransmissionVO.setUpdateuser(username);
            effectiveTransmissionVO.setFkPollutantcode(pollutant);

            effectiveTransmissionVO.setShouldnumber((int) shouldNum);//应传输数量
            effectiveTransmissionVO.setTransmissionnumber((int) count);//实传输数量
            effectiveTransmissionVO.setShouldeffectivenumber((int) shouldNum);//应有效数量
            effectiveTransmissionVO.setEffectivenumber((int) exceptioncount);//实有效数量
            Double aDouble = Double.valueOf(decimalFormat.format(Double.valueOf(count) / Double.valueOf(shouldNum)));//传输率
            Double bDouble = Double.valueOf(decimalFormat.format(Double.valueOf(exceptioncount) / Double.valueOf(shouldNum)));//有效率
            Double cDouble = Double.valueOf(decimalFormat.format((Double.valueOf(count) / Double.valueOf(shouldNum)) *
                    (Double.valueOf(exceptioncount) / Double.valueOf(shouldNum))));//传输有效率
            effectiveTransmissionVO.setTransmissionrate(aDouble);//传输率
            effectiveTransmissionVO.setEffectiverate(bDouble);//有效率
            effectiveTransmissionVO.setTransmissioneffectiverate(cDouble);//传输有效率
            datalist.add(effectiveTransmissionVO);
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/30 0030 下午 2:27
     * @Description: 计算当天应传个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [daytime]
     * @throws:
     */
    private static long calculateShouldNum(String daytime) {

        String starttime = JSONObjectUtil.getStartTime(daytime);
        Date now = new Date();
        if (DataFormatUtil.getDateYMD(daytime.substring(0, 10)).getTime() >= DataFormatUtil.getDateYMD(DataFormatUtil.getDateYMD(now)).getTime()) {
            String endTime = DataFormatUtil.getDateYMDHMS(now);
            long m = DataFormatUtil.getDateYMDHMS(endTime).getTime() - DataFormatUtil.getDateYMDHMS(starttime).getTime();
            return m / 1000 / 60 / 60;
        }
        return 24;
    }


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 下午 6:40
     * @Description: 导出企业及排口传输有效率信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, request, response]
     * @throws:
     */
    @RequestMapping(value = "ExportEffectiveTransmissionInfoByParamMap", method = RequestMethod.POST)
    public void ExportEffectiveTransmissionInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsjson, HttpServletRequest request, HttpServletResponse response) {
        try {
            List headers = new ArrayList<>();
            headers.add("企业名称/监测点名称");
            headers.add("统计时间");
            headers.add("应传数");
            headers.add("实传数");
            headers.add("应传有效数");
            headers.add("实传有效数");
            headers.add("传输率");
            headers.add("有效率");
            headers.add("传输有效率");

            List headersField = new ArrayList<>();
            headersField.add("pollutionname");
            headersField.add("CountDate");
            headersField.add("shouldnumber");
            headersField.add("transmissionnumber");
            headersField.add("shouldeffectivenumber");
            headersField.add("effectivenumber");
            headersField.add("transmissionrate");
            headersField.add("effectiverate");
            headersField.add("transmissioneffectiverate");

            JSONObject jsonObject = JSONObject.fromObject(getEffectiveTransmissionInfoByParamMap(paramsjson));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object data1 = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);


            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("传输有效率", response, request, bytesForWorkBook);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 下午 6:40
     * @Description: 导出企业下排口传输有效率详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, request, response]
     * @throws:
     */
    @RequestMapping(value = "ExportEffectiveTransmissionDetailByParamMap", method = RequestMethod.POST)
    public void ExportEffectiveTransmissionDetailByParamMap(@RequestJson(value = "paramsjson") Object paramsjson, HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> paramMap = (Map<String, Object>) paramsjson;
            String ishavepollution = paramMap.get("ishavepollution") == null ? "" : paramMap.get("ishavepollution").toString();

            List headers = new ArrayList<>();

            if ("1".equals(ishavepollution)) {
                headers.add("企业名称");
            }
            headers.add("监测点名称");
            headers.add("统计时间");
            headers.add("应传数");
            headers.add("实传数");
            headers.add("应传有效数");
            headers.add("实传有效数");
            headers.add("传输率(%)");
            headers.add("有效率(%)");
            headers.add("传输有效率(%)");

            List headersField = new ArrayList<>();
            if ("1".equals(ishavepollution)) {
                headersField.add("pollutionname");
            }
            headersField.add("outputname");
            headersField.add("countdate");
            headersField.add("shouldnumber");
            headersField.add("transmissionnumber");
            headersField.add("shouldeffectivenumber");
            headersField.add("effectivenumber");
            headersField.add("transmissionrate");
            headersField.add("effectiverate");
            headersField.add("transmissioneffectiverate");
            ((Map<String, Object>) paramsjson).put("isexport", "1");
            JSONObject jsonObject = JSONObject.fromObject(getEffectiveTransmissionDetailByParamMap(paramsjson));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object data1 = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);


            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("传输有效率详情", response, request, bytesForWorkBook);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: xsm
     * @date: 2022/01/24 下午 6:02
     * @Description: 根据监测点类型获取传输有效率(废气 、 烟气合并)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasEffectiveTransmissionByParamMap", method = RequestMethod.POST)
    public Object getGasEffectiveTransmissionByParamMap(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorPointTypes,
                                                        @RequestJson(value = "starttime", required = false) String starttime,
                                                        @RequestJson(value = "dgimn", required = false) String dgimn,
                                                        @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorPointTypes);
            paramMap.put("dgimn", dgimn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("userid", RedisTemplateUtil.getRedisCacheDataByToken("userid",String.class));
            List<Map<String, Object>> estData = effectiveTransmissionService.getGasEffectiveTransmissionByParamMap(paramMap);
            setEffectiveTransmissionMap(estData, resultMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取点位数据传输情况
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/14 14:53
     */
    @RequestMapping(value = "getPointTranCountDataByParam", method = RequestMethod.POST)
    public Object getPointTranCountDataByParam(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "monitortime") String monitortime
    ) throws ParseException {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String nowDay = DataFormatUtil.getDateYMD(new Date());
            String starttime = monitortime + " 00";
            String endtime = monitortime + " 23";
            if (monitortime.equals(nowDay)) {
                endtime = DataFormatUtil.getDateYMDH(new Date());
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            HourQueryDataVO hourDataVO = new HourQueryDataVO();
            hourDataVO.setDataGatherCode(dgimn);
            hourDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            List<HourQueryDataVO> hourQueryDataVOS = mongoBaseService.getListByParam(hourDataVO, hourcollection, "yyyy-MM-dd HH");
            Map<String,Object> timeAndIs = new HashMap<>();
            String hour;

            for (HourQueryDataVO hourQueryDataVO:hourQueryDataVOS){
                hour =DataFormatUtil.formatCST(hourQueryDataVO.getMonitorTime());
                if (hourQueryDataVO.getHourDataList()!=null&&hourQueryDataVO.getHourDataList().size()>0){
                    timeAndIs.put(DataFormatUtil.FormatDateOneToOther(hour,"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH"),"true");
                }
            }
            String end = monitortime + " 23";
            List<String> hours = DataFormatUtil.getYMDHBetween(starttime,end);
            hours.add(end);
            for (String hourIndex:hours){
                Map<String,Object> resultMap = new HashMap<>();
                if (DataFormatUtil.getDateYMDH(hourIndex).before(DataFormatUtil.getDateYMDH(endtime))||hourIndex.equals(endtime)){
                    resultMap.put("ishave",timeAndIs.get(hourIndex)!=null?timeAndIs.get(hourIndex):"false");
                }else {
                    resultMap.put("ishave","no");
                }
                hourIndex = DataFormatUtil.FormatDateOneToOther(hourIndex,"yyyy-MM-dd HH","H");
                resultMap.put("hour",hourIndex);
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}


