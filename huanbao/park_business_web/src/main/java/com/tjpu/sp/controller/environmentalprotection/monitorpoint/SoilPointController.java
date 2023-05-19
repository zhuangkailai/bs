package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.SoilPointService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: liyc
 * @date:2019/12/16 0016 18:11
 * @Description: 土壤监测点控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("soilPoint")
public class SoilPointController {
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private SoilPointService soilPointService;

    private String sysmodel = "soilMonitorPoint"; //菜单code
    private String pk_id = "pk_id";
    private String listfieldtype = "list";

    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: liyc
     * @date: 2019/12/14 0014 14:01
     * @Description: 获取土壤监测点初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson, session]
     * @throws:
     **/

    @RequestMapping(value = "getSoilPointListPage", method = RequestMethod.POST)
    public Object getSoilPointListPage(HttpServletRequest request ) {
        try {

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("listfieldtype", listfieldtype);
            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getListByParam(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/12/14 0014 13:02
     * @Description: 通过自定义参数获取土壤监测点信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     **/
    @RequestMapping(value = "getSoilPointInfoByParamMap", method = RequestMethod.POST)
    public Object getSoilPointInfoByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(param);
            return resultList;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: liyc
     * @date: 2019/12/14 0014 13:02
     * @Description: 通过自定义参数获取土壤监测点信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     **/
    @RequestMapping(value = "getAllSoilPointInfo", method = RequestMethod.POST)
    public Object getAllSoilPointInfo() {
        try {
            List<Map<String,Object>> resultList =  soilPointService.getAllSoilPointInfo();
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/12/16 0016 14:30
     * @Description: 获取土壤监测点添加页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     **/
    @RequestMapping(value = "getSoilPointAddPage", method = RequestMethod.POST)
    public Object getSoilPointAddPage() {
        //设置参数
        try {
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
     * @author: liyc
     * @date: 2019/12/14 0014 14:21
     * @Description: 添加土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     **/
    @RequestMapping(value = "addSoilPoint", method = RequestMethod.POST)
    public Object addSoilPoint(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
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
     * @author: liyc
     * @date: 2019/12/14 0014 14:42
     * @Description: 根据主键id删除土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "deleteSoilPointByID", method = RequestMethod.POST)
    public Object deleteSoilPointByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<Object, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            parammap.put("pk_id", id);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/12/14 0014 14:49
     * @Description: 获取土壤监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getSoilPointDetailById", method = RequestMethod.POST)
    public Object getSoilPointDetailById(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @author: liyc
     * @date: 2019/12/16 0016 14:37
     * @Description: 根据主键ID获取土壤监测点信息修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getSoilPointUpdatePageByID", method = RequestMethod.POST)
    public Object getSoilPointUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @author: liyc
     * @date: 2019/12/14 0014 15:36
     * @Description: 修改土壤监测点的一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "updateSoilPointByID", method = RequestMethod.POST)
    public Object updateSoilPointByID(HttpServletRequest request) throws Exception {
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
    *@author: liyc
    *@date: 2019/12/17 0017 11:42
    *@Description: 导出土壤监测点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson, request, response]
    *@throws:
    **/
    @RequestMapping(value = "exportSoilPointInfo",method = RequestMethod.POST)
    public void exportSoilPointInfo(@RequestJson(value = "paramsjson") Object paramsJson,
                                     HttpServletRequest request, HttpServletResponse response) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            //获取表头数据
            List<Map<String, Object>> tabletitledata = soilPointService.getTableTitleForSafety();
            //获取查询列表数据
            List<Map<String, Object>> newLicenseInfo = soilPointService.getSoilPointByParamMap(jsonObject);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "土壤监测点信息";
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, newLicenseInfo, "");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 5:43
     * @Description: 通过土壤类型，企业id查询土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fksoilpointtypecode, fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getSoilPointInfoByParams", method = RequestMethod.POST)
    public Object getSoilPointInfoByParams(@RequestJson(value = "fksoilpointtypecode",required =false) Object fksoilpointtypecode,
                                           @RequestJson(value = "fkpollutionid",required =false) Object fkpollutionid) throws Exception {
        try {

            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("fksoilpointtypecode",fksoilpointtypecode);
            paramMap.put("fkpollutionid",fkpollutionid);
            List<Map<String, Object>> soil= soilPointService.getSoilPointInfoByParamMap(paramMap);

            return AuthUtil.parseJsonKeyToLower("success",soil);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/8/27 0027 下午 2:28
     * @Description: 获取土壤监测点树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getSoilPointTree", method = RequestMethod.POST)
    public Object getSoilPointTree() throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            List<Map<String, Object>> soil= soilPointService.getSoilPointInfoByParamMap(paramMap);
            Map<String, List<Map<String, Object>>> collect = soil.stream().filter(m -> m.get("fk_soilpointtypename") != null).collect(Collectors.groupingBy(m -> m.get("fk_soilpointtypename").toString()));

            List<Map<String,Object>> resultList=new ArrayList<>();
            for (String fk_soilpointtypename : collect.keySet()) {
                Map<String,Object> data=new HashMap<>();
                data.put("id",UUID.randomUUID().toString());
                data.put("label",fk_soilpointtypename);
                data.put("type","soilpointtype");
                if("企业".equals(fk_soilpointtypename)){
                    List<Map<String, Object>> maps = collect.get(fk_soilpointtypename);
                    Map<String, List<Map<String, Object>>> collect1 = maps.stream().filter(m -> m.get("pollutionname") != null).collect(Collectors.groupingBy(m -> m.get("pollutionname").toString()));
                    List<Map<String,Object>> pollution=new ArrayList<>();
                    for (String pollutionname : collect1.keySet()) {
                        Map<String,Object> data1=new HashMap<>();
                        data1.put("id", UUID.randomUUID().toString());
                        data1.put("label",pollutionname);
                        data1.put("type","pollution");
                        data1.put("child",collect1.get(pollutionname));
                        pollution.add(data1);
                    }
                    data.put("child",pollution);
                }else{
                    data.put("child",collect.get(fk_soilpointtypename));
                }
                resultList.add(data);
            }
            return AuthUtil.parseJsonKeyToLower("success",resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
