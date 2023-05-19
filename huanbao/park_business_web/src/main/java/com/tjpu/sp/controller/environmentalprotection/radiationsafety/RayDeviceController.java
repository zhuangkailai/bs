package com.tjpu.sp.controller.environmentalprotection.radiationsafety;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.RayDeviceVO;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.SourcesVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.RayDeviceService;
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
 * @date:2019/10/22 0022 19:31
 * @Description: 射线装置信息控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("rayDevice")
public class RayDeviceController {

    @Autowired
    private RayDeviceService rayDeviceService;

    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:42
    *@Description: 通过自定义参数获取射线装置信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "getRayDeviceByParamMap",method = RequestMethod.POST)
    public Object getRayDeviceByParamMap(@RequestJson(value = "paramsjson",required = true) Object paramsJson) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") !=null && jsonObject.get("pagesize") != null){
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String,Object>> RayDeviceByParamMap=rayDeviceService.getRayDeviceByParamMap(jsonObject);
            PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(RayDeviceByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist",RayDeviceByParamMap);
            resultMap.put("total",total);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:48
    *@Description: 通过主键id删除射线装置单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deleteRayDeviceById",method = RequestMethod.POST)
    public Object deleteRayDeviceById(@RequestJson(value = "id",required = true) String id){
        try {
            rayDeviceService.deleteRayDeviceById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:51
    *@Description: 添加射线装置信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "addRayDeviceInfo",method = RequestMethod.POST)
    public Object addRayDeviceInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            RayDeviceVO rayDeviceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RayDeviceVO());
            rayDeviceVO.setPkRaydeviceid(UUID.randomUUID().toString());
            rayDeviceVO.setUpdatetime(new Date());
            rayDeviceVO.setUpdateuser(username);
            rayDeviceService.addRayDeviceInfo(rayDeviceVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:54
    *@Description: 射线装置列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getRayDeviceById",method = RequestMethod.POST)
    public Object getRayDeviceById(@RequestJson(value = "id",required = true) String id){
        try {
            RayDeviceVO rayDeviceVO =rayDeviceService.getRayDeviceById(id);
            return AuthUtil.parseJsonKeyToLower("success",rayDeviceVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:57
    *@Description: 编辑保射线装置列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updateRayDeviceInfo",method = RequestMethod.POST)
    public Object updateRayDeviceInfo(HttpServletRequest request )throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            RayDeviceVO rayDeviceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RayDeviceVO());
            rayDeviceVO.setUpdateuser(username);
            rayDeviceVO.setUpdatetime(new Date());
            rayDeviceService.updateRayDeviceInfo(rayDeviceVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:59
    *@Description: 获取射线装置详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getRayDeviceDetailById",method = RequestMethod.POST)
    public Object getRayDeviceDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=rayDeviceService.getRayDeviceDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
