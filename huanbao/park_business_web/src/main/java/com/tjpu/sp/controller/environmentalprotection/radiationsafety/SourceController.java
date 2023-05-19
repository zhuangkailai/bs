package com.tjpu.sp.controller.environmentalprotection.radiationsafety;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.RadiationLicenceVO;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.SourcesVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.SourceService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: liyc
 * @date:2019/10/22 0022 16:09
 * @Description: 放射源信息空控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("source")
public class SourceController {
    @Autowired
    private SourceService sourceService;

    /**
    *@author: liyc
    *@date: 2019/10/22 0022 16:28
    *@Description: 通过自定义参数获取放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "getSourceListByParamMap",method = RequestMethod.POST)
    public Object getSourceListByParamMap(@RequestJson(value = "paramsjson",required = true) Object paramsJson) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") !=null && jsonObject.get("pagesize") != null){
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String,Object>> SourceListByParamMap=sourceService.getSourceListByParamMap(jsonObject);
            PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(SourceListByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist",SourceListByParamMap);
            resultMap.put("total",total);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 17:20
    *@Description: 通过主键id删除放射源单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deleteSourceById",method = RequestMethod.POST)
    public Object deleteSourceById(@RequestJson(value = "id",required = true) String id){
        try {
            sourceService.deleteSourceById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 17:26
    *@Description: 添加放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "addSourceInfo",method = RequestMethod.POST)
    public Object addSourceInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            SourcesVO sourcesVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new SourcesVO());
            sourcesVO.setPkRadid(UUID.randomUUID().toString());
            sourcesVO.setUpdatetime(new Date());
            sourcesVO.setUpdateuser(username);
            sourceService.addSourceInfo(sourcesVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:04
    *@Description: 放射源列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getSourceInfoById",method = RequestMethod.POST)
    public Object getSourceInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            SourcesVO sourcesVO=sourceService.getSourceInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",sourcesVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:11
    *@Description: 编辑保存放射源列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updateSourceInfo",method = RequestMethod.POST)
    public Object updateSourceInfo(HttpServletRequest request )throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            SourcesVO sourcesVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new SourcesVO());
            sourcesVO.setUpdateuser(username);
            sourcesVO.setUpdatetime(new Date());
            sourceService.updateSourceInfo(sourcesVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:13
    *@Description: 获取放射源详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getSourceDetailById",method = RequestMethod.POST)
    public Object getSourceDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=sourceService.getSourceDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
