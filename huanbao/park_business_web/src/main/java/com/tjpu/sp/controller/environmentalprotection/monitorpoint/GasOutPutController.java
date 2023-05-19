package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.controller.common.RabbitmqMongoDBController;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GASOutPutInfoVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum;

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:11
 * @Description: 废气有组织排口控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("gasOutPut")
public class GasOutPutController {
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FileController fileController;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private KeyMonitorPollutantService keyMonitorPollutantService;
    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private RabbitmqMongoDBController rabbitmqMongoDBController;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;


    private String sysmodel = "gasOrganizedOutlet";
    private String pk_id = "pk_id";
    private String listfieldtype = "list-gas";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 上午 9:11
     * @Description: 获取废气有组织排口初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "/getGasOutPutListPage", method = RequestMethod.POST)
    public Object getGasOutPutListPage(HttpServletRequest request) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.putIfAbsent("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:39
     * @Description: 通过自定义参数获取废气有组织排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map]
     * @throws:
     */
    @RequestMapping(value = "/getGasOutPutByParamMap", method = RequestMethod.POST)
    public Object getGasOutPutByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = new JSONObject();
            if (map != null) {
                paramMap = JSONObject.fromObject(map);
            }
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
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:40
     * @Description: 获取废气有组织排口新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getGasOutPutAddPage", method = RequestMethod.POST)
    public Object getGasOutPutAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            paramMap.put("deletefields", new String[]{"water"});

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:40
     * @Description: 新增废气有组织排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addGasOutPut", method = RequestMethod.POST)
    public Object addGasOutPut(@RequestBody Map<String, Object> paramMap) throws Exception {
        try {
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            Object formdata = paramMap.get("formdata");
            Object dgimn = JSONObject.fromObject(formdata).get("dgimn");
            String fk_monitorpointtypecode = JSONObject.fromObject(formdata).get("fk_monitorpointtypecode") == null ? "" : JSONObject.fromObject(formdata).get("fk_monitorpointtypecode").toString();
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if ("success".equals(flag)) {
                //获取username
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                Date now = new Date();
                if (dgimn != null && !"".equals(dgimn.toString())) {
                    DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
                    deviceStatusVO.setDgimn(dgimn.toString());
                    deviceStatusVO.setPkId(UUID.randomUUID().toString());
                    deviceStatusVO.setFkMonitorpointtypecode(fk_monitorpointtypecode);
                    deviceStatusVO.setUpdatetime(now);
                    deviceStatusVO.setUpdateuser(username);
                    deviceStatusService.insert(deviceStatusVO);
                }


                Map<String, Object> params = new HashMap<>();
                params.put("pollutionid", JSONObject.fromObject(formdata).get("fk_pollutionid").toString());
                params.put("outputname", JSONObject.fromObject(formdata).get("outputname").toString());

                GASOutPutInfoVO gasOutPutInfoVO = gasOutPutInfoService.selectByPrimaryKey(jsonObject.getString("data"));
                List<Map<String, Object>> list = keyMonitorPollutantService.selectByPollutanttype(fk_monitorpointtypecode);
                for (Map<String, Object> map : list) {
                    GasOutPutPollutantSetVO gasOutPutPollutantSetVO = new GasOutPutPollutantSetVO();
                    gasOutPutPollutantSetVO.setUpdatetime(now);
                    gasOutPutPollutantSetVO.setUpdateuser(username);
                    gasOutPutPollutantSetVO.setPkDataid(UUID.randomUUID().toString());
                    gasOutPutPollutantSetVO.setFkGasoutputid(gasOutPutInfoVO.getPkId());
                    gasOutPutPollutantSetVO.setFkPollutionid(gasOutPutInfoVO.getFkPollutionid());
                    gasOutPutPollutantSetVO.setFkPollutantcode(map.get("FK_PollutantCode").toString());
                    gasOutPutPollutantSetService.insertPollutants(gasOutPutPollutantSetVO, new ArrayList<>());
                }
            }
            gasOutPutInfoService.deleteGarbageData();

            //发送消息到队列
            sendToMq(jsonObject.getString("data"));

            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:41
     * @Description: 通过id获取废气有组织排口修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getGasOutPutUpdatePageByID", method = RequestMethod.POST)
    public Object getGasOutPutUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(param);
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:42
     * @Description: 修改废气有组织排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateGasOutPut", method = RequestMethod.POST)
    public Object updateGasOutPut(@RequestBody Map<String, Object> paramMap) throws Exception {
        try {
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            Object formdata = paramMap.get("formdata");
            String id = JSONObject.fromObject(formdata).getString("pk_id");
            String dgimn = JSONObject.fromObject(formdata).getString("dgimn");
            String fk_monitorpointtypecode = JSONObject.fromObject(formdata).getString("fk_monitorpointtypecode");
            GASOutPutInfoVO gasOutPutInfoVO = gasOutPutInfoService.selectByPrimaryKey(id);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if ("success".equals(flag) && gasOutPutInfoVO != null) {
                //当废气或烟气排口有类型转换时  清空相关的报警污染物
                String oldtype = gasOutPutInfoVO.getFkMonitorpointtypecode() != null ? gasOutPutInfoVO.getFkMonitorpointtypecode() : "";
                if (!"".equals(fk_monitorpointtypecode) && !"".equals(oldtype)) {
                    if (!fk_monitorpointtypecode.equals(oldtype) && id != null && !"".equals(id)) {
                        //清空之前的报警污染物
                        gasOutPutInfoService.deleteGasOutPutPollutantByID(id);
                    }
                }
                List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(gasOutPutInfoVO.getDgimn());
                String befordgimn = gasOutPutInfoVO.getDgimn();
                //之前没有mn号，现在有mn号新增mn表记录
                if (deviceStatusVOS.size() == 0 && StringUtils.isNotBlank(dgimn)) {
                    DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
                    //获取username

                    String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                    deviceStatusVO.setDgimn(dgimn);
                    deviceStatusVO.setPkId(UUID.randomUUID().toString());
                    deviceStatusVO.setFkMonitorpointtypecode("2");
                    deviceStatusVO.setUpdatetime(new Date());
                    deviceStatusVO.setUpdateuser(username);
                    deviceStatusService.insert(deviceStatusVO);
                }
                if (deviceStatusVOS.size() > 0) {
                    DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);
                    //之前有mn号，现在没有删除之前mn表记录
                    if (StringUtils.isNotBlank(befordgimn) && StringUtils.isBlank(dgimn)) {
                        deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                    }
                    //之前没有，现在没有

                    //之前有，现在有 修改mn表记录
                    if (StringUtils.isNotBlank(befordgimn) && StringUtils.isNotBlank(dgimn)) {
                        //获取username

                        String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                        deviceStatusVO.setDgimn(dgimn);
                        deviceStatusVO.setFkMonitorpointtypecode(fk_monitorpointtypecode);
                        deviceStatusVO.setUpdatetime(new Date());
                        deviceStatusVO.setUpdateuser(username);
                        deviceStatusService.updateByPrimaryKey(deviceStatusVO);
                    }
                }

                userMonitorPointRelationDataService.updataUserMonitorPointRelationDataByMnAndType(gasOutPutInfoVO.getDgimn(), dgimn, gasOutPutInfoVO.getFkMonitorpointtypecode());
                //发送消息到队列
                sendToMq(id);

                //更新MongoDB数据的MN号
                if (!gasOutPutInfoVO.getDgimn().equals(dgimn)) {
                    Map<String, Object> mqMap = new HashMap<>();
                    mqMap.put("monitorpointtype", gasOutPutInfoVO.getFkMonitorpointtypecode());
                    mqMap.put("dgimn", dgimn);
                    mqMap.put("oldMN", gasOutPutInfoVO.getDgimn());
                    rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));
                }
            }
            gasOutPutInfoService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:42
     * @Description: 通过id删除废气有组织排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteGasOutPutByID", method = RequestMethod.POST)
    public Object deleteGasOutPutByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            GASOutPutInfoVO gasOutPutInfoVO = gasOutPutInfoService.selectByPrimaryKey(id);
            UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(id);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if ("success".equals(flag)) {
                //删除状态信息
                if (gasOutPutInfoVO != null && StringUtils.isNotBlank(gasOutPutInfoVO.getDgimn())) {
                    List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(gasOutPutInfoVO.getDgimn());
                    if (deviceStatusVOS.size() > 0) {
                        DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);
                        if (deviceStatusVO != null && StringUtils.isNotBlank(deviceStatusVO.getPkId())) {
                            deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                        }
                    }
                }
                if (gasOutPutInfoVO != null && StringUtils.isNotBlank(gasOutPutInfoVO.getFkImgid())) {
                    List<FileInfoVO> filesInfoByParam = fileInfoService.getFilesInfoByParam(gasOutPutInfoVO.getFkImgid(), null, null);
                    List<String> filePaths = filesInfoByParam.stream().map(m -> m.getFilepath()).collect(Collectors.toList());
                    //删除文件表数据
                    filePaths.stream().peek(m -> fileInfoService.deleteByFilePath(m)).collect(Collectors.toList());
                    //删除mongos数据
                    fileController.deleteFiles(filePaths, "1");
                }
                userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(gasOutPutInfoVO.getDgimn(), gasOutPutInfoVO.getFkMonitorpointtypecode());
                //发送消息到队列
                sendToMq(gasOutPutInfoVO, unorganizedMonitorPointInfoVO);
            }
            gasOutPutInfoService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:58
     * @Description: 通过id查询废气有组织排口详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getGasOutPutDetailByID", method = RequestMethod.POST)
    public Object getGasOutPutDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 1:20
     * @Description: 通过id查询废气有组织排口信息和污染物信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasOutPutAndPollutantDetailByID", method = RequestMethod.POST)
    public Object getGasOutPutAndPollutantDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            List<Map<String, Object>> detailData = new ArrayList<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            Map<String, Object> resultMap = (Map<String, Object>) resultList;
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            if (dataMap.get("detaildata") != null) {
                List<Map<String, Object>> detailDataTemp = (List<Map<String, Object>>) dataMap.get("detaildata");
                detailData = gasOutPutInfoService.setGasOutPutAndPollutantDetail(detailDataTemp, id);
            }
            return AuthUtil.parseJsonKeyToLower("success", detailData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:58
     * @Description: 查询所有废水废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getAllWaterOutputAndGasOutputInfo", method = RequestMethod.GET)
    public Object getAllWaterOutputAndGasOutputInfo() throws Exception {
        try {
            List<Map<String, Object>> allWaterOutputAndGasOutputInfo = gasOutPutInfoService.getAllWaterOutputAndGasOutputInfo();
            List<Map<String, Object>> collect = allWaterOutputAndGasOutputInfo.stream().sorted(Comparator.comparing(m -> m.get("pollutionname").toString())).collect(Collectors.toList());

            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 4:27
     * @Description: 通过排口类型获取排口名称和id type类型1水2气
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "getOutputnameAndidsByType", method = RequestMethod.POST)
    public Object getOutputnameAndidsByType(@RequestJson(value = "type", required = true) Integer type, @RequestJson(value = "pollutionid", required = true) String pollutionid) throws Exception {
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            if (type == 1) {
                data = waterOutPutInfoService.selectByPollutionid(pollutionid);
            } else if (type == 2) {
                data = gasOutPutInfoService.selectByPollutionid(pollutionid);
            }
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/14 0014 上午 10:30
     * @Description: 获取企业排口树，只包含废水，废气，雨水，水质
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointAndPollutionTree", method = RequestMethod.POST)
    public Object getMonitorPointAndPollutionTree(@RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                  @RequestJson(value = "monitorpointname", required = false) String monitorpointname) throws Exception {
        try {
            Map<String, Object> paramMAP = new HashMap<>();
            paramMAP.put("pollutionname", pollutionname);
            paramMAP.put("monitorpointname", monitorpointname);
            List<Map<String, String>> monitorPointAndPollutionTree = gasOutPutInfoService.getMonitorPointAndPollutionTree(paramMAP);
            return AuthUtil.parseJsonKeyToLower("success", monitorPointAndPollutionTree);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/8/1 0001 下午 2:10
     * @Description: 修改污染物推送消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [gasOutPutPollutantSetVO]
     * @throws:
     */
    private void sendToMq(String outputid) {
        GASOutPutInfoVO gasOutPutInfoVO = gasOutPutInfoService.selectByPrimaryKey(outputid);
        UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(outputid);
        if (gasOutPutInfoVO != null) {
            //发送消息到队列
            Map<String, Object> mqMap = new HashMap<>();
            mqMap.put("monitorpointtype", gasOutPutInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn", gasOutPutInfoVO.getDgimn());
            mqMap.put("monitorpointid", gasOutPutInfoVO.getPkId());
            mqMap.put("fkpollutionid", gasOutPutInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        } else {
            //发送消息到队列
            Map<String, Object> mqMap = new HashMap<>();
            mqMap.put("monitorpointtype", unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn", unorganizedMonitorPointInfoVO.getDgimn());
            mqMap.put("monitorpointid", unorganizedMonitorPointInfoVO.getPkId());
            mqMap.put("fkpollutionid", unorganizedMonitorPointInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        }
    }

    private void sendToMq(GASOutPutInfoVO gasOutPutInfoVO, UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO) {

        if (gasOutPutInfoVO != null) {
            //发送消息到队列
            Map<String, Object> mqMap = new HashMap<>();
            mqMap.put("monitorpointtype", gasOutPutInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn", gasOutPutInfoVO.getDgimn());
            mqMap.put("monitorpointid", gasOutPutInfoVO.getPkId());
            mqMap.put("fkpollutionid", gasOutPutInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        } else {
            //发送消息到队列
            Map<String, Object> mqMap = new HashMap<>();
            mqMap.put("monitorpointtype", unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn", unorganizedMonitorPointInfoVO.getDgimn());
            mqMap.put("monitorpointid", unorganizedMonitorPointInfoVO.getPkId());
            mqMap.put("fkpollutionid", unorganizedMonitorPointInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        }
    }
}
