package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
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
 * @Description: 其他监测点污染物控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("otherMonintPollutantSet")
public class OtherMonitorPointPollutantSetController {

    @Autowired
    private OtherMonitorPointPollutantSetService otherMonitorPointPollutantSetService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private RabbitmqController rabbitmqController;


    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: chengzq
     * @date: 2019/5/22 0022 上午 10:46
     * @Description: 通过其他监测点id和获取其他监测点关联污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOtherPollutantsByOtherId", method = RequestMethod.POST)
    public Object getOtherPollutantsByOtherId(@RequestJson(value = "outputid") String outputid, @RequestJson(value = "pollutanttype") String pollutanttype) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            params.put("userid", userid);
            //params.put("sysmodel","othermonitorpoint");
            if (pollutanttype != null) {//根据监测类型取不同的配置,
                if ("9".equals(pollutanttype)) {//9:恶臭
                    params.put("sysmodel", "stenchmonitorpoint");
                } else if ("10".equals(pollutanttype)) {
                    params.put("sysmodel", "vocmonitorpoint");//10:VOC
                } else if ("52".equals(pollutanttype)) {
                    params.put("sysmodel", "weatherMonitorPoint");//10:气象
                } else if ("33".equals(pollutanttype)) {
                    params.put("sysmodel", "microStationMonitorPoint");//10:微站
                } else if ("12".equals(pollutanttype)) {
                    params.put("sysmodel", "dustMonitorList");//12:扬尘
                } else {
                    params.put("sysmodel", "othermonitorpoint");
                }
            } else {
                params.put("sysmodel", "othermonitorpoint");
            }
            params.put("queryfieldtype", "query-othermonitorpoint");
            //微服务参数
            params.put("datasource", datasource);

            String param = AuthUtil.paramDataFormat(params);

            //查询条件数据
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = JSONObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            if (StringUtils.isNotBlank(queryData) && !"null".equals(queryData)) {
                JSONObject jsonObject3 = JSONObject.fromObject(queryData);
                String querycontroldata = jsonObject3.get("querycontroldata") == null ? "" : jsonObject3.getString("querycontroldata");
                String queryformdata = jsonObject3.get("queryformdata") == null ? "" : jsonObject3.getString("queryformdata");
                resultMap.put("queryformdata", queryformdata);
                resultMap.put("querycontrolldata", querycontroldata);
            }

