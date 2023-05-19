package com.tjpu.sp.controller.environmentalprotection.devopsinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsExplainVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DeviceDevOpsInfoService;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.EntDevOpsExplainService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("entDevOpsExplain")
public class EntDevOpsExplainController {
    @Autowired
    private EntDevOpsExplainService entDevOpsExplainService;
    @Autowired
    private DeviceDevOpsInfoService deviceDevOpsInfoService;

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:44
     * @Description: 根据自定义参数获取企业运维说明列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntDevOpsExplainsByParamMap", method = RequestMethod.POST)
    public Object getEntDevOpsExplainsByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            if (jsonObject.get("starttime") != null) {
                jsonObject.put("starttime", jsonObject.get("starttime") + " 00:00:00");
            }
            if (jsonObject.get("endtime") != null) {
                jsonObject.put("endtime", jsonObject.get("endtime") + " 23:59:59");
            }
            List<Map<String, Object>> datalist = entDevOpsExplainService.getEntDevOpsExplainsByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
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
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:21
     * @Description: 新增企业运维说明信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addEntDevOpsExplain", method = RequestMethod.POST)
    public Object addEntDevOpsExplain(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            EntDevOpsExplainVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntDevOpsExplainVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkId(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            entDevOpsExplainService.addEntDevOpsExplain(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/27 0027 上午 9:14
     * @Description: 验证企业运维信息是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    /*@RequestMapping(value = "/isTableDataHaveInfoByParamMap", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByParamMap(@RequestJson(value = "monitorpointid", required = true) String monitorpointid,
                                                @RequestJson(value = "starttime", required = true) String starttime,
                                                @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> listmap = stopProductionInfoService.getLatestStopProductionInfoByParamMap(paramMap);
            if (listmap!=null) {    //等于0 没有此条数据可以添加
                String ishave = "no";
                Map<String, List<Map<String, Object>>> listMap = listmap.stream().collect(Collectors.groupingBy(m -> m.get("FK_Outputid").toString()));
                for (String id :monitorpointids){
                    if (listMap.get(id)!=null){
                        Map<String, Object> map = listMap.get(id).get(0);
                        if (endtime!=null&&!"".equals(endtime)) {//当结束时间不为空  开始和结束时间 都要进行判断
                            Date start = DataFormatUtil.getDateYMDHMS(starttime);
                            Date end = DataFormatUtil.getDateYMDHMS(endtime);
                            Date startdate = (Date) map.get("StartTime");
                            if (map.get("EndTime")!=null) {
                                Date enddate = (Date) map.get("EndTime");
                                if (start.getTime() == startdate.getTime() && end.getTime() == enddate.getTime()) {
                                    ishave = "yes";
                                } else {
                                    boolean lateststart = DataFormatUtil.isEffectiveDate(start, startdate, enddate);
                                    boolean latestend = DataFormatUtil.isEffectiveDate(end, startdate, enddate);
                                    boolean startflag = DataFormatUtil.isEffectiveDate(startdate, start, end);
                                    boolean endflag = DataFormatUtil.isEffectiveDate(enddate, start, end);
                                    if (lateststart == true || latestend == true || startflag == true || endflag == true) {//判断开始时间和结束时间最新一条停场数据的时间范围内
                                        //已经有了不添加
                                        ishave = "yes";
                                    }
                                }
                            }else{
                                ishave = "yes";//已有永久停产记录  无法再进行停产操作
                            }
                        }else {//当结束时间为空  永久停产时
                            Date start = DataFormatUtil.getDateYMDHMS(starttime);
                            Date startdate = (Date) map.get("StartTime");
                            if (map.get("EndTime") != null) {//已有记录是否为永久停产记录
                                Date enddate = (Date) map.get("EndTime");
                                if (start.getTime() == startdate.getTime()) {
                                    return AuthUtil.parseJsonKeyToLower("success", "yes");
                                } else {
                                    Calendar c1 = Calendar.getInstance();
                                    Calendar c2 = Calendar.getInstance();
                                    c1.setTime(enddate);
                                    c2.setTime(start);
                                    int result = c1.compareTo(c2);
                                    if (result >= 0){
                                        ishave = "yes";
                                    }else{
                                        ishave = "no";
                                    }
                                }
                            }else{
                                ishave = "yes";//已有永久停产记录  无法再进行停产操作
                            }
                        }

                    }
                }
                return AuthUtil.parseJsonKeyToLower("success", ishave);
            } else{
                return AuthUtil.parseJsonKeyToLower("success", "no");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:23
     * @Description: 修改企业运维说明
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "editEntDevOpsExplain", method = RequestMethod.POST)
    public Object editEntDevOpsExplain(@RequestJson(value = "updateformdata") Object updateformdata,
                                       HttpSession session) {
        try {
            JSONObject jsonFormData = JSONObject.fromObject(updateformdata);
            EntDevOpsExplainVO obj = JSONObjectUtil.parseStringToJavaObject(jsonFormData.toString(), EntDevOpsExplainVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Date nowTime = new Date();
            obj.setUpdateuser(username);
            obj.setUpdatetime(nowTime);
            entDevOpsExplainService.editDevOpsExplain(obj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:24
     * @Description: 根据主键ID获取企业运维说明编辑页面初始化数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEntDevOpsExplainUpdateDataByID", method = RequestMethod.POST)
    public Object getEntDevOpsExplainUpdateDataByID(@RequestJson(value = "id") String id
    ) throws Exception {
        try {
            Map<String, Object> result = entDevOpsExplainService.getEntDevOpsExplainUpdateDataByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:24
     * @Description: 根据主键ID删除企业运维说明记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deleteEntDevOpsExplainByID", method = RequestMethod.POST)
    public Object deleteEntDevOpsExplainByID(@RequestJson(value = "id") String id
    ) throws Exception {
        try {
            entDevOpsExplainService.deleteEntDevOpsExplainByID(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:24
     * @Description: 根据主键ID获取企业运维说明详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEntDevOpsExplainDetailByID", method = RequestMethod.POST)
    public Object getEntDevOpsExplainDetailByID(@RequestJson(value = "id") String id
    ) throws Exception {
        try {
            Map<String, Object> result = entDevOpsExplainService.getEntDevOpsExplainDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:44
     * @Description: 根据自定义参数获取某时间段内的运维信息（设备运维及企业运维）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getDeviceAndEntDevOpsExplainsByParamMap", method = RequestMethod.POST)
    public Object getDeviceAndEntDevOpsExplainsByParamMap(
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
            @RequestJson(value = "devopstime", required = false) String devopstime,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> pollutionids = new ArrayList<>();
            if (monitorpointtypes == null || monitorpointtypes.size() == 0) {
                monitorpointtypes = new ArrayList<>();
                monitorpointtypes.add(monitorpointtype);
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Integer i : monitorpointtypes) {
                dataList.addAll(deviceDevOpsInfoService.getMonitorPointDataByPollutionIDAndType(pollutionids, i));
            }
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("devopstime", devopstime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pointlist", dataList);
            result = entDevOpsExplainService.getDeviceAndEntDevOpsExplainsByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:44
     * @Description: 根据自定义参数获取某个企业点位某时间段内的运维信息（企业运维）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getOnePointEntDevOpsExplainsByParamMap", method = RequestMethod.POST)
    public Object getOnePointEntDevOpsExplainsByParamMap(@RequestJson(value = "monitorpointid") String monitorpointid,
                                                         @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime,
                                                         @RequestJson(value = "pagesize") Integer pagesize,
                                                         @RequestJson(value = "pagenum") Integer pagenum) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            PageHelper.startPage(pagenum, pagesize);
            List<Map<String, Object>> datalist = entDevOpsExplainService.getOnePointEntDevOpsExplainsByParamMap(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
