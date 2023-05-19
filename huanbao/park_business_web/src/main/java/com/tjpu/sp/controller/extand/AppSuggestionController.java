package com.tjpu.sp.controller.extand;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.extand.AppSuggestionVO;
import com.tjpu.sp.service.extand.AppSuggestionService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @author: lip
 * @date: 2020/9/22 0022 下午 1:27
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("appSuggestion")
public class AppSuggestionController {


    @Autowired
    private AppSuggestionService appSuggestionService;


    /**
     * @author: lip
     * @date: 2020/9/22 0022 下午 1:30
     * @Description: 添加反馈意见
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addSuggestion", method = RequestMethod.POST)
    public Object addSuggestion(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            String userAccount = RedisTemplateUtil.getRedisCacheDataByToken("useraccount", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            AppSuggestionVO appSuggestionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new AppSuggestionVO());
            appSuggestionVO.setFeedbacktime(new Date());
            appSuggestionVO.setPkId(UUID.randomUUID().toString());
            appSuggestionVO.setFeedbackuser(username);
            appSuggestionVO.setFeedbackuseraccount(userAccount);
            appSuggestionService.insert(appSuggestionVO);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 15:26
     * @Description: 根据自定义参数获取app反馈意见列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:[]
     * @return:
     */
    @RequestMapping(value = "getAppSuggestionInfosByParamMap", method = RequestMethod.POST)
    public Object getAppSuggestionInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String,Object>> datalist = appSuggestionService.getAppSuggestionInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
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
     * @date: 2020/9/24 0024 下午 5:07
     * @Description: 根据id获取app反馈意见修改页面回显数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUpdatePageAppSuggestionInfoById", method = RequestMethod.POST)
    public Object getUpdatePageAppSuggestionInfoById(@RequestJson(value = "id") String id) throws Exception {
        try {
            AppSuggestionVO appSuggestionVO = appSuggestionService.getUpdatePageAppSuggestionInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", appSuggestionVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/9/24 0024 下午 5:07
     * @Description: 修改app反馈意见信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateAppSuggestion", method = RequestMethod.POST)
    public Object updateAppSuggestion(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            String userAccount = RedisTemplateUtil.getRedisCacheDataByToken("useraccount", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            AppSuggestionVO appSuggestionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new AppSuggestionVO());
            appSuggestionVO.setFeedbacktime(new Date());
            appSuggestionVO.setFeedbackuser(username);
            appSuggestionVO.setFeedbackuseraccount(userAccount);
            appSuggestionService.update(appSuggestionVO);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 16:47
     * @Description: 根据主键ID获取app反馈意见详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAppSuggestionDetailById", method = RequestMethod.POST)
    public Object getAppSuggestionDetailById( @RequestJson(value = "id", required = false) String id) {
        try {
            Map<String, Object> dataMap = appSuggestionService.getAppSuggestionDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 5:01
     * @Description: 根据ID删除app反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteAppSuggestionById", method = RequestMethod.POST)
    public Object deleteAppSuggestionById(
            @RequestJson(value = "id", required = true) String id
    ) {
        try {
            appSuggestionService.deleteAppSuggestionById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
