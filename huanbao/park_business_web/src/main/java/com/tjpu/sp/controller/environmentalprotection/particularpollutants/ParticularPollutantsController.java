package com.tjpu.sp.controller.environmentalprotection.particularpollutants;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.particularpollutants.ParticularPollutantsVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.particularpollutants.ParticularPollutantsService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: chengzq
 * @date: 2019/6/13 0013 下午 2:49
 * @Description: 特征污染物库控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("particularpollutants")
public class ParticularPollutantsController {
    @Autowired
    private ParticularPollutantsService particularPollutantsService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    /**
     * @author: chengzq
     * @date: 2019/6/10 0010 上午 9:23
     * @Description: 通过污染源名称，排口名称，污染物名称，监测点类型，版本号查询污染物库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getParticularPollutantsByParamMap", method = RequestMethod.POST)
    public Object getParticularPollutantsByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Object pagenum = jsonObject.get("pagenum");
            Object pagesize = jsonObject.get("pagesize");

            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(Integer.valueOf(pagenum.toString()), Integer.valueOf(pagesize.toString()));
            }

            //将前端传入版本号后面添加.0
            Object version = jsonObject.get("version");
            if (version != null) {
                List<String> versions = new ArrayList<>();
                JSONArray jsonArray = JSONArray.fromObject(version);
                for (Object o : jsonArray) {
                    if (o != null) {
                        versions.add(o.toString() + ".0");
                    }
                }
                jsonObject.put("version", versions);
            }

            List<Map<String, Object>> particularPollutantsByParamMap = particularPollutantsService.getParticularPollutantsByParamMap(jsonObject);
            String lastVersion = particularPollutantsService.getLastVersion();
            if (StringUtils.isNotBlank(lastVersion)) {
                resultMap.put("lastversion", Double.valueOf(lastVersion) + 1 + "");
            } else {
                resultMap.put("lastversion", "1.0");
            }
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(particularPollutantsByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("total", total);
            resultMap.put("primarykey", "pk_dataid");
            resultMap.put("datalist", particularPollutantsByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 4:08
     * @Description: 新增特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [adddatalist, session]
     * @throws:
     */
    @RequestMapping(value = "addParticularPollutants", method = RequestMethod.POST)
    public Object addParticularPollutants(@RequestJson(value = "adddatalist") List<Object> adddatalist ) throws Exception {
        try {
            List<ParticularPollutantsVO> datas = new ArrayList<>();
            if (adddatalist.size() > 0) {
                Date now = new Date();

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                JSONArray jsonArray = JSONArray.fromObject(adddatalist);
                for (Object o : jsonArray) {
                    Map<String, Object> data1 = (Map) o;
                    ParticularPollutantsVO particularPollutantsVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(data1), new ParticularPollutantsVO());

                    particularPollutantsVO.setPkDataid(UUID.randomUUID().toString());
                    Object detectiontime = data1.get("detectiontime");
                    if (detectiontime != null && !"null".equals(detectiontime.toString())) {
                        Date date = DataFormatUtil.parseDateYMD(detectiontime.toString());
                        particularPollutantsVO.setDetectiontime(date);
                    }
                    particularPollutantsVO.setUpdatetime(now);
                    particularPollutantsVO.setUpdateuser(username);
                    datas.add(particularPollutantsVO);
                }
            }
            particularPollutantsService.insertParticularPollutants(datas);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:26
     * @Description: 通过id或者版本号获取污染物库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getParticularPollutantsInfoByIDOrVersion", method = RequestMethod.POST)
    public Object getParticularPollutantsInfoByIDOrVersion(@RequestJson(value = "id", required = false) String id,
                                                           @RequestJson(value = "version", required = false) String version) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", id);
            paramMap.put("version", version);
            List<Map<String, Object>> list = particularPollutantsService.selectParticularPollutantsById(paramMap);

            List<Map<String, Object>> collect = list.stream().filter(m -> m.get("outputname") != null && m.get("pollutionname") != null).sorted(Comparator.comparing(m ->
                    ((Map) m).get("pollutionname").toString()).thenComparing(m -> ((Map) m).get("outputname").toString())).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 上午 11:22
     * @Description: 通过版本号获取所有废水废气排口的污染物库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, version]
     * @throws:
     */
    @RequestMapping(value = "getParticularPollutantsOfAllOutPutByVersion", method = RequestMethod.POST)
    public Object getParticularPollutantsOfAllOutPutByVersion(@RequestJson(value = "version", required = false) String version) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("version", version);
            List<Map<String, Object>> allWaterOutputAndGasOutputInfo = gasOutPutInfoService.getAllWaterOutputAndGasOutputInfo();
            List<Map<String, Object>> list = particularPollutantsService.selectParticularPollutantsById(paramMap);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> map : list) {
                Map<String, Object> data = new HashMap<>();
                data.put("pollutionname", map.get("pollutionname"));
                data.put("outputname", map.get("outputname"));
                data.put("fkpollutionid", map.get("fkpollutionid"));
                data.put("fkoutputid", map.get("fkoutputid"));
                data.put("monitortype", map.get("fkmonitorpointtypecode"));
                result.add(data);
            }

            allWaterOutputAndGasOutputInfo.removeAll(result);
            for (int i = 0; i < allWaterOutputAndGasOutputInfo.size(); i++) {
                Map<String, Object> map = allWaterOutputAndGasOutputInfo.get(i);
                map.put("pkdataid", "");
                map.put("fkmonitorpointtypecode", map.get("monitortype"));
                map.put("fkpollutantcode", "");
                map.put("detectiontime", "");
                map.put("detectionconcentration", "");
                map.put("ismainpollutant", "");
                map.put("version", version);
                map.put("updatetime", "");
                map.put("updateuser", "");
            }
            list.addAll(allWaterOutputAndGasOutputInfo);

            List<Map<String, Object>> collect = list.stream().filter(m -> m.get("outputname") != null && m.get("pollutionname") != null).sorted(Comparator.comparing(m ->
                    ((Map) m).get("pollutionname").toString()).thenComparing(m -> ((Map) m).get("outputname").toString())).collect(Collectors.toList());

            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 4:08
     * @Description: 修改多条排放口特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [adddatalist, session]
     * @throws:
     */
    @RequestMapping(value = "updateParticularPollutants", method = RequestMethod.POST)
    public Object updateParticularPollutants(@RequestJson(value = "updatedatalist") List<Object> updatedatalist ) throws Exception {
        try {
            List<ParticularPollutantsVO> datas = new ArrayList<>();
            if (updatedatalist.size() > 0) {
                Date now = new Date();

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                JSONArray jsonArray = JSONArray.fromObject(updatedatalist);
                for (Object o : jsonArray) {
                    Map<String, Object> data1 = (Map) o;
                    ParticularPollutantsVO particularPollutantsVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(data1), new ParticularPollutantsVO());
                    Object detectiontime = data1.get("detectiontime");
                    if (detectiontime != null && !"null".equals(detectiontime.toString())) {
                        Date date = DataFormatUtil.parseDateYMD(detectiontime.toString());
                        particularPollutantsVO.setDetectiontime(date);
                    }
                    particularPollutantsVO.setUpdatetime(now);
                    particularPollutantsVO.setPkDataid(UUID.randomUUID().toString());
                    particularPollutantsVO.setUpdateuser(username);
                    datas.add(particularPollutantsVO);
                }
            }
            particularPollutantsService.updateParticularPollutants(datas);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 4:08
     * @Description: 修改单条排放口特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [adddatalist, session]
     * @throws:
     */
    @RequestMapping(value = "updateParticularPollutant", method = RequestMethod.POST)
    public Object updateParticularPollutant(@RequestJson(value = "updatedatalist") List<Object> updatedatalist ) throws Exception {
        try {
            List<ParticularPollutantsVO> datas = new ArrayList<>();
            if (updatedatalist.size() > 0) {
                Date now = new Date();

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                JSONArray jsonArray = JSONArray.fromObject(updatedatalist);
                for (Object o : jsonArray) {
                    Map<String, Object> data1 = (Map) o;
                    ParticularPollutantsVO particularPollutantsVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(data1), new ParticularPollutantsVO());
                    Object detectiontime = data1.get("detectiontime");
                    if (detectiontime != null && !"null".equals(detectiontime.toString())) {
                        Date date = DataFormatUtil.parseDateYMD(detectiontime.toString());
                        particularPollutantsVO.setDetectiontime(date);
                    }
                    particularPollutantsVO.setUpdatetime(now);
                    particularPollutantsVO.setUpdateuser(username);
                    datas.add(particularPollutantsVO);
                }
            }
            if(datas.size()>0){
                particularPollutantsService.updateByPrimaryKey(datas.get(0));
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:27
     * @Description: 通过id删除排放口特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteParticularPollutantsByID", method = RequestMethod.POST)
    public Object deleteParticularPollutantsByID(@RequestJson(value = "id", required = false) String id) {
        try {

            particularPollutantsService.deleteParticularPollutantsById(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/8 0008 下午 1:34
     * @Description: 根据特征污染物统计企业数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutionForPollutantByMonitorPointType", method = RequestMethod.POST)
    public Object countPollutionForPollutantByMonitorPointType(@RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> dataList = particularPollutantsService.countPollutionForPollutant(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:30
     * @Description: 通过id查询排放口特征污染物库详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getParticularPollutantsDetailByID", method = RequestMethod.POST)
    public Object getParticularPollutantsDetailByID(@RequestJson(value = "id", required = false) String id) {
        try {
            return AuthUtil.parseJsonKeyToLower("success", particularPollutantsService.getParticularPollutantsDetailByID(id));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:58
     * @Description: 获取所有版本号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getParticularPollutantVersions", method = RequestMethod.POST)
    public Object getParticularPollutantVersions() {
        try {
            String lastVersion = particularPollutantsService.getLastVersion();
            List<String> data = new ArrayList<>();
            if (StringUtils.isNotBlank(lastVersion)) {
                Double integer = Double.valueOf(lastVersion);
                for (int i = 1; i <= integer; i++) {
                    data.add(i + ".0");
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/19 0019 上午 10:20
     * @Description: 通过sysmodel获取用户按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sysmodel, session]
     * @throws:
     */
    @RequestMapping(value = "getUserButtonAuthBySysmodel", method = RequestMethod.POST)
    public Object getUserButtonAuthBySysmodel(@RequestJson(value = "sysmodel", required = false) String sysmodel ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(param);

            return userButtonAuthInMenu;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/19 0019 下午 2:13
     * @Description: 通过自定义参数导出特征污染物库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sysmodel, session]
     * @throws:
     */
    @RequestMapping(value = "exportParticularPollutantsByParamMap", method = RequestMethod.POST)
    public Object exportParticularPollutantsByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            List headers = new ArrayList<>();
            headers.add("版本");
            headers.add("污染源名称");
            headers.add("排口名称");
            headers.add("污染物名称");
            headers.add("检测浓度");
            headers.add("检测时间");

            List headersField = new ArrayList<>();
            headersField.add("version");
            headersField.add("pollutionname");
            headersField.add("outputname");
            headersField.add("pollutantname");
            headersField.add("detectionconcentration");
            headersField.add("detectiontime");

            JSONObject jsonObject = JSONObject.fromObject(getParticularPollutantsByParamMap(paramsjson));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object data1 = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);
            for (int i = 0; i < jsonArray.size(); i++) {
                String string = jsonArray.getString(i);

                JSONObject jsonObject2 = JSONObject.fromObject(string);

                Object pollutantname = jsonObject2.get("pollutantname");
                Object detectionconcentration = jsonObject2.get("detectionconcentration");
                Object detectiontime = jsonObject2.get("detectiontime");
                if (pollutantname != null && "null".equals(pollutantname.toString())) {
                    jsonObject2.put("pollutantname", "");
                }
                if (detectionconcentration != null && "null".equals(detectionconcentration.toString())) {
                    jsonObject2.put("detectionconcentration", "");
                }
                if (detectiontime != null && "null".equals(detectiontime.toString())) {
                    jsonObject2.put("detectiontime", "");
                }
                jsonArray.set(i, jsonObject2);
            }

            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("特征污染物库", response, request, bytesForWorkBook);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/9 0009 下午 1:48
     * @Description: 通过监测点类型和特征污染物编码获取最新版本号的企业信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPollutionListDataByMonitorPointTypeAndPollutantCode", method = RequestMethod.POST)
    public Object getPollutionListDataByMonitorPointTypeAndPollutantCode(@RequestJson(value = "monitorpointtype", required = true) String monitorpointtype,
                                                                         @RequestJson(value = "pollutantcode", required = true) String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> particularPollutantsByParamMap = particularPollutantsService.getPollutionListDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", particularPollutantsByParamMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }






}
