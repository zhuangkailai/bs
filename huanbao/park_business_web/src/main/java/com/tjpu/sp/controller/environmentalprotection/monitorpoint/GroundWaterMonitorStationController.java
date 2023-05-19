package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GroundWaterService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/12/14 0014 11:39
 * @Description: 监测点控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("groundWater")
public class GroundWaterMonitorStationController {
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private GroundWaterService groundWaterService;

    private String sysmodel = "undergroundWaterMonitorpoint"; //菜单code
    private String pk_id = "pk_id";
    private String listfieldtype = "list-water";

    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: liyc
     * @date: 2019/12/14 0014 14:01
     * @Description: 获取地下水监测点初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson, session]
     * @throws:
     **/

    @RequestMapping(value = "getGroundWaterListPage", method = RequestMethod.POST)
    public Object getGroundWaterListPage(HttpServletRequest request ) {
        try {

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
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
     * @Description: 通过自定义参数获取地下水监测点信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     **/
    @RequestMapping(value = "getGroundWaterInfoByParamMap", method = RequestMethod.POST)
    public Object getGroundWaterInfoByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
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
     * @date: 2019/12/16 0016 14:30
     * @Description: 获取地下水监测点添加页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     **/
    @RequestMapping(value = "getGroundWaterAddPage", method = RequestMethod.POST)
    public Object getGroundWaterAddPage() {
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
     * @Description: 添加parseJsonKeyToLower监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     **/
    @RequestMapping(value = "addGroundWater", method = RequestMethod.POST)
    public Object addGroundWater(HttpServletRequest request) throws Exception {
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
     * @Description: 根据主键id删除地下水监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "deleteGroundWaterByID", method = RequestMethod.POST)
    public Object deleteGroundWaterByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @Description: 获取地下水监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getGroundWaterDetailById", method = RequestMethod.POST)
    public Object getGroundWaterDetailById(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @Description: 根据主键ID获取地下水监测点信息修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getGroundWaterUpdatePageByID", method = RequestMethod.POST)
    public Object getGroundWaterUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @Description: 修改地下水监测点的一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "updateGroundWaterByID", method = RequestMethod.POST)
    public Object updateGroundWaterByID(HttpServletRequest request) throws Exception {
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
    *@date: 2019/12/17 0017 9:01
    *@Description: 导出地下水监测点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson, session, request, response]
    *@throws:
    **/
    @RequestMapping(value = "exportGroundWaterInfo",method = RequestMethod.POST)
    public void exportGroundWaterInfo(@RequestJson(value = "paramsjson") Object paramsJson,
                                               HttpServletRequest request, HttpServletResponse response) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            //获取表头数据
            List<Map<String, Object>> tabletitledata = groundWaterService.getTableTitleForSafety();
            //获取查询列表数据
            List<Map<String, Object>> newLicenseInfo = groundWaterService.getGroundWaterInfoByParamMap(jsonObject);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "地下水监测点信息";
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, newLicenseInfo, "");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
