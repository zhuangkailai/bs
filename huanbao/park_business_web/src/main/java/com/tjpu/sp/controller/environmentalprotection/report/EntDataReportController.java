package com.tjpu.sp.controller.environmentalprotection.report;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.environmentalprotection.tracesource.TraceSourceResultController;
import com.tjpu.sp.service.environmentalprotection.report.EntDataReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年7月30日 下午 7:18
 * @Description:企业数据报表管理处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("entDataReport")
public class EntDataReportController {

    @Autowired
    private EntDataReportService entDataReportService;
    @Autowired
    private TraceSourceResultController traceSourceResultController;


    /**
     * @author: xsm
     * @date: 2019/7/30 0030 下午 7:23
     * @Description:根据报表类型和自定义参数获取某个企业的企业报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getEntDataReportByParamMap", method = RequestMethod.POST)
    public Object getEntDataReportByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                             @RequestJson(value = "pointtype") String pointtype,
                                             @RequestJson(value = "monitortime", required = false) String monitortime,
                                             @RequestJson(value = "starttime", required = false) String starttime,
                                             @RequestJson(value = "endtime", required = false) String endtime,
                                             @RequestJson(value = "id") String id,
                                             @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                             @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                             @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtype", pointtype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("id", id);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("tabletitletype", "1");//自定义表头类型，1为查询列表的表头
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            Map<String, Object> result = entDataReportService.getEntDataReportByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/30 0030 下午 7:23
     * @Description:根据报表类型和自定义参数获取企业汇总报表
     * @updateUser:xsm
     * @updateDate:2020/12/14 下午 3:36
     * @updateDescription: 环境恶臭和厂界恶臭类型合并为恶臭
     * @param: []
     */
    @RequestMapping(value = "getSummaryEntDataReportByParamMap", method = RequestMethod.POST)
    public Object getSummaryEntDataReportByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                                    @RequestJson(value = "pointtype", required = false) String pointtype,
                                                    @RequestJson(value = "outputpropertys", required = false) List<String> outputpropertys,
                                                    @RequestJson(value = "isstink", required = false) Boolean isstink,
                                                    @RequestJson(value = "monitortime", required = false) String monitortime,
                                                    @RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime,
                                                    @RequestJson(value = "pkidlist", required = false) List<String> pollutionids,
                                                    @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                                    @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes
    ) {

        try {
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtype", pointtype);
            if(isstink!=null&&isstink==true){
                paramMap.put("isstink", isstink);
            }else{
                paramMap.put("isstink", false);
            }
            paramMap.put("outputpropertys", outputpropertys);
            paramMap.put("tabletitletype", "1");//自定义表头类型，1为查询列表的表头
            if (pollutionids != null && pollutionids.size() > 0) {
                paramMap.put("pkids", pollutionids);
            } else {//查所有
                if(isstink!=null&&isstink==true){
                    List<Map<String, Object>> result = new ArrayList<>();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                    if (result != null && result.size() > 0) {
                            for (Map<String, Object> map : result) {
                                pollutionids.add(map.get("id").toString());
                            }
                    }
                }else{
                    paramMap.put("pointtype", pointtype);
                    List<Map<String, Object>> result = entDataReportService.getSelectPollutionInfoByPointType(paramMap);
                    if (result != null && result.size() > 0) {
                        for (Map<String, Object> map : result) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }
                paramMap.put("pkids", pollutionids);
            }
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            if (pollutionids != null && pollutionids.size() > 0) {
                if(isstink!=null&&isstink==true) {
                    resultmap = entDataReportService.getStinkSummaryEntDataReportByParamMap(paramMap);
                }else{
                    resultmap = entDataReportService.getSummaryEntDataReportByParamMap(paramMap);
                }
            } else {
                resultmap.put("tabletitledata", new ArrayList<>());
                resultmap.put("selectpollutants", new ArrayList<>());
                resultmap.put("tablelistdata", new ArrayList<>());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/1 0001 下午 3:12
     * @Description:根据类型获取关联该类型在线排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getSelectPollutionInfoByPointtype", method = RequestMethod.POST)
    public Object getSelectPollutionInfoByPointType(@RequestJson(value = "pointtype", required = false) String pointtype
                                                  ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("pointtype", pointtype);
            result = entDataReportService.getSelectPollutionInfoByPointType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


/**
 * @author: xsm
 * @date: 2019/8/1 0001 下午 3:12
 * @Description:根据类型获取关联该类型在线排口的企业信息（弹窗列表）
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param: []
 */
    @RequestMapping(value = "getSelectEntOrPointDataByParam", method = RequestMethod.POST)
    public Object getSelectEntOrPointDataByParam(@RequestJson(value = "pointtype", required = false) String pointtype,
                                                 @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes,
                                                 @RequestJson(value = "customname", required = false) String customname,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            if (customname!=null&&!"".equals(customname)){
                paramMap.put("customname", customname);
            }
            if (pointtypes==null||pointtypes.size()==0){
                pointtypes = new ArrayList<>();
                if (pointtype!=null) {
                    pointtypes.add(Integer.valueOf(pointtype));
                }
            }
            for (Integer i:pointtypes){
                paramMap.put("pointtype", i);
                resultlist.addAll(entDataReportService.getSelectEntOrPointDataByParam(paramMap));
            }
            if (resultlist.size() > 0) {
                //去重
                List<String> ids = new ArrayList<>();
                String id;
                for (Map<String, Object> map:resultlist){
                    id = map.get("pollutionid")!=null?map.get("pollutionid").toString():(map.get("monitorpointid")!=null?map.get("monitorpointid").toString():"");
                    if (!"".equals(id)&&!ids.contains(id)){
                        result.add(map);
                        ids.add(id);
                    }
                }
                if (pagesize != null && pagenum != null) {
                    List<Map<String, Object>> dataList = getPageData(result, pagenum, pagesize);
                    resultMap.put("total", result.size());
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", result);
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
     * @date: 2020/3/23 0023 上午 9:50
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
     * @author: xsm
     * @date: 2020/12/16 0016 下午 17:35
     * @Description: 获取所有恶臭点位信息（带数据权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllStenchPointInfoByDataAuthor", method = RequestMethod.POST)
    public Object getAllStenchPointInfoByDataAuthor() {
        try{
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",String.class);
            Map<String, Object> Map = new HashMap<>();
            Map.put("userid", userId);
            Map.put("enttypecode", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
            Map.put("othertypecode", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            List<Map<String, Object>> result = entDataReportService.getAllStenchPointInfoByDataAuthor(Map);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/1 0001 下午 3:23
     * @Description:根据类型获取该类型的监测污染物（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getSelectPollutantInfoByPointtype", method = RequestMethod.POST)
    public Object getSelectPollutantInfoByPointtype(@RequestJson(value = "pointtype", required = false) String pointtype,
                                                    @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            if (pointtype!=null){
                paramMap.put("pointtype", pointtype);
                result = entDataReportService.getSelectPollutantInfoByPointtype(paramMap);
            }else{
                List<Map<String, Object>> resulttwo = new ArrayList<>();
                if(pointtypes!=null&&pointtypes.size()>0){
                    for (Integer type:pointtypes) {
                        paramMap.put("pointtype", type);
                        resulttwo.addAll(entDataReportService.getSelectPollutantInfoByPointtype(paramMap));
                    }
                }
                if (resulttwo.size()>0){
                    Set set = new HashSet();
                    for (Map<String, Object> map:resulttwo){
                        if (map.get("Code")!=null){
                            if (!set.contains(map.get("Code").toString())){
                                result.add(map);
                                set.add(map.get("Code").toString());
                            }
                        }

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
     * @date: 2019/7/17 0017 下午 6:47
     * @Description:导出-企业报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportEntDataReport", method = RequestMethod.POST)
    public void exportEntDataReport(@RequestJson(value = "reporttype") String reporttype,
                                    @RequestJson(value = "pointtype") String pointtype,
                                    @RequestJson(value = "monitortime", required = false) String monitortime,
                                    @RequestJson(value = "starttime", required = false) String starttime,
                                    @RequestJson(value = "endtime", required = false) String endtime,
                                    @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                    @RequestJson(value = "id") String id,
                                    @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                    @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                    @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                     HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtype", pointtype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("id", id);
            paramMap.put("monitorpointid", monitorpointid);
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutionname != null) {
                paramMap.put("pollutionname", pollutionname);
            } else {
                paramMap.put("pollutionname", "");
            }
            paramMap.put("tabletitletype", "2");//自定义表头类型，2为导出列表的表头
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            Map<String, Object> result = entDataReportService.getEntDataReportByParamMap(paramMap);
            List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) result.get("tabletitledata");
            List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) result.get("tablelistdata");
            String titlename = result.get("titlename").toString();
            String name = "";
            if ("day".equals(reporttype)) {
                name = "日报";
            } else if ("week".equals(reporttype)) {
                name = "周报";
            } else if ("month".equals(reporttype)) {
                name = "月报";
            } else if ("year".equals(reporttype)) {
                name = "年报";
            }
            String reportname = "";
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                reportname = "企业报表";
            } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {
                reportname = "企业储罐报表";
            } else {
                reportname = "监测点报表";
            }
            //设置文件名称
            String fileName = reportname + "_" + name + "_" + new Date().getTime();
            //if ("custom".equals(reporttype)) {//自定义
                ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);
            /*} else {
                ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", tabletitledata, tablelistdata, "");
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 6:47
     * @Description:导出-汇总报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportSummaryEntDataReport", method = RequestMethod.POST)
    public void exportSummaryEntDataReport(@RequestJson(value = "reporttype") String reporttype,
                                           @RequestJson(value = "pointtype", required = false) String pointtype,
                                           @RequestJson(value = "outputpropertys", required = false) List<String> outputpropertys,
                                           @RequestJson(value = "isstink", required = false) Boolean isstink,
                                           @RequestJson(value = "monitortime", required = false) String monitortime,
                                           @RequestJson(value = "starttime", required = false) String starttime,
                                           @RequestJson(value = "endtime", required = false) String endtime,
                                           @RequestJson(value = "pkidlist", required = false) List<String> pollutionids,
                                           @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                           @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtype", pointtype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("outputpropertys", outputpropertys);
            if(isstink!=null&&isstink==true){
                paramMap.put("isstink", isstink);
            }else{
                paramMap.put("isstink", false);
            }
            if (pollutionids != null && pollutionids.size() > 0) {
                paramMap.put("pkids", pollutionids);
            } else {//查所有
                if(isstink!=null&&isstink==true){
                    List<Map<String, Object>> result = new ArrayList<>();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                    if (result != null && result.size() > 0) {
                        for (Map<String, Object> map : result) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }else{
                    paramMap.put("pointtype", pointtype);
                    List<Map<String, Object>> result = entDataReportService.getSelectPollutionInfoByPointType(paramMap);
                    if (result != null && result.size() > 0) {
                        for (Map<String, Object> map : result) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }
                paramMap.put("pkids", pollutionids);
            }
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            paramMap.put("tabletitletype", "2");//自定义表头类型，2为导出列表的表头
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            Map<String, Object> result = new HashMap<>();
            //获取企业数据报表
                if(isstink!=null&&isstink==true) {
                    result = entDataReportService.getStinkSummaryEntDataReportByParamMap(paramMap);
                }else{
                    result = entDataReportService.getSummaryEntDataReportByParamMap(paramMap);
                }
            List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) result.get("tabletitledata");
            List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) result.get("tablelistdata");
            String titlename = result.get("titlename").toString();
            String name = "";
            if ("day".equals(reporttype)) {
                name = "日报";
            } else if ("week".equals(reporttype)) {
                name = "周报";
            } else if ("month".equals(reporttype)) {
                name = "月报";
            } else if ("year".equals(reporttype)) {
                name = "年报";
            }
            //设置文件名称
            String fileName = "汇总报表_" + name + "_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);
            //ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", tabletitledata, tablelistdata, "");
           // ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getEntSummaryReportDataByParamMap", method = RequestMethod.POST)
    public Object getEntSummaryReportDataByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                                    @RequestJson(value = "pointtype", required = false) String pointtype,
                                                    @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes,
                                                    @RequestJson(value = "outputpropertys", required = false) List<String> outputpropertys,
                                                    @RequestJson(value = "monitortime", required = false) String monitortime,
                                                    @RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime,
                                                    @RequestJson(value = "pkidlist", required = false) List<String> pollutionids,
                                                    @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                                    @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                                    @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {

        try {
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("isstink", false);
            paramMap.put("outputpropertys", outputpropertys);
            paramMap.put("tabletitletype", "1");//自定义表头类型，1为查询列表的表头
            if (pointtypes==null||pointtypes.size()==0){
                pointtypes = new ArrayList<>();
                if (pointtype!=null){
                    pointtypes.add(Integer.valueOf(pointtype));
                }
            }
            if (pollutionids != null && pollutionids.size() > 0) {
                paramMap.put("pkids", pollutionids);
            } else {//查所有
                List<Map<String, Object>> result = new ArrayList<>();
                for (Integer i:pointtypes) {
                    paramMap.put("pointtype", i);
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                }
                if (result != null && result.size() > 0) {
                    for (Map<String, Object> map : result) {
                        if (map.get("id")!=null&&!pollutionids.contains(map.get("id").toString().toString())) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }
                paramMap.put("pkids", pollutionids);
            }
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            if (pollutionids != null && pollutionids.size() > 0) {
                if (pointtypes.size()>1){//类型大于1 则为废气、烟气合并
                    paramMap.put("pointtypes",pointtypes);
                    resultmap = entDataReportService.getGasEntSummaryReportDataByParamMap(paramMap);
                }else {
                    paramMap.put("pointtype",pointtypes.get(0));
                    resultmap = entDataReportService.getEntSummaryReportDataByParamMap(paramMap);
                }
            } else {
                resultmap.put("tabletitledata", new ArrayList<>());
                resultmap.put("selectpollutants", new ArrayList<>());
                resultmap.put("tablelistdata", new ArrayList<>());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 6:47
     * @Description:导出-汇总报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportEntSummaryReportDataByParamMap", method = RequestMethod.POST)
    public void exportEntSummaryReportDataByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                           @RequestJson(value = "pointtype", required = false) String pointtype,
                                            @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes,
                                            @RequestJson(value = "outputpropertys", required = false) List<String> outputpropertys,
                                           @RequestJson(value = "monitortime", required = false) String monitortime,
                                           @RequestJson(value = "starttime", required = false) String starttime,
                                           @RequestJson(value = "endtime", required = false) String endtime,
                                           @RequestJson(value = "pkidlist", required = false) List<String> pollutionids,
                                           @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                           @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                           HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("outputpropertys", outputpropertys);
            if (pointtypes==null||pointtypes.size()==0){
                pointtypes = new ArrayList<>();
                if (pointtype!=null){
                    pointtypes.add(Integer.valueOf(pointtype));
                }
            }
            if (pollutionids != null && pollutionids.size() > 0) {
                paramMap.put("pkids", pollutionids);
            } else {//查所有
                List<Map<String, Object>> result = new ArrayList<>();
                for (Integer i:pointtypes) {
                    paramMap.put("pointtype", i);
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                }
                if (result != null && result.size() > 0) {
                    for (Map<String, Object> map : result) {
                        if (map.get("id")!=null&&!pollutionids.contains(map.get("id").toString().toString())) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }
                paramMap.put("pkids", pollutionids);
            }
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            paramMap.put("tabletitletype", "2");//自定义表头类型，2为导出列表的表头
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            Map<String, Object> result = new HashMap<>();
            if (pointtypes.size()>1){//类型大于1 则为废气、烟气合并
                paramMap.put("pointtypes",pointtypes);
                result = entDataReportService.getGasEntSummaryReportDataByParamMap(paramMap);
            }else {
                paramMap.put("pointtype",pointtypes.get(0));
                result = entDataReportService.getEntSummaryReportDataByParamMap(paramMap);
            }
            List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) result.get("tabletitledata");
            List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) result.get("tablelistdata");
            String titlename = result.get("titlename").toString();
            String name = "";
            if ("hours".equals(reporttype)) {
                name = "小时报";
            } else if ("days".equals(reporttype)) {
                name = "日报";
            }
            //设置文件名称
            String fileName = "汇总报表_" + name + "_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getEntReportTimeSlotDataByParamMap", method = RequestMethod.POST)
    public Object getEntReportTimeSlotDataByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                                     @RequestJson(value = "pointtype") String pointtype,
                                                     @RequestJson(value = "starttime", required = false) String starttime,
                                                     @RequestJson(value = "endtime", required = false) String endtime,
                                                     @RequestJson(value = "id") String id,
                                                     @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                     @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                                     @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtype", pointtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("id", id);//企业ID
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("tabletitletype", "1");//自定义表头类型，1为查询列表的表头
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            Map<String, Object> result = entDataReportService.getEntReportTimeSlotDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/7/27 0027 下午 15:35
     * @Description:导出企业报表(小时时段、日时段)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "exportEntReportTimeSlotDataByParamMap", method = RequestMethod.POST)
    public void exportEntReportTimeSlotDataByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                                        @RequestJson(value = "pointtype") String pointtype,
                                                        @RequestJson(value = "starttime", required = false) String starttime,
                                                        @RequestJson(value = "endtime", required = false) String endtime,
                                                        @RequestJson(value = "id") String id,
                                                        @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                        @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                        @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                                        @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                                        HttpServletRequest request, HttpServletResponse response) throws Exception{
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtype", pointtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("id", id);
            paramMap.put("monitorpointid", monitorpointid);
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutionname != null) {
                paramMap.put("pollutionname", pollutionname);
            } else {
                paramMap.put("pollutionname", "");
            }
            paramMap.put("tabletitletype", "2");//自定义表头类型，2为导出列表的表头
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            Map<String, Object> result = entDataReportService.getEntReportTimeSlotDataByParamMap(paramMap);
            List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) result.get("tabletitledata");
            List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) result.get("tablelistdata");
            String titlename = result.get("titlename").toString();
            String name = "";
            if ("hours".equals(reporttype)) {
                name = "小时时段报";
            } else if ("days".equals(reporttype)) {
                name = "日时段报";
            }
            String reportname = "";
            if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "") || pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {//废气,废水，雨水
                reportname = "企业报表";
            } else if (pointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "")) {
                reportname = "企业储罐报表";
            } else {
                reportname = "监测点报表";
            }
            //设置文件名称
            String fileName = reportname + "_" + name + "_" + new Date().getTime();

            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
    @RequestMapping(value = "getGasSelectPollutionInfoByPointtypes", method = RequestMethod.POST)
    public Object getGasSelectPollutionInfoByPointtypes(@RequestJson(value = "pointtypes", required = false) List<String> pointtypes
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("pointtypes", pointtypes);
            result = entDataReportService.getGasSelectPollutionInfoByPointtype(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/08 0008 上午 11:20
     * @Description:根据报表类型和自定义参数获取废气企业汇总报表（废气、烟气合并）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getSummaryGasEntDataReportByParamMap", method = RequestMethod.POST)
    public Object getSummaryGasEntDataReportByParamMap(@RequestJson(value = "reporttype") String reporttype,
                                                    @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes,
                                                    @RequestJson(value = "outputpropertys", required = false) List<String> outputpropertys,
                                                    @RequestJson(value = "monitortime", required = false) String monitortime,
                                                    @RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime,
                                                    @RequestJson(value = "pkidlist", required = false) List<String> pollutionids,
                                                    @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                                    @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes
    ) throws ParseException {

        try {
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtypes", pointtypes);
            paramMap.put("outputpropertys", outputpropertys);
            paramMap.put("tabletitletype", "1");//自定义表头类型，1为查询列表的表头
            if (pollutionids != null && pollutionids.size() > 0) {
                paramMap.put("pkids", pollutionids);
            } else {//查所有
                List<Map<String, Object>> result = new ArrayList<>();
                for (Integer pointtype:pointtypes) {
                    paramMap.put("pointtype", pointtype);
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                }
                if (result != null && result.size() > 0) {
                    for (Map<String, Object> map : result) {
                        if (map.get("id")!=null&&!pollutionids.contains(map.get("id").toString().toString())) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }
                paramMap.put("pkids", pollutionids);
            }
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            //获取企业数据报表
            if (pollutionids != null && pollutionids.size() > 0) {
                    resultmap = entDataReportService.getGasSummaryEntDataReportByParamMap(paramMap);
            } else {
                resultmap.put("tabletitledata", new ArrayList<>());
                resultmap.put("selectpollutants", new ArrayList<>());
                resultmap.put("tablelistdata", new ArrayList<>());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 6:47
     * @Description:导出-汇总报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportSummaryGasEntDataReport", method = RequestMethod.POST)
    public void exportSummaryGasEntDataReport(@RequestJson(value = "reporttype") String reporttype,
                                              @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes,
                                           @RequestJson(value = "outputpropertys", required = false) List<String> outputpropertys,
                                           @RequestJson(value = "monitortime", required = false) String monitortime,
                                           @RequestJson(value = "starttime", required = false) String starttime,
                                           @RequestJson(value = "endtime", required = false) String endtime,
                                           @RequestJson(value = "pkidlist", required = false) List<String> pollutionids,
                                           @RequestJson(value = "showtypes", required = false) List<Integer> showtypes,
                                           @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                           HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);
            paramMap.put("pointtypes", pointtypes);
            paramMap.put("monitortime", monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("outputpropertys", outputpropertys);
            if (pollutionids != null && pollutionids.size() > 0) {
                paramMap.put("pkids", pollutionids);
            } else {//查所有
                List<Map<String, Object>> result = new ArrayList<>();
                for (Integer pointtype:pointtypes) {
                    paramMap.put("pointtype", pointtype);
                    result.addAll(entDataReportService.getSelectPollutionInfoByPointType(paramMap));
                }
                if (result != null && result.size() > 0) {
                    for (Map<String, Object> map : result) {
                        if (map.get("id")!=null&&!pollutionids.contains(map.get("id").toString().toString())) {
                            pollutionids.add(map.get("id").toString());
                        }
                    }
                }
                paramMap.put("pkids", pollutionids);
            }
            if (showtypes != null && showtypes.size() > 0) {//是否展示某类型数据（0：排放量，1：折算值）
                paramMap.put("showtypes", showtypes);
            }
            paramMap.put("tabletitletype", "2");//自定义表头类型，2为导出列表的表头
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            Map<String, Object> result = new HashMap<>();
            //获取企业数据报表
            result = entDataReportService.getGasSummaryEntDataReportByParamMap(paramMap);
            List<Map<String, Object>> tabletitledata = (List<Map<String, Object>>) result.get("tabletitledata");
            List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) result.get("tablelistdata");
            String titlename = result.get("titlename").toString();
            String name = "";
            if ("day".equals(reporttype)) {
                name = "日报";
            } else if ("week".equals(reporttype)) {
                name = "周报";
            } else if ("month".equals(reporttype)) {
                name = "月报";
            } else if ("year".equals(reporttype)) {
                name = "年报";
            }
            //设置文件名称
            String fileName = "汇总报表_" + name + "_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);
            //ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", tabletitledata, tablelistdata, "");
            // ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 测试导出溯源结果值
     * */
    @RequestMapping(value = "exportEntReportTimeSlotDataByParamMapsss", method = RequestMethod.POST)
    public void exportEntReportTimeSlotDataByParamMapssss(HttpServletRequest request, HttpServletResponse response) throws Exception{
        try {
            traceSourceResultController.exportTraceSourceResultTxtFile("",request,response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