            //按钮数据
            Object buttonAuth = publicSystemMicroService.getUserButtonAuthInMenu(param);
            JSONObject jsonObject4 = JSONObject.fromObject(buttonAuth);
            String buttonData = jsonObject4.getString("data");
            if (StringUtils.isNotBlank(buttonData) && !"null".equals(buttonData)) {
                JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
                String topbuttondata = jsonObject5.get("topbuttondata") == null ? "" : jsonObject5.getString("topbuttondata");
                String tablebuttondata = jsonObject5.get("tablebuttondata") == null ? "" : jsonObject5.getString("tablebuttondata");
                resultMap.put("topbuttondata", topbuttondata);
                resultMap.put("tablebuttondata", tablebuttondata);
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            paramMap.put("pollutanttype", pollutanttype);
            dataList = otherMonitorPointPollutantSetService.getOtherPollutantsByParamMap(paramMap);
            WaterPollutantSetController.getName(dataList);
            resultMap.put("tablelistdata", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 6:48
     * @Description: 通过其他监测点id和污染物code其他监测点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutantsByOtheridAndPollutantcode", method = RequestMethod.POST)
    public Object getPollutantsByOtheridAndPollutantcode(
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutanttype") String pollutanttype,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("pollutanttype", pollutanttype);
            List<Map<String, Object>> dataList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
            WaterPollutantSetController.getName(dataList);
            String s = JSONArray.fromObject(dataList).toString();
            return AuthUtil.parseJsonKeyToLower("success", s.replaceAll("_", "").toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:06
     * @Description:通过自定义参数新增其他监测点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "addOtherPollutantByParamMap", method = RequestMethod.POST)
    public Object addOtherPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            if (paramsjson != null) {
                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                OtherMonitorPointPollutantSetVO otherMonitorPointPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new OtherMonitorPointPollutantSetVO());
                otherMonitorPointPollutantSetVO.setPkDataid(UUID.randomUUID().toString());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                Date date = new Date();
                otherMonitorPointPollutantSetVO.setUpdatetime(date);
                otherMonitorPointPollutantSetVO.setUpdateuser(username);
                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList = new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    earlyWarningSetVO.setFkOutputid(otherMonitorPointPollutantSetVO.getFkOthermonintpointid());
                    earlyWarningSetVO.setFkPollutantcode(otherMonitorPointPollutantSetVO.getFkPollutantcode());
                    earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                    earlyWarningSetVO.setUpdatetime(date);
                    earlyWarningSetVO.setUpdateuser(username);
                    paramList.add(earlyWarningSetVO);
                }
                otherMonitorPointPollutantSetService.insertPollutants(otherMonitorPointPollutantSetVO, paramList);


                sendToMq(otherMonitorPointPollutantSetVO.getFkOthermonintpointid());
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
     * @Description:通过自定义参数修改其他监测点污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "updateOtherPollutantByParamMap", method = RequestMethod.POST)
    public Object updateOtherPollutantByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            if (paramsjson != null) {
                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                OtherMonitorPointPollutantSetVO otherMonitorPointPollutantSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new OtherMonitorPointPollutantSetVO());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                Date date = new Date();
                otherMonitorPointPollutantSetVO.setUpdatetime(new Date());
                otherMonitorPointPollutantSetVO.setUpdateuser(username);
                JSONArray alarmlist = jsonObject.getJSONArray("alarmlist");
                List<EarlyWarningSetVO> paramList = new ArrayList<>();
                for (Object o : alarmlist) {
                    EarlyWarningSetVO earlyWarningSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new EarlyWarningSetVO());
                    Double concenalarmmaxvalue = earlyWarningSetVO.getConcenalarmmaxvalue();
                    Double concenalarmminvalue = earlyWarningSetVO.getConcenalarmminvalue();
                    String fkAlarmlevelcode = earlyWarningSetVO.getFkAlarmlevelcode();
                    if (concenalarmmaxvalue != null || concenalarmminvalue != null || StringUtils.isNotBlank(fkAlarmlevelcode)) {
                        earlyWarningSetVO.setFkOutputid(otherMonitorPointPollutantSetVO.getFkOthermonintpointid());
                        earlyWarningSetVO.setFkPollutantcode(otherMonitorPointPollutantSetVO.getFkPollutantcode());
                        earlyWarningSetVO.setPkId(UUID.randomUUID().toString());
                        earlyWarningSetVO.setUpdatetime(date);
                        earlyWarningSetVO.setUpdateuser(username);
                        paramList.add(earlyWarningSetVO);
                    }
                }

                otherMonitorPointPollutantSetService.updatePollutants(otherMonitorPointPollutantSetVO, paramList);


                sendToMq(otherMonitorPointPollutantSetVO.getFkOthermonintpointid());
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
     * @Description: 通过主键id, 污染物code和其他监测点id删除其他监测点污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, outputid]
     * @throws:
     */
    @RequestMapping(value = "deleteOtherPollutantByParams", method = RequestMethod.POST)
    public Object deleteOtherPollutantByParams(@RequestJson(value = "id") String id, @RequestJson(value = "outputid") String outputid
            , @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", outputid);
            paramMap.put("pollutantcode", pollutantcode);
            OtherMonitorPointVO otherMonitorPointVO = otherMonitorPointService.getOtherMonitorPointByID(outputid);

            otherMonitorPointPollutantSetService.deletePollutants(id, paramMap);


            sendToMq(otherMonitorPointVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
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

    /**
     * @author: xsm
     * @date: 2020/11/17 0017 下午 4:04
     * @Description: 获取VOC因子组信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getVocPollutantFactorGroupData", method = RequestMethod.POST)
    public Object getVocPollutantFactorGroupData() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> listdata = otherMonitorPointService.getVocPollutantFactorGroupData(paramMap);
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    String name = CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(Integer.valueOf(map.get("code").toString()));
                    map.put("name", name);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/12/11 0011 上午 11:11
     * @Description: 根据因子组获取相关主要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getVocPollutantDataByFactorGroups", method = RequestMethod.POST)
    public Object getVocPollutantDataByFactorGroups(@RequestJson(value = "pollutantcategorys", required = false) List<Integer> pollutantcategorys) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pollutantcategorys", pollutantcategorys);
            List<Map<String, Object>> listdata = otherMonitorPointService.getVocPollutantDataByFactorGroups(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description:  列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/1 14:20
     */
    @RequestMapping(value = "getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam(@RequestJson(value = "outputid") String outputid,
                                     @RequestJson(value = "pollutanttype") String pollutanttype) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("outputid",outputid);
            paramMap.put("pollutanttype",pollutanttype);
            List<Map<String,Object>> resultList = otherMonitorPointPollutantSetService.getOtherPollutantByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
