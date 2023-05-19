package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GASOutPutInfoVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @author: chengzq
 * @date: 2019/5/9 0009 14:18
 * @Description: 废气排口污染物控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("gasPollutantSet")
public class GasPollutantSetController {

    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private RabbitmqController rabbitmqController;


    /** 数据源 */
    @Value("${spring.datasource.primary.name}")
    private String datasource;



    /**
     * @author: chengzq
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过排口id和无组织标记（默认为false，为false时查询废气，为true时查询无组织废气）获取废气排口关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasPollutantsByOutputId", method = RequestMethod.POST)
    public Object getGasPollutantsByOutputId(@RequestJson(value = "outputid") String outputid,
                                             @RequestJson(value = "unorgflag",required = false) Boolean unorgflag,
                                             @RequestJson(value = "pollutanttype",required = false) String pollutanttype,
                                             @RequestJson(value = "sysmodel",required = false) String sysmodel
                                             ) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            params.put("userid", userid);
            if (StringUtils.isBlank(sysmodel)){
                sysmodel = "waterDirectOutlet";
            }
            params.put("sysmodel",sysmodel);
            params.put("queryfieldtype", "query-base");
            //微服务参数
            params.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat( params);
            //查询条件数据
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = JSONObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            if(StringUtils.isNotBlank(queryData)&& !"null".equals(queryData)){
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
            if(StringUtils.isNotBlank(buttonData)&& !"null".equals(buttonData)){
                JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
                String topbuttondata = jsonObject5.get("topbuttondata")==null?"":jsonObject5.getString("topbuttondata");
                String tablebuttondata = jsonObject5.get("tablebuttondata")==null?"":jsonObject5.getString("tablebuttondata");
                resultMap.put("topbuttondata",topbuttondata);
                resultMap.put("tablebuttondata",tablebuttondata);
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutanttype",pollutanttype);
            paramMap.put("datamark","2");
            if(unorgflag==null){
                paramMap.put("unorgflag",false);
            }else{
                paramMap.put("unorgflag",unorgflag);
            }
            dataList = gasOutPutPollutantSetService.getGasPollutantByOutputId(paramMap);
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
     * @Description: 通过排口id和污染物code废气排口污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByOutputidAndPollutantcode", method = RequestMethod.POST)
    public Object getPollutantsByOutputidAndPollutantcode(
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "unorgflag",required = false) Boolean unorgflag,
            @RequestJson(value = "pollutanttype",required = false) String pollutanttype,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("datamark","2");
            paramMap.put("pollutanttype",pollutanttype);
            if(unorgflag==null){
                paramMap.put("unorgflag",false);
            }else{
                paramMap.put("unorgflag",unorgflag);
            }
            paramMap.put("pollutantcode",pollutantcode);
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasPollutantsByOutputId(paramMap);
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
     * @Description:通过自定义参数新增废气排口污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "addGasPollutantByParamMap", method = RequestMethod.POST)
    public Object addGasPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson )throws Exception {
        try {
            if(paramsjson!=null){

                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                GasOutPutPollutantSetVO gasOutPutPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new GasOutPutPollutantSetVO());
                gasOutPutPollutantSetVO.setPkDataid(UUID.randomUUID().toString());

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                Date date = new Date();
                gasOutPutPollutantSetVO.setUpdatetime(date);
                gasOutPutPollutantSetVO.setUpdateuser(username);

                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList=new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if(concenalarmmaxvalue!=null || concenalarmminvalue!=null || StringUtils.isNotBlank(fkAlarmlevelcode)){
                        earlyWarningSetVO.setFkOutputid(gasOutPutPollutantSetVO.getFkGasoutputid());
                        earlyWarningSetVO.setFkPollutionid(gasOutPutPollutantSetVO.getFkPollutionid());
                        earlyWarningSetVO.setFkPollutantcode(gasOutPutPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        earlyWarningSetVO.setUpdatetime(date);
                        earlyWarningSetVO.setUpdateuser(username);
                        paramList.add(earlyWarningSetVO);
                    }
                }
                gasOutPutPollutantSetService.insertPollutants(gasOutPutPollutantSetVO, paramList);

                sendToMq(gasOutPutPollutantSetVO.getFkGasoutputid());
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
     * @Description:通过自定义参数修改废气排口污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "updateGasPollutantByParamMap", method = RequestMethod.POST)
    public Object updateGasPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson )throws Exception {
        try {
            if(paramsjson!=null){

                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                GasOutPutPollutantSetVO gasOutPutPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new GasOutPutPollutantSetVO());

                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
                Date date = new Date();
                gasOutPutPollutantSetVO.setUpdatetime(date);
                gasOutPutPollutantSetVO.setUpdateuser(username);
                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList=new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if(concenalarmmaxvalue!=null || concenalarmminvalue!=null || StringUtils.isNotBlank(fkAlarmlevelcode)){
                        earlyWarningSetVO.setFkOutputid(gasOutPutPollutantSetVO.getFkGasoutputid());
                        earlyWarningSetVO.setFkPollutionid(gasOutPutPollutantSetVO.getFkPollutionid());
                        earlyWarningSetVO.setFkPollutantcode(gasOutPutPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        earlyWarningSetVO.setUpdatetime(date);
                        earlyWarningSetVO.setUpdateuser(username);
                        paramList.add(earlyWarningSetVO);
                    }
                }
                int i = gasOutPutPollutantSetService.updatePollutants(gasOutPutPollutantSetVO, paramList);

                sendToMq(gasOutPutPollutantSetVO.getFkGasoutputid());
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
     * @Description: 通过主键id，污染物code和排口id删除废气排口污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, outputid]
     * @throws:
     */
    @RequestMapping(value = "deleteGasPollutantByParams", method = RequestMethod.POST)
    public Object deleteGasPollutantByParams(@RequestJson(value = "id") String id,@RequestJson(value = "outputid") String outputid
                                                    ,@RequestJson(value = "pollutantcode") String pollutantcode)throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutantcode",pollutantcode);
            GASOutPutInfoVO gasOutPutInfoVO = gasOutPutInfoService.selectByPrimaryKey(outputid);
            UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(outputid);
            gasOutPutPollutantSetService.deletePollutants(id, paramMap);
            sendToMq(gasOutPutInfoVO,unorganizedMonitorPointInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
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
    private void sendToMq(String outputid ){
        GASOutPutInfoVO gasOutPutInfoVO = gasOutPutInfoService.selectByPrimaryKey(outputid);
        UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO = outPutUnorganizedService.selectByPrimaryKey(outputid);
        if(gasOutPutInfoVO!=null){
            //发送消息到队列
            Map<String,Object> mqMap=new HashMap<>();
            mqMap.put("monitorpointtype",gasOutPutInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn",gasOutPutInfoVO.getDgimn());
            mqMap.put("monitorpointid",gasOutPutInfoVO.getPkId());
            mqMap.put("fkpollutionid",gasOutPutInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        }else{
            //发送消息到队列
            Map<String,Object> mqMap=new HashMap<>();
            mqMap.put("monitorpointtype",unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn",unorganizedMonitorPointInfoVO.getDgimn());
            mqMap.put("monitorpointid",unorganizedMonitorPointInfoVO.getPkId());
            mqMap.put("fkpollutionid",unorganizedMonitorPointInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        }
    }
    private void sendToMq(GASOutPutInfoVO gasOutPutInfoVO ,UnorganizedMonitorPointInfoVO unorganizedMonitorPointInfoVO){

        if(gasOutPutInfoVO!=null){
            //发送消息到队列
            Map<String,Object> mqMap=new HashMap<>();
            mqMap.put("monitorpointtype",gasOutPutInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn",gasOutPutInfoVO.getDgimn());
            mqMap.put("monitorpointid",gasOutPutInfoVO.getPkId());
            mqMap.put("fkpollutionid",gasOutPutInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        }else{
            //发送消息到队列
            Map<String,Object> mqMap=new HashMap<>();
            mqMap.put("monitorpointtype",unorganizedMonitorPointInfoVO.getFkMonitorpointtypecode());
            mqMap.put("dgimn",unorganizedMonitorPointInfoVO.getDgimn());
            mqMap.put("monitorpointid",unorganizedMonitorPointInfoVO.getPkId());
            mqMap.put("fkpollutionid",unorganizedMonitorPointInfoVO.getFkPollutionid());
            rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
        }
    }

}

