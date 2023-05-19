package com.tjpu.sp.controller.environmentalprotection.monitorstandard;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AlarmLevelDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.AlarmLevelService;
import com.tjpu.sp.service.environmentalprotection.monitorstandard.MonitorStandardService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年6月05日 上午 10:10
 * @Description:监测标准处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("monitorstandard")
public class MonitorStandardController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private MonitorStandardService monitorStandardService;

    @Autowired
    private AlarmLevelService alarmLevelService;

    @Autowired
    private FileController fileController;
    private String sysmodel = "monitorstandard";
    private String pk_id = "pk_standardid";
    private String listfieldtype = "list";

    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午 10:20
     * @Description: 获取监测标准初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @return:
     */
    @RequestMapping(value = "getMonitorStandardsListPage", method = RequestMethod.POST)
    public Object getMonitorStandardsListPage(HttpServletRequest request) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午 10:32
     * @Description: 根据自定义参数获取监测标准列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @return:
     */
    @RequestMapping(value = "getMonitorStandardsByParamMap", method = RequestMethod.POST)
    public Object getMonitorStandardsByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            paramMap.put("listfieldtype", listfieldtype);
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
     * @author: xsm
     * @date: 2019/06/04 0018 上午 10:32
     * @Description: 根据自定义参数获取监测标准列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @return:
     */
    @RequestMapping(value = "getAQMonitorStandardsByParamMap", method = RequestMethod.POST)
    public Object getAQMonitorStandardsByParamMap(
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "standardname", required = false) String standardname
    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            if (pagesize != null && pagenum != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(standardname)) {
                paramMap.put("standardname", standardname);
            }
            paramMap.put("standardtype", "4");
            List<Map<String, Object>> dataList = monitorStandardService.getMonitorStandardsByParamMap(paramMap);

            if (dataList.size() > 0) {
                List<String> fileIds = new ArrayList<>();
                String fileId;
                for (Map<String, Object> dataMap : dataList) {
                    if (dataMap.get("fileid") != null) {
                        fileIds.add(dataMap.get("fileid").toString());
                    }
                }
                if (fileIds.size() > 0) {
                    Map<String, List<Map<String, Object>>> idAndData = fileController.getFileIdAndData(fileIds, "13");
                    for (Map<String, Object> dataMap : dataList) {
                        if (dataMap.get("fileid") != null) {
                            fileId = dataMap.get("fileid").toString();
                            dataMap.put("filedatalist", idAndData.get(fileId));
                        }
                    }
                }
            }
            //获取分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            resultMap.put("total", pageInfo.getTotal());
            resultMap.put("datalist", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午9:18
     * @Description: 获取监测标准新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getMonitorStandardAddPage", method = RequestMethod.POST)
    public Object getMonitorStandardAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午9:20
     * @Description: 新增监测标准信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addMonitorStandard", method = RequestMethod.POST)
    public Object addMonitorStandard(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午9:21
     * @Description: 根据主键ID获取监测标准信息修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getMonitorStandardUpdatePage", method = RequestMethod.POST)
    public Object getMonitorStandardUpdatePage(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午9:26
     * @Description: 修改监测标准信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateMonitorStandard", method = RequestMethod.POST)
    public Object updateMonitorStandard(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午9:30
     * @Description: 根据监测标准信息主键ID删除单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteMonitorStandardByID", method = RequestMethod.POST)
    public Object deleteMonitorStandardByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/06/04 0018 上午9:37
     * @Description: 根据监测标准信息主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getMonitorStandardDetailByID", method = RequestMethod.POST)
    public Object getMonitorStandardDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/09/05 0005 下午 2:50
     * @Description: 根据自定义参数获取监测标准列表数据（app）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorStandardInfosByParamMap", method = RequestMethod.POST)
    public Object getMonitorStandardInfosByParamMap(@RequestJson(value = "standardname", required = false) String standardname,
                                                    @RequestJson(value = "standardtypes", required = false) List<String> standardtypes,
                                                    @RequestJson(value = "keywords", required = false) String keywords,
                                                    @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                    @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("standardname", standardname);
            paramMap.put("standardtypes", standardtypes);
            paramMap.put("keywords", keywords);
            List<Map<String, Object>> resultList = monitorStandardService.getMonitorStandardListsByParamMap(paramMap);
            if (pagesize != null && pagenum != null) {
                List<Map<String, Object>> dataList = getPageData(resultList, pagenum, pagesize);
                resultMap.put("total", resultList.size());
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", resultList);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/09/05 0005 下午 3:08
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            resultlist = dataList.subList(pageStart, pageEnd);
        }
        return resultlist;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/10 0010 上午 9:23
     * @Description: 通过标准类型获取标准信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getStandardByStandardType", method = RequestMethod.POST)
    public Object getStandardByStandardType(@RequestJson(value = "standardtype", required = false) String standardtype) throws Exception {
        try {
            List<Map<String, Object>> allStandard = new ArrayList<>();
            allStandard = monitorStandardService.getAllStandard();
            if (standardtype != null) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (int i = 0; i < allStandard.size(); i++) {
                    Map<String, Object> map = allStandard.get(i);
                    if (map.get("StandardType") != null) {
                        String standardType = map.get("StandardType").toString();
                        String[] split = standardType.split(",");
                        if (Arrays.asList(split).contains(standardtype)) {
                            resultList.add(map);
                        }
                    }
                }
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", allStandard);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取点位标准信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/26 15:06
     */
    @RequestMapping(value = "getPointStandardDataList", method = RequestMethod.POST)
    public Object getPointStandardDataList(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pointid", required = false) String pointid
    ) {
        try {
            Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
            List<PollutantSetDataVO> pollutantSetDataVOList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            if (StringUtils.isNotBlank(pointid)) {
                paramMap.put("monitorpointid", pointid);
            }
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                case RainEnum:
                    pollutantSetDataVOList = monitorStandardService.getWaterStandardList(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                case MicroStationEnum:
                case EnvironmentalVocEnum:
                case meteoEnum:
                    pollutantSetDataVOList = monitorStandardService.getOtherStandardList(paramMap);
                    break;
                case WaterQualityEnum:
                    pollutantSetDataVOList = monitorStandardService.getWQStandardList(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                case WasteGasEnum:
                case SmokeEnum:
                    pollutantSetDataVOList = monitorStandardService.getGasStandardList(paramMap);
                    break;
                default:
                    pollutantSetDataVOList = monitorStandardService.getOtherStandardList(paramMap);
            }
            if (pollutantSetDataVOList.size() > 0) {

                List<Map<String, Object>> standMapList;
                List<AlarmLevelDataVO> alarmLevelDataVOS;
                String monitorpointid;
                String standardvalue;
                for (PollutantSetDataVO pollutantSetDataVO : pollutantSetDataVOList) {
                    monitorpointid = pollutantSetDataVO.getMonitorpointid();
                    if (resultMap.containsKey(monitorpointid)) {
                        standMapList = resultMap.get(monitorpointid);
                    } else {
                        standMapList = new ArrayList<>();
                    }
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutantcode", pollutantSetDataVO.getPollutantcode());
                    dataMap.put("pollutantname", pollutantSetDataVO.getPollutantname());
                    dataMap.put("standardname", pollutantSetDataVO.getStandardname());
                    standardvalue = "";
                    if (pollutantSetDataVO.getStandardmaxvalue() != null || pollutantSetDataVO.getStandardminvalue() != null) {
                        if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {
                            standardvalue = "、标准值：" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardmaxvalue() + "");
                        } else if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())
                        ) {
                            standardvalue = "、标准值：" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardminvalue() + "");
                        } else {
                            standardvalue = "、标准值：" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardminvalue() + "")
                                    + "-" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardmaxvalue() + "");
                        }
                    }
                    if (pollutantSetDataVO.getAlarmLevelDataVOList().size() > 0) {
                        alarmLevelDataVOS = pollutantSetDataVO.getAlarmLevelDataVOList();
                        for (AlarmLevelDataVO alarmLevelDataVO : alarmLevelDataVOS) {
                            standardvalue += "、" + alarmLevelDataVO.getLevelname() + "：";
                            if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {
                                standardvalue += DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardmaxvalue() + "");
                            } else if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())
                            ) {
                                standardvalue += DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardminvalue() + "");
                            } else {
                                standardvalue += DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardminvalue() + "")
                                        + "-" + DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardmaxvalue() + "");
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(standardvalue)) {
                        standardvalue = standardvalue.substring(1, standardvalue.length());
                        dataMap.put("standardvalue", standardvalue);
                        dataMap.put("orderindex", pollutantSetDataVO.getOrderindex() != null ? pollutantSetDataVO.getOrderindex() : -9999);
                        standMapList.add(dataMap);
                    } else {
                        dataMap.put("standardvalue", "-");
                        dataMap.put("orderindex", pollutantSetDataVO.getOrderindex() != null ? pollutantSetDataVO.getOrderindex() : -9999);
                        standMapList.add(dataMap);
                    }

                    //排序
                    standMapList = standMapList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                    resultMap.put(monitorpointid, standMapList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取点位标准信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/26 15:06
     */
    @RequestMapping(value = "getPointStandardDataListByParam", method = RequestMethod.POST)
    public Object getPointStandardDataListByParam(
            @RequestJson(value = "alarmlevel") Integer alarmlevel,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pointid", required = false) String pointid
    ) {
        try {
            Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();


            List<PollutantSetDataVO> pollutantSetDataVOList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            if (StringUtils.isNotBlank(pointid)) {
                paramMap.put("monitorpointid", pointid);
            }
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                case RainEnum:
                    pollutantSetDataVOList = monitorStandardService.getWaterStandardList(paramMap);
                    break;
                case EnvironmentalStinkEnum:
                case MicroStationEnum:
                case EnvironmentalVocEnum:
                case meteoEnum:
                    pollutantSetDataVOList = monitorStandardService.getOtherStandardList(paramMap);
                    break;
                case WaterQualityEnum:
                    pollutantSetDataVOList = monitorStandardService.getWQStandardList(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                case WasteGasEnum:
                case SmokeEnum:
                    pollutantSetDataVOList = monitorStandardService.getGasStandardList(paramMap);
                    break;
                default:
                    pollutantSetDataVOList = monitorStandardService.getOtherStandardList(paramMap);

            }
            if (pollutantSetDataVOList.size() > 0) {

                List<Map<String, Object>> standMapList;
                List<AlarmLevelDataVO> alarmLevelDataVOS;
                String monitorpointid;
                String standardvalue;
                for (PollutantSetDataVO pollutantSetDataVO : pollutantSetDataVOList) {
                    monitorpointid = pollutantSetDataVO.getMonitorpointid();
                    if (resultMap.containsKey(monitorpointid)) {
                        standMapList = resultMap.get(monitorpointid);
                    } else {
                        standMapList = new ArrayList<>();
                    }
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutantcode", pollutantSetDataVO.getPollutantcode());
                    dataMap.put("pollutantname", pollutantSetDataVO.getPollutantname());
                    standardvalue = "";
                    if (alarmlevel == 4) {
                        if (pollutantSetDataVO.getStandardmaxvalue() != null || pollutantSetDataVO.getStandardminvalue() != null) {
                            if(pollutantSetDataVO.getAlarmtype()!=null) {
                                if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {
                                    standardvalue = DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardmaxvalue() + "");
                                } else if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())
                                        ) {
                                    standardvalue = DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardminvalue() + "");
                                } else {
                                    standardvalue = DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardminvalue() + "")
                                            + "-" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardmaxvalue() + "");
                                }
                            }
                        }
                    } else {
                        if (pollutantSetDataVO.getAlarmLevelDataVOList().size() > 0) {
                            alarmLevelDataVOS = pollutantSetDataVO.getAlarmLevelDataVOList();
                            for (AlarmLevelDataVO alarmLevelDataVO : alarmLevelDataVOS) {
                                if (alarmlevel == alarmLevelDataVO.getLevelcode()) {
                                    if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {
                                        standardvalue = DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardmaxvalue() + "");
                                    } else if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())
                                    ) {
                                        standardvalue = DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardminvalue() + "");
                                    } else {
                                        standardvalue = DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardminvalue() + "")
                                                + "-" + DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardmaxvalue() + "");
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(standardvalue)) {
                        dataMap.put("standardvalue", standardvalue);
                        dataMap.put("orderindex", pollutantSetDataVO.getOrderindex() != null ? pollutantSetDataVO.getOrderindex() : -9999);
                        standMapList.add(dataMap);
                    } else {
                        dataMap.put("standardvalue", "-");
                        dataMap.put("orderindex", pollutantSetDataVO.getOrderindex() != null ? pollutantSetDataVO.getOrderindex() : -9999);
                        standMapList.add(dataMap);
                    }

                    //排序
                    standMapList = standMapList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());


                    resultMap.put(monitorpointid, standMapList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap.get(pointid));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取废气、烟气点位标准信息
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/01/25 8:41
     */
    @RequestMapping(value = "getGasPointStandardDataList", method = RequestMethod.POST)
    public Object getGasPointStandardDataList(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
            List<PollutantSetDataVO> pollutantSetDataVOList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            pollutantSetDataVOList = monitorStandardService.getGasPointStandardDataList(paramMap);
            if (pollutantSetDataVOList.size() > 0) {
                List<Map<String, Object>> standMapList;
                List<AlarmLevelDataVO> alarmLevelDataVOS;
                String monitorpointid;
                String standardvalue;
                for (PollutantSetDataVO pollutantSetDataVO : pollutantSetDataVOList) {
                    monitorpointid = pollutantSetDataVO.getMonitorpointid();
                    if (resultMap.containsKey(monitorpointid)) {
                        standMapList = resultMap.get(monitorpointid);
                    } else {
                        standMapList = new ArrayList<>();
                    }
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutantcode", pollutantSetDataVO.getPollutantcode());
                    dataMap.put("pollutantname", pollutantSetDataVO.getPollutantname());
                    dataMap.put("standardname", pollutantSetDataVO.getStandardname());
                    standardvalue = "";
                    if (pollutantSetDataVO.getStandardmaxvalue() != null || pollutantSetDataVO.getStandardminvalue() != null) {
                        if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {
                            standardvalue = "、标准值：" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardmaxvalue() + "");
                        } else if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())
                        ) {
                            standardvalue = "、标准值：" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardminvalue() + "");
                        } else {
                            standardvalue = "、标准值：" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardminvalue() + "")
                                    + "-" + DataFormatUtil.subZeroAndDot(pollutantSetDataVO.getStandardmaxvalue() + "");
                        }
                    }
                    if (pollutantSetDataVO.getAlarmLevelDataVOList().size() > 0) {
                        alarmLevelDataVOS = pollutantSetDataVO.getAlarmLevelDataVOList();
                        for (AlarmLevelDataVO alarmLevelDataVO : alarmLevelDataVOS) {
                            standardvalue += "、" + alarmLevelDataVO.getLevelname() + "：";
                            if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode())) {
                                standardvalue += DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardmaxvalue() + "");
                            } else if (pollutantSetDataVO.getAlarmtype() == Integer.parseInt(CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode())
                            ) {
                                standardvalue += DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardminvalue() + "");
                            } else {
                                standardvalue += DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardminvalue() + "")
                                        + "-" + DataFormatUtil.subZeroAndDot(alarmLevelDataVO.getStandardmaxvalue() + "");
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(standardvalue)) {
                        standardvalue = standardvalue.substring(1, standardvalue.length());
                        dataMap.put("standardvalue", standardvalue);
                        dataMap.put("orderindex", pollutantSetDataVO.getOrderindex() != null ? pollutantSetDataVO.getOrderindex() : -9999);
                        standMapList.add(dataMap);
                    }

                    //排序
                    standMapList = standMapList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                    resultMap.put(monitorpointid, standMapList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取报警等级数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/23 15:02
     */
    @RequestMapping(value = "getAlarmLevelData", method = RequestMethod.POST)
    public Object getAlarmLevelData() {
        try {
            List<Map<String, Object>> dataList = alarmLevelService.getAlarmLevelPubCodeInfo();
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> standMap = new HashMap<>();
            standMap.put("levelcode", 4);
            standMap.put("levelname", "标准");
            standMap.put("orderindex", 0);
            resultList.add(standMap);

            for (Map<String, Object> dataMap : dataList) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("levelcode", dataMap.get("Code"));
                resultMap.put("levelname", dataMap.get("Name"));
                resultMap.put("orderindex", dataMap.get("OrderIndex"));
                resultList.add(resultMap);
            }
            //排序
            resultList = resultList.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString())
                    )).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
