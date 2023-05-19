package com.tjpu.sp.controller.environmentalprotection.monitorpoint;


import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.fileconfig.BusinessTypeConfig;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.controller.common.RabbitmqMongoDBController;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.OnlineDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationStandardService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import com.tjpu.sp.service.environmentalprotection.weather.WeatherService;
import com.tjpu.sp.service.impl.environmentalprotection.tracesource.PollutionTraceSourceServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年5月22日 下午3:50:29
 * @Description:其它监测点接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("otherMonitorPoint")
public class OtherMonitorPointController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private KeyMonitorPollutantService keyMonitorPollutantService;
    @Autowired
    private OtherMonitorPointPollutantSetService otherMonitorPointPollutantSetService;
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private PollutionTraceSourceService pollutionTraceSourceService;
    @Autowired
    private NavigationStandardService navigationStandardService;
    @Autowired
    private RabbitmqMongoDBController rabbitmqMongoDBController;
    @Autowired
    private PubCodeService pubCodeService;

    //其它监测点
    private String sysmodel = "othermonitorpoint";
    //voc
    private String vocsysmodel = "vocmonitorpoint";
    //恶臭
    private String stenchsysmodel = "stenchmonitorpoint";
    //微站
    private String microstationsysmodel = "microStationMonitorPoint";
    //微站
    private String dustsysmodel = "dustMonitorList";
    //气象
    private String weathersysmodel = "weatherMonitorPoint";
    //走航
    private String navigationsysmodel = "navigationmonitorpoint";
    private String listfieldtype = "list-othermonitorpoint";
    private String pk_id = "pk_monitorpointid";


    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    @RequestMapping(value = "getOtherControlLevelByParam", method = RequestMethod.POST)
    public Object getOtherControlLevelByParam(
            @RequestJson(value = "monitorpointtypecode") Object monitorpointtypecode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tablename", "PUB_CODE_OtherControlLevel");
            paramMap.put("fields", Arrays.asList("code", "name", "FK_MonitorPointTypeCode", "OrderIndex"));
            paramMap.put("wherestring", "FK_MonitorPointTypeCode=" + monitorpointtypecode);
            paramMap.put("orderfield", "OrderIndex");
            return AuthUtil.parseJsonKeyToLower("success", pubCodeService.getPubCodesDataByParam(paramMap));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/23 下午13:33
     * @Description:获取监测点信息列表页面数据，包含查询控件，以及按钮权限，分页信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request，session]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointsListPage", method = RequestMethod.POST)
    public Object getOtherMonitorPointsListPage(HttpServletRequest request) throws Exception {
        try {
            //获取userid
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("listfieldtype", listfieldtype);
            String fk_monitorpointtypecode = "";
            if (paramMap.get("fk_monitorpointtypecode") != null) {//根据监测类型取不同的配置,
                fk_monitorpointtypecode = paramMap.get("fk_monitorpointtypecode").toString();
                if ((EnvironmentalStinkEnum.getCode() + "").equals(fk_monitorpointtypecode)) {//9:恶臭A
                    paramMap.put("sysmodel", stenchsysmodel);
                } else if ((EnvironmentalVocEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("sysmodel", vocsysmodel);//10:VOC
                } else if ((meteoEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("sysmodel", weathersysmodel);//气象
                    paramMap.put("listfieldtype", "list-meteo");
                } else if ((MicroStationEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("listfieldtype", "list-microstation");
                    paramMap.put("sysmodel", microstationsysmodel);//微型站
                } else if ((EnvironmentalDustEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("listfieldtype", "list-dust");
                    paramMap.put("sysmodel", dustsysmodel);//扬尘
                } else if ((NavigationEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("listfieldtype", "list-navigation");
                    paramMap.put("sysmodel", navigationsysmodel);//走航
                }

            }
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            resultList = AuthUtil.decryptData(resultList);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            if (data2 != null && !"null".equals(data2.toString())) {
                JSONObject jsonObject1 = JSONObject.fromObject(data2);
                Object data3 = jsonObject1.get("tabledata");
                JSONObject jsonObject2 = JSONObject.fromObject(data3);
                List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject2.get("tablelistdata");
                if (!"".equals(fk_monitorpointtypecode)) {
                    pollutantService.orderPollutantDataByParamMap(listdata, "pollutants", Integer.valueOf(fk_monitorpointtypecode));
                }
                jsonObject2.put("tablelistdata", listdata);
                jsonObject1.put("tabledata", jsonObject2);
                jsonObject.put("data", jsonObject1);
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @throws :
     * @author: xsm
     * @date: 2019/5/23 下午13:33
     * @Description: 自定义查询条件查询其它点位信息列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson，pagesize，pagenum]
     * @return:
     */
    @RequestMapping(value = "getOtherMonitorPointsByParamMap", method = RequestMethod.POST)
    public Object getOtherMonitorPointsByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (null != paramsjson) {
                paramMap = JSONObject.fromObject(paramsjson);
            }
            paramMap.put("datasource", datasource);
            paramMap.put("listfieldtype", listfieldtype);
            String fk_monitorpointtypecode = "";
            if (paramMap.get("fk_monitorpointtypecode") != null) {//根据监测类型取不同的配置,
                fk_monitorpointtypecode = paramMap.get("fk_monitorpointtypecode").toString();
                if ("9".equals(fk_monitorpointtypecode)) {//9:恶臭
                    paramMap.put("sysmodel", stenchsysmodel);
                } else if ("10".equals(fk_monitorpointtypecode)) {
                    paramMap.put("sysmodel", vocsysmodel);//10:VOC
                } else if ((meteoEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("listfieldtype", "list-meteo");
                    paramMap.put("sysmodel", weathersysmodel);//气象
                } else if ((MicroStationEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("deletefields", new String[]{"micro"});
                    paramMap.put("sysmodel", microstationsysmodel);//微型站
                } else if ((EnvironmentalDustEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("listfieldtype", "list-dust");
                    paramMap.put("sysmodel", dustsysmodel);//扬尘
                } else if ((NavigationEnum.getCode() + "").equals(fk_monitorpointtypecode)) {
                    paramMap.put("listfieldtype", "list-navigation");
                    paramMap.put("sysmodel", navigationsysmodel);//走航
                }
            }
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(Param);
            resultList = AuthUtil.decryptData(resultList);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("tablelistdata");
            if (!"".equals(fk_monitorpointtypecode)) {
                pollutantService.orderPollutantDataByParamMap(listdata, "pollutants", Integer.valueOf(fk_monitorpointtypecode));
            }
            jsonObject1.put("tablelistdata", listdata);
            jsonObject.put("data", jsonObject1);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/23 下午13:33
     * @Description: 获取其它监测点信息新增页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointAddPage", method = RequestMethod.POST)
    public Object getOtherMonitorPointAddPage(@RequestJson(value = "fk_monitorpointtypecode") Integer monitortype) throws Exception {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//9:恶臭
                paramMap.put("sysmodel", stenchsysmodel);
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                paramMap.put("sysmodel", vocsysmodel);//10:VOC
            } else if (meteoEnum.getCode() == monitortype) {
                paramMap.put("sysmodel", weathersysmodel);//气象
                paramMap.put("addfieldtype", "add-meteo");
            } else if (MicroStationEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", microstationsysmodel);//微型站
            } else if (EnvironmentalDustEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", dustsysmodel);//扬尘
            } else if (EnvironmentalDustEnum.getCode() == monitortype) {
                paramMap.put("sysmodel", navigationsysmodel);//走航
            }
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
     * @date: 2019/5/23 下午13:33
     * @Description: 根据主键ID和监测点类型获取其它监测点信息修改页面的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pk_monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointUpdatePageByID", method = RequestMethod.POST)
    public Object getOtherMonitorPointUpdatePageByID(@RequestJson(value = "id", required = true) String id, @RequestJson(value = "fk_monitorpointtypecode") Integer monitortype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//9:恶臭
                paramMap.put("sysmodel", stenchsysmodel);
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                paramMap.put("sysmodel", vocsysmodel);//10:VOC
            } else if (meteoEnum.getCode() == monitortype) {
                paramMap.put("sysmodel", weathersysmodel);//气象
                paramMap.put("editfieldtype", "edit-meteo");
            } else if (MicroStationEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", microstationsysmodel);//微型站
            } else if (EnvironmentalDustEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", dustsysmodel);//扬尘
            } else if (EnvironmentalDustEnum.getCode() == monitortype) {
                paramMap.put("sysmodel", navigationsysmodel);//走航
            }
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
     * @date: 2019/5/23 下午13:33
     * @Description: 新增其它监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addOtherMonitorPoint", method = RequestMethod.POST)
    public Object addOtherMonitorPoint(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("datasource", datasource);
            String formdata = paramMap.get("formdata").toString();
            JSONObject jsondatas = JSONObject.fromObject(formdata);
            String mnnum = jsondatas.getString("dgimn");
            String monitorpointtype = jsondatas.getString("fk_monitorpointtypecode");
            if ("9".equals(monitorpointtype)) {//9:恶臭
                paramMap.put("sysmodel", stenchsysmodel);
            } else if ("10".equals(monitorpointtype)) {
                paramMap.put("sysmodel", vocsysmodel);//10:VOC
            } else if ((meteoEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", weathersysmodel);//气象
                paramMap.put("addfieldtype", "add-meteo");
            } else if ((MicroStationEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", microstationsysmodel);//微型站
            } else if ((EnvironmentalDustEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", dustsysmodel);//扬尘
            } else if ((NavigationEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", navigationsysmodel);//走航
            }
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if ("success".equals(flag)) {
                if (mnnum != null && !"".equals(mnnum)) {
                    //根据MN号查询状态表中是否有重复数据
                    List<DeviceStatusVO> objlist = deviceStatusService.getDeviceStatusInfosByDgimn(mnnum);
                    //判断MN号是否为空，不为空则进行维护，存入到关系表中
                    if (objlist == null || objlist.size() == 0) {//当不存在重复数据时
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
                params.put("monitorpointtype", monitorpointtype);
                params.put("dgimn", JSONObject.fromObject(formdata).get("dgimn") != null ? JSONObject.fromObject(formdata).get("dgimn").toString() : null);
                params.put("monitorpointname", JSONObject.fromObject(formdata).get("monitorpointname").toString());
                //根据监测点名称和MN号获取新增的那条空气站点信息
                Map<String, Object> map = otherMonitorPointService.selectOtherMonitorPointInfoByParams(params);
                List<Map<String, Object>> list = keyMonitorPollutantService.selectByPollutanttype(monitorpointtype);
                List<OtherMonitorPointPollutantSetVO> otherlist = new ArrayList<>();
                for (Map<String, Object> objmap : list) {
                    OtherMonitorPointPollutantSetVO otherobj = new OtherMonitorPointPollutantSetVO();
                    otherobj.setUpdatetime(new Date());
                    otherobj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
                    otherobj.setPkDataid(UUID.randomUUID().toString());
                    otherobj.setFkOthermonintpointid(map.get("PK_MonitorPointID").toString());
                    otherobj.setFkPollutantcode(objmap.get("FK_PollutantCode").toString());
                    otherlist.add(otherobj);
                }
                //批量添加 将该类型的重点污染物存储到污染物设置（标准）表中
                if (otherlist != null && otherlist.size() > 0) {
                    otherMonitorPointPollutantSetService.insertOtherMonitorPointPollutantSets(otherlist);
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
     * @author: xsm
     * @date: 2019/5/23 下午13:33
     * @Description: 修改其它监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateOtherMonitorPoint", method = RequestMethod.POST)
    public Object updateOtherMonitorPoint(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("datasource", datasource);
            String formdata = paramMap.get("formdata").toString();
            JSONObject jsondatas = JSONObject.fromObject(formdata);
            String monitorpointtype = jsondatas.getString("fk_monitorpointtypecode");
            if ("9".equals(monitorpointtype)) {//9:恶臭
                paramMap.put("sysmodel", stenchsysmodel);
            } else if ("10".equals(monitorpointtype)) {
                paramMap.put("sysmodel", vocsysmodel);//10:VOC
            } else if ((meteoEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", weathersysmodel);//气象
                paramMap.put("editfieldtype", "edit-meteo");
            } else if ((MicroStationEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", microstationsysmodel);//微型站
            } else if ((EnvironmentalDustEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", dustsysmodel);//扬尘
            } else if ((NavigationEnum.getCode() + "").equals(monitorpointtype)) {
                paramMap.put("sysmodel", navigationsysmodel);//走航
            }
            String pkid = jsondatas.get("pk_monitorpointid").toString();
            String newmnnum = jsondatas.getString("dgimn");//修改后的MN号
            parammap.put("pkid", pkid);
            OtherMonitorPointVO otherMonitorPoint = otherMonitorPointService.getOtherMonitorPointByID(pkid);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");

            DeviceStatusVO obj = new DeviceStatusVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setDgimn(newmnnum);
            obj.setFkMonitorpointtypecode(monitorpointtype);

            obj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
            obj.setUpdatetime(new Date());
            //逻辑判断
            if ("success".equals(flag) && otherMonitorPoint != null) {//当修改前对象不为空
                if (otherMonitorPoint.getDgimn() != null && !"".equals(otherMonitorPoint.getDgimn())) {//判断修改前MN号不为空
                    if (newmnnum != null && !"".equals(newmnnum)) {//当修改后的MN号也不为空时
                        //比较两个MN号是否相等，判断MN号是否有修改
                        if (!newmnnum.equals(otherMonitorPoint.getDgimn())) {//当修改前和修改后的MN号不等
                            //根据MN号查询状态表中是否有重复数据
                            List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(otherMonitorPoint.getDgimn());
                            if (oldobjlist != null && oldobjlist.size() > 0) {//当存在重复数据时,删除修改前MN号的状态表数据
                                obj.setStatus(oldobjlist.get(0).getStatus());
                                deviceStatusService.deleteDeviceStatusByMN(otherMonitorPoint.getDgimn());
                            }
                            List<DeviceStatusVO> newobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(newmnnum);
                            if (newobjlist == null || newobjlist.size() == 0) {//当不存在重复数据时

                                deviceStatusService.insert(obj);
                            }
                            //修改点位MN时  批量修改数据权限表中相关点位MN
                            userMonitorPointRelationDataService.updataUserMonitorPointRelationDataByMnAndType(otherMonitorPoint.getDgimn(), newmnnum, monitorpointtype);
                            //更新MongoDB数据的MN号
                            Map<String, Object> mqMap = new HashMap<>();
                            mqMap.put("monitorpointtype", otherMonitorPoint.getFkMonitorpointtypecode());
                            mqMap.put("dgimn", newmnnum);
                            mqMap.put("oldMN", otherMonitorPoint.getDgimn());
                            rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));

                        }
                    } else {
                        //根据MN号查询状态表中是否有重复数据
                        List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(otherMonitorPoint.getDgimn());
                        if (oldobjlist != null && oldobjlist.size() > 0) {//当存在重复数据时,删除修改前MN号的状态表数据
                            deviceStatusService.deleteDeviceStatusByMN(otherMonitorPoint.getDgimn());
                        }
                        //当修改后MN为空  批量删除该MN的数据权限数据
                        //userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(otherMonitorPoint.getDgimn(),monitorpointtype);
                    }
                } else {//修改前MN为空
                    if (newmnnum != null && !"".equals(newmnnum)) {//当修改后的MN号不为空时
                        //根据MN号查询状态表中是否有重复数据
                        List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(newmnnum);
                        if (oldobjlist == null || oldobjlist.size() == 0) {//当不存在重复数据时
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
     * @author: xsm
     * @date: 2019/5/23 下午13:33
     * @Description: 根据其它监测点信息主键ID删除该条记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteOtherMonitorPointByID", method = RequestMethod.POST)
    public Object deleteOtherMonitorPointByID(@RequestJson(value = "id", required = true) String monitorpointid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, monitorpointid);
            paramMap.put("datasource", datasource);
            parammap.put("pkid", monitorpointid);
            Map<String, Object> oldobj = otherMonitorPointService.getOtherMonitorPointDeviceStatusByID(parammap);
            OtherMonitorPointVO otherMonitorPointVO = otherMonitorPointService.getOtherMonitorPointByID(monitorpointid);

            //获取附件表关系
            List<String> fileIds = otherMonitorPointService.getfileIdsByID(parammap);
            String statuspkid = (oldobj != null && oldobj.size() > 0) ? oldobj.get("PK_ID").toString() : "";
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            if (!"".equals(statuspkid)) {//删除关系
                deviceStatusService.deleteByPrimaryKey(statuspkid);
            }
            //删除数据权限表相关点位的数据权限
            userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(otherMonitorPointVO.getDgimn(), otherMonitorPointVO.getFkMonitorpointtypecode());
            //删除附件表关系以及MongoDB数据
            if (fileIds != null && fileIds.size() > 0) {
                MongoDatabase useDatabase = mongoTemplate.getDb();
                String collectionType = BusinessTypeConfig.businessTypeMap.get("1");
                GridFSBucket gridFSBucket = GridFSBuckets.create(useDatabase, collectionType);
                fileInfoService.deleteFilesByParams(fileIds, gridFSBucket);
            }
            //删除点位下的所有视频摄像头信息
            parammap.clear();
            List<Integer> monitorpointtypes = Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()
                    , CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            parammap.put("monitorpointid", monitorpointid);
            parammap.put("monitorpointtypes", monitorpointtypes);
            videoCameraService.deleteVideoCameraByParamMap(parammap);


            //发送消息到队列
            sendToMq(otherMonitorPointVO);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/23 下午13:33
     * @Description: 根据主键ID查询其它监测点信息详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointDetailByID", method = RequestMethod.POST)
    public Object getOtherMonitorPointDetailByID(@RequestJson(value = "id", required = true) String monitorpointid, @RequestJson(value = "fk_monitorpointtypecode") Integer monitortype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//9:恶臭
                paramMap.put("sysmodel", stenchsysmodel);
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                paramMap.put("sysmodel", vocsysmodel);//10:VOC
            } else if (meteoEnum.getCode() == monitortype) {
                paramMap.put("sysmodel", weathersysmodel);//气象
                paramMap.put("detailfieldtype", "detail-meteo");
            } else if (MicroStationEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", microstationsysmodel);//微型站
            } else if (EnvironmentalDustEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", dustsysmodel);//扬尘
            } else if (EnvironmentalDustEnum.getCode() == monitortype) {
                paramMap.put("deletefields", new String[]{"micro"});
                paramMap.put("sysmodel", navigationsysmodel);//走航
            } else {
                paramMap.put("sysmodel", stenchsysmodel);
            }
            paramMap.put(pk_id, monitorpointid);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object result = publicSystemMicroService.getDetail(Param);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/27  下午 1:15
     * @Description: 根据监测点类型判断该监测点名称是否重复（重复验证）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/isTableDataHaveInfoByParamMap", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByParamMap(@RequestJson(value = "monitorpointname", required = true) String monitorpointname,
                                                @RequestJson(value = "fk_monitorpointtypecode", required = true) String monitortyoe
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("fk_monitorpointtypecode", monitortyoe);
            List<Map<String, Object>> value = otherMonitorPointService.isTableDataHaveInfoByParamMap(paramMap);
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

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 上午9:37
     * @Description: 通过监测点名称和监测点类型获取该类型监测点的基础信息及点位状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointInfoAndStateByMonitorPointNameAndType", method = RequestMethod.POST)
    public Object getOtherMonitorPointInfoAndStateByMonitorPointNameAndType(@RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                                            @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> obj : listdata) {
                    if (obj.get("status") != null) {
                        obj.put("status", obj.get("status"));
                    } else {
                        obj.put("status", 0);
                    }
                }
            }
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("listdata", listdata);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午20:54
     * @Description: 根据其它监测点id和监测点类型获取该监测点下监测的所有污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointAllPollutantsByIDAndType", method = RequestMethod.POST)
    public Object getOtherMonitorPointAllPollutantsByIDAndType(@RequestJson(value = "pkids", required = true) List<Object> pkidlist,
                                                               @RequestJson(value = "monitorpointtype", required = true) String monitorpointtype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pkidlist", pkidlist);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> map : listdata) {
                Map<String, Object> objmap = new HashMap<String, Object>();
                objmap.put("labelname", map.get("name"));
                objmap.put("value", map.get("code"));
                objmap.put("standardmaxvalue", map.get("standardmaxvalue"));
                objmap.put("standardminvalue", map.get("standardminvalue"));
                objmap.put("pollutantunit", map.get("PollutantUnit"));
                result.add(objmap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 上午 10:41
     * @Description: 根据日期类型, 监测时间，污染物code查询恶臭和厂界恶臭监测点信息（包含在线监测数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dateType]
     * @throws:
     */
    @RequestMapping(value = "getStenchMonitorPointInfoByParams", method = RequestMethod.POST)
    public Object getStenchMonitorPointInfoByParams(@RequestJson(value = "dateType", required = true) String dateType,
                                                    @RequestJson(value = "stinkflag", required = false) List<Integer> stinkflag,
                                                    @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                    @RequestJson(value = "sortdata", required = false) Object sortdata,
                                                    @RequestJson(value = "monitortime", required = false) String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allcollect = new ArrayList<>();
            List<Map<String, Object>> stenchMonitorPointInfo = new ArrayList<>();
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorpoint") != null) {
                    paramMap.put("sorted", jsonObject.get("monitorpoint").toString());
                }
            }
            if (stinkflag != null) {
                if (stinkflag.size() > 0) {
                    paramMap.put("stinkflag", stinkflag);
                    stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
                } else {
                    return AuthUtil.parseJsonKeyToLower("success", allcollect);
                }
            } else {
                stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
            }
            String dgimns = stenchMonitorPointInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            List<String> mns = stenchMonitorPointInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            //String changjieMN = stenchMonitorPointInfo.stream().filter(m -> m.get("DGIMN") != null && m.get("FK_MonitorPointTypeCode") != null && "40".equals(m.get("FK_MonitorPointTypeCode").toString())).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            //String huanjingMN = stenchMonitorPointInfo.stream().filter(m -> m.get("DGIMN") != null && m.get("FK_MonitorPointTypeCode") != null && "9".equals(m.get("FK_MonitorPointTypeCode").toString())).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            List<Map<String, Object>> colorDataList = getColorData(pollutantcode, paramMap, stenchMonitorPointInfo);
            if (!StringUtils.isNotBlank(monitortime)) {//若时间为空
                monitortime = DataFormatUtil.getDateYMD(new Date());
            }
            List<Document> documents = otherMonitorPointService.getStinkHourOrDayDataByParam(mns, monitortime, dateType);
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            //判断厂界恶臭监测点是否关联空气站，若关联获取其MN号来查询风向风速，若不关联则取自身MN号
            //获取风向信息
            weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);
            for (int i = 0; i < stenchMonitorPointInfo.size(); i++) {
                Map<String, Object> map = stenchMonitorPointInfo.get(i);
                map.put("Status", 0);//初始状态为0
                map.put("AlarmLevel", null);//初始超限级别为空
                if (map.get("DGIMN") != null) {
                    String dgimn = map.get("DGIMN").toString();
                    Integer FK_MonitorPointTypeCode = Integer.valueOf(map.get("FK_MonitorPointTypeCode").toString());
                    List<Document> collect = documents.stream().filter(m -> dgimn.equals(m.getString("DataGatherCode"))).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        Document onedoc = collect.get(0);
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        if ("hour".equals(dateType)) {
                            dataList = (List<Map<String, Object>>) onedoc.get("HourDataList");
                        } else if ("day".equals(dateType)) {
                            dataList = (List<Map<String, Object>>) onedoc.get("DayDataList");
                        }
                        List<Map<String, Object>> collect1 = dataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());
                        if (collect1.size() > 0) {
                            Map<String, Object> map1 = collect1.get(0);
                            if (map1.get("AvgStrength") != null) {
                                Double avgStrength = Double.valueOf(map1.get("AvgStrength").toString());
                                String colorValue = PollutionTraceSourceServiceImpl.getColorValue(FK_MonitorPointTypeCode, avgStrength, colorDataList);
                                map.put("monitorvalue", map1.get("AvgStrength").toString());
                                map.put("colorvalue", colorValue);
                                //判断点位是否超标 超限
                                map.put("Status", 1);
                                if (map1.get("IsOverStandard") != null) {
                                    boolean isoverstandad = (boolean) map1.get("IsOverStandard");
                                    if (isoverstandad) {
                                        map.put("Status", 2);
                                    }
                                }
                                if (map1.get("IsOver") != null) {
                                    int isover = (int) map1.get("IsOver");
                                    if (isover > 0) {
                                        map.put("Status", 2);
                                        map.put("AlarmLevel", isover);
                                    }
                                }
                                if (map1.get("IsException") != null) {
                                    int isexception = (int) map1.get("IsException");
                                    if (isexception > 0) {
                                        map.put("Status", 3);
                                    }
                                }
                            } else {
                                map.put("monitorvalue", "-");
                                map.put("colorvalue", "");
                            }
                        } else {
                            map.put("monitorvalue", "-");
                            map.put("colorvalue", "");
                        }
                    } else {
                        map.put("monitorvalue", "-");
                        map.put("colorvalue", "");
                    }
                    boolean flag = false;
                    for (Map<String, Object> objmap : weatherlist) {
                        if (dgimn.equals(objmap.get("dgimn").toString())) {
                            map.put("winddirectioncode", objmap.get("winddirectioncode") != null ? objmap.get("winddirectioncode") : "");
                            map.put("winddirectionvalue", objmap.get("winddirectionvalue") != null ? objmap.get("winddirectionvalue") : "");
                            map.put("winddirectionname", objmap.get("winddirectionname") != null ? objmap.get("winddirectionname") : "");
                            map.put("windspeed", objmap.get("windspeed") != null ? objmap.get("windspeed") : "");
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        map.put("winddirectioncode", "");
                        map.put("winddirectionvalue", "");
                        map.put("winddirectionname", "");
                        map.put("windspeed", "");
                    }
                }
            }


            String sortvalue = "";
            List<Map<String, Object>> tempdata1 = stenchMonitorPointInfo.stream().filter(m -> m.get("monitorvalue") != null && "-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            List<Map<String, Object>> tempdata2 = stenchMonitorPointInfo.stream().filter(m -> m.get("monitorvalue") != null && !"-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());

            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorvalue") != null) {
                    sortvalue = jsonObject.get("monitorvalue").toString();
                    if ("ascending".equals(sortvalue)) {
                        allcollect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(m.get("monitorvalue").toString()))).collect(Collectors.toList());
                        final int[] temp = {0};
                        List<Map<String, Object>> finalCollect = allcollect;
                        IntStream.range(0, allcollect.size()).mapToObj(m -> m).peek(i -> {
                            if (!finalCollect.get(i).get("monitorvalue").toString().equals("0")) {
                                finalCollect.get(i).put("sortvalue", finalCollect.size() - 1 - temp[0]);
                                temp[0]++;
                            }
                        }).collect(Collectors.toList());
                        allcollect.addAll(tempdata1);
                    } else {
                        allcollect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("monitorvalue").toString())).reversed()).collect(Collectors.toList());
                        final int[] temp = {0};
                        List<Map<String, Object>> finalCollect1 = allcollect;
                        IntStream.range(0, allcollect.size()).mapToObj(m -> m).peek(i -> {
                            if (!finalCollect1.get(i).get("monitorvalue").toString().equals("0")) {
                                finalCollect1.get(i).put("sortvalue", temp[0]);
                                temp[0]++;
                            }
                        }).collect(Collectors.toList());
                        allcollect.addAll(tempdata1);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", allcollect);
                } else {
                    return AuthUtil.parseJsonKeyToLower("success", stenchMonitorPointInfo);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", stenchMonitorPointInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:41
     * @Description: 根据日期类型, 监测时间，污染物code查询扬尘、微站监测点信息（包含在线监测数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dateType]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointInfoByParams", method = RequestMethod.POST)
    public Object getOtherMonitorPointInfoByParams(@RequestJson(value = "dateType") String dateType,
                                                   @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                   @RequestJson(value = "pollutantcode") String pollutantcode,
                                                   @RequestJson(value = "sortdata", required = false) Object sortdata,
                                                   @RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            OnlineDataVO onlineDataVO = new OnlineDataVO();
            List<Map<String, Object>> allcollect;
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> pointList = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            String dgimns = pointList.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.joining(","));
            List<Map<String, Object>> colorDataList = getColorData(pollutantcode, paramMap, pointList);
            onlineDataVO.setDataGatherCode(dgimns);
            Map<String, Object> timeMap = new HashMap<>();
            if (StringUtils.isNotBlank(monitortime)) {
                timeMap.put("starttime", monitortime);
                timeMap.put("endtime", monitortime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            }
            if (StringUtils.isNotBlank(pollutantcode)) {
                List<Map<String, Object>> pollutantList = new ArrayList<>();
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("PollutantCode", pollutantcode);
                pollutantList.add(pollutantMap);
                if ("hour".equals(dateType)) {
                    onlineDataVO.setHourDataList(pollutantList);
                } else if ("day".equals(dateType)) {
                    onlineDataVO.setDayDataList(pollutantList);
                }
            }
            List<OnlineDataVO> listByParam = new ArrayList();
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            //判断厂界恶臭监测点是否关联空气站，若关联获取其MN号来查询风向风速，若不关联则取自身MN号
            if ("hour".equals(dateType)) {
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", "yyyy-MM-dd HH");
                //获取风向信息
                weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);
            } else if ("day".equals(dateType)) {
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", "yyyy-MM-dd");
                //获取风向信息
                weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);
            }

            for (int i = 0; i < pointList.size(); i++) {
                Map<String, Object> map = pointList.get(i);
                if (map.get("dgimn") != null) {
                    String dgimn = map.get("dgimn").toString();
                    Integer FK_MonitorPointTypeCode = Integer.valueOf(map.get("FK_MonitorPointTypeCode").toString());
                    List<OnlineDataVO> collect = listByParam.stream().filter(m -> dgimn.equals(m.getDataGatherCode())).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        OnlineDataVO onlineDataVO1 = collect.get(0);
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        if ("hour".equals(dateType)) {
                            dataList = onlineDataVO1.getHourDataList();
                        } else if ("day".equals(dateType)) {
                            dataList = onlineDataVO1.getDayDataList();
                        }
                        List<Map<String, Object>> collect1 = dataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());
                        if (collect1.size() > 0) {
                            Map<String, Object> map1 = collect1.get(0);
                            if (map1.get("AvgStrength") != null) {
                                Double avgStrength = Double.valueOf(map1.get("AvgStrength").toString());
                                String colorValue = PollutionTraceSourceServiceImpl.getColorValue(FK_MonitorPointTypeCode, avgStrength, colorDataList);
                                map.put("monitorvalue", map1.get("AvgStrength").toString());
                                map.put("colorvalue", colorValue);
                            } else {
                                map.put("monitorvalue", "-");
                                map.put("colorvalue", "");
                            }
                        } else {
                            map.put("monitorvalue", "-");
                            map.put("colorvalue", "");
                        }
                    } else {
                        map.put("monitorvalue", "-");
                        map.put("colorvalue", "");
                    }
                    boolean flag = false;
                    for (Map<String, Object> objmap : weatherlist) {
                        if (dgimn.equals(objmap.get("dgimn").toString())) {
                            map.put("winddirectioncode", objmap.get("winddirectioncode") != null ? objmap.get("winddirectioncode") : "");
                            map.put("winddirectionvalue", objmap.get("winddirectionvalue") != null ? objmap.get("winddirectionvalue") : "");
                            map.put("winddirectionname", objmap.get("winddirectionname") != null ? objmap.get("winddirectionname") : "");
                            map.put("windspeed", objmap.get("windspeed") != null ? objmap.get("windspeed") : "");
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        map.put("winddirectioncode", "");
                        map.put("winddirectionvalue", "");
                        map.put("winddirectionname", "");
                        map.put("windspeed", "");
                    }
                }
            }
            String sortvalue = "";
            List<Map<String, Object>> tempdata1 = pointList.stream().filter(m -> m.get("monitorvalue") != null && "-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            List<Map<String, Object>> tempdata2 = pointList.stream().filter(m -> m.get("monitorvalue") != null && !"-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorvalue") != null) {
                    sortvalue = jsonObject.get("monitorvalue").toString();
                    if ("ascending".equals(sortvalue)) {
                        allcollect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(m.get("monitorvalue").toString()))).collect(Collectors.toList());
                        final int[] temp = {0};
                        List<Map<String, Object>> finalCollect = allcollect;
                        IntStream.range(0, allcollect.size()).mapToObj(m -> m).peek(i -> {
                            if (!finalCollect.get(i).get("monitorvalue").toString().equals("0")) {
                                finalCollect.get(i).put("sortvalue", finalCollect.size() - 1 - temp[0]);
                                temp[0]++;
                            }
                        }).collect(Collectors.toList());
                        allcollect.addAll(tempdata1);
                    } else {
                        allcollect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("monitorvalue").toString())).reversed()).collect(Collectors.toList());
                        final int[] temp = {0};
                        List<Map<String, Object>> finalCollect1 = allcollect;
                        IntStream.range(0, allcollect.size()).mapToObj(m -> m).peek(i -> {
                            if (!finalCollect1.get(i).get("monitorvalue").toString().equals("0")) {
                                finalCollect1.get(i).put("sortvalue", temp[0]);
                                temp[0]++;
                            }
                        }).collect(Collectors.toList());
                        allcollect.addAll(tempdata1);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", allcollect);
                } else {
                    return AuthUtil.parseJsonKeyToLower("success", pointList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", pointList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/4/19 0019 下午 4:51
     * @Description: 获取污染眼溯源颜色标准信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutantcode, paramMap, stenchMonitorPointInfo]
     * @throws:
     */
    private List<Map<String, Object>> getColorData(String pollutantcode, Map<String, Object> paramMap, List<Map<String, Object>> stenchMonitorPointInfo) {
        List<String> monitorpointtypes = stenchMonitorPointInfo.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).map(m -> m.get("FK_MonitorPointTypeCode").toString()).distinct().collect(Collectors.toList());
        paramMap.put("monitorpointtypes", JSONArray.fromObject(monitorpointtypes));
        List<Map<String, Object>> mns = pollutionTraceSourceService.getTraceSourceMonitorPointInfoByParam(paramMap);
        Set<String> airmns = new HashSet<>();//查询点位风向 风速信息
        Set<String> othermns = new HashSet<>();//用来查询该时刻污染物浓度值
        for (Map<String, Object> map : mns) {
            if (map.get("airmn") != null) {
                airmns.add(map.get("airmn").toString());
            }
            othermns.add(map.get("DGIMN").toString());
        }
        paramMap.put("monitorpointtypes", monitorpointtypes);
        paramMap.put("pollutantcode", pollutantcode);
        List<Map<String, Object>> colorDataList = navigationStandardService.getStandardColorDataByParamMap(paramMap);
        return colorDataList;
    }


    @RequestMapping(value = "getMicroStationInfoByParams", method = RequestMethod.POST)
    public Object getMicroStationInfoByParams(
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "dateType", required = true) String dateType,
            @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
            @RequestJson(value = "sortdata", required = false) Object sortdata,
            @RequestJson(value = "monitortime", required = false) String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            OnlineDataVO onlineDataVO = new OnlineDataVO();
            List<Map<String, Object>> allcollect = new ArrayList<>();
            List<Map<String, Object>> stenchMonitorPointInfo = new ArrayList<>();
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorpoint") != null) {
                    paramMap.put("sorted", jsonObject.get("monitorpoint").toString());
                }
            }
            paramMap.put("monitorpointtype", monitorpointtype);

            stenchMonitorPointInfo = otherMonitorPointService.getMicroStationInfo(paramMap);
            String dgimns = stenchMonitorPointInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            onlineDataVO.setDataGatherCode(dgimns);
            Map<String, Object> timeMap = new HashMap<>();
            if (StringUtils.isNotBlank(monitortime)) {
                timeMap.put("starttime", monitortime);
                timeMap.put("endtime", monitortime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            }
            if (StringUtils.isNotBlank(pollutantcode)) {
                List<Map<String, Object>> pollutantList = new ArrayList<>();
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("PollutantCode", pollutantcode);
                pollutantList.add(pollutantMap);
                if ("hour".equals(dateType)) {
                    onlineDataVO.setHourDataList(pollutantList);
                } else if ("day".equals(dateType)) {
                    onlineDataVO.setDayDataList(pollutantList);
                }
            }
            List<OnlineDataVO> listByParam = new ArrayList();
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            //判断厂界恶臭监测点是否关联空气站，若关联获取其MN号来查询风向风速，若不关联则取自身MN号
            if ("hour".equals(dateType)) {
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", "yyyy-MM-dd HH");
                //获取风向信息
                weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);
            } else if ("day".equals(dateType)) {
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", "yyyy-MM-dd");
                //获取风向信息
                weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);

            }

            for (int i = 0; i < stenchMonitorPointInfo.size(); i++) {
                Map<String, Object> map = stenchMonitorPointInfo.get(i);
                if (map.get("DGIMN") != null) {
                    String dgimn = map.get("DGIMN").toString();
                    List<OnlineDataVO> collect = listByParam.stream().filter(m -> dgimn.equals(m.getDataGatherCode())).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        OnlineDataVO onlineDataVO1 = collect.get(0);
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        if ("hour".equals(dateType)) {
                            dataList = onlineDataVO1.getHourDataList();
                        } else if ("day".equals(dateType)) {
                            dataList = onlineDataVO1.getDayDataList();
                        }
                        List<Map<String, Object>> collect1 = dataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());
                        if (collect1.size() > 0) {
                            Map<String, Object> map1 = collect1.get(0);
                            if (map1.get("AvgStrength") != null) {
                                map.put("monitorvalue", map1.get("AvgStrength").toString());
                            } else {
                                map.put("monitorvalue", "-");
                            }
                        } else {
                            map.put("monitorvalue", "-");
                        }
                    } else {
                        map.put("monitorvalue", "-");
                    }
                    boolean flag = false;
                    for (Map<String, Object> objmap : weatherlist) {
                        if (dgimn.equals(objmap.get("dgimn").toString())) {
                            map.put("winddirectioncode", objmap.get("winddirectioncode") != null ? objmap.get("winddirectioncode") : "");
                            map.put("winddirectionvalue", objmap.get("winddirectionvalue") != null ? objmap.get("winddirectionvalue") : "");
                            map.put("winddirectionname", objmap.get("winddirectionname") != null ? objmap.get("winddirectionname") : "");
                            map.put("windspeed", objmap.get("windspeed") != null ? objmap.get("windspeed") : "");
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        map.put("winddirectioncode", "");
                        map.put("winddirectionvalue", "");
                        map.put("winddirectionname", "");
                        map.put("windspeed", "");
                    }
                }
            }


            String sortvalue = "";
            List<Map<String, Object>> tempdata1 = stenchMonitorPointInfo.stream().filter(m -> m.get("monitorvalue") != null && "-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            List<Map<String, Object>> tempdata2 = stenchMonitorPointInfo.stream().filter(m -> m.get("monitorvalue") != null && !"-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());

            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorvalue") != null) {
                    sortvalue = jsonObject.get("monitorvalue").toString();
                    if ("ascending".equals(sortvalue)) {
                        allcollect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(m.get("monitorvalue").toString()))).collect(Collectors.toList());
                        final int[] temp = {0};
                        List<Map<String, Object>> finalCollect = allcollect;
                        IntStream.range(0, allcollect.size()).mapToObj(m -> m).peek(i -> {
                            if (!finalCollect.get(i).get("monitorvalue").toString().equals("0")) {
                                finalCollect.get(i).put("sortvalue", finalCollect.size() - 1 - temp[0]);
                                temp[0]++;
                            }
                        }).collect(Collectors.toList());
                        allcollect.addAll(tempdata1);
                    } else {
                        allcollect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("monitorvalue").toString())).reversed()).collect(Collectors.toList());
                        final int[] temp = {0};
                        List<Map<String, Object>> finalCollect1 = allcollect;
                        IntStream.range(0, allcollect.size()).mapToObj(m -> m).peek(i -> {
                            if (!finalCollect1.get(i).get("monitorvalue").toString().equals("0")) {
                                finalCollect1.get(i).put("sortvalue", temp[0]);
                                temp[0]++;
                            }
                        }).collect(Collectors.toList());
                        allcollect.addAll(tempdata1);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", allcollect);
                } else {
                    return AuthUtil.parseJsonKeyToLower("success", stenchMonitorPointInfo);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", stenchMonitorPointInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 下午 2:21
     * @Description: 通过监测点集合查询恶臭及厂界恶臭污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointids]
     * @throws:
     */
    @RequestMapping(value = "getStenchPollutantMonitorPointids", method = RequestMethod.POST)
    public Object getStenchPollutantMonitorPointids(@RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids) throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointids", monitorpointids);
            List<Map<String, Object>> stenchPollutantMonitorPointids = otherMonitorPointService.getStenchPollutantMonitorPointids(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", stenchPollutantMonitorPointids);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getMicroStationPollutantMonitorPointids", method = RequestMethod.POST)
    public Object getMicroStationPollutantMonitorPointids(
            @RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype
    ) throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointids", monitorpointids);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> stenchPollutantMonitorPointids = otherMonitorPointService.getMicroStationPollutantMonitorPointids(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", stenchPollutantMonitorPointids);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 下午 2:21
     * @Description: 通过监测点mn号, 日期类型，污染物code查询在线
     * @updateUser:xsm
     * @updateDate:2019/7/27 0027 下午2:02
     * @updateDescription:返回风向、风速、超标、异常标记
     * @param: [monitorpointids]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDataByParams", method = RequestMethod.POST)
    public Object getOnlineDataByParams(@RequestJson(value = "dgimn", required = true) String dgimn,
                                        @RequestJson(value = "datetype", required = true) String datetype,
                                        @RequestJson(value = "monitortime", required = false) String monitortime,
                                        @RequestJson(value = "starttime", required = false) String starttime,
                                        @RequestJson(value = "endtime", required = false) String endtime,
                                        @RequestJson(value = "pollutantcode", required = true) String pollutantcode) throws Exception {
        try {
            OnlineDataVO onlineDataVO = new OnlineDataVO();
            DecimalFormat format = new DecimalFormat("0.#");
            onlineDataVO.setDataGatherCode(dgimn);
            List<OnlineDataVO> listByParam = new ArrayList();
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (StringUtils.isNotBlank(pollutantcode)) {
                List<Map<String, Object>> pollutantList = new ArrayList<>();
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("PollutantCode", pollutantcode);
                pollutantList.add(pollutantMap);
                if ("hour".equals(datetype)) {
                    onlineDataVO.setHourDataList(pollutantList);
                } else if ("day".equals(datetype)) {
                    onlineDataVO.setDayDataList(pollutantList);
                }
            }
            Calendar instance = Calendar.getInstance();
            Map<String, Object> timeMap = new HashMap<>();
            String pattern = "";
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            if ("hour".equals(datetype)) {
                if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                    endtime = monitortime;
                    Date date = DataFormatUtil.parseDateYMDH(monitortime);
                    instance.setTime(date);
                    instance.add(Calendar.HOUR, -23);
                    Date time = instance.getTime();
                    starttime = DataFormatUtil.getDateYMDH(time);
                }
                pattern = "yyyy-MM-dd HH";
                timeMap.put("starttime", starttime);
                timeMap.put("endtime", endtime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", pattern);
                weatherlist = weatherService.getWeatherDataByMonitortimesAndMn(datetype, starttime, endtime, dgimn);
            } else if ("day".equals(datetype)) {
                if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                    endtime = monitortime;
                    Date date = DataFormatUtil.parseDateYMD(monitortime);
                    instance.setTime(date);
                    instance.add(Calendar.DAY_OF_YEAR, -6);
                    Date time = instance.getTime();
                    starttime = DataFormatUtil.getDateYMD(time);
                }
                pattern = "yyyy-MM-dd";
                timeMap.put("starttime", starttime);
                timeMap.put("endtime", endtime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", pattern);
                weatherlist = weatherService.getWeatherDataByMonitortimesAndMn(datetype, starttime, endtime, dgimn);
            } else if ("minute".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm";
                timeMap.put("starttime", starttime);
                timeMap.put("endtime", endtime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "MinuteData", pattern);
            } else if ("realtime".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
                timeMap.put("starttime", starttime);
                timeMap.put("endtime", endtime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "RealTimeData", pattern);
            }


            for (OnlineDataVO dataVO : listByParam) {
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> datalist = dataVO.getDayDataList() == null ? dataVO.getHourDataList() : dataVO.getDayDataList();

                List<Map<String, Object>> collect = datalist.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());
                if (collect.size() > 0) {
                    Map<String, Object> map1 = collect.get(0);
                    if (map1.get("AvgStrength") != null) {
                        map.put("monitorvalue", format.format(Float.valueOf(map1.get("AvgStrength").toString())));
                    } else {
                        map.put("monitorvalue", "");
                    }
                    if (map1.get("IsOver") != null) {
                        map.put("isover", map1.get("IsOver"));
                    } else {
                        map.put("isover", "");
                    }
                    if (map1.get("IsException") != null) {
                        map.put("isexception", map1.get("IsException"));
                    } else {
                        map.put("isexception", "");
                    }
                    if (map1.get("IsOverStandard") != null) {
                        map.put("isoverstandard", map1.get("IsOverStandard"));
                    } else {
                        map.put("isoverstandard", "");
                    }
                    String time = OverAlarmController.format(dataVO.getMonitorTime(), pattern);
                    if (weatherlist != null && weatherlist.size() > 0) {
                        boolean flag = false;
                        for (Map<String, Object> objmap : weatherlist) {
                            if (time.equals(objmap.get("monitortime").toString())) {
                                map.put("winddirectioncode", objmap.get("winddirectioncode") != null ? objmap.get("winddirectioncode") : "");
                                map.put("winddirectionvalue", objmap.get("winddirectionvalue") != null ? objmap.get("winddirectionvalue") : "");
                                map.put("winddirectionname", objmap.get("winddirectionname") != null ? objmap.get("winddirectionname") : "");
                                map.put("windspeed", objmap.get("windspeed") != null ? objmap.get("windspeed") : "");
                                flag = true;
                                break;
                            }
                        }
                        if (flag == false) {
                            map.put("winddirectioncode", "");
                            map.put("winddirectionvalue", "");
                            map.put("winddirectionname", "");
                            map.put("windspeed", "");
                        }
                    } else {
                        map.put("winddirectioncode", "");
                        map.put("winddirectionvalue", "");
                        map.put("winddirectionname", "");
                        map.put("windspeed", "");
                    }
                    map.put("monitortime", OverAlarmController.format(dataVO.getMonitorTime(), pattern));
                    resultList.add(map);
                }
            }
            List<Map<String, Object>> collect = resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 上午 10:41
     * @Description: 根据日期类型, 监测时间，污染物code查询Voc监测点信息（包含在线监测数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dateType]
     * @throws:
     */
    @RequestMapping(value = "getVocMonitorPointInfoByParams", method = RequestMethod.POST)
    public Object getVocMonitorPointInfoByParams(@RequestJson(value = "dateType", required = true) String dateType,
                                                 @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                 @RequestJson(value = "sortdata", required = false) Object sortdata,
                                                 @RequestJson(value = "monitortime", required = false) String monitortime) throws Exception {
        try {
            OnlineDataVO onlineDataVO = new OnlineDataVO();
            DecimalFormat format = new DecimalFormat("0.#");
            List<Map<String, Object>> vocMonitorPointInfo = otherMonitorPointService.getAllMonitorEnvironmentalVocAndStatusInfo();

            String dgimns = vocMonitorPointInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            onlineDataVO.setDataGatherCode(dgimns);
            Map<String, Object> timeMap = new HashMap<>();
            if (StringUtils.isNotBlank(monitortime)) {
                timeMap.put("starttime", monitortime);
                timeMap.put("endtime", monitortime);
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            }
            if (StringUtils.isNotBlank(pollutantcode)) {
                List<Map<String, Object>> pollutantList = new ArrayList<>();
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("PollutantCode", pollutantcode);
                pollutantList.add(pollutantMap);
                if ("hour".equals(dateType)) {
                    onlineDataVO.setHourDataList(pollutantList);
                } else if ("day".equals(dateType)) {
                    onlineDataVO.setDayDataList(pollutantList);
                }
            }
            List<OnlineDataVO> listByParam = new ArrayList();
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            if ("hour".equals(dateType)) {
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", "yyyy-MM-dd HH");
                //获取风向信息
                weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);
            } else if ("day".equals(dateType)) {
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", "yyyy-MM-dd");
                //获取风向信息
                weatherlist = weatherService.getWeatherDataByMonitortimeAndMns(dateType, monitortime, dgimns);
            }
            for (int i = 0; i < vocMonitorPointInfo.size(); i++) {
                Map<String, Object> map = vocMonitorPointInfo.get(i);
                if (map.get("DGIMN") != null) {
                    String dgimn = map.get("DGIMN").toString();
                    List<OnlineDataVO> collect = listByParam.stream().filter(m -> dgimn.equals(m.getDataGatherCode())).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        OnlineDataVO onlineDataVO1 = collect.get(0);
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        if ("hour".equals(dateType)) {
                            dataList = onlineDataVO1.getHourDataList();
                        } else if ("day".equals(dateType)) {
                            dataList = onlineDataVO1.getDayDataList();
                        }
                        List<Map<String, Object>> collect1 = dataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());
                        if (collect1.size() > 0) {
                            Map<String, Object> map1 = collect1.get(0);
                            if (map1.get("AvgStrength") != null) {
                                map.put("monitorvalue", format.format(Float.valueOf(map1.get("AvgStrength").toString())));
                            } else {
                                map.put("monitorvalue", "-");
                            }
                        } else {
                            map.put("monitorvalue", "-");
                        }
                    } else {
                        map.put("monitorvalue", "-");
                    }
                    boolean flag = false;
                    for (Map<String, Object> objmap : weatherlist) {
                        if (dgimn.equals(objmap.get("dgimn").toString())) {
                            map.put("winddirectioncode", objmap.get("winddirectioncode") != null ? objmap.get("winddirectioncode") : "");
                            map.put("winddirectionvalue", objmap.get("winddirectionvalue") != null ? objmap.get("winddirectionvalue") : "");
                            map.put("winddirectionname", objmap.get("winddirectionname") != null ? objmap.get("winddirectionname") : "");
                            map.put("windspeed", objmap.get("windspeed") != null ? objmap.get("windspeed") : "");
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        map.put("winddirectioncode", "");
                        map.put("winddirectionvalue", "");
                        map.put("winddirectionname", "");
                        map.put("windspeed", "");
                    }
                }
            }

            String sortvalue = "";
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorvalue") != null) {
                    sortvalue = jsonObject.get("monitorvalue").toString();
                }
            }


            List<Map<String, Object>> tempdata1 = vocMonitorPointInfo.stream().filter(m -> m.get("monitorvalue") != null && "-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            List<Map<String, Object>> tempdata2 = vocMonitorPointInfo.stream().filter(m -> m.get("monitorvalue") != null && !"-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            List<Map<String, Object>> collect = new ArrayList<>();
            if ("ascending".equals(sortvalue)) {
                collect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(m.get("monitorvalue").toString()))).collect(Collectors.toList());
                final int[] temp = {0};
                List<Map<String, Object>> finalCollect = collect;
                IntStream.range(0, collect.size()).mapToObj(m -> m).peek(i -> {
                    if (!finalCollect.get(i).get("monitorvalue").toString().equals("0")) {
                        finalCollect.get(i).put("sortvalue", finalCollect.size() - 1 - temp[0]);
                        temp[0]++;
                    }
                }).collect(Collectors.toList());
            } else {
                collect = tempdata2.stream().filter(m -> m.get("monitorvalue") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("monitorvalue").toString())).reversed()).collect(Collectors.toList());
                final int[] temp = {0};
                List<Map<String, Object>> finalCollect1 = collect;
                IntStream.range(0, collect.size()).mapToObj(m -> m).peek(i -> {
                    if (!finalCollect1.get(i).get("monitorvalue").toString().equals("0")) {
                        finalCollect1.get(i).put("sortvalue", temp[0]);
                        temp[0]++;
                    }
                }).collect(Collectors.toList());

            }
            collect.addAll(tempdata1);
            if ("".equals(sortvalue)) {
                collect = vocMonitorPointInfo.stream().filter(m -> m.get("MonitorPointName") != null).sorted(Comparator.comparing(m -> m.get("MonitorPointName").toString())).collect(Collectors.toList());
            }

            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 上午 9:33
     * @Description: 通过站点类型获取站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pointtype]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointInfoByPointType", method = RequestMethod.POST)
    public Object getOtherMonitorPointInfoByPointType(@RequestJson(value = "pointtype", required = false) String pointtype) throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pointtype", pointtype);
            List<Map<String, Object>> otherMonitorPoint = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", otherMonitorPoint);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 上午 9:33
     * @Description: 通过多个站点类型获取站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pointtype]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfoByPointTypes", method = RequestMethod.POST)
    public Object getMonitorPointInfoByPointTypes(@RequestJson(value = "pointtypes", required = false) Object pointtypes) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> otherMonitorPoint = new ArrayList<>();
            paramMap.put("orderfield", "status");
            if (pointtypes != null) {
                List<Integer> types = (List<Integer>) pointtypes;
                if (types.contains(CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode())) {
                    paramMap.put("monitorpointtypes", pointtypes);
                    otherMonitorPoint = otherMonitorPointService.getTraceSourceMeteoMonitorPointMN(paramMap);
                } else {
                    paramMap.put("pointtypes", pointtypes);
                    otherMonitorPoint = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                }

            }
            return AuthUtil.parseJsonKeyToLower("success", otherMonitorPoint);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 上午 11:57
     * @Description: 获取所有恶臭和voc监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getAllOtherMonitorPoint", method = RequestMethod.POST)
    public Object getAllOtherMonitorPoint() throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", EnvironmentalVocEnum.getCode());

            List<Map<String, Object>> vocinfo = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            paramMap.put("monitortype", EnvironmentalStinkEnum.getCode());
            List<Map<String, Object>> stinkinfo = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);

            vocinfo.addAll(stinkinfo);


            return AuthUtil.parseJsonKeyToLower("success", vocinfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取恶臭监测点信息（厂界+环境）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/10/11 11:24
     */
    @RequestMapping(value = "getAllStinkPointDataList", method = RequestMethod.POST)
    public Object getAllStinkPointDataList() {
        try {
            List<Map<String, Object>> resultList = otherMonitorPointService.getAllStinkPointDataList();

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 上午 9:20
     * @Description: 通过监测点类型获取监测点下拉列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointsByMonitorType", method = RequestMethod.POST)
    public Object getMonitorPointsByMonitorType(@RequestJson(value = "monitortype") Integer monitortype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            if (CommonTypeEnum.getOtherMonitorPointTypeList().contains(monitortype)) {
                paramMap.put("monitortype", monitortype);
                result = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            } else if (AirEnum.getCode() == monitortype) {
                result = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            } else if (WaterQualityEnum.getCode() == monitortype) {
                result = waterStationService.getWaterStationByParamMap(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/9/4 15:24
     * @Description: 获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkMonitorPoint", method = RequestMethod.POST)
    public Object getStinkMonitorPoint() {
        try {
            int code = EnvironmentalStinkEnum.getCode();
            List<Map<String, Object>> otherMonitorPoint = otherMonitorPointService.getStinkMonitorPoint(code);
            return AuthUtil.parseJsonKeyToLower("success", otherMonitorPoint);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/11/2 0002 上午 10:37
     * @Description: 获取传输通道点信息
     * @updateUser:xsm
     * @updateDate:2021/03/25 0025 上午 10:37
     * @updateDescription:获取气象点信息
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getTransportChannelMonitorPointInfos", method = RequestMethod.POST)
    public Object getTransportChannelMonitorPointInfos() throws Exception {
        try {
            List<Map<String, Object>> otherMonitorPoint = otherMonitorPointService.getTransportChannelMonitorPointInfos();
            return AuthUtil.parseJsonKeyToLower("success", otherMonitorPoint);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/4/2 0002 上午 8:47
     * @Description: 获取气象站监测点信息（app）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllMeteoMonitorPointInfoForApp", method = RequestMethod.POST)
    public Object getAllMeteoMonitorPointInfoForApp() throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", meteoEnum.getCode());
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            if (listdata != null && listdata.size() > 0) {
                paramMap.clear();
                paramMap.put("monitorpointname", "工业园区");
                result.add(paramMap);
                result.addAll(listdata);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/4/9 0009 上午 11:00
     * @Description: 通过id删除气象信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "deleteMeteoInfoById", method = RequestMethod.POST)
    public Object deleteMeteoInfoById(@RequestJson(value = "id", required = true) String monitorpointid) {
        try {

            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, monitorpointid);
            paramMap.put("datasource", datasource);
            parammap.put("pkid", monitorpointid);
            parammap.put("monitortype", meteoEnum.getCode());
            Map<String, Object> oldobj = otherMonitorPointService.getOtherMonitorPointDeviceStatusByID(parammap);
            OtherMonitorPointVO otherMonitorPointVO = otherMonitorPointService.getOtherMonitorPointByID(monitorpointid);

            //获取附件表关系
            List<String> fileIds = otherMonitorPointService.getfileIdsByID(parammap);
            String statuspkid = (oldobj != null && oldobj.size() > 0) ? oldobj.get("PK_ID").toString() : "";
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            if (!"".equals(statuspkid)) {//删除关系
                deviceStatusService.deleteByPrimaryKey(statuspkid);
            }
            //删除附件表关系以及MongoDB数据
            if (fileIds != null && fileIds.size() > 0) {
                MongoDatabase useDatabase = mongoTemplate.getDb();
                String collectionType = BusinessTypeConfig.businessTypeMap.get("1");
                GridFSBucket gridFSBucket = GridFSBuckets.create(useDatabase, collectionType);
                fileInfoService.deleteFilesByParams(fileIds, gridFSBucket);
            }
            //删除点位下的所有视频摄像头信息
            parammap.clear();
            List<Integer> monitorpointtypes = Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()
                    , CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            parammap.put("monitorpointid", monitorpointid);
            parammap.put("monitorpointtypes", monitorpointtypes);
            videoCameraService.deleteVideoCameraByParamMap(parammap);


            //发送消息到队列
            sendToMq(otherMonitorPointVO);

            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/8/27 0027 下午 5:31
     * @Description: 修改气象信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateMeteoMonitorPoint", method = RequestMethod.POST)
    public Object updateMeteoMonitorPoint(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("datasource", datasource);
            String formdata = paramMap.get("formdata").toString();
            JSONObject jsondatas = JSONObject.fromObject(formdata);
            paramMap.put("sysmodel", weathersysmodel);//气象
            paramMap.put("editfieldtype", "edit-meteo");

            String pkid = jsondatas.get("pk_monitorpointid").toString();
            String newmnnum = jsondatas.getString("dgimn");//修改后的MN号
            parammap.put("pkid", pkid);
            OtherMonitorPointVO otherMonitorPoint = otherMonitorPointService.getOtherMonitorPointByID(pkid);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            DeviceStatusVO obj = new DeviceStatusVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setDgimn(newmnnum);
            obj.setFkMonitorpointtypecode(meteoEnum.getCode() + "");
            obj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
            obj.setUpdatetime(new Date());
            //逻辑判断
            if ("success".equals(flag) && otherMonitorPoint != null) {//当修改前对象不为空
                if (otherMonitorPoint.getDgimn() != null && !"".equals(otherMonitorPoint.getDgimn())) {//判断修改前MN号不为空
                    List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(otherMonitorPoint.getDgimn());
                    if (oldobjlist.size() > 0) {
                        DeviceStatusVO deviceStatusVO = oldobjlist.get(0);
                        if ((meteoEnum.getCode() + "").equals(deviceStatusVO.getFkMonitorpointtypecode())) {
                            obj.setPkId(deviceStatusVO.getPkId());
                            deviceStatusService.updateByPrimaryKey(obj);

                            //更新MongoDB数据的MN号
                            if (!otherMonitorPoint.getDgimn().equals(newmnnum)) {
                                Map<String, Object> mqMap = new HashMap<>();
                                mqMap.put("monitorpointtype", meteoEnum.getCode());
                                mqMap.put("dgimn", newmnnum);
                                mqMap.put("oldMN", otherMonitorPoint.getDgimn());
                                rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));
                            }
                        }
                    } else {
                        deviceStatusService.insert(obj);
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


    private void sendToMq(String outputid) {
        OtherMonitorPointVO otherMonitorPointVO = otherMonitorPointService.getOtherMonitorPointByID(outputid);
        //发送消息到队列
        Map<String, Object> mqMap = new HashMap<>();
        mqMap.put("monitorpointtype", otherMonitorPointVO.getFkMonitorpointtypecode());
        mqMap.put("dgimn", otherMonitorPointVO.getDgimn());
        mqMap.put("monitorpointid", otherMonitorPointVO.getPkMonitorpointid());
        mqMap.put("fkpollutionid", "");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }

    private void sendToMq(OtherMonitorPointVO otherMonitorPointVO) {
        //发送消息到队列
        Map<String, Object> mqMap = new HashMap<>();
        mqMap.put("monitorpointtype", otherMonitorPointVO.getFkMonitorpointtypecode());
        mqMap.put("dgimn", otherMonitorPointVO.getDgimn());
        mqMap.put("monitorpointid", otherMonitorPointVO.getPkMonitorpointid());
        mqMap.put("fkpollutionid", "");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }
}
