package com.tjpu.sp.controller.environmentalprotection.monitorpoint;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.MonitorEquipmentVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.MonitorEquipmentService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: xsm
 * @date: 2019/05/23 0016 上午 10:08
 * @Description: 监测设备信息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("monitorEquipment")
public class MonitorEquipmentController {
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    @Autowired
    private MonitorEquipmentService monitorEquipmentService;

    private String sysmodel = "monitorequipment";
    private String pk_id = "pk_id";
    private String listfieldtype = "list-base";

    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: xsm
     * @date: 2019/05/23  上午 10:20
     * @Description: 获取监测设备初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "getMonitorEquipmentsListPage", method = RequestMethod.POST)
    public Object getMonitorEquipmentsListPage(HttpServletRequest request ) {
        try {
            //获取userid
            Map<String, Object> datas = new HashMap<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.putIfAbsent("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            paramMap.put("queryfieldtype", "query-base");
            String param = AuthUtil.paramDataFormat(paramMap);
            //表头数据
            Object tableTitle = publicSystemMicroService.getTableTitle(param);
            JSONObject jsonObject = JSONObject.fromObject(tableTitle);
            String titleData = jsonObject.getString("data");
            //按钮数据
            paramMap.put("sysmodel", "waterDirectOutlet");//获取父级
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject4 = jsonObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject4.getString("data");
            JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject5.get("topbuttondata")!=null?jsonObject5.getString("topbuttondata"):"";
            String listoperation = jsonObject5.get("tablebuttondata")!=null?jsonObject5.getString("tablebuttondata"):"";
            Map<String, Object> buttondatamap = new HashMap<>();//按钮
            buttondatamap.put("topbuttondata", topoperations);
            buttondatamap.put("tablebuttondata", listoperation);
            //查询条件数据
            Map<String, Object> querydata = new HashMap<>();
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = jsonObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            JSONObject jsonObject3 = JSONObject.fromObject(queryData);
            String dualcontrolskey = jsonObject3.getString("dualcontrolskey");
            String querycontroldata = jsonObject3.getString("querycontroldata");
            String queryformdata = jsonObject3.getString("queryformdata");
            querydata.put("dualcontrolskey", dualcontrolskey);
            querydata.put("querycontroldata", querycontroldata);
            querydata.put("queryformdata", queryformdata);
            // 分页
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = monitorEquipmentService.getMonitorEquipmentsByParamMap(paramMap);
            // 保存分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            // 分页后的数据
            List<Map<String, Object>> listInfo = pageInfo.getList();
            tabledata.put("pagesize", pageInfo.getPageSize());// 每页条数
            tabledata.put("pagenum", pageInfo.getPageNum());// 当前页
            tabledata.put("total", pageInfo.getTotal());// 总条数
            tabledata.put("pages", pageInfo.getPages());// 总页数
            tabledata.put("total", dataList.size());// 总条数
            tabledata.put("primarykey", pk_id);// 主键
            tabledata.put("tablelistdata", dataList);// 数据
            tabledata.put("tabletitledata", titleData);// 表头
            //返回数据
            datas.put("querydata", querydata);
            datas.put("tabledata", tabledata);
            datas.put("buttondata", buttondatamap);
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/5/23  上午 10:32
     * @Description: 根据自定义参数获取监测设备列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:[paramsjson,]
     * @return:
     */
    @RequestMapping(value = "getMonitorEquipmentsByParamMap", method = RequestMethod.POST)
    public Object getMonitorEquipmentsByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                int pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
                int pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> listData = monitorEquipmentService.getMonitorEquipmentsByParamMap(paramMap);
            PageInfo<Map<String, Object>> page = new PageInfo<>(listData);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("pageSize", page.getPageSize());
                resultMap.put("pageNum", page.getPageNum());
                resultMap.put("pages", page.getPages());
            }
            //总条数
            resultMap.put("total", page.getTotal());
            resultMap.put("tablelistdata", listData);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/23  上午 11:58
     * @Description: 根据污染物类型获取监测设备新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @return:
     */
    @RequestMapping(value = "getMonitorEquipmentAddPageByPollutantType", method = RequestMethod.POST)
    public Object getMonitorEquipmentAddPageByPollutantType(@RequestJson(value = "pollutanttype", required = true) String pointtype) {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(pointtype))) {
                case WasteWaterEnum: //废水
                    paramMap.put("addfieldtype", "add-water");
                    break;
                case WasteGasEnum: //废气
                    paramMap.put("addfieldtype", "add-gas");
                    break;
                case RainEnum: //雨水
                    paramMap.put("addfieldtype", "add-rain");
                    break;
                case AirEnum: //空气
                    paramMap.put("addfieldtype", "add-air");
                    break;
                case meteoEnum: //气象
                    paramMap.put("addfieldtype", "add-meteorological");
                    break;
                case EnvironmentalVocEnum: //voc
                    paramMap.put("addfieldtype", "add-voc");
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    paramMap.put("addfieldtype", "add-stench");
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    paramMap.put("addfieldtype", "add-factorystench");
                    break;
                case EnvironmentalDustEnum: //扬尘
                    paramMap.put("addfieldtype", "add-factorydust");
                    break;
                case MicroStationEnum: //微站
                    paramMap.put("addfieldtype", "add-microstation");
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    paramMap.put("addfieldtype", "add-factorystation");
                    break;
                case WaterQualityEnum: //水质
                    paramMap.put("addfieldtype", "add-waterquality");
                    break;
            }
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
     * @date: 2019/5/23  下午 12:00
     * @Description: 新增监测设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @return:
     */
    @RequestMapping(value = "addMonitorEquipment", method = RequestMethod.POST)
    public Object addMonitorEquipment(HttpServletRequest request) {
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
     * @author: chengzq
     * @date: 2020/7/22 0022 上午 9:04
     * @Description: 不使用通用接口新增设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "monitorEquipmentAdd", method = RequestMethod.POST)
    public Object monitorEquipmentAdd(@RequestJson(value = "formdata") Object paramsjson)throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            MonitorEquipmentVO monitorEquipmentVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new MonitorEquipmentVO());
            monitorEquipmentVO.setPkId(UUID.randomUUID().toString());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            Date date = new Date();
            monitorEquipmentVO.setUpdatetime(date);
            monitorEquipmentVO.setUpdateuser(username);
            monitorEquipmentService.insert(monitorEquipmentVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/22 0022 上午 9:05
     * @Description: 不使用通用接口修改设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "monitorEquipmentUpdate", method = RequestMethod.POST)
    public Object monitorEquipmentUpdate(@RequestJson(value = "formdata") Object paramsjson)throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            MonitorEquipmentVO monitorEquipmentVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new MonitorEquipmentVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            Date date = new Date();
            monitorEquipmentVO.setUpdatetime(date);
            monitorEquipmentVO.setUpdateuser(username);
             monitorEquipmentService.updateByPrimaryKey(monitorEquipmentVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/5/23  下午 1:10
     * @Description: 根据主键ID和监测类型获取监测设备修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pk_id, pollutanttype]
     * @return:
     */
    @RequestMapping(value = "/getMonitorEquipmentUpdatePageByIDAndPollutantType", method = RequestMethod.POST)
    public Object getMonitorEquipmentUpdatePageByIDAndPollutantType(@RequestJson(value = "id", required = true) String id, @RequestJson(value = "pollutanttype", required = true) String pointtype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(pointtype))) {
                case WasteWaterEnum: //废水
                    paramMap.put("editfieldtype", "edit-water");
                    break;
                case WasteGasEnum: //废气
                    paramMap.put("editfieldtype", "edit-gas");
                    break;
                case RainEnum: //雨水
                    paramMap.put("editfieldtype", "edit-rain");
                    break;
                case AirEnum: //空气
                    paramMap.put("editfieldtype", "edit-air");
                    break;
                case meteoEnum: //气象
                    paramMap.put("editfieldtype", "edit-meteorological");
                    break;
                case EnvironmentalVocEnum: //voc
                    paramMap.put("editfieldtype", "edit-voc");
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    paramMap.put("editfieldtype", "edit-stench");
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    paramMap.put("editfieldtype", "edit-factorystench");
                    break;
                case EnvironmentalDustEnum: //扬尘
                    paramMap.put("editfieldtype", "edit-factorydust");
                    break;
                case MicroStationEnum: //微站
                    paramMap.put("editfieldtype", "edit-microstation");
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    paramMap.put("editfieldtype", "edit-factorystation");
                    break;
                case WaterQualityEnum: //水质
                    paramMap.put("editfieldtype", "edit-waterquality");
                    break;
            }
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/23  下午 1:07
     * @Description: 修改在线设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @return:
     */
    @RequestMapping(value = "updateMonitorEquipment", method = RequestMethod.POST)
    public Object updateMonitorEquipment(HttpServletRequest request) throws Exception {
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
     * @date: 2019/5/23  下午 1:05
     * @Description: 根据主键ID删除监测设备
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @return:
     */
    @RequestMapping(value = "/deleteMonitorEquipmentByID", method = RequestMethod.POST)
    public Object deleteMonitorEquipmentByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put(pk_id, id);
            paramMap.put("sysmodel", "monitorequipment");
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
     * @date: 2019/5/27  下午 1:15
     * @Description: 根据主键ID和污染物类型获取监测设备详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMonitorEquipmentDetailByIDAndPollutantType", method = RequestMethod.POST)
    public Object getMonitorEquipmentDetailByIDAndPollutantType(@RequestJson(value = "id", required = true) String id, @RequestJson(value = "pollutanttype", required = true) String pointtype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(pointtype))) {
                case WasteWaterEnum: //废水
                    paramMap.put("detailfieldtype", "detail-water");
                    break;
                case WasteGasEnum: //废气
                    paramMap.put("detailfieldtype", "detail-gas");
                    break;
                case RainEnum: //雨水
                    paramMap.put("detailfieldtype", "detail-rain");
                    break;
                case meteoEnum: //气象
                    paramMap.put("detailfieldtype", "detail-meteorological");
                    break;
                case AirEnum: //空气
                    paramMap.put("detailfieldtype", "detail-air");
                    break;
                case EnvironmentalVocEnum: //voc
                    paramMap.put("detailfieldtype", "detail-voc");
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    paramMap.put("detailfieldtype", "detail-stench");
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    paramMap.put("detailfieldtype", "detail-factorystench");
                    break;
                case EnvironmentalDustEnum: //扬尘
                    paramMap.put("detailfieldtype", "detail-factorydust");
                    break;
                case MicroStationEnum: //微站
                    paramMap.put("detailfieldtype", "detail-microstation");
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    paramMap.put("detailfieldtype", "detail-factorystation");
                    break;
                case WaterQualityEnum: //水质
                    paramMap.put("detailfieldtype", "detail-waterquality");
                    break;
            }
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/05/23  上午 10:20
     * @Description: 获取监测设备初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "getMonitorEquipmentInfoListPage", method = RequestMethod.POST)
    public Object getMonitorEquipmentInfoListPage(HttpServletRequest request ) {
        try {
            //获取userid
            Map<String, Object> datas = new HashMap<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            paramMap.put("queryfieldtype", "query-base");
            String param = AuthUtil.paramDataFormat(paramMap);
            //按钮数据
            paramMap.put("sysmodel", "waterDirectOutlet");//获取父级
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject4 = JSONObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject4.getString("data");
            JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject5.get("topbuttondata")!=null?jsonObject5.getString("topbuttondata"):"";
            String listoperation = jsonObject5.get("tablebuttondata")!=null?jsonObject5.getString("tablebuttondata"):"";
            Map<String, Object> buttondatamap = new HashMap<>();//按钮
            buttondatamap.put("topbuttondata", topoperations);
            buttondatamap.put("tablebuttondata", listoperation);
            // 分页
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = monitorEquipmentService.getMonitorEquipmentsByParamMap(paramMap);
            // 保存分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            // 分页后的数据
            List<Map<String, Object>> listInfo = pageInfo.getList();
            tabledata.put("pagesize", pageInfo.getPageSize());// 每页条数
            tabledata.put("pagenum", pageInfo.getPageNum());// 当前页
            tabledata.put("total", pageInfo.getTotal());// 总条数
            tabledata.put("pages", pageInfo.getPages());// 总页数
            tabledata.put("total", dataList.size());// 总条数
            tabledata.put("primarykey", pk_id);// 主键
            tabledata.put("tablelistdata", dataList);// 数据
            tabledata.put("tabletitledata", null);// 表头
            //返回数据
            datas.put("querydata", null);
            datas.put("tabledata", tabledata);
            datas.put("buttondata", buttondatamap);
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/05  上午 8:47
     * @Description: 根据主键ID获取监测设备修改页面初始化数据（不走通用接口）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMonitorEquipmentInfoByID", method = RequestMethod.POST)
    public Object getMonitorEquipmentInfoByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            MonitorEquipmentVO obj = monitorEquipmentService.getMonitorEquipmentInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success", obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/05  上午 8:47
     * @Description: 根据主键ID和污染物类型获取监测设备详情（不走通用接口）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMonitorEquipmentDetailByID", method = RequestMethod.POST)
    public Object getMonitorEquipmentDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pk_id", id);
            Map<String, Object> result =  monitorEquipmentService.getMonitorEquipmentDetailByID(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/05  上午 8:47
     * @Description: 根据主键ID删除监测设备信息（不走通用接口）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/deleteMonitorEquipmentInfoByID", method = RequestMethod.POST)
    public Object deleteMonitorEquipmentInfoByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            monitorEquipmentService.deleteMonitorEquipmentInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/27  下午 1:15
     * @Description: 根据监测设备名称和监测点ID判断该监测设备是否重复（重复验证）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/isTableDataHaveInfoByMonitorNameAndMonitorPointID", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByMonitorNameAndMonitorPointID(@RequestJson(value = "monitorname", required = true) String monitorname, @RequestJson(value = "monitorpointid", required = true) String monitorcode) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorname", monitorname);
            paramMap.put("fkMonitorpointoroutputid", monitorcode);
            List<Map<String, Object>> value = monitorEquipmentService.isTableDataHaveInfoByMonitorNameAndMonitorPointID(paramMap);
            if (value.size() == 0) {    //等于0 没有此条数据可以添加
                return AuthUtil.parseJsonKeyToLower("success", "no");
            } else {    //已经有了不添加
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
