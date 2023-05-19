package com.tjpu.sp.controller.environmentalprotection.radiationsafety;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.NonSealedVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.NonSealedMaterialService;
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
 * @date:2019/10/22 0022 20:08
 * @Description: 非密封放射性物质控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("nonSealed")
public class NonSealedMaterialController {
    @Autowired
    private NonSealedMaterialService nonSealedMaterialService;

    /**
    *@author: liyc
    *@date: 2019/10/23 0023 9:48
    *@Description: 通过自定义参数获取非密封放射性物质信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "getNonSealedByParamMap",method = RequestMethod.POST)
    public Object getNonSealedByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") !=null && jsonObject.get("pagesize") != null){
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String,Object>> NonSealedByParamMap=nonSealedMaterialService.getNonSealedByParamMap(jsonObject);
            PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(NonSealedByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist",NonSealedByParamMap);
            resultMap.put("total",total);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:11
    *@Description: 通过主键id删除非密封放射性物质数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deleteNonSealedById",method = RequestMethod.POST)
    public Object deleteNonSealedById(@RequestJson(value = "id",required = true) String id){
        try {
            nonSealedMaterialService.deleteNonSealedById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:19
    *@Description: 添加非密封放射性物质列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "addNonSealedInfo",method = RequestMethod.POST)
    public Object addNonSealedInfo(HttpServletRequest request )throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            NonSealedVO nonSealedVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new NonSealedVO());
            nonSealedVO.setPkNonsealedmaterialid(UUID.randomUUID().toString());
            nonSealedVO.setUpdateuser(username);
            nonSealedVO.setUpdatetime(new Date());
            nonSealedMaterialService.addNonSealedInfo(nonSealedVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:30
    *@Description: 非密封放射性物质编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getNonSealedById",method = RequestMethod.POST)
    public Object getNonSealedById(@RequestJson(value = "id",required = true) String id){
        try {
            NonSealedVO nonSealedVO=nonSealedMaterialService.getNonSealedById(id);
            return AuthUtil.parseJsonKeyToLower("success",nonSealedVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:45
    *@Description: 编辑保存非密封放射性物质信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updateNonSealedInfo",method = RequestMethod.POST)
    public Object updateNonSealedInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            NonSealedVO nonSealedVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new NonSealedVO());
            nonSealedVO.setUpdatetime(new Date());
            nonSealedVO.setUpdateuser(username);
            nonSealedMaterialService.updateNonSealedInfo(nonSealedVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:49
    *@Description: 获取非密封放射性物质详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getNonSealedDetailById",method =RequestMethod.POST)
    public Object getNonSealedDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=nonSealedMaterialService.getNonSealedDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
