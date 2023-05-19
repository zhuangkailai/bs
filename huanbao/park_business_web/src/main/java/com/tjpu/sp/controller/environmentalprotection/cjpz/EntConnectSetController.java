package com.tjpu.sp.controller.environmentalprotection.cjpz;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.cjpz.EntConnectSetVO;
import com.tjpu.sp.model.environmentalprotection.cjpz.PointAddressSetVO;
import com.tjpu.sp.service.environmentalprotection.cjpz.EntConnectSetService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @description: 企业连接设置表
 * @create: 2021-01-12 11:38
 * @version: V1.0
 */
@RestController
@RequestMapping("entConnectSet")
public class EntConnectSetController {
    @Autowired
    private EntConnectSetService entConnectSetService;
    @Autowired
    private RabbitmqController rabbitmqController;

    /**
     * @Author: xsm
     * @Date: 2021/01/12 0012 上午 11:39
     * @Description: 自定义查询条件查询企业连接设置列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEntConnectSetsByParamMap", method = RequestMethod.POST)
    public Object getEntConnectSetsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = entConnectSetService.getEntConnectSetsByParamMap(jsonObject);
            long total = entConnectSetService.getEntConnectSetNumByParamMap(jsonObject);
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 上午 11:39
     * @Description: 新增企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addEntConnectSet", method = RequestMethod.POST)
    public Object addEntConnectSet(@RequestJson(value = "addformdata") Object addformdata,HttpSession session) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            EntConnectSetVO EntConnectSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntConnectSetVO());
            EntConnectSetVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            EntConnectSetVO.setUpdateuser(username);
            String entconnectsetid = UUID.randomUUID().toString();
            EntConnectSetVO.setPkId(entconnectsetid);
            JSONArray datalist = jsonObject.getJSONArray("pointaddresssetdata");
            List<PointAddressSetVO> paramList = new ArrayList<>();
            for (Object o : datalist) {
                PointAddressSetVO obj = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new PointAddressSetVO());
                obj.setFkEntconnectid(entconnectsetid);
                obj.setPkId(UUID.randomUUID().toString());
                obj.setUpdatetime(new Date());
                obj.setUpdateuser(username);
                paramList.add(obj);
            }
            entConnectSetService.insertEntityAndSetData(EntConnectSetVO,paramList);
            //推送
            sendToAQMq(EntConnectSetVO,"add");
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 上午 11:39
     * @Description: 通过id获取企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntConnectSetByID", method = RequestMethod.POST)
    public Object getEntConnectSetByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            EntConnectSetVO EntConnectSetVO = entConnectSetService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", EntConnectSetVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 上午 11:39
     * @Description: 修改企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateEntConnectSet", method = RequestMethod.POST)
    public Object updateEntConnectSet(@RequestJson(value = "updateformdata") Object updateformdata,HttpSession session) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            EntConnectSetVO EntConnectSetVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntConnectSetVO());
            EntConnectSetVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            EntConnectSetVO.setUpdateuser(username);
            JSONArray datalist = jsonObject.getJSONArray("pointaddresssetdata");
            List<PointAddressSetVO> paramList = new ArrayList<>();
            for (Object o : datalist) {
                PointAddressSetVO obj = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(o), new PointAddressSetVO());
                obj.setFkEntconnectid(EntConnectSetVO.getPkId());
                obj.setPkId(UUID.randomUUID().toString());
                obj.setUpdatetime(new Date());
                obj.setUpdateuser(username);
                paramList.add(obj);
            }
            entConnectSetService.updateEntityAndSetData(EntConnectSetVO,paramList);
            //推送
            sendToAQMq(EntConnectSetVO,"update");
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/01/12 0012 上午 11:39
     * @Description: 通过id删除企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEntConnectSetByID", method = RequestMethod.POST)
    public Object deleteEntConnectSetByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            EntConnectSetVO obj = entConnectSetService.selectByPrimaryKey(id);
            entConnectSetService.deleteByPrimaryKey(id);
            //推送
            sendToAQMq(obj,"delete");
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/01/12 0012 上午 11:39
     * @Description: 通过id获取企业连接设置详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntConnectSetDetailByID", method = RequestMethod.POST)
    public Object getEntConnectSetDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = entConnectSetService.getEntConnectSetDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 17:01
     * @Description: 根据监测类型和企业ID获取安全点位下拉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getSecurityPointTreeData", method = RequestMethod.POST)
    public Object getSecurityPointTreeData(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                           @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("monitorpointtype",monitorpointtype);
            List<Map<String, Object>> resultList = entConnectSetService.getSecurityPointTreeData(param);
            return AuthUtil.parseJsonKeyToLower("success",resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *@author: xsm
     *@date: 2021/01/13 0013 9:32
     *@Description: 通过企业采集配置ID获取该点位采集配置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [storageid]
     *@throws:
     **/
    @RequestMapping(value = "getPointAddressSetsByEntConnectSetID",method = RequestMethod.POST)
    public Object getPointAddressSetsByEntConnectSetID(@RequestJson(value = "entconnectsetid",required = true) String entconnectsetid){
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("entconnectsetid",entconnectsetid);
            List<Map<String, Object>> dataList =entConnectSetService.getPointAddressSetsByEntConnectSetID(paramMap);
            resultMap.put("tablelistdata",dataList);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void sendToAQMq(EntConnectSetVO obj, String operationtype){
        //发送消息到队列
        Map<String,Object> mqMap=new HashMap<>();
        mqMap.put("port",obj.getPort());
        mqMap.put("ip",obj.getIp());
        mqMap.put("pkid",obj.getPkId());
        mqMap.put("fkpollutionid",obj.getFkPollutionid());
        mqMap.put("operationtype",operationtype);
        rabbitmqController.sendModBusUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }
}
