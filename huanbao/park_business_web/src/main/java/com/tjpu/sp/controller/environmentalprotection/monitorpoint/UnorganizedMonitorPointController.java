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
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
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

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:11
 * @Description: 废气有组织排口控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("unorganizedMonitorPoint")
public class UnorganizedMonitorPointController {
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FileController fileController;
    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    @Autowired
    private KeyMonitorPollutantService keyMonitorPollutantService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private RabbitmqMongoDBController rabbitmqMongoDBController;


    private String sysmodel = "gasInorganizationMonitor";
    private String pk_id = "pk_id";
    private String listfieldtype = "list-UnorganizedMonitorPoint";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 上午 9:11
     * @Description: 获取废气无组织排口初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "/getUnorMonitorPointListPage", method = RequestMethod.POST)
    public Object getUnorMonitorPointListPage(HttpServletRequest request ) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
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
     * @Description: 通过自定义参数获取废气无组织排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map]
     * @throws:
     */
    @RequestMapping(value = "/getUnorMonitorPointByParamMap", method = RequestMethod.POST)
    public Object getUnorMonitorPointByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) throws Exception {
        try {
            JSONObject paramMap = new JSONObject();
            if (map != null) {
                paramMap = JSONObject.fromObject(map);
            }
            if(paramMap.get("listfieldtype")==null || "".equals(paramMap.get("listfieldtype").toString())){
                paramMap.put("listfieldtype", listfieldtype);
            }else {
                paramMap.put("listfieldtype", paramMap.get("listfieldtype"));
            }
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String fk_monitorpointtypecode = paramMap.get("fk_monitorpointtypecode")==null?"":paramMap.get("fk_monitorpointtypecode").toString();

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(param);
            resultList = AuthUtil.decryptData(resultList);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("tablelistdata");
            if (!"".equals(fk_monitorpointtypecode)) {
                pollutantService.orderPollutantDataByParamMap(listdata, "pollutantname", Integer.valueOf(fk_monitorpointtypecode));
            }
            jsonObject1.put("tablelistdata",listdata);
            jsonObject.put("data",jsonObject1);
            return jsonObject;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:40
     * @Description: 获取废气无组织排口新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getUnorMonitorPointAddPage", method = RequestMethod.POST)
    public Object getUnorMonitorPointAddPage() {
        try {
            //设置参数
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
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:40
     * @Description: 新增废气无组织排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addUnorMonitorPoint", method = RequestMethod.POST)
    public Object addUnorMonitorPoint(@RequestBody Map<String,Object> paramMap ) throws Exception {
        try {
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);

            Object formdata = paramMap.get("formdata");
            Object dgimn = JSONObject.fromObject(formdata).get("dgimn");

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag) ){
                //获取username

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                Date now = new Date();
                if( dgimn != null && !"".equals(dgimn.toString())){
                    DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
                    deviceStatusVO.setDgimn(dgimn.toString());
                    deviceStatusVO.setPkId(UUID.randomUUID().toString());
                    if(JSONObject.fromObject(formdata).get("fk_monitorpointtypecode")!=null){
                        deviceStatusVO.setFkMonitorpointtypecode(JSONObject.fromObject(formdata).get("fk_monitorpointtypecode").toString());
                    }
                    deviceStatusVO.setUpdatetime(now);
                    deviceStatusVO.setUpdateuser(username);
                    deviceStatusService.insert(deviceStatusVO);
                }


                UnorganizedMonitorPointInfoVO data = outPutUnorganizedService.selectByPrimaryKey(jsonObject.getString("data"));
                List<Map<String, Object>> list = keyMonitorPollutantService.selectByPollutanttype(data.getFkMonitorpointtypecode());
                for (Map<String, Object> map : list) {
                    GasOutPutPollutantSetVO gasOutPutPollutantSetVO = new GasOutPutPollutantSetVO();
                    gasOutPutPollutantSetVO.setUpdatetime(now);
                    gasOutPutPollutantSetVO.setUpdateuser(username);
                    gasOutPutPollutantSetVO.setPkDataid(UUID.randomUUID().toString());
                    gasOutPutPollutantSetVO.setFkGasoutputid(data.getPkId());
                    gasOutPutPollutantSetVO.setFkPollutionid(data.getFkPollutionid());
                    gasOutPutPollutantSetVO.setFkPollutantcode(map.get("FK_PollutantCode").toString());
                    gasOutPutPollutantSetService.insertPollutants(gasOutPutPollutantSetVO,new ArrayList<>());
                }

                sendToMq(jsonObject.getString("data"));
            }
            outPutUnorganizedService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:41
     * @Description: 通过id获取废气无组织排口修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getUnorMonitorPointUpdatePageByID", method = RequestMethod.POST)
    public Object getUnorMonitorPointUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @Description: 修改废气无组织排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateUnorMonitorPoint", method = RequestMethod.POST)
    public Object updateUnorMonitorPoint(@RequestBody Map<String,Object> paramMap ) throws Exception {
        try {
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);

            Object formdata = paramMap.get("formdata");
            String id = JSONObject.fromObject(formdata).getString("pk_id");
            String dgimn = JSONObject.fromObject(formdata).getString("dgimn");
            UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(id);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag) && unorganizedMonitorPointInfoVO != null){

                List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(unorganizedMonitorPointInfoVO.getDgimn());
                String befordgimn = unorganizedMonitorPointInfoVO.getDgimn();
                //之前没有mn号，现在有mn号新增mn表记录
                if(deviceStatusVOS.size()==0  && StringUtils.isNotBlank(dgimn)){
                    DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
                    //获取username

                    String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                    deviceStatusVO.setDgimn(dgimn);
                    deviceStatusVO.setPkId(UUID.randomUUID().toString());
                    if(JSONObject.fromObject(formdata).get("fk_monitorpointtypecode")!=null){
                        deviceStatusVO.setFkMonitorpointtypecode(JSONObject.fromObject(formdata).get("fk_monitorpointtypecode").toString());
                    }
                    deviceStatusVO.setUpdatetime(new Date());
                    deviceStatusVO.setUpdateuser(username);
                    deviceStatusService.insert(deviceStatusVO);
                }

                if(deviceStatusVOS.size()>0){
                    DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);

                    //之前有mn号，现在没有删除之前mn表记录
                    if(StringUtils.isNotBlank(befordgimn) && StringUtils.isBlank(dgimn)){
                        deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                    }

                    //之前没有，现在没有

                    //之前有，现在有 修改mn表记录
                    if(StringUtils.isNotBlank(befordgimn) && StringUtils.isNotBlank(dgimn)){

                        //获取username

                        String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                        deviceStatusVO.setDgimn(dgimn);
                        deviceStatusVO.setUpdatetime(new Date());
                        deviceStatusVO.setUpdateuser(username);
                        deviceStatusService.updateByPrimaryKey(deviceStatusVO);
                    }
                }
                userMonitorPointRelationDataService.updataUserMonitorPointRelationDataByMnAndType(unorganizedMonitorPointInfoVO.getDgimn(),dgimn,unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
                sendToMq(id);

                //更新MongoDB数据的MN号
				if (!unorganizedMonitorPointInfoVO.getDgimn().equals(dgimn)) {
                    Map<String, Object> mqMap = new HashMap<>();
                    mqMap.put("monitorpointtype", unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
                    mqMap.put("oldMN", unorganizedMonitorPointInfoVO.getDgimn());
                    mqMap.put("dgimn", dgimn);
                    rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));
                }
            }
            outPutUnorganizedService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:42
     * @Description: 通过id删除废气无组织排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteUnorMonitorPointByID", method = RequestMethod.POST)
    public Object deleteUnorMonitorPointByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);

            UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(id);


            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(param);

            JSONObject jsonObject = JSONObject.fromObject(resultList);
            String flag = jsonObject.getString("flag");
            if("success".equals(flag)){
                if(unorganizedMonitorPointInfoVO !=null && StringUtils.isNotBlank(unorganizedMonitorPointInfoVO.getDgimn())){
                    List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(unorganizedMonitorPointInfoVO.getDgimn());
                    if(deviceStatusVOS.size()>0){
                        DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);
                        if(deviceStatusVO!=null && StringUtils.isNotBlank(deviceStatusVO.getPkId())){
                            deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                        }
                    }
                }
                if(unorganizedMonitorPointInfoVO !=null&& StringUtils.isNotBlank(unorganizedMonitorPointInfoVO.getFkImgid())){
                    List<FileInfoVO> filesInfoByParam = fileInfoService.getFilesInfoByParam(unorganizedMonitorPointInfoVO.getFkImgid(), null, null);
                    List<String> filePaths = filesInfoByParam.stream().map(m -> m.getFilepath()).collect(Collectors.toList());
                    //删除文件表数据
                    filePaths.stream().peek(m->fileInfoService.deleteByFilePath(m)).collect(Collectors.toList());
                    //删除mongos数据
                    fileController.deleteFiles(filePaths,"1");
                }

                userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(unorganizedMonitorPointInfoVO.getDgimn(),unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
                sendToMq(unorganizedMonitorPointInfoVO);
            }
            outPutUnorganizedService.deleteGarbageData();
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:58
     * @Description: 通过id查询废气无组织排口详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getUnorMonitorPointDetailByID", method = RequestMethod.POST)
    public Object getUnorMonitorPointDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     *
     * @author: lip
     * @date: 2019/6/21 0021 下午 3:02
     * @Description: 通过id查询废气无组织排口信息和污染物信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "getUnorMonitorPointAndPollutantDetailByID", method = RequestMethod.POST)
    public Object getUnorMonitorPointAndPollutantDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
                detailData = outPutUnorganizedService.setUNGasOutPutAndPollutantDetail(detailDataTemp,id);
            }
            return AuthUtil.parseJsonKeyToLower("success",detailData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 上午 9:11
     * @Description: 通过监测点类型获取废气无组织排口初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "/getUnorMonitorPointListPageByType", method = RequestMethod.POST)
    public Object getUnorMonitorPointListPageByType(HttpServletRequest request ) throws Exception {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String fk_monitorpointtypecode = paramMap.get("fk_monitorpointtypecode")==null?"" : paramMap.get("fk_monitorpointtypecode").toString();
            if(!paramMap.containsKey("sysmodel")){
                paramMap.put("sysmodel", sysmodel);
            }
            paramMap.put("userid", userId);
            if(paramMap.get("listfieldtype")==null || "".equals(paramMap.get("listfieldtype").toString())){
                paramMap.put("listfieldtype", listfieldtype);
            }else{
                paramMap.put("listfieldtype", paramMap.get("listfieldtype"));
            }
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            resultList = AuthUtil.decryptData(resultList);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            JSONObject jsonObject2 = JSONObject.fromObject(jsonObject1.get("tabledata"));
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject2.get("tablelistdata");
            if (!"".equals(fk_monitorpointtypecode)) {
                pollutantService.orderPollutantDataByParamMap(listdata, "pollutantname", Integer.valueOf(fk_monitorpointtypecode));
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
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:40
     * @Description: 通过新增类型获取废气无组织排口新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getUnorMonitorPointAddPageByAddtype", method = RequestMethod.POST)
    public Object getUnorMonitorPointAddPageByAddtype(@RequestJson(value = "addfieldtype",required = false) String addfieldtype) {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            if(StringUtils.isNotBlank(addfieldtype)){
                paramMap.put("addfieldtype", addfieldtype);
            }

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            if (resultList!=null){
                JSONObject jsonObject = JSONObject.fromObject(resultList);
                if (jsonObject.get("data")!=null){
                    JSONObject data = jsonObject.getJSONObject("data");
                    if (data.get("addcontroldata")!=null) {
                        List<Map<String,Object>> addcontroldata = (List<Map<String, Object>>) data.get("addcontroldata");
                        for (Map<String,Object> map: addcontroldata){
                            if (map.get("name")!=null&&"fk_pollutionid".equals(map.get("name").toString())){
                                map.put("filterable",true);
                                map.put("defaultfirstoption",true);
                                break;
                            }
                        }
                    }
                }
                return jsonObject;
            }else {
                return resultList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/26 0026 下午 9:41
     * @Description: 通过id,修改类型获取废气无组织排口修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getUnorMonitorUpdatePageByIDAndEdittype", method = RequestMethod.POST)
    public Object getUnorMonitorUpdatePageByIDAndEdittype(@RequestJson(value = "id", required = true) String id,
                                                          @RequestJson(value = "editfieldtype",required = false) String editfieldtype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            if(StringUtils.isNotBlank(editfieldtype)){
                paramMap.put("editfieldtype", editfieldtype);
            }
            paramMap.put("datasource", datasource);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(param);
            if (resultList!=null){
                JSONObject jsonObject = JSONObject.fromObject(resultList);
                if (jsonObject.get("data")!=null){
                    JSONObject data = jsonObject.getJSONObject("data");
                    if (data.get("editcontroldata")!=null) {
                        List<Map<String,Object>> addcontroldata = (List<Map<String, Object>>) data.get("editcontroldata");
                        for (Map<String,Object> map: addcontroldata){
                            if (map.get("name")!=null&&"fk_pollutionid".equals(map.get("name").toString())){
                                map.put("filterable",true);
                                map.put("defaultfirstoption",true);
                                break;
                            }
                        }
                    }
                }
                return jsonObject;
            }else {
                return resultList;
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    private void sendToMq(String outputid){
        UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(outputid);
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
        mqMap.put("dgimn",unorganizedMonitorPointInfoVO.getDgimn());
        mqMap.put("monitorpointid",unorganizedMonitorPointInfoVO.getPkId());
        mqMap.put("fkpollutionid","");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }
    private void sendToMq(UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO){
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
        mqMap.put("dgimn",unorganizedMonitorPointInfoVO.getDgimn());
        mqMap.put("monitorpointid",unorganizedMonitorPointInfoVO.getPkId());
        mqMap.put("fkpollutionid","");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }

}
