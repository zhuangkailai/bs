package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutPollutantSetService;
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

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.RainEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum;


/**
 * @author: chengzq
 * @date: 2019/5/9 0009 14:18
 * @Description: 废水排口污染物控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("waterPollutantSet")
public class WaterPollutantSetController {

    @Autowired
    private WaterOutPutPollutantSetService waterOutPutPollutantSetService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private RabbitmqController rabbitmqController;

    /** 数据源 */
    @Value("${spring.datasource.primary.name}")
    private String datasource;






    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过排口id获取废水排口关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterPollutantsByOutputId", method = RequestMethod.POST)
    public Object getWaterPollutantsByOutputId(@RequestJson(value = "outputid") String outputid,
                                               @RequestJson(value = "pollutanttype",required = false) String pollutanttype,
                                               @RequestJson(value = "sysmodel",required = false) String sysmodel
    ) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            params.put("userid", userid);
            if (StringUtils.isNotBlank(sysmodel)){
                params.put("sysmodel",sysmodel);
            }else {
                params.put("sysmodel","waterDirectOutlet");
            }
            params.put("queryfieldtype", "query-directwater");
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

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("datamark","1");
            paramMap.put("pollutanttype",pollutanttype);
            List<Map<String, Object>> dataList = waterOutPutPollutantSetService.getPollutantByParamMap(paramMap);
            getName(dataList);
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
     * @Description: 通过排口id和污染物code排口关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByOutputidAndPollutantcode", method = RequestMethod.POST)
    public Object getPollutantsByOutputidAndPollutantcode(
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "datamark",required = false) String pollutanttype,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutanttype",pollutanttype);
            paramMap.put("pollutantcode",pollutantcode);
            List<Map<String, Object>> dataList = waterOutPutPollutantSetService.getWaterOrRainPollutantsByParamMap(paramMap);
            getName(dataList);
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
     * @Description:通过自定义参数新增废水排口污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "addWaterPollutantByParamMap", method = RequestMethod.POST)
    public Object addWaterPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson )throws Exception {
        try {
            if(paramsjson!=null){

                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                WaterOutPutPollutantSetVO waterOutPutPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WaterOutPutPollutantSetVO());
                waterOutPutPollutantSetVO.setPkDataid(UUID.randomUUID().toString());

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                Date date = new Date();
                waterOutPutPollutantSetVO.setUpdatetime(date);
                waterOutPutPollutantSetVO.setUpdateuser(username);

                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList=new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if(concenalarmmaxvalue!=null || concenalarmminvalue!=null || StringUtils.isNotBlank(fkAlarmlevelcode)){
                        earlyWarningSetVO.setFkOutputid(waterOutPutPollutantSetVO.getFkWateroutputid());
                        earlyWarningSetVO.setFkPollutionid(waterOutPutPollutantSetVO.getFkPollutionid());
                        earlyWarningSetVO.setFkPollutantcode(waterOutPutPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        earlyWarningSetVO.setUpdatetime(date);
                        earlyWarningSetVO.setUpdateuser(username);
                        paramList.add(earlyWarningSetVO);
                    }
                }

                waterOutPutPollutantSetService.insertPollutants(waterOutPutPollutantSetVO, paramList);


                sendToMq(waterOutPutPollutantSetVO.getFkWateroutputid());
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
     * @Description:通过自定义参数修改废水排口污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "updateWaterPollutantByParamMap", method = RequestMethod.POST)
    public Object updateWaterPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson )throws Exception {
        try {
            if(paramsjson!=null){

                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                WaterOutPutPollutantSetVO waterOutPutPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WaterOutPutPollutantSetVO());

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                Date date = new Date();
                waterOutPutPollutantSetVO.setUpdatetime(date);
                waterOutPutPollutantSetVO.setUpdateuser(username);
                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList=new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if(concenalarmmaxvalue!=null || concenalarmminvalue!=null || StringUtils.isNotBlank(fkAlarmlevelcode)){
                        earlyWarningSetVO.setFkOutputid(waterOutPutPollutantSetVO.getFkWateroutputid());
                        earlyWarningSetVO.setFkPollutionid(waterOutPutPollutantSetVO.getFkPollutionid());
                        earlyWarningSetVO.setFkPollutantcode(waterOutPutPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        earlyWarningSetVO.setUpdatetime(date);
                        earlyWarningSetVO.setUpdateuser(username);
                        paramList.add(earlyWarningSetVO);
                    }
                }

                waterOutPutPollutantSetService.updatePollutants(waterOutPutPollutantSetVO, paramList);


                sendToMq(waterOutPutPollutantSetVO.getFkWateroutputid());
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
     * @Description: 通过主键id,污染物code和废水排口id删除废水排口污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, outputid]
     * @throws:
     */
    @RequestMapping(value = "deleteWaterPollutantByParams", method = RequestMethod.POST)
    public Object deleteWaterPollutantByParams(@RequestJson(value = "id") String id,@RequestJson(value = "outputid") String outputid
                                                      ,@RequestJson(value = "pollutantcode") String pollutantcode)throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutantcode",pollutantcode);
            WaterOutputInfoVO waterOutputInfoVO = waterOutPutInfoService.selectByPrimaryKey(outputid);

            waterOutPutPollutantSetService.deletePollutants(id, paramMap);

            sendToMq(waterOutputInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 下午 7:27
     * @Description: 转换名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dataList]
     * @throws:
     */
    public static void getName(List<Map<String, Object>> dataList){
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            Object monitorway = map.get("monitorway");
            Object earlywarningtype = map.get("alarmtype");
            if(monitorway!=null){
                if("在线".equals(monitorway.toString()) || "1".equals(monitorway.toString())){
                    map.put("monitorway","1");
                    map.put("monitorwayname","在线");
                }else if("手工".equals(monitorway.toString()) || "2".equals(monitorway.toString())){
                    map.put("monitorway","2");
                    map.put("monitorwayname","手工");
                }
            }
            if(earlywarningtype!=null){
                if(CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(earlywarningtype.toString())){
                    map.put("alarmtypename",CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getName());
                }else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(earlywarningtype.toString())){
                    map.put("alarmtypename",CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getName());
                }else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(earlywarningtype.toString())){
                    map.put("alarmtypename",CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getName());
                }
            }
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
