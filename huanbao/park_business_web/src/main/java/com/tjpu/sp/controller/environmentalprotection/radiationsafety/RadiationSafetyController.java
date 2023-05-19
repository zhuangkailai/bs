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
import com.tjpu.sp.service.environmentalprotection.radiationsafety.RadiationSafetyService;
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
 * @date:2019/10/21 0021 16:40
 * @Description: 辐射安全许可证控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("radiationSafety")
public class RadiationSafetyController {
    @Autowired
    private RadiationSafetyService radiationSafetyService;
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 16:52
    *@Description: 通过自定义参数获取辐射安全许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "/getRadiationSafetyByParamMap",method = RequestMethod.POST)
    public Object getRadiationSafetyByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") !=null && jsonObject.get("pagesize") != null){
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String,Object>> RadiationInfoByParamMap=radiationSafetyService.getRadiationSafetyByParamMap(jsonObject);
            PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(RadiationInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist",RadiationInfoByParamMap);
            resultMap.put("total",total);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:17
    *@Description: 通过主键id删除辐射安全许可证列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deleteRadiationById",method = RequestMethod.POST)
    public Object deleteRadiationById(@RequestJson(value = "id",required = true) String id){
        try {
            radiationSafetyService.deleteRadiationById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:22
    *@Description: 添加辐射安全许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "addRadiationInfo",method = RequestMethod.POST)
    public Object addRadiationInfo(HttpServletRequest request )throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            RadiationLicenceVO radiationLicenceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RadiationLicenceVO());
            radiationLicenceVO.setPkLicenceid(UUID.randomUUID().toString());
            radiationLicenceVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            radiationLicenceVO.setUpdateuser(username);
            radiationSafetyService.addRadiationInfo(radiationLicenceVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:42
    *@Description: 辐射安全许可证列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getRadiationInfoById",method = RequestMethod.POST)
    public Object getRadiationInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            RadiationLicenceVO radiationLicenceVO=radiationSafetyService.getRadiationInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",radiationLicenceVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:56
    *@Description: 编辑保存辐射安全许可证列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updateRadiationInfo",method = RequestMethod.POST)
    public Object updateRadiationInfo(HttpServletRequest request )throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            RadiationLicenceVO radiationLicenceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RadiationLicenceVO());
            radiationLicenceVO.setUpdateuser(username);
            radiationLicenceVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            radiationSafetyService.updateRadiationInfo(radiationLicenceVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 19:05
    *@Description: 获取辐射安全许证详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getRadiationDetailById",method = RequestMethod.POST)
    public Object getRadiationDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=radiationSafetyService.getRadiationDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
