package com.tjpu.sp.controller.environmentalprotection.dischargepermit;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.dischargepermit.LicenceVO;
import com.tjpu.sp.service.environmentalprotection.dischargepermit.DischargePermitService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: liyc
 * @date:2019/10/21 0021 11:50
 * @Description: 排污许可证控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("dischargePermit")
public class DischargePermitController {

    @Autowired
    private DischargePermitService dischargePermitService;
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 13:14
    *@Description: 通过自定义参数获取排污许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "/getPermitListByParamMap",method = RequestMethod.POST)
    public Object getPermitListByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") !=null && jsonObject.get("pagesize") != null){
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String,Object>> permitInfoByParamMap = dischargePermitService.getPermitListByParamMap(jsonObject);
            PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(permitInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist",permitInfoByParamMap);
            resultMap.put("total",total);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:29
    *@Description: 通过主键id删除排污许可证列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deletePermitById",method = RequestMethod.POST)
    public Object deletePermitById(@RequestJson(value = "id",required = true) String id){
        try {
            dischargePermitService.deletePermitById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:35
    *@Description: 添加排污许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "addPermitInfo",method = RequestMethod.POST)
    public Object addPermitInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            LicenceVO licenceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new LicenceVO());
            licenceVO.setPkLicenceid(UUID.randomUUID().toString());
            licenceVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            licenceVO.setUpdateuser(username);
            dischargePermitService.addPermitInfo(licenceVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:45
    *@Description: 排污许可证列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getPermitInfoById",method = RequestMethod.POST)
    public Object getPermitInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            LicenceVO licenceVO=dischargePermitService.getPermitInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",licenceVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:57
    *@Description: 编辑保存排污许可证列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updatePermitInfo",method = RequestMethod.POST)
    public Object updatePermitInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            LicenceVO licenceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new LicenceVO());
            licenceVO.setUpdateuser(username);
            licenceVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            dischargePermitService.updatePermitInfo(licenceVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 15:07
    *@Description: 获取排污许证详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getPermitDetailById",method = RequestMethod.POST)
    public Object getPermitDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=dischargePermitService.getPermitDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 14:31
    *@Description: 通过企业id获取排污许可证统计信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @RequestMapping(value = "getPWXKZLicenseByPollutionId",method = RequestMethod.POST)
    public Object getPWXKZLicenseByPollutionId(@RequestJson(value = "pollutionid",required = true) String pollutionid){
        try {
            List<Map<String,Object>> datas=dischargePermitService.getPWXKZLicenseByPollutionId(pollutionid);
            return AuthUtil.parseJsonKeyToLower("success",datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/24 24 上午 10:23
     * @Description: 根据自定义参数获取排污许可证列表信息（汇总）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getPermitListDataByParamMap", method = RequestMethod.POST)
    public Object getPermitListDataByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = (Map<String, Object>) JSONObject.fromObject(paramsjson);
            //获取总条数
            Long countall = dischargePermitService.countPermitNumDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = dischargePermitService.getPermitListDataByParamMap(paramMap);
            resultMap.put("datalist", dataList);
            resultMap.put("total", countall);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *@author: xsm
     *@date: 2022/05/24 0024 10:29
     *@Description: 排污许可证详情
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [id]
     *@throws:
     **/
    @RequestMapping(value = "getPermitDetailInfoById",method = RequestMethod.POST)
    public Object getPermitDetailInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> map = dischargePermitService.getPermitDetailInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
