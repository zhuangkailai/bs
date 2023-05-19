package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirMonitorStationVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirStationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirStationPollutantSetService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.AirEnum;


/**
 * @author: chengzq
 * @date: 2019/5/9 0009 14:18
 * @Description: 空气站污染物控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("airPollutantSet")
public class AirStationPollutantSetController {

    @Autowired
    private AirStationPollutantSetService airStationPollutantSetService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private RabbitmqController rabbitmqController;


    /** 数据源 */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    private Integer monitorpointtype= AirEnum.getCode();




    /**
     * @author: chengzq
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过空气站点id和获取空气站关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirPollutantsByAirId", method = RequestMethod.POST)
    public Object getAirPollutantsByAirId(@RequestJson(value = "outputid") String outputid ) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            params.put("userid", userid);
            params.put("sysmodel","airMonitorStation");
            params.put("queryfieldtype", "query-air");
            //微服务参数
            params.put("datasource", datasource);

            String param = AuthUtil.paramDataFormat( params);

            //查询条件数据
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = JSONObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            if(StringUtils.isNotBlank(queryData) && !"null".equals(queryData)){
                JSONObject jsonObject3 = JSONObject.fromObject(queryData);
                String querycontroldata = jsonObject3.get("querycontroldata")==null?"":jsonObject3.getString("querycontroldata");
                String queryformdata = jsonObject3.get("queryformdata")==null?"":jsonObject3.getString("queryformdata");
                resultMap.put("queryformdata",queryformdata);
                resultMap.put("querycontrolldata",querycontroldata);
            }

            //按钮数据
            Object buttonAuth = publicSystemMicroService.getUserButtonAuthInMenu(param);
            JSONObject jsonObject4 = JSONObject.fromObject(buttonAuth);
            String buttonData = jsonObject4.getString("data");
            if(StringUtils.isNotBlank(buttonData) && !"null".equals(buttonData)){
                JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
                String topbuttondata = jsonObject5.get("topbuttondata")==null?"":jsonObject5.getString("topbuttondata");
                String tablebuttondata = jsonObject5.get("tablebuttondata")==null?"":jsonObject5.getString("tablebuttondata");
                resultMap.put("topbuttondata",topbuttondata);
                resultMap.put("tablebuttondata",tablebuttondata);
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            dataList = airStationPollutantSetService.getAirPollutantsByParamMap(paramMap);
            WaterPollutantSetController.getName(dataList);
            resultMap.put("tablelistdata",dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 6:48
     * @Description: 通过空气站点id和污染物code空气站污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByAiridAndPollutantcode", method = RequestMethod.POST)
    public Object getPollutantsByAiridAndPollutantcode(
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutantcode",pollutantcode);
            List<Map<String, Object>> dataList = airStationPollutantSetService.getAirPollutantByParamMap(paramMap);
            WaterPollutantSetController.getName(dataList);
            String s = JSONArray.fromObject(dataList).toString();
            return AuthUtil.parseJsonKeyToLower("success", s.replaceAll("_","").toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:06
     * @Description:通过自定义参数新增空气站污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "addAirPollutantByParamMap", method = RequestMethod.POST)
    public Object addAirPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson )throws Exception {
        try {
            if(paramsjson!=null){

                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                AirStationPollutantSetVO airStationPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new AirStationPollutantSetVO());
                airStationPollutantSetVO.setPkDataid(UUID.randomUUID().toString());

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                airStationPollutantSetVO.setUpdatetime(new Date());
                airStationPollutantSetVO.setUpdateuser(username);
                airStationPollutantSetVO.setFkAirmonintpointid(jsonObject.getString("fk_airmonintpointid"));

                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList=new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    earlyWarningSetVO.setFkOutputid(airStationPollutantSetVO.getFkAirmonintpointid());
                    earlyWarningSetVO.setFkPollutantcode(airStationPollutantSetVO.getFkPollutantcode());
                    earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                    paramList.add(earlyWarningSetVO);
                }

                airStationPollutantSetService.insertPollutants(airStationPollutantSetVO, paramList);


                sendToMq(airStationPollutantSetVO.getFkAirmonintpointid());
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:06
     * @Description:通过自定义参数修改空气站污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "updateAirPollutantByParamMap", method = RequestMethod.POST)
    public Object updateAirPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson )throws Exception {
        try {
            if(paramsjson!=null){

                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                AirStationPollutantSetVO airStationPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new AirStationPollutantSetVO());

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                airStationPollutantSetVO.setUpdatetime(new Date());
                airStationPollutantSetVO.setUpdateuser(username);
                airStationPollutantSetVO.setFkAirmonintpointid(jsonObject.getString("fk_airmonintpointid"));

                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList=new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if(concenalarmmaxvalue!=null || concenalarmminvalue!=null || StringUtils.isNotBlank(fkAlarmlevelcode)){
                        earlyWarningSetVO.setFkOutputid(airStationPollutantSetVO.getFkAirmonintpointid());
                        earlyWarningSetVO.setFkPollutantcode(airStationPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        paramList.add(earlyWarningSetVO);
                    }
                }

                airStationPollutantSetService.updatePollutants(airStationPollutantSetVO, paramList);



                sendToMq(airStationPollutantSetVO.getFkAirmonintpointid());
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:37
     * @Description: 通过主键id，污染物code和空气站点id删除空气站污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, outputid]
     * @throws:
     */
    @RequestMapping(value = "deleteAirPollutantByParams", method = RequestMethod.POST)
    public Object deleteAirPollutantByParams(@RequestJson(value = "id") String id,@RequestJson(value = "outputid") String outputid
                                                ,@RequestJson(value = "pollutantcode") String pollutantcode)throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutantcode",pollutantcode);
            AirMonitorStationVO airmonintpoint = airMonitorStationService.getAirMonitorStationByID(outputid);

            airStationPollutantSetService.deletePollutants(id, paramMap);

            sendToMq(airmonintpoint);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void sendToMq(String outputid){
        AirMonitorStationVO airmonintpoint = airMonitorStationService.getAirMonitorStationByID(outputid);
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",monitorpointtype);
        mqMap.put("dgimn",airmonintpoint.getDgimn());
        mqMap.put("monitorpointid",airmonintpoint.getPkAirid());
        mqMap.put("fkpollutionid","");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }
    private void sendToMq(AirMonitorStationVO airmonintpoint){
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("monitorpointtype",monitorpointtype);
        mqMap.put("dgimn",airmonintpoint.getDgimn());
        mqMap.put("monitorpointid",airmonintpoint.getPkAirid());
        mqMap.put("fkpollutionid","");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }

}
