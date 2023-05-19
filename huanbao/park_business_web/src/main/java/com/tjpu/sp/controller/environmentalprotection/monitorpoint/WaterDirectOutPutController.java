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
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutPollutantSetService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.RainEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum;

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:11
 * @Description: 废水直接排口控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("waterDirect")
public class WaterDirectOutPutController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FileController fileController;
    @Autowired
    private KeyMonitorPollutantService keyMonitorPollutantService;
    @Autowired
    private WaterOutPutPollutantSetService waterOutPutPollutantSetService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private RabbitmqMongoDBController rabbitmqMongoDBController;

    private String sysmodel = "waterDirectOutlet";
    private String pk_id = "pk_id";
    private String listfieldtype = "list-directwater";
    private Integer monitorpointtype=WasteWaterEnum.getCode();
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:38
     * @Description: 获取废水直接排口初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "/getWaterDirectOutPutListPage", method = RequestMethod.POST)
    public Object getWaterDirectOutPutListPage(HttpServletRequest request ) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            paramMap.putIfAbsent("sysmodel", sysmodel);


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
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:39
     * @Description: 通过自定义参数获取废水直接排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map]
     * @throws:
     */
    @RequestMapping(value = "/getWaterDirectOutPutByParamMap", method = RequestMethod.POST)
    public Object getWaterDirectOutPutByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
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
     * @Description: 获取废水直接排口新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getWaterDirectOutPutAddPage", method = RequestMethod.POST)
    public Object getWaterDirectOutPutAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            paramMap.put("deletefields", new String[]{"rain"});
            paramMap.put("addfieldtype", "add-waterdirect");

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
     * @Description: 新增废水直接排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addWaterDirectOutPut", method = RequestMethod.POST)
    public Object addWaterDirectOutPut(@RequestBody Map<String,Object> paramMap ) throws Exception {
        try {
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            paramMap.put("deletefields", new String[]{"rain"});
            paramMap.put("addfieldtype", "add-waterdirect");

            Object formdata = paramMap.get("formdata");
            Object dgimn = JSONObject.fromObject(formdata).get("dgimn");

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag)){
                //获取username

                Date now = new Date();
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                if(dgimn != null && !"".equals(dgimn.toString())){
                    DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
                    deviceStatusVO.setDgimn(dgimn.toString());
                    deviceStatusVO.setPkId(UUID.randomUUID().toString());
                    deviceStatusVO.setFkMonitorpointtypecode("1");
                    deviceStatusVO.setUpdatetime(now);
                    deviceStatusVO.setUpdateuser(username);
                    deviceStatusService.insert(deviceStatusVO);
                }


                WaterOutputInfoVO waterOutputInfoVO = waterOutPutInfoService.selectByPrimaryKey(jsonObject.getString("data"));
                List<Map<String, Object>> list = keyMonitorPollutantService.selectByPollutanttype(WasteWaterEnum.getCode()+"");
                for (Map<String, Object> map : list) {
                    WaterOutPutPollutantSetVO waterOutPutPollutantSetVO = new WaterOutPutPollutantSetVO();
                    waterOutPutPollutantSetVO.setUpdatetime(now);
                    waterOutPutPollutantSetVO.setUpdateuser(username);
                    waterOutPutPollutantSetVO.setPkDataid(UUID.randomUUID().toString());
                    waterOutPutPollutantSetVO.setFkWateroutputid(waterOutputInfoVO.getPkId());
                    waterOutPutPollutantSetVO.setFkPollutionid(waterOutputInfoVO.getFkPollutionid());
                    waterOutPutPollutantSetVO.setFkPollutantcode(map.get("FK_PollutantCode").toString());
                    waterOutPutPollutantSetService.insertPollutants(waterOutPutPollutantSetVO,new ArrayList<>());
                }


                sendToMq(waterOutputInfoVO.getPkId());

            }
            waterOutPutInfoService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:41
     * @Description: 通过id获取废水直接排口修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getWaterDirectOutPutUpdatePageByID", method = RequestMethod.POST)
    public Object getWaterDirectOutPutUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            paramMap.put("editfieldtype", "edit-waterdirect");

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
     * @Description: 修改废水直接排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateWaterDirectOutPut", method = RequestMethod.POST)
    public Object updateWaterDirectOutPut(@RequestBody Map<String,Object> paramMap ) throws Exception {
        try {
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            paramMap.put("editfieldtype", "edit-waterdirect");

            Object formdata = paramMap.get("formdata");
            String id = JSONObject.fromObject(formdata).getString("pk_id");
            String dgimn = JSONObject.fromObject(formdata).getString("dgimn");
            WaterOutputInfoVO waterOutputInfoVO = waterOutPutInfoService.selectByPrimaryKey(id);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag) && waterOutputInfoVO != null){
                List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(waterOutputInfoVO.getDgimn());
                String befordgimn = waterOutputInfoVO.getDgimn();
                //之前没有mn号，现在有mn号新增mn表记录
                if(StringUtils.isBlank(befordgimn) && StringUtils.isNotBlank(dgimn)){
                    DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
                    //获取username

                    String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                    deviceStatusVO.setDgimn(dgimn);
                    deviceStatusVO.setPkId(UUID.randomUUID().toString());
                    deviceStatusVO.setFkMonitorpointtypecode("1");
                    deviceStatusVO.setUpdatetime(new Date());
                    deviceStatusVO.setUpdateuser(username);
                    deviceStatusService.insert(deviceStatusVO);
                }

                if(deviceStatusVOS.size()>0){
                    DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);

                    //之前有mn号，现在没有删除之前mn表记录
                    if(deviceStatusVOS.size()==0  && StringUtils.isBlank(dgimn)){
                        deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                    }

                    //之前没有，现在没有

                    //之前有，现在有 修改mn表记录
                    if(StringUtils.isNotBlank(befordgimn) && StringUtils.isNotBlank(dgimn)){

                        //获取username

                        String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                        deviceStatusVO.setDgimn(dgimn);
                        deviceStatusVO.setUpdatetime(new Date());
                        deviceStatusVO.setUpdateuser(username);
                        deviceStatusService.updateByPrimaryKey(deviceStatusVO);
                    }
                }

                userMonitorPointRelationDataService.updataUserMonitorPointRelationDataByMnAndType(waterOutputInfoVO.getDgimn(),dgimn,WasteWaterEnum.getCode()+"");

                sendToMq(waterOutputInfoVO.getPkId());

                //更新MongoDB数据的MN号
				if (!waterOutputInfoVO.getDgimn().equals(dgimn)) {
                    Integer monitopointtype=WasteWaterEnum.getCode();
                    Integer outputtype = waterOutputInfoVO.getOutputtype();
                    if(monitopointtype!=outputtype){
                        monitopointtype=RainEnum.getCode();
                    }
                    Map<String, Object> mqMap = new HashMap<>();
                    mqMap.put("monitorpointtype", monitopointtype);
                    mqMap.put("dgimn", dgimn);
                    mqMap.put("oldMN", waterOutputInfoVO.getDgimn());
                    rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));
                }
            }
            waterOutPutInfoService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:42
     * @Description: 通过id删除废水直接排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteWaterDirectOutPutByID", method = RequestMethod.POST)
    public Object deleteWaterDirectOutPutByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);

            WaterOutputInfoVO waterOutputInfoVO = waterOutPutInfoService.selectByPrimaryKey(id);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag)){
                if(waterOutputInfoVO !=null && StringUtils.isNotBlank(waterOutputInfoVO.getDgimn())){
                    List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(waterOutputInfoVO.getDgimn());
                    if(deviceStatusVOS.size()>0){
                        DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);
                        if(deviceStatusVO!=null && StringUtils.isNotBlank(deviceStatusVO.getPkId())){
                            deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                        }
                    }
                }
                if(waterOutputInfoVO !=null&& StringUtils.isNotBlank(waterOutputInfoVO.getFkImgid())){
                    List<FileInfoVO> filesInfoByParam = fileInfoService.getFilesInfoByParam(waterOutputInfoVO.getFkImgid(), null, null);
                    List<String> filePaths = filesInfoByParam.stream().map(m -> m.getFilepath()).collect(Collectors.toList());
                    //删除文件表数据
                    filePaths.stream().peek(m->fileInfoService.deleteByFilePath(m)).collect(Collectors.toList());
                    //删除mongos数据
                    fileController.deleteFiles(filePaths,"1");
                }
                userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(waterOutputInfoVO.getDgimn(),WasteWaterEnum.getCode()+"");
                sendToMq(waterOutputInfoVO);
            }
            waterOutPutInfoService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:58
     * @Description: 通过id查询废水排口详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getWaterDirectOutPutDetailByID", method = RequestMethod.POST)
    public Object getWaterDirectOutPutDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            paramMap.put("detailfieldtype", "detail-directwater");
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

     /**
      *
      * @author: lip
      * @date: 2019/6/21 0021 下午 3:19
      * @Description: 通过id查询废水直接排口信息和污染物信息详情
      * @updateUser:
      * @updateDate:
      * @updateDescription:
      * @param:
      * @return:
     */
    @RequestMapping(value = "getWaterDirectOutPutAndPollutantDetailByID", method = RequestMethod.POST)
    public Object getWaterDirectOutPutAndPollutantDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            List<Map<String,Object>> detailData = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            Map<String,Object> resultMap = (Map<String, Object>) resultList;
            Map<String,Object> dataMap = (Map<String, Object>) resultMap.get("data");
            if (dataMap.get("detaildata")!=null){
                List<Map<String,Object>> detailDataTemp = (List<Map<String, Object>>) dataMap.get("detaildata");
                Integer monitorPointType = WasteWaterEnum.getCode();
                detailData = waterOutPutInfoService.setWaterOutPutAndPollutantDetail(detailDataTemp,id, monitorPointType);
            }
            return AuthUtil.parseJsonKeyToLower("success",detailData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    private void sendToMq(String outputid){
        WaterOutputInfoVO waterOutputInfoVO = waterOutPutInfoService.selectByPrimaryKey(outputid);
        Integer monitopointtype=WasteWaterEnum.getCode();
        Integer outputtype = waterOutputInfoVO.getOutputtype();
        if(monitopointtype!=outputtype){
            monitopointtype=RainEnum.getCode();
        }
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",monitopointtype);
        mqMap.put("dgimn",waterOutputInfoVO.getDgimn());
        mqMap.put("monitorpointid",waterOutputInfoVO.getPkId());
        mqMap.put("fkpollutionid",waterOutputInfoVO.getFkPollutionid());
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }


    private void sendToMq(WaterOutputInfoVO waterOutputInfoVO){
        Integer monitopointtype=WasteWaterEnum.getCode();
        Integer outputtype = waterOutputInfoVO.getOutputtype();
        if(monitopointtype!=outputtype){
            monitopointtype=RainEnum.getCode();
        }
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",monitopointtype);
        mqMap.put("dgimn",waterOutputInfoVO.getDgimn());
        mqMap.put("monitorpointid",waterOutputInfoVO.getPkId());
        mqMap.put("fkpollutionid",waterOutputInfoVO.getFkPollutionid());
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }



    
}
