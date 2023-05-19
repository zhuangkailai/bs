package com.tjpu.sp.controller.envhousekeepers;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;
import com.tjpu.sp.service.envhousekeepers.SelfMonitorInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 自行监测要求处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("selfMonitorInfo")
public class SelfMonitorInfoController {


    @Autowired
    private SelfMonitorInfoService selfMonitorInfoService;


    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateInfo", method = RequestMethod.POST)
    public Object addOrUpdateInfo(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            SelfMonitorInfoVO selfMonitorInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new SelfMonitorInfoVO());
            selfMonitorInfoVO.setUpdatedate(new Date());
            selfMonitorInfoVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(selfMonitorInfoVO.getPkId())) {//更新操作
                selfMonitorInfoService.updateData(selfMonitorInfoVO);
            } else {//添加操作
                selfMonitorInfoVO.setPkId(UUID.randomUUID().toString());
                selfMonitorInfoService.insertData(selfMonitorInfoVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getListDataByParamMap", method = RequestMethod.POST)
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson)  {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            List<Map<String, Object>> datalist = selfMonitorInfoService.getListDataByParamMap(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据企业ID获取点位信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getOutPutByParam", method = RequestMethod.POST)
    public Object getOutPutByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                              @RequestJson(value = "pollutiontype") Integer pollutiontype ) {
        try {

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid",pollutionid);
            paramMap.put("pollutiontype",pollutiontype);
            List<Map<String, Object>> datalist=  selfMonitorInfoService.getOutPutByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @Description: 根据企业ID获取监测内容下拉内容
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getMonitorContentByPollutionId", method = RequestMethod.POST)
    public Object getMonitorContentByPollutionId(@RequestJson(value = "pollutionid") String pollutionid) {
        try {

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid",pollutionid);
            List<String> datalist=  selfMonitorInfoService.getMonitorContentByPollutionId(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteInfoById", method = RequestMethod.POST)
    public Object deleteInfoById(@RequestJson(value = "id") String id) {
        try {
            selfMonitorInfoService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
