package com.tjpu.sp.controller.environmentalprotection.systemhelp;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.systemhelp.SystemHelpCenterVO;
import com.tjpu.sp.service.environmentalprotection.systemhelp.SystemHelpCenterService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @description: 系统帮助处理类
 * @create: 2020-02-14 09:30
 * @version: V1.0
 */
@RestController
@RequestMapping("systemHelp")
public class SystemHelpController {
    @Autowired
    private SystemHelpCenterService systemHelpCenterService;

    /**
     * @Author: xsm
     * @Date: 2020/02/14 9:31
     * @Description: 自定义条件查询系统问题帮助列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getSystemHelpInfosByParamMap", method = RequestMethod.POST)
    public Object getSystemHelpInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = systemHelpCenterService.getSystemHelpInfosByParamMap(jsonObject);
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
     * @date: 2020/02/14 0014 上午 11:04
     * @Description: 新增系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addSystemHelpInfo", method = RequestMethod.POST)
    public Object addSystemHelpInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            SystemHelpCenterVO systemHelpCenterVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new SystemHelpCenterVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            systemHelpCenterVO.setUpdatetime(new Date());
            systemHelpCenterVO.setUpdateuser(username);
            systemHelpCenterVO.setPkId(UUID.randomUUID().toString());
            systemHelpCenterService.insert(systemHelpCenterVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/14 0014 上午 11:17
     * @Description: 通过id获取系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getSystemHelpInfoByID", method = RequestMethod.POST)
    public Object getSystemHelpInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            SystemHelpCenterVO systemHelpCenterVO = systemHelpCenterService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", systemHelpCenterVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/14 0014 上午 11:30
     * @Description: 修改系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateSystemHelpInfo", method = RequestMethod.POST)
    public Object updateSystemHelpInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            SystemHelpCenterVO systemHelpCenterVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new SystemHelpCenterVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            systemHelpCenterVO.setUpdateuser(username);
            systemHelpCenterVO.setUpdatetime(new Date());
            systemHelpCenterService.updateByPrimaryKey(systemHelpCenterVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/10/14 0014 上午 11:33
     * @Description: 通过id删除系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteSystemHelpInfoByID", method = RequestMethod.POST)
    public Object deleteSystemHelpInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            systemHelpCenterService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/02/14 0014 上午 10:58
     * @Description: 通过id获取系统帮助详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getSystemHelpInfoDetailByID", method = RequestMethod.POST)
    public Object getSystemHelpInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = systemHelpCenterService.getSystemHelpInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/14 0014 下午 14:09
     * @Description: 获取所有系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getAllSystemHelpInfos", method = RequestMethod.POST)
    public Object getAllSystemHelpInfos() throws Exception {
        try {
            List<Map<String, Object>> result = systemHelpCenterService.getAllSystemHelpInfos();
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
