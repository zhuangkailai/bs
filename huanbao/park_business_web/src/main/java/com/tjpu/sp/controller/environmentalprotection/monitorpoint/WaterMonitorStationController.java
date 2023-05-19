package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.fileconfig.BusinessTypeConfig;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.controller.common.RabbitmqMongoDBController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterStationPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterStationService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.RainEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum;

/**
 * @author: liyc
 * @date:2019/9/19 0019 11:18
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("waterMonitorPoint")
public class WaterMonitorStationController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private WaterStationPollutantSetService waterStationPollutantSetService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private KeyMonitorPollutantService keyMonitorPollutantService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private RabbitmqMongoDBController rabbitmqMongoDBController;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;
    //水监测点
    private String sysmodel = "watermonitorpoint";
    private String pk_id = "pk_waterstationid";
    private String listfieldtype = "list-water";
    private String monitorpointtype = String.valueOf(CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());//水质监测点类型(通过枚举获取)
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author:liyc
     * @date:2019/9/20 0020 11:19
     * @Description: 获取水质监测点初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "getWaterMonitorPointsListPage", method = RequestMethod.POST)
    public Object getWaterMonitoringListPage(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String fk_monitorpointtypecode =paramMap.get("fk_monitorpointtypecode")==null?"": paramMap.get("fk_monitorpointtypecode").toString();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("listfieldtype", listfieldtype);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object listByParam = publicSystemMicroService.getListByParam(param);
            listByParam = AuthUtil.decryptData(listByParam);
            JSONObject jsonObject = JSONObject.fromObject(listByParam);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            JSONObject jsonObject2 = JSONObject.fromObject(jsonObject1.get("tabledata"));
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject2.get("tablelistdata");
            if (!"".equals(fk_monitorpointtypecode)) {
                pollutantService.orderPollutantDataByParamMap(listdata, "pollutants", Integer.valueOf(fk_monitorpointtypecode));
            }
            jsonObject2.put("tablelistdata",listdata);
            jsonObject1.put("tabledata",jsonObject2);
            jsonObject.put("data",jsonObject1);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/9/20 0020 13:25
     * @Description: 根据自定义参数获取水质监测点列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getWaterMonitorStationsByParamMap", method = RequestMethod.POST)
    public Object getWaterMonitorStationsByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) throws Exception {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            String fk_monitorpointtypecode =paramMap.get("fk_monitorpointtypecode")==null?"": paramMap.get("fk_monitorpointtypecode").toString();
            Object resultList = publicSystemMicroService.getListData(param);
            resultList = AuthUtil.decryptData(resultList);

            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("tablelistdata");
            pollutantService.orderPollutantDataByParamMap(listdata,"pollutants",Integer.valueOf(fk_monitorpointtypecode));
            jsonObject1.put("tablelistdata",listdata);
            jsonObject.put("data",jsonObject1);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/9/23 0023 10:11
     * @Description: 获取水质监测点新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No such property: code for class: Script1
     * @throws:
     */
    @RequestMapping(value = "getWaterMonitorStationAddPage", method = RequestMethod.POST)
    public Object getWaterMonitorStationAddPage() {
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
     * @author:liyc
     * @date:2019/9/23 0023 17:28
     * @Description: 新增水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No
     * @throws:
     */
    @RequestMapping(value = "addWaterMonitorStation", method = RequestMethod.POST)
    public Object addWaterMonitorStation(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String formdata = paramMap.get("formdata").toString();
            JSONObject jsondatas = JSONObject.fromObject(formdata);
            String mnnum = jsondatas.getString("dgimn");
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if ("success".equals(flag)){
                if (mnnum != null && !"".equals(mnnum)){
                    //根据MN号查询状态表中是否有重复数据
                    List<DeviceStatusVO> objlist = deviceStatusService.getDeviceStatusInfosByDgimn(mnnum);
                    if (objlist==null||objlist.size()==0){ //当不存在重复数据时
                        //判断MN号是否为空，不为空则进行维护，存入到关系表中
                        DeviceStatusVO obj = new DeviceStatusVO();
                        obj.setPkId(UUID.randomUUID().toString());
                        obj.setDgimn(mnnum);
                        obj.setFkMonitorpointtypecode(monitorpointtype);
                        obj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
                        obj.setUpdatetime(new Date());
                        deviceStatusService.insert(obj);
                    }
                }
                Map<String, Object> params = new HashMap<>();
                params.put("dgimn", JSONObject.fromObject(formdata).get("dgimn")!=null?JSONObject.fromObject(formdata).get("dgimn").toString():null);
                params.put("monitorpointname", JSONObject.fromObject(formdata).get("monitorpointname").toString());
                //根据监测点名称和MN号获取新增的那条水质站点信息
                Map<String, Object> map = waterStationService.selectWaterStationInfoByPointNameAndDgimn(params);
                List<Map<String, Object>> list = keyMonitorPollutantService.selectByPollutanttype(CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()+"");
                List<WaterStationPollutantSetVO> waterlist = new ArrayList<>();
                for (Map<String, Object> objmap : list) {
                    WaterStationPollutantSetVO waterobj = new WaterStationPollutantSetVO();
                    waterobj.setUpdatetime(new Date());
                    waterobj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
                    waterobj.setPkDataid(UUID.randomUUID().toString());
                    waterobj.setFkWaterpointid(map.get("PK_WaterStationID").toString());
                    waterobj.setFkPollutantcode(objmap.get("FK_PollutantCode").toString());
                    waterlist.add(waterobj);
                }
                //批量添加 将该类型的重点污染物存储到污染物设置（标准）表中
                if (waterlist != null && waterlist.size() > 0){
                    waterStationPollutantSetService.insertWaterStationPollutantSets(waterlist);
                }


                //发送消息到队列
                sendToMq(jsonObject.getString("data"));
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/9/23 0023 15:21
     * @Description: 根据水质监测点信息主键ID删除单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [id]
     * @throws:
     */
    @RequestMapping(value = "deleteWaterMonitorStationByID", method = RequestMethod.POST)
    public Object deleteWaterMonitorStationByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            parammap.put("pkid",id);
            WaterStationVO waterStationByID = waterStationService.getWaterStationByID(id);

            List<Map<String, Object>> oldobj = waterStationService.getWaterStationDeviceStatusByID(parammap);
            //获取附件表关系
            List<String> fileIds = waterStationService.getfileIdsByID(parammap);
            String statuspkid = (oldobj!=null && oldobj.size()>0)?(oldobj.get(0)).get("PK_ID").toString():"";
            String DGIMN = (oldobj!=null && oldobj.size()>0)?(oldobj.get(0)).get("DGIMN").toString():"";
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag)) {
                if (!"".equals(statuspkid)) {//删除状态表关系
                    deviceStatusService.deleteByPrimaryKey(statuspkid);
                }
                //删除数据权限表相关点位的数据权限
                userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(waterStationByID.getDgimn(),CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()+"");
                //删除附件表关系以及MongoDB数据
                if (fileIds != null && fileIds.size() > 0) {
                    MongoDatabase useDatabase = mongoTemplate.getDb();
                    String collectionType = BusinessTypeConfig.businessTypeMap.get("1");
                    GridFSBucket gridFSBucket = GridFSBuckets.create(useDatabase, collectionType);
                    fileInfoService.deleteFilesByParams(fileIds, gridFSBucket);
                }
                //删除点位下的所有视频摄像头信息
                parammap.clear();
                parammap.put("monitorpointid",id);
                parammap.put("monitorpointtype",CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
                videoCameraService.deleteVideoCameraByParamMap(parammap);


                sendToMq(waterStationByID);
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/9/24 0024 15:22
     * @Description: 根据主键ID获取水质监测点信息修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [id]
     * @throws:
     */
    @RequestMapping(value = "getWaterMonitorStationUpdatePageByID", method = RequestMethod.POST)
    public Object getWaterMonitorStationUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @author:liyc
     * @date:2019/9/23 0023 15:58
     * @Description: 根据水质监测点信息主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [id]
     * @throws:
     */
    @RequestMapping(value = "getWaterMonitorStationDetailByID", method = RequestMethod.POST)
    public Object getWaterMonitorStationDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @author:liyc
     * @date:2019/9/24 0024 15:27
     * @Description: 修改水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateWaterMonitorStation", method = RequestMethod.POST)
    public Object updateWaterMonitorStation(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String formdata=paramMap.get("formdata").toString();
            JSONObject jsondatas = JSONObject.fromObject(formdata);
            String pkid =jsondatas.get("pk_waterstationid").toString();
            String newmnnum = jsondatas.getString("dgimn");//修改后的MN号
            parammap.put("pkid",pkid);
            WaterStationVO waterStation = waterStationService.getWaterStationByID(pkid);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            //将页面修改信息set进实体对象中
            DeviceStatusVO obj = new DeviceStatusVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setDgimn(newmnnum);
            obj.setFkMonitorpointtypecode(monitorpointtype);
            obj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
            obj.setUpdatetime(new Date());
            if("success".equals(flag) && waterStation != null){ //当修改前对象不为空
                if (waterStation.getDgimn() != null && !"".equals(waterStation.getDgimn())){ //判断修改前MN号不为空
                    if (newmnnum!=null&&!"".equals(newmnnum)){ //当修改后的MN号也不为空时
                        //比较两个MN号是否相等，判断MN号是否有修改
                        if (!newmnnum.equals(waterStation.getDgimn())){//当修改前和修改后的MN号不等
                            //根据MN号查询状态表中是否有重复数据
                            List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(waterStation.getDgimn());
                            if (oldobjlist!=null&&oldobjlist.size()>0) {//当存在重复数据时,删除修改前MN号的状态表数据
                                obj.setStatus(oldobjlist.get(0).getStatus());
                                deviceStatusService.deleteDeviceStatusByMN(waterStation.getDgimn());
                            }
                            List<DeviceStatusVO> newobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(newmnnum);
                            if (newobjlist==null||newobjlist.size()==0) {//当不存在重复数据时
                                deviceStatusService.insert(obj);
                            }
                            //修改点位MN时  批量修改数据权限表中相关点位MN
                            userMonitorPointRelationDataService.updataUserMonitorPointRelationDataByMnAndType(waterStation.getDgimn(),newmnnum,monitorpointtype);

                            //更新MongoDB数据的MN号
                            Map<String, Object> mqMap = new HashMap<>();
                            mqMap.put("monitorpointtype", monitorpointtype);
                            mqMap.put("oldMN", waterStation.getDgimn());
                            mqMap.put("dgimn", newmnnum);
                            rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));
                        }

                    }else {
                        //根据MN号查询状态表中是否有重复数据
                        List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(waterStation.getDgimn());
                        if (oldobjlist!=null&&oldobjlist.size()>0) {//当存在重复数据时,删除修改前MN号的状态表数据
                            deviceStatusService.deleteDeviceStatusByMN(waterStation.getDgimn());
                        }
                    }
                }else { //修改前MN为空
                    if (newmnnum!=null&&!"".equals(newmnnum)){//当修改后的MN号不为空时
                        //根据MN号查询状态表中是否有重复数据
                        List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(waterStation.getDgimn());
                        if (oldobjlist==null||oldobjlist.size()==0){//当不存在重复数据时
                            //判断MN号是否为空，不为空则进行维护，存入到关系表中
                            deviceStatusService.insert(obj);
                        }
                    }
                }


                //发送消息到队列
                sendToMq(pkid);
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/25 0025 9:16
     * @Description: 根据水质监测点id获取该监测点下监测的所有污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getWaterStationAllPollutantsByID", method = RequestMethod.POST)
    public Object getWaterStationAllPollutantsByID(@RequestJson(value = "pkids", required = true) List<Object> pkidlist) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pkidlist", pkidlist);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listdata = waterStationPollutantSetService.getWaterStationAllPollutantsByIDAndType(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> map : listdata) {
                Map<String, Object> objmap = new HashMap<String, Object>();
                objmap.put("labelname", map.get("name"));
                objmap.put("value", map.get("code"));
                objmap.put("standardmaxvalue", map.get("standardmaxvalue"));
                objmap.put("standardminvalue", map.get("standardminvalue"));
                objmap.put("pollutantunit", map.get("pollutantunit"));
                result.add(objmap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/26 0026 13:17
     * @Description: 通过水质监测站点id获取该监测点的污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [outputid, session]
     * @throws:
     */
    @RequestMapping(value = "getWaterPollutantsByWaterId", method = RequestMethod.POST)
    public Object getWaterPollutantsByWaterId(@RequestJson(value = "outputid") String outputid, HttpSession session) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            params.put("userid", userid);
            params.put("sysmodel", "watermonitorpoint");
            params.put("queryfieldtype", "query-water");
            //微服务参数
            params.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(params);
            //查询条件数据
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = JSONObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            if (StringUtils.isNotBlank(queryData) && !"null".equals(queryData)) {
                JSONObject jsonObject3 = JSONObject.fromObject(queryData);
                String querycontroldata = jsonObject3.get("querycontroldata")==null?"":jsonObject3.getString("querycontroldata");
                String queryformdata = jsonObject3.get("queryformdata")==null?"":jsonObject3.getString("queryformdata");
                resultMap.put("queryformdata", queryformdata);
                resultMap.put("querycontroldata", querycontroldata);
            }
            //获取用户在菜单上拥有的按钮权限信息
            Object buttonAuth = publicSystemMicroService.getUserButtonAuthInMenu(param);
            JSONObject jsonObject4 = JSONObject.fromObject(buttonAuth);
            String buttonData = jsonObject4.getString("data");
            if (StringUtils.isNotBlank(buttonData) && !"null".equals(buttonData)) {
                JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
                String topbuttondata = jsonObject5.get("topbuttondata")==null?"":jsonObject5.getString("topbuttondata");
                String tablebuttondata = jsonObject5.get("tablebuttondata")==null?"":jsonObject5.getString("tablebuttondata");
                resultMap.put("topbuttondata", topbuttondata);
                resultMap.put("tablebuttondata", tablebuttondata);
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            dataList = waterStationPollutantSetService.getWaterPollutantsByParamMap(paramMap);
            WaterPollutantSetController.getName(dataList);
            resultMap.put("tablelistdata", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/26 0026 15:47
     * @Description: 通过主键id，污染物code和水质站点id删除水质监测站污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, outputid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "deleteWaterPollutantByParams", method = RequestMethod.POST)
    public Object deleteWaterPollutantByParams(@RequestJson(value = "id") String id, @RequestJson(value = "outputid") String outputid,
                                               @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            paramMap.put("pollutantcode", pollutantcode);
            WaterStationVO waterStationByID = waterStationService.getWaterStationByID(outputid);
            waterStationPollutantSetService.deletePollutants(id, paramMap);

            sendToMq(waterStationByID);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/27 0027 8:50
     * @Description: 通过自定义参数修改水质站污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, session]
     * @throws:
     */
    @RequestMapping(value = "updateWaterPollutantByParamMap", method = RequestMethod.POST)
    public Object updateWaterPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson, HttpSession session) throws Exception {
        try {
            if (paramsjson != null) {
                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                WaterStationPollutantSetVO waterStationPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WaterStationPollutantSetVO());
                String sessionId = session.getId();
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                waterStationPollutantSetVO.setUpdatetime(new Date());
                waterStationPollutantSetVO.setUpdateuser(username);
                waterStationPollutantSetVO.setFkWaterpointid(jsonObject.getString("fk_waterpointid"));
                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList = new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if (concenalarmmaxvalue != null || concenalarmminvalue != null || StringUtils.isNotBlank(fkAlarmlevelcode)) {
                        earlyWarningSetVO.setFkOutputid(waterStationPollutantSetVO.getFkWaterpointid());
                        earlyWarningSetVO.setFkPollutantcode(waterStationPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        paramList.add(earlyWarningSetVO);
                    }
                }
                waterStationPollutantSetService.updatePollutants(waterStationPollutantSetVO, paramList);

                sendToMq(waterStationPollutantSetVO.getFkWaterpointid());
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/10/9 0009 10:59
     * @Description: 通过自定义参数新增水质站污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, session]
     * @throws:
     */
    @RequestMapping(value = "addWaterPollutantByParamMap", method = RequestMethod.POST)
    public Object addWaterPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson, HttpSession session) throws Exception {
        try {
            if (paramsjson != null) {
                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                WaterStationPollutantSetVO waterStationPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WaterStationPollutantSetVO());
                waterStationPollutantSetVO.setPkDataid(UUID.randomUUID().toString());
                String sessionId = session.getId();
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                waterStationPollutantSetVO.setUpdatetime(new Date());
                waterStationPollutantSetVO.setUpdateuser(username);
                waterStationPollutantSetVO.setFkWaterpointid(jsonObject.getString("fk_waterpointid"));
                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList = new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    earlyWarningSetVO.setFkOutputid(waterStationPollutantSetVO.getFkWaterpointid());
                    earlyWarningSetVO.setFkPollutantcode(waterStationPollutantSetVO.getFkPollutantcode());
                    earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                    paramList.add(earlyWarningSetVO);
                }
                waterStationPollutantSetService.insertPollutants(waterStationPollutantSetVO, paramList);


                sendToMq(waterStationPollutantSetVO.getFkWaterpointid());
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/10/10 0009 14:07
     * @Description: 通过水质站点id和污染物code水质站污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByWateridAndPollutantcode", method = RequestMethod.POST)
    public Object getPollutantsByWateridAndPollutantcode(
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> dataList = waterStationPollutantSetService.getWaterPollutantByParamMap(paramMap);
            WaterPollutantSetController.getName(dataList);
            String data = JSONArray.fromObject(dataList).toString();
            return AuthUtil.parseJsonKeyToLower("success", data.replaceAll("_", "").toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/10/12 0012 10:42
     * @Description: 验证传入数据是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_waterpointid,fk_pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "isTableDataHaveInfo", method = RequestMethod.POST)
    public Object isTableDataHaveInfo(@RequestJson(value = "fk_waterpointid") String fk_waterpointid,
                                      @RequestJson(value = "fk_pollutantcode") String fk_pollutantcode
                                     ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fk_waterpointid", fk_waterpointid);
            paramMap.put("fk_pollutantcode", fk_pollutantcode);
            int tableDataHaveInfo = waterStationPollutantSetService.isTableDataHaveInfo(paramMap);
            if (tableDataHaveInfo==0){
                return AuthUtil.parseJsonKeyToLower("success","no");
            }else { //已经有了不添加
                return AuthUtil.parseJsonKeyToLower("success","yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void sendToMq(String outputid){
        WaterStationVO waterStationByID = waterStationService.getWaterStationByID(outputid);
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",monitorpointtype);
        mqMap.put("dgimn",waterStationByID.getDgimn());
        mqMap.put("monitorpointid",waterStationByID.getPkWaterstationid());
        mqMap.put("fkpollutionid","");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }
    private void sendToMq(WaterStationVO waterStationByID){
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",monitorpointtype);
        mqMap.put("dgimn",waterStationByID.getDgimn());
        mqMap.put("monitorpointid",waterStationByID.getPkWaterstationid());
        mqMap.put("fkpollutionid","");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }

}
